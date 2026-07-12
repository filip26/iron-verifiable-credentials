package com.apicatalog.di.sd;

import java.util.Map;

import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multicodec.codec.KeyCodec;

public record Keys(
        Multicodec codec,
        byte[] basePublicKey,
        byte[] baseSecretKey,
        byte[] proofPublicKey,
        byte[] proofSecretKey,
        byte[] hmacKey) {

    static final MultibaseDecoder MULTIBASE = MultibaseDecoder.getInstance();

    static final MulticodecDecoder MULTICODEC = MulticodecDecoder.newInstance(
            KeyCodec.P256_PRIVATE,
            KeyCodec.P384_PRIVATE);

    public static Keys from(Map<String, Object> keysMap) {

        var baseKeys = (Map<String, String>) keysMap.get("baseKeyPair");

        var privateKey = MULTIBASE.decode(baseKeys.get("secretKeyMultibase"));
        var privateKeyCodec = MULTICODEC.getCodec(privateKey).orElseThrow();

        //FIXME
        return new Keys(
                privateKeyCodec, 
                null, 
                privateKeyCodec.decode(privateKey), null, null, null);
    }

}
