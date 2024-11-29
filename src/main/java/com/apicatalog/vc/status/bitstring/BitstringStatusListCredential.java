package com.apicatalog.vc.status.bitstring;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;

import com.apicatalog.linkedtree.orm.Id;
import com.apicatalog.linkedtree.orm.Type;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentError.ErrorType;

public interface BitstringStatusListCredential {

    @Id
    URI id();

    @Type
    Collection<String> type();

    default boolean isExpired() {
        return (validUntil() != null && validUntil().isBefore(Instant.now()));
    }

    default boolean isNotValidYet() {
        return (validFrom() != null && validFrom().isAfter(Instant.now()));
    }

    BitstringStatusList subject();

    Instant validFrom();

    Instant validUntil();

    default void validate() throws DocumentError {
        if ((validFrom() != null
                && validUntil() != null
                && validFrom().isAfter(validUntil()))) {
            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
        }
    }
}
