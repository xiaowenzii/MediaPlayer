package com.wellav.omp.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.wellav.omp.R;
import com.wellav.omp.inter.IMultKeyTrigger;
import com.wellav.omp.inter.MultKeyTrigger;

public class ShowIpInputDialog extends Dialog implements View.OnClickListener {
    private EditText inputId;
    private TextView titleTxt;
    private TextView submitTxt;
    private TextView cancelTxt;

    private Context mContext;
    private String ip;
    private OnCloseListener listener;
    private String positiveName;
    private String negativeName;
    private String title;
    private int i;
    private IMultKeyTrigger multKeyTrigger;


    public ShowIpInputDialog(Context context, int themeResId, String content, OnCloseListener listener) {
        super(context, themeResId);
        this.mContext = context;
        this.ip = content;
        this.listener = listener;
        this.multKeyTrigger = new MultKeyTrigger(context, new int[]{13, 13, 12, 12});
    }


    public ShowIpInputDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public ShowIpInputDialog setPositiveButton(String name) {
        this.positiveName = name;
        return this;
    }

    public ShowIpInputDialog setNegativeButton(String name) {
        this.negativeName = name;
        return this;
    }

    public ShowIpInputDialog setFocus(int i) {
        this.i = i;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_input_ip);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        initView();
        if (i == 0) {
            submitTxt.setFocusable(true);
            submitTxt.requestFocus();
        } else if (i == 1) {
            inputId.setFocusable(true);
            inputId.requestFocus();
        }
    }

    private void initView() {
        inputId = findViewById(R.id.input_ip);
        titleTxt = findViewById(R.id.input_title);
        submitTxt = findViewById(R.id.ip_ok);
        cancelTxt = findViewById(R.id.ip_cancel);
        submitTxt.setOnClickListener(this);
        cancelTxt.setOnClickListener(this);

        inputId.setText(ip);
        //inputId.setEnabled(false);
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
            case R.id.ip_cancel:
                if (listener != null) {
                    listener.onClick(this, false, inputId.getText().toString());
                }
                this.dismiss();
                break;
            case R.id.ip_ok:
                if (listener != null) {
                    listener.onClick(this, true, inputId.getText().toString());
                }
                break;
        }
    }

    public interface OnCloseListener {
        void onClick(Dialog dialog, boolean confirm, String inIp);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (handlerMultKey(keyCode, event)) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean handlerMultKey(int keyCode, KeyEvent event) {
        boolean vaildKey = false;
        if ((keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                || keyCode == KeyEvent.KEYCODE_DPAD_UP) && multKeyTrigger.allowTrigger()) {
            // 是否是有效按键输入
            vaildKey = multKeyTrigger.checkKey(keyCode, event.getEventTime());
            // 是否触发组合键
            if (vaildKey && multKeyTrigger.checkMultKey()) {
                //执行触发
                multKeyTrigger.onTrigger();
                //触发完成后清除掉原先的输入
                multKeyTrigger.clearKeys();
                inputId.setEnabled(true);
            }
        }
        return vaildKey;
    }
}