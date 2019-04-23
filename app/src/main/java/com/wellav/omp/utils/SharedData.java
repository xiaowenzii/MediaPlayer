package com.wellav.omp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by JingWen.Li on 2018/1/11.
 */

public class SharedData {

    //设置SharedPreferences
    public static SharedPreferences getSystemSettingsSharedPreferences(Context context){
        return context.getSharedPreferences("settings", Activity.MODE_PRIVATE);
    }
    //缓存信息
    public final static String LANG = "LANG";
    public final static String CHINESE = "zh_CN";
    public final static String ENGLISH = "en_GB";
    public final static String IPSERVER = "IPTV_IP_SERVER";
    public final static String APPAUTOSTART = "IPTV_APP_AUTO_START";
    public final static String VERSIONCODE = "IPTV_VERSIONCODE";

    public final static String CURRENTMODEL = "IPTV_CURRENTMODEL";
    public final static String CURRENTCHANNELPOS = "IPTV_CURRENTCHANNELPOS";

    public final static String HOTELINTRODUCTION = "酒店介绍";
    public final static String BRANDHISTORY = "品牌历史";
    public final static String ROOMINTRODUCTION = "客房介绍";
    public final static String FACILITYINTRODUCTION = "设施介绍";
    public final static String BUSINESSSERVICES = "商务服务";
    public final static String HOTELFOOD = "酒店美食";
    public final static String lOCALFEATURES = "当地特色";

}
