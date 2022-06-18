package com.apicatalog.ld.signature.ed25519;

import java.util.Optional;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.SignatureAdapter;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.ld.signature.proof.EmbeddedProof;
import com.apicatalog.ld.signature.proof.ProofOptions;

import jakarta.json.JsonValue;

public class Ed25519SignatureAdapter implements SignatureAdapter {

    @Override
    public Optional<VerificationKey> materializeKey(JsonValue value) throws DataError {
        
        if (Ed25519VerificationKey2020.isIstanceOf(value)) {
            return  Optional.of(Ed25519VerificationKey2020.from(value.asJsonObject())); 
        } 

        return materializeKeyPair(value).map(VerificationKey.class::cast);
      }

    @Override
    public Optional<KeyPair> materializeKeyPair(JsonValue value) throws DataError {
        
      if (Ed25519KeyPair2020.isIstanceOf(value)) {
          return  Optional.of(Ed25519KeyPair2020.from(value.asJsonObject())); 
      } 

      return Optional.empty();
    }

    @Override
    public Optional<EmbeddedProof> materialize(ProofOptions options) {
        
        if (Ed25519Proof2020.isTypeOf(options.getType())) {
            return Optional.of(Ed25519Proof2020.from(options));
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<SignatureSuite> getSuiteByType(String type) {
        
        if (Ed25519Signature2020.TYPE.equals(type)) {
            return Optional.of(new Ed25519Signature2020());            
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<EmbeddedProof> materializeProof(JsonValue value, DocumentLoader loader) throws DataError {
        
        if (Ed25519Proof2020.isIstanceOf(value)) {
            return Optional.of(Ed25519Proof2020.from(value, loader));
        }
        
        return Optional.empty();
    }
}
