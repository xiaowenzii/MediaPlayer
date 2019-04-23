package com.wellav.omp.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.wellav.omp.R;
import com.wellav.omp.channel.SysConfig;
import com.wellav.omp.utils.SharedData;
import com.wellav.omp.utils.Utilss;

public class ShowSystemDialog extends Dialog implements View.OnClickListener {
    private TextView submitTxt;
    private TextView cancelTxt;

    private EditText editRoom;
    private EditText editServer;
    private SharedPreferences sharedPreferences;
    private String ipServer;

    private String yes;
    private String no;
    private OnCloseListener listener;
    private String positiveName;
    private String negativeName;
    private String title;

    private TextView textRoom;
    private TextView textServerIP;
    private TextView LocalIP;
    private TextView Version;
    private TextView textLocalIP;
    private TextView textVersion;

    private Context context;

    public ShowSystemDialog(Context context, int themeResId, OnCloseListener listener) {
        super(context, themeResId);
        this.context = context;
        this.listener = listener;
    }

    public ShowSystemDialog(Context context, int themeResId, String sure, String cancel, OnCloseListener listener) {
        super(context, themeResId);
        this.context = context;
        this.yes = sure;
        this.no = cancel;
        this.listener = listener;
    }

    public ShowSystemDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public ShowSystemDialog setPositiveButton(String name) {
        this.positiveName = name;
        return this;
    }

    public ShowSystemDialog setNegativeButton(String name) {
        this.negativeName = name;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_layout);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        initView();
    }


    private void initView() {
        editRoom = findViewById(R.id.edit_room);
        editServer = findViewById(R.id.edit_server);

        textRoom = findViewById(R.id.room_number);
        textServerIP= findViewById(R.id.server_ip);
        LocalIP = findViewById(R.id.local_ip);
        textLocalIP = findViewById(R.id.text_local_ip);
        Version = findViewById(R.id.Version);
        textVersion = findViewById(R.id.text_version);
        submitTxt = findViewById(R.id.ok);
        submitTxt.setOnClickListener(this);
        cancelTxt = findViewById(R.id.cancel);
        cancelTxt.setOnClickListener(this);
        textLocalIP.setText(Utilss.getLocalIP(context));
        textVersion.setText(Utilss.getVersion(context));
        //获取缓存
        sharedPreferences = SharedData.getSystemSettingsSharedPreferences(context);
        ipServer = sharedPreferences.getString(SharedData.IPSERVER, SysConfig.IP_SERVER);

        editRoom.setText("666");
        editServer.setText((ipServer.split("//")[1].split(":")[0]));

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

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                if (listener != null) {
                    listener.onClick(this, false, editRoom.getText().toString(), editServer.getText().toString());
                }
                this.dismiss();
                break;
            case R.id.ok:
                if (listener != null) {
                    listener.onClick(this, true, editRoom.getText().toString(), editServer.getText().toString());
                }
                break;
        }
    }

    public interface OnCloseListener {
        void onClick(Dialog dialog, boolean confirm, String inRoom, String inServer);
    }

}