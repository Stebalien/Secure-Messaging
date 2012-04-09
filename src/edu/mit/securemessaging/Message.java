package edu.mit.securemessaging;

import java.util.Date;

public class Message {
    private final String ID;
    private final String senderID;
    private final String conversationID;
    private final Date timestamp;
    private final String contents;
    

    /**
     * Create a new message.
     * 
     * @param conversation - the messages conversation
     * @param sender - the message sender (you)
     * @param contents - the contents of the message
     */
    public Message(Conversation conversation, Person sender, String contents) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Create a new message with an explicit timestamp.
     * 
     * @param conversation - the messages conversation
     * @param sender - the message sender (you)
     * @param timestamp - the time that the message was sent
     * @param contents - the contents of the message
     */
    public Message(Conversation conversation, Person sender, Date timestamp, String contents) {
        throw new UnsupportedOperationException();
    }   
    
    /**
     * Get the message's unique ID.
     * @return the id
     */
    public String getID() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get the contents of the message.
     * @return the message body
     */
    public String getContents() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get the conversation to which this message is tied.
     * @return the message's conversation
     */
    public Conversation getConversation() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get the message's sender.
     * @return the message sender
     */
    public Person getSender() {
        throw new UnsupportedOperationException();
    }

}
