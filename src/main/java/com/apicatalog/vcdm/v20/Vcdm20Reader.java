package com.apicatalog.vcdm.v20;

import java.net.URI;
import java.util.Collection;
import java.util.Objects;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMapping;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMappingBuilder;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.model.ProofAdapter;
import com.apicatalog.vc.model.VerifiableAdapter;
import com.apicatalog.vc.model.VerifiableAdapterProvider;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableModel;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.io.VcdmAdapter;
import com.apicatalog.vcdm.io.VcdmReader;
import com.apicatalog.vcdm.io.VcdmResolver;

import jakarta.json.JsonObject;

public class Vcdm20Reader extends VcdmReader implements VerifiableAdapterProvider {

    protected final VerifiableAdapter v20;
    protected VerifiableAdapter v11;

    protected final ProofAdapter proofAdapter;
    
    protected Vcdm20Reader(final JsonLdTreeReader reader, ProofAdapter proofAdapter) {
        super(VcdmVersion.V20);
        this.v20 = new VcdmAdapter(reader, this, this, proofAdapter);
        this.v11 = null;
        this.proofAdapter = proofAdapter;
    }

    public static Vcdm20Reader with(final ProofAdapter proofAdapter) {
        return with(new Class[0], proofAdapter);
    }
    
    public static Vcdm20Reader with(
            Class<?>[] types,
            final ProofAdapter proofAdapter) {

        Objects.requireNonNull(types);

        TreeReaderMappingBuilder builder = TreeReaderMapping.createBuilder()
                .scan(Vcdm20Credential.class, true)
                .scan(Vcdm20Presentation.class, true)
                .scan(Vcdm20EnvelopedCredential.class, true)
                .scan(Vcdm20EnvelopedPresentation.class, true)
                ;

        for (Class<?> type : types) {
            builder.scan(type);
        }

        return new Vcdm20Reader(JsonLdTreeReader.of(builder.build()), proofAdapter);
    }

    @Override
    public Verifiable read(Collection<String> context, JsonObject document, DocumentLoader loader, URI base) throws DocumentError {

        VerifiableMaterial material = materialReader.read(context, document, loader, base);

        VerifiableModel model = read(material);

        return v20.materialize(model, loader, base);
    }

    @Override
    public VerifiableAdapter adapter(Collection<String> context) throws DocumentError {
        return VcdmVersion.V20 == VcdmResolver.getVersion(context)
                ? v20
                        : (VcdmVersion.V11 == version
                        ? v11
                        : null);

    }

//    @Override
//    protected boolean isCredential(Collection<String> types) {
//        return super.isCredential(types) || types.contains(VcdmVocab.ENVELOPED_CREDENTIAL_TYPE.name());
//    }
//
//    @Override
//    protected boolean isPresentation(Collection<String> types) {
//        return super.isPresentation(types) || types.contains(VcdmVocab.ENVELOPED_PRESENTATION_TYPE.name());
//    }

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
