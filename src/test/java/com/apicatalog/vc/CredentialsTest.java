package com.apicatalog.vc;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.apicatalog.vc.Credentials;

class CredentialsTest {

    @Test
    void testFromJsonObject() {
        
        final Credentials credentials = Credentials.from(null);
        assertNotNull(credentials);
        
        
    }
    
}
