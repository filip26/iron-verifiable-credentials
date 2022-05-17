package com.apicatalog.vc;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import jakarta.json.Json;

class CredentialsTest {

    @Test
    void testFromJsonObject() {
        final Credentials credentials = Credentials.from(Json.createObjectBuilder().build());
        assertNotNull(credentials);
    }
    
}
