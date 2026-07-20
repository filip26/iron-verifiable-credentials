package com.apicatalog.trust.lexical;

import java.util.Map;
import java.util.function.Function;

import com.apicatalog.trust.Document.Updater;
import com.apicatalog.trust.model.ContextAwareResolver;
import com.apicatalog.trust.model.Model;
import com.apicatalog.trust.model.Model.Vocab;

public class LexicalModel implements Model {

    private final LexicalAdapter.Factory processorFactory;
    private final MapProofCursor.Factory cursorFactory;
    private final Map<String, MapProofReader> proofReaders;

    private final String c14n;
    private final Function<Map<String, Object>, byte[]> canonize;

    private final Vocab vocab;

    public LexicalModel(
            Vocab vocab,
            LexicalAdapter.Factory processorFactory,
            MapProofCursor.Factory cursorFactory,
            String c14n,
            Function<Map<String, Object>, byte[]> canonize,
            Map<String, MapProofReader> proofReaders) {
        this.vocab = vocab;
        this.processorFactory = processorFactory;
        this.cursorFactory = cursorFactory;
        this.c14n = c14n;
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

//    @Override
//    public String c14n() {
//        return c14n;
//    }

    public MapProofReader reader(String proofType) {
        return proofReaders.get(proofType);
    }

    @Override
    public Vocab vocab() {
        return vocab;
    }

    @Override
    public Updater createUpdater(Map<String, Object> document) {
        // TODO Auto-generated method stub
        return null;
    }
}
