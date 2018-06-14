package com.kuangchi.sdd.comm.equipment.gate.userGroup;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;

import com.kuangchi.sdd.comm.equipment.base.BaseHandler;
import com.kuangchi.sdd.comm.equipment.common.ReceiveData;
import com.kuangchi.sdd.comm.equipment.common.ReceiveHeader;
import com.kuangchi.sdd.comm.equipment.common.UserTimeBlock;
import com.kuangchi.sdd.comm.equipment.common.server.GateLimitInterface;
import com.kuangchi.sdd.comm.equipment.gate.first.SetFirstHandler;
import com.kuangchi.sdd.comm.equipment.upload.record.UploadStatus3Handler;
import com.kuangchi.sdd.comm.util.GsonUtil;
import com.kuangchi.sdd.comm.util.Util;
/**
 * 读取用户时段组
 * */
public class GetUserGroupHandler extends BaseHandler<ByteBuf, GateLimitInterface, ReceiveData>{
	public static final Logger LOG = Logger.getLogger(GetUserGroupHandler.class);
	/**
	 * 处理报头
	 * 
	 * @param receiveBuffer
	 */
	protected String executeResponse(ByteBuf receiveBuffer) {
		
		ReceiveHeader receiver = new ReceiveHeader();
		int header = receiveBuffer.readUnsignedByte();// 报头aa
		receiver.setHeader(header);
		int sign = receiveBuffer.readUnsignedByte();// 设备标志04
		receiver.setSign(sign);
		int mac = receiveBuffer.readUnsignedMedium();// MAC地址
		receiver.setMac(mac);
		int order = receiveBuffer.readUnsignedByte();// 命令字
		receiver.setOrder(order);
		int length = receiveBuffer.readUnsignedShort();// 有效数据长度 2字节
		receiver.setLength(length);
		// 设置有效数据
		ByteBuf dataBuf = receiveBuffer.readBytes(513);// 取513字节有效数据

		
		ReceiveData data = executeData(dataBuf);
		receiver.setData(data);
		dataBuf = null;

		int orderStatus = receiveBuffer.readUnsignedByte();// 命令状态
		receiver.setOrderStatus(orderStatus);
		int checkSum = receiveBuffer.readUnsignedShort();// 校验和
		receiver.setCrc(checkSum);
		LOG.info(receiver.getCrcFromSum());
//		GateLimitInterface serverInfo = getDataForServer(data);
		String returnValue = null;
		if (receiver.getCrcFromSum() == checkSum) {
			LOG.info("数据校验成功-----");
			returnValue = GsonUtil.toJson(data.getUserTimeBlock());
		}
		LOG.info("客户端接收的十六进制信息:"
				+ Util.lpad(Integer.toHexString(header), 2)
				+ "|"
				+ Util.lpad(Integer.toHexString(sign), 2)
				+ "|"
				// + Integer.toHexString(macAddress)
				+ Util.lpad(Integer.toHexString(mac), 6) + "|"
				+ Util.lpad(Integer.toHexString(order), 2) + "|"
				+ Util.lpad(Integer.toHexString(length), 3) + "|"
				+ Util.lpad(Integer.toHexString(orderStatus), 2) + "|"
				+ checkSum + "|");
		return returnValue;

	}

	/**
	 * 获取有效数据
	 * 
	 * @param dataBuf
	 * @param bufLength
	 * @return
	 */
	protected ReceiveData executeData(ByteBuf dataBuf) {
		ReceiveData data = new ReceiveData();
		UserTimeBlock timeBlock = new UserTimeBlock();
        /**
         * 读取块号 1字节
         */
        int block = dataBuf.readUnsignedByte();
        LOG.info("block="+block);
        timeBlock.setBlock(block);
        List<Integer> times=new ArrayList<Integer>();
        for(int i=0;i<512;i++){
        	int timeNum=dataBuf.readUnsignedByte();
        	
        	 times.add(timeNum);
        }
        timeBlock.setTimes(times);
        
        
        data.setUserTimeBlock(timeBlock);
        
		return data;
	}

	/**
	 * 配置返回给服务器的数据
	 * 
	 * @param data
	 * @return
	 */
	protected GateLimitInterface getDataForServer(ReceiveData data) {
		
		return null;
	}
}
