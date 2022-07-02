package com.apicatalog.ld.signature.primitive;

import java.security.NoSuchAlgorithmException;

import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.DataError.ErrorType;
import com.apicatalog.ld.signature.algorithm.DigestAlgorithm;

public final class MessageDigest implements DigestAlgorithm {

    private final String type;

    public MessageDigest(final String type) {
        this.type = type;
    }

    @Override
    public byte[] digest(byte[] data) throws DataError {

        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance(type);
            return digest.digest(data);

        } catch (NoSuchAlgorithmException e) {
            throw new DataError(ErrorType.UnknownDigest, e);
        }
    }
}
