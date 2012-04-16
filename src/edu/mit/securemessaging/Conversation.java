package edu.mit.securemessaging;

import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public class Conversation {
    private final String id;
    private Date timestamp;
    private final static Backend backend = Backend.getInstance();
    
    private static final Random RAND = new Random();
    private Status status = Status.READ;
    private TrustLevel trustLevel = TrustLevel.VERIFIED;
    
    private List<Message> messageList;
    private List<Person> memberList;
    private final Set<ConversationListener> messageListListeners = new CopyOnWriteArraySet<ConversationListener>();
    private final Timer fake_reply_timer = new Timer();
    
    public Conversation() {
        id = UUID.randomUUID().toString();
        messageList = new ArrayList<Message>();
        memberList = new ArrayList<Person>();
        this.timestamp = new Date();
    }
    
    public Conversation(String id, List<Message> messages, List<Person> members, Date timestamp) {
        this.id = id;
        this.timestamp = timestamp;
        messageList = new ArrayList<Message>(messages);
        memberList = new ArrayList<Person>(members);
    }
    
    public List<Message> getMessages() {
        return Collections.unmodifiableList(messageList);
    }
    
    public void updateTrustLevel() {
        for (Person p : memberList) {
            TrustLevel memberTrust = p.getTrustLevel();
            if(memberTrust.isLowerThan(trustLevel))
                trustLevel=memberTrust;
        }
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
        //TODO: send message
        Message m = new Message(this, backend.getMe(), contents);
        addMessage(m);
        // Generate response
        final Conversation self = this;
        List<Person> members = getMembers();
        if (!members.isEmpty()) {
            final Message reply = new Message(self, members.get(RAND.nextInt(members.size())), "That's Nice.");
            fake_reply_timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    addMessage(reply);
                }
            }, 2000);
        }
        
        return m;
    }
    
    protected void addMessage(Message m) {
        messageList.add(m);
        status = Status.UNREAD;
        timestamp = m.getTimestamp();
        fireConversationUpdated();
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
        return id;
    }
    
    /**
     * Add a person to the conversation.
     * @param person - the person to add
     */
    public void addMember(Person person) {
        if (memberList.contains(person)) {
            return;
        }
        memberList.add(person);
        if (person.getTrustLevel().isLowerThan(getTrustLevel())) {
            this.trustLevel = person.getTrustLevel();
        }
        fireConversationUpdated();
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
        // Should have a more robust way of setting the status to read.
        status = Status.READ;
        return Collections.unmodifiableList(memberList);
    }
    
    /**
     * Get the minimum trust level for this conversation.
     * @return the trust level
     */
    public TrustLevel getTrustLevel() {
        return trustLevel;
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
        //throw new UnsupportedOperationException();
        return status;
    }
    
    protected void fireConversationUpdated() {
        for (ConversationListener l : messageListListeners) {
            l.onConversationUpdated();
        }
    }
    
    public void addConversationListener(ConversationListener listener) {
        messageListListeners.add(listener);
    }
    
    public void removeConversationListener(ConversationListener listener) {
        messageListListeners.remove(listener);
    }
    
    public static interface ConversationListener extends EventListener {
        public void onConversationUpdated();
    }
    
    /**
     * Gets the most recent message's timestamp
     * @return
     */
    public Date getTimestamp() {
        //returns the timestamp of the most recent message
        return timestamp;
    }
    
    public Message getLatestMessage() {
        if (messageList.size() == 0) {
            return null;
        } else {
            return messageList.get(messageList.size()-1);
        }
    }
}
