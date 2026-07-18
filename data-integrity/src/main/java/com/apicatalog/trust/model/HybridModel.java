package com.apicatalog.trust.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.processor.DocumentProcessor;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.proof.ProofCursor;

public class HybridModel implements ProcessingModel {

    private final ProcessingModel[] models;

    public HybridModel(ProcessingModel... models) {
        this.models = models;
    }

    public ProcessingModel[] models() {
        return models;
    }

    @Override
    // TODO add context as parameter
    public DocumentProcessor createProcessor(Map<String, Object> document) {

        var context = ContextAwareResolver.getContexts(document);

        var processor = new Processor();

        var processors = new ArrayList<DocumentProcessor>(models.length);

        for (var model : models) {
            var modelProcessor = model.createProcessor(document);
            if (modelProcessor != null) {
                processors.add(modelProcessor);
            }
        }

        processors.trimToSize();

        processor.context = context;
        processor.document = document;
        processor.processors = processors;

        return processor;
    }

    public static class Processor implements DocumentProcessor {

        Collection<DocumentProcessor> processors;
        Collection<String> context;
        Map<String, Object> document;

        @Override
        public ProofCursor createProofCursor() {
            List<ProofCursor> cursors = null;

            for (var processor : processors) {

                var cursor = processor.createProofCursor();
                if (cursor == null) {
                    continue;
                }

                if (cursors == null) {
                    cursors = new ArrayList<>(processors.size());
                }

                cursors.add(cursor);
            }

            if (cursors == null) {
                return null;
            }

            if (cursors.size() == 1) {
                return cursors.get(0);
            }

            return new Cursor(cursors);
        }

        @Override
        public PayloadGenerator createPayload() {
            throw new UnsupportedOperationException();
        }

    }

    private static class Cursor implements ProofCursor {

        final Collection<ProofCursor> cursors;
        ProofCursor accepted;

        Cursor(Collection<ProofCursor> cursors) {
            this.cursors = cursors;
            this.accepted = null;
        }

        @Override
        public boolean next() {

            Boolean next = null;

            for (var cursor : cursors) {
                if (next == null) {
                    next = cursor.next();
                    continue;
                }
                if (next != cursor.next()) {
                    throw new IllegalArgumentException();
                }
            }

            if (next == null) {
                throw new IllegalArgumentException();
            }

            accepted = null;
            return next;
        }

        @Override
        public boolean isAccepted() {
            if (accepted != null) {
                return true;
            }

            for (var cursor : cursors) {
                if (cursor.isAccepted()) {
                    accepted = cursor;
                    return true;
                }
            }

            return false;
        }

        @Override
        public Proof proof() {
            if (accepted == null) {
                for (var cursor : cursors) {
                    if (cursor.isAccepted()) {
                        accepted = cursor;
                        return cursor.proof();
                    }
                }
                return null;
            }
            return accepted.proof();
        }
    }

    @Override
    public Vocab vocab() {
        throw new UnsupportedOperationException();
    }
}
