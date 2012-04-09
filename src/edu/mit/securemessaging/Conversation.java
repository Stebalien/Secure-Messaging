package edu.mit.securemessaging;

import java.util.List;

public class Conversation {
    /**
     * Get the number messages starting at start.
     * @param start - the offset
     * @param number - the number of messages to get (at most).
     * @return the messages
     */
    public List<Message> getMessages(int start, int number) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get the messages in this conversation starting at start.
     * @param start - the start offset
     * @return the messages
     */
    public List<Message> getMessages(int start) {
        return getMessages(start, -1);
    }
    
    /**
     * Get the messages in this conversation.
     * @return the messages.
     */
    public List<Message> getMessages() {
        return getMessages(0, -1);
    }
    
    /**
     * Remove messages from the conversation.
     * @param messages - the messages to remove.
     */
    public void removeMessages(Message...messages) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Send a message to this conversation.
     * @param contents - the body of the message.
     * @return the message that was sent.
     */
    public Message sendMessage(String contents) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Clear all messages in the conversation.
     */
    public void clearMessages() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get the conversation's ID.
     * @return the conversation ID
     */
    public String getID() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Add a person to the conversation.
     * @param person - the person to add
     */
    public void addMember(Person person) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Remove a person from the conversation.
     * @param person - the person to remove
     */
    public void removeMember(Person person) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get the members of a conversation.
     * @return - the members of this conversation.
     */
    public List<Person> getMembers() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get the minimum trust level for this conversation.
     * @return the trust level
     */
    public TrustLevel getTrustLevel() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Determine if this is a group conversation.
     * @return
     */
    public boolean isGroupConversation() {
        // TODO: There may be a faster more direct way to do this.
        return getMembers().size() > 1;
    }
    
    /**
     * Get the status of the conversation (new messages since last read)?
     * @return
     */
    public Status getStatus() {
        throw new UnsupportedOperationException();
    }
}
