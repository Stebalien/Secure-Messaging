package edu.mit.securemessaging;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import android.app.Application;

import edu.mit.securemessaging.DatabaseHelper;

public class Backend extends Application {
    private static Backend INSTANCE = null;
    private static String MY_ID = "ABCDE"; // This will be stored in preferences later
    private DatabaseHelper db;
    
    private final Set<InboxListener> inboxListeners = new CopyOnWriteArraySet<InboxListener>();
    private final Set<ContactsListener> contactsListeners = new CopyOnWriteArraySet<ContactsListener>();
    
    private Person me;
    
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
    
    @Override
    public void onCreate() {
        INSTANCE = this;
        db = new DatabaseHelper(getApplicationContext());
        //me = new Person("John Doe 42", TrustLevel.VERIFIED);
        try {
            conversations = db.getConversationDao();
            people = db.getPersonDao();
            messages = db.getMessageDao();
            memberships = db.getMembershipDao();
            me = people.queryForId(MY_ID);
            if (me == null) {
                me = new Person(MY_ID, "John Doe 42", null, TrustLevel.ME, null);
                // TODO: Create account here.
                people.create(me);
                people.refresh(me);
            }
        } catch (SQLException e) {
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
    
    public Person getPerson(String id) throws SQLException {
        return people.queryForId(id);
    }
    
    /**
     * Get a list of contacts.
     * @return
     */
    protected void fireInboxUpdated() {
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
    
    
    protected void fireContactsUpdated() {
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
        DeleteBuilder<Membership, String> memberDeleteBuilder = getMembershipDao().deleteBuilder();
        memberDeleteBuilder.where().eq(Membership.PERSON_FIELD, person);
        memberDeleteBuilder.delete();
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
    
}
