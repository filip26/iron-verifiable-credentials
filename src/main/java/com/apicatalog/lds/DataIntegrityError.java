package com.apicatalog.lds;

public class DataIntegrityError extends Throwable {

    private static final long serialVersionUID = -7146533158378348477L;

    public enum ErrorType {
        Missing,
        Unknown,
        Invalid
    }
    
    //FIXME rethinks codes, status: [Missing|Invalid] property: [name] attribute: [length|encoding|..]
    @Deprecated
    public enum Code {
        Unknown,

        UnknownCryptoSuiteType,

        MissingProof,
        InvalidProof,

        MissingProofType,
        InvalidProofType,

        MissingProofValue,
        InvalidProofValue,
        InvalidProofValueLength,

        MissingProofPurpose,
        InvalidProofPurpose,

        MissingVerificationMethod,
        InvalidVerificationMethod,

        MissingCreated,
        InvalidCreated,
    }

    @Deprecated
    private Code code;

    private ErrorType type;
    private String subject;
    private String property;
    
    @Deprecated
    public DataIntegrityError() {

    }

    @Deprecated
    public DataIntegrityError(Code type) {
        super();
        this.code = type;
    }
    
    public DataIntegrityError(ErrorType type, String subject) {
        this(type, subject, null);
    }
    
    public DataIntegrityError(ErrorType type, String subject, String property) {
        super();
        this.type = type;
        this.subject = subject;
        this.property = property;
    }

    public DataIntegrityError(Throwable e) {
        super(e);
    }

    public DataIntegrityError(Code code, Throwable e) {
        super(e);
        this.code = code;
    }

    public ErrorType getType() {
        return type;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public String getProperty() {
        return property;
    }
}
