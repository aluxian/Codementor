package com.aluxian.codementor.data.converters;

import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.util.StdConverter;

import java.io.IOException;
import java.util.UUID;

public class StringIdTypeConverter extends StdConverter<String, Long> implements TypeConverter<Long> {

    @Override
    public Long convert(String value) {
        return UUID.fromString(value).getMostSignificantBits();
    }

    @Override
    public Long parse(JsonParser jsonParser) throws IOException {
        return convert(jsonParser.getValueAsString(null));
    }

    @Override
    public void serialize(Long object, String fieldName,
                          boolean writeFieldNameForObject, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStringField(fieldName, Long.toString(object));
    }

}
