package com.apicatalog.ld.node;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.adapter.LdAdapter;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.VcVocab;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

public class LdSetter {

    final LdNodeBuilder parent;
    final Term term;
    final JsonObjectBuilder builder;
    final JsonArray content;

    protected LdSetter(LdNodeBuilder parent, Term term, JsonObjectBuilder builder, JsonArray content) {
        this.parent = parent;
        this.term = term;
        this.builder = builder;
        this.content = content;
    }

    public LdNodeBuilder link(URI uri) {
        string(uri.toString());
        return parent;
    }

    public LdNodeBuilder string(String value) {
        scalar(null, value);
        return parent;
    }

    public LdNodeBuilder scalar(String type, String value) {
        JsonObjectBuilder scalar = Json.createObjectBuilder();
        if (type != null) {
            scalar.add(Keywords.TYPE, type);
        }
        scalar.add(Keywords.VALUE, value);

        value(scalar.build());
        return parent;
    }

    public LdNodeBuilder value(JsonValue value) {
        builder.add(term.uri(), setOrAdd(value));
        return parent;
    }

    public <T> LdNodeBuilder map(LdAdapter<T> adapter, T value) {
        if (value != null) {
            value(adapter.write(value));
        }
        return parent;
    }

    public LdNodeBuilder xsdDateTime(Instant created) {
        scalar(VcVocab.XSD_DATETIME.uri(), created.toString());
        return parent;
    }
    
    public LdNodeBuilder multibase(Multibase base, byte[] value) {
        scalar(VcVocab.MULTIBASE_TYPE.uri(), base.encode(value));
        return parent;
    }

    public LdNodeBuilder id(URI id) {
        if (id != null) {
            JsonObjectBuilder node = Json.createObjectBuilder();
            node.add(Keywords.ID, Json.createValue(id.toString()));
            value(node.build());
        }
        return parent;
    }
    
    JsonArray setOrAdd(JsonValue value) {
        if (content != null) {
            return Json.createArrayBuilder(content).add(value).build();
        }
        return Json.createArrayBuilder().add(value).build();
    }
}
