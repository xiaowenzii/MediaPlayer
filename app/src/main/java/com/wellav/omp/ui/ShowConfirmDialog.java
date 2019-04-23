package com.wellav.omp.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.wellav.omp.R;

public class ShowConfirmDialog extends Dialog implements View.OnClickListener {
    private TextView contentTxt;
    private TextView titleTxt;
    private TextView submitTxt;
    private TextView cancelTxt;

    private String content;
    private String yes;
    private String no;
    private OnCloseListener listener;
    private String positiveName;
    private String negativeName;
    private String title;

    public ShowConfirmDialog(Context context, int themeResId, String content, OnCloseListener listener) {
        super(context, themeResId);
        this.content = content;
        this.listener = listener;
    }

    public ShowConfirmDialog(Context context, int themeResId, String content, String sure, String cancel, OnCloseListener listener) {
        super(context, themeResId);
        this.content = content;
        this.yes = sure;
        this.no = cancel;
        this.listener = listener;
    }

    public ShowConfirmDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public ShowConfirmDialog setPositiveButton(String name) {
        this.positiveName = name;
        return this;
    }

    public ShowConfirmDialog setNegativeButton(String name) {
        this.negativeName = name;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_confirm);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        initView();
        submitTxt.setFocusable(true);
        submitTxt.requestFocus();
    }

    private void initView() {
        contentTxt = (TextView) findViewById(R.id.message);
        titleTxt = (TextView) findViewById(R.id.title);
        submitTxt = (TextView) findViewById(R.id.ok);
        submitTxt.setOnClickListener(this);
        cancelTxt = (TextView) findViewById(R.id.cancel);
        cancelTxt.setOnClickListener(this);


        contentTxt.setText(content);
        if (!TextUtils.isEmpty(yes)) {
            submitTxt.setText(yes);
            cancelTxt.setText(no);
        }
        if (!TextUtils.isEmpty(positiveName)) {
            submitTxt.setText(positiveName);
        }

        if (!TextUtils.isEmpty(negativeName)) {
            cancelTxt.setText(negativeName);
        }

        if (!TextUtils.isEmpty(title)) {
            titleTxt.setText(title);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                if (listener != null) {
                    listener.onClick(this, false);
                }
                this.dismiss();
                break;
            case R.id.ok:
                if (listener != null) {
                    listener.onClick(this, true);
                }
                break;
        }
    }

    public interface OnCloseListener {
        void onClick(Dialog dialog, boolean confirm);
    }

}