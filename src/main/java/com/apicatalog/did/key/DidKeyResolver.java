package com.apicatalog.did.key;

import com.apicatalog.did.Did;
import com.apicatalog.did.DidDocument;
import com.apicatalog.did.DidDocumentBuilder;
import com.apicatalog.did.DidResolver;
import com.apicatalog.did.DidUrl;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.ld.signature.proof.VerificationMethod;
import com.apicatalog.multicodec.Multicodec;

public class DidKeyResolver implements DidResolver {

    private static final String ED25519_VERIFICATION_KEY_2020_TYPE =  "https://w3id.org/security#Ed25519VerificationKey2020";

    @Override
    public DidDocument resolve(final Did did) {

        if (!DidKey.isDidKey(did)) {
            throw new IllegalArgumentException();
        }

        final DidKey didKey = DidKey.from(did);

        final DidDocumentBuilder builder = DidDocumentBuilder.create();

        // 4.
        VerificationKey signatureMethod = DidKeyResolver.createSignatureMethod(didKey);
        builder.add(signatureMethod);

        // 5.
        builder.add(DidKeyResolver.createEncryptionMethod(didKey));

        // 6.
        builder.id(did);

        // 7.

        // 8.

        // 9.

        return builder.build();
    }

    /**
     * Creates a new verification key by expading the given DID key.
     * 
     * @param didKey 
     *
     * @see {@link <a href="https://pr-preview.s3.amazonaws.com/w3c-ccg/did-method-key/pull/51.html#signature-method-creation-algorithm">Signature Method Algorithm</a>}
     *
     * @return The new verification key
     */
    public static VerificationKey createSignatureMethod(DidKey didKey) {

        if (!Multicodec.Codec.Ed25519PublicKey.equals(didKey.getCodec())) {
            throw new IllegalArgumentException();       //TODO
        }
        // 5.
        String encodingType = ED25519_VERIFICATION_KEY_2020_TYPE;

        final VerificationKey key = new VerificationKey();
        
        key.setId(DidUrl.from(didKey, null, null,  didKey.getMethodSpecificId()).toUri());
        key.setType(encodingType);
        key.setController(DidUrl.from(didKey, null, null,  didKey.getMethodSpecificId()).toUri());
        key.setPublicKey(didKey.getRawKey());

        return key;
     }

    public static VerificationMethod createEncryptionMethod(final DidKey didKey) {

        // 3.

        // 5.
        String encodingType = "MultiKey";

        // 6.

        // 7.

        // 9.
        final VerificationKey key = new VerificationKey();
        
        key.setId(DidUrl.from(didKey, null, null,  didKey.getMethodSpecificId()).toUri());
        key.setType(encodingType);
        key.setController(DidUrl.from(didKey, null, null,  didKey.getMethodSpecificId()).toUri());
        key.setPublicKey(didKey.getRawKey());
        
        return key;
    }
}
