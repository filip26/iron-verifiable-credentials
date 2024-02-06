package com.apicatalog.ld.node;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.Multicodec;

import jakarta.json.JsonValue;

public interface LdScalar {

    URI link() throws DocumentError;

    String string() throws DocumentError;

    String type() throws DocumentError;
    
    JsonValue value() throws DocumentError;

    default boolean exists() {
        return false;
    }

    byte[] multibase(Multibase base) throws DocumentError;

    byte[] multiformat(Multibase base, Multicodec codec) throws DocumentError;

    Instant xsdDateTime() throws DocumentError;

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
    };

}
