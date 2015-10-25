package com.aluxian.codementor.ui;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageButton;

public class MessageFieldTextWatcher implements TextWatcher {

    private final int sendButtonDisabledColor;
    private final int brandAccentColor;
    private final ImageButton sendButton;

    public MessageFieldTextWatcher(int sendButtonDisabledColor, int brandAccentColor, ImageButton sendButton) {
        this.sendButtonDisabledColor = sendButtonDisabledColor;
        this.brandAccentColor = brandAccentColor;
        this.sendButton = sendButton;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() > 0) {
            if (!sendButton.isClickable()) {
                animateSendButton(sendButtonDisabledColor, brandAccentColor);
                sendButton.setClickable(true);
            }
        } else {
            if (sendButton.isClickable()) {
                animateSendButton(brandAccentColor, sendButtonDisabledColor);
                sendButton.setClickable(false);
            }
        }
    }

    private void animateSendButton(int fromColor, int toColor) {
        final float[] from = new float[3];
        final float[] to = new float[3];

        Color.colorToHSV(fromColor, from);
        Color.colorToHSV(toColor, to);

        ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
        anim.setDuration(300);

        final float[] hsv = new float[3];
        anim.addUpdateListener(animation -> {
            hsv[0] = from[0] + (to[0] - from[0]) * animation.getAnimatedFraction();
            hsv[1] = from[1] + (to[1] - from[1]) * animation.getAnimatedFraction();
            hsv[2] = from[2] + (to[2] - from[2]) * animation.getAnimatedFraction();

            sendButton.setColorFilter(Color.HSVToColor(hsv), PorterDuff.Mode.SRC_ATOP);
        });

        anim.start();
    }

}
