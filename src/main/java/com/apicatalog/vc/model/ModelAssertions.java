package com.apicatalog.vc.model;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.VocabTerm;


public interface ModelAssertions {

    static void assertEquals(Map<String, Object> params, VocabTerm name, Object expected) throws DocumentError {

        final Object value = params.get(name.name());
        if (value == null) {
            return;
        }

        if (!Objects.equals(value, expected)) {
            throw new DocumentError(ErrorType.Invalid, name);
        }
    }

    static void assertNotNull(Supplier<?> fnc, VocabTerm term) throws DocumentError {

        Object value = null;

        try {
            value = fnc.get();

        } catch (Exception e) {
            throw new DocumentError(e, ErrorType.Invalid, term);
        }

        if (value == null) {
            throw new DocumentError(ErrorType.Missing, term);
        }
    }
}
