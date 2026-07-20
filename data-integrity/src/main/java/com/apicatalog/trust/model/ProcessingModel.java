package com.apicatalog.trust.model;

import java.util.Map;

import com.apicatalog.trust.Document;

public interface ProcessingModel {

    static final String C14N_RDFC = "RDFC";
    static final String C14N_JCS = "JCS";

    record Vocab(String context, String proof, String id, String type) {
    };

    Document.Adapter createAdapter(Map<String, Object> document);

    Document.Updater createUpdater(Map<String, Object> document);

    Vocab vocab();

}
