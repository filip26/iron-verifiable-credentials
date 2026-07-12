package com.apicatalog.di.sd;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.apicatalog.multibase.Multibase;

public class HmacIdProvider {

    final Map<String, String> mapping = new HashMap<>();
    final Mac hmac;

    protected HmacIdProvider(Mac hmac) {
        this.hmac = hmac;
    }

    public static HmacIdProvider newInstance(final byte[] hmacKey) {
        final String type = "HmacSHA256";// getHmacType("P-256");
        final SecretKeySpec key = new SecretKeySpec(hmacKey, type);
        try {
            final Mac hmac = Mac.getInstance(type);
            hmac.init(key);
            return new HmacIdProvider(hmac);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getHmacId(final String resource) {

        String hmacId = mapping.get(resource);

        if (hmacId == null) {
            hmacId = "_:" + Multibase.BASE_64_URL
                    .encode(hmac.doFinal(resource.substring(2).getBytes(StandardCharsets.UTF_8)));
            hmac.reset();
            mapping.put(resource, hmacId);
        }
        return hmacId;
    }

    public Map<String, String> mapping() {
        return mapping;
    }
}
