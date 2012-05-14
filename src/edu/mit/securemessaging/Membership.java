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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((conversation == null) ? 0 : conversation.hashCode());
        result = prime * result + ((person == null) ? 0 : person.hashCode());
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
        Membership other = (Membership) obj;
        if (conversation == null) {
            if (other.conversation != null)
                return false;
        } else if (!conversation.equals(other.conversation))
            return false;
        if (person == null) {
            if (other.person != null)
                return false;
        } else if (!person.equals(other.person))
            return false;
        return true;
    }
}
