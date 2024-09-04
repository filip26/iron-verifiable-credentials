package com.apicatalog.vcdi;

import java.util.Collection;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.linkedtree.adapter.LinkedLiteralAdapter;
import com.apicatalog.linkedtree.reader.LinkedFragmentReader;
import com.apicatalog.vc.proof.ProofAdapter;

public record DataIntegrityProofAdapter(
        DataIntegritySuite suite,
        Collection<LinkedLiteralAdapter> literalAdapters) implements ProofAdapter {

    @Override
    public LinkedFragmentReader reader() {
        return (id, types, properties, rootSupplier) -> DataIntegrityProof.of(id, types, properties, rootSupplier, suite);
    }

    @Override
    public String proofType() {
        return VcdiVocab.TYPE.uri();
    }
}
