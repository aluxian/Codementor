package com.aluxian.codementor.data.deserializers;

import com.aluxian.codementor.data.types.MessageType;
import com.aluxian.codementor.services.ErrorHandler;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

import java.io.IOException;

public class MessageTypeDeserializer extends StdScalarDeserializer<MessageType> {

    public MessageTypeDeserializer() {
        super(MessageType.class);
    }

    @Override
    public MessageType deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = StringDeserializer.instance.deserialize(parser, context);

        if (value != null) {
            value = value.toUpperCase();
        } else {
            return MessageType.OTHER;
        }

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

}
