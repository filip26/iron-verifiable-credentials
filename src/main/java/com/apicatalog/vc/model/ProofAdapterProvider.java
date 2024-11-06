package com.apicatalog.vc.model;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.fragment.FragmentPropertyError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMapping;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.Json;

public class ProofAdapterProvider implements ProofAdapter {

    protected static final TreeReaderMapping MAPPING = TreeReaderMapping.createBuilder()
            .scan(Proof.class)
            .build();

    protected static final JsonLdTreeReader READER = JsonLdTreeReader.of(MAPPING);
    
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
                    return proof;
                }
            }
        }

        // process as a generic, i.e. an unknown, proof
        try {
            return READER.read(Proof.class, Json.createArrayBuilder().add(proofMaterial.expanded()).build());
            
        } catch (FragmentPropertyError e) {
            throw DocumentError.of(e);
            
        } catch (Exception e) {
            throw new DocumentError(e, ErrorType.Invalid, "Proof");
        }
    }

}
