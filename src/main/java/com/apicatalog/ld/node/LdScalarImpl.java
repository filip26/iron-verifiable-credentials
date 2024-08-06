package com.apicatalog.ld.node;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.node.adapter.XsdDateTimeAdapter;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.oxygen.ld.LinkedData;
import com.apicatalog.oxygen.ld.LinkedLiteral;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

class LdScalarImpl implements LdScalar {

    final Term term;
    final LinkedLiteral value;

    protected LdScalarImpl(Term term, LinkedLiteral value) {
        this.term = term;
        this.value = value;
    }

    public static LdScalar of(Term term, LinkedLiteral value) throws DocumentError {
        Objects.requireNonNull(term);
        Objects.requireNonNull(value);
        return new LdScalarImpl(term, value);
        
//        JsonValue value = object.get(Keywords.VALUE);
//        final JsonValue type = object.get(Keywords.TYPE);

//        if (JsonUtils.isNull(type)) {
//            return new LdScalarImpl(term, null, value);
//
//        } else if (JsonUtils.isString(type)) {
//            return new LdScalarImpl(term, ((JsonString) type).getString(), value);
//        }
//        throw new DocumentError(ErrorType.Invalid, term);
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

        if (value.isString()) {
            return value.asString();
        }
        throw new DocumentError(ErrorType.Invalid, term);
        
//        if (JsonUtils.isString(value)) {
//            return ((JsonString) value).getString();
//        }
//        if (JsonUtils.isNotNull(value)) {
//            throw new DocumentError(ErrorType.Invalid, term);
//        }

//        return null;
    }
    
    @Override
    public String string(String expectedType) throws DocumentError {
        Objects.requireNonNull(expectedType);

        if (!expectedType.equals(value.datatype())) {
            throw new DocumentError(ErrorType.Invalid, value.datatype());
        }

        return string();
    }

    public boolean exists() {
        return true;
    }

    public byte[] multibase(Multibase base) throws DocumentError {
        
        if (!MULTIBASE_TYPE.equals(value.datatype())) {
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

//        if (!MULTIBASE_TYPE.equals(type)) {
//            throw new DocumentError(ErrorType.Invalid, term);
//        }
//
//        final String string = string();
//
//        if (string != null) {
//            try {
//                byte[] debased = base.decode(string);
//                if (debased != null && debased.length > 0) {
//                    return codec.decode(debased);
//                }
//            } catch (IllegalArgumentException e) {
//                throw new DocumentError(e, ErrorType.Invalid, term);
//            }
//        }
        return null;
    }

    public Instant xsdDateTime() throws DocumentError {

//        if (!XSD_DATE_TIME.equals(type)) {
//            throw new DocumentError(ErrorType.Invalid, term);
//        }
//
//        final String string = string();
//
//        if (string != null) {
//            try {
//                return XsdDateTimeAdapter.read(string);
//            } catch (IllegalArgumentException e) {
//                throw new DocumentError(e, ErrorType.Invalid, term);
//            }
//        }
        return null;
    }

    @Override
    public String type() throws DocumentError {
        return value != null ? value.datatype() : null;
    }

    @Override
    public LinkedData value() throws DocumentError {
        return value;
    }

    @Override
    public LinkedData value(String expectedType) throws DocumentError {
        Objects.requireNonNull(expectedType);
        
//        if (value ==)
        
//        if (!type.equals(expectedType)) {
//            throw new DocumentError(ErrorType.Invalid, type);
//        }

        return value;
    }
}
