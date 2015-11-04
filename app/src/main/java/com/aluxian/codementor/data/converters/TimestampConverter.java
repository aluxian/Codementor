package com.aluxian.codementor.data.converters;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TimestampConverter extends StdConverter<String, Long> {

    private static final SimpleDateFormat CODEMENTOR_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);

    @Override
    public Long convert(String value) {
        try {
            return Double.valueOf(value).longValue();
        } catch (NumberFormatException e1) {
            try {
                return CODEMENTOR_DATE_FORMAT.parse(value).getTime();
            } catch (ParseException e2) {
                return 0L;
            }
        }
    }

}
