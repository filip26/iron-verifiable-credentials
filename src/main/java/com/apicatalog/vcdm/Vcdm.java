package com.apicatalog.vcdm;

import java.util.ArrayList;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableModel;
import com.apicatalog.vc.model.VerifiableModelWriter;

public record Vcdm(
        VcdmVersion version,
        VerifiableMaterial data,
        Collection<VerifiableMaterial> proofs,
        VerifiableModelWriter writer) implements VerifiableModel {

    @Override
    public VerifiableMaterial materialize() throws DocumentError {
        return writer.write(this);
    }

    @Override
    public VerifiableModel withProof(VerifiableMaterial signedProof) {

        Collection<VerifiableMaterial> newProofs = new ArrayList<>(proofs);
        newProofs.add(signedProof);

        return new Vcdm(
                version,
                data,
                newProofs,
                writer);
    }

}
