package com.apicatalog.crypto.bc;

import java.security.SecureRandom;
import java.security.SignatureException;

import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.params.SLHDSAParameters;
import org.bouncycastle.crypto.params.SLHDSAPrivateKeyParameters;
import org.bouncycastle.crypto.signers.SLHDSASigner;

public final class BCSLHDSASigner {

    private final SLHDSAPrivateKeyParameters privateKeyParams;
    private SecureRandom random;

    public BCSLHDSASigner(SLHDSAPrivateKeyParameters privateKeyParams, SecureRandom random) {
        this.privateKeyParams = privateKeyParams;
        this.random = random;
    }

    public static BCSLHDSASigner new128SInstance(byte[] privateKey) {
        return new128SInstance(privateKey, null);
    }

    public static BCSLHDSASigner new128SInstance(byte[] privateKey, SecureRandom randon) {
        return new BCSLHDSASigner(toPrivateKeyParams(SLHDSAParameters.sha2_128s, privateKey), randon);
    }

    public byte[] sign(final byte[] data) throws SignatureException {
        var signer = new SLHDSASigner();

        if (random != null) {
            signer.init(true, new ParametersWithRandom(privateKeyParams, random));
        } else {
            signer.init(true, privateKeyParams);
        }

        return signer.generateSignature(data);
    }

    public BCSLHDSASigner random(SecureRandom random) {
        this.random = random;
        return this;
    }

    private static SLHDSAPrivateKeyParameters toPrivateKeyParams(SLHDSAParameters params, final byte[] privKey) {
        return new SLHDSAPrivateKeyParameters(params, privKey);
    }
}