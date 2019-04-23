package com.wellav.omp.inter;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;

public class MultKeyTrigger implements IMultKeyTrigger {
    private int[] MULT_KEY;
    //是否限定要在时间间隔里再次输入按键
    private static boolean ALLAW_SETTING_DELAYED_FLAG = true;
    //允许用户在多少时间间隔里输入按键
    private static int CHECK_NUM_ALLAW_MAX_DELAYED = 3000;
    //记录用户连续输入了多少个有效的键
    private static int check_num = 0;
    //最后一次用户输入按键的时间
    private static long lastEventTime = 0;

    public Context context;

    public MultKeyTrigger(Context context, int[] ints) {
        this.context = context;
        this.MULT_KEY = ints;
    }

    @Override
    public boolean allowTrigger() {
        return true;
    }

    @Override
    public boolean checkKey(int keycode, long eventTime) {
        boolean check;
        int delayed;
        //转换为实际数值
        int num = keycode - KeyEvent.KEYCODE_0;
        Log.e("num", num + "");
        //首次按键
        if (lastEventTime == 0) {
            delayed = 0;
        } else {
            //非首次按键
            delayed = (int) (eventTime - lastEventTime);
        }
        check = checkKeyValid(num, delayed);
        lastEventTime = check ? eventTime : 0L;
        return check;
    }

    /**
     * 传入用户输入的按键
     *
     * @param num
     * @param delayed 两次按键之间的时间间隔
     * @return
     */
    private boolean checkKeyValid(int num, int delayed) {
        //如果超过最大时间间隔，则重置
        if (ALLAW_SETTING_DELAYED_FLAG && delayed > CHECK_NUM_ALLAW_MAX_DELAYED) {
            if (lastEventTime == 0) {
                check_num = 0;
                return false;
            } else {
                check_num = 0;
                //如果输入的数刚好等于校验位置的数，则有效输入+1
                if (check_num < MULT_KEY.length && MULT_KEY[check_num] == num) {
                    check_num++;
                    return true;
                } else {
                    //如果输入错误的话，则重置掉原先输入的
                    check_num = 0;
                    return false;
                }

            }
        }
        //如果输入的数刚好等于校验位置的数，则有效输入+1
        if (check_num < MULT_KEY.length && MULT_KEY[check_num] == num) {
            check_num++;
            return true;
        } else {
            //如果输入错误的话，则重置掉原先输入的
            check_num = 0;
            return false;
        }


    }

    @Override
    public void clearKeys() {
        lastEventTime = 0;
        check_num = 0;
    }

    @Override
    public boolean checkMultKey() {
        return check_num == MULT_KEY.length;
    }

    @Override
    public void onTrigger() {
        if (checkMultKey()) {
            //TODO 发组合按键
        }
    }
}