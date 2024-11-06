package com.apicatalog.vc.model;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdReader;
import com.apicatalog.vc.proof.GenericProof;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.Json;

public class ProofAdapterProvider implements ProofAdapter {

    protected final Collection<SignatureSuite> suites;

    protected ProofAdapterProvider(Collection<SignatureSuite> suites) {
        this.suites = suites;
    }

    public static ProofAdapter of(final SignatureSuite... suites) {
        return new ProofAdapterProvider(Arrays.asList(suites));
    }

    @Override
    public Proof materialize(VerifiableMaterial data, VerifiableMaterial proofMaterial, DocumentLoader loader, URI base) throws DocumentError {

        Proof proof = null;

        // find a suite that can materialize the proof
        for (SignatureSuite suite : suites) {
            if (suite.isSupported(data, proofMaterial)) {
                proof = suite.getProof(data, proofMaterial, loader);
                if (proof != null) {
                    break;
                }
            }
        }

        // process as a generic, i.e. an unknown, proof
        if (proof == null) {
            
            
//            final JsonLdReader reader = JsonLdReader.of(null, loader); 
//                    
//
//            
//            var proofTree = reader.read(Json.createArrayBuilder().add(expandedProof.asJsonObject()).build());
//            proof = GenericProof.of(proofTree.fragment());
        }

        return proof;
    }

}
