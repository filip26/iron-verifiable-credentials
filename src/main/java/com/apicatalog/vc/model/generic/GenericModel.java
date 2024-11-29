package com.apicatalog.vc.model.generic;

import java.util.Collection;

import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentModel;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.io.DocumentModelWriter;

public record GenericModel(
        VerifiableMaterial data,
        Collection<VerifiableMaterial> proofs,
        DocumentModelWriter writer) implements DocumentModel {

    @Override
    public VerifiableMaterial materialize() throws DocumentError {
        return writer.write(this);
    }

    @Override
    public DocumentModel of(VerifiableMaterial data, Collection<VerifiableMaterial> proofs) {
        return new GenericModel(data, proofs, writer);
    }
}
