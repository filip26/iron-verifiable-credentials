package com.apicatalog.trust.lexical;

import java.util.Collection;
import java.util.Map;

import com.apicatalog.trust.Document;

public interface LexicalAdapter extends Document.Adapter {

    @FunctionalInterface
    interface Factory {
        LexicalAdapter createProcessor(
                LexicalModel model,
                Collection<String> context,
                Map<String, Object> document);
    }

    Map<String, Object> data();

    int proofs();

    Map<String, Object> proof(int index);

    Collection<String> context();
}
