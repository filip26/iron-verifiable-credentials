package com.apicatalog.trust.lexical;

import java.util.Map;

import com.apicatalog.trust.Document;

public interface LexicalAccessor extends Document.Accessor {

    @FunctionalInterface
    interface Factory {
        LexicalAccessor createAdapter(
                LexicalModel model,
                Map<String, ?> document);
    }

    @Override
    Map<String, ?> document();

    int proofs();

    Map<String, ?> proof(int index);

//    Collection<?> context();
}
