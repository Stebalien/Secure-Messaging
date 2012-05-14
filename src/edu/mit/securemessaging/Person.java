package edu.mit.securemessaging;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.field.*;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;


@DatabaseTable(tableName = "person")
public class Person {
    public static final String ID_FIELD = "_id";
    public static final String NAME_FIELD = "name";
    public static final String TRUST_FIELD = "trustLevel";
    
    private static Backend BACKEND = null;
    private static final String PHOTO_PATH = "%s-photo.jpeg";
    private static final String KEY_PATH = "%s-key.gpg";
    private static final int BUFFER_SIZE = 4096;
    
    private PreparedQuery<Conversation> conversationQuery = null;
    
    @DatabaseField(columnName=ID_FIELD, id=true)
    private String id;
    
    // Don't bother saving path as it is just id-photo.png
    private Bitmap photo;
    
    @DatabaseField(columnName=NAME_FIELD, unique=true, canBeNull=false)
    private String name;
    
    @DatabaseField(columnName=TRUST_FIELD, canBeNull=false)
    private TrustLevel trustLevel;
    
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
    
    public Person(String id, String name) {
        this(id, name, null, TrustLevel.UNKNOWN, null);
    }
    
    public Person(String id, String name, TrustLevel trustLevel) {
        this(id, name, null, trustLevel, null);
    }
    
    public Person(String id, String name, InputStream key) {
        this(id, name, key, TrustLevel.UNKNOWN, null);
    }
    
    public Person(String id, String name, InputStream key, TrustLevel trustLevel) {
        this(id, name, key, trustLevel, null);
    }
    
    /**
     * Set this person's photo.
     * @param photo
     */
    public void setPhoto(Bitmap photo) {
        FileOutputStream file = null;
        try {
            file = BACKEND.openFileOutput(getPhotoPath(), Context.MODE_PRIVATE);
            photo.compress(Bitmap.CompressFormat.JPEG, 90, file);
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
    
    public void setPhoto(Uri uri) {
        FileOutputStream file = null;
        try {
            file = BACKEND.openFileOutput(getPhotoPath(), Context.MODE_PRIVATE);
            photo = MediaStore.Images.Media.getBitmap(BACKEND.getContentResolver(), uri);
            photo.compress(Bitmap.CompressFormat.JPEG, 90, file);
        } catch (IOException e) {
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
            try {
                photo = BitmapFactory.decodeStream(BACKEND.openFileInput(getPhotoPath()));
            } catch (FileNotFoundException e) {
                // pass
            }
        }
        return photo;
    }
    
    private PreparedQuery<Conversation> getConversationQuery() throws SQLException {
        if (conversationQuery == null) {
            QueryBuilder<Membership, String> subQuery = BACKEND.getMembershipDao().queryBuilder();
            subQuery.selectColumns(Membership.CONVERSATION_FIELD);
            subQuery.where().eq(Membership.PERSON_FIELD, this);
            
            QueryBuilder<Conversation, String> outerQuery = BACKEND.getConversationDao().queryBuilder();
            outerQuery.where().in(Conversation.ID_FIELD, subQuery);
            outerQuery.orderBy(Conversation.TIMESTAMP_FIELD, false);
            
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
    
    public Conversation getLastConversation() {
        try {
            return BACKEND.getConversationDao().queryForFirst(getConversationQuery());
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
    
    protected String encrypt(String message) {
        return message;
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
        Person other = (Person) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
    
}
