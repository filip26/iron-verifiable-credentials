package com.apicatalog.ld.signature;

public class KeyGenError extends Throwable {

    private static final long serialVersionUID = 6112565832303740777L;

    public enum Code {
        Unknown,
        UnknownCryptoSuite,
    }

    private Code code;

    public KeyGenError() {
        this(Code.Unknown);
    }

    public KeyGenError(Code type) {
        super();
        this.code = type;
    }

    public KeyGenError(Throwable e) {
        this(Code.Unknown, e);
    }

    public KeyGenError(Code code, Throwable e) {
        super(e);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }
}
