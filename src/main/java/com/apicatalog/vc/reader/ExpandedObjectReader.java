package com.apicatalog.vc.reader;

import com.apicatalog.ld.LinkedObject;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.vc.ModelVersion;

import jakarta.json.JsonObject;

public abstract class ExpandedObjectReader<T extends LinkedObject> implements ObjectReader<JsonObject, T> {

    @Override
    public T read(ModelVersion version, JsonObject document) throws DocumentError {

        if (document.isEmpty()) {
            return null;
        }

        final LdNode node = LdNode.of(document);

        return read(node, newInstance(version, document));
    }

    protected abstract T newInstance(ModelVersion version, JsonObject expanded);

    protected T read(LdNode node, T status) throws DocumentError {

        // @id
        status.id(node.id());

        // @type
        status.type(node.type().strings());

        return status;
    }

}
