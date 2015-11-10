package com.aluxian.codementor.data.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;

public class TimestampDeserializer extends StdScalarDeserializer<Long> {

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
                DateFormat dateFormat = new ISO8601DateFormat();
                return dateFormat.parse(value).getTime();
            } catch (ParseException e2) {
                return 0L;
            }
        }
    }

}
