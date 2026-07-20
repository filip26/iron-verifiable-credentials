package com.apicatalog.trust;

import java.util.Map;

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

        Map<String, ?> compacted();
    }

}
