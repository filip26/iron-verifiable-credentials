package com.apicatalog.did.key;

import com.apicatalog.did.Did;
import com.apicatalog.did.DidDocument;
import com.apicatalog.did.DidDocumentBuilder;
import com.apicatalog.did.DidResolver;
import com.apicatalog.did.DidUrl;
import com.apicatalog.ld.signature.proof.VerificationMethod;
import com.apicatalog.multicodec.Multicodec;

public class DidKeyResolver implements DidResolver {

    private static final String MULTIKEY_TYPE = "Multikey";     //FIXME an absolute URI
    private static final String ED25519_VERIFICATION_KEY_2020_TYPE =  "https://w3id.org/security#Ed25519VerificationKey2020";
    //private static final String X25519_KEYAGREEMENT_KEY_2020_TYPE =  "https://w3id.org/security#X25519KeyAgreementKey2020";

    public DidKeyResolver() {
    }

    @Override
    public DidDocument resolve(final Did did) {

        if (!DidKey.isDidKey(did)) {
            throw new IllegalArgumentException();
        }

        final DidKey didKey = DidKey.from(did);

        final DidDocumentBuilder builder = DidDocumentBuilder.create();

        //TODO use configurable DidResolvers, steps 4-5
        // 4.
        DidVerificationKey signatureMethod = DidKeyResolver.createSignatureMethod(didKey);
        builder.add(signatureMethod);

        // 5.
        builder.add(DidKeyResolver.createEncryptionMethod(didKey));

        // 6.
        builder.id(did);

        // 7.
        //TODO toJson();

        // 8.
        builder.addAuthentication(signatureMethod.id);
        builder.addAssertionMethod(signatureMethod.getId());
        builder.addCapabilityInvocation(signatureMethod.getId());
        builder.addCapabilityDelegation(signatureMethod.getId());

        // 9.
        //TODO

        return builder.build();
    }

    /**
     * Creates a new verification key by expading the given DID key.
     *
     * see {@link https://pr-preview.s3.amazonaws.com/w3c-ccg/did-method-key/pull/51.html#signature-method-creation-algorithm}
     *
     * @return The new verification key
     */
    public static DidVerificationKey createSignatureMethod(DidKey didKey) {

        if (!Multicodec.Codec.Ed25519PublicKey.equals(didKey.getCodec())) {
            throw new IllegalArgumentException();       //TODO
        }

        // 5.
        String encodingType = ED25519_VERIFICATION_KEY_2020_TYPE;
        //TODO use options

        // 6.
        //TODO

        // 9.
//        if (MULTIKEY_TYPE.equals(encodingType)
//                || ED25519_VERIFICATION_KEY_2020_TYPE.equals(encodingType)) {
//            //FIXME verificationMethod.publicKeyMultibase = didKey.getMethodSpecificId();
//        }

        // 10.
        //TODO jwk

        return new DidVerificationKey(
                DidUrl.from(didKey, null, null,  didKey.getMethodSpecificId()),
                encodingType,
                DidUrl.from(didKey, null, null,  didKey.getMethodSpecificId()),
                didKey.getRawKey()
                );

    }

    public static VerificationMethod createEncryptionMethod(final DidKey didKey) {

        // 3.
        //TODO

        // 5.
        String encodingType = MULTIKEY_TYPE;
        //TODO use options

        // 6.
        //TODO

        // 7.
        //TODO

        // 9.
//        if (MULTIKEY_TYPE.equals(encodingType)
//                || X25519_KEYAGREEMENT_KEY_2020_TYPE.equals(encodingType)) {
//            //FIXME verificationMethod.publicKeyMultibase = didKey.getMethodSpecificId();
//        }

        //TODO

        return new DidVerificationKey(
                DidUrl.from(didKey, null, null,  didKey.getMethodSpecificId()),
                encodingType,
                DidUrl.from(didKey, null, null,  didKey.getMethodSpecificId()),
                didKey.getRawKey()
                );
    }
}
