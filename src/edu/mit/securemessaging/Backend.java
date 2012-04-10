package edu.mit.securemessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Backend {
    private static Backend INSTANCE = null;
    String username;
    String name;
    String key;
    
    // TODO: Make this get data from backend
    List<Person> contacts = new ArrayList<Person>();
    
    protected Backend() {
        // Set fake key and username
        setUsername("john_doe_42");
        setName("John Doe 42");
        setKey("Fake Key");
        addContact(new Person("Rob Miller", "rmiller", "fake key"));
        addContact(new Person("Stephen Jones", "", "sjones", TrustLevel.VERIFIED));
        addContact(new Person("Steven Allen", "steb", "fake key", TrustLevel.VERIFIED));
        addContact(new Person("Neel Sheth", "ndsheth", "fake key", TrustLevel.VERIFIED));
        addContact(new Person("Kenneth Schumacher", "drken", "fake key", TrustLevel.VERIFIED));
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
        // TODO
        throw new UnsupportedOperationException();
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
    
    /**
     * Get a list of contacts.
     * @return
     */
    public List<Person> getContacts() {
        return Collections.unmodifiableList(contacts);
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
        contacts.add(person);
    }
    
    /**
     * Delete a person from the contacts.
     * @param person
     */
    public void deleteContact(Person person) {
        contacts.remove(person);
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public String getKey() {
        return key;
    }
}
