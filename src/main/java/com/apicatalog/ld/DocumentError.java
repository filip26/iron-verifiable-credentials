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
    
    public DocumentError(ErrorType type, ProofProperty property) {
    	this(type, "Proof".concat(property.name()));
    }
    
    //TODO revise
    public DocumentError(ErrorType type, String subject) {
        super();
        this.type = type;
        this.subject = subject;
    }

    public ErrorType getType() {
        return type;
    }

    public String getSubject() {
        return subject;
    }
}
