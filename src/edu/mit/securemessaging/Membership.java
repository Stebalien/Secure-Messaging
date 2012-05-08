package edu.mit.securemessaging;


import com.j256.ormlite.field.*;
import com.j256.ormlite.table.*;

@DatabaseTable(tableName = "membership")
public class Membership {
    public static final String ID_FIELD = "_id";
    public static final String CONVERSATION_FIELD = "conversation_id";
    public static final String PERSON_FIELD = "person_id";
    
    @DatabaseField(columnName=ID_FIELD, generatedId=true)
    private int id;
    
    @DatabaseField(columnName = CONVERSATION_FIELD, uniqueCombo=true, foreign=true)
    private Conversation conversation;
    
    @DatabaseField(columnName = PERSON_FIELD, uniqueCombo=true, foreign=true)
    private Person person;
    
    Membership() { }
    
    public Membership(Conversation conversation, Person person) {
        this.person = person;
        this.conversation = conversation;
    }
    
    public int getID() {
        return id;
    }
    
    public Conversation getConversation() {
        return conversation;
    }
    
    public Person getPersion() {
        return person;
    }
    
}
