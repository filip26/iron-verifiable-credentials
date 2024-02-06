package com.apicatalog.ld.node;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.adapter.XsdDateTimeAdapter;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.Multicodec;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

class LdScalarImpl implements LdScalar {

    static final String XSD_DATE_TIME = "http://www.w3.org/2001/XMLSchema#dateTime";
    static final String MULTIBASE_TYPE = "https://w3id.org/security#multibase";

    final Term term;
    final String type;
    final JsonValue value;

    protected LdScalarImpl(Term term, String type, JsonValue value) {
        this.term = term;
        this.type = type;
        this.value = value;
    }

    public static LdScalar of(Term term, JsonObject object) throws DocumentError {
        if (object == null) {
            throw new IllegalArgumentException();
        }

        JsonValue value = object.get(Keywords.VALUE);
        final JsonValue type = object.get(Keywords.TYPE);

        if (JsonUtils.isNull(type)) {
            return new LdScalarImpl(term, null, value);

        } else if (JsonUtils.isString(type)) {
            return new LdScalarImpl(term, ((JsonString) type).getString(), value);
        }
        throw new DocumentError(ErrorType.Invalid, term);
    }

    public URI link() throws DocumentError {
        final String link = string();

        try {
            return link != null ? URI.create(link) : null;
        } catch (IllegalArgumentException e) {
            throw new DocumentError(e, ErrorType.Invalid, term);
        }
    }

    public String string() throws DocumentError {

        if (JsonUtils.isString(value)) {
            return ((JsonString) value).getString();
        }
        if (JsonUtils.isNotNull(value)) {
            throw new DocumentError(ErrorType.Invalid, term);
        }

        return null;
    }

    public boolean exists() {
        return JsonUtils.isNotNull(value);
    }

//    static boolean hasType(JsonObject object, String type) {
//        JsonValue types = object.get(Keywords.TYPE);
//        return JsonUtils.isArray(types)
//                && types.asJsonArray().stream()
//                        .filter(JsonUtils::isString)
//                        .map(JsonString.class::cast)
//                        .map(JsonString::getString)
//                        .anyMatch(type::equals);
//    }

    public byte[] multibase(Multibase base) throws DocumentError {

        if (!MULTIBASE_TYPE.equals(type)) {
            throw new DocumentError(ErrorType.Invalid, term);
        }

        final String string = string();

        if (string != null) {
            try {
                return base.decode(string);
            } catch (IllegalArgumentException e) {
                throw new DocumentError(e, ErrorType.Invalid, term);
            }

        }
        return null;
    }

    @Override
    public byte[] multiformat(Multibase base, Multicodec codec) throws DocumentError {

        if (!MULTIBASE_TYPE.equals(type)) {
            throw new DocumentError(ErrorType.Invalid, term);
        }

        final String string = string();

        if (string != null) {
            try {
                byte[] debased = base.decode(string);
                if (debased != null && debased.length > 0) {
                    return codec.decode(debased);
                }
            } catch (IllegalArgumentException e) {
                throw new DocumentError(e, ErrorType.Invalid, term);
            }
        }
        return null;
    }

    public Instant xsdDateTime() throws DocumentError {

        if (!XSD_DATE_TIME.equals(type)) {
            throw new DocumentError(ErrorType.Invalid, term);
        }

        final String string = string();

        if (string != null) {
            try {
                return (new XsdDateTimeAdapter()).read(string);
            } catch (IllegalArgumentException e) {
                throw new DocumentError(e, ErrorType.Invalid, term);
            }
        }
        return null;
    }

    @Override
    public String type() throws DocumentError {
        return type;
    }

    @Override
    public JsonValue value() throws DocumentError {
        return value;
    }
}
