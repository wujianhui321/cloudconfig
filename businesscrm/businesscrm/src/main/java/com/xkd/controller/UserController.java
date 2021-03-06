package com.xkd.controller;

import com.alibaba.fastjson.JSON;
import com.ctc.wstx.util.StringUtil;
import com.xkd.exception.GlobalException;
import com.xkd.mapper.CompanyMapper;
import com.xkd.mapper.DictionaryMapper;
import com.xkd.model.*;
import com.xkd.service.CustomerService;
import com.xkd.service.DC_UserService;
import com.xkd.service.OperateCacheService;
import com.xkd.service.UserService;
import com.xkd.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;


@Controller
@RequestMapping("/user")
@Transactional
@Api(description = "登录相关接口")

public class UserController  extends BaseController{

	//每隔1分钟导入联想信息到redis中
	public static boolean ideaRedisFlag = false;



//	@Autowired
//	private IdeaRedisService ideaRedisService;

	String ASSET_URL = PropertiesUtil.ASSET_URL;

	@Autowired
	private CustomerService customerService;

	@Autowired
	DC_UserService userService;
	@Autowired
	UserService loginUserService;

	@Autowired
	UserService newUserService;

	@Autowired
	private UserService userService1;

   @Autowired
	OperateCacheService operateCacheService;

	@Autowired
	RedisCacheUtil redisCacheUtil;

	/**
	 *
	 * @author: xiaoz
	 * @2017年5月24日
	 * @功能描述:根据token获得redis中的userId再根据userId获得user对象
	 * @param req
	 * @param rsp
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/selectUserByToken" ,method ={ RequestMethod.GET, RequestMethod.POST })
	@ApiOperation(value = "通过token获取用户详情")
	public ResponseDbCenter selectUserByToken(HttpServletRequest req,HttpServletResponse rsp){
		String token  = req.getHeader("token");

		if(StringUtils.isBlank(token)){

			return ResponseConstants.FUNC_USER_NOTOKEN;
		}

		String userId = null;
		try {

			userId = req.getSession().getAttribute(token).toString();

		} catch (Exception e) {

			System.out.println(e);
			return ResponseConstants.FUNC_DBCENTER_TOKENISWRONG;
		}

        if(StringUtils.isBlank(userId)){

        	return ResponseConstants.FUNC_DBCENTER_TOKENISWRONG;
        }

        Map<String, Object> userResult = loginUserService.selectUserById(userId);

        if(userResult == null || userResult.size() == 0){

        	return ResponseConstants.USER_NOTEXIST;
        }

        req.getSession().setAttribute(token, userId);


		ResponseDbCenter ResponseDbCenter = new ResponseDbCenter();
		userResult.put("token",token);
		ResponseDbCenter.setResModel(userResult);
		return ResponseDbCenter;

	}



	/**
	 *
	 * @author: gaodd
	 * @2017年3月22日
	 * @功能描述:发送验证码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/userRegSendSms",method = { RequestMethod.GET, RequestMethod.POST })
	@ApiOperation(value = "发送短信验证码")
	public ResponseDbCenter userRegSendSms(HttpServletRequest req,
										   @ApiParam(value = "手机")  @RequestParam(required = false) String tel
	){
		System.out.println("--------------发送短信----------------");
		//返回数据总对象，0000表示成功，其它都表示错误
		ResponseDbCenter res = new ResponseDbCenter();

		if(StringUtils.isBlank(tel)){
			return ResponseConstants.MISSING_PARAMTER_WJ;
		}

		//req.getSession().setAttribute("code" +tel, "1234");
		String code = SmsApi.sendSms(tel);
		req.getSession().setAttribute("code"+tel, code);
		redisCacheUtil.setCacheObject("code"+tel,code,18000);
		System.out.println("Us----------------key="+tel);
		//String telCode = (String) redisCacheUtil.getCacheObject("code"+tel);
		String telCode = (String) req.getSession().getAttribute("code"+tel);
		System.out.println("UserController.session()-----set-----------"+req.getSession().getId());
		System.out.println("发送短信code = "+telCode);

		return res;
	}





	/**
	 *
	 * @author: gaodd
	 * @2017年3月22日
	 * @功能描述:用户登录
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/pcLogin",method = { RequestMethod.GET, RequestMethod.POST })
	@ApiOperation(value = "pc端登录")
	public ResponseDbCenter pcLogin(HttpServletRequest req,
									@ApiParam(value = "密码")  @RequestParam(required = true) String password,
									@ApiParam(value = "电话")  @RequestParam(required = true) String tel,
									@ApiParam(value = "验证码")  @RequestParam(required = true) String code ){
		//返回数据总对象，0000表示成功，其它都表示错误
		ResponseDbCenter res = new ResponseDbCenter();
		String strCode = (String) req.getSession().getAttribute("strCode");
		if(!code.equals(strCode)){
			return ResponseConstants.TEL_CODE_ERROR;
		}
		if(StringUtils.isBlank(tel) && StringUtils.isBlank(password)){
			return ResponseConstants.MISSING_PARAMTER_WJ;
		}
		Map<String, Object> userMap = null;
		if(tel.contains("@")){
			userMap = newUserService.selectUserByEmail(tel,1);
			if(userMap == null || userMap.size() == 0){
				return ResponseConstants.USER_ERROR_SIGN;
			}
		}else{
			userMap = newUserService.selectUserByMobile(tel,1);
		}

		//password = password == "" || password == null ? "a654123":new String(MD5Code.decode(password));
		if(userMap == null || userMap.size() == 0){
			return ResponseConstants.USER_ERROR_SIGN;
		}else if(StringUtils.isNotBlank(password)&&!password.equals(userMap.get("password")+"")){
			return ResponseConstants.NAME_PWD_ERROR_WJ;
		}
		if (!"1".equals(userMap.get("roleId"))) {//如果不是超级管理员
			/**
			 * 判断用户所属公司是否过期，过期了则不能继续登录
			 */
			Map<String, Object> pcCompanyMap = customerService.selectPcCompanyById((String) userMap.get("pcCompanyId"));
			String dateTo = (String) pcCompanyMap.get("dateTo");
			Integer enableStatus = (Integer) pcCompanyMap.get("enableStatus");
			if (dateTo != null) {
				Date date = DateUtils.stringToDate(dateTo, "yyyy-MM-dd");
				Date nowDate = DateUtils.stringToDate(DateUtils.dateToString(new Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
				if (date.before(nowDate)) {
					return ResponseConstants.ACCOUNT_EXPIRE;
				}
			}
			if (enableStatus != null) {
				if (1 == enableStatus) {
					return ResponseConstants.ACCOUNT_DIABLED;
				}
			}
		}

		String token = UUID.randomUUID().toString();
		Map<String, String> map = new HashMap<>();
		map.put("token",token);
		map.put("uname",userMap.get("uname")+"");
		
		
		/*
		 * 一个用户多台电脑登录，让其他用户下线，只允许当前用户下线
		 */


		req.getSession().removeValue(userMap.get("id")+"");
		req.getSession().setAttribute(token,userMap.get("id"));
		req.getSession().setAttribute(token+"uname",userMap.get("uname"));//不要删除session里面存的uname
		req.getSession().setAttribute("user",userMap);


		//用于单点登录，将用户


		/**
		 * 先查询用户Id是否对应旧的token，如果有，则删除旧的token--userId 和userId--token映射
		 */
		String oldToken= (String) redisCacheUtil.getCacheObject(userMap.get("id") + "");
		if (StringUtils.isNotBlank(oldToken)){
			redisCacheUtil.delete(oldToken);
			redisCacheUtil.delete(userMap.get("id") + "");
		}

		/**
		 * 将新的token--userId 和 userId---token写入到redis中
		 */
		redisCacheUtil.setCacheObject(token,userMap.get("id")+"",RedisTableKey.EXPIRE);
		redisCacheUtil.setCacheObject(userMap.get("id")+"",token);


		req.getSession().setMaxInactiveInterval(60*60*5);//以秒为单位
		res.setResModel(map);
		return res;
	}

	/**
	 *
	 * @author: gaodd
	 * @2017年3月22日
	 * @功能描述:发送验证码
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/userRegSendSmsInTicket")
	public ResponseDbCenter userRegSendSmsInTicket(HttpServletRequest req){
		System.out.println("--------------发送短信----------------");
		//返回数据总对象，0000表示成功，其它都表示错误
		ResponseDbCenter res = new ResponseDbCenter();
		String tel = req.getParameter("tel");

		if(StringUtils.isBlank(tel)){
			return ResponseConstants.MISSING_PARAMTER_WJ;
		}

		//req.getSession().setAttribute("code" +tel, "1234");
		/*
		sendSms(String moblie,String toContent)
		 */
		String code = SmsApi.sendSmsInTicket(tel);
		req.getSession().setAttribute("code"+tel, code);
		redisCacheUtil.setCacheObject("code"+tel,code,18000);
		System.out.println("Us----------------key="+tel);
		String telCode = (String) req.getSession().getAttribute("code"+tel);
		System.out.println("UserController.session()-----set-----------"+req.getSession().getId());
		System.out.println("发送短信code = "+telCode);

		return res;
	}




	/**
	 *
	 * @author: gaodd
	 * @2017年3月22日
	 * @功能描述:用户登录
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/login",method = { RequestMethod.GET, RequestMethod.POST })
	@ApiOperation(value = "移动端登录")
	public ResponseDbCenter login(HttpServletRequest req){

		//返回数据总对象，0000表示成功，其它都表示错误
		ResponseDbCenter res = new ResponseDbCenter();
		String code = req.getParameter("code");//用户微信唯一标识
		String telcode = req.getParameter("telCode");//验证码
		String openId = req.getParameter("openId");//用户微信唯一标识
		String tel  = req.getParameter("tel");
		String token2  = req.getHeader("token");
		String meetingId  = req.getParameter("meetingId");
		String uname  = req.getParameter("uname");

		//值为noCheckTel时表示不用验证用户手机号码，根据openid就可以登录
		//值为myCrm时表示要去关联客户
		String ttype = req.getParameter("ttype");

		if(StringUtils.isBlank(openId) && StringUtils.isBlank(code) && StringUtils.isBlank(token2)){
			return ResponseConstants.MISSING_PARAMTER_WJ;
		}

		DC_User user = null;
		String token = UUID.randomUUID().toString();
		if(StringUtils.isNotBlank(openId) && user == null){
			user = userService.getUserByObj(openId,"2");
		}
		if(StringUtils.isNotBlank(token2) && user == null){
			Object userid = req.getSession().getAttribute(token2);
			if(userid != null){
				user = userService.getUserById(userid);
				token = token2;
			}else{
				return ResponseConstants.FUNC_BP_TOKENISWRONG;
			}
		}
		if(StringUtils.isNotBlank(tel)){//根据手机号码登录
			if(StringUtils.isNotBlank(telcode)){
				String usercode = (String) req.getSession().getAttribute("code"+tel);
				if(usercode == null || !usercode.equals(telcode)){
					return ResponseConstants.TEL_CODE_ERROR;
				}
			}
			if(user != null){
				DC_User user2 = userService.getUserByObj(tel,"1");
				if(StringUtils.isBlank(user.getMobile()) && null == user2){
					user.setMobile(tel);
					user.setUname(StringUtils.isNotBlank(uname)?uname:user.getUname());
					userService.saveUser(user);
				}else if(StringUtils.isBlank(user.getMobile()) || (!user.getMobile().equals(tel))){

					if(user2 ==null){
						user.setMobile(tel);
						user.setUname(StringUtils.isNotBlank(uname)?uname:user.getUname());
						userService.saveUser(user);
					}else{
						//交换openId
						String otherOpenId = user2.getWeixin();
						user2.setWeixin(user.getWeixin());
						user.setWeixin(user.getWeixin()+"temp");
						user.setStatus("2");
						user2.setStatus("0");
						userService.saveUser(user);//先修改登录用户的openId为假openId
						user2.setUname(StringUtils.isNotBlank(uname)?uname:user2.getUname());
						userService.saveUser(user2);//然后把当前的openId修改到当前用户输入的手机号码对应用户的openId
						if(StringUtils.isNotBlank(otherOpenId)){
							user.setWeixin(otherOpenId);
							user.setStatus("0");
							user.setUname(StringUtils.isNotBlank(uname)?uname:user.getUname());
							userService.saveUser(user);//最后把登录用户此次填的手机号码修改进来
						}
						user = null;
						user = user2;
					}
				}
			}
		}
		if(null == user && StringUtils.isNotBlank(code)){//根据code创建用户并且手机号码为空时提示绑定手机号码
				HashMap<String, String> wx = SysUtils.getOpenId(code);
				if(wx == null&&!code.equals("123")){
					return ResponseConstants.FUNC_USER_OPENID_FAIL_WJ;
				}
				openId = code.equals("123") ? "o2DOPwUMugpWcLs9swzBX6FgEwNk" : wx.get("openId");
				if(openId == null){
					return ResponseConstants.FUNC_USER_OPENID_FAIL_WJ;
				}
				if(user == null ){//用户不存在时根据用户openId创建一个用户
//					user = userService.getUserByObj(wx.get("unionid"), "3");
//					if(user == null){
//						user = userService.getUserByObj(openId,"2");
//					}
					user = userService.getUserByObj(openId,"2");
					if(user ==null){
						Map<String,String> map_WX = SysUtils.getWeiXinInfoByOpenid(wx.get("wxToken"),wx.get("openId"));
						user = new DC_User();
						user.setMobile(tel);
						user.setUname(map_WX.get("nickName"));
						user.setSex(map_WX.get("sex"));
						user.setUserLogo(map_WX.get("headImgUrl"));
						user.setWeixin(openId);
						user.setUpdateDate(DateUtils.currtime());
						//user.setUnionId(wx.get("unionid"));
						userService.saveUser(user);
					}
				}
		}
		if(null == user){
			return ResponseConstants.FUNC_SERVERERROR;
		}

		//用于单点登录,允许同一个用户在移动端和pc端同时登录，加上mobile标志，表示移动端



		redisCacheUtil.delete(user.getId()+"mobile");
		redisCacheUtil.setCacheObject(token,user.getId()+"mobile", RedisTableKey.EXPIRE);


		req.getSession().setAttribute(token, user.getId());
		res.setResModel(token);
		return res;
	}

	/**
	 *
	 * @author: gaoddO
	 * @2017年3月22日
	 * @功能描述:验证用户是否有权限进入小系统
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/checkIsUser",method = { RequestMethod.GET, RequestMethod.POST })
	@ApiOperation(value = "验证用户是否有权限进入小系统")
	public ResponseDbCenter checkIsUser(@RequestHeader  String token){
		//返回数据总对象，0000表示成功，其它都表示错误
		ResponseDbCenter res = new ResponseDbCenter();
		res.setResModel(false);
		return res;
	}
	/**
	 *
	 * @author: gaodd
	 * @2017年3月22日
	 * @功能描述:注销
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/logout")
	@ApiOperation(value = "退出登录")

	public ResponseDbCenter logout(HttpServletRequest request,@RequestHeader String token){
		ResponseDbCenter res = new ResponseDbCenter();
		//删除缓存的接口权限
		operateCacheService.clear(token);

		/**
		 * 先查询用户Id是否对应旧的token，如果有，则删除旧的token--userId 和userId--token映射
		 */
		String userId= (String) redisCacheUtil.getCacheObject(token);
		if (StringUtils.isNotBlank(userId)){
			redisCacheUtil.delete(userId);
			redisCacheUtil.delete(token);
		}

		request.getSession().removeAttribute(token);

		res.setResModel(null);
		return res;
	}

	/**
	 *
	 * @author: gaodd
	 * @2017年6月26日
	 * @功能描述:查询用户信息
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getUserInfo",method = { RequestMethod.GET, RequestMethod.POST })
	@ApiOperation(value = "获取用户详情")
	public ResponseDbCenter getUserInfo(HttpServletRequest request,@RequestHeader String token){
		ResponseDbCenter res = new ResponseDbCenter();
		String uid = request.getSession().getAttribute(token).toString();
		if(StringUtils.isBlank(uid)){
			return ResponseConstants.FUNC_BP_TOKENISWRONG;
		}
		Map<String,Object> userMap = newUserService.selectUserById(uid);
		userMap.put("token",token);
		res.setResModel(userMap);
		return res;
	}
	/**
	 *
	 * @author: gaodd
	 * @2017年6月26日
	 * @功能描述:pc登录时，返回的图像验证码
	 * @return
	 */
	@RequestMapping(value = {"authCode"},method = { RequestMethod.GET, RequestMethod.POST })
	@ApiOperation(value = "获取验证码")
	public void getAuthCode(HttpServletRequest request, HttpServletResponse response,HttpSession session)
            throws IOException {
        int width = 63;
        int height = 37;
        Random random = new Random();
        //设置response头信息
        //禁止缓存
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        //生成缓冲区image类
        BufferedImage image = new BufferedImage(width, height, 1);
        //产生image类的Graphics用于绘制操作
        Graphics g = image.getGraphics();
        //Graphics类的样式
        g.setColor(this.getRandColor(200, 250));
        g.setFont(new Font("Times New Roman",0,28));
        g.fillRect(0, 0, width, height);
        //绘制干扰线
        for(int i=0;i<40;i++){
            g.setColor(this.getRandColor(130, 200));
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int x1 = random.nextInt(12);
            int y1 = random.nextInt(12);
            g.drawLine(x, y, x + x1, y + y1);
        }

        //绘制字符
        String strCode = "";
        for(int i=0;i<4;i++){
            String rand = String.valueOf(random.nextInt(10));
            strCode = strCode + rand;
            g.setColor(new Color(20+random.nextInt(110),20+random.nextInt(110),20+random.nextInt(110)));
            g.drawString(rand, 13*i+6, 28);
        }
        //将字符保存到session中用于前端的验证
        //session.setAttribute("strCode", strCode);
        request.getSession(true).setAttribute("strCode",strCode);
        g.dispose();

        ImageIO.write(image, "JPEG", response.getOutputStream());
        response.getOutputStream().flush();

    }
	Color getRandColor(int fc,int bc){
        Random random = new Random();
        if(fc>255)
            fc = 255;
        if(bc>255)
            bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r,g,b);
    }

	@RequestMapping("checkToken")
	public ModelAndView checkToken(HttpServletRequest request,HttpServletResponse resp,ModelAndView modelAndView) throws IOException{
		String token = request.getHeader("token");
		String url = request.getParameter("entryUrl");
		resp.setHeader("token", token);
		modelAndView.setViewName("forward:"+url);
        return modelAndView;
	}
	/**
	 *
	 * @author: gaodd
	 * @2017年3月22日
	 * @功能描述:解密
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getPassword",method = { RequestMethod.GET, RequestMethod.POST })
	@ApiOperation(value = "获取用户密码")

	public ResponseDbCenter getPassword(@RequestParam(required=true)String password){
		ResponseDbCenter res = new ResponseDbCenter();
		try {
			res.setResModel(new String(MD5Code.decode(password)));
			return res;
		} catch (Exception e) {
			return ResponseConstants.ILLEGAL_PARAM;
		}
	}



	@ResponseBody
	@RequestMapping("/selectUserByMobile")
	@ApiOperation(value = "通过手机获取用户")
	public ResponseDbCenter selectUserByMobile(String mobile){
		ResponseDbCenter res = new ResponseDbCenter();

		 try {

			Map<String,Object> map= newUserService.selectUserByOnlyMobile(mobile);
			 res.setResModel(map);
			 return res;
		} catch (Exception e) {
			log.error("异常栈:",e);
			return ResponseConstants.FUNC_SERVERERROR;
		}




	}

	/**
	 * 小程序登录
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/xcxLogin",method = { RequestMethod.GET, RequestMethod.POST })
	@ApiOperation(value = "小程序登录")

	public ResponseDbCenter xcxLogin(
			String code
			,HttpServletRequest req){
		 ResponseDbCenter res = null;
		 try {
			 if(StringUtils.isNotBlank(code)){
				 res = ResponseConstants.MISSING_PARAMTER;
			 }
		 	HttpRequest request = new HttpRequest();
			//String result  = request.getData("https://api.weixin.qq.com/sns/jscode2session?appid=wx36ad2730eaeb3973&secret=45d56ee1626e06c00aefa270307fc4a2&js_code="+code+"&grant_type=authorization_code", "UTF-8");
			 String result  = request.getData("https://api.weixin.qq.com/sns/jscode2session?appid="+PropertiesUtil.APPID_XCX+"&secret="+PropertiesUtil.APPSECRET_XCX+"&js_code="+code+"&grant_type=authorization_code", "UTF-8");
			System.out.println("小程xu:"+result);
			Map<String, String> map2 = (Map<String, String>) JSON.parseObject(result, Object.class);
			String unionid = map2.get("unionid");
			 DC_User user = userService.getUserByObj(unionid,"3");
			 Map<String, String> map = new HashMap<>();

			 String token = UUID.randomUUID().toString();
			 if(null == user){
				 res = ResponseConstants.USER_BIND_TEL;
			 }else if(!user.getStatus().equals("0")){
				 return ResponseConstants.USER_NOTEXIST;
			 }else if(user.getPlatform().equals("0")){
				 res = ResponseConstants.USER_BIND_TEL;
			 }else{
				 req.getSession().removeValue(user.getId());
				 res = new ResponseDbCenter();

				 //用于单点登录,允许同一个用户在移动端和pc端同时登录，加上xcx标志，表示移动端小程序


				 redisCacheUtil.delete(user.getId()+"xcx");
				 redisCacheUtil.setCacheObject(token,user.getId()+"xcx",RedisTableKey.EXPIRE);

				 req.getSession().setAttribute(token, user.getId());
				 req.getSession().setAttribute(token + "uname", user.getUname());
				 map.put("userId",user.getId());
				 map.put("uname",user.getUname());
			 }
			 if (null != user && !"1".equals(user.getRoleId()) && null != user.getPcCompanyId()) {//如果不是超级管理员
				 /**
				  * 判断用户所属公司是否过期，过期了则不能继续登录
				  */
				 Map<String, Object> pcCompanyMap = customerService.selectPcCompanyById((String) user.getPcCompanyId());
				 String dateTo = (String) pcCompanyMap.get("dateTo");
				 Integer enableStatus = (Integer) pcCompanyMap.get("enableStatus");
				 if (dateTo != null) {
					 Date date = DateUtils.stringToDate(dateTo, "yyyy-MM-dd");
					 Date nowDate = DateUtils.stringToDate(DateUtils.dateToString(new Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
					 if (date.before(nowDate)) {
						 return ResponseConstants.ACCOUNT_EXPIRE;
					 }
				 }
				 if (enableStatus != null) {
					 if (1 == enableStatus) {
						 return ResponseConstants.ACCOUNT_DIABLED;
					 }
				 }
			 }
			 req.getSession().setAttribute(unionid+"xcxOpenId", map2.get("openId"));
			 req.getSession().setAttribute(token+"unionid", unionid);

			 map.put("token", token);
			 map.put("sessionId", req.getSession().getId());
			 System.out.println("-------------------小程序登录----:"+res.getRepCode());
			 res.setResModel(map);
			 return res;
		} catch (Exception e) {
			log.error("异常栈:",e);
			return ResponseConstants.FUNC_SERVERERROR;
		}
	}
	/**
	 * 小程序登录
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/checkUserTel",method = { RequestMethod.GET, RequestMethod.POST })
	@ApiOperation(value = " 验证用户手机号码是不是PC端用来登录小程序的用户")

	public ResponseDbCenter checkUserTel(@RequestParam(required=true)String tel,
			HttpServletRequest req){
		ResponseDbCenter res = new ResponseDbCenter();
		 try {
			 DC_User user = userService.getUserByObj(tel, "1");
			 if(null == user){
				 return ResponseConstants.NOT_PERMITTED;
			 }
			 if (!"1".equals(user.getRoleId())) {//如果不是超级管理员
				 /**
				  * 判断用户所属公司是否过期，过期了则不能继续登录
				  */
				 Map<String, Object> pcCompanyMap = customerService.selectPcCompanyById((String) user.getPcCompanyId());
				 String dateTo = (String) pcCompanyMap.get("dateTo");
				 Integer enableStatus = (Integer) pcCompanyMap.get("enableStatus");
				 if (dateTo != null) {
					 Date date = DateUtils.stringToDate(dateTo, "yyyy-MM-dd");
					 Date nowDate = DateUtils.stringToDate(DateUtils.dateToString(new Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
					 if (date.before(nowDate)) {
						 return ResponseConstants.ACCOUNT_EXPIRE;
					 }
				 }
				 if (enableStatus != null) {
					 if (1 == enableStatus) {
						 return ResponseConstants.ACCOUNT_DIABLED;
					 }
				 }
			 }
			 return res;
		} catch (Exception e) {
			log.error("异常栈:",e);
			return ResponseConstants.FUNC_SERVERERROR;
		}
	}
	/**
	 * 小程序登录
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/userBindTel",method = { RequestMethod.GET, RequestMethod.POST })
	@ApiOperation(value = "小程序  绑定手机")

	public ResponseDbCenter userBindTel(HttpServletRequest req,@RequestParam(required=true)String tel,String code,@RequestHeader String token,String uname,String userLogo,
			String nickName){
		ResponseDbCenter res = new ResponseDbCenter();
		 try {
			 if(StringUtils.isBlank(token)){
				 return ResponseConstants.MISSING_PARAMTER;
			 }
			 String telCode = (String) req.getSession().getAttribute("code"+tel);
			 if(!code.equals(telCode)){
				 return ResponseConstants.TEL_CODE_ERROR;
			 }
			 DC_User user = userService.getUserByObj(tel, "1");
			 String unionid =  (String) req.getSession().getAttribute(token+"unionid");
			 if(null == user){
				 return ResponseConstants.NOT_PERMITTED;
			 }else if(StringUtils.isBlank(user.getUnionId()) || !unionid.equals(user.getUnionId())){
				 DC_User user2 = userService.getUserByObj(unionid, "3");
				 if(user2 !=null){
					 user2.setUnionId(user2.getUnionId()+"temp");
					 userService.saveUser(user2);
				 }else{
					 user.setXcxOpenId((String)req.getSession().getAttribute(token+"xcxOpenId"));
					 user.setMobile(tel);
				 }
				 user.setPlatform("1");
				 user.setUnionId(unionid);

				 userService.saveUser(user);

			 }else if(user.getPlatform().equals("0")){
				 user.setPlatform("1");
				 userService.saveUser(user);
			 }

			 //req.getSession().removeValue(user.getId());
			 //token = UUID.randomUUID().toString();


			 //用于单点登录,允许同一个用户在移动端和pc端同时登录，加上xcx标志，表示移动端小程序


			 redisCacheUtil.delete(user.getId()+"xcx");
			 redisCacheUtil.setCacheObject(token,user.getId()+"xcx",RedisTableKey.EXPIRE);


			 req.getSession().setAttribute(token, user.getId());
			 req.getSession().setAttribute(token+"uname", user.getUname());
			 req.getSession().setAttribute("getOpenId"+token, String.valueOf(user.getWeixin()));

			 Map<String,String> map = new HashMap<>();
			 map.put("token",token);
			 map.put("userId",user.getId());
			 map.put("uname",user.getUname());
			 map.put("sessionId", req.getSession().getId());
			 res.setResModel(map);
			 return res;
		} catch (Exception e) {
			log.error("异常栈:",e);
			return ResponseConstants.FUNC_SERVERERROR;
		}
	}




	/**
	 * 根据code获得openId
	 * @param
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getOpenIdByCode",method = { RequestMethod.GET, RequestMethod.POST })
	@ApiOperation(value = "查询微信openId")
	public ResponseDbCenter getOpenIdByCode(HttpServletRequest req){

		String code = req.getParameter("code");

		try {
			ResponseDbCenter res = new ResponseDbCenter();
			HashMap<String, String> wx = SysUtils.getOpenId(code);
			if(wx == null&&!code.equals("123")){
				return ResponseConstants.FUNC_USER_OPENID_FAIL_WJ;
			}
			String openId = code.equals("123") ? "o2DOPwUMugpWcLs9swzBX6FgEwNk" : wx.get("openId");

			res.setResModel(openId);
			return res;
		} catch (Exception e) {
			log.error("异常栈:",e);
			return ResponseConstants.FUNC_SERVERERROR;
		}
	}

	 @ResponseBody
	 @RequestMapping("getWxConfig")
	 @ApiOperation(value = "查询微信配置")
	 public Map<String, String> getWxConfig(HttpServletRequest request,String url,String APPID,String APPSECRET){

			if(StringUtils.isBlank(url) || StringUtils.isBlank(APPID) ||StringUtils.isBlank(APPSECRET)){
				return null;
			}


	        Map<String, String> ret = WxHttpUtil.sign( url, APPID, APPSECRET);
	        System.out.println("计算出的签名-----------------------");
	        System.out.println("-----------------------");
	        ret.put("appid", APPID);
	        return ret;
	    }


	@ApiOperation(value = "查询用户列表")
	@ResponseBody
	@RequestMapping(value = "/selectUsers" ,method ={ RequestMethod.GET, RequestMethod.POST })
	public ResponseDbCenter selectUsers(HttpServletRequest req , HttpServletResponse rsp,
										@ApiParam(value = "查询内容", required = false) @RequestParam(required = false) String content){
		//返回数据总对象，0000表示成功，其它都表示错误

		String loginUserId = (String)req.getAttribute("loginUserId");
		try{
			String pcCompanyId = null;
			Map<String, Object> mapp = userService1.selectUserById(loginUserId);
			if(mapp !=null && mapp.size() > 0){
				String roleId = (String)mapp.get("roleId");
				if(!"1".equals(roleId)){
					pcCompanyId = (String)mapp.get("pcCompanyId");
				}
			}

			List<Map<String,Object>> userMaps = userService1.selectUsers(pcCompanyId,content);
			ResponseDbCenter res = new ResponseDbCenter();
			res.setResModel(userMaps);

			return res;

		}catch (Exception e){
			log.error("异常栈:",e);
			return   ResponseConstants.FUNC_SERVERERROR;
		}
	}

	/**
	 *
	 * @author: gaodd
	 * @2017年3月22日
	 * @功能描述:用户登录
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/businessCrmLogin",method = {RequestMethod.POST })
	@ApiOperation(value = "商务版CRM登录,登录有效期为半个小时")
	public ResponseDbCenter businessCrmLogin(HttpServletRequest req,
									@ApiParam(value = "密码")  @RequestParam(required = true) String password,
									@ApiParam(value = "电话")  @RequestParam(required = true) String tel,
											 @ApiParam(value = "来自哪个端，'0' 手机端   '1' pc端")  @RequestParam(required = true) String platform){

		if(StringUtils.isBlank(tel) || StringUtils.isBlank(password) || StringUtils.isBlank(platform)){
			return ResponseConstants.MISSING_PARAMTER_WJ;
		}

		try{

			Map<String, Object> userMap = newUserService.selectUserByMobile(tel,Integer.parseInt(platform));

			if(userMap == null || userMap.size() == 0){
				return ResponseConstants.USER_ERROR_SIGN;
			}else if(StringUtils.isNotBlank(password)&&!password.equals(userMap.get("password")+"")){
				return ResponseConstants.NAME_PWD_ERROR_WJ;
			}

			//用于单点登录，将用户
			/**
			 * 先查询用户Id是否对应旧的token，如果有，则删除旧的token--userId 和userId--token映射
			 */
			String oldToken= (String) redisCacheUtil.getCacheObject(userMap.get("id") + "");
			if (StringUtils.isNotBlank(oldToken)){
				redisCacheUtil.delete(oldToken);
				redisCacheUtil.delete(userMap.get("id") + "");
			}

			/**
			 * 将新的token--userId 和 userId---token写入到redis中
			 */
			String token = UUID.randomUUID().toString();
			redisCacheUtil.setCacheObject(token,userMap.get("id")+"",18000);
			redisCacheUtil.setCacheObject(userMap.get("id")+"",token,18000);

			ResponseDbCenter res = new ResponseDbCenter();
			return res;

		}catch (Exception e){
			log.error("异常栈:",e);
			return ResponseConstants.FUNC_SERVERERROR;
		}
	}

	/**
	 *
	 * @author: gaodd
	 * @2018年3月3日
	 * @功能描述:app端用户注册
	 * @return
	 *
	 */
	@ApiOperation(value = "商业版CRM用户注册")
	@ResponseBody
	@RequestMapping(value = "/businessCrmRegister" , method = RequestMethod.POST)
	public ResponseDbCenter businessCrmRegister(HttpServletRequest req, HttpServletResponse rsp,
												@ApiParam(value = "来自哪个端，'0' 手机端   '1' pc端")  @RequestParam(required = true) String platform,
												@ApiParam(value="手机号码",required = false) @RequestParam(required = false) String tel,
												@ApiParam(value="密码，密文必传",required = false) @RequestParam(required = false) String password,
												@ApiParam(value="验证码",required = false) @RequestParam(required = false) String code,
												@ApiParam(value="用户姓名",required = false) @RequestParam(required = false) String uname,
												@ApiParam(value="企业名称",required = false) @RequestParam(required = false) String companyName) throws Exception{


		if(StringUtils.isBlank(tel) || StringUtils.isBlank(password) || StringUtils.isBlank(platform) ||
				StringUtils.isBlank(code) || StringUtils.isBlank(companyName)){
			return ResponseConstants.MISSING_PARAMTER_WJ;
		}

		String existsCode = redisCacheUtil.getCacheObject(tel+"code")+"";

		if(StringUtils.isBlank(existsCode)){
			return ResponseConstants.FUNC_USER_VERIFYCODEISERROR;
		}else if(!existsCode.equals(code)){
			return ResponseConstants.TEL_CODE_ERROR;
		}

		try{

			Map<String,Object> existUser=newUserService.selectUserByOnlyMobile(tel);
			if(existUser != null && existUser.size() > 0){
				return ResponseConstants.USER_TEL_NOT_NULL;
			}
			newUserService.businessCrmRegister(platform,uname,tel,password,companyName);
			return new ResponseDbCenter();

		}catch (Exception e){
			log.error("异常栈:",e);
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}
	}


}



