package com.apicatalog.cryptosuite.primitive;

import java.security.NoSuchAlgorithmException;

import com.apicatalog.cryptosuite.CryptoSuiteError;
import com.apicatalog.cryptosuite.CryptoSuiteError.CryptoSuiteErrorCode;
import com.apicatalog.cryptosuite.algorithm.Digester;

public final class MessageDigest implements Digester {

    private final String type;

    public MessageDigest(final String type) {
        this.type = type;
    }

    @Override
    public byte[] digest(final byte[] data) throws CryptoSuiteError {

        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance(type);
            return digest.digest(data);

        } catch (NoSuchAlgorithmException e) {
            throw new CryptoSuiteError(CryptoSuiteErrorCode.Digest, e);
        }
    }
}
