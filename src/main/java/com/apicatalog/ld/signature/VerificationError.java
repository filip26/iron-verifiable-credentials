package com.apicatalog.ld.signature;

public class VerificationError extends Throwable {

    public enum Code {
        Unknown,
        Expired,
        InvalidSignature,
        InvalidProofDomain,
        UnknownCryptoSuite,
        UnknownVerificationKey, 
        UnknownVerificationMethod,
    }

    private static final long serialVersionUID = -3280731333804856855L;

    private Code code;

    public VerificationError() {
        this(Code.Unknown);
    }

    public VerificationError(Code type) {
        super(type.name());
        this.code = type;
    }

    public VerificationError(Throwable e) {
        this(Code.Unknown, e);
    }

    public VerificationError(Code code, Throwable e) {
        super(e);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }
}
