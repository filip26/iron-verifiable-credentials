package com.apicatalog.trust.model;

import java.util.Map;

import com.apicatalog.trust.processor.PayloadProcessor;

public interface ProcessorFactory {


    PayloadProcessor createProcessor(Map<String, Object> document);

    // TODO accepted proof types, for configuration dump

    // TODO proof predicate or selector returns proof graph or null
    // Function<String[], String>;

}
