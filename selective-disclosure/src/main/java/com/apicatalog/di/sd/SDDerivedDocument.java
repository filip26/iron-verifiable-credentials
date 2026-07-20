package com.apicatalog.di.sd;

import java.util.Map;
import java.util.function.Supplier;

import com.apicatalog.trust.model.Model;

public record SDDerivedDocument(
        Supplier<Map<String, Object>> compacted,
        byte[] canonicalPayload,
        byte[][] redactablePayload,
        int[] indices,
        Map<Integer, byte[]> labels) implements SDPayload {

    @Override
    public String c14n() {
        return Model.C14N_RDFC;
    }

}
