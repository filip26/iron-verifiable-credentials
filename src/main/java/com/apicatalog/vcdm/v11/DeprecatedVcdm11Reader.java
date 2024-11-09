package com.apicatalog.vcdm.v11;

import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.vc.suite.SignatureSuite;

/**
 * A JSON-LD based reader conforming to the
 * <a href="https://www.w3.org/TR/vc-data-model-1.1/">Verifiable Credentials
 * Data Model v1.1</a>
 */
public class DeprecatedVcdm11Reader  {

    protected DeprecatedVcdm11Reader(final JsonLdTreeReader reader, final SignatureSuite... suites) {
//        super(VcdmVersion.V11, reader, suites);
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

//    @Override
//    protected VerifiableReader resolve(Collection<String> context) throws DocumentError {
//        return VcdmVersion.V11 == VcdmResolver.getVersion(context)
//                ? null
//                : null;
//    }

}
