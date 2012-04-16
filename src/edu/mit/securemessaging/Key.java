package edu.mit.securemessaging;

public class Key {
    // This should be fetched from the actual key using gpg
    private String publicKey;
    private String privateKey;
    private String fingerprint;
    
    public Key() {
        publicKey = "test";
        privateKey = "test";
        fingerprint = "8F128D022B69004E8CF31890D8549F57252BA385";
    }

    public String getPublicKey() {
        return publicKey;
    }
    
    public String getPrivateKey() {
        return privateKey;
    }
    
    public String getFingerprint() {
        return fingerprint;
    }
}
