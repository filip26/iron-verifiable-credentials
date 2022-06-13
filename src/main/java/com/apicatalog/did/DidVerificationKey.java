package com.apicatalog.did;

import java.net.URI;

import com.apicatalog.ld.signature.key.VerificationKey;

import jakarta.json.JsonObject;

public class DidVerificationKey implements VerificationKey {

    private static final String MULTIKEY_TYPE = "Multikey";     //FIXME an absolute URI
    private static final String ED25519_VERIFICATION_KEY_2020_TYPE =  "https://w3id.org/security#Ed25519VerificationKey2020";
    
    private URI id;
    
    private String type;
    
    private URI controller;
    
    private byte[] publicKey;
    
    private String publicKeyMultibase;
    
    protected DidVerificationKey() {
        
    }

    /**
     * Creates a new verification key by expading the given DID key.
     * 
     * see {@link https://pr-preview.s3.amazonaws.com/w3c-ccg/did-method-key/pull/51.html#signature-method-creation-algorithm}
     * 
     * @return The new verification key
     */
    public static DidVerificationKey expand(DidKey didKey) {
        
        final DidVerificationKey verificationKey = new DidVerificationKey();
        
        // 4.
        verificationKey.id = URI.create(didKey.toString() + "#" + didKey.getPublicKeyEncoded());
        
        // 5.
        String encodingType = MULTIKEY_TYPE;
        //TODO use options
        
        // 6.
        //TODO
        
        // 7.
        verificationKey.type = encodingType;
        
        // 8.
        verificationKey.controller = verificationKey.id;
        
        verificationKey.publicKey = didKey.getRawKey();
        
        // 9.
        if (MULTIKEY_TYPE.equals(encodingType) 
                || ED25519_VERIFICATION_KEY_2020_TYPE.equals(encodingType)) {
            verificationKey.publicKeyMultibase = didKey.getPublicKeyEncoded();
        }
        
        // 10.
        //TODO jwk
        
        return verificationKey;
        
    }

    
    @Override
    public URI getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public URI getController() {
        return controller;
    }

    @Override
    public JsonObject toJson() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] getPublicKey() {
        return publicKey;
    }
    
    public String getPublicKeyMultibase() {
        return publicKeyMultibase;
    }

}
