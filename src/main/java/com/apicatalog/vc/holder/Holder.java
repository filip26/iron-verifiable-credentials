package com.apicatalog.vc.holder;

import java.util.Collection;

import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vc.verifier.Processor;

import jakarta.json.JsonObject;

public class Holder extends Processor {

    protected Holder(final SignatureSuite... suites) {
        super(suites);
    }
    
    public static Holder with(final SignatureSuite... suites) {
        return new Holder(suites);
    }

    
    public JsonObject derive(JsonObject document, Collection<String> selectors) {
        //TODO
        return null;
    }
    
}
