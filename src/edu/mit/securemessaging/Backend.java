package edu.mit.securemessaging;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

import edu.mit.securemessaging.DatabaseHelper;

public class Backend extends Application {
    private static final String PREFERENCES = "global";
    private static Backend INSTANCE = null;
    private DatabaseHelper db;
    private SharedPreferences preferences;
    
    private final Set<InboxListener> inboxListeners = new CopyOnWriteArraySet<InboxListener>();
    private final Set<ContactsListener> contactsListeners = new CopyOnWriteArraySet<ContactsListener>();
    private final Set<ConversationListener> conversationListeners = new CopyOnWriteArraySet<ConversationListener>();
    
    private Person me = null;
    
    private Communicator communicator;
    
    private Dao<Person, String> people;
    private Dao<Conversation, String> conversations;
    private Dao<Message, String> messages;
    private Dao<Membership, String> memberships;
    
    public Dao<Person, String> getPersonDao() throws SQLException {
        return people;
    }
    
    public Dao<Conversation, String> getConversationDao() throws SQLException {
        return conversations;
    }
    
    public Dao<Message, String> getMessageDao() throws SQLException {
        return messages;
    }
    
    public Dao<Membership, String> getMembershipDao() throws SQLException {
        return memberships;
    }
    
    public DatabaseHelper getHelper() {
        return db;
    }
    
    public Communicator getCommunicator() {
        return communicator;
    }
    
    @Override
    public void onCreate() {
        INSTANCE = this;
        preferences = getSharedPreferences(PREFERENCES, 0);
        db = new DatabaseHelper(getApplicationContext());
        //me = new Person("John Doe 42", TrustLevel.VERIFIED);
        try {
            conversations = db.getConversationDao();
            people = db.getPersonDao();
            messages = db.getMessageDao();
            memberships = db.getMembershipDao();
            String id = preferences.getString("id", null);
            if (id != null) {
                me = people.queryForId(id);
            } else {
                me = null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            if (me == null) {
                communicator = new Communicator();
            } else {
                communicator = new Communicator("FAKE KEY", me.getID());
            }
            communicator.last_check = preferences.getLong("last_update", 0);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Backend getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get a list of conversations.
     * @return list of conversations
     * @throws SQLException 
     */
    public List<Conversation> getConversations() {
        // TODO Very slow.
        try {
            return conversations.queryForAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Conversation newConversation() {
        Conversation conversation = new Conversation();
        
        try {
            conversations.create(conversation);
            conversations.refresh(conversation);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        fireInboxUpdated();
        return conversation;
    }
   
    /**
     * Delete conversations.
     * @param conversations - the conversations to delete
     */
    public void deleteConversations(Conversation ... conversations) {
        try {
            this.conversations.delete(Arrays.asList(conversations));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Mark the given conversations with a status.
     * @param status
     * @param conversations
     * @throws SQLException 
     */
    public void markConversations(Status status, Conversation ... conversations) {
        for (Conversation c : conversations) {
            if (!c.getStatus().equals(status)) {
                c.setStatus(status);
                try {
                    this.conversations.update(c);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    public Conversation getConversation(String id) {
        try {
            return conversations.queryForId(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Conversation createConversation(String id, Person ... members) throws SQLException {
        if (conversations.queryForId(id) != null) {
            throw new SQLException("Converstion exists.");
        }
        Conversation c = new Conversation(id);
        conversations.create(c);
        conversations.refresh(c);
        for (Person p : members) {
            p = people.createIfNotExists(p);
            c.addMember(p);
        }
        fireInboxUpdated();
        return c;
    }
    
    public Conversation getOrCreateConversation(String id) {
        try {
            return conversations.createIfNotExists(new Conversation(id));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Person getPerson(String id) throws SQLException {
        if (id == null) {
            throw new NullPointerException("null is not a valid person id.");
        }
        return people.queryForId(id);
    }
    
    public Person getOrCreatePerson(String id, String name) throws SQLException {
        // I would normally set the key.
        // TODO: Set key.
        return people.createIfNotExists(new Person(id, name));
    }
    
    /**
     * Get a list of contacts.
     * @return
     */
    public void fireInboxUpdated() {
        for (InboxListener l : inboxListeners) {
            l.InboxUpdated();
        }
    }
    
    public void addInboxListener(InboxListener listener) {
        inboxListeners.add(listener);
    }
    
    public void removeInboxListener(InboxListener listener) {
        inboxListeners.remove(listener);
    }
    
    public static interface InboxListener extends EventListener {
        public void InboxUpdated();
    }
    
    
    public void fireContactsUpdated() {
        for (ContactsListener l : contactsListeners) {
            l.onContactsUpdated();
        }
    }
    
    public void addContactsListener(ContactsListener listener) {
        contactsListeners.add(listener);
    }
    
    public void removeContactsListener(ContactsListener listener) {
        contactsListeners.remove(listener);
    }
    
    public static interface ContactsListener extends EventListener {
        public void onContactsUpdated();
    }
    
    /**
     * Removes a person from the contacts.
     * @param person
     * @throws SQLException 
     */
    public void deleteContact(Person person) throws SQLException {
        person.setTrustLevel(TrustLevel.UNKNOWN);
        getPersonDao().update(person);
        fireContactsUpdated();
    }
    
    /**
     * Removes a person from all conversations. 
     * @param person
     * @throws SQLException 
     */
    public void removePerson(Person person) throws SQLException {
        List<Conversation> convs = person.getConversations();
        
        DeleteBuilder<Membership, String> memberDeleteBuilder = getMembershipDao().deleteBuilder();
        memberDeleteBuilder.where().eq(Membership.PERSON_FIELD, person);
        memberDeleteBuilder.delete();
        
        // Fire updates.
        for (Conversation c : convs) {
            fireConversationUpdated(c.getID());
        }
        fireInboxUpdated();
    }
    
    /**
     * Removes a person from the contacts and removes all evidence of communication.
     * Does not remove empty conversations.
     * @param person
     * @throws SQLException 
     */
    public void forgetPerson(Person person) throws SQLException {
        DeleteBuilder<Message, String> messageDeleteBuilder = getMessageDao().deleteBuilder();
        messageDeleteBuilder.where().eq(Message.SENDER_FIELD, person);
        messageDeleteBuilder.delete();
        
        // Call this second because it updates the inbox.
        removePerson(person);
        
        person.deleteKey();
        person.deletePhoto();
        getPersonDao().delete(person);
        
        fireContactsUpdated();
    }
    
    public Person getMe() {
        return me;
    }
    
    public void addOrUpdateContact(Person person) throws SQLException {
        if (person.getTrustLevel() == TrustLevel.UNKNOWN)
            person.setTrustLevel(TrustLevel.KNOWN);
        getPersonDao().createOrUpdate(person);
        fireContactsUpdated();
    }
    
    protected void fireConversationUpdated(String id) {
        for (ConversationListener l : conversationListeners) {
            l.onConversationUpdated(id);
        }
    }
    
    public void addConversationListener(ConversationListener listener) {
        conversationListeners.add(listener);
    }
    
    public void removeConversationListener(ConversationListener listener) {
        conversationListeners.remove(listener);
    }
    
    public static interface ConversationListener extends EventListener {
        public void onConversationUpdated(String id);
    }
    
    public String sign(String message) {
        try {
            return Base64.encodeToString(message.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean register(String name, String password) throws MalformedURLException, IOException {
        try {
            if (people.queryForFirst(people.queryBuilder().where().eq(Person.NAME_FIELD, name).prepare()) != null) {
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        String id = communicator.register(name);
        me = new Person(id, name, TrustLevel.ME);
        try {
            people.create(me);
            people.refresh(me);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        Editor e = preferences.edit();
        e.putString("id", id);
        e.putString("password", password);
        e.commit();
        return true;
    }
    
    public boolean checkPassword(String password) {
        String realPassword = preferences.getString("password", null);
        return password.equals(realPassword);
    }
    
    public void sendMessage(final Message m) {
        new Thread(new Runnable() {
            public void run() {
                communicator.sendMessage(m);
            }
        }).start();
    }
    
    public void sendInvite(final Conversation c, final Person p, final String message) {
        new Thread(new Runnable() {
            public void run() {
                communicator.sendInviteAnnounce(c, p, message);
            }
        }).start();
    }
    
    // Run in current thread.
    public void catchupMessages() {
        communicator.catchupMessages();
        Editor e = preferences.edit();
        //TODO: Don't access public field.
        e.putLong("last_update", communicator.last_check);
        e.commit();
    }
    
    public void fetchMessage(int id) {
        // TODO: I really should fetch the individual message but I am lazy. And this will fail less often.
        catchupMessages();
    }
}
