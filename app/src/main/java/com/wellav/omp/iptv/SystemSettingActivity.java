package com.wellav.omp.iptv;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.wellav.omp.R;
import com.wellav.omp.app.TVideoApplication;
import com.wellav.omp.ui.ShowConfirmDialog;
import com.wellav.omp.utils.SharedData;
import com.wellav.omp.utils.ToastUtil;
import com.wellav.omp.utils.Utilss;

import java.io.File;

public class SystemSettingActivity extends BaseActivity implements View.OnClickListener {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Switch st_selfstart;
    private TextView tv_banben, tv_xin;
    private RelativeLayout rl_banben;
    private RelativeLayout rl_switch;
    private String CurrentVersion;
    private File storgePath;
    private String updateUrl;
    private int versionCode;
    private String version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_settings);

        versionCode = TVideoApplication.getInstance().getVersionCode();
        version = TVideoApplication.getInstance().getVersion();
        updateUrl = TVideoApplication.getInstance().getUpdateUrl();

        //获取缓存
        sharedPreferences = SharedData.getSystemSettingsSharedPreferences(this);
        editor = sharedPreferences.edit();

        init();
    }

    public void init() {
        st_selfstart = findViewById(R.id.st_selfstart);
        st_selfstart.setChecked(sharedPreferences.getBoolean(SharedData.APPAUTOSTART, false));
        st_selfstart.setOnClickListener(this);
        tv_banben = findViewById(R.id.tv_banben);
        tv_xin = findViewById(R.id.tv_xin);
        rl_banben = findViewById(R.id.rl_banben);
        rl_switch = findViewById(R.id.rl_switch);
        CurrentVersion = Utilss.getVersion(this);
        tv_banben.setText("V " + CurrentVersion);

        if (versionCode > Utilss.getVersionCode(this)) {
            tv_xin.setText("(new)");
            tv_xin.setTextColor(Color.parseColor("#EA2000"));
        } else {
            tv_xin.setText("");
        }
        rl_banben.setOnClickListener(this);
        rl_switch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.st_selfstart:
                if (st_selfstart.isChecked()) {
                    editor.putBoolean(SharedData.APPAUTOSTART, true);
                    ToastUtil.showText(this, getResources().getString(R.string.self_start_open_words));
                } else {
                    editor.putBoolean(SharedData.APPAUTOSTART, false);
                    ToastUtil.showText(this, getResources().getString(R.string.self_start_close_words));
                }
                editor.commit();
                break;
            case R.id.rl_banben:
                if (versionCode > Utilss.getVersionCode(this)) {
                    showUpdateDialog();
                } else {
                    ToastUtil.showText(this, getResources().getString(R.string.app_is_the_latest));
                }
                break;
            case R.id.rl_switch:
                st_selfstart.performClick();
                break;

        }
    }

    private void showUpdateDialog() {
        new ShowConfirmDialog(this, R.style.dialog, getResources().getString(R.string.update_apk_version_1) + version + " ?", new ShowConfirmDialog.OnCloseListener() {
            @Override
            public void onClick(Dialog dialog, boolean confirm) {
                if (confirm) {
                    dialog.dismiss();
                    Utilss.downFile(SystemSettingActivity.this, storgePath, updateUrl);
                } else {
                    dialog.dismiss();
                }
            }
        }).setTitle(getResources().getString(R.string.prompt)).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
