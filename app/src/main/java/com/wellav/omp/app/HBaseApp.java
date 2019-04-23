package com.wellav.omp.app;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class HBaseApp extends Application {

    protected static HBaseApp s_instance = null;
    private static String LOGTAG = "HBaseApp";
    Map<String, Object> mGlobalObjs = null;

    public static HBaseApp getInstance() {
        if (s_instance == null) {
            //此处不能打印日志这时会出现栈错误
            Log.e(LOGTAG, "HBaseApp object is null,fetal ERROR!!");
        }
        return s_instance;
    }

    @Override
    public void onCreate() {
        Log.e(LOGTAG, "get instance object now!");
        s_instance = this;
        //CrashHandler crashHandler = CrashHandler.getInstance();
        //crashHandler.init(getApplicationContext());
        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        Log.e(LOGTAG, "onLowMemory,some object or class would be recycled!");
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        Log.e(LOGTAG, "onTerminate,app exit,release instance object!");
        s_instance = null;
        super.onTerminate();
    }

    // 线程同步/定时器部
    Handler mUiHandler = null; // 用于向界面发送执行代
    Handler mWorkHandler = null; // 用于向工作线程发送执行代
    HandlerThread mWorkThread = null; // 工作线程处理耗时操作,防止在主线程中执行界面卡

    /**
     * 获取Handler化跨线程执行代码,实现同步/定时器等功能
     * 1.获取指定线程Handler 2.实现Runnable:run()代码
     * 2.调用Handler:post/postAt postDelayed传入Runnable 获取界面Handler
     */
    public Handler getUiHandler() {
        return (mUiHandler != null) ? (mUiHandler) : (mUiHandler = new Handler(
                getMainLooper()));
    }

    /**
     * 获取工作线程Handler
     * @return
     */
    public Handler getWorkHandler() {
        return (mWorkHandler != null) ? (mWorkHandler)
                : (mWorkHandler = new Handler(getWorkLooper()));
    }

    public Looper getWorkLooper() {
        if (mWorkThread == null) {
            mWorkThread = new HandlerThread("Rtcclient_WorkThread");
            mWorkThread.start();
        }
        return mWorkThread.getLooper();
    }

    /**
     * 释放工作线程
     */
    public void clearWorkThread() {
        if (mWorkHandler != null)
            mWorkHandler = null;
        if (mWorkThread != null) {
            if (mWorkThread.isAlive()) {
                mWorkThread.quit();
                try {
                    mWorkThread.join(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mWorkThread = null;
        }
    }

    /**
     * 创建Handler实现同步和定时器 直接向ui线程执行代码
     */
    static public boolean post2UIRunnable(Runnable r) {
        return (s_instance != null) ? s_instance.getUiHandler().post(r) : false;
    }

    /**
     * UI线程中的定时器
     * @param r
     * @param uptimeMillis
     * @return
     */
    static public boolean post2UIAtTime(Runnable r, long uptimeMillis) {
        return (s_instance != null) ? s_instance.getUiHandler().postAtTime(r,
                uptimeMillis) : false;
    }

    static public boolean post2UIDelayed(Runnable r, long delayMillis) {
        return (s_instance != null) ? s_instance.getUiHandler().postDelayed(r,
                delayMillis) : false;
    }

    /**
     * 直接向工作线程线程执行
     * @param r
     * @return
     */
    static public boolean post2WorkRunnable(Runnable r) {
        return (s_instance != null) ? s_instance.getWorkHandler().post(r)
                : false;
    }

    /**
     * 工作线程中的定时器
     * @param r
     * @param uptimeMillis
     * @return
     */
    static public boolean post2WorkAtTime(Runnable r, long uptimeMillis) {
        return (s_instance != null) ? s_instance.getWorkHandler().postAtTime(r,
                uptimeMillis) : false;
    }

    static public boolean post2WorkDelayed(Runnable r, long delayMillis) {
        return (s_instance != null) ? s_instance.getWorkHandler().postDelayed(
                r, delayMillis) : false;
    }

    /**
     * 数据对象存储(全局) 通过名字存储单例对象 sKey建议直接使用类名
     */

    Map<String, Object> getGlobalObjs() {
        return (mGlobalObjs != null) ? (mGlobalObjs)
                : (mGlobalObjs = new HashMap<String, Object>());
    }

    public Object addGlobalObjs(String sKey, Object obj) {
        return getGlobalObjs().put(sKey, obj);
    }

    public Object findGlobalObjs(String sKey) {
        return (mGlobalObjs != null) ? mGlobalObjs.get(sKey) : null;
    }

    public Object removeGlobalObjs(String sKey) {
        return (mGlobalObjs != null) ? mGlobalObjs.remove(sKey) : null;
    }

    @SuppressWarnings("unchecked")
    static public <T> T setGlobalObjs(String sKey, T newObj) {
        return (T) ((s_instance != null) ? s_instance.addGlobalObjs(sKey,
                newObj) : null);
    }

    /**
     * 获取已保存在app中的全局对象
     * @param <T>
     * @param sKey
     * @return
     */
    @SuppressWarnings("unchecked")
    static public <T> T getGlobalObjs(String sKey) {
        return (T) ((s_instance != null) ? s_instance.findGlobalObjs(sKey)
                : null);
    }

    /**
     * 删除指定全局对象
     * @param sKey
     */
    static public void unsetGlobalObjs(String sKey) {
        if (s_instance != null)
            s_instance.removeGlobalObjs(sKey);
    }

}
