package com.apicatalog.di.sd;

import com.apicatalog.trust.model.DataModel;

public class SDDerivedDocument implements SDPayload {

    byte[] base;
    byte[][] disclosed;
    int[] disclosedIndices;

    @Override
    public byte[] canonicalPayload() {
        return base;
    }

    @Override
    public byte[][] redactablePayload() {
        return disclosed;
    }

    @Override
    public String c14n() {
        return DataModel.C14N_RDFC;
    }

}
