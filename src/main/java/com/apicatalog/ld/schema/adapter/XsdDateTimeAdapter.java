package com.apicatalog.ld.schema.adapter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.ld.schema.LdValueAdapter;

public class XsdDateTimeAdapter implements LdValueAdapter<String, Instant> {

    @Override
    public Instant read(String value) {

        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("Cannot convert null into an instant.");
        }

        try {
            OffsetDateTime createdOffset = OffsetDateTime.parse(value);

            return createdOffset.toInstant();

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(e);
        }

    }

    @Override
    public String write(Instant value) {
        return value.toString();
    }

}
