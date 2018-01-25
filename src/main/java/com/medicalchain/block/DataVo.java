package com.medicalchain.block;

public class DataVo {
	
	private String user;
	
	private String type;
	
	private int count;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	public String toString() {
		return user + "_" + type + "count";
	}

}
