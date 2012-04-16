package edu.mit.securemessaging;

import java.util.EventListener;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import android.graphics.Bitmap;


public class Person {
    private Bitmap photo;
    private Key key;
    private String name;
    private String username;
    private TrustLevel trustLevel;
    private Set<PersonListener> personListeners = new CopyOnWriteArraySet<PersonListener>();
    
    public Person(String name, String username, Key key, TrustLevel trust, Bitmap photo) {
        this.name = name;
        this.username = username;
        this.key = key;
        this.trustLevel = trust;
        this.photo = photo;
    }
    
    public Person(String name, String username, Key key) {
        this(name, username, key, TrustLevel.UNKNOWN, null);
    }
    
    public Person(String name, String username, Key key, TrustLevel trust) {
        this(name, username, key, trust, null);
    }
    
    /**
     * Set this person's photo.
     * @param photo
     */
    public void setPhoto(Bitmap photo) {
        // TODO: Get external resource.
        this.photo = photo;
    }
    
    /**
     * Get this persons photo. Returns null if no photo is set.
     * @return
     */
    public Bitmap getPhoto() {
        // TODO: Set external resource.
        return photo;
    }
    
    /**
     * Set this person's encryption key.
     * @param key
     */
    public void setKey(Key key) {
        this.key = key;
    }
    
    /**
     * Get this person's encryption key
     * @return
     */
    public Key getKey() {
        return key;
    }
    
    
    /**
     * Set this person's name
     * @param name
     */
    public void setName(String name) {
        // TODO: Set external resource.
        this.name = name;
    }
    
    /**
     * Get this person's name.
     * @return
     */
    public String getName() {
        // TODO: Get external resource.
        return name;
    }
    
    /**
     * Get this person's trust level.
     * @return
     */
    public TrustLevel getTrustLevel() {
        // TODO: Get external resource.
        return trustLevel;
    }
    
    /**
     * Set this person's trust level.
     * @param trust
     */
    public void setTrustLevel(TrustLevel trust) {
        // TODO: Set external resource.
        this.trustLevel = trust;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getID() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    protected void firePersonUpdated() {
        for (PersonListener l : personListeners) {
            l.onPersonUpdated();
        }
    }
    
    public void addConversationListener(PersonListener listener) {
        personListeners.add(listener);
    }
    
    public void removeConversationListener(PersonListener listener) {
        personListeners.remove(listener);
    }
    
    // Unused for now.
    public static interface PersonListener extends EventListener {
        public void onPersonUpdated();
    }
}
