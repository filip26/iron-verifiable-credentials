package com.apicatalog.trust.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    public ProofCursor createProofCursor(Collection<String> context, Map<String, Object> document) {

        List<ProofCursor> cursors = null;

        for (var model : models) {

            var cursor = model.createProofCursor(context, document);
            if (cursor == null) {
                continue;
            }

            if (cursors == null) {
                cursors = new ArrayList<>(models.length);
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
}
