package com.apicatalog.trust.model;

import java.util.Map;
import java.util.SequencedCollection;

import com.apicatalog.trust.Document;

// processing model
//FIXME move to Document.Model
public interface Model {

    static final String C14N_RDFC = "RDFC";
    static final String C14N_JCS = "JCS";

    //FIXME make model specific
    @Deprecated
    record Vocab(String context, String proof, String id, String type) {
    };

    //FIXME mode to Document.Processor
    Document.Accessor createAccessor(SequencedCollection<?> context, Map<String, ?> document);

    Document.Updater createUpdater(SequencedCollection<?> context, Map<String, ?> document);

}
