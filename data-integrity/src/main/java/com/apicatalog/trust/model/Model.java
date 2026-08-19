package com.apicatalog.trust.model;

import java.util.Collection;
import java.util.Map;

import com.apicatalog.trust.Document;

// processing model
public interface Model {

    static final String C14N_RDFC = "RDFC";
    static final String C14N_JCS = "JCS";

    record Vocab(String context, String proof, String id, String type) {
    };

    Document.Accessor createAccessor(Collection<?> context, Map<String, ?> document);
    Document.Updater createUpdater(Collection<?> context, Map<String, ?> document);

    Vocab vocab();

}
