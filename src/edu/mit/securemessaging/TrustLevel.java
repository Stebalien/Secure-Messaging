package edu.mit.securemessaging;

public enum TrustLevel {
    UNKNOWN, KNOWN, VERIFIED, ME;
    
    public boolean isHigherThan(TrustLevel other) {
        return this.compareTo(other) > 0;
    }
    
    public boolean isLowerThan(TrustLevel other) {
        return this.compareTo(other) < 0;
    }
}
