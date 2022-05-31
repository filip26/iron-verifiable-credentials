package com.apicatalog.vc;

public class SigningError extends Throwable {

    public enum Code {
        Unknown,
    }
    
    private static final long serialVersionUID = -3280731333804856855L;

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
