package com.apicatalog.vc.subject.reader;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.vc.ModelVersion;

import jakarta.json.JsonObject;

public class ExpandedSubjectReader implements SubjectReader {

    @Override
    public ExpandedSubject read(ModelVersion version, JsonObject document) throws DocumentError {

        if (document.isEmpty()) {
            return null;
        }

        final ExpandedSubject status = new ExpandedSubject(version);
        final LdNode node = LdNode.of(document);
        
        return read(node, status);
    }

    protected ExpandedSubject read(LdNode node, ExpandedSubject subject) throws DocumentError {

        // @id
        subject.id(node.id());

        // @type
        subject.type(node.type().strings());
        
        return subject;
    }

}
