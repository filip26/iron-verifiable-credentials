package com.apicatalog.ld.signature.key;

public interface Key {

    /**
     * An identifier of the key curve.
     * 
     * @return a key type
     */
    String curve();
    
    /**
     * A raw byte array representing the key.
     * 
     * @return a raw byte array
     */
    byte[] bytes();
}
