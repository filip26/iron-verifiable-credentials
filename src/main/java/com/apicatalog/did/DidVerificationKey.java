package com.apicatalog.did;

import java.net.URI;

import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.ld.signature.proof.VerificationMethod;

import jakarta.json.JsonObject;

public class DidVerificationKey implements VerificationKey {

    private static final String MULTIKEY_TYPE = "Multikey";     //FIXME an absolute URI
    private static final String ED25519_VERIFICATION_KEY_2020_TYPE =  "https://w3id.org/security#Ed25519VerificationKey2020";
    private static final String X25519_KEYAGREEMENT_KEY_2020_TYPE =  "https://w3id.org/security#X25519KeyAgreementKey2020";
    
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
    public static DidVerificationKey createSignatureMethod(DidKey didKey) {

        // 1.
        final DidVerificationKey verificationMethod = new DidVerificationKey();
        verificationMethod.publicKey = didKey.getRawKey();
        
        // 4.
        verificationMethod.id = URI.create(didKey.toString() + "#" + didKey.getPublicKeyEncoded());
        
        // 5.
        String encodingType = MULTIKEY_TYPE;
        //TODO use options
        
        // 6.
        //TODO
        
        // 7.
        verificationMethod.type = encodingType;
        
        // 8.
        verificationMethod.controller = verificationMethod.id;
        
        // 9.
        if (MULTIKEY_TYPE.equals(encodingType) 
                || ED25519_VERIFICATION_KEY_2020_TYPE.equals(encodingType)) {
            verificationMethod.publicKeyMultibase = didKey.getPublicKeyEncoded();
        }
        
        // 10.
        //TODO jwk
        
        return verificationMethod;
        
    }
    
    public static VerificationMethod createEncryptionMethod(final DidKey didKey) {

        // 1.
        final DidVerificationKey verificationMethod = new DidVerificationKey();
        verificationMethod.publicKey = didKey.getRawKey();
        
        // 3.
        //TODO
        
        // 4.
        verificationMethod.id = URI.create(didKey.toString() + "#" + didKey.getPublicKeyEncoded());

        // 5.
        String encodingType = MULTIKEY_TYPE;
        //TODO use options
        
        // 6.
        //TODO
        
        // 7.
        //TODO

        // 8.
        verificationMethod.type = encodingType;
        
        // 9.
        verificationMethod.controller = verificationMethod.id;
        
        // 9.
        if (MULTIKEY_TYPE.equals(encodingType) 
                || X25519_KEYAGREEMENT_KEY_2020_TYPE.equals(encodingType)) {
            verificationMethod.publicKeyMultibase = didKey.getPublicKeyEncoded();
        }
        
        //TODO
        
        // 12
        return verificationMethod;
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
