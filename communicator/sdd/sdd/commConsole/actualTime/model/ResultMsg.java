package com.kuangchi.sdd.commConsole.actualTime.model;

import java.util.List;

public class ResultMsg {
	private String result_code;
	private String result_msg;
	private ActualTimeBean allActualTime;
	
	public ActualTimeBean getAllActualTime() {
		return allActualTime;
	}
	public void setAllActualTime(ActualTimeBean allActualTime) {
		this.allActualTime = allActualTime;
	}
	public String getResult_code() {
		return result_code;
	}
	public void setResult_code(String result_code) {
		this.result_code = result_code;
	}
	public String getResult_msg() {
		return result_msg;
	}
	public void setResult_msg(String result_msg) {
		this.result_msg = result_msg;
	}
}
