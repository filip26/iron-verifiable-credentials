package com.apicatalog.ld.signature.ed25519;

import java.net.URI;
import java.util.Objects;

import com.apicatalog.ld.signature.key.VerificationKey;

public record Ed25519VerificationKey2020(
        URI id,
        URI controller,
        String type,
        byte[] publicKey
        ) implements VerificationKey {

    public Ed25519VerificationKey2020 {
        Objects.requireNonNull(id);
    }
}
