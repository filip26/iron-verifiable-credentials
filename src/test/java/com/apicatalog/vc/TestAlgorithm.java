package com.apicatalog.vc;

import java.util.Arrays;
import java.util.Random;

import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.ld.signature.algorithm.SignatureAlgorithm;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.key.MulticodecKey;
import com.apicatalog.multicodec.MulticodecRegistry;

class TestAlgorithm implements SignatureAlgorithm {

    @Override
    public void verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError {

        final byte[] result = new byte[Math.min(publicKey.length, data.length)];

        for (int i = 0; i < Math.min(publicKey.length, data.length); i++) {
            result[i] = (byte) (data[i] ^ publicKey[i]);
        }

        if (!Arrays.equals(result, signature)) {
            throw new VerificationError(Code.InvalidSignature);
        }
    }

    @Override
    public byte[] sign(byte[] privateKey, byte[] data) throws SigningError {

        final byte[] result = new byte[Math.min(privateKey.length, data.length)];

        for (int i = 0; i < Math.min(privateKey.length, data.length); i++) {
            result[i] = (byte) (data[i] ^ privateKey[i]);
        }

        return result;
    }

    @Override
    public KeyPair keygen() throws KeyGenError {

        byte[] key = new byte[32];

        new Random().nextBytes(key);

        return new TestKeyPair(key, key);
    }
}
