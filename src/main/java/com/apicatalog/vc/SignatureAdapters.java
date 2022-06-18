package com.apicatalog.vc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.ed25519.Ed25519SignatureAdapter;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.ld.signature.proof.EmbeddedProof;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.ld.signature.proof.SignatureAdapter;

import jakarta.json.JsonValue;

public class SignatureAdapters implements SignatureAdapter {

    protected Collection<SignatureAdapter> adapters;
    
    public SignatureAdapters() {
        this(Arrays.asList(new SignatureAdapter[] { new Ed25519SignatureAdapter() }));
    }

    public SignatureAdapters(Collection<SignatureAdapter> adapters) {
        this.adapters = adapters;
    }
    
    @Override
    public Optional<VerificationKey> materializeKey(final JsonValue value) throws DataError {
        return materialize(value, (a, v) -> a.materializeKey(v));
    }

    @Override
    public Optional<KeyPair> materializeKeyPair(JsonValue value) throws DataError {
        return materialize(value, (a, v) -> a.materializeKeyPair(v));
    }

    @Override
    public Optional<EmbeddedProof> materialize(ProofOptions options) throws DataError {
        return materialize(options, (a, v) -> a.materialize(v));
    }

    @Override
    public Optional<SignatureSuite> getSuiteByType(String type) throws DataError {
        return materialize(type, (a, v) -> a.getSuiteByType(v));
    }

    @Override
    public Optional<EmbeddedProof> materializeProof(JsonValue value, final DocumentLoader loader) throws DataError {
        return materialize(value, (a, v) -> a.materializeProof(v, loader));
    }
    
    protected <V, R> Optional<R> materialize(final V value, MaterializeFunction<V, R> method) throws DataError {
        
        for (final SignatureAdapter adapter : adapters) {           
            final Optional<R> result = method.apply(adapter, value);
            
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
    
    @FunctionalInterface
    protected interface MaterializeFunction<V, R> {
        Optional<R> apply(SignatureAdapter adapter, V value) throws DataError;
    }

}
