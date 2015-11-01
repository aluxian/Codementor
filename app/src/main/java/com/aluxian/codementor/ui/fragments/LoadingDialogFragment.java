package com.aluxian.codementor.ui.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aluxian.codementor.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoadingDialogFragment extends DialogFragment {

    private static final String ARG_INITIAL_MESSAGE_ID = "initial_message_id";
    private static final String ARG_CURRENT_MESSAGE = "current_message";

    @Bind(R.id.tv_message) TextView titleTextView;
    private DialogInterface.OnCancelListener onCancelListener;

    public static LoadingDialogFragment newInstance(int initialMessageId) {
        Bundle args = new Bundle();
        args.putInt(ARG_INITIAL_MESSAGE_ID, initialMessageId);

        LoadingDialogFragment fragment = new LoadingDialogFragment();
        fragment.setStyle(STYLE_NO_TITLE, R.style.LoadingTheme);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_loading, container, false);
        ButterKnife.bind(this, rootView);

        if (savedInstanceState != null) {
            String message = savedInstanceState.getString(ARG_CURRENT_MESSAGE);
            titleTextView.setText(message);
        } else {
            int messageId = getArguments().getInt(ARG_INITIAL_MESSAGE_ID);
            titleTextView.setText(messageId);
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_CURRENT_MESSAGE, titleTextView.getText().toString());
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (onCancelListener != null) {
            onCancelListener.onCancel(dialog);
        }
    }

    public void setMessage(int resId) {
        if (titleTextView != null) {
            titleTextView.setText(resId);
        }
    }

    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
    }

}
