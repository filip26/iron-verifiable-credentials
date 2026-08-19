package com.apicatalog.trust.lexical;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import com.apicatalog.trust.Document.Updater;
import com.apicatalog.trust.model.Model;

public class LexicalModel implements Model {

    private final LexicalAccessor.Factory processorFactory;
    private final PropertyProofCursor.Factory cursorFactory;
    private final Map<String, PropertyProofMapper> proofReaders;

    private final Function<Map<String, ?>, byte[]> canonize;

    private final Vocab vocab;

    public LexicalModel(
            Vocab vocab,
            LexicalAccessor.Factory processorFactory,
            PropertyProofCursor.Factory cursorFactory,
            Function<Map<String, ?>, byte[]> canonize,
            Map<String, PropertyProofMapper> proofReaders) {
        this.vocab = vocab;
        this.processorFactory = processorFactory;
        this.cursorFactory = cursorFactory;
        this.canonize = canonize;
        this.proofReaders = proofReaders;
    }

    @Override
    public LexicalAccessor createAccessor(Collection<?> context, Map<String, ?> document) {
        return processorFactory.createAdapter(
                this,
                context,
                document);
    }

    @Override
    public Updater createUpdater(Collection<?> context, Map<String, ?> document) {
        return new LexicalUpdater(this, createAccessor(context, document));
    }

    public PropertyProofCursor createCursor(LexicalAccessor processor) {
        return cursorFactory.newInstance(this, processor);
    }

    public PropertyMapPayloadGenerator createPayload(Collection<?> context, Map<String, ?> document) {
        return createPayload(createAccessor(context, document));
    }

    public PropertyMapPayloadGenerator createPayload(LexicalAccessor processor) {
        return new PropertyMapPayloadGenerator(this, processor);
    }

    public byte[] canonize(Map<String, ?> data) {
        return canonize.apply(data);
    }

    public PropertyProofMapper reader(String proofType) {
        return proofReaders.get(proofType);
    }

    @Override
    public Vocab vocab() {
        return vocab;
    }
}
