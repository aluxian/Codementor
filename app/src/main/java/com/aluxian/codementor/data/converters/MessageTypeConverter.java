package com.aluxian.codementor.data.converters;

import com.aluxian.codementor.data.types.MessageType;
import com.aluxian.codementor.services.ErrorHandler;
import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;

public class MessageTypeConverter extends StringBasedTypeConverter<MessageType> {

    @Override
    public MessageType getFromString(String value) {
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
    public String convertToString(MessageType value) {
        return value.name().toLowerCase();
    }

}
