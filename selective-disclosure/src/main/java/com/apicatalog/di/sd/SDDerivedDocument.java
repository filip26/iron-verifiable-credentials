package com.apicatalog.di.sd;

import java.util.Map;

import com.apicatalog.trust.model.DataModel;

public record SDDerivedDocument(
        byte[] canonicalPayload,
        byte[][] redactablePayload,
        int[] indices,
        Map<Integer, byte[]> labels) implements SDPayload {

    @Override
    public String c14n() {
        return DataModel.C14N_RDFC;
    }

}
