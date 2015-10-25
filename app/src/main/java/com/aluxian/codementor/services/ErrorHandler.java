package com.aluxian.codementor.services;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.aluxian.codementor.BuildConfig;
import com.crashlytics.android.Crashlytics;

public class ErrorHandler {

    private static final String TAG_INIT = "> ";
    private Context context;

    public ErrorHandler(Context context) {
        this.context = context;
    }

    /**
     * Log the given exception.
     *
     * @param e The error to log.
     */
    public static void log(Exception e) {
        Log.e(tag(), e.getMessage(), e);

        if (!BuildConfig.DEBUG) {
            Crashlytics.logException(e);
        }
    }

    /**
     * Log the given exception and show a toast with an error message.
     *
     * @param e The error to log.
     */
    public void logAndToast(Exception e) {
        if (e == null) {
            return;
        }

        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e(tag(), e.getMessage(), e);

        if (!BuildConfig.DEBUG) {
            Crashlytics.logException(e);
        }
    }

    /**
     * @return The calling class and method name to be used as the logcat tag.
     */
    private static String tag() {
        if (!BuildConfig.DEBUG) {
            return "ERROR";
        }

        StackTraceElement element = new Exception().getStackTrace()[2];

        String clazz = element.getClassName().replace(BuildConfig.APPLICATION_ID + ".", "");
        String method = element.getMethodName();
        int line = element.getLineNumber();

        return TAG_INIT + clazz + "::" + method + "@L" + line;
    }

}
