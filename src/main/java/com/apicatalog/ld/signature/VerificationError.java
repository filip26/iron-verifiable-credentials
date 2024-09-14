package com.apicatalog.ld.signature;

public class VerificationError extends Exception {

    private static final long serialVersionUID = -7137366427204418675L;

    public enum Code {
        Expired,
        NotValidYet,
        InvalidSignature,
        UnsupportedCryptoSuite,
        Internal,
    }

    private Code code;

    public VerificationError(Code code) {
        super(code.name());
        this.code = code;
    }

    public VerificationError(Code code, Throwable e) {
        super(code.name(), e);
        this.code = code;
    }

    public Code code() {
        return code;
    }
}
