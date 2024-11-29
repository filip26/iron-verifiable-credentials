package com.apicatalog.vc.adapter;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.linkedtree.fragment.FragmentPropertyError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMapping;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMappingBuilder;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentModel;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.DocumentError.ErrorType;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.Json;

public class ProofAdapterProvider implements ProofAdapter {

    protected final JsonLdTreeReader reader;

    protected final SignatureSuite[] suites;

    protected ProofAdapterProvider(SignatureSuite[] suites, JsonLdTreeReader genericReader) {
        this.suites = suites;
        this.reader = genericReader;
    }

    public static ProofAdapter of(SignatureSuite... suites) {
        return of(suites, new Class<?>[0]);
    }

    public static ProofAdapter of(final SignatureSuite[] suites, Class<?>... types) {

        TreeReaderMappingBuilder builder = TreeReaderMapping.createBuilder();

        if (types != null) {
            for (Class<?> type : types) {
                builder.scan(type);
            }
        }
        builder.scan(Proof.class);

        return new ProofAdapterProvider(suites, JsonLdTreeReader.of(builder.build()));
    }

    @Override
    public Proof materialize(DocumentModel model, DocumentLoader loader, URI base) throws DocumentError {

        Proof proof = null;
        
        VerifiableMaterial data = model.data();
        VerifiableMaterial proofMaterial = model.proofs().iterator().next();

        // find a suite that can materialize the proof
        for (SignatureSuite suite : suites) {
            if (suite.isSupported(data, proofMaterial)) {
                proof = suite.getProof(model, loader, base);
                if (proof != null) {
                    return proof;
                }
            }
        }

        // process as a generic, i.e. an unknown, proof
        try {
            return reader.read(Proof.class, Json.createArrayBuilder().add(proofMaterial.expanded()).build());

        } catch (FragmentPropertyError e) {
            throw DocumentError.of(e);

        } catch (Exception e) {
            throw new DocumentError(e, ErrorType.Invalid, "Proof");
        }
    }

}
