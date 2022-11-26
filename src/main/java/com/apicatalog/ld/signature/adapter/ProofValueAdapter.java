package com.apicatalog.ld.signature.adapter;

import java.net.URI;

import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonObject;

public interface ProofValueAdapter {

    String encode(byte[] value) throws DocumentError;

    byte[] decode(String value) throws DocumentError;

    URI id();

    // TODO

    /**
     * Deserialize the given JSON-LD document in an expanded form representing a
     * proof. Used when verifying.
     * 
     * @param proof
     * @return
     * @throws DocumentError
     */
    // T deserialize(JsonObject proof) throws DocumentError;

    /**
     * Remove a proof value from the given JSON-LD document in an expanded form
     * representing a proof. Used when verifying.
     * 
     * @param proof an expanded JSON-LD object
     * @return a new instance, a clone, with no proof value attached
     * @throws DocumentError
     */
    // JsonObject removeProofValue(JsonObject proof) throws DocumentError;

    /**
     * Serialize the given proof as an expanded JSON-LD document. Used when issuing.
     * 
     * @param proof a proof to serialize
     * @return a serialized proof with no value set
     * @throws DocumentError
     */
//    JsonObject serialize(T proof) throws DocumentError;

    /**
     * Set a proof value (a signature) to the given JSON-LD object representing a
     * serialized proof. Used when issuing.
     * 
     * @param proof a serialized proof as an expanded JSON-LD document
     * @param value a computed signature
     * @return
     * @throws DocumentError
     */
//    JsonObject setProofValue(JsonObject proof, byte[] value) throws DocumentError;

}