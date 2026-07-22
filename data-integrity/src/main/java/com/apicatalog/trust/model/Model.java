package com.apicatalog.trust.model;

import java.util.Map;

import com.apicatalog.trust.Document;

// processing model
public interface Model {

    static final String C14N_RDFC = "RDFC";
    static final String C14N_JCS = "JCS";

    record Vocab(String context, String proof, String id, String type) {
    };

    //TODO add context param
    Document.Adapter createAdapter(Map<String, Object> document);
    //TODO add context param
    Document.Updater createUpdater(Map<String, Object> document);

    Vocab vocab();

}
