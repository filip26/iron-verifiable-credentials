package com.apicatalog.vc.status.bitstring;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.vc.status.Status;

@Fragment
@Vocab("https://www.w3.org/ns/credentials/status#")
public interface BitstringStatusListEntry extends Status {

    @Term
    String purpose();

    @Term
    long index();

    @Term
    URI credential();

    @Term
    int indexBitLength();

    @Term
    BitstringStatusListMessages messages();

    @Term
    Collection<URI> references();
}
