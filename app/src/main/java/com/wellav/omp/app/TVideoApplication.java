package com.wellav.omp.app;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.hardware.display.DisplayManager;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Display;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.Locale;

/**
 * Created by JingWen.Li on 2017/11/22.
 */

public class TVideoApplication extends HBaseApp {
    public static Typeface TypeFaceYaHei;
    static TVideoApplication s_instance;
    //保存图片地址
    public static String strFolder = Environment.getExternalStorageDirectory() + "/TVideo/";

    @Override
    public void onCreate() {
        super.onCreate();
        s_instance = this;
        setTypeFace();
    }

    //设置全局字体
    public void setTypeFace() {
        TypeFaceYaHei = Typeface.createFromAsset(getAssets(), "fonts/heiti.ttf");
        try {
            Field field = Typeface.class.getDeclaredField("MONOSPACE");
            field.setAccessible(true);
            field.set(null, TypeFaceYaHei);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * @return
     */
    public static TVideoApplication getInstance() {
        return s_instance;
    }

    public void saveBitmap(String name, Bitmap bitmap) {
        File file = new File(strFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
        String fileName = name + ".jpg";
        file = new File(file, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取设备分辨率
    public static Point getPhysicalScreenSize(Context context) {
        Point p = new Point();
        DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = dm.getDisplays();
        int maxWidth = -1, maxHeight = -1;
        DisplayMetrics met = new DisplayMetrics();
        for (Display d : displays) {
            d.getMetrics(met);
            maxWidth = met.widthPixels > maxWidth ? met.widthPixels : maxWidth;
            maxHeight = met.heightPixels > maxHeight ? met.heightPixels : maxHeight;
        }
        p.x = maxWidth;
        p.y = maxHeight;
        return p;
    }

    public void setLanguage(Locale locale) {
        if (locale == null) {
            return;
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        config.locale = locale;
        getApplicationContext().getResources().updateConfiguration(config, dm);
    }


    //保存全局一些变量配置
    private int versionCode = 0;
    private String version = "1.0.0";
    private String updateUrl = "";
    private Boolean isNeedUpdateApk = false;
    private int updateOpt;
    private int exitOpt;


    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public Boolean getNeedUpdateApk() {
        return isNeedUpdateApk;
    }

    public void setNeedUpdateApk(Boolean needUpdateApk) {
        isNeedUpdateApk = needUpdateApk;
    }

    public int getUpdateOpt() {
        return updateOpt;
    }

    public void setUpdateOpt(int updateOpt) {
        this.updateOpt = updateOpt;
    }

    public int getExitOpt() {
        return exitOpt;
    }

    public void setExitOpt(int exitOpt) {
        this.exitOpt = exitOpt;
    }
}
