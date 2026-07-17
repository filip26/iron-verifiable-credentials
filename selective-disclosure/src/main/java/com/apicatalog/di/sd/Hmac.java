package com.apicatalog.di.sd;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.apicatalog.multibase.Multibase;

class Hmac {

    private final Map<String, String> mapping = new HashMap<>();
    private final Mac hmac;

    private Hmac(Mac hmac) {
        this.hmac = hmac;
    }

    public static Hmac newInstance(final byte[] hmacKey) {
        final String type = "HmacSHA256";// getHmacType("P-256");
        final SecretKeySpec key = new SecretKeySpec(hmacKey, type);
        try {
            final Mac hmac = Mac.getInstance(type);
            hmac.init(key);
            return new Hmac(hmac);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public String assignId(final String resource) {

        String id = mapping.get(resource);

        if (id == null) {
            id = "_:" + Multibase.BASE_64_URL
                    .encode(hmac.doFinal(resource.substring(2).getBytes(StandardCharsets.UTF_8)));
            hmac.reset();
            mapping.put(resource, id);
        }
        return id;
    }

    public String getId(final String resource) {
        return mapping.get(resource);
    }
    
    public Map<String, String> mapping() {
        return mapping;
    }
}
