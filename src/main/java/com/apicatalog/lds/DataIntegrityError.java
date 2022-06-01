package com.apicatalog.lds;

public class DataIntegrityError extends Throwable {

    private static final long serialVersionUID = -7146533158378348477L;
    
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
    
    private Code code;

    public DataIntegrityError() {
        this(Code.Unknown);
    }

    public DataIntegrityError(Code type) {
        super();
        this.code = type;
    }

    public DataIntegrityError(Throwable e) {
        this(Code.Unknown, e);
    }

    public DataIntegrityError(Code code, Throwable e) {
        super(e);
        this.code = code;
    }
    
    public Code getCode() {
        return code;
    }
}
