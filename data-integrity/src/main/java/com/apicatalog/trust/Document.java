package com.apicatalog.trust;

import java.util.Map;

import com.apicatalog.trust.model.Model.Vocab;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.ProofCursor;

public interface Document {

    interface Accessor {

        Object document();

        ProofCursor createProofCursor();

    }

    interface Updater {

        PayloadGenerator createPayload();

        void addProof(Map<String, ?> compacted);

        Map<String, ?> compacted();

        Vocab vocab();
    }

}
