package com.apicatalog.trust.lexical;

import java.util.Map;
import java.util.function.Function;

import com.apicatalog.trust.Document.Updater;
import com.apicatalog.trust.model.ContextAwareResolver;
import com.apicatalog.trust.model.Model;

public class LexicalModel implements Model {

    private final LexicalAdapter.Factory processorFactory;
    private final MapProofCursor.Factory cursorFactory;
    private final Map<String, MapProofReader> proofReaders;

    private final Function<Map<String, Object>, byte[]> canonize;

    private final Vocab vocab;

    public LexicalModel(
            Vocab vocab,
            LexicalAdapter.Factory processorFactory,
            MapProofCursor.Factory cursorFactory,
            Function<Map<String, Object>, byte[]> canonize,
            Map<String, MapProofReader> proofReaders) {
        this.vocab = vocab;
        this.processorFactory = processorFactory;
        this.cursorFactory = cursorFactory;
        this.canonize = canonize;
        this.proofReaders = proofReaders;
    }

    @Override
    public LexicalAdapter createAdapter(Map<String, Object> document) {
        return processorFactory.createProcessor(
                this,
                ContextAwareResolver.getContexts(document),
                document);
    }

    @Override
    public Updater createUpdater(Map<String, Object> document) {
        return new MapUpdater(this, createAdapter(document));
    }

    public MapProofCursor createCursor(LexicalAdapter processor) {
        return cursorFactory.newInstance(this, processor);
    }

    public MapPayloadGenerator createPayload(Map<String, Object> document) {
        return createPayload(createAdapter(document));
    }

    public MapPayloadGenerator createPayload(LexicalAdapter processor) {
        return new MapPayloadGenerator(this, processor);
    }

    public byte[] canonize(Map<String, Object> data) {
        return canonize.apply(data);
    }

    public MapProofReader reader(String proofType) {
        return proofReaders.get(proofType);
    }

    @Override
    public Vocab vocab() {
        return vocab;
    }
}
