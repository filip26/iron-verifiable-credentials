package com.apicatalog.lds.ed25519;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.lds.key.KeyPair;
import com.apicatalog.lds.proof.VerificationMethod;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.Multicodec.Codec;
import com.apicatalog.multicodec.Multicodec.Type;
import com.apicatalog.vc.DataIntegrityError;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class Ed25519KeyPair2020 implements KeyPair {

    private static final String ID = "id";
    private static final String TYPE = "type";
    private static final String CONTROLLER = "controller";
    private static final String PUBLIC_KEY_MULTIBASE = "publicKeyMultibase";
    private static final String PRIVATE_KEY_MULTIBASE = "privateKeyMultibase";

    private final URI id;
    private String type;
    private URI controller;

    private byte[] publicKey;
    private byte[] privateKey;

    public Ed25519KeyPair2020(final URI id) {
        this.id = id;
    }
    
    public static final Ed25519KeyPair2020 reference(URI id) {
        return new Ed25519KeyPair2020(id);
    }
    
    public static final Ed25519KeyPair2020 from(JsonObject json) throws DataIntegrityError {

        // TODO check json object type!
        final Ed25519KeyPair2020 key = new Ed25519KeyPair2020(URI.create(json.getString(ID)));

        key.type = json.getString(TYPE);
        key.controller = URI.create(json.getString(CONTROLLER));
        key.publicKey = getKey(json, PUBLIC_KEY_MULTIBASE, Codec.Ed25519PublicKey);
        
        // verify verification key length - TODO needs to be clarified
        if (key.publicKey.length == 32 || key.publicKey.length == 57 || key.publicKey.length == 114) {
            //FIXME throw new VerificationError(Code.InvalidProofLength);
        }
        
        key.privateKey = getKey(json, PRIVATE_KEY_MULTIBASE, Codec.Ed25519PrivateKey);

        return key;
    }
    
    public static VerificationMethod fetch(URI id, DocumentLoader loader) {

        try {
            final Document document = loader.loadDocument(id, new DocumentLoaderOptions());

            return from(document.getJsonContent().orElseThrow().asJsonObject());    //TODO check types
        } catch (DataIntegrityError | JsonLdError e) {
            //TODO
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public JsonObject toJson() {
        final JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add(ID, id.toString());
        builder.add(TYPE, type);

        if (controller != null) {
            builder.add(CONTROLLER, controller.toString());
        }

        setKey(builder, publicKey, PUBLIC_KEY_MULTIBASE, Codec.Ed25519PublicKey);
        setKey(builder, privateKey, PRIVATE_KEY_MULTIBASE, Codec.Ed25519PrivateKey);

        return builder.build();
    }

    public URI getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    public URI getController() {
        return controller;
    }

    @Override
    public byte[] getPrivateKey() {
        return privateKey;
    }

    @Override
    public byte[] getPublicKey() {
        return publicKey;
    }

    static byte[] getKey(JsonObject json, String property, Codec expected) throws DataIntegrityError {

        if (!json.containsKey(property)) {
            return null;
        }

        final String privateKeyMultibase = json.getString(property);

        // decode private key
        final byte[] encodedKey = Multibase.decode(privateKeyMultibase);
        
        final Codec codec = Multicodec.codec(Type.Key, encodedKey).orElseThrow(DataIntegrityError::new);

        if (expected != codec) {
            throw new DataIntegrityError();
        }

        return Multicodec.decode(codec, encodedKey);
    }

    static void setKey(JsonObjectBuilder builder, byte[] key, String property, Codec codec) {

        if (key == null || key.length == 0) {
            return;
        }

        final byte[] encoded = Multicodec.encode(codec, key);

        final String multibase = Multibase.encode(encoded);

        builder.add(property, multibase);
    }
}