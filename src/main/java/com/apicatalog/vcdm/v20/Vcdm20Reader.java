package com.apicatalog.vcdm.v20;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.xsd.XsdDateTime;
import com.apicatalog.vc.reader.ReaderResolver;
import com.apicatalog.vc.reader.VerifiableReader;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.VcdmReader;
import com.apicatalog.vcdm.VcdmResolver;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

public class Vcdm20Reader extends VcdmReader {

    protected static final JsonLdTreeReader reader = JsonLdTreeReader.create()
            .with(VcdmVocab.CREDENTIAL_TYPE.uri(),
                    Vcdm20Credential.class,
                    Vcdm20Credential::of)
            .with(VcdmVocab.ENVELOPED_CREDENTIAL_TYPE.uri(),
                    Vcdm20EnvelopedCredential.class,
                    Vcdm20EnvelopedCredential::of)
            .with(VcdmVocab.PRESENTATION_TYPE.uri(),
                    Vcdm20Presentation.class,
                    Vcdm20Presentation::of)
            .with(VcdmVocab.ENVELOPED_PRESENTATION_TYPE.uri(),
                    Vcdm20EnvelopedPresentation.class,
                    Vcdm20EnvelopedPresentation::of)
            .with(XsdDateTime.typeAdapter())
            .build();

    protected final ReaderResolver resolver;

    public Vcdm20Reader(final ReaderResolver resolver, final SignatureSuite... suites) {
        super(VcdmVersion.V20, reader, suites);
        this.resolver = resolver;
    }

    @Override
    protected VerifiableReader resolve(Collection<String> context) throws DocumentError {
        return VcdmVersion.V20 == VcdmResolver.getVersion(context)
                ? this
                : resolver.resolveReader(context);
    }

    @Override
    protected boolean isCredential(Collection<String> types) {
        return super.isCredential(types) || types.contains(VcdmVocab.ENVELOPED_CREDENTIAL_TYPE.name());
    }

    @Override
    protected boolean isPresentation(Collection<String> types) {
        return super.isPresentation(types) || types.contains(VcdmVocab.ENVELOPED_PRESENTATION_TYPE.name());
    }

}
