package edu.mit.securemessaging;

import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "message")
public class Message {
    public static enum MessageType {
        INVITE, MESSAGE, REFERRAL;
    }
    public static final String ID_FIELD = "_id";
    public static final String CONVERSATION_FIELD = "conversation_id";
    public static final String SENDER_FIELD = "sender_id";
    public static final String TIMESTAMP_FIELD = "timestamp";
    public static final String CONTENTS_FIELD = "contents";
    public static final String TYPE_FIELD = "type";
    public static final String EXTRA_FIELD = "extra";
    
    @DatabaseField(columnName = ID_FIELD, id = true)
    private final String id;
    
    @DatabaseField(canBeNull=false, columnName=TIMESTAMP_FIELD)
    private final Date timestamp;
    
    @DatabaseField(foreign = true, canBeNull=false, columnName = CONVERSATION_FIELD)
    private final Conversation conversation;
    
    @DatabaseField(foreign = true, canBeNull=false, columnName = SENDER_FIELD)
    private final Person sender;
    
    @DatabaseField(columnName = TYPE_FIELD)
    private final MessageType type;
    
    @DatabaseField(columnName = EXTRA_FIELD)
    private final String extra;
    
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
    public Message(Conversation conversation, Person sender, String contents, MessageType type, String extra) {
        this(UUID.randomUUID().toString(), conversation, sender, new Date(), contents, type, extra);
    }
    
    /**
     * Create a new message.
     * 
     * @param conversation - the messages conversation
     * @param sender - the message sender (you)
     * @param contents - the contents of the message
     */
    public Message(Conversation conversation, Person sender, String contents) {
        this(UUID.randomUUID().toString(), conversation, sender, new Date(), contents, MessageType.MESSAGE, null);
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
    public Message(String id, Conversation conversation, Person sender, Date timestamp, String contents, MessageType type, String extra) {
        this.id = id;
        this.conversation = conversation;
        this.sender = sender;
        this.contents = contents;
        this.timestamp = timestamp;
        this.extra = extra;
        this.type = type;
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
    
    public MessageType getType() {
        return type;
    }
    
    public String getExtra() {
        return extra;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Message other = (Message) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    /**
     * Serialize the message into a string.
     */
    public String toString() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("contents", contents);
            obj.put("sender_id", sender.getID());
            obj.put("sender_name", sender.getName());
            obj.put("conversation_id", conversation.getID());
            obj.put("type", type.toString());
            obj.put("extra", extra);
            return obj.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
