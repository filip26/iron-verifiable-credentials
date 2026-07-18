package com.apicatalog.trust.processor;

import java.util.Collection;
import java.util.Map;

import com.apicatalog.trust.model.LexicalModel;

public interface MapProcessor extends DocumentProcessor {

    @FunctionalInterface
    interface Factory {
        MapProcessor createProcessor(
                LexicalModel model,
                Collection<String> context,
                Map<String, Object> document);
    }

    Map<String, Object> data();

    int proofs();

    Map<String, Object> proof(int index);

    Collection<String> context();
}
