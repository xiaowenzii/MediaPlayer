package com.wellav.omp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by bingjia.zheng on 2018/8/20.
 */

public class GetBitmapUtil {

    public static Bitmap httpGetBitmap(String url, int width, int height) {
        URL myFileUrl;
        Bitmap bitmap = null;
        try {
            myFileUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setConnectTimeout(2000);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.connect();
            InputStream is = conn.getInputStream();
            if (!is.markSupported()) {
                System.out.println("mark/reset not supported!");
            }
            bitmap = getFitSampleBitmap(is, width, height);

            /*BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inSampleSize = 2;
            bitmap = BitmapFactory.decodeStream(is, null, options);*/
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap getFitSampleBitmap(InputStream inputStream, int width, int height) throws Exception {
        Bitmap bitmap = null;
        inputStream.mark(0);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
//        byte[] bytes = readStream(inputStream);
        BitmapFactory.decodeStream(inputStream, null, options);
//        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        options.inSampleSize = getFitInSampleSize(width, height, options);
        inputStream.reset();
        options.inJustDecodeBounds = false;

//        options.inPreferredConfig = Bitmap.Config.RGB_565;
        bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        return bitmap;
//        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    /*
     * 从inputStream中获取字节流 数组大小
    * */
    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    public static int getFitInSampleSize(int reqWidth, int reqHeight, BitmapFactory.Options options) {
        int inSampleSize = 1;
        if (options.outWidth > reqWidth || options.outHeight > reqHeight) {
            int widthRatio = Math.round((float) options.outWidth / (float) reqWidth);
            int heightRatio = Math.round((float) options.outHeight / (float) reqHeight);
            inSampleSize = Math.min(widthRatio, heightRatio);
        }
        return inSampleSize;
    }
}
