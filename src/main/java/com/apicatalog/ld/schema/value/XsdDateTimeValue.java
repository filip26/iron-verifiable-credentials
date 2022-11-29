package com.apicatalog.ld.schema.value;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import com.apicatalog.ld.schema.LdValueAdapter;

public class XsdDateTimeValue implements LdValueAdapter<String, Instant> {

    @Override
    public Instant apply(String value) {
                
        try {
            OffsetDateTime createdOffset = OffsetDateTime.parse(value);

            return createdOffset.toInstant();

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(e);
        }

    }

    @Override
    public String inverse(Instant value) {
        return value.toString();
    }

}
