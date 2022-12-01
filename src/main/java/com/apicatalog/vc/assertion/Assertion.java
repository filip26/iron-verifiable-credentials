package com.apicatalog.vc.assertion;

import jakarta.json.JsonValue;

public class Assertion {

    protected AssertionScope scope;
    
    public Assertion(AssertionScope scope) {
        this.scope = scope;
    }
    
    public boolean apply(JsonValue value) {
        return false;
    }
    
    public static ObjectAssertion thatProof() {
        return null;
    }
        
    
//    enum Location {
//        Subject,
//        Issuer,
//        Proof
//    }
//    
//    Location location();
//    
//    String property();
//    String vocabulary();
//    
//    boolean match(JsonValue value);
    
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
