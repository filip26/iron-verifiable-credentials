package com.apicatalog.vcdm.v20;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.orm.context.ContextReducer;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMapping;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMappingBuilder;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.model.CredentialAdapter;
import com.apicatalog.vc.model.ProofAdapter;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableModel;
import com.apicatalog.vc.model.VerifiableModelAdapter;
import com.apicatalog.vc.model.VerifiableReader;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.io.VcdmModelReader;
import com.apicatalog.vcdm.v11.Vcdm11Reader;

import jakarta.json.JsonObject;

public class Vcdm20Reader extends VcdmModelReader implements CredentialAdapter, VerifiableReader {

    protected final static ContextReducer contextReducer = new ContextReducer()
            .define("https://www.w3.org/ns/credentials/v2",
                    List.of("https://w3id.org/security/data-integrity/v2"));

    protected VerifiableModelAdapter v20;
    protected Vcdm11Reader v11;

    protected final ProofAdapter proofAdapter;

    protected Vcdm20Reader(final JsonLdTreeReader reader, ProofAdapter proofAdapter) {
        super(VcdmVersion.V20, contextReducer);
        this.v20 = new Vcdm20Adapter(reader, this, proofAdapter);
        this.v11 = null;
        this.proofAdapter = proofAdapter;
    }

    public static Vcdm20Reader with(final ProofAdapter proofAdapter) {
        return with(proofAdapter, new Class[0]);
    }

    public static Vcdm20Reader with(
            final ProofAdapter proofAdapter,
            final Class<?>... types) {

        TreeReaderMappingBuilder builder = TreeReaderMapping.createBuilder()
                .scan(Vcdm20Credential.class, true)
                .scan(Vcdm20Presentation.class, true)
                .scan(Vcdm20EnvelopedCredential.class, true)
                .scan(Vcdm20EnvelopedPresentation.class, true)
                .scan(Credential.class, true);

        if (types != null) {
            for (Class<?> type : types) {
                builder.scan(type);
            }
        }

        return new Vcdm20Reader(JsonLdTreeReader.of(builder.build()), proofAdapter);
    }

    @Override
    public Verifiable materialize(VerifiableModel model, DocumentLoader loader, URI base) throws DocumentError {

        Objects.requireNonNull(model);

        return v20.materialize(model, loader, base);
    }

    @Override
    public VerifiableModel read(JsonObject document, DocumentLoader loader, URI base) throws DocumentError {

        VerifiableMaterial material = materialReader.read(document, loader, base);

        if (material != null) {

            VerifiableModel model = read(material);
            if (model != null) {
                return model;
            }
        }
        throw new DocumentError(ErrorType.Unknown, "Model");
    }

    public Vcdm20Reader v11(Vcdm11Reader v11) {
        this.v11 = v11;
        return this;
    }

    @Override
    public Credential materialize(VerifiableMaterial data, DocumentLoader loader, URI base) throws DocumentError {

        VcdmVersion version = modelVersion(data.context());

        if (version == null) {
            throw new DocumentError(ErrorType.Unknown, "Model");
        }

        if (v20 != null && VcdmVersion.V20 == version) {

            VerifiableModel model = read(data);

            if (model != null) {
                Verifiable verifiable = v20.materialize(model, loader, base);

                if (verifiable instanceof Credential credential) {
                    return credential;
                }
            }
            throw new DocumentError(ErrorType.Unknown, "Model");

        }

        if (v11 != null && VcdmVersion.V11 == version) {
            return v11.materialize(data, loader, base);
        }

        throw new DocumentError(ErrorType.Unknown, "DocumentModel");
    }

    @Override
    protected VcdmVersion modelVersion(Collection<String> context) throws DocumentError {

        if (context == null || context.isEmpty()) {
            return null;
        }

        final String firstContext = context.iterator().next();

        if (VcdmVocab.CONTEXT_MODEL_V2.equals(firstContext)) {
            return VcdmVersion.V20;
        }
        if (VcdmVocab.CONTEXT_MODEL_V1.equals(firstContext)) {
            return VcdmVersion.V11;
        }

        return null;
    }
}
