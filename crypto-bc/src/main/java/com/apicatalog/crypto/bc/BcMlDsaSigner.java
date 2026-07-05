package com.apicatalog.crypto.bc;

import java.security.SecureRandom;
import java.security.SignatureException;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.params.MLDSAParameters;
import org.bouncycastle.crypto.params.MLDSAPrivateKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.MLDSASigner;

public final class BcMlDsaSigner {

    private final MLDSAPrivateKeyParameters privateKeyParams;
    private SecureRandom random;

    public BcMlDsaSigner(MLDSAPrivateKeyParameters privateKeyParams, SecureRandom random) {
        this.privateKeyParams = privateKeyParams;
        this.random = random;
    }

    public static BcMlDsaSigner getInstance(byte[] privateKey) {
        return getInstance(privateKey, null);
    }

    public static BcMlDsaSigner getInstance(byte[] privateKey, SecureRandom randon) {
        return new BcMlDsaSigner(toPrivateKeyParams(privateKey), randon);
    }

    public byte[] sign(final byte[] data) throws SignatureException {

        try {

            var signer = new MLDSASigner();

            if (random != null) {
                signer.init(true, new ParametersWithRandom(privateKeyParams, random));
            } else {
                signer.init(true, privateKeyParams);
            }

            signer.update(data, 0, data.length);

            return signer.generateSignature();

        } catch (CryptoException e) {
            throw new IllegalStateException("Failed to generate ML-DSA-44 signature", e);
        }
    }
    
    public BcMlDsaSigner random(SecureRandom random) {
        this.random = random;
        return this;
    }

    private static MLDSAPrivateKeyParameters toPrivateKeyParams(final byte[] privKey) {
        return new MLDSAPrivateKeyParameters(MLDSAParameters.ml_dsa_44, privKey);
    }
}