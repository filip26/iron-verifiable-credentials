package com.apicatalog.vc;

public class VerificationError extends Throwable {

    public enum Type {
        Unknown,
        UnknownCryptoSuiteType,
        InvalidProofValue,
        InvalidProofLenght, 
    }
    
    private static final long serialVersionUID = -3280731333804856855L;

    private Type type;

    public VerificationError() {
        this(Type.Unknown);
    }

    public VerificationError(Type type) {
        super();
        this.type = type;
    }

    public VerificationError(Throwable e) {
        this(Type.Unknown, e);
    }

    public VerificationError(Type type, Throwable e) {
        super(e);
        this.type = type;
    }
    
    public Type getType() {
        return type;
    }
}
