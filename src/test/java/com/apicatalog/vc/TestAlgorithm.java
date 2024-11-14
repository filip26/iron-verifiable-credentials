package com.apicatalog.vc;

import java.util.Arrays;
import java.util.Random;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.cryptosuite.KeyGenError;
import com.apicatalog.cryptosuite.SigningError;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.cryptosuite.VerificationError.VerificationErrorCode;
import com.apicatalog.cryptosuite.algorithm.Signer;
import com.apicatalog.linkedtree.jsonld.io.JsonLdWriter;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.key.GenericMulticodecKey;
import com.apicatalog.multikey.GenericMultikey;
import com.apicatalog.multikey.Multikey;

/**
 * Simple XOR to test the rest.
 */
class TestAlgorithm implements Signer {

    @Override
    public void verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError {
        final byte[] result = new byte[publicKey.length];

        for (int i = 0; i <  data.length; i++) {
            result[i % publicKey.length] = (byte) (data[i] ^ publicKey[i % publicKey.length]);
        }

        if (!Arrays.equals(result, signature)) {
            throw new VerificationError(VerificationErrorCode.InvalidSignature);
        }
    }

    @Override
    public byte[] sign(byte[] privateKey, byte[] data) throws SigningError {

        final byte[] result = new byte[privateKey.length];

        for (int i = 0; i <  data.length; i++) {
            result[i % privateKey.length] = (byte) (data[i] ^ privateKey[i % privateKey.length]);
        }

        return result;
    }

    @Override
    public KeyPair keygen() throws KeyGenError {

        byte[] raw = new byte[32];

        new Random().nextBytes(raw);

        var publicKey = new GenericMulticodecKey(
                TestMulticodecKeyAdapter.PUBLIC_KEY_CODEC,
                Multibase.BASE_58_BTC,
                raw);

        var privateKey = new GenericMulticodecKey(
                TestMulticodecKeyAdapter.PRIVATE_KEY_CODEC,
                Multibase.BASE_58_BTC,
                raw);

        return GenericMultikey.of(null, null, publicKey, privateKey);
    }

    public static void main(String[] args) throws KeyGenError {
        System.out.println(
                new JsonLdWriter()
                        .scan(Multikey.class)
                        .compacted((new TestAlgorithm()).keygen()));
    }
}
