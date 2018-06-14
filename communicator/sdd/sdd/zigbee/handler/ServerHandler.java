package com.kuangchi.sdd.zigbee.handler;

import com.kuangchi.sdd.zigbee.model.Data0xAA0x01;
import com.kuangchi.sdd.zigbee.model.Data0xAA0x02;
import com.kuangchi.sdd.zigbee.model.Data0xAA0x03;
import com.kuangchi.sdd.zigbee.model.Data0xAA0x04;
import com.kuangchi.sdd.zigbee.model.Data0xAA0x05;
import com.kuangchi.sdd.zigbee.model.Data0xAA0x06;
import com.kuangchi.sdd.zigbee.model.Data0xAA0x07;
import com.kuangchi.sdd.zigbee.model.Data0xAA0x08;
import com.kuangchi.sdd.zigbee.model.Data0xAA0x09;
import com.kuangchi.sdd.zigbee.model.Data0xAA0x0A;
import com.kuangchi.sdd.zigbee.model.Data0xAA0x0B;
import com.kuangchi.sdd.zigbee.model.Data0xAA0x0C;
import com.kuangchi.sdd.zigbee.model.Data0xAA0x0D;
import com.kuangchi.sdd.zigbee.model.Data0xAA0x0E;
import com.kuangchi.sdd.zigbee.model.Data0xAA0x0F;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
/**
 * 
 * 读到网关发到平台的记录后进行业务处理
 * 
 * 
 * */
public class ServerHandler   extends ChannelHandlerAdapter{
     @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    		throws Exception {
    	 if (msg instanceof  Data0xAA0x01) {
    		Data0xAA0x01 data0xAA0x01=(Data0xAA0x01) msg;
			SendService.sendData0xEE0x01(data0xAA0x01, ctx);
		 }else if(msg instanceof Data0xAA0x02){
			 Data0xAA0x02 data0xAA0x02=(Data0xAA0x02) msg;
			 SendService.sendData0xEE0x02(data0xAA0x02, ctx);
		 }else if(msg instanceof Data0xAA0x03){
			 Data0xAA0x03 data0xAA0x03=(Data0xAA0x03) msg;
			 SendService.sendData0xEE0x03(data0xAA0x03, ctx);
		 }else if(msg instanceof Data0xAA0x04){
			 Data0xAA0x04 data0xAA0x04=(Data0xAA0x04) msg;
			 SendService.sendData0xEE0x04(data0xAA0x04, ctx);
		 }else if(msg instanceof Data0xAA0x05){
			 Data0xAA0x05 data0xAA0x05=(Data0xAA0x05) msg;
			 SendService.sendData0xEE0x05(data0xAA0x05, ctx);
		 }else if(msg instanceof Data0xAA0x06){
			 Data0xAA0x06 data0xAA0x06=(Data0xAA0x06) msg;
			 SendService.sendData0xEE0x06(data0xAA0x06, ctx);
		 }else if(msg instanceof Data0xAA0x07){
			 Data0xAA0x07 data0xAA0x07=(Data0xAA0x07) msg;
			 SendService.sendData0xEE0x07(data0xAA0x07, ctx);
		 }else if(msg instanceof Data0xAA0x08){
			 Data0xAA0x08 data0xAA0x08=(Data0xAA0x08) msg;
			 SendService.sendData0xEE0x08(data0xAA0x08, ctx);
		 }else if(msg instanceof Data0xAA0x09){
			 Data0xAA0x09 data0xAA0x09=(Data0xAA0x09) msg;
			 SendService.sendData0xEE0x09(data0xAA0x09, ctx);
		 }else if(msg instanceof Data0xAA0x0A){
			 Data0xAA0x0A data0xAA0x0A=(Data0xAA0x0A) msg;
			 SendService.sendData0xEE0x0A(data0xAA0x0A, ctx);
		 }else if(msg instanceof Data0xAA0x0B){
			 Data0xAA0x0B data0xAA0x0B=(Data0xAA0x0B) msg;
			 SendService.sendData0xEE0x0B(data0xAA0x0B, ctx);
		 }else if(msg instanceof Data0xAA0x0C){
			 Data0xAA0x0C data0xAA0x0C=(Data0xAA0x0C) msg;
			 SendService.sendData0xEE0x0C(data0xAA0x0C, ctx);
		 }else if(msg instanceof Data0xAA0x0D){
			 Data0xAA0x0D data0xAA0x0D=(Data0xAA0x0D) msg;
			 SendService.sendData0xEE0x0D(data0xAA0x0D, ctx);
		 }else if(msg instanceof Data0xAA0x0E){
			 Data0xAA0x0E data0xAA0x0E=(Data0xAA0x0E) msg;
			 SendService.sendData0xEE0x0E(data0xAA0x0E, ctx);
		 }else if(msg instanceof Data0xAA0x0F){
			 Data0xAA0x0F data0xAA0x0F=(Data0xAA0x0F) msg;
			 SendService.sendData0xEE0x0F(data0xAA0x0F, ctx);
		 }
    	 
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    		throws Exception {
    	// TODO Auto-generated method stub
    	super.exceptionCaught(ctx, cause);
    }
}