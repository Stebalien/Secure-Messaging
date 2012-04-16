package edu.mit.securemessaging;

import java.util.Date;
import java.util.UUID;

public class Message {
    private final String id;
    private final Date timestamp;
    private final Conversation conversation;
    private final Person sender;
    private final String contents;
    

    /**
     * Create a new message.
     * 
     * @param conversation - the messages conversation
     * @param sender - the message sender (you)
     * @param contents - the contents of the message
     */
    public Message(Conversation conversation, Person sender, String contents) {
        this(UUID.randomUUID().toString(), conversation, sender, new Date(), contents);
    }

    /**
     * Create a new message with an explicit timestamp.
     * 
     * @param id - the message's ID.
     * @param conversation - the messages conversation
     * @param sender - the message sender (you)
     * @param timestamp - the time that the message was sent
     * @param contents - the contents of the message
     */
    public Message(String id, Conversation conversation, Person sender, Date timestamp, String contents) {
        this.id = id;
        this.conversation = conversation;
        this.sender = sender;
        this.contents = contents;
        this.timestamp = timestamp;
    }   
    
    /**
     * Get the message's unique ID.
     * @return the id
     */
    public String getID() {
        return id;
    }
    
    /**
     * Get the contents of the message.
     * @return the message body
     */
    public String getContents() {
        return contents;
    }
    
    /**
     * Get the conversation to which this message is tied.
     * @return the message's conversation
     */
    public Conversation getConversation() {
        return conversation;
    }
    
    /**
     * Get the message's sender.
     * @return the message sender
     */
    public Person getSender() {
        return sender;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
}
