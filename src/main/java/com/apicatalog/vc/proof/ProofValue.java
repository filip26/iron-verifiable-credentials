package com.apicatalog.vc.proof;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.linkedtree.Linkable;

import jakarta.json.JsonObject;

public interface ProofValue extends Linkable {

    void verify(CryptoSuite crypto, Collection<String> context, JsonObject data, JsonObject unsignedProof, byte[] publicKey) throws VerificationError, DocumentError;

    byte[] toByteArray() throws DocumentError;
}
