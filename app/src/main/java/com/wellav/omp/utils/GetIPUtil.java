package com.wellav.omp.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by bingjia.zheng on 2018/8/20.
 */

public class GetIPUtil {
    public static final String FLAG = "omp120_ip_";

    // 构造方法私有化 不允许new对象
    private GetIPUtil() {
    }

    public static ArrayList namelist(File[] files, ArrayList name) {
        getFileName(files, name);
        return name;
    }

    public static String getPathIp(ArrayList nameList) {
        int version = 0;
        String ip = "";
        if (nameList.size() > 0) {
            for (int i = 0; i < nameList.size(); i++) {
                if (nameList.get(i).toString().contains(FLAG)) {
                    String[] name = nameList.get(i).toString().split("_");
                    if (name.length == 4) {
                        String verStr = name[3].substring(0, name[3].length() - 1);
                        if (Integer.parseInt(verStr) > version) {
                            version = Integer.parseInt(verStr);
                            ip = name[2];
                        }
                    }
                }
            }
        }
        return ip;
    }

    private static void getFileName(File[] files, ArrayList name) {
        if (name == null) {
            name = new ArrayList();
        }

        // 先判断目录是否为空，否则会报空指针
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    getFileName(file.listFiles(), name);
                } else {
                    String fileName = file.getName();
                    if (fileName.endsWith(".apk")) {
                        HashMap map = new HashMap();
                        map.put("Name", fileName.substring(0, fileName.lastIndexOf(".")));
                        name.add(map);
                    }
                }
            }
        }
    }

    public static List<String> getExtSDCardPath() {
        List<String> lResult = new ArrayList<String>();
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("extSdCard")) {
                    String[] arr = line.split(" ");
                    String path = arr[1];
                    File file = new File(path);
                    if (file.isDirectory()) {
                        lResult.add(path);
                    }
                }
            }
            isr.close();
        } catch (Exception e) {
        }
        return lResult;
    }
}
