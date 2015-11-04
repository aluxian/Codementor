package com.aluxian.codementor.data.converters;

import com.aluxian.codementor.data.models.Message;
import com.fasterxml.jackson.databind.util.StdConverter;

import java.util.HashMap;
import java.util.Map;

public class TimestampCounterConverter extends StdConverter<Message, Message> {

    private static final Map<Long, Long> TIMESTAMPS = new HashMap<>();

    @Override
    public Message convert(Message message) {
        Long id = message.getId();

        if (TIMESTAMPS.containsKey(id)) {
            Long createdAt = TIMESTAMPS.get(id);
            message.setCreatedAt(createdAt);
        } else {
            TIMESTAMPS.put(id, message.getCreatedAt());
        }

        return message;
    }

}
