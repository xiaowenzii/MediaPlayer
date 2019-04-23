package com.wellav.omp.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.wellav.omp.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TimeZone;

public class Utilss {
    /**
     * 毫秒转换成时分秒
     *
     * @return
     */
    public static String getHHMMSS(int mTime, int timeLength) {
        String timeStr = null;
        int time = mTime / 1000;
        int hour = 0;
        int minute = 0;
        int second = 0;
        minute = time / 60;
        if (time <= 0) {
            return "00:00";
        } else {
            if (timeLength == 2) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99) {
                    return "59:59:59";
                }
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }

    private static boolean logTag = true;

    public static void PrintLog(String tag, String txt) {
        if (logTag) {
            Log.e(tag, txt);
        }
    }

    public static boolean isConnByHttp(String stringurl) {
        boolean isConn = false;
        URL url;
        HttpURLConnection conn = null;
        if (stringurl.equals("")) {
            return false;
        }
        try {
            url = new URL(stringurl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000 * 5);
            if (conn.getResponseCode() == 200) {
                isConn = true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
        return isConn;
    }

    /**
     * 获取网络请求
     *
     * @param path
     * @return
     */
    public static String httpGetChannels(String path) {
        StringBuffer sb = new StringBuffer();
        try {
            // 新建一个URL对象
            URL url = new URL(path);
            // 打开一个HttpURLConnection连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置请求方式get请求
            conn.setRequestMethod("GET");
            // 设置连接超时时间
            conn.setConnectTimeout(5000);
            // //再设置超时时间
            conn.setReadTimeout(5000);
            // 开始连接
            conn.connect();
            // 判断请求是否成功 成功码为200
            if (200 == conn.getResponseCode()) {
                InputStream inputStream = conn.getInputStream();
                // 把字节流转化成字符流 InputStreamReader
                InputStreamReader isr = new InputStreamReader(inputStream, "UTF-8");
                // 把字符流转换成缓冲字符流
                BufferedReader br = new BufferedReader(isr);
                // 创建一个StringBuffer

                String str = "";
                while ((str = br.readLine()) != null) {
                    sb.append(str);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    // 获得当前年月日时分秒星期
    public static String getTime() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String mYear = String.valueOf(c.get(Calendar.YEAR));
        String mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);
        String mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        String mHour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
        String mMinute = String.valueOf(c.get(Calendar.MINUTE));
        return mYear + "/" + timeLength(mMonth) + "/" + timeLength(mDay) + "  " + timeLength(mHour) + ":"
                + timeLength(mMinute);
    }

    public static String timeLength(String time) {
        String reTime = time;
        if (reTime.length() == 1) {
            reTime = "0" + reTime;
        }
        return reTime;
    }

    // 获得当前年月日时分秒星期
    public static String getHourMinuteTime() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String mHour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
        String mMinute = String.valueOf(c.get(Calendar.MINUTE));
        return timeLength(mHour) + ":" + timeLength(mMinute);
    }

    //获取网络图片
    public static Bitmap httpGetBitmap(String url) {
        HttpURLConnection conn = null;
        try {
            URL mURL = new URL(url);
            conn = (HttpURLConnection) mURL.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(500);
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                //访问成功
                InputStream is = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    // 有图片压缩
    public static Bitmap httpGetBitmap2(String url) {
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
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inSampleSize = 2;
            bitmap = BitmapFactory.decodeStream(is, null, options);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    //获取网络图片
    public static Drawable httpGetDrawable(String url) {
        Drawable drawable = null;
        try {
            // 可以在这里通过文件名来判断，是否本地有此图片
            drawable = Drawable.createFromStream(
                    new URL(url).openStream(), "image.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return drawable;
    }

    /**
     * 以最省内存的方式读取本地资源的图片
     *
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    public static String getVersion(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }


    public static int getVersionCode(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static ProgressDialog progressDialog;

    public static void downFile(Context context, File storgePath, String updateUrl) {

        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(context.getResources().getString(R.string.downloading));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setProgress(0);
        //获取存储路径
        if (DiskLruCacheUtil.isExternalStorageWritable()) {
            //如果挂载了sdcard，获取外部存储私有区域路径
            storgePath = new File(context.getExternalCacheDir(), "IPTV.apk");
        } else {
            //如果没有挂载sdcard，则获取内部存储缓存区域
            storgePath = new File(context.getCacheDir(), "IPTV.apk");
        }
        final DownloadTask downloadTask = new DownloadTask(context, storgePath);
        if (!"".equals(updateUrl) && updateUrl != null) {
            downloadTask.execute(updateUrl);
        }
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });
    }

    public static class DownloadTask extends AsyncTask<String, Integer, String> {

        public Context context;
        public File storgePath;
        public PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context, File storgePath) {
            this.context = context;
            this.storgePath = storgePath;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... url) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL urll = new URL(url[0]);
                connection = (HttpURLConnection) urll.openConnection();
                connection.connect();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }
                int fileLength = connection.getContentLength();
                input = connection.getInputStream();
                output = new FileOutputStream(storgePath);
                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    if (fileLength > 0) {
                        publishProgress((int) (total * 100 / fileLength));
                    }
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException ignored) {
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            progressDialog.dismiss();
            if (result != null)
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            else {
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
            }
            //这里主要是做下载后自动安装的处理
            update(context, storgePath);
            ((Activity) context).finish();
        }
    }

    //下载完成后更新APK
    public static void update(Context context, File storgePath) {
        progressDialog.cancel();
        chmod("777", storgePath.getPath());
        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.fromFile(storgePath), "application/vnd.android.package-archive");
        context.startActivity(i);
    }

    public static void chmod(String permission, String path) {
        String command = "chmod " + permission + " " + path;
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /******************************* Json 数据解析 *****************************/

    //判断json数据是否包含某个数据, Json数组
    public static JSONArray getJsonArray(JSONObject js, String key) throws Exception {
        JSONArray jsonArray = null;
        if (js != null) {
            if (js.has(key)) {
                jsonArray = js.getJSONArray(key);
            }
        }
        return jsonArray;
    }

    public static JSONObject getJsonObject(JSONObject js, String key) throws Exception {
        JSONObject jsonObject = null;
        if (js != null) {
            if (js.has(key)) {
                jsonObject = js.getJSONObject(key);
            }
        }
        return jsonObject;
    }

    //判断json数据是否包含某个数据，Json字符串
    public static String getJsonDataString(JSONObject js, String key) throws Exception {
        String dataString = "";
        if (js != null) {
            if (js.has(key)) {
                dataString = js.getString(key);
            }
        }
        return dataString;
    }

    //判断json数据是否包含某个数据，Json整数
    public static int getJsonDataInt(JSONObject js, String key) throws Exception {
        int dataInt = -1;
        if (js != null) {
            if (js.has(key)) {
                dataInt = js.getInt(key);
            }
        }
        return dataInt;
    }

    //判断json数据是否包含某个数据，Boolean类型
    public static boolean getJsonDataBoolean(JSONObject js, String key) throws Exception {
        boolean dataBoolean = false;
        if (js != null) {
            if (js.has(key)) {
                dataBoolean = js.getBoolean(key);
            }
        }
        return dataBoolean;
    }

    /**
     * 从控件所在位置移动到控件的底部
     *
     * @return
     */
    public static TranslateAnimation moveToViewBottom() {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        mHiddenAction.setDuration(200);
        return mHiddenAction;
    }

    /**
     * 从控件的底部移动到控件所在位置
     *
     * @return
     */
    public static TranslateAnimation moveToViewLocation() {
        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mHiddenAction.setDuration(200);
        return mHiddenAction;
    }

    //获取本地IP
    public static String getLocalIP(Context context) {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getWeek(Context context, String date) {
        String str = "";
        SharedPreferences sharedPreferences = SharedData.getSystemSettingsSharedPreferences(context);
        if (sharedPreferences.getString(SharedData.LANG, SharedData.CHINESE).equals(SharedData.CHINESE)) {
            switch (date) {
                case "1":
                    str = "星    期    一";
                    break;
                case "2":
                    str = "星    期    二";
                    break;
                case "3":
                    str = "星    期    三";
                    break;
                case "4":
                    str = "星    期    四";
                    break;
                case "5":
                    str = "星    期    五";
                    break;
                case "6":
                    str = "星    期    六";
                    break;
                case "0":
                    str = "星    期    日";
                    break;
            }
        } else {
            switch (date) {
                case "1":
                    str = "Monday";
                    break;
                case "2":
                    str = "Tuesday";
                    break;
                case "3":
                    str = "Wednesday";
                    break;
                case "4":
                    str = "Thursday";
                    break;
                case "5":
                    str = "Friday";
                    break;
                case "6":
                    str = "Saturday";
                    break;
                case "0":
                    str = "Sunday";
                    break;
            }
        }
        return str;
    }

    //dp->px
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //px->dp
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    //遍历设置字体
    public static void changeViewSize(ViewGroup viewGroup, int screenWidth, int screenHeight) {//传入Activity顶层Layout,屏幕宽,屏幕高
        int adjustFontSize = adjustFontSize(screenWidth, screenHeight);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View v = viewGroup.getChildAt(i);
            if (v instanceof ViewGroup) {
                changeViewSize((ViewGroup) v, screenWidth, screenHeight);
            } else if (v instanceof Button) {//按钮加大这个一定要放在TextView上面，因为Button也继承了TextView
//                ((Button) v).setTextSize(adjustFontSize);
            } else if (v instanceof TextView) {
                ((TextView) v).setTextSize(adjustFontSize);
                /*if(v.getId()== R.id.title_msg){//顶部标题
                    ( (TextView)v ).setTextSize(adjustFontSize+4);
                }else{
                    ( (TextView)v ).setTextSize(adjustFontSize);
                }*/
            }
        }
    }


    //获取字体大小
    public static int adjustFontSize(int screenWidth, int screenHeight) {
        screenWidth = screenWidth > screenHeight ? screenWidth : screenHeight;
        /**
         * 1. 在视图的 onsizechanged里获取视图宽度，一般情况下默认宽度是320，所以计算一个缩放比率
         rate = (float) w/320   w是实际宽度
         2.然后在设置字体尺寸时 paint.setTextSize((int)(8*rate));   8是在分辨率宽为320 下需要设置的字体大小
         实际字体大小 = 默认字体大小 x  rate
         */
        int rate = (int) (5 * (float) screenWidth / 320); //我自己测试这个倍数比较适合，当然你可以测试后再修改
        return rate < 15 ? 15 : rate; //字体太小也不好看的
    }

    /**
     * 获取当前videoView截图
     */
    public static Bitmap getCurrentVideoBitmap(String url, Long currentPosition) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(url, new HashMap<String, String>());
            bitmap = retriever.getFrameAtTime(currentPosition * 1000); //取得指定时间的Bitmap，即可以实现抓图（缩略图）功能
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }

        if (bitmap == null) {
            return null;
        }

        //bitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
        bitmap = Bitmap.createBitmap(bitmap);
        return bitmap;
    }

    /**
     * 对单独某个View进行截图
     *
     * @param v
     * @return
     */
    public static Bitmap loadBitmapFromView(View v) {
        if (v == null) {
            return null;
        }
        Bitmap screenshot;
        screenshot = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(screenshot);
        c.translate(-v.getScrollX(), -v.getScrollY());
        v.draw(c);
        return screenshot;
    }
    //获取设备分辨率，宽
    public static int getPhysicalScreenSize(Context context) {
        DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = dm.getDisplays();
        int maxWidth = -1, maxHeight = -1;
        DisplayMetrics met = new DisplayMetrics();
        for (Display d : displays) {
            d.getMetrics(met);
            maxWidth = met.widthPixels > maxWidth ? met.widthPixels : maxWidth;
            maxHeight = met.heightPixels > maxHeight ? met.heightPixels : maxHeight;
        }
        return maxWidth;
    }
}
