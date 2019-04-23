package com.wellav.omp.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by JingWen.Li on 2018/1/9.
 */

public class DiskLruCacheUtil {
    public static final String DiskLruCachePath = "advertismentPaicture";
    public DiskLruCache mDiskLruCache = null;
    static DiskLruCacheUtil diskLruCacheHelper;

    private DiskLruCacheUtil(Context context) {
        try {
            File cacheDir = getDiskCacheDir(context, "bitmap");
            //如果文件不存在，则创建
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            mDiskLruCache = DiskLruCache.open(cacheDir, getAppVersion(context), 1, 10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DiskLruCacheUtil getInstance(Context context) {
        if (diskLruCacheHelper == null) {
            diskLruCacheHelper = new DiskLruCacheUtil(context);
        }
        return diskLruCacheHelper;
    }

    public File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (isExternalStorageWritable()) {
            //如果挂载了sdcard，获取外部存储私有区域路径
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            //如果没有挂载sdcard，则获取内部存储缓存区域
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * 检查外部存储是否可用
     * @return
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            //挂载了sdcard，返回真
            return true;
        } else {
            //否则返回假
            return false;
        }
    }

    /**
     * 获取应用版本号
     * 当版本号改变，缓存路径下存储的所有数据都会被清除掉，因为DiskLruCache认为
     * 当应用程序有版本更新的时候，所有的数据都应该从网上重新获取。
     * @param context
     * @return
     */
    public int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 写入图片数据到文件缓存
     * @param bitmap
     */
    public void writeToCache(Bitmap bitmap) {
        try {
            String key = hashKeyForDisk(DiskLruCachePath);;
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(0);
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                    editor.commit();
                } else {
                    editor.abort();
                }
            }
            mDiskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从缓存读取数据
     * @return
     */
    public Bitmap readFromCache() {
        Bitmap bitmap = null;
        try {
            String key = hashKeyForDisk(DiskLruCachePath);
            DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
            if (snapShot != null) {
                //如果文件存在,读取数据转换为Bitmap对象
                InputStream is = snapShot.getInputStream(0);
                bitmap = BitmapFactory.decodeStream(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 将文件名转换成"MD5"编码
     * @param key
     * @return
     */
    public String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
