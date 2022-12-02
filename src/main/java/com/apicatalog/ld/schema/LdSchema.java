package com.apicatalog.ld.schema;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.schema.adapter.LdFlatMap;
import com.apicatalog.ld.schema.adapter.LdObjectAdapter;
import com.apicatalog.ld.schema.adapter.LdValueObjectAdapter;
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

    final LdObjectAdapter schema;

    public LdSchema(LdObjectAdapter schema) {
        this.schema = schema;
    }

    public <X> LdProperty<X> property(String tag) {
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
        return property(LdTerm.TYPE, LdPipe.map(new StringAdapter(), new UriAdapter()));
    }

    public static final <X> LdProperty<X> property(LdTerm id, LdValueAdapter<JsonValue, X> adapter) {
        return new LdProperty<X>(id, adapter);
    }

    public static final <X> LdProperty<X> property(LdTerm id, LdValueAdapter<JsonValue, X> adapter, String tag) {
        return new LdProperty<X>(id, adapter, tag);
    }

    public static final LdValueAdapter<JsonValue, Instant> xsdDateTime() {
        return LdPipe.map(value(XSD_DATETIME, new StringAdapter()), new XsdDateTimeAdapter());
    }

    public static final <X> LdValueAdapter<JsonValue, X> value(LdTerm type, LdValueAdapter<JsonValue, X> adapter) {
        return LdPipe.map(new LdValueObjectAdapter(type), adapter);
    }

    public static final <X> LdValueAdapter<JsonValue, X> value(LdValueAdapter<JsonValue, X> adapter) {
        return LdPipe.map(new LdValueObjectAdapter(), adapter);
    }

    public static final LdValueAdapter<JsonValue, String> string() {
        return value(new StringAdapter());
    }

    public static final <X> LdValueAdapter<JsonValue, X> string(LdValueAdapter<String, X> adapter) {
        return LdPipe.map(value(new StringAdapter()), adapter);
    }

    public static final LdValueAdapter<JsonValue, URI> link() {
        return new LinkAdapter();
    }

    public static final LdValueAdapter<JsonValue, byte[]> multibase(Algorithm algorithm) {
        return LdPipe.map(value(MULTIBASE_TYPE, (new StringAdapter())), new MultibaseAdapter(algorithm));
    }

    public static final LdValueAdapter<JsonValue, byte[]> multibase(Algorithm algorithm, Multicodec.Codec codec) {
        return LdPipe.map(value(MULTIBASE_TYPE, (new StringAdapter())), new MultibaseAdapter(algorithm, codec));
    }

    public static final LdValueAdapter<JsonValue, URI> uri() {
        return string(new UriAdapter());
    }

    public static final <X> LdValueAdapter<JsonValue, X> array(LdValueAdapter<JsonValue, X> adapter) {
        return LdPipe.map(new LdFlatMap(), adapter);
    }
}
