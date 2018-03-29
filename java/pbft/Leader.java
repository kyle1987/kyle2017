package com.formssi.middleware.pbft;

import java.util.Map;

import com.formssi.middleware.utils.HttpUtils;

public class Leader {
	public boolean isConnected(int node) throws Exception {
		boolean isconnected = false;
		String url = null;
		Map<String, String> map = null;

		String connected = HttpUtils.httpPost(url, map);
		if (connected.equals("")) {
			isconnected = true;
		}
		return isconnected;
	}

	public int getleader(int node) throws Exception {
		int nodenum = 4;
		int connectnum = 0;
		for (int i = 0; i <= nodenum; i++) {
			if (i != node) {
				boolean isconnected = isConnected(i);
				if (isconnected == true) {
					connectnum++;
				}
			}
		}
		if (connectnum >= 2) {
			return node;
		} else {
			return -1;
		}

	}
	
}
