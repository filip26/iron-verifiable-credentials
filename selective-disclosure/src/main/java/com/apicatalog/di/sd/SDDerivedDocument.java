package com.apicatalog.di.sd;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import com.apicatalog.trust.model.Model;

public record SDDerivedDocument(
        Collection<?> documentContext,
        Supplier<Map<String, Object>> compacted,
        byte[] canonicalPayload,
        byte[][] redactablePayload,
        int[] indices,
        Map<Integer, byte[]> labels) implements RedactablePayload {

    @Override
    public String c14n() {
        return Model.C14N_RDFC;
    }

}
