package edu.mit.securemessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import edu.mit.securemessaging.Conversation.ConversationListener;

public class Backend {
    private static Backend INSTANCE = null;
    private final Person me;
    private final Map<String, Conversation> conversationMap = new HashMap<String, Conversation>();
    private final List<Conversation> conversationList = new ArrayList<Conversation>();
    private final Map<String, Person> contactMap = new HashMap<String, Person>();
    private final List<Person> contactList = new ArrayList<Person>();
    private final Set<InboxListener> inboxListeners = new CopyOnWriteArraySet<InboxListener>();
    private final Set<ContactsListener> contactsListeners = new CopyOnWriteArraySet<ContactsListener>();
    
    protected Backend() {
        // Set fake key and username
        me = new Person("John Doe 42", "john_doe_42", new Key(), TrustLevel.VERIFIED);
        addContact(new Person("Rob Miller", "rmiller", new Key()));
        addContact(new Person("Stephen Jones", "sjones", new Key(), TrustLevel.VERIFIED));
        addContact(new Person("Steven Allen", "steb", new Key(), TrustLevel.VERIFIED));
        addContact(new Person("Neel Sheth", "ndsheth", new Key(), TrustLevel.VERIFIED));
        addContact(new Person("Kenneth Schumacher", "drken", new Key(), TrustLevel.VERIFIED));
    }
    
    public static Backend getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Backend();
        }
        return INSTANCE;
    }
    
    /**
     * Get a list of conversations.
     * @return list of conversations
     */
    public List<Conversation> getConversations() {
        // TODO Very slow.
        return Collections.unmodifiableList(conversationList);
    }
    
    public Conversation newConversation() {
        Conversation conversation = new Conversation();
        conversationMap.put(conversation.getID(), conversation);
        conversationList.add(conversation);
        conversation.addConversationListener(new ConversationListener() {
            public void onConversationUpdated() {
                fireInboxUpdated();
            }
        });
        fireInboxUpdated();
        return conversation;
    }
   
    /**
     * Delete conversations.
     * @param conversations - the conversations to delete
     */
    public void deleteConversations(Conversation ... conversations) {
        // TODO
        throw new UnsupportedOperationException();
    }
    
    /**
     * Mark the given conversations with a status.
     * @param status
     * @param conversations
     */
    public void markConversations(Status status, Conversation ... conversations) {
        // TODO
        throw new UnsupportedOperationException();
    }
    
    public Conversation getConversation(String id) {
        return conversationMap.get(id);
    }
    
    /**
     * Get a list of contacts.
     * @return
     */
    public List<Person> getContacts() {
        return Collections.unmodifiableList(contactList);
    }
    
    public Person getContact(String id) {
        return contactMap.get(id);
    }
    
    /**
     * Add a person to the contacts.
     * @param person
     */
    public void addContact(Person person) {
        // XXX: Do this or throw error?
        if (person.getTrustLevel() == TrustLevel.UNKNOWN) {
            person.setTrustLevel(TrustLevel.KNOWN);
        }
        contactMap.put(person.getID(), person);
        contactList.add(person);
        fireContactsUpdated();
    }
    
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
     * Delete a person from the contacts.
     * @param person
     */
    public void deleteContact(Person person) {
        contactMap.remove(person.getID());
        contactList.remove(person);
        fireContactsUpdated();
    }
    
    public Person getMe() {
        return me;
    }
}
