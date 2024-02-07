package com.apicatalog.ld.signature.primitive;

import java.security.NoSuchAlgorithmException;

import com.apicatalog.ld.signature.LinkedDataSuiteError;
import com.apicatalog.ld.signature.LinkedDataSuiteError.Code;
import com.apicatalog.ld.signature.algorithm.DigestAlgorithm;

public final class MessageDigest implements DigestAlgorithm {

    private final String type;

    public MessageDigest(final String type) {
        this.type = type;
    }

    @Override
    public byte[] digest(final byte[] data) throws LinkedDataSuiteError {

        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance(type);
            return digest.digest(data);

        } catch (NoSuchAlgorithmException e) {
            throw new LinkedDataSuiteError(Code.Digest, e);
        }
    }
}
