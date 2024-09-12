package com.apicatalog.vcdi;

import java.util.Collection;

import com.apicatalog.linkedtree.literal.adapter.LiteralAdapter;

public record DataIntegrityProofAdapter(
        DataIntegritySuite suite,
        Collection<LiteralAdapter> literalAdapters) 
//implements ProofAdapter
{

//    @Override
//    public LinkedFragmentReader reader() {
//        return DataIntegrityProof::of;
//    }
//
//    @Override
//    public String proofType() {
//        return VcdiVocab.TYPE.uri();
//    }
}
