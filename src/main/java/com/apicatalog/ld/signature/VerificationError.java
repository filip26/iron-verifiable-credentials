package com.apicatalog.ld.signature;

public class VerificationError extends CryptoSuiteError {
    
    private static final long serialVersionUID = -7137366427204418675L;

    public enum VerificationErrorCode {
        Expired,
        NotValidYet,
        InvalidSignature,
        UnsupportedCryptoSuite,
        Internal,
    }

    private VerificationErrorCode code;

    public VerificationError(VerificationErrorCode code) {
        super(CryptoSuiteErrorCode.Signature, code.name());
        this.code = code;
    }

    public VerificationError(VerificationErrorCode code, Throwable e) {
        super(CryptoSuiteErrorCode.Signature, code.name(), e);
        this.code = code;
    }

    public VerificationErrorCode verificationErrorCode() {
        return code;
    }
}
