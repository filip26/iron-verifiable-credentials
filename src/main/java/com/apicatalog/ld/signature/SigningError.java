package com.apicatalog.ld.signature;

public class SigningError extends Throwable {

    private static final long serialVersionUID = 8441020241391845866L;

    public enum Code {
        Expired,  
        UnsupportedCryptoSuite,
        Internal,
    }

    private Code code;

    public SigningError(Code code) {
        super(code.name());
        this.code = code;
    }

    public SigningError(Code code, Throwable e) {
        super(code.name(), e);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }
}
