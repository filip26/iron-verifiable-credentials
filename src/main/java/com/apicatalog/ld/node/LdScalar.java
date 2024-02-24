package com.apicatalog.ld.node;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.Multicodec;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public interface LdScalar {

    URI link() throws DocumentError;

    String string() throws DocumentError;

    String string(String type) throws DocumentError;

    String type() throws DocumentError;

    JsonValue value() throws DocumentError;

    JsonValue value(String type) throws DocumentError;

    default boolean exists() {
        return false;
    }

    byte[] multibase(Multibase base) throws DocumentError;

    byte[] multiformat(Multibase base, Multicodec codec) throws DocumentError;

    Instant xsdDateTime() throws DocumentError;

    static JsonObject encode(String type, String value) {
        return Json.createObjectBuilder()
                .add(Keywords.TYPE, type)
                .add(Keywords.VALUE, value)
                .build();
    }

    public final static LdScalar NULL = new LdScalar() {

        @Override
        public URI link() throws DocumentError {
            return null;
        }

        @Override
        public String string() throws DocumentError {
            return null;
        }

        @Override
        public byte[] multibase(Multibase base) throws DocumentError {
            return null;
        }

        @Override
        public Instant xsdDateTime() throws DocumentError {
            return null;
        }

        @Override
        public byte[] multiformat(Multibase base, Multicodec codec) throws DocumentError {
            return null;
        }

        @Override
        public String type() throws DocumentError {
            return null;
        }

        @Override
        public JsonValue value() throws DocumentError {
            return null;
        }

        @Override
        public String string(String type) throws DocumentError {
            return null;
        }

        @Override
        public JsonValue value(String type) throws DocumentError {
            return null;
        }
    };

}
