package com.apicatalog.di.sd;

import java.util.Map;
import java.util.function.Supplier;

import com.apicatalog.trust.model.ProcessingModel;

public record SDDerivedDocument(
        Supplier<Map<String, Object>> compacted,
        byte[] canonicalPayload,
        byte[][] redactablePayload,
        int[] indices,
        Map<Integer, byte[]> labels) implements SDPayload {

    @Override
    public String c14n() {
        return ProcessingModel.C14N_RDFC;
    }

}
