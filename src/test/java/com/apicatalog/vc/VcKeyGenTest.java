package com.apicatalog.vc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.apicatalog.lds.key.KeyPair;

@DisplayName("Key Generation Test")
@TestMethodOrder(OrderAnnotation.class)
class VcKeyGenTest {

    @DisplayName("Data Integrity")
    @Order(1)
    @Test
    void generate32L() {
        KeyPair kp = Vc.generateKeys("https://w3id.org/security#Ed25519KeyPair2020");
        assertNotNull(kp);
        assertEquals("https://w3id.org/security#Ed25519KeyPair2020", kp.getType());
        assertNotNull(kp.getPublicKey());
        assertNotNull(kp.getPrivateKey());
        assertEquals(32, kp.getPublicKey().length);
        assertEquals(32, kp.getPrivateKey().length);
    }
}
