package com.apicatalog.vcdm.v2;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.trust.model.ContextAwareResolver;

public class MaterializeTest {

    static final ContextAwareResolver MODEL_RESOLVER = ContextAwareResolver.newBuilder()
            // accepts VCDM v2.0
            .model(VCDM2::isDefined,
                    // in processing preferences order
                    Resources.GRAPH_PROCESSOR
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

        if (document instanceof Credential credential) {
            IO.println(credential.context);
            IO.println(credential.id);
            IO.println(credential.type);
            IO.println(credential.name);
            IO.println(credential.description);
            IO.println(credential.validFrom);
            IO.println(credential.validUntil);
            IO.println(credential.issuer);
            IO.println(credential.subject);

            assertTrue(credential.hasRequired());
            assertFalse(credential.isExpired());
            assertFalse(credential.isPostDated());

        } else if (document instanceof Presentation presentation) {
            IO.println(presentation.context());
            IO.println(presentation.id());
            IO.println(presentation.type());
            IO.println(presentation.holder());

            var credentials = presentation.newCredentialCursor();

            while (credentials.next()) {

                var credential = credentials.newAccessor();

                IO.println(credential.document());

                var proofs = credential.createProofCursor();
                if (proofs != null) {
                    while (proofs.next()) {
                        
                        IO.println("proof > " + proofs.isAccepted());
                        IO.println("      > " + proofs.proof());
                        
                    }
                }
            }

        }

//        IO.println(document.);
    }

    static final Stream<String> resources() {
        return Resources
                .stream()
                .filter(name -> name.endsWith(".json"))
                .sorted();
    }
}
