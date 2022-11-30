package com.apicatalog.ld.schema;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.ld.schema.adapter.LdFlatMap;
import com.apicatalog.ld.schema.adapter.LdObjectAdapter;
import com.apicatalog.ld.schema.adapter.LdValueObjectAdapter;
import com.apicatalog.ld.schema.adapter.MultibaseAdapter;
import com.apicatalog.ld.schema.adapter.StringAdapter;
import com.apicatalog.ld.schema.adapter.UriAdapter;
import com.apicatalog.ld.schema.adapter.XsdDateTimeAdapter;
import com.apicatalog.ld.signature.method.VerificationMethod;
import com.apicatalog.multibase.Multibase.Algorithm;
import com.apicatalog.multicodec.Multicodec.Type;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class LdSchema {
    
    protected static final LdTerm MULTIBASE_TYPE = LdTerm.create("multibase", "https://w3id.org/security#");

    protected static final String XSD_VOCAB = "http://www.w3.org/2001/XMLSchema#";
    
    protected static final LdTerm XSD_DATETIME = LdTerm.create("dateTime", XSD_VOCAB);

    final LdObjectAdapter schema;
    
    public LdSchema(LdObjectAdapter schema) {
        this.schema = schema;
    }
    
    public <X> LdProperty<X> property(LdTag tag) {
        return schema.property(tag);
    }

    public JsonObject write(LdObject value) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public LdObject read(JsonObject value) {
        return schema.read(value);
    }

    public static LdSchema create(LdProperty<?>... properties) {
        return new LdSchema(object(properties));
    }
    
    public static final LdObjectAdapter object(LdProperty<?>... properties) {
        return LdObjectAdapter.create(properties);
    }

    public static final <X> LdValueAdapter<JsonValue, X> object(LdValueAdapter<LdObject, X> adapter, LdProperty<?>... properties) {
        return LdPipe.map(LdObjectAdapter.create(properties), adapter);
    }

    public static final LdProperty<URI> id() {
        return property(LdTerm.ID, LdPipe.map(new StringAdapter(), new UriAdapter()));
    }

    public static final LdProperty<?> type(LdTerm id) {
        return property(LdTerm.TYPE, LdPipe.map(array(new StringAdapter()), new UriAdapter()));
    }

    public static final <X> LdProperty<X> property(LdTerm id, LdValueAdapter<JsonValue, X> adapter) {
        return new LdProperty<X>(id, adapter);
    }

    public static final <X> LdProperty<X> property(LdTerm id, LdValueAdapter<JsonValue, X> adapter, LdTag tag) {
        return new LdProperty<X>(id, adapter, tag);
    }

    public static final LdValueAdapter<JsonValue, Instant> xsdDateTime() {
        return  LdPipe.map(value(XSD_DATETIME, new StringAdapter()), new XsdDateTimeAdapter());
    }
    
    public static final <X> LdValueAdapter<JsonValue, X> value(LdTerm type, LdValueAdapter<JsonValue, X> adapter) {
        return LdPipe.map(new LdValueObjectAdapter(type), adapter);
    }

    public static final <X> LdValueAdapter<JsonValue, X> value(LdValueAdapter<JsonValue, X> adapter) {
        return LdPipe.map(new LdValueObjectAdapter(), adapter);
    }

    public static final LdValueAdapter<JsonValue,  String> string() {
        return value(new StringAdapter());
    }

    public static final <X> LdValueAdapter<JsonValue, X> string(LdValueAdapter<String, X> adapter) {
        return LdPipe.map(value(new StringAdapter()), adapter);
    }
    
    public static final LdValueAdapter<JsonValue, LdObject> reference() {
        return object(id());
    }
    
    public static final LdProperty<byte[]> proofValue(LdTerm id, LdValueAdapter<JsonValue, byte[]> adapter) {
        return property(id, adapter, LdTag.ProofValue);
    }
    
    public static final LdValueAdapter<JsonValue, byte[]> multibase(Algorithm algorithm) {
        return LdPipe.map(value(MULTIBASE_TYPE, (new StringAdapter())), new MultibaseAdapter(algorithm));
    }

    public static final LdValueAdapter<JsonValue, byte[]> multibase(Algorithm algorithm, Type multicodec) {
        return LdPipe.map(value(MULTIBASE_TYPE, (new StringAdapter())), new MultibaseAdapter(algorithm, multicodec));
    }

    
    public static final LdProperty<VerificationMethod> verificationMethod(LdTerm id, LdValueAdapter<JsonValue, VerificationMethod> adapter) {
        return property(id, adapter, LdTag.VerificationMethod);
    }
    
    public static final LdValueAdapter<JsonValue, URI> uri() {
        return string(new UriAdapter());
    }
        
    public static final <X> LdValueAdapter<JsonValue, X> array(LdValueAdapter<JsonValue, X> adapter) {
        return LdPipe.map(new LdFlatMap(), adapter);
    }

    //public static final <X> LdValueAdapter<JsonValue, X> pipe(LdValueAdapter<JsonValue, X> adapter)
    
}
