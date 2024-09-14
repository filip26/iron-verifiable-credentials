package com.apicatalog.ld.signature;

public class SigningError extends Exception {

    private static final long serialVersionUID = 5560695193588466945L;

    public enum Code {
        Expired,
        UnsupportedCryptoSuite,
        Internal,
        //InvalidKey?
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

    public Code code() {
        return code;
    }
}
