package com.wellav.omp.iptv;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.wellav.omp.R;
import com.wellav.omp.been.ContentVideoView;
import com.wellav.omp.been.Info;
import com.wellav.omp.channel.SysConfig;
import com.wellav.omp.ui.FullScreenView;
import com.wellav.omp.ui.ShowBottomMenu;
import com.wellav.omp.utils.GetSystemTime;
import com.wellav.omp.utils.SharedData;
import com.wellav.omp.utils.Utilss;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HotelActivity extends Activity {
    private final static int SHOW_TIME = 0;
    private final static int SHOW_IMAGE = 1;
    private final static int SHOW_VIDEO = 2;
    private final static int CHANGEAUTOSHOWIMAGE = 3;

    private String time;
    private String date;
    private String week;
    private ImageView imageView;
    private TextView mainTime;
    private TextView mainDate;
    private TextView mainWeek;
    private TextView picCount;
    private String ipServer;
    private int signatureNum = 0;
    private Thread thread;

    private boolean isAutoShowImage;

    private String[] urls = {
            "/source/jdjs/jdjs.json",
            "/source/ppls/ppls.json",
            "/source/kfjs/kfjs.json",
            "/source/ssjs/ssjs.json",
            "/source/swfw/swfw.json",
            "/source/mwjy/mwjy.json",
            "/source/ddts/ddts.json"
    };
    private Info infos;
    private OkHttpClient client = new OkHttpClient.Builder().readTimeout(3, TimeUnit.SECONDS).build();

    private int infoNum = 0;
    private int picNum = 0;
    private String sysTime;
    private static SharedPreferences sharedPreferences;

    private ShowBottomMenu showBottomMenu;
    private RelativeLayout relativeLayoutHotel;
    private boolean isShowBottom = false;

    private View relativeLayoutHotelVideo;
    private View relativeLayoutHotelImage;
    private FullScreenView hotelVideo;
    private ContentVideoView contentVideoView;
    private boolean isVideo;
    private TextView textTitle;
    private TextView textBody;
    private String title;
    private String url;
    private int size;
    private String lang;
    private int videoNum = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBX_8888);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_hotel);

        //获取缓存
        sharedPreferences = SharedData.getSystemSettingsSharedPreferences(this);
        ipServer = sharedPreferences.getString(SharedData.IPSERVER, SysConfig.IP_SERVER);
        lang = sharedPreferences.getString(SharedData.LANG, SharedData.CHINESE);
        isAutoShowImage = true;

        //初始化插件
        mainTime = findViewById(R.id.main_time);
        mainDate = findViewById(R.id.main_date);
        mainWeek = findViewById(R.id.main_week);
        picCount = findViewById(R.id.count);
        relativeLayoutHotelImage = findViewById(R.id.hotel_Image);
        relativeLayoutHotelVideo = findViewById(R.id.hotel_Video);
        relativeLayoutHotel = findViewById(R.id.relativeLayout_Hotel);
        hotelVideo = findViewById(R.id.hotel_video);
        textTitle = findViewById(R.id.text_title);
        textBody = findViewById(R.id.text_body);

        imageView = findViewById(R.id.image);

        title = getIntent().getStringExtra("title");
        if (SharedData.HOTELINTRODUCTION.equals(title)) {
            url = urls[0];
            textTitle.setText(getResources().getString(R.string.hotel_introduction));
        } else if (SharedData.BRANDHISTORY.equals(title)) {
            url = urls[1];
            textTitle.setText(getResources().getString(R.string.brand_history));
        } else if (SharedData.ROOMINTRODUCTION.equals(title)) {
            url = urls[2];
            textTitle.setText(getResources().getString(R.string.room_introduction));
        } else if (SharedData.FACILITYINTRODUCTION.equals(title)) {
            url = urls[3];
            textTitle.setText(getResources().getString(R.string.facility_introduction));
        } else if (SharedData.BUSINESSSERVICES.equals(title)) {
            url = urls[4];
            textTitle.setText(getResources().getString(R.string.business_service));
        } else if (SharedData.HOTELFOOD.equals(title)) {
            url = urls[5];
            textTitle.setText(getResources().getString(R.string.hotel_food));
        } else if (SharedData.lOCALFEATURES.equals(title)) {
            url = urls[6];
            textTitle.setText(getResources().getString(R.string.local_features));
        }

        contentVideoView = new ContentVideoView(getApplicationContext(), hotelVideo, new ContentVideoView.OnCompleteListener() {
            @Override
            public void OnComplete() {
                isAutoShowImage = false;
                showNext(true);
            }
        });
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                        sysTime = GetSystemTime.GetTimeFromServer(ipServer, SysConfig.YEAR_MONTH_DATE);
                        mHandler.sendEmptyMessage(SHOW_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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
                    sysTime = GetSystemTime.GetTimeFromServer(ipServer, SysConfig.YEAR_MONTH_DATE);
                    Request request = new Request.Builder().url(ipServer + url).get().build();
                    Call call = client.newCall(request);
                    Response response = call.execute();
                    infos = new Info(response.body().string());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean b) {
                super.onPostExecute(b);
                if (b) {
                    if (sysTime.split(" ").length > 1) {
                        mainTime.setText(sysTime.split(" ")[1].split(":")[0] + " : " + sysTime.split(" ")[1].split(":")[1]);
                        mainDate.setText(sysTime.split(" ")[0]);
                        if (sysTime.split(" ").length == 3) {
                            mainWeek.setText(Utilss.getWeek(getApplicationContext(), sysTime.split(" ")[2]));
                        }
                    }
                    infoNum = 0;
                    picNum = 0;
                    size = infos.getPromotionList().size();
                    if (size == 0) {
                        relativeLayoutHotelImage.setVisibility(View.VISIBLE);
                        picCount.setText("0/0");
                        Glide.with(HotelActivity.this)
                                .load(R.mipmap.background2)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(imageView);
                    } else {
                        if ("image".equals(infos.getPromotionList().get(0).getType())) {
                            isVideo = false;
                            mHandler.sendEmptyMessage(SHOW_IMAGE);
                        } else if ("video".equals(infos.getPromotionList().get(0).getType())) {
                            isVideo = true;
                            mHandler.sendEmptyMessage(SHOW_VIDEO);
                        }
                    }
                    mHandler.sendEmptyMessage(SHOW_TIME);
                    thread.start();
                }
            }
        }.execute();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (isShowBottom) {
                    showBottomMenu.moveToLeft();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (isShowBottom) {
                    showBottomMenu.moveToRight();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (!isShowBottom) {
                    isAutoShowImage = false;
                    showNext(false);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (!isShowBottom) {
                    isAutoShowImage = false;
                    showNext(true);
                }
                break;
            case KeyEvent.KEYCODE_BACK:
                if (isShowBottom) {
                    isShowBottom = false;
                    showBottomMenu.hideBottom();
                } else {
                    finish();
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                isShowBottom = true;
                if (showBottomMenu == null) {
                    showBottomMenu = new ShowBottomMenu(HotelActivity.this, relativeLayoutHotel, false);
                    showBottomMenu.addView();
                    showBottomMenu.showBottom();
                } else {
                    showBottomMenu.showBottom();
                }
                break;
            default:
                break;
        }
        return false;
    }

    private boolean isFirstShow = true;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_TIME:
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
                    if (isAutoShowImage && !isVideo && !isFirstShow) {
                        showNext(true);
                    }
                    isFirstShow = false;
                    break;
                case SHOW_IMAGE:
                    relativeLayoutHotelVideo.setVisibility(View.GONE);
                    relativeLayoutHotelImage.setVisibility(View.VISIBLE);
                    Glide.with(HotelActivity.this)
                            .load(ipServer + infos.getPromotionList().get(0).getPic())
                            .error(R.mipmap.background2)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(imageView);
                    picCount.setText((size == 0) ? "0/0" : "1/" + size);
                    if (lang.equals(SharedData.CHINESE)) {
                        textBody.setText(infos.getPromotionList().get(0).getContext().replaceAll(" ", ""));
                    } else {
//                        textBody.setTextSize(getResources().getDimension(R.dimen.S20));
                        textBody.setText(infos.getPromotionList().get(0).getContext_eng());
                    }
                    break;
                case SHOW_VIDEO:
                    relativeLayoutHotelImage.setVisibility(View.GONE);
                    relativeLayoutHotelVideo.setVisibility(View.VISIBLE);
                    contentVideoView.setPlayUrl(ipServer + infos.getPromotionList().get(0).getPic());
                    contentVideoView.play();
                    hotelVideo.setFocusable(false);
                    picCount.setText((size == 0) ? "0/0" : "1/" + size);
                    if (lang.equals(SharedData.CHINESE)) {
                        textBody.setText(infos.getPromotionList().get(0).getContext().replaceAll(" ", ""));
                    } else {
                        textBody.setText(infos.getPromotionList().get(0).getContext_eng());
                    }
                    break;
                case CHANGEAUTOSHOWIMAGE:
                    isAutoShowImage = true;
                    break;
                default:
                    break;
            }
        }
    };

    public void showNext(boolean toNext) {
        int size = infos.getPromotionList().size();
        if (isDestroyed()) {
            thread.interrupt();
        } else if (size == 0) {
            showImageOrVideo("Image");
            picNum = -1;
            picCount.setText("0/0");
            Glide.with(this)
                    .load(R.mipmap.background2)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .signature(new StringSignature("" + signatureNum++))
                    .into(imageView);
        } else if (size > 0) {
            picNum = toNext ? (picNum + 1) % size : (picNum - 1 + size) % size;
            if ("image".equals(infos.getPromotionList().get(picNum).getType())) {
                if (contentVideoView != null && contentVideoView.isPlaying()) {
                    contentVideoView.pause();
                }
                isVideo = false;
                showImageOrVideo("Image");
                Glide.with(this)
                        .load(ipServer + infos.getPromotionList().get(picNum).getPic())
                        .error(R.mipmap.background2)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .signature(new StringSignature("" + signatureNum++))
                        .into(imageView);
                picCount.setText(picNum + 1 + "/" + size);
                mHandler.removeMessages(CHANGEAUTOSHOWIMAGE);
                mHandler.sendEmptyMessageDelayed(CHANGEAUTOSHOWIMAGE, 4000);
                if ("image".equals(infos.getPromotionList().get((picNum + 1) % size).getType())) {
                    preload();
                }
            } else if ("video".equals(infos.getPromotionList().get(picNum).getType())) {
                isVideo = true;
                showImageOrVideo("Video");
                contentVideoView.setPlayUrl(ipServer + infos.getPromotionList().get(picNum).getPic());
                contentVideoView.play();
                picCount.setText(picNum + 1 + "/" + size);
                hotelVideo.setFocusable(false);
                if ("image".equals(infos.getPromotionList().get((picNum + 1) % size).getType())) {
                    preload();
                }
            }
            if (lang.equals(SharedData.CHINESE)) {
                textBody.setText(infos.getPromotionList().get(picNum).getContext().replaceAll(" ", ""));
            } else {
                textBody.setText(infos.getPromotionList().get(picNum).getContext_eng());
            }
        }
    }

    public void showImageOrVideo(String showType) {
        if ("Image".equals(showType)) {
            if (relativeLayoutHotelVideo.getVisibility() == View.VISIBLE) {
                relativeLayoutHotelVideo.setVisibility(View.GONE);
            }
            relativeLayoutHotelImage.setVisibility(View.VISIBLE);
        } else if ("Video".equals(showType)) {
            if (relativeLayoutHotelImage.getVisibility() == View.VISIBLE) {
                relativeLayoutHotelImage.setVisibility(View.GONE);
            }
            relativeLayoutHotelVideo.setVisibility(View.VISIBLE);
        }
    }

    public void showInfo(boolean up) {
        infoNum = up ? (infoNum + 4) % 5 : (infoNum + 1) % 5;
        picNum = 0;
        if (infoNum != 1 && infoNum != 4 && infoNum != 0) {
            int size = infos.getPromotionList().size();
            if (isDestroyed()) {
                thread.interrupt();
            } else if (size == 0) {
                picNum = -1;
                picCount.setText("0/0");
                Glide.with(this)
                        .load(R.mipmap.background2)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .signature(new StringSignature("" + signatureNum++))
                        .into(imageView);
            } else if (size > 0) {
                Glide.with(this)
                        .load(ipServer + infos.getPromotionList().get(picNum).getPic())
                        .error(R.mipmap.background2)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .signature(new StringSignature("" + signatureNum++))
                        .into(imageView);
                picCount.setText(picNum + 1 + "/" + size);
                preload();
            }
        } else {
            mHandler.sendEmptyMessage(2);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.get(this).clearMemory();
        if (contentVideoView != null) {
            contentVideoView.Destroy();
            contentVideoView = null;
        }
    }

    public void preload() {
        int size = infos.getPromotionList().size();
        if ("image".equals(infos.getPromotionList().get((picNum + 1) % size).getType())) {
            Glide.with(this)
                    .load(ipServer + infos.getPromotionList().get((picNum + 1) % size).getPic())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .preload();
        }
    }

    public class videoCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            isAutoShowImage = false;
            showNext(true);
        }
    }
}
