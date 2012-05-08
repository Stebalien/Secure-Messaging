package edu.mit.securemessaging;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.EventListener;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import com.j256.ormlite.field.*;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


@DatabaseTable(tableName = "person")
public class Person {
    public static final String ID_FIELD = "_id";
    public static final String NAME_FIELD = "name";
    public static final String TRUST_FIELD = "trustLevel";
    
    private static Backend BACKEND = null;
    private static final String USER_DIR = "users/%s/";
    private static final String PHOTO_PATH = USER_DIR + "photo.png";
    private static final String KEY_PATH = USER_DIR + "key.gpg";
    private static final int BUFFER_SIZE = 4096;
    
    private PreparedQuery<Conversation> conversationQuery = null;
    
    @DatabaseField(columnName=ID_FIELD, id=true)
    private String id;
    
    // Don't bother saving path as it is just users/id/photo.png
    private Bitmap photo;
    
    @DatabaseField(columnName=NAME_FIELD, unique=true, canBeNull=false)
    private String name;
    
    @DatabaseField(columnName=TRUST_FIELD, canBeNull=false)
    private TrustLevel trustLevel;
    
    private Set<PersonListener> personListeners = new CopyOnWriteArraySet<PersonListener>();
    
    Person() {
        if (BACKEND == null) BACKEND = Backend.getInstance();
    }
    
    public Person(String id, String name, InputStream key, TrustLevel trustLevel, Bitmap photo) {
        this();
        // Check null
        if (trustLevel == null) {
            throw new NullPointerException("'trustLevel' cannot be null.");
        }
        if (id == null) {
            throw new NullPointerException("'id' cannot be null.");
        }
        if (name == null) {
            throw new NullPointerException("'name' cannot be null.");
        }
        
        this.id = id;
        this.name = name;
        this.trustLevel = trustLevel;
        
        if (key != null) {
            this.setKey(key);
        }
        if (photo != null) {
            this.setPhoto(photo);
        }
    }
    
    public Person(String name) {
        this(UUID.randomUUID().toString(), name, null, TrustLevel.UNKNOWN, null);
    }
    
    public Person(String name, TrustLevel trustLevel) {
        this(UUID.randomUUID().toString(), name, null, trustLevel, null);
    }
    
    public Person(String name, InputStream key) {
        this(UUID.randomUUID().toString(), name, key, TrustLevel.UNKNOWN, null);
    }
    
    public Person(String name, InputStream key, TrustLevel trustLevel) {
        this(UUID.randomUUID().toString(), name, key, trustLevel, null);
    }
    
    /**
     * Set this person's photo.
     * @param photo
     */
    public void setPhoto(Bitmap photo) {
        FileOutputStream file = null;
        try {
            file = BACKEND.openFileOutput(getPhotoPath(), Context.MODE_PRIVATE);
            photo.compress(Bitmap.CompressFormat.PNG, 90, file);
            this.photo = photo;
        } catch (FileNotFoundException e) {
            // This should not hapen (we are writing a file).
            throw new RuntimeException(e);
        } finally {
            if (file != null) {
                try { file.close(); } catch (IOException e) { }
            }
        }
    }
    
    public String getPhotoPath() {
        return String.format(PHOTO_PATH, id);
    }
    
    /**
     * Get this persons photo. Returns null if no photo is set.
     * @return
     */
    public Bitmap getPhoto() {
        // TODO: Set external resource.
        if (photo == null) {
            photo = BitmapFactory.decodeFile(getPhotoPath());
        }
        return photo;
    }
    
    private PreparedQuery<Conversation> getConversationQuery() throws SQLException {
        if (conversationQuery == null) {
            QueryBuilder<Membership, String> subQuery = BACKEND.getMembershipDao().queryBuilder();
            subQuery.selectColumns(Membership.PERSON_FIELD);
            subQuery.where().eq(Membership.CONVERSATION_FIELD, this);
            
            QueryBuilder<Conversation, String> outerQuery = BACKEND.getConversationDao().queryBuilder();
            outerQuery.where().in(Conversation.ID_FIELD, subQuery);
            
            conversationQuery = outerQuery.prepare();
        }
        return conversationQuery;
    }
    
    /**
     * Get the conversations in which this user is participating.
     * @return - the members of this conversation.
     */
    public List<Conversation> getConversations()  {
        try {
            return BACKEND.getConversationDao().query(getConversationQuery());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Set this person's encryption key.
     * @param key
     */
    public void setKey(InputStream key) {
        FileOutputStream file;
        try {
            file = BACKEND.openFileOutput(getKeyPath(), Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        
        
        try {
            byte buffer[] = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = key.read(buffer)) != 0) {
                file.write(buffer, 0, read);
            }
        } catch (IOException e) {
            deleteKey();
            throw new RuntimeException(e);
        } finally {
            try { file.close(); } catch (IOException e) { }
        }
    }
    
    public boolean deleteKey() {
        return BACKEND.deleteFile(getKeyPath());
    }
    
    public boolean deletePhoto() {
        return BACKEND.deleteFile(getPhotoPath());
    }
    
    public boolean hasPhoto() {
        return (photo != null) || BACKEND.getFileStreamPath(getPhotoPath()).exists();
    }
    
    public String getKeyPath() {
        return String.format(KEY_PATH, id);
    }
    
    /**
     * Get this person's encryption key
     * @return
     * @throws FileNotFoundException 
     */
    public InputStream getKey() throws FileNotFoundException {
        return BACKEND.openFileInput(getKeyPath());
    }
    
    public boolean hasKey() {
        return BACKEND.getFileStreamPath(getKeyPath()).exists();
    }
    
    
    /**
     * Set this person's name
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get this person's name.
     * @return
     */
    public String getName() {
        if (name == null) refresh();
        return name;
    }
    
    public void refresh() {
        try {
            BACKEND.getPersonDao().refresh(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Get this person's trust level.
     * @return
     */
    public TrustLevel getTrustLevel() {
        if (trustLevel == null) refresh();
        return trustLevel;
    }
    
    /**
     * Set this person's trust level.
     * @param trust
     */
    public void setTrustLevel(TrustLevel trust) {
        if (trust == null) {
            throw new NullPointerException("Trust cannot be null.");
        }
        this.trustLevel = trust;
    }
    
    public String getID() {
        return id;
    }
    
    protected void firePersonUpdated() {
        for (PersonListener l : personListeners) {
            l.onPersonUpdated();
        }
    }
    
    public void addPersonListener(PersonListener listener) {
        personListeners.add(listener);
    }
    
    public void removePersonListener(PersonListener listener) {
        personListeners.remove(listener);
    }
    
    // Unused for now.
    public static interface PersonListener extends EventListener {
        public void onPersonUpdated();
    }
}
