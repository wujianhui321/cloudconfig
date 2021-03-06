package com.xkd.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xkd.exception.GlobalException;
import com.xkd.mapper.ScheduleUserMapper;
import com.xkd.model.*;
import com.xkd.model.Dictionary;
import com.xkd.service.*;
import com.xkd.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.HTTP;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Api(description = "添加修改公司，删除公司，修改工商信息，企查查相关，上传公司信息等")
@Controller
@RequestMapping("/company")
@Transactional
public class CompanyController extends BaseController {

	@Autowired
	private CompanyService companyServie;
	@Autowired
	private AddressService addressService;
	@Autowired
	private PaymentService paymentService;
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private UserAttendMeetingService userAttendMeetingService;

	@Autowired
	private AdviserService adviserService;
	@Autowired
	private DictionaryService dictionaryService;
	@Autowired
	private UserDynamicService userDynamicService;
	@Autowired
	private DC_PC_UserService pcUserService;
	@Autowired
	private SolrService solrService;
	@Autowired
	private RoleService roleService;
	@Autowired
	private UserService userService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private CompanyNeedService companyNeedService;

	@Autowired
	private MeetingService meetingService;

	@Autowired
	private CompanyRelativeUserService companyRelativeUserService;

	@Autowired
			private UserDataPermissionService userDataPermissionService;
	@Autowired
	PagerFileService pagerFileService;

	@Autowired
	ScheduleUserMapper scheduleColleagueMapper;


 	@Autowired
	DepartmentService departmentService;

	Logger logger = Logger.getLogger(CompanyController.class);

	/**
	 *
	 * @author: xiaoz
	 * @2017年4月21日
	 * @功能描述:通过企查名称查询企业详细名称
	 * @param req
	 * @param rsp
	 * @return
	 */
	@ApiOperation(value = "根据名称调用企查查接口查询公司列表")
	@ResponseBody
	@RequestMapping(value = "/selectQiccCompanyInfoByName",method = {RequestMethod.POST,RequestMethod.GET})
	public ResponseDbCenter selectQiccCompanyInfoByName(HttpServletRequest req, HttpServletResponse rsp,
														@ApiParam(value="公司名称",required = false) @RequestParam(required = false) String companyName,
														@ApiParam(value="当前页",required = false) @RequestParam(required = false) 	String pageIndex,
														@ApiParam(value="每页数量",required = false) @RequestParam(required = false) String pageSize,
														@ApiParam(value="标志位  ",required = false) @RequestParam(required = false) String flag)
			throws Exception {



		if (StringUtils.isBlank(companyName) || StringUtils.isBlank(flag)) {

			return ResponseConstants.MISSING_PARAMTER;
		}

		String qiccCompanyInfo = null;

		int pageIndexInt = 1;
		int pageSizeInt = 10;

		if(StringUtils.isNotBlank(pageIndex)){

			pageIndexInt = Integer.parseInt(pageIndex);
			pageSizeInt = Integer.parseInt(pageSize);
		}

		try {

			if ("JQ".equals(flag)) {

				qiccCompanyInfo = CompanyInfoApi.queryCompanyInfo(CompanyInfoApi.COMANY_NAME, companyName,1,10);

			} else {

				qiccCompanyInfo = CompanyInfoApi.queryCompanyInfo(CompanyInfoApi.COMANY_NAME_LIKE, companyName,pageIndexInt,pageSizeInt);

			}

		} catch (Exception e) {

			e.printStackTrace();
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();
		responseDbCenter.setResModel(qiccCompanyInfo);

		return responseDbCenter;
	}

	/**
	 *
	 * @author: xiaoz
	 * @2017年4月25日
	 * @功能描述:通过顾问类型得到顾问和授课老师信息，如果没有传入type就查询所有信息
	 * @param req
	 * @param rsp
	 * @return
	 */
	@ApiOperation(value = "根据类型查询顾问列表")
	@ResponseBody
	@RequestMapping(value = "/selectAdviserByType",method = {RequestMethod.POST,RequestMethod.GET})
	public ResponseDbCenter selectAdviserByType(HttpServletRequest req, HttpServletResponse rsp,
												@ApiParam(value = "类型",required = false)@RequestParam(required = false) String type) throws Exception {


		List<Map<String, Object>> advisersGuwen = null;
		List<Map<String, Object>> advisersLaoshi = null;
		List<Map<String, Object>> advisersZongjian = null;
		List<Map<String, Object>> advisers = null;
		Map<String, Object> map = null;
		String loginUserId = (String) req.getAttribute("loginUserId");

		try {

			if (StringUtils.isNotBlank(type)) {

				advisers = userService.selectUserByTeacherType("",loginUserId);

			} else {

				advisersLaoshi = userService.selectUserByTeacherType("授课老师",loginUserId);

				advisersZongjian = userService.selectUserByTeacherType("总监",loginUserId);

				advisersGuwen = userService.selectUserByTeacherType("顾问",loginUserId);

				/*
				 * advisersLaoshi = adviserService.selectAdviserByType("1");
				 * advisersZongjian = adviserService.selectAdviserByType("2");
				 * advisersGuwen = adviserService.selectAdviserByType("3");
				 */

				if (advisersZongjian != null && advisersLaoshi != null) {

					advisersZongjian.addAll(advisersLaoshi);
				}
			}

		} catch (Exception e) {

			e.printStackTrace();
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();
		if (advisers != null) {

			responseDbCenter.setResModel(advisers);

		} else {

			map = new HashMap<>();
			// 为了
			map.put("teacherIdList", advisersLaoshi);
			map.put("adviserIdList", advisersGuwen);
			map.put("directorIdList", advisersZongjian);

			responseDbCenter.setResModel(map);
		}

		return responseDbCenter;
	}




	@ApiOperation(value = "根据公司名搜索----有数据权限")
	@ResponseBody
	@RequestMapping(value = "/control/searchCompanyByName",method =  {RequestMethod.POST,RequestMethod.GET})
	public ResponseDbCenter searchCompanyByName(HttpServletRequest req, HttpServletResponse rsp,
												  @ApiParam(value="公司名称",required = false) @RequestParam(required = false) String companyName ) throws Exception {

		if (StringUtils.isBlank(companyName)) {
			return ResponseConstants.MISSING_PARAMTER;
		}
		// 当前登录用户的Id
		String loginUserId = (String) req.getAttribute("loginUserId");
		Map<String,Object>  loginUserMap=userService.selectUserById(loginUserId);

		List<Map<String, Object>> companyList = null;

		try {
			List<String>  departmentIdList=null;


			if ("1".equals(loginUserMap.get("roleId"))){
				departmentIdList=departmentService.selectChildDepartmentIds("1",loginUserMap);
			}else{
				departmentIdList=userDataPermissionService.getDataPermissionDepartmentIdList((String)loginUserMap.get("pcCompanyId"),loginUserId);
			}
			 companyList =companyServie.searchCompanyByName(companyName,departmentIdList,0,20);

		} catch (Exception e) {

			e.printStackTrace();
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();
		responseDbCenter.setResModel(companyList);
		return responseDbCenter;
	}


	@ApiOperation(value = "根据公司名搜索----无数据权限")
	@ResponseBody
	@RequestMapping(value = "/searchCompanyByName",method =  {RequestMethod.POST,RequestMethod.GET})
	public ResponseDbCenter getCompanyByName(HttpServletRequest req, HttpServletResponse rsp,
											   @ApiParam(value="公司名称",required = false) @RequestParam(required = false) String companyName ) throws Exception {

		if (StringUtils.isBlank(companyName)) {
			return ResponseConstants.MISSING_PARAMTER;
		}
		// 当前登录用户的Id
		String loginUserId = (String) req.getAttribute("loginUserId");
		Map<String,Object>  loginUserMap=userService.selectUserById(loginUserId);

		List<Map<String, Object>> companyList = null;

		try {
			List<String>  departmentIdList=null;


			if ("1".equals(loginUserMap.get("roleId"))){
				departmentIdList=departmentService.selectChildDepartmentIds("1",loginUserMap);
			}else{
				departmentIdList=departmentService.selectChildDepartmentIds((String) loginUserMap.get("pcCompanyId"), loginUserMap);
			}
			companyList =companyServie.searchCompanyByName(companyName,departmentIdList,0,20);

		} catch (Exception e) {

			e.printStackTrace();
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();
		responseDbCenter.setResModel(companyList);
		return responseDbCenter;
	}




	@ApiOperation(value = "根据公司名称模糊查询公司列表-----会务")
	@ResponseBody
	@RequestMapping(value = "/selectCompanyByNameMH",method =  {RequestMethod.POST,RequestMethod.GET})
	public ResponseDbCenter selectCompanyByNameMH(HttpServletRequest req, HttpServletResponse rsp,
												  @ApiParam(value="公司名称",required = false) @RequestParam(required = false) String companyName,
												  @ApiParam(value="会务ID,可以为空",required = false) @RequestParam(required = false) String meetingId) throws Exception {

		if (StringUtils.isBlank(companyName)) {
			return ResponseConstants.MISSING_PARAMTER;
		}

		List<HashMap<String, Object>> companies = null;

		try {

			String pcCompanyId = null;
			if(StringUtils.isNotBlank(meetingId)){
				Meeting meeting = meetingService.selectMeetingById(meetingId);
				if(meeting !=null){
					pcCompanyId = meeting.getPcCompanyId();
				}
			}


			companyName = "'%" + companyName + "%'";
			companies = companyServie.selectCompanyByNameMH(companyName,pcCompanyId);

		} catch (Exception e) {

			e.printStackTrace();
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();
		responseDbCenter.setResModel(companies);
		return responseDbCenter;
	}

	/**
	 *
	 * @author: wujianhui
	 * @2017年10月20日
	 * @功能描述:通过企查id查询企业基本名称
	 * @param req
	 * @param rsp
	 * @return
	 */
	@ApiOperation(value = "查询公司基本信息")
	@ResponseBody
	@RequestMapping(value = "/selectBasicInfoByCompanyId",method =  {RequestMethod.POST,RequestMethod.GET})
	public ResponseDbCenter selectBasicInfoByCompanyId(HttpServletRequest req, HttpServletResponse rsp,
													   @ApiParam(value = "公司Id",required = false) @RequestParam(required = false) String companyId)
			throws Exception {
		// 当前登录用户的Id
		String loginUserId = (String) req.getAttribute("loginUserId");

		String token = req.getParameter("token");


		if (StringUtils.isBlank(companyId)) {

			return ResponseConstants.MISSING_PARAMTER;
		}

		Company company = null;
		Map<String, Object> address = new HashMap();
		List<Map<String, Object>> userInfos = null;
		Map<String, Object> map = new HashMap<>();



		try {

			// 得到企业信息，工商信息，里面包含企查查信息
			company = companyServie.selectCompanyInfoById(companyId);

			if (company == null) {

				return ResponseConstants.FUNC_GETCOMPANYERROR;

			} else {

				Map<String, Object> user = userService.selectUserById(loginUserId);

				address.put("country", company.getCountry());
				address.put("province", company.getProvince());
				address.put("city", company.getCity());
				address.put("county", company.getCounty());
				address.put("address", company.getAddress());



				// 得到企业联系人多个
				userInfos = userInfoService.selectUserInfoByCompanyId(company.getId());
				//查询天眼查信息
				Map<String,Object> extraInfo=companyServie.selectCrawlInfo(company.getId());

				Integer dynamicCount=userDynamicService.selectUserDynamicCountByCompanyId(company.getId());

				//查询相关人员
				List<String> companyIdList=new ArrayList<>();
				companyIdList.add(companyId);
				List<Map<String,Object>> relativeUserList=companyRelativeUserService.selectRelativeUserListByCompanyIds(companyIdList);
				company.setRelativeUserList(relativeUserList);


				/**
				 * 隐藏敏感信息
				 */

				boolean flag=true;

				/**
				 * 如果即没有总监，也没有顾问，则默认所有人都可以看
				 */

				if ((StringUtil.isBlank(company.getCompanyAdviserId())&&StringUtil.isBlank(company.getCompanyDirectorId()))) {
					flag=true;
				}else if ((!userService.getPrivatePermission(loginUserId, "company/private")
						&&!"1".equals(user.get("roleId"))
						&& (null==user.get("isAdmin")||(null!=user.get("isAdmin")&&1!=(Integer)user.get("isAdmin")))
						&&!loginUserId.equals(company.getCompanyAdviserId())
						&&!loginUserId.equals(company.getCompanyDirectorId())
						&&!loginUserId.equals(company.getCreatedBy())
						&&!companyServie.isRelativePermission(companyId,loginUserId))) {
					flag=false;
				}

				if (!flag) {
					for (Map<String, Object> userMap : userInfos) {
						if (userMap != null) {
							userMap.put("mobile", "***");
							userMap.put("phone","***");

						}
					}
				}


				// 找出默认联系人
				String defaultContactUserId = company.getContactUserId();
				if (!StringUtils.isBlank(defaultContactUserId)) {
					for (Map<String, Object> userMap : userInfos) {
						if (defaultContactUserId.equals(userMap.get("userId"))) {
							userMap.put("uflag", 1);
						} else {
							userMap.put("uflag", 0);
						}
					}
				}

				map.put("company", company);
				map.put("address", address);
				map.put("userInfos", userInfos);
				map.put("extraInfo", extraInfo);
				map.put("dynamicCount", dynamicCount);
			}
		} catch (Exception e) {

			e.printStackTrace();
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();

		responseDbCenter.setResModel(map);

		return responseDbCenter;
	}





	/**
	 *
	 * @author: wujianhui
	 * @2017年10月20日
	 * @功能描述:查询企业高级信息
	 * @param req
	 * @param rsp
	 * @return
	 */
	@ApiOperation(value = "查询公司高级信息")
	@ResponseBody
	@RequestMapping(value = "/selectAdvancedInfoByCompanyId",method =  {RequestMethod.POST,RequestMethod.GET})
	public ResponseDbCenter selectAdvancedInfoByCompanyId(HttpServletRequest req, HttpServletResponse rsp,
														  @ApiParam(value = "公司Id",required = true) @RequestParam(required = true) String companyId)
			throws Exception {


		if (StringUtils.isBlank(companyId)) {
			return ResponseConstants.MISSING_PARAMTER;
		}

		Company company = null;
		Map<String, Object> address = new HashMap();
		List<Payment> payments = null;
		List<Map<String, Object>> userInfos = null;
		Map<String, Object> map = new HashMap<>();

		try {

			// 得到企业信息，工商信息，里面包含企查查信息
			company = companyServie.selectCompanyInfoById(companyId);

			if (company == null) {

				return ResponseConstants.FUNC_GETCOMPANYERROR;

			} else {

				address.put("country", company.getCountry());
				address.put("province", company.getProvince());
				address.put("city", company.getCity());
				address.put("county", company.getCounty());
				address.put("address", company.getAddress());

				// 得到缴费信息，得到客户等级，渠道来源，所属顾问，所属总监（一条记录），缴费时间，缴费金额（多条）,授课老师
				payments = paymentService.selectPaymentByCompanyId(company.getId());


				//查询公司需求

				List<Map<String,Object>> needList= companyNeedService.selectCompanyNeedByCompanyId(company.getId());

				//查询公司项目

				List<Map<String,Object>> projectList=projectService.selectProjectByCompanyId(company.getId());


				//查询相关人员
				List<String> companyIdList=new ArrayList<>();
				companyIdList.add(companyId);
				List<Map<String,Object>> relativeUserList=companyRelativeUserService.selectRelativeUserListByCompanyIds(companyIdList);
				company.setRelativeUserList(relativeUserList);




				//查询会务信息
				List<Map<String,Object>> meetingList=meetingService.selectMeetingByCompanyId(company.getId());

				//查询做题
				List<Map<String,Object>> exerciseList=meetingService.selectUserExcerciseByCompanyId(companyId);



				map.put("company", company);
				map.put("address", address);
				map.put("payments", payments);
				map.put("userInfos", userInfos);

				map.put("needList", needList);
				map.put("projectList", projectList);
				map.put("meetingList", meetingList);
				map.put("exerciseList", exerciseList);
			}
		} catch (Exception e) {

			e.printStackTrace();
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();

		responseDbCenter.setResModel(map);

		return responseDbCenter;
	}



	/**
	 *
	 * @author: xiaoz
	 * @2017年4月19日
	 * @功能描述:通过企业名称查询企业名字
	 * @param req
	 * @param rsp
	 * @return
	 */
	@ApiOperation(value = "根据公司名称精确查找公司")
	@RequestMapping(value = "/selectCompanyByName",method =  {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public ResponseDbCenter selectCompanyByName(HttpServletRequest req, HttpServletResponse rsp,
												@ApiParam(value="公司名称",required = true)@RequestParam(required = true) String companyName) throws Exception {


		if (StringUtils.isBlank(companyName)) {

			return ResponseConstants.MISSING_PARAMTER;
		}
		String loginUserId = (String) req.getAttribute("loginUserId");
		List<Company> companys = null;
		Company company = null;
		try {

			String pcCompanyId = null;
			Map<String, Object> mapp = userService.selectUserById(loginUserId);
			if(mapp !=null && mapp.size() > 0){
				pcCompanyId = (String)mapp.get("pcCompanyId");
			}

			companys = companyServie.selectCompanyByName(companyName,pcCompanyId);
			if (companys != null && companys.size() > 0) {
				company = companys.get(0);
			}

		} catch (Exception e) {

			e.printStackTrace();
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();

		responseDbCenter.setResModel(company);

		return responseDbCenter;
	}

	@ApiOperation(value = "查询是否存在同名公司")
	@RequestMapping(value = "/existsCompany",method =  {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public ResponseDbCenter checkCompanyByName(HttpServletRequest req, HttpServletResponse rsp,
											   @ApiParam(value = "公司名称",required = true) @RequestParam(required = true) String companyName) throws Exception {


		if (StringUtils.isBlank(companyName)) {

			return ResponseConstants.MISSING_PARAMTER;
		}

		String loginUserId = (String) req.getAttribute("loginUserId");
		List<Company> companys = null;

		try {

			String pcCompanyId = null;
			Map<String, Object> mapp = userService.selectUserById(loginUserId);
			if(mapp !=null && mapp.size() > 0){
				pcCompanyId = (String)mapp.get("pcCompanyId");
			}

			companys = companyServie.selectCompanyByName(companyName,pcCompanyId);

		} catch (Exception e) {

			e.printStackTrace();
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();

		if (companys != null && companys.size() > 0) {

			responseDbCenter.setResModel(true);

		} else {

			responseDbCenter.setResModel(false);
		}

		return responseDbCenter;
	}

	/**
	 *
	 * @author: xiaoz
	 * @2017年4月20日
	 * @功能描述:删除企业信息
	 * @param req
	 * @param rsp
	 * @return
	 */
	@ApiOperation(value = "批量删除公司")
	@RequestMapping(value = "/deleteCompanyByIds",method =  {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public ResponseDbCenter deleteCompanyByIds(HttpServletRequest req, HttpServletResponse rsp,
											   @ApiParam(value = "ids 多个值以逗号隔开  ",required = true) @RequestParam(required = true) String ids)  throws Exception{


		if (StringUtils.isBlank(ids)) {

			return ResponseConstants.MISSING_PARAMTER;
		}

		List<String> idList = new ArrayList<>();

		String[] cids = ids.split(",");

		String idString = "";
		for (int i = 0; i < cids.length; i++) {

			idString += "'" + cids[i] + "',";
			idList.add(cids[i]);
		}

		if (StringUtils.isNotBlank(idString)) {
			idString = "(" + idString.substring(0, idString.lastIndexOf(",")) + ")";
		}






		try {
							/*
				 * 从索引库中删除该企业相关的信息
				 */
			solrService.deleteDocumentByCompanyId(idList);

			companyServie.deleteCompanyById(idString);
			meetingService.deleteMeetingUserByCompanyIds(idString);
			userInfoService.deleteUserInfoByCompanyId(idString);
			projectService.deleteProjectByCompanyIds(idString);
			companyRelativeUserService.deleteByCompanyIdsString(idString);



		} catch (Exception e) {

			e.printStackTrace();
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();

		return responseDbCenter;
	}

	/**
	 *
	 * @author: xiaoz
	 * @2017年4月20日
	 * @功能描述:删除企业信息
	 * @param req
	 * @param rsp
	 * @return
	 */
	@ApiOperation(value = "更新公司标签")
	@RequestMapping(value = "/updateCompanyLabelById",method =  {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public ResponseDbCenter updateCompanyLabelById(HttpServletRequest req, HttpServletResponse rsp,
												   @ApiParam(value = "公司Id",required = true)@RequestParam(required = true) String companyId,
												   @ApiParam(value = "标签值",required = false)@RequestParam(required = false) String label )  throws Exception{


		String loginUserId = (String) req.getAttribute("loginUserId");

		if (StringUtils.isBlank(companyId)) {

			return ResponseConstants.MISSING_PARAMTER;
		}

		/**
		 * 判断企业是否有权限被修改
		 */
		boolean hasPermission=userDataPermissionService.hasPermission(companyId,loginUserId);
		if (!hasPermission){
			return  ResponseConstants.DATA_NOT_PERMITED;
		}
		try {

			companyServie.updateCompanyLabelById(companyId, label);
			companyServie.updateInfoScore(companyId);
			solrService.updateCompanyIndex(companyId);




		} catch (Exception e) {

			e.printStackTrace();
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();

		return responseDbCenter;
	}

	/**
	 *
	 * @author: xiaoz
	 * @2017年4月20日
	 * @功能描述:根据企业Id编辑企业工商信息
	 * @param req
	 * @param rsp
	 * @return
	 */
	@ApiOperation(value = "更新公司信息完整度")
	@RequestMapping(value = "/bbbbb",method =  {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public ResponseDbCenter bbbbb(HttpServletRequest req, HttpServletResponse rsp) throws Exception {

		try {

			List<String> idList=  companyServie.selecAllCompanyId();
			for (int i = 0; i <idList.size() ; i++) {
				companyServie.updateInfoScore(idList.get(i));
			}


		} catch (Exception e) {

			e.printStackTrace();
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();

		return responseDbCenter;
	}





	/**
	 *
	 * @author: xiaoz
	 * @2017年5月11日
	 * @功能描述:新增企业信息
	 * @param req
	 * @param rsp
	 * @return
	 */
	@ApiOperation(value="添加或修改公司")
	@RequestMapping(value = "/changeCompanyInfo",method = {RequestMethod.GET,RequestMethod.POST})
	@ResponseBody
	public ResponseDbCenter changeCompanyInfo(HttpServletRequest req, HttpServletResponse rsp,
											  @ApiParam(value = "插入标识 insert 表示插入  update 表示更新",required = false) @RequestParam(required = false) String ttype,
											  @ApiParam(value = "公司Id",required = false) @RequestParam(required = false) String companyId,
											  @ApiParam(value = "公司名称",required = false) @RequestParam(required = false) String companyName,
											  @ApiParam(value = "社会信用统一编号",required = false) @RequestParam(required = false)String socialCredit,
											  @ApiParam(value = "注册编号",required = false) @RequestParam(required = false)String registrationCode,
											  @ApiParam(value = "组织机构编号",required = false) @RequestParam(required = false)String organizationCode,
											  @ApiParam(value = "法人代表",required = false) @RequestParam(required = false)String representative,
											  @ApiParam(value = "注册资金",required = false) @RequestParam(required = false)String registeredMoney,
											  @ApiParam(value = "创建时间",required = false) @RequestParam(required = false)String establishTime,
											  @ApiParam(value = "顾问",required = false) @RequestParam(required = false) String companyAdviser,
											  @ApiParam(value = "总监",required = false) @RequestParam(required = false) String companyDirector,
											  @ApiParam(value = "公司logo",required = false) @RequestParam(required = false) String logo,
											  @ApiParam(value = "开始营业时间",required = false) @RequestParam(required = false) String termStart,
											  @ApiParam(value = "到期时间",required = false) @RequestParam(required = false) String operatingPeriod,
											  @ApiParam(value = "注册机构",required = false) @RequestParam(required = false) String registrationAuthority,
											  @ApiParam(value = "年营业额（万）",required = false) @RequestParam(required = false) String annualSalesVolume,
											  @ApiParam(value = "年利润（年）",required = false) @RequestParam(required = false) String annualProfit,
											  @ApiParam(value = "本年营收预测",required = false) @RequestParam(required = false) String thisYearSalesVolume,
											  @ApiParam(value = "下一年营收预测",required = false) @RequestParam(required = false) String nextYearSalesVolume,
											  @ApiParam(value = "业务范围 如 全国，华南",required = false) @RequestParam(required = false) String businessScope,
											  @ApiParam(value = "审核日期",required = false) @RequestParam(required = false) String approveDate,
											  @ApiParam(value = "公司行业类型",required = false) @RequestParam(required = false) String companyType,
											  @ApiParam(value = "公司规模",required = false) @RequestParam(required = false) String companySize,
											  @ApiParam(value = "公司英文名称",required = false) @RequestParam(required = false) String englishName,
											  @ApiParam(value = "曾用名",required = false) @RequestParam(required = false) String beforeName,
											  @ApiParam(value = "公司网站",required = false) @RequestParam(required = false) String website,
											  @ApiParam(value = "公司邮箱",required = false) @RequestParam(required = false) String  email,
											  @ApiParam(value = "微信",required = false) @RequestParam(required = false) String  weChat,
											  @ApiParam(value = "企业描述",required = false) @RequestParam(required = false) String content,
											  @ApiParam(value = "企业定位",required = false) @RequestParam(required = false) String companyPosition,
											  @ApiParam(value = "公司类型 如:有限责任公司",required = false) @RequestParam(required = false) String econKind,
											  @ApiParam(value = "省",required = false) @RequestParam(required = false) String province,
											  @ApiParam(value = "市",required = false) @RequestParam(required = false) String city,
											  @ApiParam(value = "区",required = false) @RequestParam(required = false) String county,
											  @ApiParam(value = "公司地址",required = false) @RequestParam(required = false) String address,
											  @ApiParam(value = "父行业Id",required = false) @RequestParam(required = false) String parentIndustryId,
											  @ApiParam(value = "子行业  弃用",required = false) @RequestParam(required = false) String sonIndustry,
											  @ApiParam(value = "公司一句话描述",required = false) @RequestParam(required = false) String companyDesc,
											  @ApiParam(value = "获投状态 ",required = false) @RequestParam(required = false) String investStatus,
											  @ApiParam(value = "融资状态",required = false) @RequestParam(required = false) String financeStatus,
											  @ApiParam(value = "经营状态 如（存续）",required = false) @RequestParam(required = false) String manageType,
											  @ApiParam(value = "经营范围",required = false) @RequestParam(required = false) String manageScope,
											  @ApiParam(value = "公司属性Id 如 （数据字典中的 国有 私有）",required = false) @RequestParam(required = false) String companyProperty,
											  @ApiParam(value = "客户类型",required = false) @RequestParam(required = false) String  userType,
											  @ApiParam(value = "客户等级",required = false) @RequestParam(required = false) String userLevel,
											  @ApiParam(value = "渠道来源",required = false) @RequestParam(required = false) String channel,
											  @ApiParam(value = "报名日期",required = false) @RequestParam(required = false) String enrollDate ,
											  @ApiParam(value = "全款状态",required = false) @RequestParam(required = false) String moneySituation,
											  @ApiParam(value = "相关人员Id json数组 如 ['1','2']",required = false) @RequestParam(required = false) String relativeUserIds,
											  @ApiParam(value = "优先级",required = false) @RequestParam(required = false) String priority,
											  @ApiParam(value = "付款时间",required = false)  @RequestParam(required = false) String paymentDate,
											  @ApiParam(value = "付款金额",required = false) @RequestParam(required = false) String paymentMoney,
											  @ApiParam(value = "收款人",required = false) @RequestParam(required = false) String dealPerson  ,
											  @ApiParam(value = "参会状态  未参会  已参会",required = false) @RequestParam(required = false) String attendStatus  ,
											  @ApiParam(value = "部门Id",required = false) @RequestParam(required = false) String departmentId ) throws Exception {

		// 当前登录用户的Id
		String loginUserId = (String) req.getAttribute("loginUserId");
		Map<String,Object>  loginUserMap=userService.selectUserById(loginUserId);
		String token = req.getParameter("token");

		Map<String, Object> company = new HashMap();
		ResponseDbCenter responseDbCenter = new ResponseDbCenter();


		/**
		 * 如果部门Id为空，则添加时默认以添加人的部门Id作为该条数据的部门
		 */
		if (StringUtil.isBlank(departmentId)){
			Map<String,Object> map= pcUserService.selectPcUserById(loginUserId);
			departmentId=(String)map.get("departmentId");
		}


		  Map<String,Object> companyMap=   departmentService.getCompanyIdByDepartmentId(departmentId);
			//设置该记录属性哪一个部门客户
 			company.put("pcCompanyId",companyMap.get("id"));

		try {
			establishTime = establishTime == null ? "" : establishTime.substring(0, 10);
		} catch (Exception e) {
			establishTime = "";
		}

		try {
			operatingPeriod = (operatingPeriod == null ? "" : operatingPeriod.substring(0, 10));
		} catch (Exception e) {
			operatingPeriod = "";
		}

		try {
			termStart = (termStart == null ? "" : termStart.substring(0, 10));
		} catch (Exception e) {
			termStart = "";
		}

		// 通过企业名称查询该企业是否存在，如果存在就返回提示信息

		List<Company> alreadyExists   =companyServie.selectCompanyByNameIncludingDeleted(companyName,(String)company.get("pcCompanyId"));
		if (alreadyExists.size()>0) {
			if ("insert".equals(ttype)) {
				if (alreadyExists.get(0).getStatus()!=0) {
					//如果名称跟已经删除的数据中冲突，则将库里的旧数据名字上加上时间戳
					companyServie.deleteByCompanyById(alreadyExists.get(0).getId());
				}else{
					//如果名称与未删除的数据冲突，则不允许再添加了
					return ResponseConstants.FUNC_COMPANY_EXIST;
				}
			}else{
				if (!alreadyExists.get(0).getId().equals(companyId)) {
					if (alreadyExists.get(0).getStatus()!=0) {
						//如果名称跟已经删除的数据中冲突，则将库里的旧数据名字上加上时间戳
						companyServie.deleteByCompanyById(alreadyExists.get(0).getId());
					}else{
						//如果名称与未删除的数据冲突，则不允许再添加了
						return ResponseConstants.FUNC_COMPANY_EXIST;
					}
				}
			}
		}
		if ("insert".equals(ttype)) {
			String insertCompanyId 	= UUID.randomUUID().toString();

			company.put("id", insertCompanyId);
			company.put("companyName", companyName);
			company.put("englishName", englishName);
			company.put("label", null);
			company.put("representative", representative);
			company.put("parentIndustryId", parentIndustryId);
			company.put("sonIndustry", sonIndustry);
			company.put("investStatus", investStatus);
			company.put("financeStatus", financeStatus);
			company.put("phone", null);
			company.put("companyAdviserId", companyAdviser);
			company.put("companyDirectorId", companyDirector);
			company.put("logo", logo);
			company.put("following", null);
			company.put("qccUpdatedDate", DateUtils.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss"));
			company.put("status", 0);
			company.put("province", province);
			company.put("city", city);
			company.put("county", county);
			company.put("address", address);
			company.put("createdBy", loginUserId);
			company.put("updatedBy", loginUserId);
			company.put("createDate", new Date());
			company.put("updateDate", new Date());
			company.put("userLevel", userLevel);
			company.put("userType", userType);
			company.put("enrollDate", enrollDate);
			company.put("channel", channel);
			company.put("departmentId", departmentId);
			company.put("priority", priority);
			company.put("attendStatus", attendStatus);
			// 工商信息
			company.put("socialCredit", socialCredit);
			company.put("registrationCode", registrationCode);
			company.put("organizationCode", organizationCode);
			company.put("manageType", manageType);
			company.put("manageScope", manageScope);
			company.put("registeredMoney", registeredMoney);
			company.put("registrationAuthority", registrationAuthority);
			company.put("annualSalesVolume", annualSalesVolume);
			company.put("annualProfit", annualProfit);
			company.put("thisYearSalesVolume", thisYearSalesVolume);
			company.put("nextYearSalesVolume", nextYearSalesVolume);
			company.put("businessScope", businessScope);
			company.put("companyType", companyType);
			company.put("companyPropertyId", companyProperty);
			company.put("companySize", companySize);
			company.put("beforeName", beforeName);
			company.put("companyDesc", companyDesc);
			company.put("website", website);
			company.put("phone", null);
			company.put("email", email);
			company.put("wechat", weChat);
			company.put("content", content);
			company.put("companyPosition", companyPosition);
			company.put("econKind", econKind);
			company.put("establishTime", establishTime);
			company.put("approveDate", approveDate);
			company.put("termStart", termStart);
			company.put("operatingPeriod", operatingPeriod);
			company.put("companyOpportunity", null);

			try {
				//插入企业信息
				companyServie.insertCompanyInfo(company);
				//插入公司详情
				companyServie.insertCompanyDetailInfo(company);
				userDynamicService.addUserDynamic(loginUserId, insertCompanyId, companyName,"添加",  "创建了企业\"" + companyName + "\"", 0,null,null,null);
				//更新企业信息完整度
				companyServie.updateInfoScore((String)company.get("id"));
			} catch (Exception e) {
				e.printStackTrace();
				throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
			}
			//将该企业维护到solr库中
			solrService.updateCompanyIndex(insertCompanyId);
			responseDbCenter.setResModel(insertCompanyId);
		} else {
			if (StringUtils.isNotBlank(companyId)) {
				//判断企业是否有权限被修改
				boolean hasPermission=userDataPermissionService.hasPermission(companyId,loginUserId);
				if (!hasPermission){
					return  ResponseConstants.DATA_NOT_PERMITED;
				}
				try {
					company.put("id", companyId);
					company.put("companyName", companyName);
					company.put("englishName", englishName);
					company.put("label", null);
					company.put("representative", representative);
					company.put("parentIndustryId", parentIndustryId);
					company.put("sonIndustry", sonIndustry);
					company.put("investStatus", investStatus);
					company.put("financeStatus", financeStatus);
					company.put("phone", null);
					company.put("companyAdviserId", companyAdviser);
					company.put("companyDirectorId", companyDirector);
					company.put("logo", logo);
					company.put("following", null);
					company.put("qccUpdatedDate", DateUtils.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss"));
					company.put("province", province);
					company.put("city", city);
					company.put("county", county);
					company.put("address", address);

					company.put("userLevel", userLevel);
					company.put("userType", userType);
					company.put("enrollDate", enrollDate);
					company.put("moneySituation", moneySituation);
					company.put("channel", channel);
					company.put("departmentId", departmentId);
					company.put("priority", priority);
					company.put("attendStatus", attendStatus);


					// 企业详情属性
					company.put("socialCredit", socialCredit);
					company.put("registrationCode", registrationCode);
					company.put("organizationCode", organizationCode);
					company.put("manageType", manageType);
					company.put("manageScope", manageScope);
					company.put("registeredMoney", registeredMoney);
					company.put("registrationAuthority", registrationAuthority);
					company.put("annualSalesVolume", annualSalesVolume);
					company.put("annualProfit", annualProfit);
					company.put("thisYearSalesVolume", thisYearSalesVolume);
					company.put("nextYearSalesVolume", nextYearSalesVolume);
					company.put("businessScope", businessScope);
					company.put("companyType", companyType);
					company.put("companyPropertyId", companyProperty);
					company.put("companySize", companySize);
					company.put("beforeName", beforeName);
					company.put("companyDesc", companyDesc);
					company.put("website", website);
					company.put("phone", null);
					company.put("email", email);
					company.put("wechat", weChat);
					company.put("content", content);
					company.put("companyPosition", companyPosition);
					company.put("econKind", econKind);
					company.put("establishTime", establishTime);
					company.put("approveDate", approveDate);
					company.put("termStart", termStart);
					company.put("operatingPeriod", operatingPeriod);
					company.put("companyOpportunity", null);
					company.put("updatedBy", loginUserId);
					company.put("updateDate", new Date());
				} catch (Exception e) {
					e.printStackTrace();
					throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
				}
			}

			try {
				//添加第一条付款信息
				if (StringUtils.isNotBlank(moneySituation)
						&&StringUtils.isNotBlank(paymentMoney)
						&&StringUtils.isNotBlank(paymentDate)) {
					Payment payment = new Payment();
					String id = UUID.randomUUID().toString();
					payment.setId(id);
					payment.setMoneySituation(moneySituation);
					payment.setPaymentDate(paymentDate);
					Integer paymentMoneyInYuan = ((Double) (Double.valueOf(paymentMoney) * 10000.0)).intValue();
					payment.setPaymentMoney(String.valueOf(paymentMoneyInYuan));

					payment.setCompanyId(companyId);

					payment.setUpdatedBy(loginUserId);
					payment.setCreatedBy(loginUserId);
					Date createdDate = new Date();
					payment.setUpdateDate(createdDate);
					payment.setCreateDate(createdDate);
					payment.setDealPerson(dealPerson);
					paymentService.insertPayment(payment);
					company.put("paymentId", payment.getId());
					company.put("paymentDate", payment.getPaymentDate());
					company.put("moneySituation", moneySituation);
				}
				//更新企业信息
				companyServie.updateCompanyInfoById(company);
				companyServie.updateCompanyDetailInfoById(company);
				//修改客户部门以后，我们需要将其对应的文件夹等的部门Id也修改了
				if (StringUtils.isNotBlank(departmentId)) {
					pagerFileService.editFolderDepartment(companyId, departmentId, loginUserId);
				}
				int time = 1;
				if(StringUtils.isNotBlank(companyAdviser)){
					Map<String,Object> historyUserInfo = new HashMap<>();
					historyUserInfo.put("myUserId",loginUserId);
					historyUserInfo.put("userId", companyAdviser);
					historyUserInfo.put("time",time++);
					scheduleColleagueMapper.saveHistoryUser(historyUserInfo);
				}
				if(StringUtils.isNotBlank(companyDirector)){
					Map<String,Object> historyUserInfo = new HashMap<>();
					historyUserInfo.put("myUserId",loginUserId);
					historyUserInfo.put("userId", companyDirector);
					historyUserInfo.put("time",time++);
					scheduleColleagueMapper.saveHistoryUser(historyUserInfo);
				}

				userDynamicService.addUserDynamic(loginUserId, companyId, companyName, "更新", "修改了企业\"" + companyName + "\"", 0,null,null,null);
				//更新企业相关人员记录
				if (StringUtils.isNotBlank(relativeUserIds)) {
					List<String> userIds = JSON.parseObject(relativeUserIds, new TypeReference<List<String>>() {
					});
					List<Map<String, Object>> relativeUserMapList = new ArrayList<>();
					if (null!=userIds) {
						for (int i = 0; i < userIds.size(); i++) {
							Map<String, Object> map = new HashMap<>();
							String id=UUID.randomUUID().toString();
							map.put("id", id);
							map.put("companyId", companyId);
							map.put("userId", userIds.get(i));
							relativeUserMapList.add(map);
							map.put("time",i+time);
							map.put("myUserId",loginUserId);
							scheduleColleagueMapper.saveHistoryUser(map);
						}
						companyRelativeUserService.deleteByCompanyId(companyId);
						if (relativeUserMapList.size() > 0) {
							companyRelativeUserService.insertList(relativeUserMapList);
						}
					}
				}
				List<Map<String,String>> historyUser = scheduleColleagueMapper.getMyHistoryUser(loginUserId);
				if(historyUser.size() > 20){
					for (int i = 20; i < historyUser.size(); i++) {
						scheduleColleagueMapper.deleteHistoryUser(loginUserId,historyUser.get(i).get("userId"));
					}
				}
				//更新企业信息完整度
				companyServie.updateInfoScore((String)company.get("id"));
			} catch (Exception e) {
				e.printStackTrace();
				throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
			}
			//将该企业维护到solr库中
			solrService.updateCompanyIndex(companyId);
		}
		return responseDbCenter;
	}

	//更新企业工商信息
	@RequestMapping(value = "/transferAdviser",method ={RequestMethod.GET,RequestMethod.POST} )
	@ResponseBody
	@ApiOperation(value = "转顾问", response = ResponseDbCenter.class, notes = "")
	public ResponseDbCenter transferAdviser(HttpServletRequest req, HttpServletResponse rsp,
														  @ApiParam(value="公司Id",required = true) @RequestParam(required = true) String companyIds,
														  @ApiParam(value="用户Id",required = true) @RequestParam(required = true) String adviserId

	)
			throws Exception {
		String loginUserId = (String) req.getAttribute("loginUserId");
  		try{
  			String companyIdList [] = companyIds.split(",");
			Map<String,Object> companyMap= new HashMap<>();
			companyMap.put("companyAdviserId", adviserId);
			Map<String, Object> user = userService.selectUserById(adviserId);
			companyMap.put("updatedBy",loginUserId);
			for (String companyId:companyIdList) {
				companyMap.put("id",companyId);
				companyServie.updateCompanyInfoById(companyMap);
				//更新企业信息完整度
				companyServie.updateInfoScore((String)companyMap.get("id"));
				//将该企业维护到solr库中
				solrService.updateCompanyIndex(companyId);
				Company company = companyServie.selectCompanyInfoById(companyId);
				userDynamicService.addUserDynamic(loginUserId, companyId, company.getCompanyName(), "更新", "将\"" + company.getCompanyName() + "\"转移到"+user.get("uname")+"名下", 0,null,null,null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}
		ResponseDbCenter responseDbCenter = new ResponseDbCenter();
		return responseDbCenter;
	}


	/**
	 *
	 * @author: xiaoz
	 * @2017年6月21日
	 * @功能描述:更新企业工商信息
	 * @param req
	 * @param rsp
	 * @return
	 */
	@RequestMapping(value = "/updateCompanyIndustryCommerce",method ={RequestMethod.GET,RequestMethod.POST} )
	@ResponseBody
	@ApiOperation(value = "更新公司工商信息", response = ResponseDbCenter.class, notes = "")
	public ResponseDbCenter updateCompanyIndustryCommerce(HttpServletRequest req, HttpServletResponse rsp,
														  @ApiParam(value="公司Id",required = true) @RequestParam(required = true) String companyId,
														  @ApiParam(value="社会信用编号",required = false) @RequestParam(required = false) String socialCredit ,
														  @ApiParam(value="登记册号",required = false) @RequestParam(required = false) String registrationCode,
														  @ApiParam(value="组织机构号",required = false) @RequestParam(required = false) String organizationCode,
														  @ApiParam(value="经营类型 如存续",required = false) @RequestParam(required = false) String manageType,
														  @ApiParam(value="法人代表",required = false) @RequestParam(required = false) String representative,
														  @ApiParam(value="注册资本",required = false) @RequestParam(required = false) String registeredMoney,
														  @ApiParam(value="公司类型如有限责任公司",required = false) @RequestParam(required = false) String econKind,
														  @ApiParam(value="创建时间",required = false) @RequestParam(required = false) String establishTime,
														  @ApiParam(value="经营范围",required = false) @RequestParam(required = false) String manageScope,
														  @ApiParam(value="公司描述",required = false) @RequestParam(required = false) String content,
														  @ApiParam(value="营业开始时间",required = false) @RequestParam(required = false) String termStart,
														  @ApiParam(value="营业结束时间",required = false) @RequestParam(required = false) String operatingPeriod,
														  @ApiParam(value="注册机构",required = false) @RequestParam(required = false) String registrationAuthority,
														  @ApiParam(value="核准日期",required = false) @RequestParam(required = false) String approveDate,
														  @ApiParam(value="公司规模",required = false) @RequestParam(required = false) String companySize,
														  @ApiParam(value="曾用名",required = false) @RequestParam(required = false) String beforeName,
														  @ApiParam(value="省",required = false) @RequestParam(required = false) String province,
														  @ApiParam(value="市",required = false) @RequestParam(required = false) String city,
														  @ApiParam(value="县",required = false) @RequestParam(required = false) String county,
														  @ApiParam(value="地址",required = false) @RequestParam(required = false) String address
														  )
			throws Exception {
		String loginUserId = (String) req.getAttribute("loginUserId");



		if (StringUtils.isBlank(companyId)) {

			return ResponseConstants.MISSING_PARAMTER;
		}


		/**
		 * 判断企业是否有权限被修改
		 */
		boolean hasPermission=userDataPermissionService.hasPermission(companyId,loginUserId);
		if (!hasPermission){
			return  ResponseConstants.DATA_NOT_PERMITED;
		}

		try {

			Map<String, Object> company = new HashMap();
			company.put("id", companyId);
			company.put("socialCredit", socialCredit);
			company.put("registrationCode", registrationCode);
			company.put("organizationCode", organizationCode);
			company.put("manageType", manageType);
			company.put("representative", representative);
			company.put("registeredMoney", registeredMoney);
			company.put("econKind", econKind);
			company.put("establishTime", establishTime);
			company.put("manageScope", manageScope);
			company.put("content", content);
			company.put("termStart", termStart);
			company.put("operatingPeriod", operatingPeriod);
			company.put("registrationAuthority", registrationAuthority);
			company.put("approveDate", approveDate);
			company.put("companySize", companySize);
			company.put("beforeName", beforeName);
			company.put("province", province);
			company.put("city", city);
			company.put("county", county);
			company.put("address", address);
			company.put("qccUpdatedDate",DateUtils.dateToString( new Date(), "yyyy-MM-dd HH:mm:ss"));


			try {
				if (!StringUtils.isBlank(address)) {
					LngLatData lngLatData= BaiduAddressUtil.parseAddressToLngLat(address);
					if (lngLatData!=null) {
						AddressData addressData=BaiduAddressUtil.parseLngLatToAddress(lngLatData.getLongitude(),lngLatData.getLatitude());
						if (addressData!=null) {
							company.put("country", addressData.getCountry());
							company.put("province", addressData.getProvince());
							company.put("city", addressData.getCity());
							company.put("county", addressData.getCounty());
						}
					}
				}
			}catch (Exception e){
				e.printStackTrace();
			}



			companyServie.updateCompanyInfoById(company);
			companyServie.updateCompanyDetailInfoById(company);



			companyServie.updateInfoScore(companyId);
			solrService.updateCompanyIndex(companyId);

			userDynamicService.addUserDynamic(loginUserId, companyId, "", "更新", "更新了企业工商信息", 0,null,null,null);

		} catch (Exception e) {

			e.printStackTrace();
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();

		return responseDbCenter;
	}

	/**
	 *
	 * @author: xiaoz
	 * @2017年5月22日
	 * @功能描述:根据企业id查询缴费信息
	 * @param req
	 * @param rsp
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/selectPaymentByCompanyId",method ={RequestMethod.GET,RequestMethod.POST} )
	@ApiOperation(value = "查询某个公司的付款信息", response = ResponseDbCenter.class, notes = "")
	public ResponseDbCenter selectPaymentByCompanyId(HttpServletRequest req, HttpServletResponse rsp ,
													 @ApiParam(value = "公司Id",required = true) @RequestParam(required = true)String companyId
	) throws Exception {


		if (StringUtils.isBlank(companyId)) {

			return ResponseConstants.MISSING_PARAMTER;
		}

		List<Payment> payments = paymentService.selectPaymentByCompanyIdOrderByPaymentDate(companyId);

		Map<String, Object> map = new HashMap<>();

		map.put("payments", payments);

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();
		responseDbCenter.setResModel(map);
		;

		return responseDbCenter;
	}

	/**
	 * 上传企业图片
	 * @param files
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/uploadCompanyLogo",method ={RequestMethod.GET,RequestMethod.POST})
	@ApiOperation(value = "上传企业图片", response = ResponseDbCenter.class, notes = "")
	public ResponseDbCenter uploadCompanyPicture(@RequestParam(value = "files", required = false) MultipartFile[] files,
												 HttpServletRequest req ) throws Exception {

		if (files == null) {

			return ResponseConstants.MISSING_PARAMTER;
		}

		String uploadPath = PropertiesUtil.FILE_UPLOAD_PATH + "companysLogo";
		String httpPath = PropertiesUtil.FILE_HTTP_PATH + "companysLogo";

		List<String> fileList = FileUtil.fileUpload(files, uploadPath, httpPath);

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();
		responseDbCenter.setResModel(fileList);

		return responseDbCenter;
	}


	/**
	 *
	 * @param req
	 * @param companyNeed  :
	 *

	{
	"companyId": "12145",
	"time": "2012-09-09",
	"need": "需求",
	"needDiagnosis": "需求诊断"
	}

	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value="/saveCompanyNeed",method={RequestMethod.POST,RequestMethod.GET})
	@ApiOperation(value = "添加公司需求", response = ResponseDbCenter.class, notes = "")
	public ResponseDbCenter changeCompanyNeed( HttpServletRequest req ,
											   @ApiParam(value = "公司需求 格式如: \t{\n" +
			"\t\"companyId\": \"12145\",\n" +
			"\t\"time\": \"2012-09-09\",\n" +
			"\t\"need\": \"需求\",\n" +
			"\t\"needDiagnosis\": \"需求诊断\"\n" +
			"\t} ",required = true) @RequestParam(required = true)  String companyNeed  )
			throws Exception {
		String loginUserId = (String) req.getAttribute("loginUserId");

		if (StringUtil.isBlank(companyNeed)) {
			return ResponseConstants.MISSING_PARAMTER;

		}

		try {
			Map<String, Object> map  = JSON.parseObject(companyNeed, new TypeReference<Map<String, Object>>() {
			});


			/**
			 * 判断企业是否有权限被修改
			 */
			boolean hasPermission=userDataPermissionService.hasPermission((String)map.get("companyId"),loginUserId);
			if (!hasPermission){
				return  ResponseConstants.DATA_NOT_PERMITED;
			}

			map.put("id",UUID.randomUUID().toString());
			companyNeedService.insertCompanyNeed(map);
			userDynamicService.addUserDynamic(loginUserId, map.get("companyId").toString(), "", "添加", "添加了企业需求", 0,null,null,null);
			companyServie.updateInfoScore((String) map.get("companyId"));
			solrService.updateCompanyIndex((String) map.get("companyId"));
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
	 * @param companyNeed  ：

	{
	"id":"12545",
	"companyId": "12145",
	"time": "2012-09-09",
	"need": "需求",
	"needDiagnosis": "需求诊断"
	}

	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value="/updateCompanyNeed",method={RequestMethod.POST,RequestMethod.GET})
	@ApiOperation(value = "更新公司需求", response = ResponseDbCenter.class, notes = "")
	public ResponseDbCenter updateCompanyNeed( HttpServletRequest req ,@ApiParam(value = "公司需求 格式如: \t{\n" +
			"\t\"companyId\": \"12145\",\n" +
			"\t\"time\": \"2012-09-09\",\n" +
			"\t\"need\": \"需求\",\n" +
			"\t\"needDiagnosis\": \"需求诊断\"\n" +
			"\t} ",required = true) @RequestParam(required = true)  String companyNeed)
			throws Exception {
		String loginUserId = (String) req.getAttribute("loginUserId");

		if (StringUtil.isBlank(companyNeed)) {
			return ResponseConstants.MISSING_PARAMTER;

		}

		try {
			Map<String, Object> map  = JSON.parseObject(companyNeed, new TypeReference<Map<String, Object>>() {
			});
			/**
			 * 判断企业是否有权限被修改
			 */
			boolean hasPermission=userDataPermissionService.hasPermission((String) map.get("companyId"),loginUserId);
			if (!hasPermission){
				return  ResponseConstants.DATA_NOT_PERMITED;
			}
			companyNeedService.updateCompanyNeed(map);
			userDynamicService.addUserDynamic(loginUserId, map.get("companyId").toString(), "","更新", "更新企业需求", 0,null,null,null);
			companyServie.updateInfoScore((String) map.get("companyId"));
			solrService.updateCompanyIndex((String) map.get("companyId"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();

		return responseDbCenter;

	}


	/**
	 * 删除公司需求
	 * @param req
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/deleteCompanyNeed",method ={RequestMethod.GET,RequestMethod.POST})
	@ApiOperation(value = "删除公司需求", response = ResponseDbCenter.class, notes = "")
	public ResponseDbCenter deleteCompanyNeed( HttpServletRequest req ,
											   @ApiParam(value = "公司需求ID ",required = true) @RequestParam(required = true)    String id  )
			throws Exception {
		String loginUserId = (String) req.getAttribute("loginUserId");

		if (StringUtil.isBlank(id)) {
			return ResponseConstants.MISSING_PARAMTER;

		}

		try {
			Map<String,Object> companyNeedMap=companyNeedService.selectComopanyNeedById(id);
			/**
			 * 判断企业是否有权限被修改
			 */
			boolean hasPermission=userDataPermissionService.hasPermission((String)companyNeedMap.get("companyId"),loginUserId);
			if (!hasPermission){
				return  ResponseConstants.DATA_NOT_PERMITED;
			}
			companyNeedService.deleteCompanyNeedById(id);
			userDynamicService.addUserDynamic(loginUserId, (String) companyNeedMap.get("companyId"), "","删除", "删除企业需求", 0,null,null,null);
			companyServie.updateInfoScore((String) companyNeedMap.get("companyId"));
			solrService.updateCompanyIndex((String) companyNeedMap.get("companyId"));
 		} catch (Exception e) {
			e.printStackTrace();
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();

		return responseDbCenter;

	}

	/**
	 * 临时方法
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/updateInfoScore",method = {RequestMethod.GET} )
	@ApiOperation(value = "重新计算库中的公司信息完整度--- 临时方法", response = ResponseDbCenter.class, notes = "")
	public ResponseDbCenter updateInfoScore( HttpServletRequest req  )
			throws Exception {



		try {
			List<String> list=companyServie.selecAllCompanyId();
			for (int i = 0; i <list.size() ; i++) {
				companyServie.updateInfoScore(list.get(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();

		return responseDbCenter;
	}
	@ApiOperation(value = "批量导入公司信息")
	@ResponseBody
	@RequestMapping(value = "/uploadCompanyDocument", method =  {RequestMethod.POST})
	public ResponseDbCenter uploadCompanyDocument(@RequestParam(value = "files", required = false) MultipartFile[] files,HttpServletRequest req) throws Exception {



		if (files == null) {
			return ResponseConstants.MISSING_PARAMTER;
		}
		// 当前登录用户的Id
		String loginUserId = (String) req.getAttribute("loginUserId");

		String pcCompanyId = null;
		Map<String, Object> mapp = userService.selectUserById(loginUserId);
		if(mapp !=null && mapp.size() > 0){
			String roleId = (String)mapp.get("roleId");
			if(!"1".equals(roleId)){
				pcCompanyId = (String)mapp.get("pcCompanyId");
			}
		}
		Map<String,Object> loginUserMap=userService.selectUserById(loginUserId);

		//try {

		List<String> errorMessageList = new ArrayList<>();
		Map<String,Object> returnMap = new HashMap<>();
		Map<String,Object> tacherMap = new HashMap<>();
		Map<String,Object> dictionaryMap = new HashMap<>();

		JsonParser parse =new JsonParser();  //创建json解析器

		//任何浮点型数字
		String regEx = "^\\d+(\\.\\d+)?$";
		String titleList [] = {"序号","日期","城市","老师名","课程名","客户姓名","联系电话","客户企业名称","收款合计（元）","定金或全款","备注","渠道","顾问"};
		// 编译正则表达式
		Pattern pattern = Pattern.compile(regEx);
		XSSFSheet xsheet = null;
		XSSFWorkbook xSSFWorkbook = null;
		List<String> error = new ArrayList<>();
		List<String> successCompanyId = new ArrayList<>();
		Map<Integer,String> errorRow = new HashMap<>();
		try {
		for (MultipartFile file : files) {
			xSSFWorkbook = new XSSFWorkbook(new BufferedInputStream(file.getInputStream()));
			xsheet = xSSFWorkbook.getSheetAt(0);  //获取 某个表  ，一般默认是第一个表
			System.out.println("导入：总共多少列："+xsheet.getLastRowNum());
			for( int rows=0;rows<=xsheet.getLastRowNum();rows++){
				XSSFRow row = xsheet.getRow(rows);//取得某一行   对象
				for (int i = 0; i <= 12; i++) {
					if(row.getCell(i) == null){
						continue;
					}
					//标题
					row.getCell(i).setCellType(Cell.CELL_TYPE_STRING);
					String value = row.getCell(i).getStringCellValue();
					if(rows == 0){
						if(StringUtils.isBlank(value)){
							errorMessageList.add("第"+(rows)+"行第"+i+"列  :"+titleList[i]+"不能为空");
						}
					}else{
						if( i == 7 ){//企业名称
							if(StringUtils.isBlank(value)){
								errorMessageList.add("第"+(rows)+"行  :"+titleList[i]+"不能为空");
							}
						}else if((i == 3 || i == 12 )&& StringUtils.isNotBlank(value)){//老师名||顾问
							if(tacherMap.get(value+"="+pcCompanyId) == null){
								List<Map<String,Object>> teacherMaps = userService.selectTeacherByTeacherName(value,pcCompanyId);
								if(teacherMaps == null || teacherMaps.size() == 0){
									errorMessageList.add("第"+(rows+1)+"行 :系统中没有找到该"+titleList[i]);
								}else{
									tacherMap.put(value+"="+pcCompanyId,teacherMaps.get(0).get("id"));
								}

							}

						}else if(i == 9 && StringUtils.isNotBlank(value) && !"定金".equals(value) && !"全款".equals(value)){
							errorMessageList.add("第"+(rows+1)+"行 :系统中没有找到该"+titleList[i]);
						}else if(i == 4 && StringUtils.isNotBlank(value) ){
							if(dictionaryMap.get(value+"=userType") == null){
								List<Dictionary> userTypeDictionary = dictionaryService.selectDictionaryByTtypeValue("userType", value,(String)loginUserMap.get("pcCompanyId"));
								if(userTypeDictionary == null || userTypeDictionary.size() == 0) {
									errorMessageList.add("第"+(rows+1)+"行 :系统中没有找到该"+titleList[i]);
								}else{
									dictionaryMap.put(value+"=userType",userTypeDictionary.get(0).getId());
								}
							}

						}else if(i == 11 && StringUtils.isNotBlank(value) ){
							if(dictionaryMap.get(value+"=channelType") == null){
								List<Dictionary> userTypeDictionary = dictionaryService.selectDictionaryByTtypeValue("channelType", value,(String)loginUserMap.get("pcCompanyId"));
								if(userTypeDictionary == null || userTypeDictionary.size() == 0) {
									errorMessageList.add("第"+(rows+1)+"行 :系统中没有找到该"+titleList[i]);
								}else{
									dictionaryMap.put(value+"=channelType",userTypeDictionary.get(0).getId());
								}
							}
						}else if(i == 8 && StringUtils.isNotBlank(value) ){
							Matcher matcher = pattern.matcher(value);
							// 字符串是否与正则表达式相匹配
							boolean rs = matcher.matches();
							if(rs == false){
								errorMessageList.add("第" + (rows + 1) + "行 :金额格式错误");
							}
						}
					}
				}
			}
		}

		//当没有任何错误信息的时候

		if(errorMessageList.size() > 0){
			ResponseDbCenter responseDbCenter = new ResponseDbCenter();
			returnMap.put("errorMessage", errorMessageList);
			responseDbCenter.setResModel(returnMap);
			return responseDbCenter;
		}

		System.out.println("保存：总共多少列："+xsheet.getLastRowNum());
		for( int rows=1;rows<=xsheet.getLastRowNum();rows++){

			XSSFRow row = xsheet.getRow(rows);//取得某一行   对象
			Map<String,Object> companyMap = new HashMap<>();
			Map<String,Object> companyDetailMap = new HashMap<>();
			Map<String,Object> userInfoMap = new HashMap<>();
			Map<String,Object> user = null;

			Payment payment = new Payment();

			String companyId = UUID.randomUUID().toString();
			String userId = UUID.randomUUID().toString();
			String paymentId = UUID.randomUUID().toString();
			for (int i = 0; i <= 12; i++) {
				String value = "";
				if(row.getCell(i) != null){
					value = row.getCell(i).getStringCellValue();
					row.getCell(i).setCellType(Cell.CELL_TYPE_STRING);
				}
				//标题



				if(i == 1 && StringUtils.isNotBlank(value)){
					//缴费日期

						row.getCell(i).setCellType(Cell.CELL_TYPE_NUMERIC);

						double paymentDateDouble = 0;

						paymentDateDouble = row.getCell(i).getNumericCellValue();
						int paymentDateTemt = (int)paymentDateDouble;
						Calendar cal = Calendar.getInstance();
						cal.set(1900, 0, -1);
						cal.add(Calendar.DAY_OF_MONTH, paymentDateTemt);
						value = DateUtils.dateToString(cal.getTime(), "yyyy-MM-dd");

					payment.setPaymentDate(value);
				}else if(i == 2 && StringUtils.isNotBlank(value)){
					companyMap.put("city",value);
				}else if(i == 3 && StringUtils.isNotBlank(value)){//老师名
					if(tacherMap.get(value+"="+pcCompanyId) == null){
						List<Map<String,Object>> teacherMaps = userService.selectTeacherByTeacherName(value,pcCompanyId);
						if(teacherMaps != null || teacherMaps.size() > 0){
							String teacherId = (String)teacherMaps.get(0).get("id");
							companyMap.put("companyDirectorId",teacherId);
						}else{
							companyMap.put(value+"="+pcCompanyId,teacherMaps.get(0).get("id"));
						}

					}else{
						companyMap.put("companyDirectorId",tacherMap.get(value+"="+pcCompanyId));
					}

				}else if(i == 4 && StringUtils.isNotBlank(value) ){//课程
					companyMap.put("userType", value);
				}else if(i == 5 && StringUtils.isNotBlank(value)){
					companyMap.put("contactName", value);
					userInfoMap.put("uname",value);

				}else if(i == 6 ){
					if(StringUtils.isNotBlank(value)){
						userInfoMap.put("mobile",value);
						user = userService.selectUserByOnlyMobile(value);
						if(user == null){
							user = new HashMap<>();
							user.put("mobile",value);
							user.put("id",userId);
							user.put("uname",userInfoMap.get("uname"));
							user.put("platform","1");
							user.put("status","0");
							user.put("createDate",DateUtils.currtime());
							user.put("createBy",loginUserId);
						}else if(null != userInfoMap.get("uname") && !userInfoMap.get("uname").equals(user.get("uname"))){
							user.put("uname",userInfoMap.get("uname"));
							user.put("updateDate",DateUtils.currtime());
							user.put("updatedBy",loginUserId);
						}
						userInfoMap.put("id", UUID.randomUUID().toString());
						userInfoMap.put("companyId", companyId);
						userInfoMap.put("userId", user.get("id"));
						userInfoMap.put("createDate",new Date());
						userInfoMap.put("createdBy",loginUserId);
						companyMap.put("contactPhone", value);
					}
					if(userInfoMap.get("uname") != null && null == userInfoMap.get("mobile")){
						error.add("第 "+rows+"行,客户姓名和联系电话必须同时存在。 ");
						errorRow.put(rows,"rows");
					}
					if(userInfoMap.get("uname") == null && null != userInfoMap.get("mobile")){
						error.add("第 "+rows+"行,客户姓名和联系电话必须同时存在。 ");
						errorRow.put(rows,"rows");
					}

				}else if( i == 7 && StringUtils.isNotBlank(value)){//企业名称
					List<HashMap<String, Object>> companie = companyServie.checkCompany(value,pcCompanyId);
					if(companie != null && companie.size() >0){
						error.add("第 "+rows+" 行 ,"+value+"  已存在。");
						errorRow.put(rows,"rows");
					}else{
						companyMap.put("id",companyId);
						companyMap.put("companyName",value);
						companyMap.put("updateDate",new Date());
						companyMap.put("paymentId", payment.getPaymentMoney()!=null ? paymentId:null);
						companyMap.put("contactUserId", user !=null ?user.get("id"):null);
						companyMap.put("status","0");
						companyDetailMap.put("id",companyId);
						companyDetailMap.put("companyName",value);
						companyDetailMap.put("updateDate",new Date());

						companyMap.put("qccUpdatedDate",DateUtils.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss"));
						String companyInfo =CompanyInfoApi.queryCompanyInfo("http://i.yjapi.com/ECISimple/GetDetailsByName", value,1,10);


						//企查查有数据的时候
						if(companyInfo != null){
							JsonObject json = (JsonObject) parse.parse(companyInfo);  //创建jsonObject对象
							if("200".equals(json.get("Status").getAsString())){
								Map<String,Map> qcc = companyServie.getQCC(json,companyMap,companyDetailMap);
								companyMap.putAll(qcc.get("companyMap"));
								companyDetailMap.putAll(qcc.get("companyDetailMap"));
							}
						}
					}

				}else if(i == 8 && StringUtils.isNotBlank(value) && !value.equals("0")){
					payment.setId(paymentId);
					payment.setPaymentMoney(value);
					payment.setCreateDate(new Date());
					payment.setCreatedBy(loginUserId);
					payment.setUpdateDate(payment.getCreateDate());
					payment.setUpdatedBy(loginUserId);
					payment.setCompanyId(companyId);
				}else if(i == 9 ){
					if(StringUtils.isNotBlank(value) && ("定金".equals(value) || "全款".equals(value))){
						payment.setMoneySituation(value);
					}
					if(StringUtils.isNotBlank(payment.getPaymentMoney()) && StringUtils.isBlank(payment.getMoneySituation())){
						error.add("第 "+rows+"行，定金或全款对应的收款合计必须同时存在，并且金额大于0。 ");
						errorRow.put(rows,"rows");
					}
					if(StringUtils.isBlank(payment.getPaymentMoney()) && StringUtils.isNotBlank(payment.getMoneySituation())){
						error.add("第 "+rows+"行，定金或全款对应的收款合计必须同时存在，并且金额大于0。 ");
						errorRow.put(rows,"rows");
					}


				}else if(i == 10 && StringUtils.isNotBlank(value)){
					payment.setRemark(value);
				}else if(i == 11 && StringUtils.isNotBlank(value) ){//渠道
					companyMap.put("channel", value);
				}else if(i == 12 && StringUtils.isNotBlank(value)){//顾问
					if(tacherMap.get(value+"="+pcCompanyId) == null){
						List<Map<String,Object>> teacherMaps = userService.selectTeacherByTeacherName(value,pcCompanyId);
						if(teacherMaps != null || teacherMaps.size() > 0){
							String teacherId = (String)teacherMaps.get(0).get("id");
							companyMap.put("companyAdviserId",teacherId);
						}else{
							companyMap.put(value+"="+pcCompanyId,teacherMaps.get(0).get("id"));
						}
					}else{
						companyMap.put("companyAdviserId",tacherMap.get(value+"="+pcCompanyId));
					}
				}

				if(i == 12 && (errorRow == null || StringUtils.isBlank(errorRow.get(rows))) && null != companyMap.get("id")){

					//企业不存在的时候新建企业
					if(null != payment.getPaymentMoney()){
						payment.setCompanyId(companyMap.get("id").toString() );
						System.out.println("保存钱-----------------------："+payment.toString());
						paymentService.insertPayment(payment);
						companyMap.put("paymentDate", payment.getPaymentDate());
						companyMap.put("paymentMoney", payment.getPaymentMoney());
						companyMap.put("moneySituation",payment.getMoneySituation());
					}
					//if(companie != null && companie.size() > 0){
						//error.add(companyMap.get("companyName")+"  已存在。不能导入");
						/*String existsCompanyId = companie.get(0).getId();
						companyMap.put("id",existsCompanyId);
						companyMap.put("status",0);
						companyDetailMap.put("id",existsCompanyId);
						if(StringUtils.isBlank(companie.get(0).getDepartmentId())){
							Map<String, Object> map = userService.selectUserById(loginUserId);
							if(map.get("departmentId") != null){
								companyMap.put("departmentId",(String) map.get("departmentId"));
							}
						}
						companyServie.updateCompanyInfoById(companyMap);
						companyServie.updateCompanyDetailInfoById(companyDetailMap);
						solrService.updateCompanyIndex(existsCompanyId);*/
					//}else {
						companyMap.put("attendStatus","未参会");
						companyMap.put("createdBy",loginUserId);
						companyMap.put("createDate",new Date());
						companyDetailMap.put("createDate",new Date());
						companyDetailMap.put("createdBy",loginUserId);

						if(mapp.get("departmentId") != null){
							companyMap.put("departmentId",(String) mapp.get("departmentId"));
						}
						companyMap.put("pcCompanyId",pcCompanyId);
						System.out.println("保存企业-----------------------："+companyMap.toString());
						companyServie.insertCompanyInfo(companyMap);
						companyServie.insertCompanyDetailInfo(companyDetailMap);
						successCompanyId.add(companyMap.get("id").toString());
					if(null != user ){
						if(userId.equals(user.get("id"))){
							System.out.println("保存用户-----------------------："+user.toString());
							userService.insertDcUser(user);
						}else{
							System.out.println("修改用户-----------------------："+user.toString());
							userService.updateUserById(user);
						}
						userInfoMap.put("companyId",companyMap.get("id"));
						userInfoService.replaceUserInfo(userInfoMap);
					}
					}

				//}
		}
	}
		}catch (Exception e){
			e.printStackTrace();
			return  ResponseConstants.FUNC_SERVERERROR;
		}
		ResponseDbCenter responseDbCenter = new ResponseDbCenter();
		if(error.size() > 0){
			Map<String,Object> rrmap = new HashMap<>();
			rrmap.put("repeatCompany", error);
			responseDbCenter.setResModel(rrmap);
		}
		for (int i = 0; i < successCompanyId.size(); i++) {
			solrService.updateCompanyIndex(successCompanyId.get(i));
		}
		return responseDbCenter;
	}
	/**
	 *
	 * @author: xiaoz
	 * @2017年7月4日
	 * @功能描述:上传企业Excell,录入企业信息
	 * @param req
	 * @return
	 */
	@ApiOperation(value = "批量导入公司信息")
	@ResponseBody
	@RequestMapping(value = "/uploadCompanyDocument2", method =  {RequestMethod.POST})
	public ResponseDbCenter uploadCompanyDocument2(@RequestParam(value = "files", required = false) MultipartFile[] files,HttpServletRequest req) throws Exception {


		// 当前登录用户的Id
		String loginUserId = (String) req.getAttribute("loginUserId");

		String pcCompanyId = null;
		Map<String, Object> mapp = userService.selectUserById(loginUserId);
		if(mapp !=null && mapp.size() > 0){
			String roleId = (String)mapp.get("roleId");
			if(!"1".equals(roleId)){
				pcCompanyId = (String)mapp.get("pcCompanyId");
			}
		}

		Map<String,Object> loginUserMap=userService.selectUserById(loginUserId);


		if (files == null) {

			return ResponseConstants.MISSING_PARAMTER;
		}

		List<String> repeatCompanyMessageList = new ArrayList<>();

		//try {


			String errorMessage = "";
			List<String> errorMessageList = new ArrayList<>();
			List<String> repeatCompanyMobileRowsList = new ArrayList<>();
			Map<String,Object> returnMap = new HashMap<>();

			JsonParser parse =new JsonParser();  //创建json解析器

			//任何浮点型数字
			String regEx = "^\\d+(\\.\\d+)?$";
			// 编译正则表达式
			Pattern pattern = Pattern.compile(regEx);

			for (MultipartFile file : files) {

						/*
						 * 做兼容Excell2003、Excell2003以上版本
						 */

				XSSFWorkbook xSSFWorkbook = null;
				HSSFWorkbook hssfWorkbook = null;

				try {

					xSSFWorkbook = new XSSFWorkbook(new BufferedInputStream(file.getInputStream()));

				} catch (Exception e) {

					hssfWorkbook = new HSSFWorkbook(new BufferedInputStream(file.getInputStream()));
				}

				XSSFSheet xsheet = null;
				HSSFSheet hsheet = null;

				if(xSSFWorkbook == null){

					hsheet = hssfWorkbook.getSheetAt(0);  //获取 某个表  ，一般默认是第一个表
					System.out.println("第一种导入：总共多少列："+xsheet.getLastRowNum());
					for( int rows=0;rows<=hsheet.getLastRowNum();rows++){//有多少行

						if(rows == 1){

							continue;
						}

						HSSFRow row = hsheet.getRow(rows);//取得某一行   对象

						String xuhaoString = null;
						double xuhaoNumber = 0;
						//序号
						if(rows == 0){

							xuhaoString = row.getCell(0).getStringCellValue();

						}
						/*else {

							xuhaoNumber = row.getCell(0).getNumericCellValue();
						}*/

						String paymentDate = "";

						//缴费日期
						if(rows == 0){

							row.getCell(1).setCellType(Cell.CELL_TYPE_STRING);
							paymentDate = row.getCell(1).getStringCellValue();

						}else {

							double paymentDateDouble = 0;

							if(row.getCell(1) == null){

								continue;
							}

							paymentDateDouble = row.getCell(1).getNumericCellValue();

							int paymentDateTemt = (int)paymentDateDouble;

							Calendar cal = Calendar.getInstance();
							cal.set(1900, 0, -1);
							cal.add(Calendar.DAY_OF_MONTH, paymentDateTemt);

							paymentDate = DateUtils.dateToString(cal.getTime(), "yyyy-MM-dd");
						}

						//城市
//					            String city = row.getCell(2).getStringCellValue();
						//授课老师名
						String shouketeacherName = row.getCell(3).getStringCellValue();
						//客户类型
						String userType = row.getCell(4).getStringCellValue();
						//客户姓名
						String userName = row.getCell(5).getStringCellValue();
						//联系电话
						row.getCell(6).setCellType(Cell.CELL_TYPE_STRING);
						String mobile = row.getCell(6).getStringCellValue();
						//企业名称
						String companyName = row.getCell(7).getStringCellValue();
						//收款合计(元)
						row.getCell(8).setCellType(Cell.CELL_TYPE_STRING);
						String paymentMoney = row.getCell(8).getStringCellValue();
						//全款情况
						String moneySituation = row.getCell(9).getStringCellValue();
						//备注
						String remark = row.getCell(10).getStringCellValue();
						//渠道
						String channel = row.getCell(11).getStringCellValue();
						//顾问
						String adviserName = row.getCell(12).getStringCellValue();

					            /*
					             * 判断表头信息，是否正确
					             */
						if(rows == 0){

							if(StringUtils.isBlank(xuhaoString)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第1列  :序号不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"序号".equals(xuhaoString) && !"\n序号\n".equals(xuhaoString)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第1列  :表头名称为序号";
								errorMessageList.add(errorMessage);
							}


							if(StringUtils.isBlank(paymentDate)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第2列  :日期不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"日期".equals(paymentDate) && !"\n日期\n".equals(paymentDate)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第2列  :表头名称为日期";
								errorMessageList.add(errorMessage);
							}


							if(StringUtils.isBlank(shouketeacherName)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第4列  :老师名不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"老师名".equals(shouketeacherName) && !"\n老师名\n".equals(shouketeacherName)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第4列  :表头为老师名";
								errorMessageList.add(errorMessage);
							}


							if(StringUtils.isBlank(userType)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第5列  :课程名不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"课程名".equals(userType) && !"\n课程名\n".equals(userType)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第5列  :表头为课程名";
								errorMessageList.add(errorMessage);
							}


							if(StringUtils.isBlank(userName)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第6列  :表头客户姓名不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"客户姓名".equals(userName) && !"\n客户姓名\n".equals(userName)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第6列  :表头为客户姓名";
								errorMessageList.add(errorMessage);
							}


							if(StringUtils.isBlank(mobile)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第7列  :表头联系电话不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"联系电话".equals(mobile) && !"\n联系电话\n".equals(mobile)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第7列  :表头为联系电话";
								errorMessageList.add(errorMessage);
							}


							if(StringUtils.isBlank(companyName)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第8列  :表头客户企业名称不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"客户企业名称".equals(companyName) && !"\n客户企业名称\n".equals(companyName)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第8列  :表头为客户企业名称";
								errorMessageList.add(errorMessage);
							}


							if(StringUtils.isBlank(paymentMoney)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第9列  :表头收款合计（元）不能为空";
								errorMessageList.add(errorMessage);

							}else if (paymentMoney.indexOf("收款合计") < 0 && paymentMoney.indexOf("\n收款合计\n") < 0) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第9列  :表头为收款合计（元）";
								errorMessageList.add(errorMessage);
							}


							if(StringUtils.isBlank(moneySituation)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第10列  :表头定金或全款不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"定金或全款".equals(moneySituation) && !"\n定金或全款\n".equals(moneySituation)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第10列  :表头为定金或全款";
								errorMessageList.add(errorMessage);
							}

							if(StringUtils.isBlank(remark)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第11列  :表头备注不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"备注".equals(remark) && !"\n备注\n".equals(remark)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第11列  :表头为备注";
								errorMessageList.add(errorMessage);
							}


							if(StringUtils.isBlank(channel)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第12列  :表头渠道不能为空";
								errorMessageList.add(errorMessage);
							}else if (!"渠道".equals(channel) && !"\n渠道\n".equals(channel)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第12列  :表头为渠道";
								errorMessageList.add(errorMessage);
							}

							if(StringUtils.isBlank(adviserName)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第13列  :表头顾问不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"顾问".equals(adviserName) && !"\n顾问\n".equals(adviserName)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第13列  :表头为顾问";
								errorMessageList.add(errorMessage);
							}
							//如果不是第一行，则检查数据是否正确规范
						}else if (StringUtils.isBlank(companyName) && StringUtils.isBlank(userType) && StringUtils.isBlank(userName)
								&& StringUtils.isBlank(mobile) && StringUtils.isBlank(paymentMoney) && StringUtils.isBlank(moneySituation)
								&& StringUtils.isBlank(remark) && StringUtils.isBlank(channel) && StringUtils.isBlank(adviserName)) {
							continue;
						}else if(StringUtils.isBlank(companyName)){
							int rowTemt = rows + 1;
							errorMessage = "第"+rowTemt+"行 :企业名称不能为空";
							errorMessageList.add(errorMessage);
						}else {

							List<Company> companies = companyServie.selectCompanyByName(companyName,pcCompanyId);

							if(companies != null && companies.size() > 0){
//					            		int rowTemt = rows + 1;
//					            		errorMessage = "第"+rowTemt+"行 :企业名称系统中已存在，请删掉这行记录再导入";

								Map<String, Object>  map =  userService.selectUserByMobile(mobile,null);
								if(map != null && map.size() > 0){
									repeatCompanyMessageList.add(companyName+"      手机号"+mobile);
									repeatCompanyMobileRowsList.add(rows+"");
								}

							}

							String companyInfo = CompanyInfoApi.queryCompanyInfo("http://i.yjapi.com/ECISimple/GetDetailsByName", companyName,1,10);
							//检查该企业是否在企查查存在，存在的话检查是否在数据库中已经存在
							if(companyInfo != null){

								JsonObject json = (JsonObject) parse.parse(companyInfo);  //创建jsonObject对象

								if(!"200".equals(json.get("Status").getAsString())){

								}else{

									String name = json.get("Result").getAsJsonObject().get("Name").isJsonNull()?"":json.get("Result").getAsJsonObject().get("Name").getAsString();

									List<Company> qicCompanies = companyServie.selectCompanyByName(name,pcCompanyId);

									if(qicCompanies != null && qicCompanies.size() > 0){
//						            			int rowTemt = rows + 1;
//							            		errorMessage = "第"+rowTemt+"行 :企业名称通过企查查询到系统中已存在，企业名称为---"+name+"---请删掉这行记录再导入";
										Map<String, Object>  map =  userService.selectUserByMobile(mobile,null);
										if(map != null && map.size() > 0){
											repeatCompanyMessageList.add(companyName+"      手机号"+mobile);
											repeatCompanyMobileRowsList.add(rows+"");
										}
									}
								}
							}

							if(StringUtils.isNotBlank(shouketeacherName)){

								List<Map<String,Object>> teacherMaps = userService.selectTeacherByTeacherName(shouketeacherName,pcCompanyId);
								if(teacherMaps == null || teacherMaps.size() == 0){
									int rowTemt = rows + 1;
									errorMessage = "第"+rowTemt+"行 :系统中没有找到该老师名";
									errorMessageList.add(errorMessage);
								}
							}

							if(StringUtils.isNotBlank(adviserName)){

								List<Map<String,Object>> adviserMaps = userService.selectTeacherByTeacherName(adviserName,pcCompanyId);

								if(adviserMaps == null || adviserMaps.size() == 0){
									int rowTemt = rows + 1;
									errorMessage = "第"+rowTemt+"行  :系统中没有找到该顾问";
									errorMessageList.add(errorMessage);
								}
							}

							if(StringUtils.isNotBlank(moneySituation) && !"定金".equals(moneySituation) && !"全款".equals(moneySituation)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行  :定金或全款书写错误";
								errorMessageList.add(errorMessage);
							}

							if(StringUtils.isNotBlank(userType)){

								List<Dictionary> userTypeDictionary = dictionaryService.selectDictionaryByTtypeValue("userType", userType,(String)loginUserMap.get("pcCompanyId"));

								if(userTypeDictionary == null || userTypeDictionary.size() == 0){
									int rowTemt = rows + 1;
									errorMessage = "第"+rowTemt+"行  :系统中没有找到该课程名";
									errorMessageList.add(errorMessage);
								}
							}

							if(StringUtils.isNotBlank(channel)){

								List<Dictionary> channelDictionary = dictionaryService.selectDictionaryByTtypeValue("channelType", channel,(String)loginUserMap.get("pcCompanyId"));

								if(channelDictionary == null || channelDictionary.size() == 0){
									int rowTemt = rows + 1;
									errorMessage = "第"+rowTemt+"行  :系统中没有找到该渠道";
									errorMessageList.add(errorMessage);
								}
							}

							if(StringUtils.isNotBlank(paymentMoney)){

								Matcher matcher = pattern.matcher(paymentMoney);
								// 字符串是否与正则表达式相匹配
								boolean rs = matcher.matches();

								if(rs == false){
									int rowTemt = rows + 1;
									errorMessage = "第"+rowTemt+"行  :金额格式错误";
									errorMessageList.add(errorMessage);
								}
							}

						}
					}

				}else {

					xsheet = xSSFWorkbook.getSheetAt(0);  //获取 某个表  ，一般默认是第一个表
					System.out.println("第二种导入：总共多少列："+xsheet.getLastRowNum());
					for( int rows=0;rows<=xsheet.getLastRowNum();rows++){//有多少行
						System.out.println("----------------------"+rows);
						/*if(rows == 1){

							continue;
						}*/

						XSSFRow row = xsheet.getRow(rows);//取得某一行   对象

						String xuhaoString = null;
						double xuhaoNumber = 0;
						//序号
						if(rows == 0){

							xuhaoString = row.getCell(0).getStringCellValue();

						}
						/*else {


							xuhaoNumber = row.getCell(0).getNumericCellValue();
						}*/

						String paymentDate = "";

						//缴费日期
						if(rows == 0){

							row.getCell(1).setCellType(Cell.CELL_TYPE_STRING);
							paymentDate = row.getCell(1).getStringCellValue();

						}else {

							double paymentDateDouble = 0;

							if(row.getCell(1) == null){

								continue;
							}

							paymentDateDouble = row.getCell(1).getNumericCellValue();

							int paymentDateTemt = (int)paymentDateDouble;

							Calendar cal = Calendar.getInstance();
							cal.set(1900, 0, -1);
							cal.add(Calendar.DAY_OF_MONTH, paymentDateTemt);

							paymentDate = DateUtils.dateToString(cal.getTime(), "yyyy-MM-dd");
						}

						//城市
//					            String city = row.getCell(2).getStringCellValue();
						//授课老师名
						String shouketeacherName = row.getCell(3).getStringCellValue();

						//客户类型
						String userType = row.getCell(4).getStringCellValue();
						//客户姓名
						String userName = row.getCell(5).getStringCellValue();
						//联系电话
						row.getCell(6).setCellType(Cell.CELL_TYPE_STRING);
						String mobile = row.getCell(6).getStringCellValue();
						System.out.println("---------mobile-------------"+mobile);

						//企业名称
						String companyName = row.getCell(7).getStringCellValue();
						System.out.println("---------companyName-------------"+companyName);
						//缴费金额
						row.getCell(8).setCellType(Cell.CELL_TYPE_STRING);
						System.out.println("---------row.getCell(8)-------------"+row.getCell(8));
						String paymentMoney = row.getCell(8).getStringCellValue();
						//全款情况
						String moneySituation = row.getCell(9).getStringCellValue();
						//备注
						String remark = row.getCell(10).getStringCellValue();
						//渠道
						String channel = row.getCell(11).getStringCellValue();
						//顾问
						String adviserName = row.getCell(12).getStringCellValue();

					            /*
					             * 判断表头信息，是否正确
					             */
						if(rows == 0){

							if(StringUtils.isBlank(xuhaoString)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第1列  :序号不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"序号".equals(xuhaoString) && !"\n序号\n".equals(xuhaoString)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第1列  :表头名称为序号";
								errorMessageList.add(errorMessage);
							}


							if(StringUtils.isBlank(paymentDate)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第2列  :日期不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"日期".equals(paymentDate) && !"\n日期\n".equals(paymentDate)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第2列  :表头名称为日期";
								errorMessageList.add(errorMessage);
							}


							if(StringUtils.isBlank(shouketeacherName)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第4列  :老师名不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"老师名".equals(shouketeacherName) && !"\n老师名\n".equals(shouketeacherName)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第4列  :表头为老师名";
								errorMessageList.add(errorMessage);
							}


							if(StringUtils.isBlank(userType)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第5列  :课程名不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"课程名".equals(userType) && !"\n课程名\n".equals(userType)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第5列  :表头为课程名";
								errorMessageList.add(errorMessage);
							}


							if(StringUtils.isBlank(userName)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第6列  :表头客户姓名不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"客户姓名".equals(userName) && !"\n客户姓名\n".equals(userName)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第6列  :表头为客户姓名";
								errorMessageList.add(errorMessage);
							}


							if(StringUtils.isBlank(mobile)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第7列  :表头联系电话不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"联系电话".equals(mobile) && !"\n联系电话\n".equals(mobile)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第7列  :表头为联系电话";
								errorMessageList.add(errorMessage);
							}


							if(StringUtils.isBlank(companyName)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第8列  :表头客户企业名称不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"客户企业名称".equals(companyName) && !"\n客户企业名称\n".equals(companyName)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第8列  :表头为客户企业名称";
								errorMessageList.add(errorMessage);
							}


							if(StringUtils.isBlank(paymentMoney)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第9列  :表头收款合计（元）不能为空";
								errorMessageList.add(errorMessage);

							}else if (paymentMoney.indexOf("收款合计") < 0 && paymentMoney.indexOf("\n收款合计\n") < 0) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第9列  :表头为收款合计（元）";
								errorMessageList.add(errorMessage);
							}


							if(StringUtils.isBlank(moneySituation)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第10列  :表头定金或全款不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"定金或全款".equals(moneySituation) && !"\n定金或全款\n".equals(moneySituation)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第10列  :表头为定金或全款";
								errorMessageList.add(errorMessage);
							}

							if(StringUtils.isBlank(remark)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第11列  :表头备注不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"备注".equals(remark) && !"\n备注\n".equals(remark)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第11列  :表头为备注";
								errorMessageList.add(errorMessage);
							}


							if(StringUtils.isBlank(channel)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第12列  :表头渠道不能为空";
								errorMessageList.add(errorMessage);
							}else if (!"渠道".equals(channel) && !"\n渠道\n".equals(channel)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第12列  :表头为渠道";
								errorMessageList.add(errorMessage);
							}

							if(StringUtils.isBlank(adviserName)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第13列  :表头顾问不能为空";
								errorMessageList.add(errorMessage);

							}else if (!"顾问".equals(adviserName) && !"\n顾问\n".equals(adviserName)) {
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行第13列  :表头为顾问";
								errorMessageList.add(errorMessage);
							}
							//如果不是第一行，则检查数据是否正确规范
						            /*
						             *  String shouketeacherName = row.getCell(3).getStringCellValue();
					            //客户类型
					            String userType = row.getCell(4).getStringCellValue();
					            //客户姓名
					            String userName = row.getCell(5).getStringCellValue();
					            //联系电话
					            row.getCell(6).setCellType(Cell.CELL_TYPE_STRING);
					            String mobile = row.getCell(6).getStringCellValue();
					            //企业名称
					            String companyName = row.getCell(7).getStringCellValue();
					            //缴费金额
					            row.getCell(8).setCellType(Cell.CELL_TYPE_STRING);
					            String paymentMoney = row.getCell(8).getStringCellValue();
					            //全款情况
					            String moneySituation = row.getCell(9).getStringCellValue();
					            //备注
					            String remark = row.getCell(10).getStringCellValue();
					            //渠道
					            String channel = row.getCell(11).getStringCellValue();
					            //顾问
					            String adviserName = row.getCell(12).getStringCellValue();
						             */
						}else if (StringUtils.isBlank(companyName) && StringUtils.isBlank(userType) && StringUtils.isBlank(userName)
								&& StringUtils.isBlank(mobile) && StringUtils.isBlank(paymentMoney) && StringUtils.isBlank(moneySituation)
								&& StringUtils.isBlank(remark) && StringUtils.isBlank(channel) && StringUtils.isBlank(adviserName)) {
							continue;
						}else if(StringUtils.isBlank(companyName)){
							int rowTemt = rows + 1;
							errorMessage = "第"+rowTemt+"行 :企业名称不能为空";
							errorMessageList.add(errorMessage);
						}else {

							List<Company> companies = companyServie.selectCompanyByName(companyName,pcCompanyId);

							if(companies != null && companies.size() > 0){
//					            		int rowTemt = rows + 1;
//					            		errorMessage = "第"+rowTemt+"行 :企业名称系统中已存在，请删掉这行记录再导入";
								Map<String, Object>  map =  userService.selectUserByMobile(mobile,null);
								if(map != null && map.size() > 0){
									repeatCompanyMessageList.add(companyName+"      手机号"+mobile);
									repeatCompanyMobileRowsList.add(rows+"");
								}
							}

							String companyInfo = CompanyInfoApi.queryCompanyInfo("http://i.yjapi.com/ECISimple/GetDetailsByName", companyName,1,10);
							//检查该企业是否在企查查存在，存在的话检查是否在数据库中已经存在
							if(companyInfo != null){

								JsonObject json = (JsonObject) parse.parse(companyInfo);  //创建jsonObject对象

								if(!"200".equals(json.get("Status").getAsString())){

								}else{

									String name = json.get("Result").getAsJsonObject().get("Name").isJsonNull()?"":json.get("Result").getAsJsonObject().get("Name").getAsString();

									List<Company> qicCompanies = companyServie.selectCompanyByName(name,pcCompanyId);

									if(qicCompanies != null && qicCompanies.size() > 0){
//						            			int rowTemt = rows + 1;
//							            		errorMessage = "第"+rowTemt+"行 :企业名称通过企查查询到系统中已存在，企业名称为---"+name+"---请删掉这行记录再导入";
										Map<String, Object>  map =  userService.selectUserByMobile(mobile,null);
										if(map != null && map.size() > 0){
											repeatCompanyMessageList.add(name+"      手机号"+mobile);
											repeatCompanyMobileRowsList.add(rows+"");
										}
									}
								}
							}

							if(StringUtils.isNotBlank(shouketeacherName)){

								List<Map<String,Object>> teacherMaps = userService.selectTeacherByTeacherName(shouketeacherName,pcCompanyId);
								if(teacherMaps == null || teacherMaps.size() == 0){
									int rowTemt = rows + 1;
									errorMessage = "第"+rowTemt+"行 :系统中没有找到该老师名";
									errorMessageList.add(errorMessage);
								}
							}

							if(StringUtils.isNotBlank(adviserName)){

								List<Map<String,Object>> adviserMaps = userService.selectTeacherByTeacherName(adviserName,pcCompanyId);
								if(adviserMaps == null || adviserMaps.size() == 0){
									int rowTemt = rows + 1;
									errorMessage = "第"+rowTemt+"行  :系统中没有找到该顾问";
									errorMessageList.add(errorMessage);
								}
							}

							if(StringUtils.isNotBlank(moneySituation) && !"定金".equals(moneySituation) && !"全款".equals(moneySituation)){
								int rowTemt = rows + 1;
								errorMessage = "第"+rowTemt+"行  :定金或全款书写错误";
								errorMessageList.add(errorMessage);
							}

							if(StringUtils.isNotBlank(userType)){

								List<Dictionary> userTypeDictionary = dictionaryService.selectDictionaryByTtypeValue("userType", userType,(String)loginUserMap.get("pcCompanyId"));

								if(userTypeDictionary == null || userTypeDictionary.size() == 0){
									int rowTemt = rows + 1;
									errorMessage = "第"+rowTemt+"行  :系统中没有找到该课程名";
									errorMessageList.add(errorMessage);
								}
							}

							if(StringUtils.isNotBlank(channel)){

								List<Dictionary> channelDictionary = dictionaryService.selectDictionaryByTtypeValue("channelType", channel,(String)loginUserMap.get("pcCompanyId"));

								if(channelDictionary == null || channelDictionary.size() == 0){
									int rowTemt = rows + 1;
									errorMessage = "第"+rowTemt+"行  :系统中没有找到该渠道";
									errorMessageList.add(errorMessage);
								}
							}

							if(StringUtils.isNotBlank(paymentMoney)){

								Matcher matcher = pattern.matcher(paymentMoney);
								// 字符串是否与正则表达式相匹配
								boolean rs = matcher.matches();

								if(rs == false){
									int rowTemt = rows + 1;
									errorMessage = "第"+rowTemt+"行  :金额格式错误";
									errorMessageList.add(errorMessage);
								}
							}
						}
					}
				}
			}

					/*
					 * 当没有任何错误信息的时候
					 */
			if(errorMessageList.size() > 0){

				ResponseDbCenter responseDbCenter = new ResponseDbCenter();

				returnMap.put("errorMessage", errorMessageList);
				/*returnMap.put("repeatCompany", repeatCompanyMessageList);*/

				responseDbCenter.setResModel(returnMap);

				return responseDbCenter;
			}

					 /*
					  * 数据没有问题之后，就将数据插入到数据库
					  */
			for (MultipartFile file : files) {


						 /*
							 * 做兼容Excell2003、Excell2003以上版本
							 */

				XSSFWorkbook xSSFWorkbook = null;
				HSSFWorkbook hssfWorkbook = null;

				try {

					xSSFWorkbook = new XSSFWorkbook(new BufferedInputStream(file.getInputStream()));

				} catch (Exception e) {

					hssfWorkbook = new HSSFWorkbook(new BufferedInputStream(file.getInputStream()));
				}

				XSSFSheet xsheet = null;
				HSSFSheet hsheet = null;

				if(xSSFWorkbook == null){

					hsheet = hssfWorkbook.getSheetAt(0);  //获取 某个表  ，一般默认是第一个表
						System.out.println("第一种插入：");
					for( int rows=1;rows<=hsheet.getLastRowNum();rows++){//有多少行

						if(repeatCompanyMobileRowsList.size() > 0 && repeatCompanyMobileRowsList.contains(rows+"")){
							continue;
						}

						HSSFRow row = hsheet.getRow(rows);//取得某一行   对象

						String xuhaoString = null;
						double xuhaoNumber = 0;
						//序号
						if(rows == 0){

							xuhaoString = row.getCell(0).getStringCellValue();

						}
						/*else {

							xuhaoNumber = row.getCell(0).getNumericCellValue();
						}*/

						String paymentDate = "";

						//缴费日期
						if(rows == 0){

							row.getCell(1).setCellType(Cell.CELL_TYPE_STRING);
							paymentDate = row.getCell(1).getStringCellValue();

						}else {

							double paymentDateDouble = 0;

							if(row.getCell(1) == null){

								continue;
							}

							paymentDateDouble = row.getCell(1).getNumericCellValue();

							int paymentDateTemt = (int)paymentDateDouble;

							Calendar cal = Calendar.getInstance();
							cal.set(1900, 0, -1);
							cal.add(Calendar.DAY_OF_MONTH, paymentDateTemt);

							paymentDate = DateUtils.dateToString(cal.getTime(), "yyyy-MM-dd");
						}

						//城市
//						            String city = row.getCell(2).getStringCellValue();
						//授课老师名
						String shouketeacherName = row.getCell(3).getStringCellValue();
						//客户类型
						String userType = row.getCell(4).getStringCellValue();
						//客户姓名
						String userName = row.getCell(5).getStringCellValue();
						//联系电话
						row.getCell(6).setCellType(Cell.CELL_TYPE_STRING);
						String mobile = row.getCell(6).getStringCellValue();
						//企业名称
						String companyName = row.getCell(7).getStringCellValue();
						//缴费金额
						row.getCell(8).setCellType(Cell.CELL_TYPE_STRING);
						String paymentMoney = row.getCell(8).getStringCellValue();
						//全款情况
						String moneySituation = row.getCell(9).getStringCellValue();
						//备注
						String remark = row.getCell(10).getStringCellValue();
						//渠道
						String channel = row.getCell(11).getStringCellValue();
						//顾问
						String adviserName = row.getCell(12).getStringCellValue();

						if(StringUtils.isBlank(companyName) && StringUtils.isBlank(userType) && StringUtils.isBlank(userName)
								&& StringUtils.isBlank(mobile) && StringUtils.isBlank(paymentMoney) && StringUtils.isBlank(moneySituation)
								&& StringUtils.isBlank(remark) && StringUtils.isBlank(channel) && StringUtils.isBlank(adviserName)){

							continue;
						}


						String companyInfo = CompanyInfoApi.queryCompanyInfo("http://i.yjapi.com/ECISimple/GetDetailsByName", companyName,1,10);

						if(companyInfo != null){

							JsonObject json = (JsonObject) parse.parse(companyInfo);  //创建jsonObject对象

							if(!"200".equals(json.get("Status").getAsString())){

								Map<String,Object> companyMap = new HashMap<>();
								Map<String,Object> companyDetailMap = new HashMap<>();
								Map<String,Object> userMap = new HashMap<>();
								Map<String,Object> userMapDetail = new HashMap<>();
								Map<String,Object> userInfoMap = new HashMap<>();

								Payment payment = new Payment();

								String companyId = UUID.randomUUID().toString();
								String userId = UUID.randomUUID().toString();
								String userCompanyId = UUID.randomUUID().toString();
								String paymentId = UUID.randomUUID().toString();

								userInfoMap.put("id", userCompanyId);
								userInfoMap.put("companyId", companyId);
								userInfoMap.put("userId", userId);
								userInfoMap.put("createDate",new Date());
								userInfoMap.put("createdBy",loginUserId);


								List<Map<String,Object>> adviserMaps = userService.selectTeacherByTeacherName(adviserName,pcCompanyId);

								if(adviserMaps != null && adviserMaps.size() > 0){

									String adviserId = (String)adviserMaps.get(0).get("id");
									companyMap.put("companyAdviserId",adviserId);
								}

								List<Map<String,Object>> teacherMaps = userService.selectTeacherByTeacherName(shouketeacherName,pcCompanyId);

								if(teacherMaps != null && teacherMaps.size() > 0){

									String teacherId = (String)teacherMaps.get(0).get("id");
									companyMap.put("teacherId",teacherId);
								}

							            	/*List<Dictionary> userTypeDictionary = dictionaryService.selectDictionaryByTtypeValue("userType", userType);

						            		if(userTypeDictionary != null && userTypeDictionary.size() > 0){

						            			String userTypeId = userTypeDictionary.get(0).getId();
						            			payment.setUserType(userTypeId);
						            		}*/

						            		/*List<Dictionary> channelDictionary = dictionaryService.selectDictionaryByTtypeValue("channelType", channel);

						            		if(channelDictionary != null && channelDictionary.size() > 0){

						            			String channelId = channelDictionary.get(0).getId();
						            			payment.setChannel(channelId);
						            		}*/

								payment.setId(paymentId);
								payment.setPaymentDate(paymentDate);
								payment.setPaymentMoney(paymentMoney);
								payment.setMoneySituation(moneySituation);
								payment.setRemark(remark);
								payment.setCreateDate(new Date());
								payment.setCreatedBy(loginUserId);
								payment.setCompanyId(companyId);

								paymentService.insertPayment(payment);


								companyMap.put("id",companyId);
								companyMap.put("companyName",companyName);
								companyMap.put("createDate",new Date());
								companyMap.put("updateDate",new Date());
								companyMap.put("createdBy",loginUserId);
								companyMap.put("paymentId", paymentId);
								companyMap.put("userType", userType);
								companyMap.put("channel", channel);
								companyMap.put("contactUserId", userId);
								companyMap.put("contactPhone", mobile);
								companyMap.put("status","0");

								String SumMoney = paymentService.selectSumMoneyByCompanyId(companyId);

								companyMap.put("paymentMoney", SumMoney);
//						            		companyMap.put("paymentDate", paymentDate);

								companyDetailMap.put("id",companyId);
								companyDetailMap.put("companyName",companyName);
								companyDetailMap.put("createDate",new Date());
								companyDetailMap.put("updateDate",new Date());
								companyDetailMap.put("createdBy",loginUserId);
								companyMap.put("qccUpdatedDate",DateUtils.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss"));

								/*Map<String, Object>  map =  userService.selectUserByMobile(mobile,null);

								//如果这个手机号已经存在，就将这个用户绑定到用户企业关联表中
								if(map != null && map.size() > 0){

									String existsUserId = (String)map.get("id");
									String existUname = (String)map.get("uname");
									String existMobile = (String)map.get("mobile");
									String existPhone = (String)map.get("phone");

									userInfoMap.put("id", userCompanyId);
									userInfoMap.put("companyId", companyId);
									userInfoMap.put("userId", existsUserId);
									userInfoMap.put("createDate",new Date());
									userInfoMap.put("createdBy",loginUserId);

									//插入与用户企业关联表
									userInfoService.insertUserInfo(userInfoMap);

									companyMap.put("contactUserId", existsUserId);
									companyMap.put("contactName", existUname);
									companyMap.put("contactPhone", (existMobile==null || "".equals(existMobile)) ?existPhone:existMobile);

								}else {

									userInfoMap.put("id", userCompanyId);
									userInfoMap.put("companyId", companyId);
									userInfoMap.put("userId", userId);
									userInfoMap.put("createDate",new Date());
									userInfoMap.put("createdBy",loginUserId);

									userMap.put("id",userId);
									userMap.put("uname", userName);
									userMap.put("mobile", mobile);
									userMap.put("platform", "1");
									userMap.put("status", "0");
									userMap.put("createdBy",loginUserId);
									userMap.put("createDate", new Date());

									userMapDetail.put("id",userId);
									userMapDetail.put("createdBy",loginUserId);
									userMapDetail.put("createDate", new Date());

									userService.insertDcUser(userMap);
									userService.insertDcUserDetail(userMapDetail);

									//插入与用户企业关联表
									userInfoService.insertUserInfo(userInfoMap);
								}*/

								List<Company> companies = companyServie.selectAllStatusCompanyByName(companyName,pcCompanyId);

								if(companies != null && companies.size() > 0){

									String existsCompanyId = companies.get(0).getId();
									companyMap.put("id",existsCompanyId);
									companyMap.put("status",0);
									companyDetailMap.put("id",existsCompanyId);

									if(StringUtils.isBlank(companies.get(0).getDepartmentId())){
										Map<String, Object> map = userService.selectUserById(loginUserId);
										if(map.get("departmentId") != null){
											companyMap.put("departmentId",(String) map.get("departmentId"));
										}
									}

									companyServie.updateCompanyInfoById(companyMap);
									companyServie.updateCompanyDetailInfoById(companyDetailMap);
									userInfoService.changeUserInfo("insert",userName,null,"",existsCompanyId,null,"0",null,null,"",mobile,"","","",loginUserId,null);
									solrService.updateCompanyIndex(existsCompanyId);

								}else {

									Map<String, Object> map = userService.selectUserById(loginUserId);
									if(map.get("departmentId") != null){
										companyMap.put("departmentId",(String) map.get("departmentId"));
									}


									companyMap.put("pcCompanyId",pcCompanyId);
									companyServie.insertCompanyInfo(companyMap);
									companyServie.insertCompanyDetailInfo(companyDetailMap);
									userInfoService.changeUserInfo("insert",userName,null,"",companyId,null,"0",null,null,"",mobile,"","","",loginUserId,null);
									solrService.updateCompanyIndex(companyId);
 								}

							}else{

								System.err.println("----------------"+companyName+"=====>"+(json.get("Result").getAsJsonObject().get("Name").isJsonNull()?"":json.get("Result").getAsJsonObject().get("Name").getAsString())+"----------------");

								//返回企业信息正确
								String establish_time = "";
								String operating_period = "";
								String approve_date = "";
								String updated_date = "";
								String termStart = "";

								try {

									establish_time = json.get("Result").getAsJsonObject().get("StartDate")==null?"":json.get("Result").getAsJsonObject().get("StartDate").getAsString().substring(0, 10);//get("StartDate").getAsString().substring(0, 10)
									operating_period = json.get("Result").getAsJsonObject().get("TeamEnd")==null?"":json.get("Result").getAsJsonObject().get("TeamEnd").getAsString().substring(0, 10);
									approve_date = json.get("Result").getAsJsonObject().get("CheckDate")==null?"":json.get("Result").getAsJsonObject().get("CheckDate").getAsString().substring(0, 10);
									updated_date = json.get("Result").getAsJsonObject().get("UpdatedDate")==null?"":json.get("Result").getAsJsonObject().get("UpdatedDate").getAsString().substring(0, 10);
									termStart = json.get("Result").getAsJsonObject().get("TermStart")==null?"":json.get("Result").getAsJsonObject().get("TermStart").getAsString().substring(0, 10);

								} catch (Exception e) {

									e.printStackTrace();
								}

//								            String id = json.get("Result").getAsJsonObject().get("KeyNo").isJsonNull()?null:json.get("Result").getAsJsonObject().get("KeyNo").getAsString();
								String social_credit = json.get("Result").getAsJsonObject().get("CreditCode").isJsonNull()?null:json.get("Result").getAsJsonObject().get("CreditCode").getAsString();
								String registration_code = json.get("Result").getAsJsonObject().get("No").isJsonNull()?null:json.get("Result").getAsJsonObject().get("No").getAsString();
								String organization_code = json.get("Result").getAsJsonObject().get("No").isJsonNull()?null:json.get("Result").getAsJsonObject().get("No").getAsString();
								String manage_type = json.get("Result").getAsJsonObject().get("Status").isJsonNull()?null:json.get("Result").getAsJsonObject().get("Status").getAsString();
								String representative = json.get("Result").getAsJsonObject().get("OperName").isJsonNull()?null:json.get("Result").getAsJsonObject().get("OperName").getAsString();
								String registered_money = json.get("Result").getAsJsonObject().get("RegistCapi").isJsonNull()?null:json.get("Result").getAsJsonObject().get("RegistCapi").getAsString();
								String registration_authority = json.get("Result").getAsJsonObject().get("BelongOrg").isJsonNull()?null:json.get("Result").getAsJsonObject().get("BelongOrg").getAsString();
								String companyAddress = json.get("Result").getAsJsonObject().get("Address").isJsonNull()?null:json.get("Result").getAsJsonObject().get("Address").getAsString();
								String scope = json.get("Result").getAsJsonObject().get("Scope").isJsonNull()?"":json.get("Result").getAsJsonObject().get("Scope").getAsString();
//						            		String province = json.get("Result").getAsJsonObject().get("Province").isJsonNull()?"":json.get("Result").getAsJsonObject().get("Province").getAsString();
								String econKind = json.get("Result").getAsJsonObject().get("EconKind").isJsonNull()?"":json.get("Result").getAsJsonObject().get("EconKind").getAsString();
								String name = json.get("Result").getAsJsonObject().get("Name").isJsonNull()?"":json.get("Result").getAsJsonObject().get("Name").getAsString();

								Map<String,Object> companyMap = new HashMap<>();
								Map<String,Object> companyDetailMap = new HashMap<>();
								Map<String,Object> userMap = new HashMap<>();
								Map<String,Object> userMapDetail = new HashMap<>();
								Map<String,Object> userInfoMap = new HashMap<>();

								Payment payment = new Payment();

								String companyId = UUID.randomUUID().toString();
								String userId = UUID.randomUUID().toString();
								String userCompanyId = UUID.randomUUID().toString();
								String paymentId = UUID.randomUUID().toString();
								String addressId = UUID.randomUUID().toString();

								List<Map<String,Object>> adviserMaps = userService.selectTeacherByTeacherName(adviserName,pcCompanyId);

								if(adviserMaps != null && adviserMaps.size() > 0){

									String adviserId = (String)adviserMaps.get(0).get("id");
									companyMap.put("companyAdviserId",adviserId);
								}


								List<Map<String,Object>> teacherMaps = userService.selectTeacherByTeacherName(shouketeacherName,pcCompanyId);

								if(teacherMaps != null && teacherMaps.size() > 0){

									String teacherId = (String)teacherMaps.get(0).get("id");
									companyMap.put("teacherId",teacherId);
								}

							            	/*List<Dictionary> userTypeDictionary = dictionaryService.selectDictionaryByTtypeValue("userType", userType);

						            		if(userTypeDictionary != null && userTypeDictionary.size() > 0){

						            			String userTypeId = userTypeDictionary.get(0).getId();
						            			payment.setUserType(userTypeId);
						            		}*/

						            		/*List<Dictionary> channelDictionary = dictionaryService.selectDictionaryByTtypeValue("channelType", channel);

						            		if(channelDictionary != null && channelDictionary.size() > 0){

						            			String channelId = channelDictionary.get(0).getId();
						            			payment.setChannel(channelId);
						            		}*/


								//拆分地址
								String province = null;
								String city = null;
								String area = null;
								String county = null;
								String country = null;
								//Map<String, String> mapCompanyAddresss = SplitAddress.getCountryMap(companyAddress);
								AddressData addressData = BaiduAddressUtil.parseAddressToCountryProvinceCityCounty(companyAddress);
								// {area=c区, county=d县, provinces=a省, country=a省b市c区d县, city=b市}
								if(addressData != null){

									//	province = mapCompanyAddresss.get("provinces");
									province = addressData.getProvince();
									//	city = mapCompanyAddresss.get("city");
									city = addressData.getCity();
									//	area = mapCompanyAddresss.get("area");
									//	county = mapCompanyAddresss.get("county");
									county = addressData.getCounty();
									//	country = mapCompanyAddresss.get("country");
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

								payment.setId(paymentId);
								payment.setPaymentDate(paymentDate);
								payment.setPaymentMoney(paymentMoney);
								payment.setMoneySituation(moneySituation);
								payment.setRemark(remark);
								payment.setCreateDate(new Date());
								payment.setCreatedBy(loginUserId);
								payment.setCompanyId(companyId);

								paymentService.insertPayment(payment);

								companyMap.put("id",companyId);
								companyMap.put("companyName",name);
								companyMap.put("representative",representative);
								companyMap.put("paymentId", paymentId);
								companyMap.put("userType", userType);
								companyMap.put("channel", channel);
								companyMap.put("contactUserId", userId);
								companyMap.put("contactName", userName);
								companyMap.put("contactPhone", mobile);
								companyMap.put("createDate",new Date());
								companyMap.put("updateDate",new Date());
								companyMap.put("createdBy",loginUserId);
								companyMap.put("status","0");

								String SumMoney = paymentService.selectSumMoneyByCompanyId(companyId);

								companyMap.put("paymentMoney", SumMoney);
//						            		companyMap.put("paymentDate", paymentDate);

								companyDetailMap.put("id",companyId);
								companyDetailMap.put("companyName",name);
								companyDetailMap.put("socialCredit",social_credit);
								companyDetailMap.put("registrationCode",registration_code);
								companyDetailMap.put("organizationCode",organization_code);
								companyDetailMap.put("manageType",manage_type);
								companyDetailMap.put("manageScope",scope);
								companyDetailMap.put("registeredMoney",registered_money);
								companyDetailMap.put("registrationAuthority",registration_authority);
								companyDetailMap.put("content",scope);
								companyDetailMap.put("econKind",econKind);
								companyDetailMap.put("establishTime",establish_time);
								companyDetailMap.put("approveDate",approve_date);
								companyDetailMap.put("termStart",termStart);
								companyDetailMap.put("operatingPeriod",operating_period);
								companyDetailMap.put("createDate",new Date());
								companyDetailMap.put("updateDate",new Date());
								companyDetailMap.put("createdBy",loginUserId);
								companyMap.put("qccUpdatedDate",DateUtils.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss"));

								/*Map<String, Object>  map =  userService.selectUserByMobile(mobile,null);

								//如果这个手机号已经存在，就将这个用户绑定到用户企业关联表中
								if(map != null && map.size() > 0){

									String existsUserId = (String)map.get("id");
									String existUname = (String)map.get("uname");
									String existMobile = (String)map.get("mobile");
									String existPhone = (String)map.get("phone");

									userInfoMap.put("id", userCompanyId);
									userInfoMap.put("companyId", companyId);
									userInfoMap.put("userId", existsUserId);
									userInfoMap.put("createDate",new Date());
									userInfoMap.put("createdBy",loginUserId);

									//插入与用户企业关联表
									userInfoService.insertUserInfo(userInfoMap);

									companyMap.put("contactUserId", existsUserId);
									companyMap.put("contactName", existUname);
									companyMap.put("contactPhone", (existMobile==null || "".equals(existMobile)) ?existPhone:existMobile);

								}else {

									userInfoMap.put("id", userCompanyId);
									userInfoMap.put("companyId", companyId);
									userInfoMap.put("userId", userId);
									userInfoMap.put("createDate",new Date());
									userInfoMap.put("createdBy",loginUserId);

									userMap.put("id",userId);
									userMap.put("uname", userName);
									userMap.put("mobile", mobile);
									userMap.put("platform", "1");
									userMap.put("status", "0");
									userMap.put("createdBy",loginUserId);
									userMap.put("createDate", new Date());

									userMapDetail.put("id",userId);
									userMapDetail.put("createdBy",loginUserId);
									userMapDetail.put("createDate", new Date());

									userService.insertDcUser(userMap);
									userService.insertDcUserDetail(userMapDetail);

									//插入与用户企业关联表
									userInfoService.insertUserInfo(userInfoMap);
								}*/


								List<Company> companies = companyServie.selectAllStatusCompanyByName(companyName,pcCompanyId);

								if(companies != null && companies.size() > 0){

									String existsCompanyId = companies.get(0).getId();
									companyMap.put("id",existsCompanyId);
									companyMap.put("status",0);
									companyDetailMap.put("id",existsCompanyId);

									if(StringUtils.isBlank(companies.get(0).getDepartmentId())){
										Map<String, Object> map = userService.selectUserById(loginUserId);
										if(map.get("departmentId") != null){
											companyMap.put("departmentId",(String) map.get("departmentId"));
										}
									}

									companyServie.updateCompanyInfoById(companyMap);
									companyServie.updateCompanyDetailInfoById(companyDetailMap);
									userInfoService.changeUserInfo("insert",userName,null,"",existsCompanyId,null,"0",null,null,"",mobile,"","","",loginUserId,null);
									solrService.updateCompanyIndex(existsCompanyId);
								}else {


									Map<String, Object> map = userService.selectUserById(loginUserId);
									if(map.get("departmentId") != null){
										companyMap.put("departmentId",(String) map.get("departmentId"));
									}

									companyMap.put("pcCompanyId",pcCompanyId);
									companyServie.insertCompanyInfo(companyMap);
									companyServie.insertCompanyDetailInfo(companyDetailMap);
									userInfoService.changeUserInfo("insert",userName,null,"",companyId,null,"0",null,null,"",mobile,"","","",loginUserId,null);
									solrService.updateCompanyIndex(companyId);
 								}

							}
							//如果企查查没有查到企业信息，就直接插入
						}else {

							Map<String,Object> companyMap = new HashMap<>();
							Map<String,Object> companyDetailMap = new HashMap<>();
							Map<String,Object> userMap = new HashMap<>();
							Map<String,Object> userMapDetail = new HashMap<>();
							Map<String,Object> userInfoMap = new HashMap<>();

							Payment payment = new Payment();

							String companyId = UUID.randomUUID().toString();
							String userId = UUID.randomUUID().toString();
							String userCompanyId = UUID.randomUUID().toString();
							String paymentId = UUID.randomUUID().toString();

							userInfoMap.put("id", userCompanyId);
							userInfoMap.put("companyId", companyId);
							userInfoMap.put("userId", userId);
							userInfoMap.put("createDate",new Date());
							userInfoMap.put("createdBy",loginUserId);

							List<Map<String,Object>> adviserMaps = userService.selectTeacherByTeacherName(adviserName,pcCompanyId);

							if(adviserMaps != null && adviserMaps.size() > 0){

								String adviserId = (String)adviserMaps.get(0).get("id");
								companyMap.put("companyAdviserId",adviserId);
							}

							List<Map<String,Object>> teacherMaps = userService.selectTeacherByTeacherName(shouketeacherName,pcCompanyId);

							if(teacherMaps != null && teacherMaps.size() > 0){

								String teacherId = (String)teacherMaps.get(0).get("id");
								companyMap.put("teacherId",teacherId);
							}

						            	/*List<Dictionary> userTypeDictionary = dictionaryService.selectDictionaryByTtypeValue("userType", userType);

					            		if(userTypeDictionary != null && userTypeDictionary.size() >0){

					            			String userTypeId = userTypeDictionary.get(0).getId();
					            			payment.setUserType(userTypeId);
					            		}*/

					            		/*List<Dictionary> channelDictionary = dictionaryService.selectDictionaryByTtypeValue("channelType", channel);

					            		if(channelDictionary != null && channelDictionary.size() > 0){

					            			String channelId = channelDictionary.get(0).getId();
					            			payment.setChannel(channelId);
					            		}*/

							payment.setId(paymentId);;
							payment.setPaymentDate(paymentDate);
							payment.setPaymentMoney(paymentMoney);
							payment.setMoneySituation(moneySituation);
							payment.setRemark(remark);
							payment.setCreateDate(new Date());
							payment.setCreatedBy(loginUserId);
							payment.setCompanyId(companyId);

							paymentService.insertPayment(payment);

							companyMap.put("id",companyId);
							companyMap.put("companyName",companyName);
							companyMap.put("createDate",new Date());
							companyMap.put("updateDate",new Date());
							companyMap.put("createdBy",loginUserId);
							companyMap.put("paymentId", paymentId);
							companyMap.put("userType", userType);
							companyMap.put("channel", channel);
							companyMap.put("contactUserId", userId);
							companyMap.put("contactName", userName);
							companyMap.put("contactPhone", mobile);
							companyMap.put("status","0");

							String SumMoney = paymentService.selectSumMoneyByCompanyId(companyId);
//					            		companyMap.put("paymentDate", paymentDate);
							companyMap.put("paymentMoney", SumMoney);

							companyDetailMap.put("id",companyId);
							companyDetailMap.put("companyName",companyName);
							companyDetailMap.put("createDate",new Date());
							companyDetailMap.put("updateDate",new Date());
							companyDetailMap.put("createdBy",loginUserId);
							companyMap.put("qccUpdatedDate",DateUtils.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss"));

							/*Map<String, Object>  map =  userService.selectUserByMobile(mobile,null);

							//如果这个手机号已经存在，就将这个用户绑定到用户企业关联表中
							if(map != null && map.size() > 0){

								String existsUserId = (String)map.get("id");
								String existUname = (String)map.get("uname");
								String existMobile = (String)map.get("mobile");
								String existPhone = (String)map.get("phone");

								userInfoMap.put("id", userCompanyId);
								userInfoMap.put("companyId", companyId);
								userInfoMap.put("userId", existsUserId);
								userInfoMap.put("createDate",new Date());
								userInfoMap.put("createdBy",loginUserId);

								//插入与用户企业关联表
								userInfoService.insertUserInfo(userInfoMap);

								companyMap.put("contactUserId", existsUserId);
								companyMap.put("contactName", existUname);
								companyMap.put("contactPhone", (existMobile==null || "".equals(existMobile)) ?existPhone:existMobile);

							}else {

								userInfoMap.put("id", userCompanyId);
								userInfoMap.put("companyId", companyId);
								userInfoMap.put("userId", userId);
								userInfoMap.put("createDate",new Date());
								userInfoMap.put("createdBy",loginUserId);

								userMap.put("id",userId);
								userMap.put("uname", userName);
								userMap.put("mobile", mobile);
								userMap.put("platform", "1");
								userMap.put("status", "0");
								userMap.put("createdBy",loginUserId);
								userMap.put("createDate", new Date());

								userMapDetail.put("id",userId);
								userMapDetail.put("createdBy",loginUserId);
								userMapDetail.put("createDate", new Date());

								userService.insertDcUser(userMap);
								userService.insertDcUserDetail(userMapDetail);

								//插入与用户企业关联表
								userInfoService.insertUserInfo(userInfoMap);
							}*/

							List<Company> companies = companyServie.selectAllStatusCompanyByName(companyName,pcCompanyId);

							if(companies != null && companies.size() > 0){

								String existsCompanyId = companies.get(0).getId();
								companyMap.put("id",existsCompanyId);
								companyMap.put("status",0);
								companyDetailMap.put("id",existsCompanyId);

								if(StringUtils.isBlank(companies.get(0).getDepartmentId())){
									Map<String, Object> map = userService.selectUserById(loginUserId);
									if(map.get("departmentId") != null){
										companyMap.put("departmentId",(String) map.get("departmentId"));
									}
								}

								companyServie.updateCompanyInfoById(companyMap);
								companyServie.updateCompanyDetailInfoById(companyDetailMap);
								userInfoService.changeUserInfo("insert",userName,null,"",existsCompanyId,null,"0",null,null,"",mobile,"","","",loginUserId,null);
								solrService.updateCompanyIndex(existsCompanyId);
							}else {

								Map<String, Object> map = userService.selectUserById(loginUserId);
								if(map.get("departmentId") != null){
									companyMap.put("departmentId",(String) map.get("departmentId"));
								}

								companyMap.put("pcCompanyId",pcCompanyId);
								companyServie.insertCompanyInfo(companyMap);
								companyServie.insertCompanyDetailInfo(companyDetailMap);
								userInfoService.changeUserInfo("insert",userName,null,"",companyId,null,"0",null,null,"",mobile,"","","",loginUserId,null);
								solrService.updateCompanyIndex(companyId);
							}
						}
					}

				}else {

					xsheet = xSSFWorkbook.getSheetAt(0);  //获取 某个表  ，一般默认是第一个表
					System.out.println("第二种插入：总共多少列："+xsheet.getLastRowNum());
					for( int rows=1;rows<=xsheet.getLastRowNum();rows++){//有多少行

						if(repeatCompanyMobileRowsList.size() > 0 && repeatCompanyMobileRowsList.contains(rows+"")){
							continue;
						}

						XSSFRow row = xsheet.getRow(rows);//取得某一行   对象

						String xuhaoString = null;
						double xuhaoNumber = 0;
						//序号
						if(rows == 0 || rows == 1){
							row.getCell(0).setCellType(Cell.CELL_TYPE_STRING);
							xuhaoString = row.getCell(0).getStringCellValue();

						}
						/*else {

							xuhaoNumber = row.getCell(0).getNumericCellValue();
						}*/

						String paymentDate = "";

						//缴费日期
						if(rows == 0){

							row.getCell(1).setCellType(Cell.CELL_TYPE_STRING);
							paymentDate = row.getCell(1).getStringCellValue();

						}else {

							double paymentDateDouble = 0;

							if(row.getCell(1) != null){

								paymentDateDouble = row.getCell(1).getNumericCellValue();

								int paymentDateTemt = (int)paymentDateDouble;

								Calendar cal = Calendar.getInstance();
								cal.set(1900, 0, -1);
								cal.add(Calendar.DAY_OF_MONTH, paymentDateTemt);

								paymentDate = DateUtils.dateToString(cal.getTime(), "yyyy-MM-dd");
							}


						}

						//城市
//						            String city = row.getCell(2).getStringCellValue();
						//授课老师名
						String shouketeacherName = row.getCell(3).getStringCellValue();
						//客户类型
						String userType = row.getCell(4).getStringCellValue();
						//客户姓名
						String userName = row.getCell(5).getStringCellValue();
						//联系电话
						row.getCell(6).setCellType(Cell.CELL_TYPE_STRING);
						String mobile = row.getCell(6).getStringCellValue();
						//企业名称
						String companyName = row.getCell(7).getStringCellValue();
						//缴费金额
						row.getCell(8).setCellType(Cell.CELL_TYPE_STRING);
						String paymentMoney = row.getCell(8).getStringCellValue();
						//全款情况
						String moneySituation = row.getCell(9).getStringCellValue();
						//备注
						String remark = row.getCell(10).getStringCellValue();
						//渠道
						String channel = row.getCell(11).getStringCellValue();
						//顾问
						String adviserName = row.getCell(12).getStringCellValue();

						if(StringUtils.isBlank(companyName) && StringUtils.isBlank(userType) && StringUtils.isBlank(userName)
								&& StringUtils.isBlank(mobile) && StringUtils.isBlank(paymentMoney) && StringUtils.isBlank(moneySituation)
								&& StringUtils.isBlank(remark) && StringUtils.isBlank(channel) && StringUtils.isBlank(adviserName)){

							continue;
						}

						String companyInfo = CompanyInfoApi.queryCompanyInfo("http://i.yjapi.com/ECISimple/GetDetailsByName", companyName,1,10);
						System.out.println("企查查验证企业是否存在："+companyName+ "    "+companyInfo );
						if(companyInfo != null){

							JsonObject json = (JsonObject) parse.parse(companyInfo);  //创建jsonObject对象

							if(!"200".equals(json.get("Status").getAsString())){

								Map<String,Object> companyMap = new HashMap<>();
								Map<String,Object> companyDetailMap = new HashMap<>();
								Map<String,Object> userMap = new HashMap<>();
								Map<String,Object> userMapDetail = new HashMap<>();
								Map<String,Object> userInfoMap = new HashMap<>();

								Payment payment = new Payment();

								String companyId = UUID.randomUUID().toString();
								String userId = UUID.randomUUID().toString();
								String userCompanyId = UUID.randomUUID().toString();
								String paymentId = UUID.randomUUID().toString();

								userInfoMap.put("id", userCompanyId);
								userInfoMap.put("companyId", companyId);
								userInfoMap.put("userId", userId);
								userInfoMap.put("createDate",new Date());
								userInfoMap.put("createdBy",loginUserId);

								List<Map<String,Object>> adviserMaps = userService.selectTeacherByTeacherName(adviserName,pcCompanyId);

								if(adviserMaps != null && adviserMaps.size() > 0){

									String adviserId = (String)adviserMaps.get(0).get("id");
									companyMap.put("companyAdviserId",adviserId);
								}

								List<Map<String,Object>> teacherMaps = userService.selectTeacherByTeacherName(shouketeacherName,pcCompanyId);

								if(teacherMaps != null && teacherMaps.size() > 0){

									String teacherId = (String)teacherMaps.get(0).get("id");
									companyMap.put("teacherId",teacherId);
								}
							            	
							            	/*List<Dictionary> userTypeDictionary = dictionaryService.selectDictionaryByTtypeValue("userType", userType);
						            		
						            		if(userTypeDictionary != null && userTypeDictionary.size() > 0){
						            			
						            			String userTypeId = userTypeDictionary.get(0).getId();
						            			payment.setUserType(userTypeId);
						            		}*/
						            		
						            		/*List<Dictionary> channelDictionary = dictionaryService.selectDictionaryByTtypeValue("channelType", channel);
						            		
						            		if(channelDictionary != null && channelDictionary.size() > 0){
						            			
						            			String channelId = channelDictionary.get(0).getId();
						            			payment.setChannel(channelId);
						            		}*/

								payment.setId(paymentId);;
								payment.setPaymentDate(paymentDate);
								payment.setPaymentMoney(paymentMoney);
								payment.setMoneySituation(moneySituation);
								payment.setRemark(remark);
								payment.setCreateDate(new Date());
								payment.setCreatedBy(loginUserId);
								payment.setCompanyId(companyId);

								paymentService.insertPayment(payment);

								companyMap.put("id",companyId);
								companyMap.put("companyName",companyName);
								companyMap.put("createDate",new Date());
								companyMap.put("updateDate",new Date());
								companyMap.put("createdBy",loginUserId);
								companyMap.put("paymentId", paymentId);
								companyMap.put("userType", userType);
								companyMap.put("channel", channel);
								companyMap.put("contactUserId", userId);
								companyMap.put("contactName", userName);
								companyMap.put("contactPhone", mobile);
								companyMap.put("status","0");

								String SumMoney = paymentService.selectSumMoneyByCompanyId(companyId);

								companyMap.put("paymentMoney", SumMoney);
//						            		companyMap.put("paymentDate", paymentDate);

								companyDetailMap.put("id",companyId);
								companyDetailMap.put("companyName",companyName);
								companyDetailMap.put("createDate",new Date());
								companyDetailMap.put("updateDate",new Date());
								companyDetailMap.put("createdBy",loginUserId);
								companyMap.put("qccUpdatedDate",DateUtils.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss"));

								/*Map<String, Object>  map =  userService.selectUserByMobile(mobile,null);

								//如果这个手机号已经存在，就将这个用户绑定到用户企业关联表中
								if(map != null && map.size() > 0){

									String existsUserId = (String)map.get("id");
									String existUname = (String)map.get("uname");
									String existMobile = (String)map.get("mobile");
									String existPhone = (String)map.get("phone");

									userInfoMap.put("id", userCompanyId);
									userInfoMap.put("companyId", companyId);
									userInfoMap.put("userId", existsUserId);
									userInfoMap.put("createDate",new Date());
									userInfoMap.put("createdBy",loginUserId);

									//插入与用户企业关联表
									userInfoService.insertUserInfo(userInfoMap);

									companyMap.put("contactUserId", existsUserId);
									companyMap.put("contactName", existUname);
									companyMap.put("contactPhone", (existMobile==null || "".equals(existMobile)) ?existPhone:existMobile);

								}else {

									userInfoMap.put("id", userCompanyId);
									userInfoMap.put("companyId", companyId);
									userInfoMap.put("userId", userId);
									userInfoMap.put("createDate",new Date());
									userInfoMap.put("createdBy",loginUserId);

									userMap.put("id",userId);
									userMap.put("uname", userName);
									userMap.put("mobile", mobile);
									userMap.put("platform", "1");
									userMap.put("status", "0");
									userMap.put("createdBy",loginUserId);
									userMap.put("createDate", new Date());

									userMapDetail.put("id",userId);
									userMapDetail.put("createdBy",loginUserId);
									userMapDetail.put("createDate", new Date());

									userService.insertDcUser(userMap);
									userService.insertDcUserDetail(userMapDetail);

									//插入与用户企业关联表
									userInfoService.insertUserInfo(userInfoMap);
								}*/


								List<Company> companies = companyServie.selectAllStatusCompanyByName(companyName,pcCompanyId);

								if(companies != null && companies.size() > 0){

									String existsCompanyId = companies.get(0).getId();
									companyMap.put("id",existsCompanyId);
									companyMap.put("status",0);
									companyDetailMap.put("id",existsCompanyId);

									if(StringUtils.isBlank(companies.get(0).getDepartmentId())){
										Map<String, Object> map = userService.selectUserById(loginUserId);
										if(map.get("departmentId") != null){
											companyMap.put("departmentId",(String) map.get("departmentId"));
										}
									}

									companyServie.updateCompanyInfoById(companyMap);
									companyServie.updateCompanyDetailInfoById(companyDetailMap);
									userInfoService.changeUserInfo("insert",userName,null,"",existsCompanyId,null,"0",null,null,"",mobile,"","","",loginUserId,null);
									solrService.updateCompanyIndex(existsCompanyId);
								}else {

									Map<String, Object> map = userService.selectUserById(loginUserId);
									if(map.get("departmentId") != null){
										companyMap.put("departmentId",(String) map.get("departmentId"));
									}

									companyMap.put("pcCompanyId",pcCompanyId);
									companyServie.insertCompanyInfo(companyMap);
									companyServie.insertCompanyDetailInfo(companyDetailMap);
									userInfoService.changeUserInfo("insert",userName,null,"",companyId,null,"0",null,null,"",mobile,"","","",loginUserId,null);
									solrService.updateCompanyIndex(companyId);
 								}

							}else{

								System.err.println("----------------"+companyName+"=====>"+(json.get("Result").getAsJsonObject().get("Name").isJsonNull()?"":json.get("Result").getAsJsonObject().get("Name").getAsString())+"----------------");

								//返回企业信息正确
								String establish_time = "";
								String operating_period = "";
								String approve_date = "";
								String updated_date = "";
								String termStart = "";

								try {

									establish_time = json.get("Result").getAsJsonObject().get("StartDate")==null?"":json.get("Result").getAsJsonObject().get("StartDate").getAsString().substring(0, 10);//get("StartDate").getAsString().substring(0, 10)
									operating_period = json.get("Result").getAsJsonObject().get("TeamEnd")==null?"":json.get("Result").getAsJsonObject().get("TeamEnd").getAsString().substring(0, 10);
									approve_date = json.get("Result").getAsJsonObject().get("CheckDate")==null?"":json.get("Result").getAsJsonObject().get("CheckDate").getAsString().substring(0, 10);
									updated_date = json.get("Result").getAsJsonObject().get("UpdatedDate")==null?"":json.get("Result").getAsJsonObject().get("UpdatedDate").getAsString().substring(0, 10);
									termStart = json.get("Result").getAsJsonObject().get("TermStart")==null?"":json.get("Result").getAsJsonObject().get("TermStart").getAsString().substring(0, 10);

								} catch (Exception e) {
									e.printStackTrace();
								}

//								String id = json.get("Result").getAsJsonObject().get("KeyNo").isJsonNull()?null:json.get("Result").getAsJsonObject().get("KeyNo").getAsString();
								String social_credit = json.get("Result").getAsJsonObject().get("CreditCode").isJsonNull()?null:json.get("Result").getAsJsonObject().get("CreditCode").getAsString();
								String registration_code = json.get("Result").getAsJsonObject().get("No").isJsonNull()?null:json.get("Result").getAsJsonObject().get("No").getAsString();
								String organization_code = json.get("Result").getAsJsonObject().get("No").isJsonNull()?null:json.get("Result").getAsJsonObject().get("No").getAsString();
								String manage_type = json.get("Result").getAsJsonObject().get("Status").isJsonNull()?null:json.get("Result").getAsJsonObject().get("Status").getAsString();
								String representative = json.get("Result").getAsJsonObject().get("OperName").isJsonNull()?null:json.get("Result").getAsJsonObject().get("OperName").getAsString();
								String registered_money = json.get("Result").getAsJsonObject().get("RegistCapi").isJsonNull()?null:json.get("Result").getAsJsonObject().get("RegistCapi").getAsString();
								String registration_authority = json.get("Result").getAsJsonObject().get("BelongOrg").isJsonNull()?null:json.get("Result").getAsJsonObject().get("BelongOrg").getAsString();
								String companyAddress = json.get("Result").getAsJsonObject().get("Address").isJsonNull()?null:json.get("Result").getAsJsonObject().get("Address").getAsString();
								String scope = json.get("Result").getAsJsonObject().get("Scope").isJsonNull()?"":json.get("Result").getAsJsonObject().get("Scope").getAsString();
//						            		String province = json.get("Result").getAsJsonObject().get("Province").isJsonNull()?"":json.get("Result").getAsJsonObject().get("Province").getAsString();
								String econKind = json.get("Result").getAsJsonObject().get("EconKind").isJsonNull()?"":json.get("Result").getAsJsonObject().get("EconKind").getAsString();
								String name = json.get("Result").getAsJsonObject().get("Name").isJsonNull()?"":json.get("Result").getAsJsonObject().get("Name").getAsString();

								Map<String,Object> companyMap = new HashMap<>();
								Map<String,Object> companyDetailMap = new HashMap<>();
								Map<String,Object> userMap = new HashMap<>();
								Map<String,Object> userMapDetail = new HashMap<>();
								Map<String,Object> userInfoMap = new HashMap<>();

								Payment payment = new Payment();

								String companyId = UUID.randomUUID().toString();
								String userId = UUID.randomUUID().toString();
								String userCompanyId = UUID.randomUUID().toString();
								String paymentId = UUID.randomUUID().toString();
								String addressId = UUID.randomUUID().toString();

								List<Map<String,Object>> adviserMaps = userService.selectTeacherByTeacherName(adviserName,pcCompanyId);


								if(adviserMaps != null && adviserMaps.size() > 0){
									String adviserId = (String)adviserMaps.get(0).get("id");
									companyMap.put("companyAdviserId",adviserId);
								}


								List<Map<String,Object>> teacherMaps = userService.selectTeacherByTeacherName(shouketeacherName,pcCompanyId);

								if(teacherMaps != null && teacherMaps.size() > 0){

									String teacherId = (String)teacherMaps.get(0).get("id");
									companyMap.put("teacherId",teacherId);
								}
							            	
							            	/*List<Dictionary> userTypeDictionary = dictionaryService.selectDictionaryByTtypeValue("userType", userType);
						            		
						            		if(userTypeDictionary != null && userTypeDictionary.size() > 0){
						            			
						            			String userTypeId = userTypeDictionary.get(0).getId();
						            			payment.setUserType(userTypeId);
						            		}*/
						            		
						            		/*List<Dictionary> channelDictionary = dictionaryService.selectDictionaryByTtypeValue("channelType", channel);
						            		
						            		if(channelDictionary != null && channelDictionary.size() > 0){
						            			
						            			String channelId = channelDictionary.get(0).getId();
						            			payment.setChannel(channelId);
						            		}*/


								//拆分地址
								String province = null;
								String city = null;
								String area = null;
								String county = null;
								String country = null;
								//Map<String, String> mapCompanyAddresss = SplitAddress.getCountryMap(companyAddress);
								AddressData addressData = BaiduAddressUtil.parseAddressToCountryProvinceCityCounty(companyAddress);
								// {area=c区, county=d县, provinces=a省, country=a省b市c区d县, city=b市}
								if(addressData != null){

								//	province = mapCompanyAddresss.get("provinces");
									province = addressData.getProvince();
								//	city = mapCompanyAddresss.get("city");
									city = addressData.getCity();
								//	area = mapCompanyAddresss.get("area");
								//	county = mapCompanyAddresss.get("county");
									county = addressData.getCounty();
								//	country = mapCompanyAddresss.get("country");
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

								payment.setId(paymentId);
								payment.setPaymentDate(paymentDate);
								payment.setPaymentMoney(paymentMoney);
								payment.setMoneySituation(moneySituation);
								payment.setRemark(remark);
								payment.setCreateDate(new Date());
								payment.setCreatedBy(loginUserId);
								payment.setCompanyId(companyId);

								paymentService.insertPayment(payment);

								companyMap.put("id",companyId);
								companyMap.put("companyName",name);
								companyMap.put("representative",representative);
								companyMap.put("paymentId", paymentId);
								companyMap.put("userType", userType);
								companyMap.put("channel", channel);
								companyMap.put("contactUserId", userId);
								companyMap.put("contactName", userName);
								companyMap.put("contactPhone", mobile);
								companyMap.put("createDate",new Date());
								companyMap.put("updateDate",new Date());
								companyMap.put("createdBy",loginUserId);
								companyMap.put("status","0");

								String SumMoney = paymentService.selectSumMoneyByCompanyId(companyId);

								companyMap.put("paymentMoney", SumMoney);
//						            		companyMap.put("paymentDate", paymentDate);

								companyDetailMap.put("id",companyId);
								companyDetailMap.put("companyName",name);
								companyDetailMap.put("socialCredit",social_credit);
								companyDetailMap.put("registrationCode",registration_code);
								companyDetailMap.put("organizationCode",organization_code);
								companyDetailMap.put("manageType",manage_type);
								companyDetailMap.put("manageScope",scope);
								companyDetailMap.put("registeredMoney",registered_money);
								companyDetailMap.put("registrationAuthority",registration_authority);
								companyDetailMap.put("content",scope);
								companyDetailMap.put("econKind",econKind);
								companyDetailMap.put("establishTime",establish_time);
								companyDetailMap.put("approveDate",approve_date);
								companyDetailMap.put("termStart",termStart);
								companyDetailMap.put("operatingPeriod",operating_period);
								companyDetailMap.put("createDate",new Date());
								companyDetailMap.put("updateDate",new Date());
								companyDetailMap.put("createdBy",loginUserId);
								companyMap.put("qccUpdatedDate",DateUtils.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss"));

								/*Map<String, Object>  map =  userService.selectUserByMobile(mobile,null);

								//如果这个手机号已经存在，就将这个用户绑定到用户企业关联表中
								if(map != null && map.size() > 0){

									String existsUserId = (String)map.get("id");
									String existUname = (String)map.get("uname");
									String existMobile = (String)map.get("mobile");
									String existPhone = (String)map.get("phone");

									userInfoMap.put("id", userCompanyId);
									userInfoMap.put("companyId", companyId);
									userInfoMap.put("userId", existsUserId);
									userInfoMap.put("createDate",new Date());
									userInfoMap.put("createdBy",loginUserId);

									//插入与用户企业关联表
									userInfoService.insertUserInfo(userInfoMap);

									companyMap.put("contactUserId", existsUserId);
									companyMap.put("contactName", existUname);
									companyMap.put("contactPhone", (existMobile==null || "".equals(existMobile)) ?existPhone:existMobile);

								}else {

									userInfoMap.put("id", userCompanyId);
									userInfoMap.put("companyId", companyId);
									userInfoMap.put("userId", userId);
									userInfoMap.put("createDate",new Date());
									userInfoMap.put("createdBy",loginUserId);

									userMap.put("id",userId);
									userMap.put("uname", userName);
									userMap.put("mobile", mobile);
									userMap.put("platform", "1");
									userMap.put("status", "0");
									userMap.put("createdBy",loginUserId);
									userMap.put("createDate", new Date());

									userMapDetail.put("id",userId);
									userMapDetail.put("createdBy",loginUserId);
									userMapDetail.put("createDate", new Date());

									userService.insertDcUser(userMap);
									userService.insertDcUserDetail(userMapDetail);

									//插入与用户企业关联表
									userInfoService.insertUserInfo(userInfoMap);
								}*/

								List<Company> companies = companyServie.selectAllStatusCompanyByName(companyName,pcCompanyId);

								if(companies != null && companies.size() > 0){

									String existsCompanyId = companies.get(0).getId();
									companyMap.put("id",existsCompanyId);
									companyMap.put("status",0);
									companyDetailMap.put("id",existsCompanyId);

									if(StringUtils.isBlank(companies.get(0).getDepartmentId())){
										Map<String, Object> map = userService.selectUserById(loginUserId);
										if(map.get("departmentId") != null){
											companyMap.put("departmentId",(String) map.get("departmentId"));
										}
									}

									companyServie.updateCompanyInfoById(companyMap);
									companyServie.updateCompanyDetailInfoById(companyDetailMap);
									/*
									userMap.put("id",userId);
									userMap.put("uname", userName);
									userMap.put("mobile", mobile);
									userMap.put("platform", "1");
									userMap.put("status", "0");
									userMap.put("createdBy",loginUserId);
									userMap.put("createDate", new Date());
									 */
									userInfoService.changeUserInfo("insert",userName,null,"",existsCompanyId,null,"0",null,null,"",mobile,"","","",loginUserId,null);
									solrService.updateCompanyIndex(existsCompanyId);
								}else {

									Map<String, Object> map = userService.selectUserById(loginUserId);
									if(map.get("departmentId") != null){
										companyMap.put("departmentId",(String) map.get("departmentId"));
									}

									companyMap.put("pcCompanyId",pcCompanyId);
									companyServie.insertCompanyInfo(companyMap);
									companyServie.insertCompanyDetailInfo(companyDetailMap);
									userInfoService.changeUserInfo("insert",userName,null,"",companyId,null,"0",null,null,"",mobile,"","","",loginUserId,null);
									solrService.updateCompanyIndex(companyId);
								}

							}
							//如果企查查没有查到企业信息，就直接插入
						}else {

							Map<String,Object> companyMap = new HashMap<>();
							Map<String,Object> companyDetailMap = new HashMap<>();
							Map<String,Object> userMap = new HashMap<>();

							Map<String,Object> userMapDetail = new HashMap<>();
							Map<String,Object> userInfoMap = new HashMap<>();

							Payment payment = new Payment();

							String companyId = UUID.randomUUID().toString();
							String userId = UUID.randomUUID().toString();
							String userCompanyId = UUID.randomUUID().toString();
							String paymentId = UUID.randomUUID().toString();

							userInfoMap.put("id", userCompanyId);
							userInfoMap.put("companyId", companyId);
							userInfoMap.put("userId", userId);
							userInfoMap.put("createDate",new Date());
							userInfoMap.put("createdBy",loginUserId);

							List<Map<String,Object>> adviserMaps = userService.selectTeacherByTeacherName(adviserName,pcCompanyId);

							if(adviserMaps != null && adviserMaps.size() > 0){

								String adviserId = (String)adviserMaps.get(0).get("id");
								companyMap.put("companyAdviserId",adviserId);
							}

							List<Map<String,Object>> teacherMaps = userService.selectTeacherByTeacherName(shouketeacherName,pcCompanyId);

							if(teacherMaps != null && teacherMaps.size() > 0){

								String teacherId = (String)teacherMaps.get(0).get("id");
								companyMap.put("teacherId",teacherId);
							}
						            	
						            	/*List<Dictionary> userTypeDictionary = dictionaryService.selectDictionaryByTtypeValue("userType", userType);
					            		
					            		if(userTypeDictionary != null && userTypeDictionary.size() >0){
					            			
					            			String userTypeId = userTypeDictionary.get(0).getId();
					            			payment.setUserType(userTypeId);
					            		}*/
					            		
					            		/*List<Dictionary> channelDictionary = dictionaryService.selectDictionaryByTtypeValue("channelType", channel);
					            		
					            		if(channelDictionary != null && channelDictionary.size() > 0){
					            			
					            			String channelId = channelDictionary.get(0).getId();
					            			payment.setChannel(channelId);
					            		}*/

							payment.setId(paymentId);;
							payment.setPaymentDate(paymentDate);
							payment.setPaymentMoney(paymentMoney);
							payment.setMoneySituation(moneySituation);
							payment.setRemark(remark);
							payment.setCreateDate(new Date());
							payment.setCreatedBy(loginUserId);
							payment.setCompanyId(companyId);

							paymentService.insertPayment(payment);

							companyMap.put("id",companyId);
							companyMap.put("companyName",companyName);
							companyMap.put("createDate",new Date());
							companyMap.put("updateDate",new Date());
							companyMap.put("createdBy",loginUserId);
							companyMap.put("paymentId", paymentId);
							companyMap.put("userType", userType);
							companyMap.put("channel", channel);
							companyMap.put("contactUserId", userId);
							companyMap.put("contactName", userName);
							companyMap.put("contactPhone", mobile);
							companyMap.put("status","0");

							String SumMoney = paymentService.selectSumMoneyByCompanyId(companyId);
							companyMap.put("paymentMoney", SumMoney);

							companyDetailMap.put("id",companyId);
							companyDetailMap.put("companyName",companyName);
							companyDetailMap.put("createDate",new Date());
							companyDetailMap.put("updateDate",new Date());
							companyDetailMap.put("createdBy",loginUserId);
							companyMap.put("qccUpdatedDate",DateUtils.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss"));
							/*Map<String, Object>  map =  userService.selectUserByMobile(mobile,null);

							//如果这个手机号已经存在，就将这个用户绑定到用户企业关联表中
							if(map != null && map.size() > 0){

								String existsUserId = (String)map.get("id");
								String existUname = (String)map.get("uname");
								String existMobile = (String)map.get("mobile");
								String existPhone = (String)map.get("phone");

								userInfoMap.put("id", userCompanyId);
								userInfoMap.put("companyId", companyId);
								userInfoMap.put("userId", existsUserId);
								userInfoMap.put("createDate",new Date());
								userInfoMap.put("createdBy",loginUserId);

								//插入与用户企业关联表
								userInfoService.insertUserInfo(userInfoMap);

								companyMap.put("contactUserId", existsUserId);
								companyMap.put("contactName", existUname);
								companyMap.put("contactPhone", (existMobile==null || "".equals(existMobile)) ?existPhone:existMobile);

							}else {

								userInfoMap.put("id", userCompanyId);
								userInfoMap.put("companyId", companyId);
								userInfoMap.put("userId", userId);
								userInfoMap.put("createDate",new Date());
								userInfoMap.put("createdBy",loginUserId);

								userMap.put("id",userId);
								userMap.put("uname", userName);
								userMap.put("mobile", mobile);
								userMap.put("platform", "1");
								userMap.put("status", "0");
								userMap.put("createdBy",loginUserId);
								userMap.put("createDate", new Date());

								userMapDetail.put("id",userId);
								userMapDetail.put("createdBy",loginUserId);
								userMapDetail.put("createDate", new Date());

								userService.insertDcUser(userMap);
								userService.insertDcUserDetail(userMapDetail);

								//插入与用户企业关联表
								userInfoService.insertUserInfo(userInfoMap);
							}*/

							List<Company> companies = companyServie.selectAllStatusCompanyByName(companyName,pcCompanyId);

							if(companies != null && companies.size() > 0){

								String existsCompanyId = companies.get(0).getId();
								companyMap.put("id",existsCompanyId);
								companyMap.put("status",0);
								companyDetailMap.put("id",existsCompanyId);

								if(StringUtils.isBlank(companies.get(0).getDepartmentId())){
									Map<String, Object> map = userService.selectUserById(loginUserId);
									if(map.get("departmentId") != null){
										companyMap.put("departmentId",(String) map.get("departmentId"));
									}
								}

								companyServie.updateCompanyInfoById(companyMap);
								companyServie.updateCompanyDetailInfoById(companyDetailMap);
								userInfoService.changeUserInfo("insert",userName,null,"",existsCompanyId,null,"0",null,null,"",mobile,"","","",loginUserId,null);
								solrService.updateCompanyIndex(existsCompanyId);
							}else {

								Map<String, Object> map = userService.selectUserById(loginUserId);
								if(map.get("departmentId") != null){
									companyMap.put("departmentId",(String) map.get("departmentId"));
								}
								companyMap.put("pcCompanyId",pcCompanyId);
								companyServie.insertCompanyInfo(companyMap);
								companyServie.insertCompanyDetailInfo(companyDetailMap);
								userInfoService.changeUserInfo("insert",userName,null,"",companyId,null,"0",null,null,"",mobile,"","","",loginUserId,null);
								solrService.updateCompanyIndex(companyId);
							}

						}
					}
				}
			}

		/*} catch (Exception e) {

			e.printStackTrace();
			throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
		}*/

		ResponseDbCenter responseDbCenter = new ResponseDbCenter();
		if(repeatCompanyMessageList.size() > 0){

			Map<String,Object> rrmap = new HashMap<>();

			rrmap.put("repeatCompany", repeatCompanyMessageList);
			responseDbCenter.setResModel(rrmap);
		}

		return responseDbCenter;

	}

}
