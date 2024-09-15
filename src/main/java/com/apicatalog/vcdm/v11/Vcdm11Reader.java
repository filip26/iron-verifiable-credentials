package com.apicatalog.vcdm.v11;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.adapter.AdapterError;
import com.apicatalog.linkedtree.builder.TreeBuilderError;
import com.apicatalog.linkedtree.jsonld.JsonLdType;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.selector.InvalidSelector;
import com.apicatalog.linkedtree.traversal.NodeSelector.TraversalPolicy;
import com.apicatalog.linkedtree.xsd.XsdDateTime;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.proof.GenericProof;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.reader.ReaderResolver;
import com.apicatalog.vc.reader.VerifiableReader;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.EmbeddedProof;
import com.apicatalog.vcdm.VcdmResolver;
import com.apicatalog.vcdm.VcdmVerifiable;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/**
 * A JSON-LD based reader conforming to the
 * <a href="https://www.w3.org/TR/vc-data-model-1.1/">Verifiable Credentials
 * Data Model v1.1</a>
 */
public class Vcdm11Reader extends VcdmReader {

    protected static final JsonLdTreeReader reader = JsonLdTreeReader.create()

            .with(VcdmVocab.CREDENTIAL_TYPE.uri(),
                    Vcdm11Credential.class,
                    Vcdm11Credential::of)
            .with(VcdmVocab.PRESENTATION_TYPE.uri(),
                    Vcdm11Presentation.class,
                    Vcdm11Presentation::of)
            .with(XsdDateTime.typeAdapter())
            .build();

    public Vcdm11Reader(final SignatureSuite... suites) {
        super(reader, suites);
    }

    @Override
    protected VerifiableReader resolve(Collection<String> context) throws DocumentError {
        return VcdmVersion.V11 == VcdmResolver.getVersion(context)
                ? this
                : null;
    }

    
    
}
