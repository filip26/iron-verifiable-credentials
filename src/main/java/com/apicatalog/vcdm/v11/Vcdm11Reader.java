package com.apicatalog.vcdm.v11;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.orm.context.ContextReducer;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMapping;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMappingBuilder;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.adapter.CredentialAdapter;
import com.apicatalog.vc.adapter.ProofAdapter;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentModel;
import com.apicatalog.vc.model.VerifiableDocument;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.DocumentError.ErrorType;
import com.apicatalog.vc.model.adapter.DocumentAdapter;
import com.apicatalog.vc.model.adapter.DocumentModelAdapter;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.io.VcdmAdapter;
import com.apicatalog.vcdm.io.VcdmModelReader;

import jakarta.json.JsonObject;

/**
 * A JSON-LD based reader conforming to the
 * <a href="https://www.w3.org/TR/vc-data-model-1.1/">Verifiable Credentials
 * Data Model v1.1</a>
 */
public class Vcdm11Reader extends VcdmModelReader implements CredentialAdapter, DocumentModelAdapter {

    protected final DocumentAdapter adapter;
    protected final ProofAdapter proofAdapter;

    protected final static ContextReducer contextReducer = new ContextReducer()
            .define("https://www.w3.org/ns/controller/v1",
                    List.of("https://w3id.org/security/jwk/v1",
                            "https://w3id.org/security/multikey/v1"));
    
    protected Vcdm11Reader(final JsonLdTreeReader reader, ProofAdapter proofAdapter) {
        super(VcdmVersion.V11, contextReducer);
        this.adapter = new VcdmAdapter(reader, this, proofAdapter);
        this.proofAdapter = proofAdapter;
    }

    public static Vcdm11Reader with(final ProofAdapter proofAdapter) {
        return with(proofAdapter, new Class[0]);
    }

    public static Vcdm11Reader with(
            final ProofAdapter proofAdapter,
            final Class<?>... types) {

        Objects.requireNonNull(types);

        TreeReaderMappingBuilder builder = TreeReaderMapping.createBuilder()
                .scan(Vcdm11Credential.class, true)
                .scan(Vcdm11Presentation.class, true)
                .scan(Credential.class, true);

        for (Class<?> type : types) {
            builder.scan(type);
        }

        return new Vcdm11Reader(JsonLdTreeReader.of(builder.build()), proofAdapter);
    }

    @Override
    public DocumentModel read(VerifiableMaterial data) throws DocumentError {

        // TODO check and validate contexts

        return super.read(data);
    }

    @Override
    public VerifiableDocument materialize(DocumentModel model, DocumentLoader loader, URI base) throws DocumentError {
        return adapter.materialize(model, loader, base);
    }

    @Override
    public DocumentModel read(JsonObject document, DocumentLoader loader, URI base) throws DocumentError {
        VerifiableMaterial material = materialReader.read(document, loader, base);

        DocumentModel model = read(material);

        return model;
    }

    public DocumentAdapter adapter() {
        return adapter;
    }

    @Override
    public Credential materialize(VerifiableMaterial data, DocumentLoader loader, URI base) throws DocumentError {
        VcdmVersion version = modelVersion(data.context().strings());

        if (version != null && adapter != null && VcdmVersion.V11 == version) {

            DocumentModel model = read(data);

            if (model != null) {
                VerifiableDocument verifiable = adapter.materialize(model, loader, base);

                if (verifiable instanceof Credential credential) {
                    return credential;
                }
            }
        }

        throw new DocumentError(ErrorType.Unknown, "DocumentModel");
    }

    @Override
    protected VcdmVersion modelVersion(Collection<String> context) {
        return context != null && !context.isEmpty()
                && VcdmVocab.CONTEXT_MODEL_V1.equals(context.iterator().next())
                        ? VcdmVersion.V11
                        : null;
    }

}
