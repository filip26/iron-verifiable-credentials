package com.apicatalog.trust.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.Document.Updater;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.proof.ProofCursor;

public class HybridModel implements Model {

    private final Model[] models;

    public HybridModel(Model... models) {
        this.models = models;
    }

    public Model[] models() {
        return models;
    }

    @Override
    // TODO add context as parameter
    public Document.Accessor createAccessor(Collection<?> context, Map<String, ?> document) {

        var hybrid = new Adapter();

        var adapters = new ArrayList<Document.Accessor>(models.length);

        for (var model : models) {
            var modelProcessor = model.createAccessor(context, document);
            if (modelProcessor != null) {
                adapters.add(modelProcessor);
            }
        }

        adapters.trimToSize();

        hybrid.context = context;
        hybrid.document = document;
        hybrid.acessors = adapters;

        return hybrid;
    }

    public static class Adapter implements Document.Accessor {

        SequencedCollection<Document.Accessor> acessors;
        Collection<?> context;
        Map<String, ?> document;
        Object data;

        @Override
        public ProofCursor createProofCursor() {
            List<ProofCursor> cursors = null;

            for (var accessor : acessors) {

                var cursor = accessor.createProofCursor();
                if (cursor == null) {
                    continue;
                }

                if (cursors == null) {
                    cursors = new ArrayList<>(acessors.size());
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
        public Object document() {
            if (data == null) {
                data = acessors.getFirst().document();
            }
            return data;
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
    public Updater createUpdater(Collection<?> context, Map<String, ?> document) {
        throw new UnsupportedOperationException();
    }
}
