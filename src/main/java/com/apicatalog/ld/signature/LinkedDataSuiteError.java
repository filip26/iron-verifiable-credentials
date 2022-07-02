package com.apicatalog.ld.signature;

public class LinkedDataSuiteError extends Throwable {

    private static final long serialVersionUID = 1850026823047424380L;

    public enum Code {
    Canonicalization,
    Digest,
    }

    private Code code;

    public LinkedDataSuiteError(Code type) {
        super(type.name());
        this.code = type;
    }

    public LinkedDataSuiteError(Code code, Throwable e) {
        super(code.name(), e);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }
}
