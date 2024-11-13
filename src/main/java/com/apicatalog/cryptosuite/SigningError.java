package com.apicatalog.cryptosuite;

public class SigningError extends CryptoSuiteError {

    private static final long serialVersionUID = 5560695193588466945L;

    public enum SignatureErrorCode {
        Expired, Unknown,
        //InvalidKey?
    }

    private SignatureErrorCode code;

    public SigningError(SignatureErrorCode code) {
        super(CryptoSuiteErrorCode.Signature, code.name());
        this.code = code;
    }

    public SigningError(Throwable e, SignatureErrorCode code) {
        super(CryptoSuiteErrorCode.Signature, code.name(), e);
        this.code = code;
    }

    public SignatureErrorCode signatureErrorCode() {
        return code;
    }
}
