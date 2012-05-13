package edu.mit.securemessaging;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "message")
public class Message {
    public static final String ID_FIELD = "_id";
    public static final String CONVERSATION_FIELD = "conversation_id";
    public static final String SENDER_FIELD = "sender_id";
    public static final String TIMESTAMP_FIELD = "timestamp";
    public static final String CONTENTS_FIELD = "contents";
    
    @DatabaseField(columnName = ID_FIELD, id = true)
    private final String id;
    
    @DatabaseField(canBeNull=false, columnName=TIMESTAMP_FIELD)
    private final Date timestamp;
    
    @DatabaseField(foreign = true, canBeNull=false, columnName = CONVERSATION_FIELD)
    private final Conversation conversation;
    
    @DatabaseField(foreign = true, canBeNull=false, columnName = SENDER_FIELD)
    private final Person sender;
    
    @DatabaseField(canBeNull=false, columnName=CONTENTS_FIELD)
    private final String contents;
    
    public Message() {
        // For ORMLite
        this(null, null, null);
    }
    

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
