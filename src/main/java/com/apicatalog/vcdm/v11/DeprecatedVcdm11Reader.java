package com.apicatalog.vcdm.v11;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.vc.reader.VerifiableReader;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.io.DeprecatedVcdmReader;
import com.apicatalog.vcdm.io.VcdmResolver;

/**
 * A JSON-LD based reader conforming to the
 * <a href="https://www.w3.org/TR/vc-data-model-1.1/">Verifiable Credentials
 * Data Model v1.1</a>
 */
public class DeprecatedVcdm11Reader extends DeprecatedVcdmReader {

    protected DeprecatedVcdm11Reader(final JsonLdTreeReader reader, final SignatureSuite... suites) {
        super(VcdmVersion.V11, reader, suites);
    }

//    public static Vcdm11Reader with(
//            final Consumer<JsonLdTreeReader.Builder> apply,
//            final SignatureSuite... suites) {
//
//        JsonLdTreeReader.Builder reader = JsonLdTreeReader
//                .createBuilder()
//                .with(VcdmVocab.CREDENTIAL_TYPE.uri(),
//                        Vcdm11Credential.class,
//                        Vcdm11Credential::of)
//                .with(VcdmVocab.PRESENTATION_TYPE.uri(),
//                        Vcdm11Presentation.class,
//                        Vcdm11Presentation::of)
//                .with(XsdDateTime.typeAdapter());
//        
//        apply.accept(reader);
//
//        return new Vcdm11Reader(reader.build(), suites);
//    }

    @Override
    protected VerifiableReader resolve(Collection<String> context) throws DocumentError {
        return VcdmVersion.V11 == VcdmResolver.getVersion(context)
                ? this
                : null;
    }

}
