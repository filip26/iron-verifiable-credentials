package com.apicatalog.trust.model;

import java.util.Map;
import java.util.function.Function;

import com.apicatalog.trust.processor.MapProcessor;
import com.apicatalog.trust.proof.MapProofCursor;
import com.apicatalog.trust.proof.MapProofReader;

public class LexicalModel implements ProcessingModel {

    private final MapProcessor.Factory processorFactory;
    private final MapProofCursor.Factory cursorFactory;
    private final Map<String, MapProofReader> proofReaders;

    private final String c14n;
    private final Function<Map<String, Object>, byte[]> canonize;

    private final Vocab vocab;
    
    public LexicalModel(
            Vocab vocab,
            MapProcessor.Factory processorFactory,
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
    public MapProcessor createProcessor(Map<String, Object> document) {
        return processorFactory.createProcessor(
                this,
                ContextAwareResolver.getContexts(document),
                document);
    }

    public MapProofCursor createCursor(MapProcessor processor) {
        return cursorFactory.newInstance(this, processor);
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
}
