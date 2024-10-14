package com.apicatalog.vc;

import java.util.Arrays;
import java.util.Random;

import com.apicatalog.controller.method.KeyPair;
import com.apicatalog.cryptosuite.KeyGenError;
import com.apicatalog.cryptosuite.SigningError;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.cryptosuite.VerificationError.VerificationErrorCode;
import com.apicatalog.cryptosuite.algorithm.Signer;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.Multicodec.Tag;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multicodec.key.MulticodecKey;
import com.apicatalog.multikey.GenericMultikey;

class TestAlgorithm implements Signer {

    protected static Multicodec PUBLIC_KEY_CODEC = Multicodec.of(
            "test-pub",
            Tag.Key,
            12345l);

    protected static Multicodec PRIVATE_KEY_CODEC = Multicodec.of(
            "test-priv",
            Tag.Key,
            12346l);

    protected static MulticodecDecoder DECODER = MulticodecDecoder.getInstance(
            PUBLIC_KEY_CODEC,
            PRIVATE_KEY_CODEC);

    
    @Override
    public void verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError {
        final byte[] result = new byte[Math.min(publicKey.length, data.length)];

        for (int i = 0; i < Math.min(publicKey.length, data.length); i++) {
            result[i] = (byte) (data[i] ^ publicKey[i]);
        }

        if (!Arrays.equals(result, signature)) {
            throw new VerificationError(VerificationErrorCode.InvalidSignature);
        }
    }

    @Override
    public byte[] sign(byte[] privateKey, byte[] data) throws SigningError {

        final byte[] result = new byte[Math.min(privateKey.length, data.length)];

        for (int i = 0; i < Math.min(privateKey.length, data.length); i++) {
            result[i] = (byte) (data[i] ^ privateKey[i]);
        }

//TODO        for (int i = 0; i < Math.min(privateKey.length, data.length); i++) {
//            result[i] = (byte) (data[i + 32] ^ privateKey[i]);
//        }

        return result;
    }

    @Override
    public KeyPair keygen() throws KeyGenError {

        byte[] raw = new byte[32];

        new Random().nextBytes(raw);

        var publicKey = new MulticodecKey() {

            @Override
            public String type() {
                return PUBLIC_KEY_CODEC.name();
            }

            @Override
            public byte[] rawBytes() {
                return raw;
            }

            @Override
            public Multicodec codec() {
                return PUBLIC_KEY_CODEC;
            }
        };

        var privateKey = new MulticodecKey() {

            @Override
            public String type() {
                return PRIVATE_KEY_CODEC.name();
            }

            @Override
            public byte[] rawBytes() {
                return raw;
            }

            @Override
            public Multicodec codec() {
                return PRIVATE_KEY_CODEC;
            }
        };

        return GenericMultikey.of(null, null, publicKey, privateKey);
    }

    public static void main(String[] args) throws KeyGenError {

//        final LinkedData keypair = (new TestKeyAdapter()).write((new TestAlgorithm()).keygen());

//        System.out.println(keypair.asObject().id());
//        System.out.println(keypair.asObject().type());

//        final StringWriter out = new StringWriter();

//        final JsonWriterFactory writerFactory = Json.createWriterFactory(
//                Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));
//        
//        try (final JsonWriter jsonWriter = writerFactory.createWriter(out)) {
//            jsonWriter.write(keypair);
//        }

        // System.out.println(out.toString());
    }
}
