package com.apicatalog.vc;

import java.io.Closeable;

public interface VcReader extends Closeable {

    VcDocument read();

    Credentials readCredentials();

    Presentation readPresentation();

    @Override
    void close();
}
