package com.kuangchi.sdd.comm.equipment.gate.respondRecord;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;

import com.kuangchi.sdd.comm.equipment.base.BaseHandler;
import com.kuangchi.sdd.comm.equipment.common.ReceiveData;
import com.kuangchi.sdd.comm.equipment.common.ReceiveHeader;
import com.kuangchi.sdd.comm.equipment.common.RespondRecord;
import com.kuangchi.sdd.comm.equipment.common.server.GateRecordInterface;
import com.kuangchi.sdd.comm.equipment.gate.first.SetFirstHandler;
import com.kuangchi.sdd.comm.util.GsonUtil;
import com.kuangchi.sdd.comm.util.Util;
/**
 * 回复设备上报记录
 * 
 * */
public class RespondRecordHandler extends BaseHandler<ByteBuf,GateRecordInterface, ReceiveData> {
	public static final Logger LOG = Logger.getLogger(RespondRecordHandler.class);
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
		//设置有效数据
		ByteBuf dataBuf = receiveBuffer.readBytes(5);// 取有效数据
		ReceiveData data = executeData(dataBuf);
		receiver.setData(data);
		dataBuf = null;
		
		int orderStatus = receiveBuffer.readUnsignedByte();// 命令状态
		receiver.setOrderStatus(orderStatus);
		int checkSum = receiveBuffer.readUnsignedShort();// 校验和
		receiver.setCrc(checkSum);
		LOG.info(receiver.getCrcFromSum());
		//GateRecordInterface serverInfo = getDataForServer(data);
		String returnValue = null;
		if(receiver.getCrcFromSum()==checkSum){
			LOG.info("数据校验成功！");
			returnValue = GsonUtil.toJson(data.getRespondRecord());
		}
		LOG.info("客户端接收的十六进制信息:"
				+ Util.lpad(Integer.toHexString(header),2)
				+ "|"
				+ Util.lpad(Integer.toHexString(sign),2)
				+ "|"
				// + Integer.toHexString(macAddress)
				+ Util.lpad(Integer.toHexString(mac),6) + "|"
				+ Util.lpad(Integer.toHexString(order),2) + "|"
				+ Util.lpad(Integer.toHexString(length),3) + "|"
				+ Util.lpad(Integer.toHexString(orderStatus),2) + "|"
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
		RespondRecord recordData = new RespondRecord();
		
		int recordType = dataBuf.readUnsignedByte();
		recordData.setRecordType(recordType);
		
		long recordId=dataBuf.readUnsignedInt();
		recordData.setRecordId(recordId);
		
		return data;
	}
	/**
	 * 配置返回给服务器的数据
	 * @param data
	 * @return
	 */
	protected GateRecordInterface getDataForServer(ReceiveData data) {
		
		return null;
	}
}
