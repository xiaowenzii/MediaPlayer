package com.wellav.omp.iptv;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wellav.omp.R;
import com.wellav.omp.app.TVideoApplication;
import com.wellav.omp.been.ContentVideoView;
import com.wellav.omp.been.Info;
import com.wellav.omp.channel.SysConfig;
import com.wellav.omp.ui.FullScreenView;
import com.wellav.omp.ui.MarqueeText;
import com.wellav.omp.ui.ShowIpInputDialog;
import com.wellav.omp.utils.DiskLruCacheUtil;
import com.wellav.omp.utils.GetIPUtil;
import com.wellav.omp.utils.GetSystemTime;
import com.wellav.omp.utils.NetUtils;
import com.wellav.omp.utils.SharedData;
import com.wellav.omp.utils.ToastUtil;
import com.wellav.omp.utils.Utilss;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.wellav.omp.utils.Utilss.getJsonDataInt;
import static com.wellav.omp.utils.Utilss.getJsonDataString;
import static com.wellav.omp.utils.Utilss.getPhysicalScreenSize;
import static com.wellav.omp.utils.Utilss.httpGetBitmap;

public class SplashActivity extends BaseActivity implements View.OnClickListener {
    private final static int GET_IP = 0;
    private final static int INITLAZYDATA = 1;
    private final static int SET_TIME = 2;
    private final static String VIDEO_COMPLETE = "VIDEO_COMPLETE";
    private final static int AUTOPLAY = 3;
    private final static int SHOWNEXT = 4;

    private Info infos;
    private int size;

    //服务器
    private String ipServer;

    private Bitmap bm;
    //欢迎词
    private String ChineseWords;
    private String EnglishWords;

    // 更新版本要用到的一些信息
    private int versionCode;
    private String version;
    private String updateUrl;
    private Boolean isNeedUpdateApk;
    private String sysTime;

    //登入后获取到的信息
    private String loginData;

    private static SharedPreferences sharedPreferences;

    private RelativeLayout relativeLayoutVideo;
    private RelativeLayout relativeLayoutImage;
    private FullScreenView welcomeVideo;
    private Button buttonLocal;
    private Button buttonEnglish;
    private TextView welcomeTime;
    private TextView welcomeDate;
    private MarqueeText welcomeChineseWords;
    private ImageView welcomeImageView;
    private boolean getTime = true;
    private boolean isVideo;
    private TimeThread timeThread;
    private ContentVideoView contentVideoView;
    private SharedPreferences.Editor editor;
    private Bitmap mainBg;

    private int startOpt = 1;
    private int exitOpt = 1;
    private int updateOpt = 1;

    private List<String> urls = new ArrayList<>();
    private List<String> types = new ArrayList<>();
    private int num = 0;

    private boolean isAuthoPlay = true;

    private String time;
    private String date;

    private TextView textChinnese;
    private TextView textEnglish;
    private RelativeLayout relativeLayoutWelcomeLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_splash);

        //默认不可更新
        isNeedUpdateApk = false;

        //获取缓存
        sharedPreferences = SharedData.getSystemSettingsSharedPreferences(this);
        editor = sharedPreferences.edit();
        ipServer = sharedPreferences.getString(SharedData.IPSERVER, SysConfig.IP_SERVER);

        buttonLocal = findViewById(R.id.button_local);
        buttonLocal.setOnClickListener(this);
        buttonEnglish = findViewById(R.id.button_English);
        buttonEnglish.setOnClickListener(this);
        welcomeTime = findViewById(R.id.welcome_time);
        welcomeDate = findViewById(R.id.welcome_date);
        welcomeChineseWords = findViewById(R.id.welcome_Chinese_words);
        welcomeChineseWords.setMarqueeSpeed(getPhysicalScreenSize(getApplicationContext()) / 1000);
        welcomeImageView = findViewById(R.id.welcome_Imageview);
        relativeLayoutVideo = findViewById(R.id.relativeLayout_Welcome_Video);
        relativeLayoutImage = findViewById(R.id.relativeLayout_Welcome_Image);
        welcomeVideo = findViewById(R.id.welcome_video);
        relativeLayoutVideo.setVisibility(View.GONE);

        textChinnese = findViewById(R.id.text_ok_ch);
        textEnglish = findViewById(R.id.text_ok_en);
        relativeLayoutWelcomeLeft = findViewById(R.id.welcome_left);

        //动态设置按钮布局的宽度
        TextPaint textChinnesePaint = textChinnese.getPaint();
        TextPaint textEnglishPaint = textEnglish.getPaint();
        int textChineseWidth = (int) textChinnesePaint.measureText(textChinnese.getText().toString());
        int textEnglishWidth = (int) textEnglishPaint.measureText(textChinnese.getText().toString());
        ViewGroup.LayoutParams pp = relativeLayoutWelcomeLeft.getLayoutParams();
        if (textChineseWidth > textEnglishWidth) {
            pp.width = textChineseWidth;
        } else {
            pp.width = textEnglishWidth;
        }
        relativeLayoutWelcomeLeft.setLayoutParams(pp);

        contentVideoView = new ContentVideoView(getApplicationContext(), welcomeVideo, new ContentVideoView.OnCompleteListener() {
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
                            mHandler.sendEmptyMessage(SET_TIME);
                        }
                    }).start();
                    mHandler.sendEmptyMessageDelayed(AUTOPLAY, 5000);
                } else if ("video".equals(types.get(num))) {
                    showNext(true);
                }
            }
        });

        buttonLocal.requestFocus();
        buttonLocal.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    welcomeChineseWords.addMessage(ChineseWords);
                } else {
                    welcomeChineseWords.addMessage(EnglishWords);
                }
            }
        });

        if (ipServer.equals(SysConfig.IP_SERVER)) {
            mHandler.sendEmptyMessage(GET_IP);
        } else {
            //加载网络数据
            initLazyData();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_local:
                TVideoApplication.getInstance().setLanguage(Locale.CHINA);
                editor.putString(SharedData.LANG, SharedData.CHINESE);
                editor.commit();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                this.finish();
                //进入主页
                break;
            case R.id.button_English:
                TVideoApplication.getInstance().setLanguage(Locale.ENGLISH);
                editor.putString(SharedData.LANG, SharedData.ENGLISH);
                editor.commit();
                Intent intent1 = new Intent(this, MainActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent1);
                this.finish();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                break;
        }
        return false;
    }

    private int videoPause = 0;

    public class TimeThread extends Thread {
        @Override
        public void run() {
            try {
                do {
                    try {
                        Thread.sleep(5000);
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
                        mHandler.sendEmptyMessageDelayed(SET_TIME, 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (getTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_IP:
                    //获取安装包的IP
                    ArrayList name = new ArrayList();
                    File path = null;
                    if (GetIPUtil.getExtSDCardPath().size() > 0) {
                        path = new File(GetIPUtil.getExtSDCardPath().get(0) + "/IPTV");
                    } else if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        path = new File(Environment.getExternalStorageDirectory().toString() + "/IPTV");
                    }

                    if (path != null) {
                        File[] files = path.listFiles();
                        if (files != null) {
                            if (files.length > 0) {
                                String ip = GetIPUtil.getPathIp(GetIPUtil.namelist(files, name));
                                if (!ip.equals("")) {
                                    ipServer = "http://" + ip;
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(SharedData.IPSERVER, ipServer);
                                    editor.commit();
                                }
                            }
                        }
                    }
                    mHandler.sendEmptyMessage(INITLAZYDATA);
                    break;
                case INITLAZYDATA:
                    initLazyData();
                    break;
                case SET_TIME:
                    if (sysTime.split(" ").length > 1) {
                        if (!(sysTime.split(" ")[1].split(":")[0] + " : " + sysTime.split(" ")[1].split(":")[1]).equals(time)) {
                            time = sysTime.split(" ")[1].split(":")[0] + " : " + sysTime.split(" ")[1].split(":")[1];
                            welcomeTime.setText(time);
                        }
                        if (!(sysTime.split(" ")[0]).equals(date)) {
                            date = sysTime.split(" ")[0];
                            welcomeDate.setText(sysTime.split(" ")[0]);
                        }
                    }
                    if (!isVideo) {
                        showNext(true);
                    }
                    break;
                case AUTOPLAY:
                    isAuthoPlay = true;
                    break;
                case SHOWNEXT:
                    showNext(true);
                    break;
            }
        }
    };

    public void showImageOrVideo(String showType) {
        if ("Image".equals(showType)) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getTime = false;
        if (timeThread != null) {
            timeThread.interrupt();
            timeThread = null;
        }
        if (contentVideoView != null) {
            contentVideoView.Destroy();
            contentVideoView = null;
        }
        mHandler.removeMessages(SET_TIME);
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
                    //TODO 获取语言设置
                    String url = ipServer + "/source/language.json";
                    loginData = Utilss.httpGetChannels(url);
                    if (!"".equals(loginData) && loginData != null) {
                        //TODO 获取欢迎词和广告图片
                        String wordsData = Utilss.httpGetChannels(ipServer + "/source/wlcm/wellcome.json");
                        if (!"".equals(wordsData) && wordsData != null) {
                            JSONObject wordsDataJson = new JSONObject(wordsData);
                            if (Utilss.getJsonDataInt(wordsDataJson, "code") == 0) {
                                JSONObject dataJson = Utilss.getJsonObject(wordsDataJson, "data");
                                if (dataJson != null) {
                                    JSONArray promotionlistJson = Utilss.getJsonArray(dataJson, "promotionlist");
                                    if (promotionlistJson != null) {
                                        JSONObject wordsPic = (JSONObject) promotionlistJson.get(0);
                                        ChineseWords = getJsonDataString(wordsPic, "context");
                                        EnglishWords = getJsonDataString(wordsPic, "context_eng");
                                        bm = httpGetBitmap(ipServer + getJsonDataString(wordsPic, "pic"));
                                    }
                                }
                            } else {
                                ChineseWords = getResources().getString(R.string.welcome_language);
                                EnglishWords = "Welcome TO WELLAV Hotel";
                                bm = null;
                            }
                            if (bm == null) {
                                bm = DiskLruCacheUtil.getInstance(SplashActivity.this).readFromCache();
                            } else {
                                DiskLruCacheUtil.getInstance(SplashActivity.this).writeToCache(bm);
                            }
                            String httpGetChannels = Utilss.httpGetChannels(ipServer + "/source/kjhm/kjhm.json");
                            if (!"".equals(httpGetChannels) && httpGetChannels != null) {
                                JSONObject mainBgDataJson = new JSONObject(httpGetChannels);
                                if (Utilss.getJsonDataInt(mainBgDataJson, "code") == 0) {
                                    JSONObject dataJson = Utilss.getJsonObject(mainBgDataJson, "data");
                                    if (dataJson != null) {
                                        JSONArray mainBgArray = Utilss.getJsonArray(dataJson, "promotionlist");
                                        if (mainBgArray != null) {
                                            if ("image".equals(Utilss.getJsonDataString(mainBgArray.getJSONObject(0), "type"))) {
                                                mainBg = Utilss.httpGetBitmap2(ipServer + Utilss.getJsonDataString(mainBgArray.getJSONObject(0), "pic"));
                                            }
                                            for (int i = 0; i < mainBgArray.length(); i++) {
                                                String Url = Utilss.getJsonDataString(mainBgArray.getJSONObject(i), "pic");
                                                if (Url != null && !"".equals(Url)) {
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
                        }

                        //TODO 获取时间
                        sysTime = GetSystemTime.GetTimeFromServer(ipServer, SysConfig.YEAR_MONTH_DATE);
                        //TODO 判断是否需要升级升级
                        isNeedUpdateApk = IsNeedUpdateApk(ipServer + "/source/apk/appversion.json");
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
                if ("".equals(loginData)) {
                    if (!SplashActivity.this.isFinishing()) {
                        showSetIP();
                    }
                } else {
                    mHandler.sendEmptyMessage(SET_TIME);
                    welcomeChineseWords.addMessage(ChineseWords);
                    TVideoApplication.getInstance().setNeedUpdateApk(isNeedUpdateApk);
                    TVideoApplication.getInstance().setVersion(version);
                    TVideoApplication.getInstance().setVersionCode(versionCode);
                    TVideoApplication.getInstance().setUpdateUrl(updateUrl);
                    TVideoApplication.getInstance().setExitOpt(exitOpt);
                    TVideoApplication.getInstance().setUpdateOpt(updateOpt);
                    if (startOpt == 0) {
                        editor.putBoolean(SharedData.APPAUTOSTART, true);
                    } else {
                        editor.putBoolean(SharedData.APPAUTOSTART, false);
                    }
                    timeThread = new TimeThread();
                    timeThread.start();
                }
                super.onPostExecute(b);
            }
        }.execute();
    }

    public void showNext(boolean toNext) {
        size = types.size();
        if (size == 0) {
            showImageOrVideo("Image");
            num = -1;
            mainBg = Utilss.readBitMap(this, R.mipmap.main_background);
            welcomeImageView.setImageBitmap(mainBg);
        } else if (size > 0) {
            if ("image".equals(types.get(num))) {
                if (contentVideoView != null && contentVideoView.isPlaying()) {
                    contentVideoView.pause();
                }
                isVideo = false;
                showImageOrVideo("Image");
                if (mainBg != null) {
                    welcomeImageView.setImageBitmap(mainBg);
                } else {
                    urls.remove(urls.get(num));
                    types.remove(types.get(num));
                    if (urls.size() > 0) {
                        num = num % (urls.size());
                        if ("image".equals(types.get(num))) {
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
                        welcomeImageView.setImageBitmap(mainBg);
                    }
                }
            } else if ("video".equals(types.get(num))) {
                isVideo = true;
                isAuthoPlay = false;
                showImageOrVideo("Video");
                contentVideoView.setPlayUrl(urls.get(num));
                contentVideoView.play();
                welcomeVideo.setFocusable(false);
            }
        }
    }

    //判断是否需要升级
    private Boolean IsNeedUpdateApk(String url) throws Exception {
        String updateInfo = Utilss.httpGetChannels(url);
        if (!"".equals(updateInfo) && updateInfo != null) {
            JSONObject updateInfoJson = new JSONObject(updateInfo);
            if (Utilss.getJsonDataInt(updateInfoJson, "code") == 0) {
                JSONObject jsonData = Utilss.getJsonObject(updateInfoJson, "data");
                if (jsonData != null) {
                    version = getJsonDataString(jsonData, "version");
                    versionCode = Integer.parseInt(version.split("-")[1]);
                    updateUrl = ipServer + getJsonDataString(jsonData, "url");
                    startOpt = getJsonDataInt(jsonData, "start_opt");
                    exitOpt = getJsonDataInt(jsonData, "exit_opt");
                    updateOpt = getJsonDataInt(jsonData, "update_opt");
                } else {
                    versionCode = Utilss.getVersionCode(this);
                    version = Utilss.getVersion(this);
                    startOpt = 0;
                    exitOpt = 0;
                    updateUrl = "";
                    updateOpt = 0;
                }
            } else {
                versionCode = Utilss.getVersionCode(this);
                version = Utilss.getVersion(this);
                startOpt = 0;
                exitOpt = 0;
                updateUrl = "";
                updateOpt = 0;
            }
        }
        return isNeedUpdate();
    }

    private boolean isNeedUpdate() {
        boolean needUpdate = false;
        if (updateOpt == 0 || updateOpt == 1) {
            if (versionCode > Utilss.getVersionCode(this)) {
                needUpdate = true;
            }
        } else if (updateOpt == 2 && versionCode > sharedPreferences.getInt(SharedData.VERSIONCODE, Utilss.getVersionCode(this))) {
            needUpdate = true;
        }
        return needUpdate;
    }

    @Override
    protected void onStart() {
        if (!NetUtils.isNetworkAvailable(this)) {
            ToastUtil.showText(SplashActivity.this, getResources().getString(R.string.net_work_unsed));
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /*
     * 打开设置IP
     */
    public void showSetIP() {
        new ShowIpInputDialog(this, R.style.dialog, ipServer, new ShowIpInputDialog.OnCloseListener() {
            @Override
            public void onClick(Dialog dialog, boolean confirm, String newIp) {
                if (confirm) {
                    dialog.dismiss();
                    if (!"".equals(newIp) && newIp != null) {
                        ipServer = newIp;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(SharedData.IPSERVER, ipServer);
                        editor.commit();
                        initLazyData();
                    } else {
                        ToastUtil.showText(SplashActivity.this, "IP can't be null");
                        if (!SplashActivity.this.isFinishing()) {
                            showSetIP();
                        }

                    }
                } else {
                    dialog.dismiss();
                    finish();
                }
            }
        }).setFocus(0).setTitle(getResources().getString(R.string.input_ip)).show();
    }


    public static class startAppReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            sharedPreferences = SharedData.getSystemSettingsSharedPreferences(context);
            if (sharedPreferences.getBoolean(SharedData.APPAUTOSTART, false)) {
                Intent i = new Intent(context, SplashActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
    }

    public class videoCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            isVideo = false;
            isAuthoPlay = false;
            num = (num + 1) % urls.size();
            if ("image".equals(types.get(num))) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mainBg = Utilss.httpGetBitmap2(urls.get(num));
                        mHandler.sendEmptyMessage(SET_TIME);
                    }
                }).start();
                mHandler.sendEmptyMessageDelayed(AUTOPLAY, 5000);
            } else if ("video".equals(types.get(num))) {
                showNext(true);
            }
        }
    }
}