package com.aluxian.codementor.data.converters;

import com.aluxian.codementor.data.types.MessageType;
import com.aluxian.codementor.services.ErrorHandler;
import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.util.StdConverter;

import java.io.IOException;

public class MessageTypeConverter extends StdConverter<String, MessageType> implements TypeConverter<MessageType> {

    @Override
    public MessageType convert(String value) {
        value = value.toUpperCase();

        if (value.contains("MSG")) {
            value = "MESSAGE";
        }

        if (value.equals("SESSIONLINK") || value.equals("CONNECT")) {
            value = "CONNECT";
        }

        try {
            return MessageType.valueOf(value);
        } catch (IllegalArgumentException e) {
            ErrorHandler.logError(e);
            return MessageType.OTHER;
        }
    }

    @Override
    public MessageType parse(JsonParser jsonParser) throws IOException {
        String value = jsonParser.getValueAsString(null);

        if (value != null) {
            value = value.toUpperCase();
        } else {
            return MessageType.OTHER;
        }

        return convert(value);
    }

    @Override
    public void serialize(MessageType object, String fieldName,
                          boolean writeFieldNameForObject, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStringField(fieldName, object.name().toLowerCase());
    }

}
