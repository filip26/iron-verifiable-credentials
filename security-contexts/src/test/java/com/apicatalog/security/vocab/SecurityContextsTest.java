package com.apicatalog.security.vocab;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SecurityContextsTest {

    @ParameterizedTest
    @MethodSource("uris")
    void testIsValid(String uri) {
        assertTrue(SecurityContexts.getContext(uri).isValid());
    }

    static Stream<String> uris() {
        return SecurityContexts.uris().stream();
    }

}
