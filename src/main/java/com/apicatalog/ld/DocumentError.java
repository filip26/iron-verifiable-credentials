package com.apicatalog.ld;

import com.apicatalog.ld.signature.proof.ProofProperty;

public class DocumentError extends Throwable {

    private static final long serialVersionUID = -7146533158378348477L;

    public enum ErrorType {
        Missing,
        Unknown,
        Invalid,
    }

    private final ErrorType type;
    private final String subject;
    private final String[] attibutes;
    
    public DocumentError(ErrorType type, ProofProperty property) {
    	this(type, "Proof", property.name());
    }
    
    //TODO revise
    public DocumentError(ErrorType type, String subject, String... attributes) {
        super();
        this.type = type;
        this.subject = subject;
        this.attibutes = attributes;
    }

    public ErrorType getType() {
        return type;
    }

    public String getSubject() {
        return subject;
    }

    public String[] getAttibutes() {
        return attibutes;
    }
}
