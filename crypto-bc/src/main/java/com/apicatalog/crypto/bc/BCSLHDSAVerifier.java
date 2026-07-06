package com.apicatalog.crypto.bc;

import java.security.InvalidKeyException;
import java.security.SignatureException;

import org.bouncycastle.crypto.params.SLHDSAParameters;
import org.bouncycastle.crypto.params.SLHDSAPublicKeyParameters;
import org.bouncycastle.crypto.signers.SLHDSASigner;

public final class BCSLHDSAVerifier {

    private static final BCSLHDSAVerifier INSTANCE_128S = new BCSLHDSAVerifier();

    public static BCSLHDSAVerifier get128SInstance() {
        return INSTANCE_128S;
    }

    public boolean verify(final byte[] publicKey, final byte[] data, final byte[] signature)
            throws SignatureException, InvalidKeyException {

        var verifier = new SLHDSASigner();

        verifier.init(false, getPublicKeyFromBytes(publicKey));

        return verifier.verifySignature(data, signature);

    }

    private static SLHDSAPublicKeyParameters getPublicKeyFromBytes(final byte[] publicKey) {
        return new SLHDSAPublicKeyParameters(SLHDSAParameters.sha2_128s, publicKey);
    }
}