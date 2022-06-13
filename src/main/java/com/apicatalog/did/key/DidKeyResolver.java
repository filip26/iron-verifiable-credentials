package com.apicatalog.did.key;

import com.apicatalog.did.Did;
import com.apicatalog.did.DidDocument;
import com.apicatalog.did.DidResolver;
import com.apicatalog.did.DidUrl;
import com.apicatalog.ld.signature.proof.VerificationMethod;

public class DidKeyResolver implements DidResolver {

    private static final String MULTIKEY_TYPE = "Multikey";     //FIXME an absolute URI
    private static final String ED25519_VERIFICATION_KEY_2020_TYPE =  "https://w3id.org/security#Ed25519VerificationKey2020";
    private static final String X25519_KEYAGREEMENT_KEY_2020_TYPE =  "https://w3id.org/security#X25519KeyAgreementKey2020";

    protected DidKeyResolver() {
    }

    @Override
    public DidDocument resolve(Did did) {

        if (!DidKey.isDidKey(did)) {
            throw new IllegalArgumentException();
        }
        
        return null;
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
        verificationMethod.id = DidUrl.from(didKey, null, null,  didKey.getMethodSpecificId());
        
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
            verificationMethod.publicKeyMultibase = didKey.getMethodSpecificId();
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
        verificationMethod.id = DidUrl.from(didKey, null, null,  didKey.getMethodSpecificId());

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
            verificationMethod.publicKeyMultibase = didKey.getMethodSpecificId();
        }
        
        //TODO
        
        // 12
        return verificationMethod;
    }
}
