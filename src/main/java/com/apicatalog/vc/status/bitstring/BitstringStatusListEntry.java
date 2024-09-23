package com.apicatalog.vc.status.bitstring;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.vc.status.Status;

public interface BitstringStatusListEntry extends Status {

    String purpose();

    int indexList();

    URI credential();

    int size();

    BitstringStatusListMessages messages();
    
    Collection<URI> references();

}
