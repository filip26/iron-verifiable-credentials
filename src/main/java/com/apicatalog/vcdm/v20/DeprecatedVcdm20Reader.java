package com.apicatalog.vcdm.v20;

import java.util.Collection;
import java.util.function.Consumer;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.xsd.XsdDateTime;
import com.apicatalog.vc.reader.VerifiableReaderProvider;
import com.apicatalog.vc.reader.VerifiableReader;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.io.DeprecatedVcdmReader;
import com.apicatalog.vcdm.io.VcdmResolver;

public class DeprecatedVcdm20Reader extends DeprecatedVcdmReader {
    
    protected final VerifiableReaderProvider resolver;

    protected DeprecatedVcdm20Reader(final JsonLdTreeReader reader, final VerifiableReaderProvider resolver, final SignatureSuite... suites) {
        super(VcdmVersion.V20, reader, suites);
        this.resolver = resolver;
    }
    
//    public static Vcdm20Reader with(
//            final Consumer<JsonLdTreeReader.Builder> apply,
//            final ReaderResolver resolver,
//            final SignatureSuite... suites) {
//    
//        JsonLdTreeReader.Builder reader = JsonLdTreeReader.createBuilder()
//                .with(VcdmVocab.CREDENTIAL_TYPE.uri(),
//                        Vcdm20Credential.class,
//                        Vcdm20Credential::of)
//                .with(VcdmVocab.ENVELOPED_CREDENTIAL_TYPE.uri(),
//                        Vcdm20EnvelopedCredential.class,
//                        Vcdm20EnvelopedCredential::of)
//                .with(VcdmVocab.PRESENTATION_TYPE.uri(),
//                        Vcdm20Presentation.class,
//                        Vcdm20Presentation::of)
//                .with(VcdmVocab.ENVELOPED_PRESENTATION_TYPE.uri(),
//                        Vcdm20EnvelopedPresentation.class,
//                        Vcdm20EnvelopedPresentation::of)
//                .with(XsdDateTime.typeAdapter());
//                
//        apply.accept(reader);
//        
//        return new Vcdm20Reader(reader.build(), resolver, suites);
//    }

    @Override
    protected VerifiableReader resolve(Collection<String> context) throws DocumentError {
        return VcdmVersion.V20 == VcdmResolver.getVersion(context)
                ? this
                : resolver.reader(context);
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
