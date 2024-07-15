package com.apicatalog.vc.status.reader;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.vc.ModelVersion;

import jakarta.json.JsonObject;

public class ExpandedStatusReader implements StatusReader {

    @Override
    public ExpandedStatus read(ModelVersion version, JsonObject document) throws DocumentError {

        if (document.isEmpty()) {
            return null;
        }
        
        final ExpandedStatus status = new ExpandedStatus(version);
        final LdNode node = LdNode.of(document);

        return read(node, status);
    }

    protected ExpandedStatus read(LdNode node, ExpandedStatus status) throws DocumentError {

        // @id
        status.id(node.id());

        // @type
        status.type(node.type().strings());
        
        return status;
    }

}
