package com.apicatalog.did;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@DisplayName("DID Key")
@TestMethodOrder(OrderAnnotation.class)
class DidKeyTest {

    @Test
    void t1() {
        DidKey key = DidKey.create(URI.create("did:key:z6MkpTHR8VNsBxYAAWHut2Geadd9jSwuBV8xRoAnwWsdvktH"));
        assertNotNull(key);
    }

    @Test
    void t2() {
        DidKey key = DidKey.create(URI.create("did:key:z6MkiTBz1ymuepAQ4HEHYSF1H8quG5GLVVQR3djdX3mDooWp"));
        assertNotNull(key);
    }

    @Test
    void t3() {
        DidKey key = DidKey.create(URI.create("did:key:z6MkjchhfUsD6mmvni8mCdXHw216Xrm9bQe2mBH1P5RDjVJG"));
        assertNotNull(key);
    }

    @Test
    void t4() {
        DidKey key = DidKey.create(URI.create("did:key:z6MknGc3ocHs3zdPiJbnaaqDi58NGb4pk1Sp9WxWufuXSdxf"));
        assertNotNull(key);
    }

}
