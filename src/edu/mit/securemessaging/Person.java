package edu.mit.securemessaging;

import android.graphics.Picture;

public class Person {
    
    /**
     * Set this person's photo.
     * @param photo
     */
    public void setPhoto(Picture photo) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get this persons photo. Returns a filler photo if none is set.
     * @return
     */
    public Picture getPhoto() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get this person's name.
     * @return
     */
    public String getName() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get this person's trust level.
     * @return
     */
    public TrustLevel getTrustLevel() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Set this person's trust level.
     * @param trust
     */
    public void setTrustLevel(TrustLevel trust) {
        throw new UnsupportedOperationException();
    }
}
