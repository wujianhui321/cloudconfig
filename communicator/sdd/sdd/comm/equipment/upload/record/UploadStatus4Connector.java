package com.kuangchi.sdd.comm.equipment.upload.record;

import com.kuangchi.sdd.comm.equipment.base.BaseHandler;
import com.kuangchi.sdd.comm.equipment.base.Connector;
import com.kuangchi.sdd.comm.equipment.base.Manager;
/**
 *上传门禁状态
 * */
public class UploadStatus4Connector extends Connector{
	@Override
	public String run() throws Exception {
		BaseHandler handler = new UploadStatus3Handler();
		Manager manager = new UploadStatus3Manager(this.getDeviceInfo());//设备接口类
		String result = super.event(handler, manager);
		return result;
	}
}
