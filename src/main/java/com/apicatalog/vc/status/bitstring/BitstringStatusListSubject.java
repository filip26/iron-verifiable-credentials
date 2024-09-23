package com.apicatalog.vc.status.bitstring;

import java.util.Collection;

import com.apicatalog.vc.subject.Subject;

public interface BitstringStatusListSubject extends Subject {

    Collection<String> type();
    
    Collection<String> statusPurpose();
    
    Bitstring encodedList();
    
    long ttl();
}
