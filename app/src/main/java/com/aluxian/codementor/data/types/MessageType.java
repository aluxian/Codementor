package com.aluxian.codementor.data.types;

import com.aluxian.codementor.R;
import com.aluxian.codementor.services.ErrorHandler;

public enum MessageType {

    MESSAGE(R.layout.item_msg_text_left, R.layout.item_msg_text_right),
    CONNECT(R.layout.item_msg_system_left, R.layout.item_msg_system_left),
    FILE(R.layout.item_msg_html_left, R.layout.item_msg_html_left),
    REQUEST(R.layout.item_msg_html_left, R.layout.item_msg_html_left),
    SIGNATURE(R.layout.item_msg_system_left, R.layout.item_msg_system_left),
    OTHER(R.layout.item_msg_system_left, R.layout.item_msg_system_left);

    public final int leftLayoutId;
    public final int rightLayoutId;

    MessageType(int leftLayoutId, int rightLayoutId) {
        this.leftLayoutId = leftLayoutId;
        this.rightLayoutId = rightLayoutId;
    }

    public static MessageType parse(String rawType) {
        rawType = rawType.toUpperCase();

        if (rawType.contains("MSG")) {
            rawType = "MESSAGE";
        }

        if (rawType.equals("SESSIONLINK") || rawType.equals("CONNECT")) {
            rawType = "CONNECT";
        }

        try {
            return valueOf(rawType);
        } catch (IllegalArgumentException e) {
            ErrorHandler.log(e);
            return OTHER;
        }
    }

}
