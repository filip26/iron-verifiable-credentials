package com.apicatalog.trust.semantic;

import java.util.Map;
import java.util.SequencedCollection;
import java.util.function.BiFunction;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.Document.Processor;

// TODO make it interface allowing single processor or hybrid processor instances
public class ProcessorFactory {

    final BiFunction<SequencedCollection<?>, Map<String, ?>, Document.Processor> processorFactory;
    final Document.Model[] models;

    private ProcessorFactory(BiFunction<SequencedCollection<?>, Map<String, ?>, Document.Processor> processorFactory, Document.Model[] models) {
        this.processorFactory = processorFactory;
        this.models = models;
    }

    public Processor newInstance(SequencedCollection<?> context, Map<String, ?> document) {

        
        
        return null;
    }

}
