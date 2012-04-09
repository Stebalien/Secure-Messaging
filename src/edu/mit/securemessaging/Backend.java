package edu.mit.securemessaging;

import java.util.List;

public class Backend {
    String username;
    String key;
    public Backend() {
        
    }
    
    /**
     * Get a list of at most number conversations starting at offset.
     * @param offset - the starting offset
     * @param number - the maximum number of conversations to get
     * @return list of conversations
     */
    public List<Conversation> getConversations(int offset, int number) {
        // TODO
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get a list of conversations starting at offset.
     * @param offset - the starting offset
     * @return list of conversations
     */
    public List<Conversation> getConversations(int offset) {
        return getConversations(offset, -1);
    }
    
    /**
     * Get a list of conversations.
     * @return list of conversations
     */
    public List<Conversation> getConversations() {
        return getConversations(0, -1);
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
     * Get a list of at most number contacts starting at offset.
     * @param offset
     * @param number
     * @return
     */
    public List<Person> getContacts(int offset, int number) {
        // TODO
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get a list of contacts.
     * @return
     */
    public List<Person> getContacts() {
        // TODO
        throw new UnsupportedOperationException();
    }
    
    /**
     * Add a person to the contacts.
     * @param person
     */
    public void addContact(Person person) {
        // TODO
        throw new UnsupportedOperationException();
    }
    
    /**
     * Delete a person from the contacts.
     * @param person
     */
    public void deleteContact(Person person) {
        // TODO
        throw new UnsupportedOperationException();
    }
}
