package com.apicatalog.trust;

import java.util.Map;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.function.Predicate;

import com.apicatalog.trust.model.Model.Vocab;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.ProofCursor;

public interface Document {

    public interface Model {

        static final String C14N_RDFC = "RDFC";
        static final String C14N_JCS = "JCS";

        String id();

        String c14n();

        Predicate<SequencedCollection<?>> contextMatcher();

        Set<String> types();

    }

    interface Processor {
        Accessor createAccessor(Model model, SequencedCollection<?> context, Map<String, ?> document);

        Updater createUpdater(Model model, SequencedCollection<?> context, Map<String, ?> document);
    }

    interface Accessor {

        Object document();

        ProofCursor createProofCursor();

    }

    interface Updater {

        PayloadGenerator createPayload();

        void addProof(Map<String, ?> compacted);

        Map<String, ?> compact();

        Vocab vocab();
    }

}
