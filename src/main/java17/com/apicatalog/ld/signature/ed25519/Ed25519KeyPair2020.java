package com.apicatalog.ld.signature.ed25519;

import java.net.URI;
import java.util.Objects;

import com.apicatalog.ld.signature.key.KeyPair;

public record Ed25519KeyPair2020(
        URI id,
        URI controller,
        String type,
        byte[] publicKey,
        byte[] privateKey
        ) implements KeyPair {

    public Ed25519KeyPair2020 {
        Objects.requireNonNull(id);
    }
}
