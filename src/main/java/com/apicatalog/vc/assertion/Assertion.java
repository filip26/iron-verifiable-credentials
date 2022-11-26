package com.apicatalog.vc.assertion;

import jakarta.json.JsonValue;

public interface Assertion {

    enum Location {
        Subject,
        Issuer,
        Proof
    }
    
    Location location();
    
    String property();
    String vocabulary();
    
    boolean match(JsonValue value);
    
    // isTrue
    // equals 
    // isGreater
    // ...
    // isPresent
    // isEmpty
    
    /*
     * 
     * 
     *  when vc type is and/or proof type is and/or subject type is/and or issuer type is ...
     *  import * dataAssertions
     *    Vc.verify()
     *    //TODO per suite
     *      .assert(Proof, "domain", domain -> "xxxx".equals(domain)
     *      .assert(proofDomain("xxxx"))
     *      .assert(subjectId(""))
     *       .assert(proofCreated(), created -> Instant.now().after(created))
     *       
     *      .isValid()
     *      
     * 
     */
    
}
