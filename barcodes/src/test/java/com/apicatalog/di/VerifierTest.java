package com.apicatalog.di;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.crypto.bc.BCECDSAVerifier;
import com.apicatalog.di.barcodes.ECDSAXI2023.BarcodePayload;
import com.apicatalog.di.suite.ECDSA2019;
import com.apicatalog.did.key.DidKey;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.did.resolver.MultiKeyResolver;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.trust.model.ContextAwareResolver;
import com.apicatalog.trust.proof.ProofVerifier;

public class VerifierTest {

    static final DidKeyResolver DID_KEY_RESOLVER = DidKeyResolver.newBuilder()
            .multibaseDecoder(MultibaseDecoder.getInstance(Multibase.BASE_58_BTC)::decode)
            .multikey()
            .build();

    static final MultiKeyResolver MULTIKEY_RESOLVER = MultiKeyResolver.newBuilder()
            .codec(ECDSA2019.P256, KeyCodec.P256_PUBLIC.varint())
            .codec(ECDSA2019.P384, KeyCodec.P384_PUBLIC.varint())
            .methodResolver(DidKey.METHOD_NAME, DID_KEY_RESOLVER)
            .build();

    static ProofVerifier PROOF_VERIFIER = ProofVerifier.newBuilder()
            .publicKeyResolver(MULTIKEY_RESOLVER::getPublicKey)
            .verifier(ECDSA2019.P256, BCECDSAVerifier.getP256Instance()::verify)
            .verifier(ECDSA2019.P384, BCECDSAVerifier.getP384Instance()::verify)
            .digestFactory(Resources.DIGEST_FACTORY)
            .build();

    @ParameterizedTest
    @MethodSource({ "resources" })
    void testVerify(String resource) throws Throwable {

        Map<String, Object> options = Resources.getMap(resource + ".options.json");
        Map<String, Object> signed = Resources.getMap(resource + ".signed.json");

        var context = ContextAwareResolver.getContexts(signed);
        
        var processor = Resources.SEMANTIC_MODEL.createAccessor(context, signed);

        var cursor = processor.createProofCursor();

        if (cursor == null || !cursor.next()) {
            fail("No proof(s) to verify");
        }

        if (!cursor.isAccepted()) {
            fail();
        }

        var proof = cursor.proof();

        if (!(proof.signature().payload() instanceof BarcodePayload barcode)) {
            fail();
            return;
        }

        barcode.opticalData(((Collection<?>) options.get("opticalDataBytes"))
                .stream().map(BigInteger.class::cast).map(BigInteger::byteValue)
                .collect(ByteArrayOutputStream::new, ByteArrayOutputStream::write, (_, _) -> {
                })
                .toByteArray());

        var verified = PROOF_VERIFIER.verify(proof);
        assertTrue(verified);

        assertFalse(cursor.next());
    }

    static final Stream<String> resources() {
        return Resources
                .stream()
                .filter(name -> name.endsWith(".signed.json"))
                .map(name -> name.substring(0, name.indexOf('.')))
                .sorted();
    }
}
