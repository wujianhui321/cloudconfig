package com.xkd.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xkd.model.RedisTableKey;
import com.xkd.service.DictionaryService;
import com.xkd.service.OperateCacheService;
import com.xkd.utils.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xkd.model.Operate;
import com.xkd.model.ResponseConstants;
import com.xkd.model.ResponseDbCenter;
import com.xkd.service.SysOperateService;
import com.xkd.service.UserService;

public class LoginFilter extends HandlerInterceptorAdapter {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysOperateService sysOperateService;

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    RedisCacheUtil redisCacheUtil;

    @Autowired
    UserService userService;

    @Autowired
    OperateCacheService operateCacheService;


    private static Map<String, String> pvProperties = null;




    /**
     * 页面pv  URL与模块名映射关系
     */
    static {
        pvProperties = PropertiesUtil.loadProperties("pv.properties");
    }


    //初始化资源
    @PostConstruct
    public void init() {
        //加载数据字典到缓存
        initDictinary();
        //加载用户到缓存
        initUser();
    }





    /**
     * 将数据字典加载到Redis缓存中去
     */

    public void initDictinary() {
        redisCacheUtil.delete(RedisTableKey.DICTIONARY);
        List<Map<String,Object>> mapList=dictionaryService.selectAllDictionary();
        Map<String,Object> dataMap=new HashMap<>();
        for (int i = 0; i <mapList.size() ; i++) {
            Map<String,Object> m=mapList.get(i);
            dataMap.put((String) m.get("id"),m.get("value"));
        }
        redisCacheUtil.setCacheMap(RedisTableKey.DICTIONARY,dataMap);
    }


    /**
     * 初始化用户信息
     */
    public void initUser(){
        redisCacheUtil.delete(RedisTableKey.USER);
        List<Map<String,Object>> userList=userService.selectAllUsers();
        Map<String,Object> dataMap=new HashMap<>();
        for (int i = 0; i <userList.size() ; i++) {
            Map<String,Object> m=userList.get(i);
            dataMap.put((String) m.get("id"),m);
        }
        redisCacheUtil.setCacheMap(RedisTableKey.USER,dataMap);

    }












    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String requestIP = request.getRemoteHost();
        StringBuffer url = request.getRequestURL();
        String url_new = url.toString();
        request.getSession().setAttribute("url", url_new);
        String uri = request.getRequestURI().replace("/businesscrm/", "");
        String modelName = pvProperties.get(uri);
        if (StringUtils.isBlank(modelName)) {
            modelName = "---";
        }
        String pcuserId = null;

        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            token = request.getParameter("token");
        }

        String appFlag = request.getParameter("appFlag");    //小程序为"xcx"

        // token为555时这个时候就通过
        if ("555".equals(token)) {
            pcuserId = "555";
            redisCacheUtil.setCacheObject("555","818");

            request.getSession().setAttribute("555", "818");
            request.getSession().setAttribute("getOpenId555", "o2DOPwUMugpWcLs9swzBX6FgEwNk");
            request.setAttribute("loginUserId", "818");
        } else if (StringUtils.isNotBlank(token)) {

            pcuserId = (String) request.getSession().getAttribute(token);
            redisCacheUtil.setCacheObject(token,pcuserId);
            // 这里把当前登录用户的Id存进去方便在Controller层使用
            request.setAttribute("loginUserId", pcuserId);
        }

        /**
         * swagger 在线文档直接放过
         */
        if (url_new.contains("swagger") || url_new.contains("api-docs")) {
            return true;
        }

        if (
            (url_new.indexOf("/user/") > 0) ||
            (url_new.indexOf("/meeting/loginUserMeetingSign") >= 0) ||
            (url_new.indexOf("/meeting/userMeetingSign") >= 0) ||
            (url_new.indexOf("/spread/selectSpreadGetMoneyLogDetail") >= 0) ||
            (url_new.indexOf("/spread/selectSpreadUserById") >= 0) ||
            (url_new.indexOf("/spread/registerSpreadUser") >= 0) ||
            (url_new.indexOf("/spread/loginSpreadUser") >= 0) ||
            (url_new.indexOf("/spread/updateSpreadUser") >= 0) ||
            (url_new.indexOf("/spread/SpreadUserGetMoney") >= 0) ||
            (url_new.indexOf("/spread/selectSpreadUserDetail") >= 0) ||
            (url_new.indexOf("/spread/selectSpreadUserGetMoneyLogs") >= 0)

                ){
            //打印日志，便于数据分析：2017-09-25 15:02:06 [INFO ] type:kedouCRM userid: 555 trans:company method:selectAllDetailInfoByCompanyId date:2017-09-25 15:02:06
            String[] strs = url_new.split("/");
            log.info("modelName@" + modelName + "+requestIP:" + requestIP + " type:kedouCRM userid: " + pcuserId + " trans:" + strs[strs.length - 2] + " method:" + strs[strs.length - 1] + " date:" + DateUtils.currtime());

            //获取配置信息是否要写kafka日志
            String writeKafka = PropertiesUtil.KAFKA_WRITE;
            if ("true".equals(writeKafka)) {
                //发送日志到Kafka日志收集队列
                KafkaUtil.sendMessage("crmLog", "modelName@" + modelName + "+requestIP@" + requestIP + "+type@kedouCRM+userid@" + pcuserId + "+trans@" + strs[strs.length - 2] + "+method@" + strs[strs.length - 1] + "+date@" + DateUtils.currtime());
            }
            log.info("----------登录拦截-------------");

            return true;
        }


        if (pcuserId != null) {

            if (redisCacheUtil.getCacheObject(token) == null) {
                return false;
            }
            //刷新时间
            redisCacheUtil.setCacheObject(token,redisCacheUtil.getCacheObject(token),RedisTableKey.EXPIRE);

            //打印日志，便于数据分析：2017-09-25 15:02:06 [INFO ] type:kedouCRM userid: 555 trans:company method:selectAllDetailInfoByCompanyId date:2017-09-25 15:02:06
            String[] strs = url_new.split("/");
            log.info("2modelName@" + modelName + "+requestIP:" + requestIP + " type:kedouCRM userid: " + pcuserId + " trans:" + strs[strs.length - 2] + " method:" + strs[strs.length - 1] + " date:" + DateUtils.currtime());
            //获取配置信息是否要写kafka日志
            String writeKafka = PropertiesUtil.KAFKA_WRITE;
            if ("true".equals(writeKafka)) {
                //发送日志到Kafka日志收集队列
                KafkaUtil.sendMessage("crmLog", "modelName@" + modelName + "+requestIP@" + requestIP + "+type@kedouCRM+userid@" + pcuserId + "+trans@" + strs[strs.length - 2] + "+method@" + strs[strs.length - 1] + "+date@" + DateUtils.currtime());
            }
            //后台验证接口权限
//            return hasPermission(token, request, response);
            return true;
        }


        log.info("3type:errorRequest userid: " + pcuserId + " trans:" + url_new + " date:" + DateUtils.currtime());

        return false;
    }

    /**
     * 验证接口权限
     *
     * @param token
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    private boolean hasPermission(String token, HttpServletRequest request, HttpServletResponse response) throws IOException {

        String requestUrl = request.getRequestURL().toString();

        if ("555".equals(token)) {
            return true;
        }

        String userId = request.getSession().getAttribute(token).toString();

        // 定义权限列表
        List<Operate> userOperateList = new ArrayList<Operate>();
        //定义用户权限列表
        List<Operate> allOperateList = new ArrayList<>();
        /**
         * 如果userId不为空，则说明是明确的用户对象，在redis中存在，则去加载这个人的权限
         */
        if (StringUtils.isNotBlank(userId)) {
            // 获取用户操作列表
            userOperateList = operateCacheService.getUserOperates(token);

            //获取所有权限
            allOperateList = operateCacheService.getAllOperates();

            //如果没有加载所有权限，则从数据库中加载
            if (null == allOperateList|| allOperateList.size()==0) {
                //读取所有权限
                allOperateList = sysOperateService.selectAllOperate();

                operateCacheService.putAllOperate(allOperateList);
            }


            if (null == userOperateList || userOperateList.size() == 0) {// 第一次访问，还没有从库中查询权限信息

                //读取用户对应的权限信息
                List<Operate> allUserOperateList = userService.selectAllOperateByUSerId(userId);
                operateCacheService.putUserOperates(token, allUserOperateList);

            }

            String extra = request.getParameter("extra");//获取前台传递的数据相关的人员Id
            if (extra != null) {
                Map<String, Object> user = userService.selectUserById(userId);
                if ("1".equals(user.get("roleId"))) {//如果是超级管理员，则直接拥有所有数据权限
                    return true;
                }
                List<Map<String, Object>> listMap = JSON.parseObject(extra, new TypeReference<List<Map<String, Object>>>() {
                });
                for (int i = 0; i < listMap.size(); i++) {
                    String createdBy = (String) listMap.get(i).get("e");   //创建人
                    String companyAdviserId = (String) listMap.get(i).get("f"); //顾问
                    String companyDirectorId = (String) listMap.get(i).get("g"); //总监
                    String relativeUserIds = (String) listMap.get(i).get("h"); //相关人员，其权限和总监顾问差不多
                    String responsibleId2 = (String) listMap.get(i).get("i"); //二级负责人
                    String createdBy2 = (String) listMap.get(i).get("j"); //二级创建人

                    //如果登录用户是创建人员
                    if (createdBy != null && createdBy.equals(userId)) {
                        return true;
                    }
                    //如果登录用户是顾问
                    if (companyAdviserId != null && companyAdviserId.equals(userId)) {
                        return true;
                    }
                    //如果登录用户是总监
                    if (companyDirectorId != null && companyDirectorId.equals(userId)) {
                        return true;
                    }

                    //如果登录用户是相关人员
                    if (!StringUtils.isBlank(relativeUserIds)) {
                        String[] hIds = relativeUserIds.split(",");
                        for (int j = 0; j < hIds.length; j++) {
                            if (hIds[j].equals(userId)) {
                                return true;
                            }
                        }
                    }


                    //如果登录用户是2级对象创建人，如企业下面的商机的创建人
                    if (createdBy2 != null && createdBy2.equals(userId)) {
                        return true;
                    }
                    //如果登录用户是2级对象负责人，如企业下面的商机的负责人
                    if (responsibleId2 != null && responsibleId2.equals(userId)) {
                        return true;
                    }

                }

            }


        }
        /**
         * 如果是登录动作，直接允许登录
         */
        if (requestUrl.indexOf("/user/") >= 0) {
            return true;
        }


        /**
         * 判断是否需要控制权限
         */
        boolean shouldControl = false; // 是否需要控制权限
        for (int i = 0; i < allOperateList.size(); i++) {
            if ((requestUrl + "/").indexOf(allOperateList.get(i).getUrl() + "/") >= 0) {
                shouldControl = true;
                break;
            }
        }

        if (shouldControl) {                // 如果需要控制权限
            /**
             * 判断用户是否有权限
             */
            userOperateList = operateCacheService.getUserOperates(token);

            boolean hasPermission = false;
            for (int i = 0; i < userOperateList.size(); i++) {
                String ur = userOperateList.get(i).getUrl();
                if ((requestUrl + "/").indexOf(ur + "/") >= 0) {
                    hasPermission = true;
                    break;
                }
            }
            if (!hasPermission) {
                returnErrorMessage(response, requestUrl);
                return false;

            } else {
                return true;
            }

        } else {
            return true;
        }


    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }

    private void returnErrorMessage(HttpServletResponse response, String url) throws IOException {
        ResponseDbCenter rst = ResponseConstants.NOT_PERMITTED;
        rst.setResExtra(url);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(JSON.toJSON(rst));
        out.flush();
    }


}
