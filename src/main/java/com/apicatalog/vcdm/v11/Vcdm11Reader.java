package com.apicatalog.vcdm.v11;

import java.util.Collection;
import java.util.Objects;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMapping;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMappingBuilder;
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
public class Vcdm11Reader extends VcdmReader {

    protected Vcdm11Reader(final JsonLdTreeReader reader, final SignatureSuite... suites) {
        super(VcdmVersion.V11, reader, suites);
    }

    public static Vcdm11Reader with(
            final SignatureSuite... suites) {
        return with(new Class[0], suites);
    }

    public static Vcdm11Reader with(
            Class<?>[] types,
            final SignatureSuite... suites) {

        Objects.requireNonNull(types);
        
        TreeReaderMappingBuilder builder = TreeReaderMapping.createBuilder()
                .scan(Vcdm11Credential.class)
                .scan(Vcdm11Presentation.class)
                ; 
        
        for (Class<?> type : types) {
            builder.scan(type);
        }

        return new Vcdm11Reader(JsonLdTreeReader.of(builder.build()), suites);
    }

    @Override
    protected VerifiableReader resolve(Collection<String> context) throws DocumentError {
        return VcdmVersion.V11 == VcdmResolver.getVersion(context)
                ? this
                : null;
    }
}
