package com.apicatalog.vc.status.bitstring;

import java.time.Instant;

import com.apicatalog.vc.Credential;

public interface BitstringStatusListCredential extends Credential {

    Instant validFrom();
    Instant validUntil();

}
