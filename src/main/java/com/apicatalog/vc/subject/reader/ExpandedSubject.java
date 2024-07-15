package com.apicatalog.vc.subject.reader;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.vc.subject.Subject;

public class ExpandedSubject extends Subject {

    public void id(URI id) {
        this.id = id;
    }
    
    public void type(Collection<String> type) {
        this.type = type;
    }
}
