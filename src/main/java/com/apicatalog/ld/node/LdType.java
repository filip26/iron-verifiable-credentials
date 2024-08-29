package com.apicatalog.ld.node;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

@Deprecated
public class LdType {

    final JsonObject object;

    public LdType(JsonObject object) {
        this.object = object;
    }

    public URI link() throws DocumentError {
        final String link = string();

        return link != null ? URI.create(link) : null;
    }

    public Collection<String> strings() throws DocumentError {
        return strings(object);
    }
    
    public static Collection<String> strings(final JsonObject object) throws DocumentError {
        JsonValue types = object.get(Keywords.TYPE);
        if (JsonUtils.isNonEmptyArray(types)) {
            final List<String> strings = new ArrayList<>(types.asJsonArray().size());
            for (final JsonValue type : types.asJsonArray()) {
                if (JsonUtils.isNotString(type)) {
                    throw new DocumentError(ErrorType.Invalid, Keywords.TYPE);
                }
                strings.add(((JsonString) type).getString());
            }
            return strings;
        }
        return Collections.emptyList();        
    }

    public String string() throws DocumentError {
        JsonValue type = object.get(Keywords.TYPE);

        if (JsonUtils.isNotNull(type)) {
            Collection<JsonValue> types = JsonUtils.toCollection(type);

            if (types.size() > 1) {
                throw new DocumentError(ErrorType.Invalid, Keywords.TYPE);
            }
            if (types.size() > 0) {
                type = types.iterator().next();
            }
        }

        if (JsonUtils.isNull(type)) {
            return null;
        }

        if (JsonUtils.isString(type)) {
            return ((JsonString) type).getString();
        }

        throw new DocumentError(ErrorType.Invalid, Keywords.TYPE);
    }

    public boolean exists() {
        return (object != null) && JsonUtils.isNotNull(object.get(Keywords.TYPE));
    }

    public boolean hasType(final Term type) {
        return hasType(type.uri()); 
    }
    
    public boolean hasType(final String type) {
        if (JsonUtils.isNull(object)) {
            return false;
        }

        final JsonValue types = object.get(Keywords.TYPE);

        return JsonUtils.isNotNull(types) && JsonUtils.toStream(types)
                .filter(JsonUtils::isString)
                .map(t -> ((JsonString) t).getString())
                .anyMatch(t -> type.equals(t));
    }
}
