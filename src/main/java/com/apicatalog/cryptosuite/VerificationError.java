package com.apicatalog.cryptosuite;

public class VerificationError extends Exception {

    private static final long serialVersionUID = -4665370904573792390L;

    public enum VerificationErrorCode {
        Expired,
        NotValidYet,
        InvalidSignature, 
        UnsupportedSignature,
    }

    private VerificationErrorCode code;

    public VerificationError(VerificationErrorCode code) {
//        super(CryptoSuiteErrorCode.Signature, code.name());
        this.code = code;
    }

    public VerificationError(VerificationErrorCode code, Throwable e) {
//        super(CryptoSuiteErrorCode.Signature, code.name(), e);
        super(e);
        this.code = code;
    }

    public VerificationErrorCode code() {
        return code;
    }
}
