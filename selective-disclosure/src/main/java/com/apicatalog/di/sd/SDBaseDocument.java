package com.apicatalog.di.sd;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.apicatalog.trust.model.ProcessingModel;
import com.apicatalog.trust.model.SemanticModel;

public class SDBaseDocument implements SDPayload {

    byte[] base;

    byte[][] redactable;
    int[] redactableIndices;

    Collection<String> mandatoryPointers;
    int[] mandatoryIndices;

    byte[] hmacKey;

    Collection<String> context;
    Map<String, Object> compacted;
    List<String> canonized;

    Map<String, String> labels;

    SemanticModel model;

    @Override
    public byte[] canonicalPayload() {
        return base;
    }

    @Override
    public byte[][] redactablePayload() {
        return redactable;
    }

    public Collection<String> mandatoryPointers() {
        return mandatoryPointers;
    }

    public byte[] hmacKey() {
        return hmacKey;
    }

    @Override
    public String c14n() {
        return ProcessingModel.C14N_RDFC;
    }
}
