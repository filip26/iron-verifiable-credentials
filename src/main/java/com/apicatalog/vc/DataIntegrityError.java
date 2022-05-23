package com.apicatalog.vc;

public class DataIntegrityError extends Throwable {

    private static final long serialVersionUID = -7146533158378348477L;
    
    public DataIntegrityError() {
        super();
    }

    public DataIntegrityError(Throwable e) {
        super(e);
    }
}
