package com.apicatalog.ld.schema;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.ld.schema.value.MultibaseValue;
import com.apicatalog.ld.schema.value.StringValue;
import com.apicatalog.ld.schema.value.UriValue;
import com.apicatalog.ld.schema.value.XsdDateTimeValue;

import jakarta.json.JsonValue;

public class LdSchema {
    
    protected static final LdTerm MULTIBASE_TYPE = LdTerm.create("multibase", "https://w3id.org/security#");

    protected static final String XSD_VOCAB = "http://www.w3.org/2001/XMLSchema#";
    
    protected static final LdTerm XSD_DATETIME = LdTerm.create("dateTime", XSD_VOCAB);

    public static final LdObject object(LdProperty<?>... properties) {
        return LdObject.create(properties);
    }

    public static final LdProperty<URI> id() {
        return property(LdTerm.ID, uri());
    }

    public static final LdProperty<?> type(LdTerm id) {
        return property(LdTerm.TYPE, array().map(new StringValue()).map(new UriValue()));
    }

    public static final <X> LdProperty<X> property(LdTerm id, LdValue<JsonValue, X> value) {
        return new LdProperty<X>(id, value);
    }
    
    public static final LdValue<JsonValue, Instant> xsdDateTime() {
        return value(XSD_DATETIME, new StringValue().map(new XsdDateTimeValue()));
    }
    
    public static final <X> LdValue<JsonValue, X> value(LdTerm type, LdValue<JsonValue, X> adapter) {
        return (new LdValueObject(type)).map(adapter);
    }

    public static final <X> LdValue<JsonValue, X> value(LdValue<JsonValue, X> adapter) {
        return (new LdValueObject()).map(adapter);
    }

    public static final LdValue<JsonValue, String> string() {
        return value(new StringValue());
    }

    public static final <X> LdValue<JsonValue, X> string(LdValue<String, X> adapter) {
        return value(new StringValue().map(adapter));
    }
    
    public static final LdValue<JsonValue, LdTerm> reference() {
        return null;
    }
    
    public static final LdProperty<byte[]> proofValue(LdTerm id, LdValue<JsonValue, byte[]> value) {
        return property(id, value);
    }
    
    public static final LdValue<JsonValue, byte[]> multibase() {
        return value(MULTIBASE_TYPE, (new StringValue()).map(new MultibaseValue()));
    }

    public static final LdProperty verificationMethod(LdTerm id, LdValue<?, byte[]> value) {
        return null;
    }
    
    public static final LdValue<JsonValue, URI> uri() {
        return string(new UriValue());
    }
    
    public static final LdValue<JsonValue, JsonValue> array() {
        return new LdArray();
    }
    
}
