package com.aluxian.codementor.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Helpers {

    public static final SimpleDateFormat CODEMENTOR_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);

    public static boolean isSameDay(long timestamp1, long timestamp2) {
        return isSameDay(new Date(timestamp1), new Date(timestamp2));
    }

    public static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        cal1.setTime(date1);
        cal2.setTime(date2);

        boolean sameYear = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

        return sameYear && sameDay;
    }

    public static long parseDate(Object createdAt) {
        String stringCreatedAt = String.valueOf(createdAt);

        try {
            return Double.valueOf(stringCreatedAt).longValue();
        } catch (NumberFormatException e1) {
            try {
                return CODEMENTOR_DATE_FORMAT.parse(stringCreatedAt).getTime();
            } catch (ParseException e2) {
                return 0;
            }
        }
    }

    public static long parseStringId(String uid) {
        return UUID.fromString(uid).getMostSignificantBits();
    }

    public static String italic(String text) {
        return "<i>" + text + "</i>";
    }

    public static String escapeHtml(String text) {
        StringBuilder out = new StringBuilder();
        withinStyle(out, text, 0, text.length());
        return out.toString();
    }

    private static void withinStyle(StringBuilder out, CharSequence text, int start, int end) {
        for (int i = start; i < end; i++) {
            char c = text.charAt(i);

            if (c == '<') {
                out.append("&lt;");
            } else if (c == '>') {
                out.append("&gt;");
            } else if (c == '&') {
                out.append("&amp;");
            } else if (c >= 0xD800 && c <= 0xDFFF) {
                if (c < 0xDC00 && i + 1 < end) {
                    char d = text.charAt(i + 1);
                    if (d >= 0xDC00 && d <= 0xDFFF) {
                        i++;
                        int codePoint = 0x010000 | (int) c - 0xD800 << 10 | (int) d - 0xDC00;
                        out.append("&#").append(codePoint).append(";");
                    }
                }
            } else if (c > 0x7E || c < ' ') {
                out.append("&#").append((int) c).append(";");
            } else if (c == ' ') {
                while (i + 1 < end && text.charAt(i + 1) == ' ') {
                    out.append("&nbsp;");
                    i++;
                }

                out.append(' ');
            } else {
                out.append(c);
            }
        }
    }

}
