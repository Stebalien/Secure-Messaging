package edu.mit.securemessaging;

import android.graphics.Bitmap;


public class Person {
    private Bitmap photo;
    private String key;
    private String name;
    private TrustLevel trustLevel;
    
    public Person(String name, String key, TrustLevel trust, Bitmap photo) {
        this.name = name;
        this.key = key;
        this.trustLevel = trust;
        this.photo = photo;
    }
    
    public Person(String name, String key) {
        this(name, key, TrustLevel.UNKNOWN, null);
    }
    
    public Person(String name, String key, TrustLevel trust) {
        this(name, key, trust, null);
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
    public void setKey(String key) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get this person's encryption key
     * @return
     */
    public String getKey() {
        throw new UnsupportedOperationException();
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
}
