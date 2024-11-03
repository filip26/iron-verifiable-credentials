package com.apicatalog.vcdm.v11;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMapping;
import com.apicatalog.linkedtree.orm.mapper.TreeMappingBuilder;
import com.apicatalog.vc.reader.VerifiableReader;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.io.VcdmReader;
import com.apicatalog.vcdm.io.VcdmResolver;

/**
 * A JSON-LD based reader conforming to the
 * <a href="https://www.w3.org/TR/vc-data-model-1.1/">Verifiable Credentials
 * Data Model v1.1</a>
 */
public class Vcdm11Reader2 extends VcdmReader {

    protected Vcdm11Reader2(final JsonLdTreeReader reader, final SignatureSuite... suites) {
        super(VcdmVersion.V11, reader, suites);
    }

    public static Vcdm11Reader with(
            final SignatureSuite... suites) {
        return with(new Class[0], suites);
    }

    public static Vcdm11Reader with(
            Class<?>[] types,
            final SignatureSuite... suites) {

        TreeMappingBuilder builder = TreeReaderMapping.createBuilder();
        for (Class<?> type : types) {
            builder.scan(type);
        }
        builder.scan(Vcdm11Credential2.class)
                .scan(Vcdm11Presentation2.class);

        return new Vcdm11Reader(JsonLdTreeReader.of(builder.build()), suites);
    }

    @Override
    protected VerifiableReader resolve(Collection<String> context) throws DocumentError {
        return VcdmVersion.V11 == VcdmResolver.getVersion(context)
                ? this
                : null;
    }
}
