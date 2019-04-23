package com.wellav.omp.iptv;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wellav.omp.R;
import com.wellav.omp.adapter.ChannelAdapter;
import com.wellav.omp.channel.Channel;
import com.wellav.omp.channel.Channels;
import com.wellav.omp.channel.SysConfig;
import com.wellav.omp.ui.FullScreenView;
import com.wellav.omp.ui.ShowBottomMenu;
import com.wellav.omp.ui.ShowConfirmDialog;
import com.wellav.omp.utils.GetSystemTime;
import com.wellav.omp.utils.SharedData;
import com.wellav.omp.utils.ToastUtil;
import com.wellav.omp.utils.Utilss;

import java.util.List;

public class LivePlayerActivity extends BaseActivity implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener, MediaPlayer.OnErrorListener {

    //节目显示列表
    private boolean isShowChanelRelativeLayout;
    private RelativeLayout showChannelLayout;
    private ListView channelListView;
    private ChannelAdapter mChannelAdapter;
    private RelativeLayout tvLiveLayout;

    //节目列表
    private Channels mChannels;
    private List<Channel> contentList;
    private int mChannelsSize;

    //当前节目名字
    private Channel mCurrentChannel;
    private int currentPos;
    //预选择的节目
    private int channelListSelected;
    //服务器
    private String ipServer;

    private boolean isShowChannalLayout;
    private RelativeLayout channalLayout;
    private TextView liveTopChannelNum;
    private TextView liveBottomNum;
    private TextView liveBottomName;

    //播放器
    private FullScreenView videoView;
    private SurfaceHolder surfaceHolder;
    private boolean isPlay;

    //监听遥控数字
    private RelativeLayout inputChannelNumLayout;
    private TextView inputChannelNum;
    private boolean isControlChannel;
    private String inputChannel;

    // 主线程handler
    private Handler handler = null;
    private String sysTime;
    private static SharedPreferences sharedPreferences;
    private int isPlayNum = 10;
    private TimeThread timeThread;
    private boolean isShowBottom = false;
    private ShowBottomMenu showBottomMenu;
    private RelativeLayout activityMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBX_8888);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_live_player);
        //获取节目列表
        getChannelData();
        //初始化插件
        initView();
    }

    private void getChannelData() {
        //获取缓存
        sharedPreferences = SharedData.getSystemSettingsSharedPreferences(this);
        ipServer = sharedPreferences.getString(SharedData.IPSERVER, SysConfig.IP_SERVER);
        mChannels = (Channels) getIntent().getSerializableExtra("channels");
        contentList = mChannels.getContentList();
        mChannelsSize = contentList.size();
        if (mChannelsSize == 0) {
            Toast.makeText(this, getResources().getString(R.string.no_channel), Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        //插件初始化
        videoView = findViewById(R.id.surface_view);
        showChannelLayout = findViewById(R.id.is_show_channel_layout);
        channelListView = findViewById(R.id.channel_list);
        tvLiveLayout = findViewById(R.id.tv_live_layout);

        channalLayout = findViewById(R.id.channal_layout);
        liveTopChannelNum = findViewById(R.id.live_top_channel_num);
        liveBottomNum = findViewById(R.id.live_bottom_num);
        liveBottomName = findViewById(R.id.live_bottom_name);

        //输入数字
        inputChannelNumLayout = findViewById(R.id.input_channel_num_layout);
        inputChannelNum = findViewById(R.id.input_channel_num);
        initInputChannelNum();

        activityMain = findViewById(R.id.activity_main);

        handler = new Handler();
        timeThread = new TimeThread();
        timeThread.start();
        showChannelLayout.setVisibility(View.GONE);
        channalLayout.setVisibility(View.GONE);
        isShowChanelRelativeLayout = false;
        isShowChannalLayout = false;

        //直播节目表头高度为屏幕高度的1/10，整个节目列表为屏幕高度的3/4
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        int screenHeight = wm.getDefaultDisplay().getHeight();

        //设置 节目表头 高度
        ViewGroup.LayoutParams pp = tvLiveLayout.getLayoutParams();
        pp.height = screenHeight / 10;
        tvLiveLayout.setLayoutParams(pp);

        //计算获得ListView每条项目的高度
        int i = screenHeight / 40 * 26 / 9;
        mChannelAdapter = new ChannelAdapter(this, i * 9, contentList);
        channelListView.setAdapter(mChannelAdapter);

        //整个节目列表高度 = 节目表头高度 + 节目列表高度
        ViewGroup.LayoutParams ppp = showChannelLayout.getLayoutParams();
        ppp.height = screenHeight / 10 + i * 9;
        showChannelLayout.setLayoutParams(ppp);
        
        surfaceHolder = videoView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(new SurfaceHolderCallback());
    }

    private void play() {
        try {
            videoView.setVideoURI(Uri.parse(mCurrentChannel.getPlayUrl()));
            videoView.setOnPreparedListener(this);
            videoView.setOnCompletionListener(this);
            videoView.setOnErrorListener(this);
            videoView.setOnInfoListener(this);
        } catch (Exception e) {
            Utilss.PrintLog("Exception", "PlayActivity " + e.toString());
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        videoView.start();
        isPlay = true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        setCurrentChannel();
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (isPlay) {
                    videoView.pause();
                    isPlay = false;
                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (!isPlay) {
                    videoView.start();
                    isPlay = true;
                    isPlayNum = 10;
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // 处理播放过程中播放出错问题
        play();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPlay) {
            videoView.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
            videoView = null;
        }
        isPlay = false;
        if (timeThread != null) {
            timeThread.interrupt();
            timeThread = null;
        }
        System.gc();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (isShowBottom) {
                    showBottomMenu.moveToRight();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (isShowBottom) {
                    showBottomMenu.moveToLeft();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (!isShowBottom) {
                    if (isControlChannel) {
                        int inNum = Integer.parseInt(inputChannel) - 1;
                        if (inNum < mChannelsSize) {
                            currentPos = inNum;
                            initInputChannelNum();
                            showCurrentChannel(true);
                            setCurrentChannel();
                        } else {
                            initInputChannelNum();
                            ToastUtil.showText(this, "非法频道");
                        }
                    } else {
                        initInputChannelNum();
                        if (isShowChanelRelativeLayout) {
                            if (channelListSelected != currentPos) {
                                currentPos = channelListSelected;
                                setCurrentChannel();
                            }
                            showCurrentChannel(true);
                            showChannelList(false);
                        } else {
                            if (isShowChannalLayout) {
                                showCurrentChannel(false);
                            }
                            showChannelList(true);
                        }
                    }
                } else {
                    return super.onKeyDown(KeyEvent.KEYCODE_ENTER, event);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_CHANNEL_UP:
                if (!isShowBottom) {
                    initInputChannelNum();
                    if (isShowChanelRelativeLayout) {
                        if (channelListSelected < mChannelsSize - 1) {
                            channelListSelected = channelListSelected + 1;
                        } else {
                            channelListSelected = 0;
                        }
                        setChannelListSelected(true);
                        showChannelList(true);
                    } else {
                        if (currentPos < mChannelsSize - 1) {
                            currentPos = currentPos + 1;
                        } else {
                            currentPos = 0;
                        }
                        showCurrentChannel(true);
                        liveTopChannelNum.setText(timeLength((currentPos + 1) + ""));
                        liveBottomNum.setText((currentPos + 1) + "");
                        liveBottomName.setText(contentList.get(currentPos).getName());
                        //TODO 延迟500ms响应切换节目
                        disHandler.removeMessages(4);
                        disHandler.sendEmptyMessageDelayed(4, 500);
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_CHANNEL_DOWN:
                if (!isShowBottom) {
                    initInputChannelNum();
                    if (isShowChanelRelativeLayout) {
                        if (channelListSelected > 0) {
                            channelListSelected = channelListSelected - 1;
                        } else {
                            channelListSelected = mChannelsSize - 1;
                        }
                        setChannelListSelected(true);
                        showChannelList(true);
                    } else {
                        if (currentPos > 0) {
                            currentPos = currentPos - 1;
                        } else {
                            currentPos = mChannelsSize - 1;
                        }
                        showCurrentChannel(true);
                        liveTopChannelNum.setText(timeLength((currentPos + 1) + ""));
                        liveBottomNum.setText((currentPos + 1) + "");
                        liveBottomName.setText(contentList.get(currentPos).getName());

                        disHandler.removeMessages(4);
                        disHandler.sendEmptyMessageDelayed(4, 500);
                    }
                }
                break;
            case KeyEvent.KEYCODE_0:
                setInputChannel(0);
                break;
            case KeyEvent.KEYCODE_1:
                setInputChannel(1);
                break;
            case KeyEvent.KEYCODE_2:
                setInputChannel(2);
                break;
            case KeyEvent.KEYCODE_3:
                setInputChannel(3);
                break;
            case KeyEvent.KEYCODE_4:
                setInputChannel(4);
                break;
            case KeyEvent.KEYCODE_5:
                setInputChannel(5);
                break;
            case KeyEvent.KEYCODE_6:
                setInputChannel(6);
                break;
            case KeyEvent.KEYCODE_7:
                setInputChannel(7);
                break;
            case KeyEvent.KEYCODE_8:
                setInputChannel(8);
                break;
            case KeyEvent.KEYCODE_9:
                setInputChannel(9);
                break;
            case KeyEvent.KEYCODE_BACK:
                if (isShowBottom) {
                    isShowBottom = false;
                    showBottomMenu.hideBottom();
                } else {
                    initInputChannelNum();
                    if (isShowChanelRelativeLayout || isShowChannalLayout) {
                        if (isShowChanelRelativeLayout) {
                            showChannelList(false);
                            channelListSelected = currentPos;
                            setChannelListSelected(false);
                        } else {
                            showCurrentChannel(false);
                        }
                    } else {
                        showIsExit();
                    }
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                isShowBottom = true;
                if (showBottomMenu == null) {
                    showBottomMenu = new ShowBottomMenu(LivePlayerActivity.this, activityMain, false);
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

    private Handler disHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                showChannelLayout.setVisibility(View.GONE);
                isShowChanelRelativeLayout = false;
            } else if (msg.what == 1) {
                channalLayout.setVisibility(View.GONE);
                isShowChannalLayout = false;
            } else if (msg.what == 2) {
                channelListSelected = currentPos;
                setChannelListSelected(false);
            } else if (msg.what == 3) {
                initInputChannelNum();
            } else if (msg.what == 4) {
                setCurrentChannel();
            }
        }
    };

    /**
     * 设置当前的list列表选择
     */
    private void setChannelListSelected(boolean isNeedDelay) {
        int topChannel;
        if (channelListSelected > 3) {
            topChannel = channelListSelected - 4;
        } else {
            topChannel = 0;
        }
        mChannelAdapter.setSelectedPosition(channelListSelected);
        mChannelAdapter.notifyDataSetInvalidated();
        channelListView.setSelection(topChannel);
        channelListView.smoothScrollToPosition(channelListSelected);
        disHandler.removeMessages(2);
        if (isNeedDelay) {
            disHandler.sendEmptyMessageDelayed(2, 5000);
        }
    }

    /**
     * 设置当前的节目
     */
    private void setCurrentChannel() {
        if (videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        int topChannel;
        if (currentPos > 3) {
            topChannel = currentPos - 4;
        } else {
            topChannel = 0;
        }
        channelListSelected = currentPos;
        mChannelAdapter.setSelectedPosition(currentPos);
        mChannelAdapter.notifyDataSetInvalidated();
        channelListView.setSelection(topChannel);
        channelListView.smoothScrollToPosition(currentPos);

        liveTopChannelNum.setText(timeLength((currentPos + 1) + ""));
        liveBottomNum.setText((currentPos + 1) + "");
        liveBottomName.setText(contentList.get(currentPos).getName());
        mCurrentChannel = contentList.get(currentPos);
        play();
    }

    /**
     * 显示菜单列表
     */
    private void showChannelList(boolean isShowOrHide) {
        disHandler.removeMessages(0);
        if (isShowOrHide) {
            showChannelLayout.setVisibility(View.VISIBLE);
            isShowChanelRelativeLayout = true;
            disHandler.sendEmptyMessageDelayed(0, 5000);
        } else {
            showChannelLayout.setVisibility(View.GONE);
            isShowChanelRelativeLayout = false;
        }
    }

    /**
     * 显示当前频道
     */
    private void showCurrentChannel(boolean isShowOrHide) {
        disHandler.removeMessages(1);
        if (isShowOrHide) {
            channalLayout.setVisibility(View.VISIBLE);
            isShowChannalLayout = true;
            disHandler.sendEmptyMessageDelayed(1, 5000);
        } else {
            channalLayout.setVisibility(View.GONE);
            isShowChannalLayout = false;
        }
    }

    //设置输入的节目数字
    private void setInputChannel(int num) {
        //其它显示隐藏
        channalLayout.setVisibility(View.GONE);
        isShowChannalLayout = false;
        showChannelLayout.setVisibility(View.GONE);
        isShowChanelRelativeLayout = false;

        isControlChannel = true;
        if (inputChannel.length() < 3) {
            inputChannel = inputChannel + num;
            inputChannelNumLayout.setVisibility(View.VISIBLE);
            inputChannelNum.setText(inputChannel);
            isControlChannel = true;
            disHandler.removeMessages(3);
            disHandler.sendEmptyMessageDelayed(3, 5000);
        }
    }

    //初始化输入的节目数字
    private void initInputChannelNum() {
        disHandler.removeMessages(3);
        inputChannelNumLayout.setVisibility(View.GONE);
        inputChannel = "";
        isControlChannel = false;
    }

    class SurfaceHolderCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            //设置初始节目
            currentPos = sharedPreferences.getInt(SharedData.CURRENTCHANNELPOS, 0);
            channelListSelected = currentPos;
            if (currentPos > contentList.size() - 1) {
                currentPos = 0;
            }
            setCurrentChannel();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }

    public class TimeThread extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    if (!isPlay) {
                        if (isPlayNum == 0) {
                            disHandler.removeMessages(4);
                            disHandler.sendEmptyMessage(4);
                            isPlayNum = 10;
                        }
                        isPlayNum--;
                    }
                    sysTime = GetSystemTime.GetTimeFromServer(ipServer, SysConfig.HOUR_MINUNTE);
                    Thread.sleep(1000);
//                    timeHandler.sendEmptyMessage(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String timeLength(String time) {
        String reTime = time;
        if (reTime.length() == 1) {
            reTime = "00" + reTime;
        } else if (reTime.length() == 2) {
            reTime = "0" + reTime;
        }
        return reTime;
    }

    private void showIsExit() {
        new ShowConfirmDialog(this, R.style.dialog, getResources().getString(R.string.exit_play), new ShowConfirmDialog.OnCloseListener() {
            @Override
            public void onClick(Dialog dialog, boolean confirm) {
                if (confirm) {
                    dialog.dismiss();
                    if (videoView != null) {
                        videoView.stopPlayback();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(SharedData.CURRENTCHANNELPOS, currentPos);
                        editor.commit();
                    }
                    finish();
                } else {
                    dialog.dismiss();
                }
            }
        }).setTitle(getResources().getString(R.string.prompt)).show();
    }
}
