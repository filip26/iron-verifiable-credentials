package com.apicatalog.vc.holder;

import java.net.URI;

import com.apicatalog.ld.DocumentError;

public interface PresentationHolder {

    URI id();

    void validate() throws DocumentError;

}
