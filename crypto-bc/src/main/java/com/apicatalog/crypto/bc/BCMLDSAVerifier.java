package com.apicatalog.crypto.bc;

import java.security.InvalidKeyException;
import java.security.SignatureException;

import org.bouncycastle.crypto.params.MLDSAParameters;
import org.bouncycastle.crypto.params.MLDSAPublicKeyParameters;
import org.bouncycastle.crypto.signers.MLDSASigner;

public final class BCMLDSAVerifier {

    private static final BCMLDSAVerifier INSTANCE = new BCMLDSAVerifier();

    public static BCMLDSAVerifier getInstance() {
        return INSTANCE;
    }

    public boolean verify(final byte[] publicKey, final byte[] data, final byte[] signature)
            throws SignatureException, InvalidKeyException {

        var verifier = new MLDSASigner();

        verifier.init(false, getPublicKeyFromBytes(publicKey));
        verifier.update(data, 0, data.length);

        return verifier.verifySignature(signature);

    }

    private static MLDSAPublicKeyParameters getPublicKeyFromBytes(final byte[] publicKey) {
        return new MLDSAPublicKeyParameters(MLDSAParameters.ml_dsa_44, publicKey);
    }
}