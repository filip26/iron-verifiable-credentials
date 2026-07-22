package com.apicatalog.trust.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.Document.Updater;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.proof.ProofCursor;

public class HybridAdapterModel implements Model {

    private final Model[] models;

    public HybridAdapterModel(Model... models) {
        this.models = models;
    }

    public Model[] models() {
        return models;
    }

    @Override
    // TODO add context as parameter
    public Document.Adapter createAdapter(Map<String, Object> document) {

        var context = ContextAwareResolver.getContexts(document);

        var hybrid = new Adapter();

        var adapters = new ArrayList<Document.Adapter>(models.length);

        for (var model : models) {
            var modelProcessor = model.createAdapter(document);
            if (modelProcessor != null) {
                adapters.add(modelProcessor);
            }
        }

        adapters.trimToSize();

        hybrid.context = context;
        hybrid.document = document;
        hybrid.adapters = adapters;

        return hybrid;
    }

    public static class Adapter implements Document.Adapter {

        Collection<Document.Adapter> adapters;
        Collection<String> context;
        Map<String, Object> document;

        @Override
        public ProofCursor createProofCursor() {
            List<ProofCursor> cursors = null;

            for (var processor : adapters) {

                var cursor = processor.createProofCursor();
                if (cursor == null) {
                    continue;
                }

                if (cursors == null) {
                    cursors = new ArrayList<>(adapters.size());
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
            return isAccepted() ? accepted.proof() : null;
        }
    }

    @Override
    public Vocab vocab() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Updater createUpdater(Map<String, Object> document) {
        throw new UnsupportedOperationException();
    }
}
