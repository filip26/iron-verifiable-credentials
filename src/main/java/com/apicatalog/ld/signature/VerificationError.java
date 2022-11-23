package com.apicatalog.ld.signature;

public class VerificationError extends Throwable {

    private static final long serialVersionUID = 5031043246158660527L;

    public enum Code {
        Expired, NotValidYet,

        InvalidSignature, InvalidProofDomain,

        UnknownCryptoSuite, UnknownVerificationKey, UnknownVerificationMethod,
        
        LinkedDataSignature,
    }

    private Code code;

    public VerificationError(Code code) {
        super(code.name());
        this.code = code;
    }

//    public VerificationError(Throwable e) {
//        this(Code.Internal, e);
//    }

    public VerificationError(Code code, Throwable e) {
        super(code.name(), e);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }
}
