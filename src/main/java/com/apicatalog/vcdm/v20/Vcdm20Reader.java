package com.apicatalog.vcdm.v20;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.xsd.XsdDateTime;
import com.apicatalog.vc.reader.ReaderResolver;
import com.apicatalog.vc.reader.VerifiableReader;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.v11.VcdmReader;

public class Vcdm20Reader extends VcdmReader {

    protected static final JsonLdTreeReader reader = JsonLdTreeReader.create()

            .with(VcdmVocab.CREDENTIAL_TYPE.uri(),
                    Vcdm20Credential.class,
                    Vcdm20Credential::of)
//            .with(VcdmVocab.PRESENTATION_TYPE.uri(),
//                    Vcdm11Presentation.class,
//                    Vcdm11Presentation::of)
            .with(XsdDateTime.typeAdapter())
            .build();

    protected final ReaderResolver resolver;
    
    public Vcdm20Reader(final ReaderResolver resolver, final SignatureSuite... suites) {
        super(reader, suites);
        this.resolver = resolver;
    }

    @Override
    protected VerifiableReader resolve(Collection<String> context) throws DocumentError {
        return resolver.resolveReader(context);
    }


}
