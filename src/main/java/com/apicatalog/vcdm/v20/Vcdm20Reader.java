package com.apicatalog.vcdm.v20;

import java.net.URI;
import java.util.Collections;

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
import com.apicatalog.vcdm.io.VcdmReader;

import jakarta.json.JsonObject;

public class Vcdm20Reader extends VcdmReader implements VerifiableAdapterProvider {

    protected final VerifiableAdapter v20;
    protected VerifiableAdapter v11;

    protected final ProofAdapter proofAdapter;

    protected Vcdm20Reader(final JsonLdTreeReader reader, ProofAdapter proofAdapter) {
        super(VcdmVersion.V20);
        this.v20 = new Vcdm20Adapter(reader, this, this, proofAdapter);
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
    public VerifiableModel read(VerifiableMaterial data) throws DocumentError {

        // TODO check and validate contexts

        return super.read(data);
    }

    @Override
    public Verifiable read(JsonObject document, DocumentLoader loader, URI base) throws DocumentError {

        VerifiableMaterial material = materialReader.read(document, loader, base);

        VerifiableModel model = read(material);

        return v20.materialize(model, loader, base);
    }

    @Override
    public VerifiableAdapter adapter(VerifiableModel model) throws DocumentError {
        if (model instanceof Vcdm vcdm) {
            if (VcdmVersion.V20 == vcdm.version()) {
                return v20;
            }
            if (VcdmVersion.V11 == vcdm.version()) {
                return v11;
            }
        }
        return null;
    }

    public Vcdm20Reader v11(VerifiableAdapter v11) {
        this.v11 = v11;
        return this;
    }

    public VerifiableAdapter v11() {
        return v11;
    }

    public VerifiableAdapter v20() {
        return v20;
    }

}
