package com.aluxian.codementor.data.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TimestampDeserializer extends StdScalarDeserializer<Long> {

    private static final SimpleDateFormat CODEMENTOR_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);

    public TimestampDeserializer() {
        super(Long.class);
    }

    @Override
    public Long deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = StringDeserializer.instance.deserialize(parser, context);
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
