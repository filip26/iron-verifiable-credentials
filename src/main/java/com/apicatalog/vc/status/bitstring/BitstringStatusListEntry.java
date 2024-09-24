package com.apicatalog.vc.status.bitstring;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.vc.status.Status;

public interface BitstringStatusListEntry extends Status {

    String purpose();

    long index();

    URI credential();

    int indexBitLength();

    BitstringStatusListMessages messages();
    
    Collection<URI> references();

}
