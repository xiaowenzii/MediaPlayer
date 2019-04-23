package com.wellav.omp.been;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceHolder;

import com.wellav.omp.ui.FullScreenView;
import com.wellav.omp.utils.Utilss;

/**
 * Created by bingjia.zheng on 2018/8/6.
 */

public class ContentVideoView implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnSeekCompleteListener {

    public FullScreenView fullScreenView;
    public boolean isPlay;
    private SurfaceHolder surfaceHolder;
    private String path;
    private int currentPosition;
    private Context context;
    private OnCompleteListener listener;


    public ContentVideoView(Context context, FullScreenView fullScreenView, OnCompleteListener listener) {
        this.context = context;
        this.fullScreenView = fullScreenView;
        this.listener = listener;
        surfaceHolder = fullScreenView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setFormat(PixelFormat.RGBX_8888);
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        surfaceHolder.addCallback(new SurfaceHolderCallback());
    }

    /**
     * 播放视频
     */
    public void play() {
        try {
            fullScreenView.setVideoURI(Uri.parse(path));
            fullScreenView.setOnPreparedListener(this);
            fullScreenView.setOnInfoListener(this);
            fullScreenView.setOnCompletionListener(this);
            fullScreenView.setOnErrorListener(this);
        } catch (Exception e) {
        }
    }

    public void setPlayUrl(String path) {
        this.path = path;
    }

    public boolean isPlaying() {
        if (fullScreenView.isPlaying()) {
            return true;
        } else {
            return false;
        }
    }

    public void Destroy() {
        if (fullScreenView != null && fullScreenView.isPlaying()) {
            fullScreenView.stopPlayback();
            fullScreenView = null;
        }
    }

    public void pause() {
        fullScreenView.pause();
    }

    public void restart(int currentPosition) {
        fullScreenView.seekTo(currentPosition);
    }

    public void stopPlayback() {
        fullScreenView.stopPlayback();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        listener.OnComplete();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int arg1, int extra) {
        switch (arg1) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (isPlay) {
                    fullScreenView.pause();
                    isPlay = false;
                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (!isPlay) {
                    fullScreenView.start();
                    isPlay = true;
                }
                break;
        }
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        fullScreenView.start();
        isPlay = true;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
    }

    class SurfaceHolderCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            play();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }

    public interface OnCompleteListener {
        void OnComplete();
    }
}
