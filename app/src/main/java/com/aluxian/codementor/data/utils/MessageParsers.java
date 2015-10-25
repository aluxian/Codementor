package com.aluxian.codementor.data.utils;

import android.text.Html;

import com.aluxian.codementor.data.models.Request;
import com.aluxian.codementor.data.models.User;
import com.aluxian.codementor.data.types.MessageType;
import com.aluxian.codementor.services.ErrorHandler;
import com.aluxian.codementor.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MessageParsers {

    public static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);

    public static String parseTypeContent(MessageType type, String content, boolean sentByCurrentUser,
                                          User otherUser, Request request) {
        switch (type) {
            case MESSAGE:
                return content;

            case CONNECT:
                if (sentByCurrentUser) {
                    return "You requested to start a session.";
                }

                return otherUser.getShortestName() + " requested to start a session.";

            case FILE:
                String fileUrl = escapeHtml(request.getUrl());
                String fileText = escapeHtml(request.getFilename());
                return "<a href=\"" + fileUrl + "\">" + fileText + "</a>";

            case REQUEST:
                String attachedMessage = otherUser.getShortestName() + " attached a request: ";

                if (sentByCurrentUser) {
                    attachedMessage = "You attached a request: ";
                }

                String reqUrl = escapeHtml(Constants.getRequestUrl(request.getId()));
                String reqText = escapeHtml(request.getTitle());
                String attachedText = escapeHtml(attachedMessage);

                return "<i><b>" + attachedText + "</b></i><a href=\"" + reqUrl + "\">" + reqText + "</a>";

            case SIGNATURE:
                if (sentByCurrentUser) {
                    return "You initiated a Non-Disclosure Agreement request.";
                }

                return otherUser.getShortestName() + " initiated a Non-Disclosure Agreement request.";

            default:
                return "This message type is not yet supported.";
        }
    }

    public static String parseTypeContentChatroom(MessageType type, String content, boolean sentByCurrentUser,
                                                  User otherUser, Request request) {
        switch (type) {
            case MESSAGE:
                return content;

            case CONNECT:
                if (sentByCurrentUser) {
                    return italic("You requested a session.");
                }

                return italic(otherUser.getShortestName() + " requested a session.");

            case FILE:
                return italic(escapeHtml(request.getFilename()));

            case REQUEST:
                if (sentByCurrentUser) {
                    return italic("You attached a request.");
                }

                return italic(otherUser.getShortestName() + " attached a request.");

            case SIGNATURE:
                if (sentByCurrentUser) {
                    return italic("You initiated a NDA request.");
                }

                return italic(otherUser.getShortestName() + " initiated a NDA request.");

            default:
                return italic("Message type not yet supported.");
        }
    }

    public static String italic(String text) {
        return "<i>" + text + "</i>";
    }

    public static String escapeHtml(String html) {
        return Html.fromHtml(html).toString();
    }

    public static MessageType parseType(String rawType, ErrorHandler errorHandler) {
        rawType = rawType.toUpperCase();

        if (rawType.equals("PENDING_MSG")) {
            rawType = "MESSAGE";
        }

        if (rawType.equals("SESSIONLINK") || rawType.contains("CONNECT")) {
            rawType = "CONNECT";
        }

        try {
            return MessageType.valueOf(rawType);
        } catch (IllegalArgumentException e) {
            errorHandler.log(e);
            return MessageType.OTHER;
        }
    }

    public static long parseDate(Object createdAt) {
        try {
            return Double.valueOf(String.valueOf(createdAt)).longValue();
        } catch (NumberFormatException e1) {
            try {
                return DATE_FORMAT.parse((String) createdAt).getTime();
            } catch (ParseException e2) {
                return 0;
            }
        }
    }

}
