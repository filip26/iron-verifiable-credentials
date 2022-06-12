package com.apicatalog.ld.signature;

public class SigningError extends Throwable {

    private static final long serialVersionUID = 8441020241391845866L;

    public enum Code {
        Unknown,
        Expired,
    }

    private Code code;

    public SigningError() {
        this(Code.Unknown);
    }

    public SigningError(Code type) {
        super();
        this.code = type;
    }

    public SigningError(Throwable e) {
        this(Code.Unknown, e);
    }

    public SigningError(Code code, Throwable e) {
        super(e);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }
}
