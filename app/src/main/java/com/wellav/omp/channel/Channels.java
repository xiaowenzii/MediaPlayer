package com.wellav.omp.channel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Channels implements Serializable {

	private int mCode;
	private String currentTime;
	private List<Channel> contentList = new ArrayList<Channel>();


	public int getmCode() {
		return mCode;
	}

	public void setmCode(int mCode) {
		this.mCode = mCode;
	}

	public String getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(String currentTime) {
		this.currentTime = currentTime;
	}

	public List<Channel> getContentList() {
		return contentList;
	}

	public void setContentList(List<Channel> contentList) {
		this.contentList = contentList;
	}
}
