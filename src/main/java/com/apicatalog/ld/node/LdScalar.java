package com.apicatalog.ld.node;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.oxygen.ld.LinkedData;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public interface LdScalar {

    static final String XSD_DATE_TIME = "http://www.w3.org/2001/XMLSchema#dateTime";
    static final String MULTIBASE_TYPE = "https://w3id.org/security#multibase";

    URI link() throws DocumentError;

    String string() throws DocumentError;

    String string(String type) throws DocumentError;

    String type() throws DocumentError;

    LinkedData value() throws DocumentError;

    LinkedData value(String type) throws DocumentError;

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
    
    static JsonObject multibase(Multibase base, byte[] value) {
        return encode(MULTIBASE_TYPE, base.encode(value));
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
        public LinkedData value() throws DocumentError {
            return null;
        }

        @Override
        public String string(String type) throws DocumentError {
            return null;
        }

        @Override
        public LinkedData value(String type) throws DocumentError {
            return null;
        }
    };

}
