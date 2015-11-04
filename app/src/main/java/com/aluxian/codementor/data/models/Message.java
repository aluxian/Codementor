package com.aluxian.codementor.data.models;

import android.text.TextUtils;
import android.text.format.Formatter;

import com.aluxian.codementor.data.converters.TimestampCounterConverter;
import com.aluxian.codementor.data.deserializers.MessageTypeDeserializer;
import com.aluxian.codementor.data.deserializers.StringIdDeserializer;
import com.aluxian.codementor.data.deserializers.TimestampDeserializer;
import com.aluxian.codementor.data.types.MessageType;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.aluxian.codementor.services.UserManager.LOGGED_IN_USERNAME;
import static com.aluxian.codementor.utils.Helpers.escapeHtml;
import static com.aluxian.codementor.utils.Helpers.italic;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
@JsonDeserialize(converter = TimestampCounterConverter.class)
public class Message extends ConversationItem implements Serializable {

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("kk:mm", Locale.ENGLISH);

    @JsonDeserialize(using = StringIdDeserializer.class) Long id;
    @JsonDeserialize(using = MessageTypeDeserializer.class) MessageType type;
    @JsonDeserialize(using = TimestampDeserializer.class) @JsonProperty("created_at") Long createdAt;
    @JsonProperty("read_at") String readAt;

    String content;
    Request request;

    User sender;
    User receiver;

    private String text;
    private String subtext;
    private String contentDescription;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int getLayoutId() {
        if (sentByMe()) {
            return type.rightLayoutId;
        } else {
            return type.leftLayoutId;
        }
    }

    @Override
    public long getTimestamp() {
        return createdAt;
    }

    @Override
    public String getText() {
        if (text == null) {
            text = generateText();
        }

        return text;
    }

    @Override
    public String getSubtext() {
        if (subtext == null) {
            subtext = TIME_FORMAT.format(new Date(createdAt));
            if (request != null) {
                subtext += " " + Formatter.formatShortFileSize(null, request.getSize());
            }
        }

        return subtext;
    }

    @Override
    public boolean isHtmlText() {
        return type.isHtml();
    }

    @Override
    public boolean isRead() {
        return !TextUtils.isEmpty(readAt);
    }

    @Override
    public boolean sentByMe() {
        return sender.equals(getCurrentUser());
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public String getContentDescription() {
        if (contentDescription == null) {
            contentDescription = generateContentDescription();
        }

        return contentDescription;
    }

    public User getCurrentUser() {
        return sender.getUsername().equals(LOGGED_IN_USERNAME) ? sender : receiver;
    }

    public User getOtherUser() {
        return sender.getUsername().equals(LOGGED_IN_USERNAME) ? receiver : sender;
    }

    private String generateText() {
        switch (type) {
            case MESSAGE:
                return content;

            case CONNECT:
                if (sentByMe()) {
                    return "You requested to start a session.";
                }

                return getOtherUser().getShortestName() + " requested to start a session.";

            case FILE:
                String fileUrl = escapeHtml(request.getUrl());
                String fileText = escapeHtml(request.getFilename());
                return "<a href=\"" + fileUrl + "\">" + fileText + "</a>";

            case REQUEST:
                String attached = getOtherUser().getShortestName() + " attached a request: ";

                if (sentByMe()) {
                    attached = "You attached a request: ";
                }

                String reqUrl = escapeHtml(request.getQuestionPageUrl());
                String reqText = escapeHtml(request.getTitle());

                return italic("<b>" + attached + "</b><a href=\"" + reqUrl + "\">" + reqText + "</a>");

            case SIGNATURE:
                if (sentByMe()) {
                    return "You initiated a Non-Disclosure Agreement request.";
                }

                return getOtherUser().getShortestName() + " initiated a Non-Disclosure Agreement request.";

            default:
                return "Message type not yet supported by this app.";
        }
    }

    private String generateContentDescription() {
        switch (type) {
            case MESSAGE:
                if (sentByMe()) {
                    return italic("You: ") + content;
                }

                return content;

            case CONNECT:
                if (sentByMe()) {
                    return italic("You requested a session.");
                }

                return italic(getOtherUser().getShortestName() + " requested a session.");

            case FILE:
                return italic(escapeHtml(request.getFilename()));

            case REQUEST:
                if (sentByMe()) {
                    return italic("You attached a request.");
                }

                return italic(getOtherUser().getShortestName() + " attached a request.");

            case SIGNATURE:
                if (sentByMe()) {
                    return italic("You initiated a NDA request.");
                }

                return italic(getOtherUser().getShortestName() + " initiated a NDA request.");

            default:
                return italic("Unsupported message type.");
        }
    }

}
