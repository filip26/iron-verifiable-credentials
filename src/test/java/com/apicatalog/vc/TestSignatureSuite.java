package com.apicatalog.vc;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.adapter.MethodAdapter;
import com.apicatalog.ld.signature.adapter.MultibaseKeyAdapter;
import com.apicatalog.ld.signature.adapter.MultibaseProofValueAdapter;
import com.apicatalog.ld.signature.primitive.MessageDigest;
import com.apicatalog.ld.signature.primitive.Urdna2015;
import com.apicatalog.ld.signature.proof.ProofType;
import com.apicatalog.multicodec.Multicodec.Codec;
import com.apicatalog.vc.integrity.DataIntegritySchema;
import com.apicatalog.vc.integrity.DataIntegritySuite;

class TestSignatureSuite extends DataIntegritySuite {
    
    static final ProofType TYPE = new TestProofType();
	
	static final CryptoSuite CRYPTO = new CryptoSuite(
	            TYPE.id(),
	            new Urdna2015(),
	            new MessageDigest("MD5"),
	            new TestAlgorithm()
            );
	
//	static final MultibaseProofValueAdapter ADAPTER = new MultibaseProofValueAdapter(URI.create("https://example.org/security#multibase"));
//	
//	static final Map<String, MethodAdapter> METHOD_ADAPTERS;
//	
//	static {
//	    
//	    Map<String, MethodAdapter> methods = new LinkedHashMap<>();
//	    
//	    MultibaseKeyAdapter multibase = new MultibaseKeyAdapter(
//	            URI.create("https://example.org/security#TestVerificationKey2022"),
//	            Codec.Ed25519PublicKey,
//	            LdTerm.create("publicKeyMultibase", "https://example.org/security#")
//	            );
//	    
//	    methods.put("https://example.org/security#TestVerificationKey2022", multibase);
//	    
//	    METHOD_ADAPTERS = Collections.unmodifiableMap(methods);
//	    
//	}
	
	public TestSignatureSuite() {
	    super(TYPE, CRYPTO, DataIntegritySchema.getSchema(LdTerm.create("TestSignatureSuite2022", "https://w3id.org/security#")));
	}	
}
