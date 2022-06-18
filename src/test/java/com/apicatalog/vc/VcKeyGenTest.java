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
import com.apicatalog.vc.api.Vc;

@DisplayName("Keys Generation")
@TestMethodOrder(OrderAnnotation.class)
class VcKeyGenTest {

    @DisplayName("Data Integrity")
    @Order(1)
    @Disabled   //TODO SunSignatureProvider does not support key generation
    @Test
    void generate32L() throws KeyGenError {
        KeyPair kp = Vc.generateKeys("https://w3id.org/security#Ed25519KeyPair2020").get(URI.create("urn:1"), 256);
        assertNotNull(kp);
        assertEquals("urn:1", kp.getId());
        assertEquals("https://w3id.org/security#Ed25519KeyPair2020", kp.getType());
        assertNotNull(kp.getPublicKey());
        assertNotNull(kp.getPrivateKey());
        assertEquals(32, kp.getPublicKey().length);
        assertEquals(32, kp.getPrivateKey().length);
    }
}
