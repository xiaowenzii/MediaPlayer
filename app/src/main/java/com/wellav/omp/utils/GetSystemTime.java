package com.wellav.omp.utils;

import com.wellav.omp.channel.SysConfig;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by JingWen.Li on 2018/1/25.
 */

public class GetSystemTime {

    public static String GetSysTime(int hourMin) {
        String sysTime = "";
        Calendar calendar = Calendar.getInstance();
        String year = calendar.get(Calendar.YEAR) + "";
        String month = (calendar.get(Calendar.MONTH) + 1) + "";
        String day = calendar.get(Calendar.DAY_OF_MONTH) + "";
        String hour = calendar.get(Calendar.HOUR_OF_DAY) + "";
        String minute = calendar.get(Calendar.MINUTE) + "";
        if (hourMin == SysConfig.HOUR_MINUNTE) {
            sysTime = (hour.length() == 1 ? "0" + hour : hour) + ":" + (minute.length() == 1 ? "0" + minute : minute);
        } else {
            sysTime = year + "/" + (month.length() == 1 ? "0" + month : month) + "/" + (day.length() == 1 ? "0" + day : day) + "  " + (hour.length() == 1 ? "0" + hour : hour) + ":" + (minute.length() == 1 ? "0" + minute : minute);
        }
        return sysTime;
    }

    public static String GetTimeFromServer(String ipServer, int hourMin) throws Exception {
        String sysTime = "";
        String timeData = Utilss.httpGetChannels(ipServer + "/omp120/sys_settings/systime");
        //String timeData = Utilss.httpGetChannels(ipServer + "/source/time.json");
        if (!"".equals(timeData) && timeData != null) {
            JSONObject timeDataJson = new JSONObject(timeData);
            if (Utilss.getJsonDataInt(timeDataJson, "code") == 0) {
                JSONObject dataJson = Utilss.getJsonObject(timeDataJson, "data");
                if (dataJson != null) {
                    String time = Utilss.getJsonDataString(dataJson, "currentTime");
                    String timeArray[] = time.split(" ");

                    String[] hourMinArray = timeArray[1].split(":");
                    String hourMinute = hourMinArray[0] + ":" + hourMinArray[1];
                    if (hourMin == SysConfig.HOUR_MINUNTE) {
                        sysTime = hourMinute;
                    } else {
                        if (timeArray.length > 2) {
                            sysTime = timeArray[0] + " " + hourMinute + " " + timeArray[2];
                        } else {
                            sysTime = timeArray[0] + " " + hourMinute;
                        }
                    }
                }
            }
        }
        return sysTime;
    }
}
