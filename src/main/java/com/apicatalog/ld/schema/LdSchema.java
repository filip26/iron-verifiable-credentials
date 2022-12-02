package com.apicatalog.ld.schema;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.schema.adapter.ArrayAdapter;
import com.apicatalog.ld.schema.adapter.ObjectAdapter;
import com.apicatalog.ld.schema.adapter.LdValueAdapter;
import com.apicatalog.ld.schema.adapter.ValueObjectAdapter;
import com.apicatalog.ld.schema.adapter.LinkAdapter;
import com.apicatalog.ld.schema.adapter.MultibaseAdapter;
import com.apicatalog.ld.schema.adapter.StringAdapter;
import com.apicatalog.ld.schema.adapter.UriAdapter;
import com.apicatalog.ld.schema.adapter.XsdDateTimeAdapter;
import com.apicatalog.multibase.Multibase.Algorithm;
import com.apicatalog.multicodec.Multicodec;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class LdSchema {

    protected static final LdTerm MULTIBASE_TYPE = LdTerm.create("multibase", "https://w3id.org/security#");

    protected static final String XSD_VOCAB = "http://www.w3.org/2001/XMLSchema#";

    protected static final LdTerm XSD_DATETIME = LdTerm.create("dateTime", XSD_VOCAB);

    final ObjectAdapter schema;

    public LdSchema(ObjectAdapter schema) {
        this.schema = schema;
    }

    public <X> LdProperty<X> tagged(String tag) {
        return schema.property(tag);
    }

    public LdObject read(JsonObject value) throws DocumentError {
        return schema.read(value);
    }

    public void validate(LdObject value, Map<String, Object> params) throws DocumentError {
        schema.validate(value, params);
    }

    public JsonObject write(LdObject value) throws DocumentError {
        return schema.write(value);
    }

    public static LdSchema create(LdProperty<?>... properties) {
        return new LdSchema(object(properties));
    }

    public static final ObjectAdapter object(LdProperty<?>... properties) {
        return ObjectAdapter.create(properties);
    }

    public static final LdProperty<URI> id() {
        return property(LdTerm.ID, LdPipe.create(new StringAdapter()).map(new UriAdapter()));
    }

    public static final LdProperty<URI> type(final LdTerm id) {
        return property(LdTerm.TYPE,  
                    array(LdPipe.create(new StringAdapter())
                                .map(new UriAdapter()))
                                .find(uri -> id.uri().equals(uri.toString()))
                );
    }

    public static final LdProperty<Collection<URI>> type() {
        return property(LdTerm.TYPE, array(LdPipe.create(new StringAdapter()).map(new UriAdapter())));
    }

    public static final <X> LdProperty<X> property(LdTerm id, LdValueAdapter<JsonValue, X> adapter) {
        return new LdProperty<X>(id, adapter);
    }

    public static final <X> LdProperty<X> property(LdTerm id, LdValueAdapter<JsonValue, X> adapter, String tag) {
        return new LdProperty<X>(id, adapter, tag);
    }

    public static final LdValueAdapter<JsonValue, Instant> xsdDateTime() {
        return LdPipe.create(value(XSD_DATETIME, new StringAdapter())).map(new XsdDateTimeAdapter());
    }

    public static final <X> LdValueAdapter<JsonValue, X> value(LdTerm type, LdValueAdapter<JsonValue, X> adapter) {
        return LdPipe.create(new ValueObjectAdapter(type)).map(adapter);
    }

    public static final <X> LdValueAdapter<JsonValue, X> value(LdValueAdapter<JsonValue, X> adapter) {
        return LdPipe.create(new ValueObjectAdapter()).map(adapter);
    }

    public static final LdValueAdapter<JsonValue, String> string() {
        return value(new StringAdapter());
    }

    public static final <X> LdValueAdapter<JsonValue, X> string(LdValueAdapter<String, X> adapter) {
        return LdPipe.create(value(new StringAdapter())).map(adapter);
    }

    public static final LdValueAdapter<JsonValue, URI> link() {
        return new LinkAdapter();
    }

    public static final LdValueAdapter<JsonValue, byte[]> multibase(Algorithm algorithm) {
        return LdPipe.create(value(MULTIBASE_TYPE, (new StringAdapter()))).map(new MultibaseAdapter(algorithm));
    }

    public static final LdValueAdapter<JsonValue, byte[]> multibase(Algorithm algorithm, Multicodec.Codec codec) {
        return LdPipe.create(value(MULTIBASE_TYPE, (new StringAdapter()))).map(new MultibaseAdapter(algorithm, codec));
    }

    public static final LdValueAdapter<JsonValue, URI> uri() {
        return string(new UriAdapter());
    }

    public static final <X> ArrayAdapter<X> array(LdValueAdapter<JsonValue, X> adapter) {
        return new ArrayAdapter<X>(adapter);
    }
}
