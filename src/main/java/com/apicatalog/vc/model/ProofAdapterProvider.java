package com.apicatalog.vc.model;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.fragment.FragmentPropertyError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMapping;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMappingBuilder;
import com.apicatalog.linkedtree.orm.proxy.PropertyValueConsumer;
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

    public static ProofAdapter of(SignatureSuite[] suites) {
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
            return reader.read(Proof.class, Json.createArrayBuilder().add(proofMaterial.expanded()).build());

        } catch (FragmentPropertyError e) {
            throw DocumentError.of(e);

        } catch (Exception e) {
            throw new DocumentError(e, ErrorType.Invalid, "Proof");
        }
    }

}
