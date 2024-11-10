package com.apicatalog.vcdm.v11;

import java.net.URI;
import java.util.Collections;
import java.util.Objects;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMapping;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMappingBuilder;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.model.ProofAdapter;
import com.apicatalog.vc.model.VerifiableAdapter;
import com.apicatalog.vc.model.VerifiableAdapterProvider;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableModel;
import com.apicatalog.vcdm.Vcdm;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.io.VcdmAdapter;
import com.apicatalog.vcdm.io.VcdmReader;

import jakarta.json.JsonObject;

/**
 * A JSON-LD based reader conforming to the
 * <a href="https://www.w3.org/TR/vc-data-model-1.1/">Verifiable Credentials
 * Data Model v1.1</a>
 */
public class Vcdm11Reader extends VcdmReader implements VerifiableAdapterProvider {

    protected final VerifiableAdapter adapter;
    protected final ProofAdapter proofAdapter;

    protected Vcdm11Reader(final JsonLdTreeReader reader, ProofAdapter proofAdapter) {
        super(VcdmVersion.V11);
        this.adapter = new VcdmAdapter(reader, this, this, proofAdapter);
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
    public VerifiableModel read(VerifiableMaterial data) throws DocumentError {

        //TODO check and validate contexts
        
        return super.read(data);
    }

    @Override
    public Verifiable read(JsonObject document, DocumentLoader loader, URI base) throws DocumentError {

        VerifiableMaterial material = materialReader.read(document, loader, base);

        VerifiableModel model = read(material);

        return adapter.materialize(model, loader, base);
    }

    @Override
    public VerifiableAdapter adapter(VerifiableModel model) throws DocumentError {
        if (model instanceof Vcdm vcdm && VcdmVersion.V11 == vcdm.version()) {
            return adapter;
        }
        return null;
    }

    public VerifiableAdapter adapter() {
        return adapter;
    }
}
