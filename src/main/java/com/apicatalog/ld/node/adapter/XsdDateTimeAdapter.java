package com.apicatalog.ld.node.adapter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import com.apicatalog.jsonld.StringUtils;

public class XsdDateTimeAdapter {

    protected XsdDateTimeAdapter() {
        /* protected */}

    public static Instant read(String value) {

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

    public static String write(Instant value) {
        return value.toString();
    }

}
