package com.apicatalog.vcdm.v11;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.xsd.XsdDateTime;
import com.apicatalog.vc.reader.VerifiableReader;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.VcdmReader;
import com.apicatalog.vcdm.VcdmResolver;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

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
