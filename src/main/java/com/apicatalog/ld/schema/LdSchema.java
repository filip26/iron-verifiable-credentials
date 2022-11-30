package com.apicatalog.ld.schema;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

import com.apicatalog.ld.schema.value.MultibaseValue;
import com.apicatalog.ld.schema.value.StringValue;
import com.apicatalog.ld.schema.value.UriValue;
import com.apicatalog.ld.schema.value.XsdDateTimeValue;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class LdSchema {
    
    protected static final LdTerm MULTIBASE_TYPE = LdTerm.create("multibase", "https://w3id.org/security#");

    protected static final String XSD_VOCAB = "http://www.w3.org/2001/XMLSchema#";
    
    protected static final LdTerm XSD_DATETIME = LdTerm.create("dateTime", XSD_VOCAB);

    public <X> LdProperty<X> property(LdTag tag) {
        // TODO Auto-generated method stub
        return null;
    }

    public JsonObject write(Map<String, Object> hashMap) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public Map<String, Object> read(JsonObject value) {
        return null;
    }

    
    public static final LdObject object(LdProperty<?>... properties) {
        return LdObject.create(properties);
    }

    public static final LdProperty<URI> id() {
        return property(LdTerm.ID, uri());
    }

    public static final LdProperty<?> type(LdTerm id) {
        return property(LdTerm.TYPE, LdPipe.map(array(new StringValue()), new UriValue()));
    }

    public static final <X> LdProperty<X> property(LdTerm id, LdValueAdapter<JsonValue, X> value) {
        return new LdProperty<X>(id, value);
    }
    
    public static final LdValueAdapter<JsonValue, Instant> xsdDateTime() {
        return  LdPipe.map(value(XSD_DATETIME, new StringValue()), new XsdDateTimeValue());
    }
    
    public static final <X> LdValueAdapter<JsonValue, X> value(LdTerm type, LdValueAdapter<JsonValue, X> adapter) {
        return LdPipe.map(new LdValueObject(type), adapter);
    }

    public static final <X> LdValueAdapter<JsonValue, X> value(LdValueAdapter<JsonValue, X> adapter) {
        return LdPipe.map(new LdValueObject(), adapter);
    }

    public static final LdValueAdapter<JsonValue,  String> string() {
        return value(new StringValue());
    }

    public static final <X> LdValueAdapter<JsonValue, X> string(LdValueAdapter<String, X> adapter) {
        return LdPipe.map(value(new StringValue()), adapter);
    }
    
    public static final LdValueAdapter<JsonValue, LdTerm> reference() {
        return null;
    }
    
    public static final LdProperty<byte[]> proofValue(LdTerm id, LdValueAdapter<JsonValue, byte[]> value) {
        return property(id, value);
    }
    
    public static final LdValueAdapter<JsonValue, byte[]> multibase() {
        return LdPipe.map(value(MULTIBASE_TYPE, (new StringValue())), new MultibaseValue());
    }

    public static final LdProperty verificationMethod(LdTerm id, LdValueAdapter<?, byte[]> value) {
        return null;
    }
    
    public static final LdValueAdapter<JsonValue, URI> uri() {
        return string(new UriValue());
    }
        
    public static final <X> LdValueAdapter<JsonValue, X> array(LdValueAdapter<JsonValue, X> adapter) {
        return LdPipe.map(new LdArray(), adapter);
    }

}
