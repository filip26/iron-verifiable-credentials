package com.apicatalog.vcdi;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.adapter.LinkedFragmentReader;
import com.apicatalog.linkedtree.adapter.LinkedLiteralAdapter;
import com.apicatalog.vc.proof.ProofAdapter;

public record DataIntegrityProofAdapter(
        DataIntegritySuite suite,
        Collection<LinkedLiteralAdapter> literalAdapters) implements ProofAdapter {

    @Override
    public LinkedFragmentReader reader() {
        return (id, types, properties, rootSupplier) -> {
            try {
                return DataIntegrityProof.of(id, types, properties, rootSupplier, suite);
            } catch (DocumentError e) {
                //FIXME
                throw new IllegalArgumentException(e);
            }
        };
    }

    @Override
    public String proofType() {
        return DataIntegrityVocab.TYPE.uri();
    }
}
