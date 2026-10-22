package com.apicatalog.trust;

import java.util.Map;
import java.util.SequencedCollection;

import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.ProofCursor;

public interface Document {

    @FunctionalInterface
    public interface ContextExtractor {
        SequencedCollection<?> extract(Map<String, ?> document);
    }

    public interface Model {

        static final String C14N_RDFC = "RDFC";
        static final String C14N_JCS = "JCS";

        String id();

        String c14n();

        boolean isAccepted(SequencedCollection<?> context, Map<String, ?> document);

    }

    interface Processor {

        Accessor createAccessor(Model model, SequencedCollection<?> context, Map<String, ?> document);

        Updater createUpdater(Model model, SequencedCollection<?> context, Map<String, ?> document);

    }

    interface Accessor {

        // FIXME use mapper
        @Deprecated
        Object document();

        Mapper createDocumentMapper();

        ProofCursor createProofCursor();

    }

    interface Updater {

        PayloadGenerator createPayload();

        void addProof(Map<String, ?> compacted);

        Map<String, ?> compact();

//        Vocab vocab();
    }

    interface Mapper {

    }
}
