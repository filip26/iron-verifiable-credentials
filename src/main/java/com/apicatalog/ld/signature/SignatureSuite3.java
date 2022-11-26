package com.apicatalog.ld.signature;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.signature.adapter.MethodAdapter;
import com.apicatalog.ld.signature.adapter.ProofAdapter;
import com.apicatalog.ld.signature.algorithm.CanonicalizationAlgorithm;
import com.apicatalog.ld.signature.algorithm.DigestAlgorithm;
import com.apicatalog.ld.signature.algorithm.SignatureAlgorithm;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.proof.ProofOptions;

import jakarta.json.JsonStructure;

/**
 * A specified set of cryptographic primitives consisting of a canonicalization
 * algorithm, a message digest algorithm, and a signature algorithm.
 */
public class SignatureSuite3 implements CanonicalizationAlgorithm, DigestAlgorithm, SignatureAlgorithm {

	protected final URI id;

	protected final CanonicalizationAlgorithm canonicalization;
	protected final DigestAlgorithm digester;
	protected final SignatureAlgorithm signer;

	protected final ProofAdapter proofAdapter;
    protected final Collection<MethodAdapter> methodAdapter;

    
    /*
     * https://www.w3.org/TR/vc-data-integrity/#verification-material
     *  A cryptographic suite specification is responsible for specifying the verification method 
     */
	public SignatureSuite3(final URI id, final CanonicalizationAlgorithm canonicalization,
			final DigestAlgorithm digester, final SignatureAlgorithm signer, final ProofAdapter proofAdapter,
			final Collection<MethodAdapter> methodAdapter
	        ) {
		this.id = id;
		this.canonicalization = canonicalization;
		this.digester = digester;
		this.signer = signer;
		this.proofAdapter = proofAdapter;
		this.methodAdapter = methodAdapter;
	}

	@Override
	public void verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError {
		signer.verify(publicKey, signature, data);
	}

	@Override
	public byte[] sign(byte[] privateKey, byte[] data) throws SigningError {
		return signer.sign(privateKey, data);
	}

	@Override
	public byte[] digest(byte[] data) throws LinkedDataSuiteError {
		return digester.digest(data);
	}

	@Override
	public byte[] canonicalize(JsonStructure document) throws LinkedDataSuiteError {
		return canonicalization.canonicalize(document);
	}

	@Override
	public KeyPair keygen(int length) throws KeyGenError {
		return signer.keygen(length);
	}

	public URI getId() {
		return id;
	}

	public ProofAdapter getProofAdapter() {
		return proofAdapter;
	}

    public MethodAdapter getMethodAdapter(String type) {
//        return methodAdapter;
        //FIXME
        return null;
    }

    public <T extends ProofOptions> T createOptions() {
        // TODO Auto-generated method stub
        return null;
    }

//    /**
//     * A JSON-LD context used to expand the proof
//     * 
//     * @return an {@link URI} referencing a JSON-LD context or <code>null</code> if a context is embedded or not needed
//     */
//    public URI contexs() {
//        return context;
//    }
    
    //TODO URI getBase(URI id);
}
