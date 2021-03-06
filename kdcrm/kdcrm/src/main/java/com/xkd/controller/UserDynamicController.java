package com.xkd.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xkd.model.Company;
import com.xkd.service.CompanyService;
import com.xkd.service.SolrService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xkd.exception.GlobalException;
import com.xkd.model.ResponseConstants;
import com.xkd.model.ResponseDbCenter;
import com.xkd.service.DC_PC_UserService;
import com.xkd.service.UserDynamicService;

@Api(description = "用户动态相关接口")
@Controller
@RequestMapping("/userDynamic")
@Transactional
public class UserDynamicController extends BaseController {

    @Autowired
    private UserDynamicService userDynamicService;
    @Autowired
    private DC_PC_UserService pcUserService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    SolrService solrService;

    /**
     * 查询某一个公司下的动态
     *
     * @param req
     * @param rsp
     * @param currentPage 当前页
     * @param pageSize    每页显示多少条
     * @param orderFlag   排序规则 asc 升序  desc 降序
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/selectUserDynamicByGroupId",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "查询动态列表")
    public ResponseDbCenter selectUserDynamicByGroupId(HttpServletRequest req, HttpServletResponse rsp,
                                                       @ApiParam(value = "公司Id或银行项目Id或商机Id银行方案Id",required = true) @RequestParam(required = true) String groupId,
                                                       @ApiParam(value = "当前页",required = false)@RequestParam(required = false) Integer currentPage,
                                                       @ApiParam(value = "每页显示多少条",required = false) @RequestParam(required = false)  Integer pageSize,
                                                       @ApiParam(value = "排序规则  asc 升序  desc 降序",required = false)@RequestParam(required = false)    String orderFlag) {


         if (currentPage == null) {
            currentPage = 1;
            pageSize = 10000;
        }

        if (StringUtils.isBlank(orderFlag)) {
            orderFlag = "asc";
        } else {
            orderFlag = "desc";
        }

        if (StringUtils.isBlank(groupId)) {

            return ResponseConstants.MISSING_PARAMTER;
        }

        List<Map<String, Object>> maps = null;
        Integer count = 0;
        try {

            maps = userDynamicService.selectUserDynamicByGroupId(groupId, currentPage, pageSize, orderFlag);
            count = userDynamicService.selectUserDynamicCountByCompanyId(groupId);


        } catch (Exception e) {

            System.out.println(e);
            return ResponseConstants.FUNC_SERVERERROR;
        }

        ResponseDbCenter responseDbCenter = new ResponseDbCenter();
        responseDbCenter.setResModel(maps);
        responseDbCenter.setTotalRows(count + "");

        return responseDbCenter;
    }

    /**
     * 添加用户动态
     *
     * @param req
     * @param rsp
     * @return
     */
    @ApiOperation(value = "添加用户动态")
    @ResponseBody
    @RequestMapping(value = "/saveUserDynamic",method = {RequestMethod.GET,RequestMethod.POST})
    public ResponseDbCenter saveUserDynamic(HttpServletRequest req, HttpServletResponse rsp,
                                            @ApiParam(value = "公司Id",required = true)@RequestParam(required = true)  String groupId,
                                            @ApiParam(value = "内容",required = false)@RequestParam(required = false)  String contentValue,
                                            @ApiParam(value = "类型 0 系统动态  1 用户动态",required = false)@RequestParam(required = false)  String ttype,
                                            @ApiParam(value = "跟进类型：电话跟进， 邮件跟进， 信息跟进，  客户面谈，  微信跟进   ",required = false)@RequestParam(required = false)  String followingType,
                                            @ApiParam(value = "被联系人  ",required = false)@RequestParam(required = false)  String contactee,
                                            @ApiParam(value = " 附件URl",required = false)@RequestParam(required = false)  String imageUrl


    ) throws Exception {

        //登录员工Id
        String loginUserId = (String) req.getAttribute("loginUserId");



        if (StringUtils.isBlank(groupId)) {

            return ResponseConstants.MISSING_PARAMTER;
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", UUID.randomUUID().toString());
        paramMap.put("groupId", groupId);
        paramMap.put("createdBy", loginUserId + "");
        paramMap.put("contentValue", contentValue);
        paramMap.put("updatedBy", loginUserId + "");
        if (StringUtils.isNotBlank(ttype)){
            paramMap.put("ttype", ttype);

        }else{
            paramMap.put("ttype", 1);
        }

        paramMap.put("followingType",followingType);
        paramMap.put("contactee",contactee);
        paramMap.put("imageUrl",imageUrl);


        try {

            if (StringUtils.isNotBlank(followingType)){
                //更新企业沟通状态，为已沟通
                Map<String,Object> company=new HashMap<>();
                company.put("id",groupId);
                company.put("communicatStatus","已沟通");
                company.put("latestContactTime",new Date());
                company.put("updateDate",new Date());
                companyService.updateCompanyInfoById(company);
            }else {
                //更新企业更新时间
                Map<String,Object> company=new HashMap<>();
                company.put("id",groupId);
                company.put("updateDate",new Date());
                companyService.updateCompanyInfoById(company);
            }
            solrService.updateCompanyIndex(groupId);

            userDynamicService.saveUserDynamic(paramMap);

        } catch (Exception e) {

            e.printStackTrace();
            throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
        }

        ResponseDbCenter ResponseDbCenter = new ResponseDbCenter();

        return ResponseDbCenter;
    }

    /**
     * 添加用户动态与企业无关联
     *
     * @param req
     * @param rsp
     * @return
     */
    @ApiOperation(value = "添加用户动态")
    @ResponseBody
    @RequestMapping(value = "/saveTaskDynamic",method = {RequestMethod.GET,RequestMethod.POST})
    public ResponseDbCenter saveTaskDynamic(HttpServletRequest req, HttpServletResponse rsp,
                                            @ApiParam(value = "公司Id",required = true)@RequestParam(required = true)  String groupId,
                                            @ApiParam(value = "内容",required = false)@RequestParam(required = false)  String contentValue,
                                            @ApiParam(value = "类型 0 系统动态  1 用户动态",required = false)@RequestParam(required = false)  String ttype,
                                            @ApiParam(value = "跟进类型：电话跟进， 邮件跟进， 信息跟进，  客户面谈，  微信跟进   ",required = false)@RequestParam(required = false)  String followingType,
                                            @ApiParam(value = " 附件URl",required = false)@RequestParam(required = false)  String imageUrl


    ) throws Exception {

        //登录员工Id
        String loginUserId = (String) req.getAttribute("loginUserId");



        if (StringUtils.isBlank(groupId)) {

            return ResponseConstants.MISSING_PARAMTER;
        }




        try {

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id", UUID.randomUUID().toString());
            paramMap.put("groupId", groupId);
            paramMap.put("createdBy", loginUserId);
            paramMap.put("contentValue", contentValue);
            paramMap.put("updatedBy", loginUserId);
            if (StringUtils.isNotBlank(ttype)){
                paramMap.put("ttype", ttype);

            }else{
                paramMap.put("ttype", 1);
            }

            paramMap.put("followingType",followingType);
            paramMap.put("imageUrl",imageUrl);

            userDynamicService.saveUserDynamic(paramMap);

        } catch (Exception e) {

            e.printStackTrace();
            throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
        }

        ResponseDbCenter ResponseDbCenter = new ResponseDbCenter();

        return ResponseDbCenter;
    }

    /**
     * 删除用户动态
     *
     * @param req
     * @param rsp
     * @param ids 动态Id数组
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "删除用户动态")
    @ResponseBody
    @RequestMapping(value = "/deleteUserDynamicByIds",method = {RequestMethod.GET,RequestMethod.POST})
    public ResponseDbCenter deleteUserDynamicByIds(HttpServletRequest req, HttpServletResponse rsp,
                                                   @ApiParam(value = "动态Id数组",required = true)@RequestParam(required = true)   String[]  ids) throws Exception {


        try {
            if (ids != null && ids.length > 0) {
                List<String> idList = new ArrayList();
                for (int i = 0; i < ids.length; i++) {
                    idList.add(ids[i]);
                }
                userDynamicService.deleteUserDynamicByIds(idList);

            }


        } catch (Exception e) {

            e.printStackTrace();
            throw new GlobalException(ResponseConstants.FUNC_SERVERERROR);
        }

        ResponseDbCenter ResponseDbCenter = new ResponseDbCenter();

        return ResponseDbCenter;
    }
    //修改沟通内容
    @ApiOperation(value = "修改沟通内容")
    @ResponseBody
    @RequestMapping(value = "/changeUserDynamic",method = {RequestMethod.GET,RequestMethod.POST})
    public ResponseDbCenter changeUserDynamic(HttpServletRequest req, HttpServletResponse rsp,
                                                   @ApiParam(value = "动态id",required = true)@RequestParam(required = true)   String  id,
                                                   @ApiParam(value = "沟通内容",required = true)@RequestParam(required = true)   String  contentValue,
                                                    String followingType,String contactee
                                              ) throws Exception {

        Map<String,String> userDynamic = new HashMap();
        userDynamic.put("id",id);
        userDynamic.put("contentValue",contentValue);
        userDynamic.put("followingType",followingType);
        userDynamic.put("contactee",contactee);
        userDynamic.put("updatedBy",req.getAttribute("loginUserId").toString());
        userDynamicService.changeUserDynamic(userDynamic);
        ResponseDbCenter ResponseDbCenter = new ResponseDbCenter();

        return ResponseDbCenter;
    }

}
