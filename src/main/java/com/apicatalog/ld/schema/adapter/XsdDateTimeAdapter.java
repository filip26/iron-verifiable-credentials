package com.apicatalog.ld.schema.adapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.apicatalog.jsonld.StringUtils;

public class XsdDateTimeAdapter implements LdValueAdapter<String, Date> {

    @Override
    public Date read(String value) {

        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("Cannot convert null into an instant.");
        }

        
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        try {
            Date date = formatter.parse(value);

            return date;
            
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
        
        
//        try {
//            OffsetDateTime createdOffset = OffsetDateTime.parse(value);
//
//            return createdOffset.toInstant();
//
//        } catch (DateTimeParseException e) {
//            throw new IllegalArgumentException(e);
//        }

    }

    @Override
    public String write(Date value) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter.format(value);            

    }

}
