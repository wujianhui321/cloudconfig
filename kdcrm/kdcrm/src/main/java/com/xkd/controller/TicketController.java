package com.xkd.controller;

import com.alibaba.fastjson.JSON;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xkd.exception.GlobalException;
import com.xkd.model.*;
import com.xkd.service.*;
import com.xkd.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.models.auth.In;
import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Api(description = "票务功能接口")
@Controller
@RequestMapping("/ticket")
@Transactional
public class TicketController extends BaseController {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private CompanyService companyServie;
    @Autowired
    private AddressService addressService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private SolrService solrService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private  UserAttendMeetingService userAttendMeetingService;
    @Autowired
    private  MeetingService meetingService;

    @ApiOperation(value = "保存用户订票相关信息")
    @ResponseBody
    @RequestMapping(value = "/saveUserTicket", method = RequestMethod.POST)
    @Transactional
    public synchronized ResponseDbCenter saveUserTicket(HttpServletRequest req, HttpServletResponse rsp,
                                                        @ApiParam(value = "token", required = false) @RequestParam(required = false) String token,
                                                        @ApiParam(value = "票和票的数量", required = false) @RequestParam(required = false) String ticketNumbers,
                                                        @ApiParam(value = "用户姓名", required = false) @RequestParam(required = false) String uname,
                                                        @ApiParam(value = "手机号", required = false) @RequestParam(required = false) String mobile,
                                                        @ApiParam(value = "是否发送短信", required = false) @RequestParam(required = false) String sendMessageFlag,
                                                        @ApiParam(value = "验证码", required = false) @RequestParam(required = false) String mobileCode,
                                                        @ApiParam(value = "企业名称", required = false) @RequestParam(required = false) String companyName,
                                                        @ApiParam(value = "职位", required = false) @RequestParam(required = false) String station,
                                                        @ApiParam(value = "邮箱", required = false) @RequestParam(required = false) String email,
                                                        @ApiParam(value = "用户备注", required = false) @RequestParam(required = false) String remark,
                                                        @ApiParam(value = "会务ID", required = false) @RequestParam(required = false) String meetingId,
                                                        @ApiParam(value = "推广人ID", required = false) @RequestParam(required = false) String userSpreadId) throws Exception {


        if (StringUtils.isBlank(mobile) || StringUtils.isBlank(uname) || StringUtils.isBlank(meetingId)) {
            return ResponseConstants.MISSING_PARAMTER;
        }

        String usercode = (String) req.getSession().getAttribute("code" + mobile);
        if (usercode == null || !usercode.equals(mobileCode)) {
            return ResponseConstants.TEL_CODE_ERROR;
        }

        //因为该用户在前面已经登录了，所以直接是编辑用户
        String loginUserId = (String)req.getAttribute("loginUserId");

        String pcCompanyId = null;
        if(StringUtils.isNotBlank(meetingId)){
            Meeting meeting = meetingService.selectMeetingById(meetingId);
            if(meeting !=null){
                pcCompanyId = meeting.getPcCompanyId();
            }
        }

        JsonParser parse = new JsonParser();  //创建json解析器
        SortedMap<Object, Object> sortedMap = null;
        Meeting meeting = null;

        Map<String, Object> loginUserMap = userService.selectUserById(loginUserId);

        String openId = (String) loginUserMap.get("weixin");
        System.out.println("----------------------登录用户的openId="+openId);


        try {

            String companyId = UUID.randomUUID().toString();
            String insertUserId = "";
            String existCompanyId = "";
            String userCompanyId = UUID.randomUUID().toString();
            String addressId = UUID.randomUUID().toString();
            String existsUserIdd = null;

            //是否新增用户和当前登录用户没关系，只和手机号有关系，和当前登录用户没关系
            Map<String, Object> mapp = userService.selectUserByMobile(mobile, null);

            if (mapp != null && mapp.size() > 0) {

                existsUserIdd = (String) mapp.get("id");

                String realUserId = existsUserIdd;
                List<UserAttendMeeting> userAttendMeetings = userAttendMeetingService.selectMeetingUserByUserIdAndMeetingId(meetingId,realUserId);
                if(userAttendMeetings != null && userAttendMeetings.size()>0){
                    return ResponseConstants.TICKET_USER_EXISTS;
                }
            }



            System.out.println("=====================ticketNumbers===="+ticketNumbers);
            List<Map<String, Object>> maps = (List<Map<String, Object>>) JSON.parseObject(ticketNumbers,Object.class);
            System.out.println("=====================maps===="+maps);

            boolean ticketFlag = false;
            boolean numberFlag = false;
            long totoPrice = 0;
            String totalTicketMessage = "";
            String mhtOrderNo = "";
            for (Map<String, Object> map : maps) {

                String ticketId = (String) map.get("id");
                Integer number = (Integer) map.get("number");

                //查出库存
                Integer num = ticketService.getTicketSavingByTicketId(ticketId);
                /*if (num == null || num.intValue() == 0) {
                    return ResponseConstants.TICKET_HAS_SELL_OUT;
                }*/

                int saving = num.intValue();
                int numberInt = number.intValue();

                if (numberInt > saving) {
                    return ResponseConstants.TICKET_NOT_ENOUGHT;
                }

                if(numberInt > 0){
                    numberFlag = true;
                }

                if ((0 == saving)) {
                    continue;
                }


                Map<String, Object> ticketMap = ticketService.selectTicketById(ticketId);

                String ticketType = (String) ticketMap.get("ticketType");
                String price = (String) ticketMap.get("price");

                long doublePrice = Long.valueOf(price);

                totoPrice += doublePrice * (number.intValue());

                totalTicketMessage += ticketType + number.intValue() + "张,";

                ticketFlag = true;
            }

            if(!numberFlag && maps != null && maps.size() > 0){
                return ResponseConstants.TICKET_NUMBER_ZERO;
            }else if (!ticketFlag && maps != null && maps.size() > 0) {
                return ResponseConstants.TICKET_HAS_SELL_OUT;
            }


            meeting = meetingService.selectMeetingById(meetingId);

            if (StringUtils.isNotBlank(companyName)) {

                List<Company> companiess = companyServie.selectCompanyByName(companyName,pcCompanyId);
                List<Company> qicCompanies = null;
                JsonObject json = null;

                String companyInfoo = CompanyInfoApi.queryCompanyInfo("http://i.yjapi.com/ECISimple/GetDetailsByName", companyName, 1, 10);

                if (StringUtils.isNotBlank(companyInfoo)) {
                    json = (JsonObject) parse.parse(companyInfoo);  //创建jsonObject对象
                }
                //检查该企业是否在企查查存在，数据库中已经存在该企业
                if (json != null && "200".equals(json.get("Status").getAsString())) {
                    String name = json.get("Result").getAsJsonObject().get("Name").isJsonNull() ? "" : json.get("Result").getAsJsonObject().get("Name").getAsString();
                    qicCompanies = companyServie.selectCompanyByName(name,pcCompanyId);
                }

                Company company = (companiess == null || companiess.size() == 0) ?
                        ((qicCompanies == null || qicCompanies.size() == 0) ? null : qicCompanies.get(0)) : companiess.get(0);

                //企业已经存在,用户肯定存在的


                if (company != null) {


                    Map<String, Object> companyMap = new HashMap<>();
                    Map<String, Object> userInfoMap = new HashMap<>();

                    existCompanyId = company.getId();

                    userInfoService.changeUserInfo("insert",uname,station,"",existCompanyId,email,"0",null,null,"",mobile,"","","",loginUserId,null);
                    solrService.updateCompanyIndex(existCompanyId);
                } else {





                    if (json != null && "200".equals(json.get("Status").getAsString())) {

                        System.err.println("----------------" + companyName + "=====>" + (json.get("Result").getAsJsonObject().get("Name").isJsonNull() ? "" : json.get("Result").getAsJsonObject().get("Name").getAsString()) + "----------------");

                        //返回企业信息正确
                        String establish_time = "";
                        String operating_period = "";
                        String approve_date = "";
                        String updated_date = "";
                        String termStart = "";

                        try {

                            establish_time = json.get("Result").getAsJsonObject().get("StartDate") == null ? "" : json.get("Result").getAsJsonObject().get("StartDate").getAsString().substring(0, 10);//get("StartDate").getAsString().substring(0, 10)
                            operating_period = json.get("Result").getAsJsonObject().get("TeamEnd") == null ? "" : json.get("Result").getAsJsonObject().get("TeamEnd").getAsString().substring(0, 10);
                            approve_date = json.get("Result").getAsJsonObject().get("CheckDate") == null ? "" : json.get("Result").getAsJsonObject().get("CheckDate").getAsString().substring(0, 10);
                            updated_date = json.get("Result").getAsJsonObject().get("UpdatedDate") == null ? "" : json.get("Result").getAsJsonObject().get("UpdatedDate").getAsString().substring(0, 10);
                            termStart = json.get("Result").getAsJsonObject().get("TermStart") == null ? "" : json.get("Result").getAsJsonObject().get("TermStart").getAsString().substring(0, 10);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

//								            String id = json.get("Result").getAsJsonObject().get("KeyNo").isJsonNull()?null:json.get("Result").getAsJsonObject().get("KeyNo").getAsString();
                        String social_credit = json.get("Result").getAsJsonObject().get("CreditCode").isJsonNull() ? null : json.get("Result").getAsJsonObject().get("CreditCode").getAsString();
                        String registration_code = json.get("Result").getAsJsonObject().get("No").isJsonNull() ? null : json.get("Result").getAsJsonObject().get("No").getAsString();
                        String organization_code = json.get("Result").getAsJsonObject().get("No").isJsonNull() ? null : json.get("Result").getAsJsonObject().get("No").getAsString();
                        String manage_type = json.get("Result").getAsJsonObject().get("Status").isJsonNull() ? null : json.get("Result").getAsJsonObject().get("Status").getAsString();
                        String representative = json.get("Result").getAsJsonObject().get("OperName").isJsonNull() ? null : json.get("Result").getAsJsonObject().get("OperName").getAsString();
                        String registered_money = json.get("Result").getAsJsonObject().get("RegistCapi").isJsonNull() ? null : json.get("Result").getAsJsonObject().get("RegistCapi").getAsString();
                        String registration_authority = json.get("Result").getAsJsonObject().get("BelongOrg").isJsonNull() ? null : json.get("Result").getAsJsonObject().get("BelongOrg").getAsString();
                        String companyAddress = json.get("Result").getAsJsonObject().get("Address").isJsonNull() ? null : json.get("Result").getAsJsonObject().get("Address").getAsString();
                        String scope = json.get("Result").getAsJsonObject().get("Scope").isJsonNull() ? "" : json.get("Result").getAsJsonObject().get("Scope").getAsString();
//						            		String province = json.get("Result").getAsJsonObject().get("Province").isJsonNull()?"":json.get("Result").getAsJsonObject().get("Province").getAsString();
                        String econKind = json.get("Result").getAsJsonObject().get("EconKind").isJsonNull() ? "" : json.get("Result").getAsJsonObject().get("EconKind").getAsString();
                        String name = json.get("Result").getAsJsonObject().get("Name").isJsonNull() ? "" : json.get("Result").getAsJsonObject().get("Name").getAsString();

                        Map<String, Object> companyMap = new HashMap<>();
                        Map<String, Object> companyDetailMap = new HashMap<>();
                        Map<String, Object> userInfoMap = new HashMap<>();

                        //拆分地址
                        String province = null;
                        String city = null;
                        String area = null;
                        String county = null;
                        String country = null;
                        AddressData addressData = BaiduAddressUtil.parseAddressToCountryProvinceCityCounty(companyAddress);
                        if (addressData != null) {
                            province = addressData.getProvince();
                            city = addressData.getCity();
                            county = addressData.getCounty();
                        }

                        Address address = new Address();
                        address.setId(addressId);
                        address.setAddress(companyAddress);
                        address.setCity(city);
                        address.setProvince(province);
                        address.setCounty(county);
                        address.setUserId(companyId);
                        address.setCreatedBy(loginUserId);
                        address.setCreateDate(new Date());

                        addressService.saveUserAddress(address);

                        companyMap.put("id", companyId);
                        companyMap.put("companyName", name);
                        companyMap.put("representative", representative);
                        companyMap.put("createDate", new Date());
                        companyMap.put("updateDate", new Date());
                        companyMap.put("createdBy", loginUserId);
                        companyMap.put("status", "0");

                        companyMap.put("departmentId", meeting !=null?meeting.getDepartmentId():"");
                        companyMap.put("pcCompanyId", pcCompanyId);


                        companyDetailMap.put("id", companyId);
                        companyDetailMap.put("companyName", name);
                        companyDetailMap.put("socialCredit", social_credit);
                        companyDetailMap.put("registrationCode", registration_code);
                        companyDetailMap.put("organizationCode", organization_code);
                        companyDetailMap.put("manageType", manage_type);
                        companyDetailMap.put("manageScope", scope);
                        companyDetailMap.put("registeredMoney", registered_money);
                        companyDetailMap.put("registrationAuthority", registration_authority);
                        companyDetailMap.put("content", scope);
                        companyDetailMap.put("econKind", econKind);
                        companyDetailMap.put("establishTime", establish_time);
                        companyDetailMap.put("approveDate", approve_date);
                        companyDetailMap.put("termStart", termStart);
                        companyDetailMap.put("operatingPeriod", operating_period);
                        companyDetailMap.put("createDate", new Date());
                        companyDetailMap.put("updateDate", new Date());
                        companyDetailMap.put("createdBy", loginUserId);
                        companyMap.put("qccUpdatedDate", DateUtils.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss"));


                        companyServie.insertCompanyInfo(companyMap);
                        companyServie.insertCompanyDetailInfo(companyDetailMap);
                        userInfoService.changeUserInfo("insert",uname,station,"",companyId,email,"0",null,null,"",mobile,"","","",loginUserId,null);
                        solrService.updateCompanyIndex(companyId);
                    } else {

                        Map<String, Object> companyMap = new HashMap<>();
                        Map<String, Object> companyDetailMap = new HashMap<>();
                        Map<String, Object> userMap = new HashMap<>();
                        Map<String, Object> userMapDetail = new HashMap<>();
                        Map<String, Object> userInfoMap = new HashMap<>();


                        companyMap.put("id", companyId);
                        companyMap.put("companyName", companyName);
                        companyMap.put("createDate", new Date());
                        companyMap.put("updateDate", new Date());
                        companyMap.put("createdBy", loginUserId);
                        companyMap.put("status", "0");
                        companyMap.put("departmentId", meeting !=null?meeting.getDepartmentId():"");
                        companyMap.put("pcCompanyId", pcCompanyId);

                        companyDetailMap.put("id", companyId);
                        companyDetailMap.put("companyName", companyName);
                        companyDetailMap.put("createDate", new Date());
                        companyDetailMap.put("updateDate", new Date());
                        companyDetailMap.put("createdBy", loginUserId);
                        companyMap.put("qccUpdatedDate", DateUtils.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss"));
                        companyMap.put("contactName", uname);
                        companyMap.put("contactPhone", mobile);
                        companyMap.put("status", 0);

                        companyServie.insertCompanyInfo(companyMap);
                        companyServie.insertCompanyDetailInfo(companyDetailMap);

                        userInfoService.changeUserInfo("insert",uname,station,"",companyId,email,"0",null,null,"",mobile,"","","",loginUserId,null);
                        solrService.updateCompanyIndex(companyId);
                    }
                }
            }

            //通过上面的代码，手机号已经绑定到企业了
            Map<String, Object> realUserMap = userService.selectUserByMobile(mobile, null);

            String realUserId = (String)realUserMap.get("id");
           /* String realUserId = (StringUtils.isBlank(insertUserId))?existsUserIdd:insertUserId;*/


           //发送短信给该手机的用户
            if("true".equals(meeting.getSendMessageFlag()) && StringUtils.isNotBlank(meeting.getMobile())){
                SmsApi.sendSmsInTicketAttendMeeting(meeting.getMobile(),mobile,uname,meeting.getMeetingName());
            }


            if(maps != null && maps.size() > 0){


                String orderId = UUID.randomUUID().toString();
                //当金额大于0时才进行支付


                    if (StringUtils.isNotBlank(totalTicketMessage)) {
                        totalTicketMessage = totalTicketMessage.substring(0, totalTicketMessage.lastIndexOf(","));
                        totalTicketMessage += "共" + totoPrice + "分";
                    }

                    Map<String, Object> userOrderMap = new HashMap<>();

                //如果金额大于0就调用微信支付信息
                if(totoPrice > 0){

                    Map<String, Object> weixinUserOrderMap = new HashMap<>();
                    weixinUserOrderMap.put("body", totalTicketMessage);
                    weixinUserOrderMap.put("total_fee", totoPrice);
                    weixinUserOrderMap.put("openId", openId);
                    weixinUserOrderMap.put("NOTIFY_URL", "dbcenter/ticket/savePay");
                    System.out.println("下单openId--------------:"+openId);
                    sortedMap = WeixinPlayUtils.weixinUserOrder(weixinUserOrderMap);
                    //订单号
                    mhtOrderNo = (String)sortedMap.get("mhtOrderNo");

                    userOrderMap.put("id", orderId);
                    //订单要绑在当前登录用户上
                    userOrderMap.put("userId",loginUserId);
                    userOrderMap.put("orderName", totalTicketMessage);
                    userOrderMap.put("mhtOrderNo", mhtOrderNo);
                    userOrderMap.put("orderTime", new Date());
                    userOrderMap.put("mhtOrderAmt", totoPrice);
                    userOrderMap.put("mhtOrderStartTime", WeixinPlayUtils.getTimeStamp());
                    userOrderMap.put("mhtOrderAmt", totoPrice);
                    userOrderMap.put("payStatus", "0");
                    userOrderMap.put("status", 0);
                    userOrderMap.put("meetingId", meetingId);
                    userOrderMap.put("companyId", StringUtils.isBlank(existCompanyId)?companyId:existCompanyId);
                    //被支付人，手机号用户，支付人是当前登录用户
                    userOrderMap.put("payedUserId", realUserId);
                    //推广人
                    userOrderMap.put("userSpreadId", userSpreadId);

                    orderService.saveUserOrder(userOrderMap);

                    System.out.println("===========maps=========="+maps);

                    /*
                     * 开启线程，如果用户15分钟之内没有支付的话，就取消订单，释放库存
                     */
                    OrderOvertimeThread orderOvertimeThread = new OrderOvertimeThread(mhtOrderNo);
                    Thread thread = new Thread(orderOvertimeThread);
                    thread.start();

                }else{

                    mhtOrderNo = WeixinPlayUtils.getOrderNoStr();

                    userOrderMap.put("id", orderId);
                    //订单要绑在当前登录用户上
                    userOrderMap.put("userId",loginUserId);
                    userOrderMap.put("orderName", totalTicketMessage);
                    userOrderMap.put("mhtOrderNo", mhtOrderNo);
                    userOrderMap.put("orderTime", new Date());
                    userOrderMap.put("mhtOrderAmt", totoPrice);
                    userOrderMap.put("mhtOrderStartTime", WeixinPlayUtils.getTimeStamp());
                    userOrderMap.put("mhtOrderAmt", totoPrice);
                    //因为金额为0没有调用微信，在状态中显示支付成功
                    userOrderMap.put("payStatus", "1");
                    userOrderMap.put("payTime", DateUtils.currtime());
                    userOrderMap.put("status", 0);
                    userOrderMap.put("meetingId", meetingId);
                    userOrderMap.put("companyId", StringUtils.isBlank(existCompanyId)?companyId:existCompanyId);
                    //被支付人，手机号用户，支付人是当前登录用户
                    userOrderMap.put("payedUserId", realUserId);
                    userOrderMap.put("userSpreadId", userSpreadId);

                    orderService.saveUserOrder(userOrderMap);

                    System.out.println("===========maps=========="+maps);
                }

                for (Map<String, Object> map : maps) {

                    String ticketId = (String) map.get("id");
                    Integer number = (Integer) map.get("number");

                    Map<String, Object> orderTicketMap = new HashMap<>();
                    orderTicketMap.put("id", UUID.randomUUID().toString());
                    orderTicketMap.put("orderId",orderId);
                    orderTicketMap.put("ticketId", ticketId);
                    orderTicketMap.put("ticketNumber", number);

                    System.out.println("===========map=========="+map);
                    orderService.saveOrderTicket(orderTicketMap);

                    Integer num = ticketService.getTicketSavingByTicketId(ticketId);
                    int saving = num.intValue();
                    int ticketNumberInt = number.intValue();

                    //减少库存
                    if(saving >= ticketNumberInt){
                        ticketService.updateTicketSavingById(ticketId,ticketNumberInt);
                    }else{
                        ticketService.updateTicketSavingById(ticketId,saving);
                    }
                }

                //点击支付后将用户添加到参会人、就算没有支付也会添加到参会人中
                List<UserAttendMeeting> userAttendMeetings = userAttendMeetingService.selectMeetingUserByUserIdAndMeetingId(meetingId,realUserId);
                if(userAttendMeetings != null && userAttendMeetings.size() > 0){

                    UserAttendMeeting userAttendMeeting = userAttendMeetings.get(0);
                    userAttendMeeting.setStatus("已报名");
                    userAttendMeeting.setCompanyId(StringUtils.isBlank(existCompanyId)?companyId:existCompanyId);
                    userAttendMeeting.setUpdateTime(new Date());
                    Integer learnStatusInt = 0;
                    userAttendMeeting.setLearnStatus(learnStatusInt);
                    userAttendMeeting.setUserId(realUserId);
                    userAttendMeeting.setTicketLoginUserId(loginUserId);
                    userAttendMeeting.setMeetingId(meetingId);

                    userAttendMeetingService.updateUserMeeting(userAttendMeeting);

                }else{
                    UserAttendMeeting userAttendMeeting = new UserAttendMeeting();
                    userAttendMeeting.setStatus("已报名");
                    userAttendMeeting.setCompanyId(StringUtils.isBlank(existCompanyId)?companyId:existCompanyId);
                    userAttendMeeting.setUpdateTime(new Date());
                    Integer learnStatusInt = 0;
                    userAttendMeeting.setLearnStatus(learnStatusInt);
                    userAttendMeeting.setUserId(realUserId);
                    userAttendMeeting.setTicketLoginUserId(loginUserId);
                    userAttendMeeting.setMeetingId(meetingId);

                    userAttendMeetingService.saveUserMeeting(userAttendMeeting);
                }

                ResponseDbCenter responseDbCenter = new ResponseDbCenter();

                if(sortedMap == null){
                    sortedMap = new TreeMap<Object,Object>();
                    sortedMap.put("mhtOrderNo", mhtOrderNo);//订单号
                    responseDbCenter.setResModel(sortedMap);
                }else{
                    responseDbCenter.setResModel(sortedMap);
                }
                return responseDbCenter;

            }else{

                List<UserAttendMeeting> userAttendMeetings = userAttendMeetingService.selectMeetingUserByUserIdAndMeetingId(meetingId,realUserId);
                if(userAttendMeetings != null && userAttendMeetings.size() > 0){

                    UserAttendMeeting userAttendMeeting = userAttendMeetings.get(0);
                    userAttendMeeting.setStatus("已报名");
                    userAttendMeeting.setCompanyId(StringUtils.isBlank(existCompanyId)?companyId:existCompanyId);
                    userAttendMeeting.setUpdateTime(new Date());
                    Integer learnStatusInt = 0;
                    userAttendMeeting.setLearnStatus(learnStatusInt);
                    userAttendMeeting.setUserId(realUserId);
                    userAttendMeeting.setTicketLoginUserId(loginUserId);
                    userAttendMeeting.setMeetingId(meetingId);

                    userAttendMeetingService.updateUserMeeting(userAttendMeeting);

                }else{
                    UserAttendMeeting userAttendMeeting = new UserAttendMeeting();
                    userAttendMeeting.setStatus("已报名");
                    userAttendMeeting.setCompanyId(StringUtils.isBlank(existCompanyId)?companyId:existCompanyId);
                    userAttendMeeting.setUpdateTime(new Date());
                    Integer learnStatusInt = 0;
                    userAttendMeeting.setLearnStatus(learnStatusInt);
                    userAttendMeeting.setUserId(realUserId);
                    userAttendMeeting.setTicketLoginUserId(loginUserId);
                    userAttendMeeting.setMeetingId(meetingId);

                    userAttendMeetingService.saveUserMeeting(userAttendMeeting);
                }

                ResponseDbCenter responseDbCenter = new ResponseDbCenter();
                return responseDbCenter;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
        }
    }


    class OrderOvertimeThread implements Runnable{

        private  String mhtOrderNo;

        public OrderOvertimeThread(String mhtOrderNo){
            this.mhtOrderNo = mhtOrderNo;
        }

        public void run() {
            try {
                //睡觉15分钟
                Thread.sleep(1000*60*15);

                //如果15分钟没有支付的话就释放库存
                Map<String, String> csOrder = ticketService.selectUserOrderByOrderNo(mhtOrderNo);
                String payStatus = csOrder.get("payStatus");
                String meetingId = csOrder.get("meetingId");
                String payedUserId = csOrder.get("payedUserId");
                String payAgain = csOrder.get("payAgain");
                //如果订单没有被支付，并且该订单没有被重新支付
                if("0".equals(payStatus) && !"2".equals(payAgain)){

                    List<Map<String,Object>> maps = ticketService.selectTicketNumberByOderId((String) csOrder.get("id"));
                    for(Map<String,Object> map : maps){
                        String ticketId = (String)map.get("ticketId");
                        Integer ticketNumber = (Integer)map.get("ticketNumber");

                        int ticketNumberInt = ticketNumber.intValue();
                        ticketService.updateTicketSavingById(ticketId,-ticketNumberInt);

                        csOrder.put("payTime", DateUtils.currtime());
                        csOrder.put("payStatus","2");
                        ticketService.updateUserTicketById(csOrder);

                    }

                    //超时如果这个人有参会那就取消参会
                    List<UserAttendMeeting> userAttendMeetings = userAttendMeetingService.selectMeetingUserByUserIdAndMeetingId(meetingId,payedUserId);
                    if(userAttendMeetings != null && userAttendMeetings.size() > 0){
                        userAttendMeetingService.deleteMeetingUserByIds("id ='"+userAttendMeetings.get(0).getId()+"'");
                    }

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    //支付成功后回调
    @ResponseBody
    @RequestMapping("/savePay")
    public   void savePay(HttpServletRequest req,HttpServletResponse res,@RequestBody String result) throws Exception{



        System.out.println("---------------------支付成功后回调:"+result);
        String result_code = result.substring(result.indexOf("<result_code><![CDATA[")+22, result.indexOf("]]></result_code>"));//支付是否成功
        String openid = result.substring(result.indexOf("<openid><![CDATA[")+17, result.indexOf("]]></openid>"));//用户openid
        String out_trade_no = result.substring(result.indexOf("<out_trade_no><![CDATA[")+23, result.indexOf("]]></out_trade_no>"));//自己提交给微信的订单号
        String transaction_id = result.substring(result.indexOf("<transaction_id><![CDATA[")+25, result.indexOf("]]></transaction_id>"));//微信的订单号

        System.out.println("结果："+result_code+"  "+openid+" "+out_trade_no+" "+transaction_id);
        String xml = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA]></return_msg></xml>";

        try{

            if("FAIL".equals(result_code)){
                xml = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[支付失败]]></return_msg></xml>";
            }else if(StringUtils.isBlank(result_code) || StringUtils.isBlank(openid) ||  StringUtils.isBlank(out_trade_no) || StringUtils.isBlank(transaction_id)){
                xml = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[参数格式校验错误]]></return_msg></xml>";
            }else{

                Map<String, String> csOrder = ticketService.selectUserOrderByOrderNo(out_trade_no);

                csOrder.put("channelOrderNo", transaction_id);
                csOrder.put("payTime", DateUtils.currtime());
                csOrder.put("payStatus","1");
                ticketService.updateUserTicketById(csOrder);

               /* List<Map<String,Object>> maps = ticketService.selectTicketNumberByOderId((String) csOrder.get("id"));
                for(Map<String,Object> map : maps){
                    *//*
                    ot.id as orderTecketId,ot.orderId,ot.ticketId,ot.ticketNumber,concat(t.ticketType,'*',ot.ticketNumber) as ticketNumberStr,ot.getTicketNumber,
                     *//*
                    String ticketId = (String)map.get("ticketId");
                    Integer ticketNumber = (Integer)map.get("ticketNumber");
                    Integer saving = (Integer)map.get("saving");

                    int ticketNumberInt = ticketNumber.intValue();
                    int savingInt = saving.intValue();

                    if(savingInt >= ticketNumberInt){
                        ticketService.updateTicketSavingById(ticketId,ticketNumberInt);
                    }else{
                        ticketService.updateTicketSavingById(ticketId,savingInt);
                    }
                }*/

               /* if(csOrder != null && csOrder.size() > 0){

                    //被支付人添加到会务中
                    String payedUserId = (String)csOrder.get("payedUserId");
                    String meetingId = (String)csOrder.get("meetingId");
                    String companyId = (String)csOrder.get("companyId");
                    String userId = (String)csOrder.get("userId");

                    List<UserAttendMeeting> userAttendMeetings = userAttendMeetingService.selectMeetingUserByUserIdAndMeetingId(meetingId,payedUserId);

                    if(userAttendMeetings != null && userAttendMeetings.size() > 0){

                        UserAttendMeeting userAttendMeeting = userAttendMeetings.get(0);

                        userAttendMeeting.setStatus("已报名");
                        userAttendMeeting.setCompanyId(companyId);
                        userAttendMeeting.setUpdateTime(new Date());
                        Integer learnStatusInt = 0;
                        userAttendMeeting.setLearnStatus(learnStatusInt);
                        //当前登录人、支付人
                        userAttendMeeting.setTicketLoginUserId(userId);
                        //被支付人
                        userAttendMeeting.setUserId(payedUserId);
                        userAttendMeeting.setMeetingId(meetingId);


                        userAttendMeetingService.updateUserMeeting(userAttendMeeting);
                    }else{

                        UserAttendMeeting userAttendMeeting = new UserAttendMeeting();
                        userAttendMeeting.setStatus("已报名");
                        userAttendMeeting.setCompanyId(companyId);
                        userAttendMeeting.setUpdateTime(new Date());
                        Integer learnStatusInt = 0;
                        userAttendMeeting.setLearnStatus(learnStatusInt);
                        userAttendMeeting.setTicketLoginUserId(userId);
                        userAttendMeeting.setUserId(payedUserId);
                        userAttendMeeting.setMeetingId(meetingId);


                        userAttendMeetingService.saveUserMeeting(userAttendMeeting);
                    }
                }*/
            }
            res.getOutputStream().write(xml.getBytes());

        }catch (Exception e){
            e.printStackTrace();
            res.getOutputStream().write(xml.getBytes());
        }
    }

    /**
     *
     * @author: xiaoz
     * @2017年7月13日
     * @功能描述:查询所有顾问
     * @param req
     * @return
     */
    @ApiOperation(value = "pc端查询票务信息")
    @ResponseBody
    @RequestMapping(value = "/selectTicketsByContent",method = RequestMethod.POST)
    public ResponseDbCenter selectTicketsByContent(HttpServletRequest req,HttpServletResponse rsp,
                                                   @ApiParam(value="手机号或者姓名",required = false) @RequestParam(required = false) String content,
                                                   @ApiParam(value="会务ID",required = false) @RequestParam(required = false) String meetingId,
                                                   @ApiParam(value="每页条数",required = false) @RequestParam(required = false) String pageSize,
                                                   @ApiParam(value="当前页",required = false) @RequestParam(required = false) String currentPage,
                                                   @ApiParam(value="票务类型",required = false) @RequestParam(required = false) String ticketType,
                                                   @ApiParam(value="状态",required = false) @RequestParam(required = false) String payStatus,
                                                   @ApiParam(value="分组",required = false) @RequestParam(required = false) String mgroup){


        if(StringUtils.isBlank(meetingId)){
            return ResponseConstants.MISSING_PARAMTER;
        }

        if(StringUtils.isBlank(pageSize) || StringUtils.isBlank(currentPage)){
            pageSize = "10";
            currentPage = "1";
        }


        int  pageSizeInt = Integer.parseInt(pageSize);
        int  currentPageInt = (Integer.parseInt(currentPage)-1)*pageSizeInt;
        Integer totalRows = 0;

        try {

            String payStatusStr = null;

            if("已付款".equals(payStatus)){
                payStatusStr = "1";
            }else if("待付款".equals(payStatus)){
                payStatusStr = "0";
            }else if("已取消".equals(payStatus)){
                payStatusStr = "2";
            }

            List<Map<String,Object>> maps = ticketService.selectTicketsByContent(meetingId,content,currentPageInt,pageSizeInt,payStatusStr,mgroup,ticketType);
            totalRows = ticketService.selectTicketsTotalByContent(meetingId,content,payStatusStr,mgroup,ticketType);
            Map<String,Object> ticketNumberMap = new HashMap<>();
            for(Map<String,Object> map : maps){
                String orderId = (String)map.get("orderId");
//                List<Map<String,Object>> orderTickets = ticketService.selectTicketNumberByOderId(orderId);
                List<Map<String,Object>> orderTickets = ticketService.selectorderTicketByOderId(orderId);
                map.put("orderTickets",orderTickets);
                /*StringBuffer sb = new StringBuffer();
                StringBuffer sb1 = new StringBuffer();

                for(Map<String,Object> orderTicketMap : orderTickets){
                    if(orderTicketMap != null && orderTicketMap.size() > 0 && orderTicketMap.get("ticketNumberStr") != null){
                        String ticketNumberStr = (String)orderTicketMap.get("ticketNumberStr");
                        sb.append(ticketNumberStr+" ");
                    }
                    if(orderTicketMap != null && orderTicketMap.size() > 0 && orderTicketMap.get("getTicketNumberStatus") != null){
                        String getTicketNumberStatus = (String)orderTicketMap.get("getTicketNumberStatus");
                        sb1.append(getTicketNumberStatus+" ");
                    }
                }
                if(StringUtils.isNotBlank(sb.toString())){
                    String ticketNumberStrr =  sb.substring(0,sb.lastIndexOf(" "));
                    map.put("ticketNumberStr",ticketNumberStrr);
                }
                if(StringUtils.isNotBlank(sb1.toString())){
                    String getTicketNumberStatus =  sb1.substring(0,sb1.lastIndexOf(" "));
                    map.put("getTicketNumberStatus",getTicketNumberStatus);
                }*/
            }

            ResponseDbCenter responseDbCenter = new ResponseDbCenter();
            responseDbCenter.setResModel(maps);
            responseDbCenter.setTotalRows(totalRows.toString());
            return responseDbCenter;

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseConstants.FUNC_SERVERERROR;
        }
    }


    /**
     *
     * @author: xiaoz
     * @2017年7月13日
     * @功能描述:pc端确定领票
     * @param req
     * @return
     */
    @ApiOperation(value = "pc端查询票务信息")
    @ResponseBody
    @RequestMapping(value = "/ensureGetTickets",method = RequestMethod.POST)
    public ResponseDbCenter ensureGetTickets(HttpServletRequest req,HttpServletResponse rsp,
                                                   @ApiParam(value="orderTecketList集合orderTecketId,getTicketNumber(已经取得票)",
                                                           required = false) @RequestParam(required = false) String orderTecketList) throws  Exception{

        if(StringUtils.isBlank(orderTecketList)){
            return ResponseConstants.MISSING_PARAMTER;
        }

        try {

            List<Map<String,Object>> maps = (List<Map<String,Object>>)JSON.parseObject(orderTecketList,Object.class);

            for(Map<String,Object> map : maps){
                ticketService.ensureGetTickets(map);
            }

            ResponseDbCenter responseDbCenter = new ResponseDbCenter();
            return responseDbCenter;

        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
        }
    }


    /**
     *
     * @author: xiaoz
     * @2017年7月13日
     * @功能描述:获得支付后信息
     * @param req
     * @return
     */
    @ApiOperation(value = "获得支付后信息")
    @ResponseBody
    @RequestMapping(value = "/selectPayMessageAfterPay",method = RequestMethod.GET)
    public ResponseDbCenter selectPayMessageAfterPay(HttpServletRequest req,HttpServletResponse rsp,
                                             @ApiParam(value="会务ID",required = false) @RequestParam(required = false) String meetingId,
                                             @ApiParam(value="订单号",required = false) @RequestParam(required = false) String mhtOrderNo
                                                     ) throws  Exception{

        if(StringUtils.isBlank(meetingId) || StringUtils.isBlank(mhtOrderNo)){
            return ResponseConstants.MISSING_PARAMTER;
        }

        try {

            Map<String,Object> map = ticketService.selectPayMessageAfterPay(meetingId,mhtOrderNo);
            if(map != null ){
                String orderId = (String)map.get("id");
                List<Map<String,Object>> orderTickets = ticketService.selectorderTicketByOderId(orderId);
                map.put("ticketNumbers",orderTickets);
            }

            ResponseDbCenter responseDbCenter = new ResponseDbCenter();
            responseDbCenter.setResModel(map);
            return responseDbCenter;

        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
        }
    }

    /**
     *
     * @author: xiaoz
     * @2017年7月13日
     * @功能描述:获得我的支付信息
     * @param req
     * @return
     */
    @ApiOperation(value = "获得我的支付信息")
    @ResponseBody
    @RequestMapping(value = "/selectUserTicketsByToken",method = RequestMethod.GET)
    public ResponseDbCenter selectUserTicketsByToken(HttpServletRequest req,HttpServletResponse rsp,
                                             @ApiParam(value="token",required = false) @RequestParam(required = false) String token,
                                             @ApiParam(value="会务ID",required = false) @RequestParam(required = false) String meetingId){



        if(StringUtils.isBlank(token)){
            return ResponseConstants.MISSING_PARAMTER;
        }

       String userId = (String) req.getSession().getAttribute(token);
//       String userId = "761";

        /*
        select uo.id,u.id as userId,uo.orderName,uo.mhtOrderNo,uo.mhtOrderAmt/100 as mhtOrderAmt,uo.mhtOrderStartTime,uo.channelOrderNo,
        DATE_FORMAT(uo.orderTime,'%Y-%m-%d %H:%i:%s') as orderTime,mu.companyId,uo.payedUserId,
        uo.payTime,uo.payConsumerId,uo.payStatus,uo.meetingId,u.uname,u.mobile,m.meetingName,m.startTime,m.endTime,m.place,m.province,m.city,m.county,m.meetingImage
        from  (select * from dc_meeting_user where meetingId= #{meetingId}) mu left join dc_user  u
        on mu.userId = u.id LEFT JOIN dc_meeting m on mu.meetingId = m.id left join (select * from dc_user_order where payAgain !=2) uo on uo.meetingId = m.id and uo.payedUserId = mu.userId
        where mu.meetingId = #{meetingId} and mu.ticketLoginUserId = #{userId}
         */

        try {

            List<Map<String,Object>> userTicketMaps = ticketService.selectUserTicketsByUserId(userId,meetingId);

            for(Map<String,Object> map : userTicketMaps){

                String orderId = (String)map.get("id");
                List<Map<String,Object>> orderTickets = ticketService.selectorderTicketByOderId(orderId);
                map.put("ticketNumbers",orderTickets);
            }

            ResponseDbCenter responseDbCenter = new ResponseDbCenter();
            responseDbCenter.setResModel(userTicketMaps);
            return responseDbCenter;

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseConstants.FUNC_SERVERERROR;
        }
    }

    /**
     *
     * @author: xiaoz
     * @2017年7月13日
     * @功能描述:重新支付、立即支付
     * @param req
     * @return
     */
    @ApiOperation(value = "重新支付")
    @ResponseBody
    @RequestMapping(value = "/payAgain",method = RequestMethod.POST)
    public synchronized ResponseDbCenter payAgain(HttpServletRequest req,HttpServletResponse rsp,
                                                     @ApiParam(value="ticketNumbers",required = false) @RequestParam(required = false) String ticketNumbers,
                                                     @ApiParam(value="会务ID",required = false) @RequestParam(required = false) String meetingId,
                                                     @ApiParam(value="token",required = false) @RequestParam(required = false) String token,
                                                     @ApiParam(value="手机号对应的用户",required = false) @RequestParam(required = false) String payedUserId,
                                                     @ApiParam(value="企业ID",required = false) @RequestParam(required = false) String companyId,
                                                     @ApiParam(value="订单号",required = false) @RequestParam(required = false) String mhtOrderNo,
                                                  @ApiParam(value="推广人",required = false) @RequestParam(required = false) String userSpreadId){



        if(StringUtils.isBlank(token)){
            return ResponseConstants.MISSING_PARAMTER;
        }

        String userIdd = (String) req.getSession().getAttribute(token);

        String openId = userService.selectUserById(userIdd).get("weixin").toString();

        SortedMap<Object, Object> sortedMap = null;

        try {

            Map<String, String> csOrder = ticketService.selectUserOrderByOrderNo(mhtOrderNo);
            String payStatus = csOrder.get("payStatus");
            if("2".equals(payStatus)){
                return ResponseConstants.USER_ORDER_CANCELED;
            }


            List<Map<String, Object>> maps = (List<Map<String, Object>>) JSON.parseObject(ticketNumbers,Object.class);

            boolean ticketFlag = false;
            boolean numberFlag = false;
            long totoPrice = 0;
            String totalTicketMessage = "";
//            String mhtOrderNo = "";
            for (Map<String, Object> map : maps) {

                String ticketId = (String) map.get("id");
                Integer number = (Integer) map.get("number");

                /*
                 * 重新支付，以前的订单就会作废掉，库存隔15分钟后就会减掉，现在要将库存加上来，
                 * 因为订单换了，所以要重新走一趟减少库存，15分钟定时取消、释放库存
                 */
                int ticketNumberInt = number.intValue();
                ticketService.updateTicketSavingById(ticketId,-ticketNumberInt);

                //查出库存
                Integer num = ticketService.getTicketSavingByTicketId(ticketId);
               /* if (num == null || num.intValue() == 0) {
                    return ResponseConstants.TICKET_HAS_SELL_OUT;
                }*/

                int saving = num.intValue();
                int numberInt = number.intValue();

                if (numberInt > saving) {
                    return ResponseConstants.TICKET_NOT_ENOUGHT;
                }

                if(numberInt > 0){
                    numberFlag = true;
                }

                if ((0 == saving)) {
                    continue;
                }


                Map<String, Object> ticketMap = ticketService.selectTicketById(ticketId);

                String ticketType = (String) ticketMap.get("ticketType");
                String price = (String) ticketMap.get("price");

                double doublePrice = Double.valueOf(price);

                totoPrice += doublePrice * (number.intValue());

                totalTicketMessage += ticketType + number.intValue() + "张,";

                ticketFlag = true;
            }

            if(!numberFlag){
                return ResponseConstants.TICKET_NUMBER_ZERO;
            }else if (!ticketFlag) {
                return ResponseConstants.TICKET_HAS_SELL_OUT;
            } else {

                String orderId = UUID.randomUUID().toString();
                if(totoPrice > 0){

                    Map<String, Object> userOrderMap = new HashMap<>();

                    Map<String, Object> weixinUserOrderMap = new HashMap<>();
                    weixinUserOrderMap.put("body", totalTicketMessage);
                    weixinUserOrderMap.put("total_fee", totoPrice);
                    weixinUserOrderMap.put("openId", openId);
                    weixinUserOrderMap.put("NOTIFY_URL", "dbcenter/ticket/savePay");
                    System.out.println("下单openId--------------:" + openId);
                    sortedMap = WeixinPlayUtils.weixinUserOrder(weixinUserOrderMap);
                    //订单号
                    String mhtOrderNoNo = (String) sortedMap.get("mhtOrderNo");

                    userOrderMap.put("id", orderId);
                    userOrderMap.put("userId", userIdd);

                    //加上企业Id
                    userOrderMap.put("companyId", companyId);
                    userOrderMap.put("orderName", totalTicketMessage);
                    userOrderMap.put("mhtOrderNo", mhtOrderNoNo);
                    userOrderMap.put("mhtOrderAmt", totoPrice);
                    userOrderMap.put("mhtOrderStartTime", WeixinPlayUtils.getTimeStamp());
                    userOrderMap.put("orderTime", new Date());
                    userOrderMap.put("payStatus", "0");
                    userOrderMap.put("status", 0);
                    userOrderMap.put("meetingId", meetingId);
                    userOrderMap.put("companyId", companyId);
                    userOrderMap.put("payedUserId", payedUserId);
                    userOrderMap.put("userSpreadId", userSpreadId);


                    orderService.saveUserOrder(userOrderMap);

                    System.out.println("===========maps==========" + maps);
                    //把新的订单号传入到定时任务中
                    OrderOvertimeThread orderOvertimeThread = new OrderOvertimeThread(mhtOrderNoNo);
                    Thread thread = new Thread(orderOvertimeThread);
                    thread.start();
                }

                for (Map<String, Object> map : maps) {

                    String ticketId = (String) map.get("id");
                    Integer number = (Integer) map.get("number");

                    Integer num = ticketService.getTicketSavingByTicketId(ticketId);
                    int saving = num.intValue();
                    int ticketNumberInt = number.intValue();

                    //减少库存
                    if(saving >= ticketNumberInt){
                        ticketService.updateTicketSavingById(ticketId,ticketNumberInt);
                    }else{
                        ticketService.updateTicketSavingById(ticketId,saving);
                    }

                    Map<String, Object> orderTicketMap = new HashMap<>();
                    orderTicketMap.put("id", UUID.randomUUID().toString());
                    orderTicketMap.put("orderId", orderId);
                    orderTicketMap.put("ticketId", ticketId);
                    orderTicketMap.put("ticketNumber", number);
                    System.out.println("===========map==========" + map);

                    orderService.saveOrderTicket(orderTicketMap);

                }

                orderService.deleteOrderBymhtOrderNoByPayAgain(mhtOrderNo);

                ResponseDbCenter responseDbCenter = new ResponseDbCenter();
                responseDbCenter.setResModel(sortedMap);
                return responseDbCenter;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseConstants.FUNC_SERVERERROR;
        }
    }

    /**
     *
     * @author: xiaoz
     * @2017年7月13日
     * @功能描述:获得我的支付信息
     * @param req
     * @return
     */
    @ApiOperation(value = "取消订单")
    @ResponseBody
    @RequestMapping(value = "/cancelOrder",method = RequestMethod.GET)
    public ResponseDbCenter cancelOrder(HttpServletRequest req,HttpServletResponse rsp,
                                                     @ApiParam(value="订单ID",required = false) @RequestParam(required = false) String id)
                                                            throws Exception{


        try {

            Map<String, String> order = ticketService.selectUserOrderById(id);
            String meetingId = order.get("meetingId");
            String payedUserId = order.get("payedUserId");

            Map<String,String> csOrder = new HashMap<>();

            csOrder.put("id",id);
            csOrder.put("payTime", DateUtils.currtime());
            csOrder.put("payStatus","2");
            ticketService.updateUserTicketById(csOrder);

            List<Map<String,Object>> maps = ticketService.selectTicketNumberByOderId(id);
            for(Map<String,Object> map : maps){
                String ticketId = (String)map.get("ticketId");
                Integer ticketNumber = (Integer)map.get("ticketNumber");
                Integer saving = (Integer)map.get("saving");

                int ticketNumberInt = ticketNumber.intValue();
                ticketService.updateTicketSavingById(ticketId,-ticketNumberInt);

                csOrder.put("payTime", DateUtils.currtime());
                csOrder.put("payStatus","2");
                ticketService.updateUserTicketById(csOrder);

            }

            List<UserAttendMeeting> userAttendMeetings = userAttendMeetingService.selectMeetingUserByUserIdAndMeetingId(meetingId,payedUserId);
            if(userAttendMeetings != null && userAttendMeetings.size() > 0){
                userAttendMeetingService.deleteMeetingUserByIds("id ='"+userAttendMeetings.get(0).getId()+"'");
            }


            ResponseDbCenter responseDbCenter = new ResponseDbCenter();
            return responseDbCenter;

        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
        }
    }



}
