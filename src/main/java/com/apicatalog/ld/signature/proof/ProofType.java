package com.apicatalog.ld.signature.proof;

import java.net.URI;

import com.apicatalog.ld.signature.SignatureSuite;

public interface ProofType {

    // proof type id
    URI id();   

    
    
    // property
//    String name();
//    String vocabulary();
    
    //TODO how to deal with embedded properties? a selector?
    // assertThat(subject).hasId()
    // assertThat(subject).hasC(propery).hasId()
    
    
    /* real instances
    Subject domain();
    Subject challenge();
    
    ProofType assertThat(Subject).isString(),usEqyalto(value);
    */
    
}
