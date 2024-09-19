package com.apicatalog.ld.signature.primitive;

import java.security.NoSuchAlgorithmException;

import com.apicatalog.ld.signature.CryptoSuiteError;
import com.apicatalog.ld.signature.CryptoSuiteError.CryptoSuiteErrorCode;
import com.apicatalog.ld.signature.algorithm.Digester;

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
