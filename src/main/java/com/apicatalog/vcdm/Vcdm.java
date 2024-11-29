package com.apicatalog.vcdm;

import java.util.Collection;

import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentModel;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.io.DocumentModelWriter;

public record Vcdm(
        VcdmVersion version,
        VerifiableMaterial data,
        Collection<VerifiableMaterial> proofs,
        DocumentModelWriter writer) implements DocumentModel {

    @Override
    public VerifiableMaterial materialize() throws DocumentError {
        return writer.write(this);
    }

    @Override
    public DocumentModel of(VerifiableMaterial data, Collection<VerifiableMaterial> proofs) {
        return new Vcdm(
                version,
                data,
                proofs,
                writer);
    }
}
