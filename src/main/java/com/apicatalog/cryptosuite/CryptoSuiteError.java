package com.apicatalog.cryptosuite;

public class CryptoSuiteError extends Throwable {

    private static final long serialVersionUID = 1850026823047424380L;

    public enum CryptoSuiteErrorCode {
        Canonicalization, 
        Digest, 
        Signature,
    }

    private CryptoSuiteErrorCode code;

    public CryptoSuiteError(CryptoSuiteErrorCode code) {
        this(code, code.name());
    }

    public CryptoSuiteError(CryptoSuiteErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public CryptoSuiteError(CryptoSuiteErrorCode code, Throwable e) {
        this(code, code.name(), e);
    }

    public CryptoSuiteError(CryptoSuiteErrorCode code, String message, Throwable e) {
        super(message, e);
        this.code = code;
    }

    public CryptoSuiteErrorCode getCode() {
        return code;
    }
}
