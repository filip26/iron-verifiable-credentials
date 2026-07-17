package com.apicatalog.trust.processor;

import java.util.Collection;
import java.util.Map;

import com.apicatalog.trust.model.LexicalModel;

public interface MapProcessor extends PayloadProcessor {

    @FunctionalInterface
    public interface Factory {
        MapProcessor newInstance(
                LexicalModel model,
                Collection<String> context,
                Map<String, Object> document);
    }

    public Collection<?> proofs();

}
