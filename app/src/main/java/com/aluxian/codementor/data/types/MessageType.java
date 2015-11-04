package com.aluxian.codementor.data.types;

import com.aluxian.codementor.R;

public enum MessageType {

    MESSAGE(R.layout.item_msg_text_left, R.layout.item_msg_text_right),
    CONNECT(R.layout.item_msg_system_left, R.layout.item_msg_system_right),
    FILE(R.layout.item_msg_html_left, R.layout.item_msg_html_right),
    REQUEST(R.layout.item_msg_html_left, R.layout.item_msg_html_right),
    SIGNATURE(R.layout.item_msg_system_left, R.layout.item_msg_system_right),
    OTHER(R.layout.item_msg_system_left, R.layout.item_msg_system_right);

    public final int leftLayoutId;
    public final int rightLayoutId;

    MessageType(int leftLayoutId, int rightLayoutId) {
        this.leftLayoutId = leftLayoutId;
        this.rightLayoutId = rightLayoutId;
    }

    public boolean isHtml() {
        return leftLayoutId == R.layout.item_msg_html_left;
    }

}
