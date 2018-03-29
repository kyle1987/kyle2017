package com.formssi.middleware.pbft;


public class ChangeView {
	public int num;
	public int changeLeader() {
		int oldLeader=0;
		num = num ++;
		if(num>=2){
			Leader leader = new Leader();
			try {
				int newLeader = leader.getleader(oldLeader+1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return changeLeader();
	}
}
