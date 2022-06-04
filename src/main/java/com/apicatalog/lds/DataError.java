package com.apicatalog.lds;

public class DataError extends Throwable {

    private static final long serialVersionUID = -7146533158378348477L;

    public enum ErrorType {
        Missing,
        Unknown,
        Invalid
    }

    private final ErrorType type;
    private final String subject;
    private final String[] attibutes;

    @Deprecated
    public DataError() {
        this.type = null;
        this.subject = null;
        this.attibutes = null;
    }


    @Deprecated
    public DataError(Throwable e) {
        super(e);
        this.type = null;
        this.subject = null;
        this.attibutes = null;
    }

    public DataError(ErrorType type, String subject) {
        super();
        this.type = type;
        this.subject = subject;
        this.attibutes = null;
    }

    public DataError(ErrorType type, String subject, String... attributes) {
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