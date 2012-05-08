package edu.mit.securemessaging;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private final static String DB = "secure-messaging.db";
    private final static int VERSION = 1;
    
    private Dao<Person, String> people;
    private Dao<Conversation, String> conversations;
    private Dao<Message, String> messages;
    private Dao<Membership, String> memberships;
    
    public DatabaseHelper(Context context) {
        super(context, DB, null, VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource con){
        try {
            TableUtils.createTable(con, Person.class);
            TableUtils.createTable(con, Message.class);
            TableUtils.createTable(con, Membership.class);
            TableUtils.createTable(con, Conversation.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource con, int oldVersion,
            int newVersion) {
        switch(oldVersion) {
            default:
        }
    }
    
    public Dao<Person, String> getPersonDao() throws SQLException {
        if (people == null) {
            people = getDao(Person.class);
        }
        return people;
    }
    
    public Dao<Membership, String> getMembershipDao() throws SQLException {
        if (memberships == null) {
            memberships = getDao(Membership.class);
        }
        return memberships;
    }
    
    public Dao<Conversation, String> getConversationDao() throws SQLException {
        if (conversations == null) {
            conversations = getDao(Conversation.class);
        }
        return conversations;
    }
    
    public Dao<Message, String> getMessageDao() throws SQLException {
        if (messages == null) {
            messages = getDao(Message.class);
        }
        return messages;
    }
    
    public void close() {
        super.close();
        people = null;
        conversations = null;
    }
}
