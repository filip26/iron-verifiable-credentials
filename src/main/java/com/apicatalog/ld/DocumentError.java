package com.apicatalog.ld;

public class DocumentError extends Throwable {

    private static final long serialVersionUID = -7146533158378348477L;

    public enum ErrorType {
        //TODO merge Unknown and Invalid?
        Missing,
        Unknown,
     Invalid,
        //Malformed,
    }

    private final ErrorType type;
    private final String subject;

    public DocumentError(ErrorType type, String subject) {
        super();
        this.type = type;
        this.subject = subject;
    }

    public DocumentError(ErrorType type, String subject, Throwable e) {
        super(e);
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
