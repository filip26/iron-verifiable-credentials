package com.apicatalog.vc.issuer.reader;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdNode;

import jakarta.json.JsonObject;

public class ExpandedIssuerDetailsReader implements IssuerDetailsReader {

    @Override
    public ExpandedIssuerDetails read(JsonObject document) throws DocumentError {

        if (document.isEmpty()) {
            return null;
        }

        final ExpandedIssuerDetails status = new ExpandedIssuerDetails();
        final LdNode node = LdNode.of(document);

        return read(node, status);
    }

    protected ExpandedIssuerDetails read(LdNode node, ExpandedIssuerDetails subject) throws DocumentError {

        // @id
        subject.id(node.id());

        // @type
        subject.type(node.type().strings());
        
        return subject;
    }

}
