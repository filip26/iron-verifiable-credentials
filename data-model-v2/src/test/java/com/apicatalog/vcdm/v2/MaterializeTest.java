package com.apicatalog.vcdm.v2;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.trust.model.ContextAwareResolver;

public class MaterializeTest {

    static final ContextAwareResolver MODEL_RESOLVER = ContextAwareResolver.newBuilder()
            // accepts VCDM v2.0
            .model(VCDM2::isDefined,
                    // in processing preferences order
                    Resources.VCDM20_SEMANTIC_MODEL
//                    Resources.VCDM20_LEXICAL_MODEL
            )
            .build();

    @ParameterizedTest
    @MethodSource({ "resources" })
    void testMaterialize(String resource) throws Throwable {

        var signed = Resources.getMap(resource);

        var contexts = ContextAwareResolver.getContexts(signed);

        var model = MODEL_RESOLVER.resolve(contexts, signed);

        var accessor = model.createAccessor(contexts, signed);

//        Map<String, Class<?>> documentTypeMapping = accessor.documentMapping();

        var document = accessor.document();

        IO.println(document);
    }

    static final Stream<String> resources() {
        return Resources
                .stream()
                .filter(name -> name.endsWith(".signed.json"))
                .sorted();
    }
}
