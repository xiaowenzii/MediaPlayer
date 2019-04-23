package com.wellav.omp.iptv;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wellav.omp.R;
import com.wellav.omp.app.TVideoApplication;
import com.wellav.omp.been.ContentVideoView;
import com.wellav.omp.channel.SysConfig;
import com.wellav.omp.inter.IMultKeyTrigger;
import com.wellav.omp.inter.MultKeyTrigger;
import com.wellav.omp.ui.FullScreenView;
import com.wellav.omp.ui.ShowBottomMenu;
import com.wellav.omp.ui.ShowConfirmDialog;
import com.wellav.omp.ui.ShowSystemDialog;
import com.wellav.omp.utils.GetSystemTime;
import com.wellav.omp.utils.NetUtils;
import com.wellav.omp.utils.PermisionUtils;
import com.wellav.omp.utils.SharedData;
import com.wellav.omp.utils.ToastUtil;
import com.wellav.omp.utils.ULog;
import com.wellav.omp.utils.Utilss;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {
    private final static String TAG = "MainActivity";
    private final static int CHANGE_MAINBG_TIME = 0;
    private final static int SHOWDIALOG = 1;
    private final static int EXIT = 2;
    private final static int AUTOPLAY = 3;
    private final static int RELOAD = 4;
    private final static int SHOWBOTTOMMENU = 5;
    private final static int SHOWNEXT = 6;
    private final static int NEW_LANG_DATA = 7;

    //服务器
    private String ipServer;
    private String newIP;
    private Boolean isNeedUpdateApk;
    private int updateOpt;
    private int versionCode;
    private String version;
    private String updateUrl;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private File storgePath;
    //节目列表
    private Bitmap mainBg;
    private String sysTime;
    private String time;
    private String date;
    private String week;
    private ImageView mainContainerBg;
    private RelativeLayout relativeLayoutMain;

    private TextView mainTime;
    private TextView mainDate;
    private TextView mainWeek;

    private List<String> urls = new ArrayList<>();
    private List<String> types = new ArrayList<>();

    private IMultKeyTrigger showMenuMultKeyTrigger;
    private IMultKeyTrigger exitMultKeyTrigger;

    private Thread getBitmapThread;
    private boolean isGetBitmap = true;

    private RelativeLayout relativeLayoutVideo;
    private RelativeLayout relativeLayoutImage;
    private FullScreenView mainVideo;
    private ContentVideoView contentVideoView;

    private ShowBottomMenu showBottomMenu;
    private String lang;
    private String langData;

    private ShowSystemDialog showSystemDialog;

    private int exitOpt;
    private int num = 0;
    private boolean isVideo;
    private boolean isAuthoPlay = true;
    private int videoPause = 0;
    private int size = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        //获取数据
        isNeedUpdateApk = TVideoApplication.getInstance().getNeedUpdateApk();
        updateOpt = TVideoApplication.getInstance().getUpdateOpt();
        versionCode = TVideoApplication.getInstance().getVersionCode();
        version = TVideoApplication.getInstance().getVersion();
        updateUrl = TVideoApplication.getInstance().getUpdateUrl();
        exitOpt = TVideoApplication.getInstance().getExitOpt();

        //获取缓存
        sharedPreferences = SharedData.getSystemSettingsSharedPreferences(this);
        editor = sharedPreferences.edit();
        ipServer = sharedPreferences.getString(SharedData.IPSERVER, SysConfig.IP_SERVER);
        lang = sharedPreferences.getString(SharedData.LANG, SharedData.CHINESE);

        //插件初始化
        mainContainerBg = findViewById(R.id.main_image);
        mainVideo = findViewById(R.id.main_video);
        relativeLayoutMain = findViewById(R.id.relativeLayout_main);
        relativeLayoutImage = findViewById(R.id.relativeLayout_Main_Image);
        relativeLayoutVideo = findViewById(R.id.relativeLayout_Main_Video);

        mainTime = findViewById(R.id.main_time);
        mainDate = findViewById(R.id.main_date);
        mainWeek = findViewById(R.id.main_week);
        if (exitOpt == 1) {
            exitMultKeyTrigger = new MultKeyTrigger(this, new int[]{-3, -3, -3, -3, -3});
        }
        //弹出设置组合键：下下上上
        showMenuMultKeyTrigger = new MultKeyTrigger(this, new int[]{13, 13, 12, 12});
        contentVideoView = new ContentVideoView(getApplicationContext(), mainVideo, new ContentVideoView.OnCompleteListener() {
            @Override
            public void OnComplete() {
                isVideo = false;
                isAuthoPlay = false;
                num = (num + 1) % urls.size();
                if ("image".equals(types.get(num))) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mainBg = Utilss.httpGetBitmap2(urls.get(num));
                            mHandler.sendEmptyMessage(CHANGE_MAINBG_TIME);
                        }
                    }).start();
                    mHandler.sendEmptyMessageDelayed(AUTOPLAY, 3000);
                } else if ("video".equals(types.get(num))) {
                    showNext(true);
                }
            }
        });
        editor.putInt(SharedData.CURRENTMODEL, 0);
        editor.commit();
        showBottomMenu = new ShowBottomMenu(MainActivity.this, relativeLayoutMain, true);
        showBottomMenu.addView();
        showBottomMenu.showBottom();
        //加载网络数据
        initLazyData();
    }

    protected void initLazyData() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    //获取时间
                    sysTime = GetSystemTime.GetTimeFromServer(ipServer, SysConfig.YEAR_MONTH_DATE);
                    //TODO 获取主界面背景图片URLs
                    String mainBgData = Utilss.httpGetChannels(ipServer + "/source/home/home.json");
                    if (!"".equals(mainBgData) && mainBgData != null) {
                        JSONObject mainBgDataJson = new JSONObject(mainBgData);
                        if (Utilss.getJsonDataInt(mainBgDataJson, "code") == 0) {
                            JSONObject dataJson = Utilss.getJsonObject(mainBgDataJson, "data");
                            if (dataJson != null) {
                                JSONArray mainBgArray = Utilss.getJsonArray(dataJson, "promotionlist");
                                if (mainBgArray != null) {
                                    if ("image".equals(Utilss.getJsonDataString(mainBgArray.getJSONObject(0), "type"))) {
                                        mainBg = Utilss.httpGetBitmap2(ipServer + Utilss.getJsonDataString(mainBgArray.getJSONObject(0), "pic"));
                                    }
                                    for (int i = 0; i < mainBgArray.length(); i++) {
                                        String url = Utilss.getJsonDataString(mainBgArray.getJSONObject(i), "pic");
                                        if (url != null && !"".equals(url)) {
                                            urls.add(ipServer + Utilss.getJsonDataString(mainBgArray.getJSONObject(i), "pic"));
                                        }
                                        String type = Utilss.getJsonDataString(mainBgArray.getJSONObject(i), "type");
                                        if (type != null && !"".equals(type)) {
                                            types.add(Utilss.getJsonDataString(mainBgArray.getJSONObject(i), "type"));
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean b) {
                super.onPostExecute(b);
                showNext(true);
                if (sysTime.split(" ").length > 1) {
                    mainTime.setText(sysTime.split(" ")[1].split(":")[0] + " : " + sysTime.split(" ")[1].split(":")[1]);
                    mainDate.setText(sysTime.split(" ")[0]);
                    if (sysTime.split(" ").length == 3) {
                        mainWeek.setText(Utilss.getWeek(getApplicationContext(), sysTime.split(" ")[2]));
                    }
                }
                init();
            }
        }.execute();
    }

    private void init() {
        //设置背景图片
        if (urls.size() != 0) {
            mainContainerBg.setImageBitmap(mainBg);
        } else {
            ULog.e(TAG, "获取主界面图片失败");
            mainContainerBg.setImageResource(R.mipmap.main_background);
        }
        getBitmapThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isGetBitmap) {
                    try {
                        Thread.sleep(6000);
                        sysTime = GetSystemTime.GetTimeFromServer(ipServer, SysConfig.YEAR_MONTH_DATE);
                        if (urls.size() > 0 && !isVideo && isAuthoPlay) {
                            num += 1;
                            num %= urls.size();
                            if ("image".equals(types.get(num))) {
                                mainBg = Utilss.httpGetBitmap2(urls.get(num));
                            }
                        }
                        if (isVideo) {
                            if (!contentVideoView.isPlaying()) {
                                if (videoPause == 1) {
                                    videoPause = 0;
                                    if (urls.size() > 0) {
                                        urls.remove(urls.get(num));
                                        types.remove(types.get(num));
                                        if (urls.size() > 0) {
                                            size = urls.size();
                                            num = num % (urls.size());
                                        } else {
                                            size = 0;
                                        }
                                    }
                                    mHandler.sendEmptyMessage(SHOWNEXT);
                                }
                                videoPause++;
                            }
                        }
                        mHandler.sendEmptyMessage(CHANGE_MAINBG_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        getBitmapThread.start();

        //Android 7.0及以上，请求权限
        if (Build.VERSION.SDK_INT >= 23) {
            PermisionUtils.verifyStoragePermissions(this);
        }

        //判断网络是否可用
        if (!NetUtils.isNetworkAvailable(this)) {
            showSetNetworkUI(this);
        }
        if (updateOpt == 0 && isNeedUpdateApk) {
            Utilss.downFile(MainActivity.this, storgePath, updateUrl);
        } else if (updateOpt == 1 && isNeedUpdateApk) {
            showUpdateDialog();
        } else if (updateOpt == 2 && isNeedUpdateApk) {
            editor.putInt(SharedData.VERSIONCODE, versionCode);
            editor.commit();
            showUpdateDialog();
        }

        if (lang.equals(SharedData.CHINESE)) {
            TVideoApplication.getInstance().setLanguage(Locale.CHINESE);
        } else {
            TVideoApplication.getInstance().setLanguage(Locale.ENGLISH);
        }
    }

    public void showNext(boolean toNext) {
        size = types.size();
        if (size == 0) {
            showImageOrVideo("Image");
            num = -1;
            mainBg = Utilss.readBitMap(this, R.mipmap.main_background);
            mainContainerBg.setImageBitmap(mainBg);
        } else if (size > 0) {
            if ("image".equals(types.get(num))) {
                if (contentVideoView != null && contentVideoView.isPlaying()) {
                    contentVideoView.pause();
                }
                isVideo = false;
                showImageOrVideo("Image");
                if (mainBg != null) {
                    mainContainerBg.setImageBitmap(mainBg);
                } else {
                    urls.remove(urls.get(num));
                    types.remove(types.get(num));
                    if (urls.size() > 0) {
                        num = num % (urls.size());
                        if ("image".equals(types.get(num))) {
                            showImageOrVideo("Image");
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    mainBg = Utilss.httpGetBitmap2(urls.get(num));
                                    mHandler.sendEmptyMessageDelayed(SHOWNEXT, 1000);
                                }
                            }).start();
                        } else {
                            showNext(true);
                        }
                    } else {
                        mainBg = Utilss.readBitMap(this, R.mipmap.main_background);
                        mainContainerBg.setImageBitmap(mainBg);
                    }
                }
            } else if ("video".equals(types.get(num))) {
                isVideo = true;
                isAuthoPlay = false;
                showImageOrVideo("Video");
                contentVideoView.setPlayUrl(urls.get(num));
                contentVideoView.play();
            }
        }
    }

    public void showImageOrVideo(String showType) {
        if ("Image".equals(showType)) {
            if (contentVideoView.isPlaying()) {
                contentVideoView.pause();
            }
            if (relativeLayoutVideo.getVisibility() == View.VISIBLE) {
                relativeLayoutVideo.setVisibility(View.GONE);
            }
            relativeLayoutImage.setVisibility(View.VISIBLE);
        } else if ("Video".equals(showType)) {
            if (relativeLayoutImage.getVisibility() == View.VISIBLE) {
                relativeLayoutImage.setVisibility(View.GONE);
            }
            relativeLayoutVideo.setVisibility(View.VISIBLE);
        }
    }

    private void showUpdateDialog() {
        new ShowConfirmDialog(this, R.style.dialog, getResources().getString(R.string.update_apk_version) + version, new ShowConfirmDialog.OnCloseListener() {
            @Override
            public void onClick(Dialog dialog, boolean confirm) {
                if (confirm) {
                    dialog.dismiss();
                    Utilss.downFile(MainActivity.this, storgePath, updateUrl);
                } else {
                    dialog.dismiss();
                }
            }
        }).setTitle(getResources().getString(R.string.prompt)).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (exitOpt == 0) {
                    showIsExit();
                } else if (exitOpt == 1) {
                    handlerMultKey(keyCode, event, EXIT);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                showBottomMenu.moveToLeft();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                showBottomMenu.moveToRight();
                break;
        }
        handlerMultKey(keyCode, event, SHOWDIALOG);
        return false;
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        showBottomMenu.reSetNavBg(sharedPreferences.getInt(SharedData.CURRENTMODEL, 0));
        ipServer = sharedPreferences.getString(SharedData.IPSERVER, SysConfig.IP_SERVER);
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        isGetBitmap = false;
        if (getBitmapThread != null) {
            getBitmapThread.interrupt();
            getBitmapThread = null;
        }
        if (contentVideoView != null) {
            contentVideoView.Destroy();
            contentVideoView = null;
        }
        super.onDestroy();
    }


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CHANGE_MAINBG_TIME:
                    if (sysTime.split(" ").length > 1) {
                        if (!(sysTime.split(" ")[1].split(":")[0] + " : " + sysTime.split(" ")[1].split(":")[1]).equals(time)) {
                            time = sysTime.split(" ")[1].split(":")[0] + " : " + sysTime.split(" ")[1].split(":")[1];
                            mainTime.setText(time);
                        }
                        if (!(sysTime.split(" ")[0]).equals(date)) {
                            date = sysTime.split(" ")[0];
                            mainDate.setText(sysTime.split(" ")[0]);
                        }
                        if (sysTime.split(" ").length == 3) {
                            if (!Utilss.getWeek(getApplicationContext(), sysTime.split(" ")[2]).equals(week)) {
                                week = Utilss.getWeek(getApplicationContext(), sysTime.split(" ")[2]);
                                mainWeek.setText(week);
                            }
                        }
                    }
                    if (!isVideo) {
                        showNext(true);
                    }
                    break;
                case SHOWDIALOG:
                    if (!showSystemDialog.isShowing()) {
                        showSystemDialog.show();
                    }
                    break;
                case AUTOPLAY:
                    isAuthoPlay = true;
                    break;
                case RELOAD:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                isVideo = true;
                                String mainBgData = Utilss.httpGetChannels(ipServer + "/source/home/home.json");
                                if (!"".equals(mainBgData) && mainBgData != null) {
                                    JSONObject mainBgDataJson = new JSONObject(mainBgData);
                                    if (Utilss.getJsonDataInt(mainBgDataJson, "code") == 0) {
                                        JSONObject dataJson = Utilss.getJsonObject(mainBgDataJson, "data");
                                        if (dataJson != null) {
                                            JSONArray mainBgArray = Utilss.getJsonArray(dataJson, "promotionlist");
                                            if (mainBgArray != null) {
                                                if (urls.size() != 0 || types.size() != 0) {
                                                    urls.clear();
                                                    types.clear();
                                                }
                                                if ("image".equals(Utilss.getJsonDataString(mainBgArray.getJSONObject(0), "type"))) {
                                                    mainBg = Utilss.httpGetBitmap2(ipServer + Utilss.getJsonDataString(mainBgArray.getJSONObject(0), "pic"));
                                                }
                                                for (int i = 0; i < mainBgArray.length(); i++) {
                                                    String url = Utilss.getJsonDataString(mainBgArray.getJSONObject(i), "pic");
                                                    if (url != null && !"".equals(url)) {
                                                        Log.e("ipServer", ipServer + "");
                                                        urls.add(ipServer + Utilss.getJsonDataString(mainBgArray.getJSONObject(i), "pic"));
                                                    }
                                                    String type = Utilss.getJsonDataString(mainBgArray.getJSONObject(i), "type");
                                                    if (type != null && !"".equals(type)) {
                                                        types.add(Utilss.getJsonDataString(mainBgArray.getJSONObject(i), "type"));
                                                    }
                                                }
                                            } else {
                                                if (urls.size() != 0 || types.size() != 0) {
                                                    urls.clear();
                                                    types.clear();
                                                }
                                            }
                                            num = 0;
                                            isVideo = false;
                                            isAuthoPlay = true;
                                            mHandler.sendEmptyMessage(CHANGE_MAINBG_TIME);
                                        }
                                    }
                                } else {
                                    if (urls.size() != 0 || types.size() != 0) {
                                        urls.clear();
                                        types.clear();
                                    }
                                    num = 0;
                                    isVideo = false;
                                    isAuthoPlay = true;
                                    mHandler.sendEmptyMessage(CHANGE_MAINBG_TIME);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    break;
                case SHOWBOTTOMMENU:
                    showBottomMenu.showBottom();
                    showBottomMenu.updateNavBg();
                    break;
                case SHOWNEXT:
                    showNext(true);
                    break;
                case NEW_LANG_DATA:
                    if (langData != null && !"".equals(langData)) {
                        try {
                            JSONObject jsonObject = new JSONObject(langData);
                            if (Utilss.getJsonDataInt(jsonObject, "code") == 0) {
                                ipServer = newIP;
                                editor.putString(SharedData.IPSERVER, ipServer);
                                editor.commit();
                                showBottomMenu.updateIP();
                                ToastUtil.showText(getApplicationContext(), getResources().getString(R.string.ip_set_success));
                                mHandler.sendEmptyMessage(RELOAD);
                            } else {
                                ToastUtil.showText(getApplicationContext(), getResources().getString(R.string.netword_unavailable));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        ToastUtil.showText(getApplicationContext(), getResources().getString(R.string.ip_cannot_null));
                    }
                default:
                    break;
            }
        }
    };

    private void showIsExit() {
        new ShowConfirmDialog(this, R.style.dialog, getResources().getString(R.string.exit_system), new ShowConfirmDialog.OnCloseListener() {
            @Override
            public void onClick(Dialog dialog, boolean confirm) {
                if (confirm) {
                    dialog.dismiss();
                    finish();
                } else {
                    dialog.dismiss();
                }
            }
        }).setTitle(getResources().getString(R.string.prompt)).show();
    }

    /*
     * 打开设置网络界面
     */
    public void showSetNetworkUI(final Context context) {
        // 提示对话框
        new ShowConfirmDialog(this, R.style.dialog, getResources().getString(R.string.net_work_not_used), new ShowConfirmDialog.OnCloseListener() {
            @Override
            public void onClick(Dialog dialog, boolean confirm) {
                if (confirm) {
                    dialog.dismiss();
                    // 播放
                    Intent intent = null;
                    if (Build.VERSION.SDK_INT > 10) {
                        intent = new Intent(
                                android.provider.Settings.ACTION_WIFI_SETTINGS);
                    } else {
                        intent = new Intent();
                        ComponentName component = new ComponentName(
                                "com.android.settings",
                                "com.android.settings.WirelessSettings");
                        intent.setComponent(component);
                        intent.setAction("android.intent.action.VIEW");
                    }
                    context.startActivity(intent);
                } else {
                    dialog.dismiss();
                    finish();
                }
            }
        }).setTitle(getResources().getString(R.string.prompt)).show();
    }

    public boolean handlerMultKey(int keyCode, KeyEvent event, int type) {
        boolean vaildKey = false;
        if (type == SHOWDIALOG) {
            if (showBottomMenu.getCurrentModel() == 0 && (keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                    || keyCode == KeyEvent.KEYCODE_DPAD_UP) && showMenuMultKeyTrigger.allowTrigger()) {
                // 是否是有效按键输入
                vaildKey = showMenuMultKeyTrigger.checkKey(keyCode, event.getEventTime());
                // 是否触发组合键
                if (vaildKey && showMenuMultKeyTrigger.checkMultKey()) {
                    //执行触发
                    showMenuMultKeyTrigger.onTrigger();
                    //触发完成后清除掉原先的输入
                    showMenuMultKeyTrigger.clearKeys();
                    mainVideo.setFocusable(false);
                    showBottomMenu.hideBottom();
                    showSystemDialog = new ShowSystemDialog(this, R.style.dialog, new ShowSystemDialog.OnCloseListener() {
                        @Override
                        public void onClick(Dialog dialog, boolean confirm, final String inRoom, final String inServer) {
                            if (confirm) {
                                if (!"".equals(inServer) && inServer != null && !("http://" + inServer).equals(ipServer)) {
                                    newIP = "http://" + inServer;
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            langData = Utilss.httpGetChannels("http://" + inServer + "/source/language.json");
                                            mHandler.sendEmptyMessage(NEW_LANG_DATA);
                                        }
                                    }).start();
                                }
                            }
                            dialog.dismiss();
                            mHandler.sendEmptyMessage(SHOWBOTTOMMENU);
                        }
                    });
                    mHandler.sendEmptyMessageDelayed(SHOWDIALOG, 500);
                }
            }
        } else if (type == EXIT) {
            if (keyCode == KeyEvent.KEYCODE_BACK && exitMultKeyTrigger.allowTrigger()) {
                // 是否是有效按键输入
                vaildKey = exitMultKeyTrigger.checkKey(keyCode, event.getEventTime());
                // 是否触发组合键
                if (vaildKey && exitMultKeyTrigger.checkMultKey()) {
                    //执行触发
                    exitMultKeyTrigger.onTrigger();
                    exitMultKeyTrigger.clearKeys();
                    //触发完成后清除掉原先的输入
                    showIsExit();
                }
            }
        }
        return vaildKey;
    }
}