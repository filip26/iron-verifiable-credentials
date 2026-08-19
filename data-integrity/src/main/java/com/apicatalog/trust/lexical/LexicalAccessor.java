package com.apicatalog.trust.lexical;

import java.util.Collection;
import java.util.Map;

import com.apicatalog.trust.Document;

public interface LexicalAccessor extends Document.Accessor {

    @FunctionalInterface
    interface Factory {
        LexicalAccessor createAdapter(
                LexicalModel model,
                Collection<?> context,
                Map<String, ?> document);
    }

    @Override
    Map<String, ?> document();

    int proofs();

    Map<String, Object> proof(int index);

    Collection<?> context();
}
