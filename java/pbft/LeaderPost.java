package com.formssi.middleware.pbft;

import java.util.Map;

import com.formssi.middleware.utils.HttpUtils;

public class LeaderPost extends Thread{
	
	public LeaderPost(String peer) {
		// TODO Auto-generated constructor stub
		super(peer);
	}
	
	@Override
	public void run() {
		String url = null;
		Map<String, String> map = null;
		try {
			HttpUtils.httpPost(url, map);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
