package com.apicatalog.di.sd;

import java.util.Collection;

import com.apicatalog.trust.model.DataModel;

public class SDDerivedDocument implements SDPayload {

    byte[] base;
    Collection<byte[]> disclosed;
    int[] disclosedIndices;

    @Override
    public byte[] canonicalPayload() {
        return base;
    }

    @Override
    public Collection<byte[]> redactablePayload() {
        return disclosed;
    }

    @Override
    public String c14n() {
        return DataModel.C14N_RDFC;
    }

}
