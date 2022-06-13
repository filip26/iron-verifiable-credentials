package com.apicatalog.did;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.multicodec.Multicodec.Codec;

@DisplayName("DID Key")
@TestMethodOrder(OrderAnnotation.class)
class DidKeyTest {
    
    @DisplayName("Create DID key from string")
    @ParameterizedTest(name = "{0}")
    @MethodSource({ "testVectors" })
    void createFromString(DidKeyTestCase testCase) {

        try {
        
            final DidKey didKey = DidKey.create(testCase.uri);
            
            if (testCase.negative) {
                fail("Expected failure but got " + didKey);
                return;
            }

            assertNotNull(didKey);
            assertNotNull(didKey.getRawKey());
            assertEquals(testCase.version, didKey.getVersion());
            assertEquals(testCase.codec, didKey.getCodec());
            assertEquals(testCase.keyLength, didKey.getRawKey().length);

        } catch (IllegalArgumentException | NullPointerException e) {
            if (!testCase.negative) {
                e.printStackTrace();
                fail(e);
            }
        }
    }

    static final Stream<DidKeyTestCase> testVectors() throws JsonLdError, IOException {
        return Arrays.stream(testCases);
    }    
    
    static final DidKeyTestCase testCases[] = new DidKeyTestCase[] {
            DidKeyTestCase.create(
                    "did:key:z6MkpTHR8VNsBxYAAWHut2Geadd9jSwuBV8xRoAnwWsdvktH",
                    Codec.Ed25519PublicKey,
                    32
                    ),
            DidKeyTestCase.create(
                    "did:key:z6MkiTBz1ymuepAQ4HEHYSF1H8quG5GLVVQR3djdX3mDooWp",
                    Codec.Ed25519PublicKey,
                    32
                    ),
            DidKeyTestCase.create(
                    "did:key:z6MkjchhfUsD6mmvni8mCdXHw216Xrm9bQe2mBH1P5RDjVJG",
                    Codec.Ed25519PublicKey,
                    32
                    ),
            DidKeyTestCase.create(
                    "did:key:z6MknGc3ocHs3zdPiJbnaaqDi58NGb4pk1Sp9WxWufuXSdxf",
                    Codec.Ed25519PublicKey,
                    32
                    ),
            DidKeyTestCase.create(
                    "did:key:z6MkicdicToW5HbxPP7zZV1H7RHvXgRMhoujWAF2n5WQkdd2",
                    Codec.Ed25519PublicKey,
                    32
                    ),
            
            // invalid keys
            DidKeyTestCase.create("http:key:z6MkicdicToW5HbxPP7zZV1H7RHvXgRMhoujWAF2n5WQkdd2"),
            DidKeyTestCase.create("did:example:z6MkicdicToW5HbxPP7zZV1H7RHvXgRMhoujWAF2n5WQkdd2"),
            DidKeyTestCase.create(null),

            // versioned keys
            DidKeyTestCase.create(
                    "did:key:1.1:z6MkicdicToW5HbxPP7zZV1H7RHvXgRMhoujWAF2n5WQkdd2",
                    Codec.Ed25519PublicKey,
                    32,
                    "1.1"
                    ),
            DidKeyTestCase.create(
                    "did:key:0.7:z6MkicdicToW5HbxPP7zZV1H7RHvXgRMhoujWAF2n5WQkdd2",
                    Codec.Ed25519PublicKey,
                    32,
                    "0.7"
                    ),
    };
    
}
