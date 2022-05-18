package com.apicatalog.vc;

import java.io.Closeable;

public interface VcReader extends Closeable {

    StructuredData read();
    
    Credentials readCredentials();
    
    Presentation readPresentation();
    
    @Override
    void close();
}
