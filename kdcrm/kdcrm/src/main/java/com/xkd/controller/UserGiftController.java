package com.xkd.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xkd.exception.GlobalException;
import com.xkd.model.ResponseConstants;
import com.xkd.model.ResponseDbCenter;
import com.xkd.model.TicketJson;
import com.xkd.model.TokenJson;
import com.xkd.model.WxParams;
import com.xkd.service.UserGiftService;
import com.xkd.utils.PropertiesUtil;
import com.xkd.utils.WxHttpUtil;


@Controller
@RequestMapping("/userGift")
@Transactional
public class UserGiftController {

    @Autowired
    private UserGiftService userGiftService;

    /**
     *
     * @param req
     * @return保存用户抽奖的信息
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/saveUserGift")
    public ResponseDbCenter saveUserGift(HttpServletRequest req) throws Exception{

        String activityId = req.getParameter("activityId");
        String openId = req.getParameter("openId");
        String uname = req.getParameter("uname");
        String mobile = req.getParameter("mobile");
        String gift = req.getParameter("gift");
        String companyName = req.getParameter("companyName");

        try {

            if(StringUtils.isBlank(activityId) || (StringUtils.isBlank(openId) && (StringUtils.isBlank(mobile)))){
                return  ResponseConstants.MISSING_PARAMTER;
            }

            openId = StringUtils.isBlank(openId)?null:openId;
            mobile = StringUtils.isBlank(mobile)?null:mobile;

            List<Map<String,Object>>  userGiftMaps = userGiftService.selectUserGiftByMobileOrOpenId(activityId,mobile,openId);
            if(userGiftMaps != null && userGiftMaps.size()>0){
                return  ResponseConstants.USER_GIFT_HAS_GETGIFT;
            }

            Map<String,Object> map = new HashMap<>();
            map.put("id", UUID.randomUUID().toString());
            map.put("activityId", activityId);
            map.put("uname", uname);
            map.put("openId", openId);
            map.put("mobile", mobile);
            map.put("gift", gift);
            map.put("companyName", companyName);

           Integer num = userGiftService.saveUserGift(map);

        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
        }
        ResponseDbCenter responseDbCenter = new ResponseDbCenter();
        return responseDbCenter;
    }

    /**
     *
     * @param req
     * @return保存用户抽奖的信息
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/updateUserGiftById")
    public ResponseDbCenter updateUserGiftById(HttpServletRequest req) throws Exception{

        String id = req.getParameter("id");
        String gift = req.getParameter("gift");


        try {

            if(StringUtils.isBlank(id) || StringUtils.isBlank(gift)){
                return  ResponseConstants.MISSING_PARAMTER;
            }

            Integer num = userGiftService.updateUserGiftById(id,gift);

        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
        }
        ResponseDbCenter responseDbCenter = new ResponseDbCenter();
        return responseDbCenter;
    }

    /**
     *
     * @param req
     * @return保存用户抽奖的信息
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/selectUserGift")
    public ResponseDbCenter selectUserGift(HttpServletRequest req) throws Exception{

        String activityId = req.getParameter("activityId");
        String openId = req.getParameter("openId");
        String mobile = req.getParameter("mobile");
        Map<String,Object>  userGiftMap = null;

        try {

            if(StringUtils.isBlank(activityId) || (StringUtils.isBlank(openId))){
                return  ResponseConstants.MISSING_PARAMTER;
            }
            ResponseDbCenter responseDbCenter = new ResponseDbCenter();
            List<Map<String,Object>> userGiftMaps = userGiftService.selectUserGiftByMobileOrOpenId(activityId,mobile,openId);

            List<Map<String,Object>> mapps = new ArrayList<>();


            if(userGiftMaps == null || userGiftMaps.size() ==0){
                responseDbCenter.setResModel(null);
                return responseDbCenter;
            }else if(userGiftMaps.size() ==2){
                for(Map<String,Object> map : userGiftMaps){
                    String gift = map.get("gift") == null?null:(String) map.get("gift");
                    if(StringUtils.isNotBlank(gift)){
                        mapps.add(map);
                    }
                }
            }else{
                responseDbCenter.setResModel(userGiftMaps == null?null:userGiftMaps.get(0));
                return responseDbCenter;
            }

            if(mapps.size() == 2 || mapps.size() == 0){
                List<Map<String,Object>> openIdUserGiftMaps = userGiftService.selectUserGiftByMobileOrOpenId(activityId,null,openId);
                responseDbCenter.setResModel(openIdUserGiftMaps.get(0));
                return responseDbCenter;
            }else{
                responseDbCenter.setResModel(mapps.get(0));
                return responseDbCenter;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
        }


    }

    /**
     *
     * @param req
     * @return保存用户抽奖的信息
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/selectUserGiftsByActivityId")
    public ResponseDbCenter selectUserGiftsByActivityId(HttpServletRequest req) throws Exception{

        String activityId = req.getParameter("activityId");
        List<Map<String,Object>> userGiftMaps = null;

        try {

            if(StringUtils.isBlank(activityId)){
                return  ResponseConstants.MISSING_PARAMTER;
            }
            userGiftMaps = userGiftService.selectUserGiftsByActivityId(activityId);

        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
        }
        ResponseDbCenter responseDbCenter = new ResponseDbCenter();
        responseDbCenter.setResModel(userGiftMaps);
        return responseDbCenter;
    }


    @ResponseBody
	@RequestMapping("/getWxConfig")
	public Map<String, String> getWxConfig(HttpServletRequest request){
		String url = request.getParameter("url");
        //处理token失效的问题  
        try {  
            long tokenTimeLong = Long.parseLong(WxParams.tokenTime);  
            long tokenExpiresLong = Long.parseLong(WxParams.tokenExpires);  
              
            //时间差  
            long differ = (System.currentTimeMillis() - tokenTimeLong) /1000;  
            if (WxParams.token == null ||  differ > (tokenExpiresLong - 1800)) {  
                System.out.println("token为null，或者超时，重新获取");  
                TokenJson tokenJson = WxHttpUtil.getToken();  
                if (tokenJson != null) {  
                    WxParams.token = tokenJson.getAccess_token();  
                    WxParams.tokenTime = System.currentTimeMillis()+"";  
                    WxParams.tokenExpires = tokenJson.getExpires_in()+"";  
                }  
            }  
        } catch (Exception e) {  
            // TODO: handle exception  
            System.out.println(e);  
            TokenJson tokenJson = WxHttpUtil.getToken();  
            if (tokenJson != null) {  
                WxParams.token = tokenJson.getAccess_token();  
                WxParams.tokenTime = System.currentTimeMillis()+"";  
                WxParams.tokenExpires = tokenJson.getExpires_in()+"";  
            }  
        }  
  
        //处理ticket失效的问题  
        try {  
            long ticketTimeLong = Long.parseLong(WxParams.ticketTime);  
            long ticketExpiresLong = Long.parseLong(WxParams.ticketExpires);  
              
            //时间差  
            long differ = (System.currentTimeMillis() - ticketTimeLong) /1000;  
            if (WxParams.ticket == null ||  differ > (ticketExpiresLong - 1800)) {  
                System.out.println("ticket为null，或者超时，重新获取");  
                TicketJson ticketJson = WxHttpUtil.getTicket(WxParams.token);  
                if (ticketJson != null) {  
                    WxParams.ticket = ticketJson.getTicket();  
                    WxParams.ticketTime = System.currentTimeMillis()+"";  
                    WxParams.ticketExpires = ticketJson.getExpires_in()+"";  
                }  
            }  
        } catch (Exception e) {  
            // TODO: handle exception  
            System.out.println(e); 
            TicketJson ticketJson = WxHttpUtil.getTicket(WxParams.token);  
            if (ticketJson != null) {  
                WxParams.ticket = ticketJson.getTicket();  
                WxParams.ticketTime = System.currentTimeMillis()+"";  
                WxParams.ticketExpires = ticketJson.getExpires_in()+"";  
            }  
        }  
  
        Map<String, String> ret = WxHttpUtil.sign(WxParams.ticket, url); 
        System.out.println("计算出的签名-----------------------");  
//        for (Map.Entry entry : ret.entrySet()) {  
//            System.out.println(entry.getKey() + ", " + entry.getValue());  
//        }  
        System.out.println("-----------------------"); 
        ret.put("appid", PropertiesUtil.APPID);
        return ret;  
    } 



}
