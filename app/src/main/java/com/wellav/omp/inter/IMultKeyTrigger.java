package com.wellav.omp.inter;

public interface IMultKeyTrigger {

	//是否允许触发，也就是触发组合键的条件
	boolean allowTrigger();

	//检查输入的按键是否是对应组合键某个位置
	boolean checkKey(int keycode, long eventTime);

	//检查组合键是否已经输入完成
	boolean checkMultKey();

	//清除所有记录的键
	void clearKeys();

	//组合键触发事件
	void onTrigger();
}