package com.aluxian.codementor.data.types;

import com.aluxian.codementor.data.models.ConversationItem;

import static com.aluxian.codementor.data.models.ConversationItem.TYPE_CONNECT;
import static com.aluxian.codementor.data.models.ConversationItem.TYPE_FILE;
import static com.aluxian.codementor.data.models.ConversationItem.TYPE_MESSAGE;
import static com.aluxian.codementor.data.models.ConversationItem.TYPE_SIGNATURE;

public enum MessageType {

    MESSAGE(TYPE_MESSAGE),
    CONNECT(ConversationItem.TYPE_CONNECT),
    FILE(ConversationItem.TYPE_FILE),
    SIGNATURE(ConversationItem.TYPE_SIGNATURE),
    OTHER(ConversationItem.TYPE_OTHER);

    public final int typeFlag;

    MessageType(int typeFlag) {
        this.typeFlag = typeFlag;
    }

    public static MessageType getByFlag(int flag) {
        if ((TYPE_MESSAGE & flag) == TYPE_MESSAGE) return MESSAGE;
        if ((TYPE_CONNECT & flag) == TYPE_CONNECT) return CONNECT;
        if ((TYPE_FILE & flag) == TYPE_FILE) return FILE;
        if ((TYPE_SIGNATURE & flag) == TYPE_SIGNATURE) return SIGNATURE;
        return OTHER;
    }

}
