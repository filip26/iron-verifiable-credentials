package com.apicatalog.vc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.key.KeyPair;

@DisplayName("Keys Generation")
@TestMethodOrder(OrderAnnotation.class)
class VcKeyGenTest {

    @DisplayName("Data Integrity")
    @Order(1)
    @Disabled
    @Test
    void generate32L() throws KeyGenError {
        KeyPair kp = Vc.generateKeys("https://w3id.org/security#Ed25519KeyPair2020").get(URI.create("urn:1"), 256);
        assertNotNull(kp);
        assertEquals("urn:1", kp.id());
        assertEquals("https://w3id.org/security#Ed25519KeyPair2020", kp.type());
        assertNotNull(kp.publicKey());
        assertNotNull(kp.privateKey());
        assertEquals(32, kp.publicKey().length);
        assertEquals(32, kp.privateKey().length);
    }
}
