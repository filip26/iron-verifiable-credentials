package com.apicatalog.vc.issuer;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.node.LdNodeBuilder;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.ModelVersion;
import com.apicatalog.vc.integrity.DataIntegrityVocab;
import com.apicatalog.vc.method.MethodAdapter;

import jakarta.json.JsonObject;

public abstract class ProofDraft {
    
    protected final CryptoSuite crypto;
    protected final VerificationMethod method;
    protected final URI verificatonUrl;

    protected URI id;
    protected URI previousProof;

    protected ProofDraft(CryptoSuite crypto, VerificationMethod method) {
        this.crypto = crypto;
        this.method = method;
        this.verificatonUrl = null;
        this.id = null;
        this.previousProof = null;
    }

    protected ProofDraft(CryptoSuite crypto, URI verificatonUrl) {
        this.crypto = crypto;
        this.method = null;
        this.verificatonUrl = verificatonUrl;
        this.id = null;
        this.previousProof = null;
    }

    /**
     * The proof JSON-LD context URI(s) to compact the proof
     *
     * @param model a credential data model version
     * @return the proof JSON-LD context URI(s)
     */
    public abstract Collection<String> context(ModelVersion model);

    public abstract JsonObject unsigned();

    /**
     * Returns a {@link CryptoSuite} used to create and to verify the proof value.
     * 
     * @return {@link CryptoSuite} attached to the proof.
     */
    public CryptoSuite cryptoSuite() {
        return crypto;
    }
    
    public void id(URI id) {
        this.id = id;
    }
    
    public void previousProof(URI previousProof) {
        this.previousProof = previousProof;
    }

    protected LdNodeBuilder unsigned(LdNodeBuilder builder, MethodAdapter adapter) {
        
        if (id != null) {
            builder.id(id);
        }
        
        if (previousProof != null) {
            builder.set(DataIntegrityVocab.PREVIOUS_PROOF).id(previousProof);
        }
        
        if (verificatonUrl != null) {
            builder.set(DataIntegrityVocab.VERIFICATION_METHOD).id(verificatonUrl);
        } else if (method != null) {
            builder.set(DataIntegrityVocab.VERIFICATION_METHOD).map(adapter, method);
        }
        return builder;
    }

}
