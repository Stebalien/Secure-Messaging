package edu.mit.securemessaging;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "conversation")
public class Conversation {
    private static final Random RAND = new Random();
    private static Backend BACKEND;
    
    public static final String ID_FIELD = "_id";
    public static final String TIMESTAMP_FIELD = "timestamp";
    public static final String STATUS_FIELD = "status";
    
    
    
    // Stores the conversation's ID
    @DatabaseField(columnName = ID_FIELD, id = true)
    private final String id;
    
    // Caches the timestamp of the last received message.
    @DatabaseField(columnName = TIMESTAMP_FIELD)
    private Date timestamp;
    
    // Caches the read status of the conversation
    @DatabaseField(columnName = STATUS_FIELD)
    private Status status = Status.READ;
    
    @ForeignCollectionField(eager = false, orderColumnName = "timestamp")
    private ForeignCollection<Message> messages;
    
    @ForeignCollectionField(eager = true)
    private ForeignCollection<Membership> memberships;
    
    private final Timer fake_reply_timer = new Timer();
    
    private PreparedQuery<Person> personQuery = null;
    private PreparedQuery<Person> trustLevelQuery = null;
    private PreparedQuery<Membership> membershipQuery = null;
    private PreparedQuery<Message> lastMessageQuery = null;
    
    public Conversation() {
        this(UUID.randomUUID().toString());
    }
    
    public Conversation(String id) {
        this.id = id;
        this.timestamp = new Date();
        if (BACKEND == null) BACKEND = Backend.getInstance();
    }
    
    
    public Collection<Message> getMessages() {
        return Collections.unmodifiableCollection(messages);
    }
    
    public PreparedQuery<Message> getLastMessageQuery() throws SQLException {
        if (lastMessageQuery == null) {
            QueryBuilder<Message, String> qBuilder = BACKEND.getMessageDao().queryBuilder();
            qBuilder.where().eq(Message.CONVERSATION_FIELD, this);
            qBuilder.orderBy(Message.TIMESTAMP_FIELD, false).limit((long)1);
            lastMessageQuery = qBuilder.prepare();
        }
        return lastMessageQuery;
        
    }
    
    public PreparedQuery<Person> getPersonQuery() throws SQLException {
        if (personQuery == null) {
            QueryBuilder<Membership, String> subQuery = BACKEND.getMembershipDao().queryBuilder();
            subQuery.selectColumns(Membership.PERSON_FIELD);
            subQuery.where().eq(Membership.CONVERSATION_FIELD, this);
            
            QueryBuilder<Person, String> outerQuery = BACKEND.getPersonDao().queryBuilder();
            outerQuery.where().in(Person.ID_FIELD, subQuery);
            
            personQuery = outerQuery.prepare();
        }
        return personQuery;
    }
    
    public PreparedQuery<Person> getTrustLevelQuery() throws SQLException {
        if (trustLevelQuery == null) {
            
            QueryBuilder<Membership, String> subQuery = BACKEND.getMembershipDao().queryBuilder();
            subQuery.selectColumns(Membership.PERSON_FIELD);
            subQuery.where().eq(Membership.CONVERSATION_FIELD, this);
            
            QueryBuilder<Person, String> outerQuery = BACKEND.getPersonDao().queryBuilder();
            outerQuery.selectColumns(Person.TRUST_FIELD).groupBy(Person.TRUST_FIELD);
            outerQuery.where().in(Person.ID_FIELD, subQuery);
            
            trustLevelQuery = outerQuery.prepare();
        }
        return trustLevelQuery;
    }
    
    public PreparedQuery<Membership> getMembershipQuery(Person person) throws SQLException {
        if (membershipQuery == null) {
            membershipQuery = BACKEND.getMembershipDao().queryBuilder().where().eq(Membership.CONVERSATION_FIELD, this).eq(Membership.PERSON_FIELD, new SelectArg(person)).prepare();
            return membershipQuery;
        } else {
            membershipQuery.setArgumentHolderValue(0, person);
            return membershipQuery;
        }
        
    }
    
    /**
     * Get the members of a conversation.
     * @return - the members of this conversation.
     */
    public List<Person> getMembers()  {
        try {
            return BACKEND.getPersonDao().query(getPersonQuery());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public TrustLevel getTrustLevel() {
        try {
            TrustLevel trustLevel = TrustLevel.ME; // Initialize to me only.
            for (Person p : BACKEND.getPersonDao().query(getTrustLevelQuery())) {
                TrustLevel memberTrust = p.getTrustLevel();
                if(memberTrust.isLowerThan(trustLevel))
                    trustLevel=memberTrust;
            }
            return trustLevel;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Remove messages from the conversation.
     * @param messages - the messages to remove.
     */
    public void removeMessages(Message...messages) {
        this.messages.removeAll(Arrays.asList(messages));
        try {
            for (Message m : messages) {
                    this.messages.update(m);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Send a message to this conversation.
     * @param contents - the body of the message.
     * @return the message that was sent.
     * @throws SQLException 
     */
    public Message sendMessage(String contents) throws SQLException {
        //TODO: send message
        Message m = new Message(this, BACKEND.getMe(), contents);
        addMessage(m);
        // Generate response
        // Slow but good for debugging.
        final Conversation self = this;
        List<Person> members = getMembers();
        if (!members.isEmpty()) {
            final Message reply = new Message(self, members.get(RAND.nextInt(members.size())), "That's Nice.");
            fake_reply_timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        addMessage(reply);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 2000);
        }
        
        return m;
    }
    
    protected void addMessage(Message m) throws SQLException {
        messages.add(m);
        messages.update(m);
        status = Status.UNREAD;
        timestamp = m.getTimestamp();
        update();
        BACKEND.fireConversationUpdated(this.id);
        BACKEND.fireInboxUpdated();
    }
    
    protected void addMessages(Collection<Message> messages) throws SQLException {
        this.messages.addAll(messages);
        for (Message m : messages) {
            this.messages.update(m);
        }
        
        status = Status.UNREAD;
        timestamp = getLatestMessage().getTimestamp();
        update();
        BACKEND.fireConversationUpdated(this.id);
        BACKEND.fireInboxUpdated();
    }
    
    /**
     * Clear all messages in the conversation.
     */
    public void clearMessages() {
        messages.clear();
        try {
            messages.updateAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Get the conversation's ID.
     * @return the conversation ID
     */
    public String getID() {
        return id;
    }
    
    /**
     * Add a person to the conversation.
     * @param person - the person to add
     */
    public boolean addMember(Person person) {
        Membership m = new Membership(this, person);
        try {
            if (!BACKEND.getMembershipDao().queryForMatching(m).isEmpty()) {
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        try {
            this.memberships.add(m);
            this.memberships.update(m);
        } catch (SQLException e) {
            return false;
        }
        BACKEND.fireConversationUpdated(this.id);
        BACKEND.fireInboxUpdated();
        return true;
    }
    
    /**
     * Remove a person from the conversation.
     * @param person - the person to remove
     */
    public boolean removeMember(Person person) {
        try {
            Membership m = BACKEND.getMembershipDao().queryForFirst(getMembershipQuery(person));
            memberships.remove(m);
            memberships.update(m);
        } catch (SQLException e) {
            return false;
        }
        BACKEND.fireConversationUpdated(this.id);
        BACKEND.fireInboxUpdated();
        return true;
    }
    
    /**
     * Determine if this is a group conversation.
     * @return
     */
    public boolean isGroupConversation() {
        CloseableIterator<Membership> i = null;
        try {
            i = this.memberships.closeableIterator();
            if (i.hasNext()) {
                i.moveToNext();
                return i.hasNext();
            }
            return false;
        } finally {
            try {
                i.close();
            } catch (SQLException e) { }
        }
    }
    
    /**
     * Get the status of the conversation (new messages since last read)?
     * @return
     */
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        if (!this.status.equals(status)) {
            this.status = status;
        }
    }
    
    /**
     * Gets the most recent message's timestamp
     * @return
     */
    public Date getTimestamp() {
        //returns the timestamp of the most recent message
        return timestamp;
    }
    
    
    public Message getLatestMessage() throws SQLException {
        return BACKEND.getMessageDao().queryForFirst(getLastMessageQuery());
    }
    
    public void update() throws SQLException {
        BACKEND.getConversationDao().update(this);
    }
}
