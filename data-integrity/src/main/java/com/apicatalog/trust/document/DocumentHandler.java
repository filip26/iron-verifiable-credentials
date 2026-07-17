package com.apicatalog.trust.document;

import com.apicatalog.tree.io.TreeEmitter;
import com.apicatalog.trust.proof.Proof;

public interface DocumentHandler {

    <T> T adapt(Class<T> type);
    
    void add(Proof proof);
    
    void write(TreeEmitter emitter);
}
