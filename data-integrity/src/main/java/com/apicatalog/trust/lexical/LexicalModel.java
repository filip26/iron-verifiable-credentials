package com.apicatalog.trust.lexical;

import java.util.Map;
import java.util.function.Function;

import com.apicatalog.trust.Document.Updater;
import com.apicatalog.trust.model.ContextAwareResolver;
import com.apicatalog.trust.model.Model;

public class LexicalModel implements Model {

    private final LexicalAccessor.Factory processorFactory;
    private final PropertyMapProofCursor.Factory cursorFactory;
    private final Map<String, PropertyMapProofMapper> proofReaders;

    private final Function<Map<String, Object>, byte[]> canonize;

    private final Vocab vocab;

    public LexicalModel(
            Vocab vocab,
            LexicalAccessor.Factory processorFactory,
            PropertyMapProofCursor.Factory cursorFactory,
            Function<Map<String, Object>, byte[]> canonize,
            Map<String, PropertyMapProofMapper> proofReaders) {
        this.vocab = vocab;
        this.processorFactory = processorFactory;
        this.cursorFactory = cursorFactory;
        this.canonize = canonize;
        this.proofReaders = proofReaders;
    }

    @Override
    public LexicalAccessor createAdapter(Map<String, Object> document) {
        return processorFactory.createAdapter(
                this,
                ContextAwareResolver.getContexts(document),
                document);
    }

    @Override
    public Updater createUpdater(Map<String, Object> document) {
        return new LexicalUpdater(this, createAdapter(document));
    }

    public PropertyMapProofCursor createCursor(LexicalAccessor processor) {
        return cursorFactory.newInstance(this, processor);
    }

    public PropertyMapPayloadGenerator createPayload(Map<String, Object> document) {
        return createPayload(createAdapter(document));
    }

    public PropertyMapPayloadGenerator createPayload(LexicalAccessor processor) {
        return new PropertyMapPayloadGenerator(this, processor);
    }

    public byte[] canonize(Map<String, Object> data) {
        return canonize.apply(data);
    }

    public PropertyMapProofMapper reader(String proofType) {
        return proofReaders.get(proofType);
    }

    @Override
    public Vocab vocab() {
        return vocab;
    }
}
