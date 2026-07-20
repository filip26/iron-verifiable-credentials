package com.apicatalog.trust;

import com.apicatalog.tree.io.TreeEmitter;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.proof.ProofCursor;

public interface Document {

    interface Adapter {
//TODO       
//      <T> T adapt(Class<T> type);
//      boolean hasProofs();
        
        ProofCursor createProofCursor();

    }

    interface Updater {
        
        
        PayloadGenerator createPayload();
        
        void addProof(Proof proof);

        void write(TreeEmitter emitter);
    }

}
