package com.apicatalog.ld;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class ExpandedLinkedObject {

    protected URI id;
    protected Collection<String> type;

    protected JsonObject expanded;

    protected ExpandedLinkedObject(JsonObject expanded) {
        this.expanded = expanded;
    }

    public URI id() {
        return id;
    }

    public void id(URI id) {
        this.id = id;
    }

    public Collection<String> type() {
        return type;
    }

    public void type(Collection<String> type) {
        this.type = type;
    }

    public Collection<String> terms() {
        return expanded.keySet()
                .stream()
                .filter(termsFilter())
                .toList();
    }

    public Optional<JsonValue> term(String name) {
        final JsonValue value = expanded.get(name);
        return JsonUtils.isNull(value) ? Optional.empty() : Optional.of(value);
    }

    protected Predicate<String> termsFilter() {
        return term -> !Keywords.ID.equals(term) && !Keywords.TYPE.equals(term);
    }
}
