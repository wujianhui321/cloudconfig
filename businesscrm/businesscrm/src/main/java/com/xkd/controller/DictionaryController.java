package com.xkd.controller;

import com.alibaba.fastjson.JSON;
import com.xkd.model.Dictionary;
import com.xkd.model.RedisTableKey;
import com.xkd.model.ResponseConstants;
import com.xkd.model.ResponseDbCenter;
import com.xkd.service.DictionaryService;
import com.xkd.service.SolrService;
import com.xkd.service.UserService;
import com.xkd.utils.RedisCacheUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RequestMapping("/dictionary")
@Controller
@Transactional
@Api(description = "数据字典相关接口")
public class DictionaryController extends BaseController {

    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    SolrService solrService;

    @Autowired
    UserService userService;
    @Autowired
    RedisCacheUtil redisCacheUtil;

    /**
     * 根据类型查询数据
     *
     * @param req
     * @param rsp
     * @return
     */
    @ApiOperation(value = "查询数据字典所有类型")
    @ResponseBody
    @RequestMapping(value = "/selectDictionaryTtypes", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseDbCenter selectDictionaryTtypes(HttpServletRequest req, HttpServletResponse rsp
    ) {

        List<Map<String, Object>> maps = null;

        // 当前登录用户的Id
        String loginUserId = (String) req.getAttribute("loginUserId");
        Map<String, Object> loginUserMap = userService.selectUserById(loginUserId);

//		List<Map<String,Object>>  returnMaps = new ArrayList<>();

        try {
            if ("1".equals(loginUserMap.get("roleId"))) { //如果是超级管理员，则返回公共数据字典
                maps = dictionaryService.selectDictionaryTtypes(null);
            } else {  //否则返回该登录用户所在公司的数据字典
                maps = dictionaryService.selectDictionaryTtypes((String) loginUserMap.get("pcCompanyId"));
            }

        } catch (Exception e) {

            log.error("异常栈:",e);
            return ResponseConstants.FUNC_SERVERERROR;
        }

        ResponseDbCenter responseDbCenter = new ResponseDbCenter();

        responseDbCenter.setResModel(maps);

        return responseDbCenter;
    }

    /**
     * 查询数据字典根据ttypes
     *
     * @param req
     * @param rsp
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/selectDictionarysByTtypes", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "查询数据字典所有类型")
    public ResponseDbCenter selectDictionarysByTtypes(HttpServletRequest req, HttpServletResponse rsp,
                                                      @ApiParam(value = "数据字典类型,多个类型用逗号分隔", required = true) @RequestParam(required = true) String ttypes,
                                                      @ApiParam(value = "当前页", required = true) @RequestParam(required = true) Integer currentPage,
                                                      @ApiParam(value = "每页多少条", required = true) @RequestParam(required = true) Integer pageSize) {


        // 当前登录用户的Id
        String loginUserId = (String) req.getAttribute("loginUserId");
        Map<String, Object> loginUserMap = userService.selectUserById(loginUserId);


        if (StringUtils.isBlank(ttypes)) {

            return ResponseConstants.MISSING_PARAMTER;
        }

        String[] types = ttypes.split(",");

        String ttypesStr = "";

        for (int i = 0; i < types.length; i++) {

            ttypesStr += "'" + types[i] + "', ";
        }

        if (StringUtils.isNotBlank(ttypesStr)) {

            ttypesStr = "ttype in(" + ttypesStr.substring(0, ttypesStr.lastIndexOf(",")) + ")";
        }

        int start = 0;

        if (pageSize == null) {
            pageSize = 10;
        }
        if (currentPage == null) {
            currentPage = 1;
        }
        start = (currentPage - 1) * pageSize;


        List<Map<String, Object>> maps = null;
        Map<String, Object> returnMap = new HashMap<>();

        try {

            maps = dictionaryService.selectDictionarysByTtypes(ttypesStr, pageSize, start, (String) loginUserMap.get("pcCompanyId"));


            for (int i = 0; i < types.length; i++) {

                List<Map<String, Object>> typesList = new ArrayList<>();

                for (Map<String, Object> map : maps) {

                    if (types[i].equals(map.get("ttype") == null ? null : (String) map.get("ttype"))) {

                        typesList.add(map);
                    }

                }

                returnMap.put(types[i], typesList);

            }


        } catch (Exception e) {

            System.out.println(e);
            return ResponseConstants.FUNC_SERVERERROR;
        }

        ResponseDbCenter responseDbCenter = new ResponseDbCenter();

        responseDbCenter.setResModel(returnMap);

        return responseDbCenter;
    }


    /**
     * 保存数据字典信息
     *
     * @param req
     * @param rsp
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/saveDictionarys", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "保存数据字典")
    public ResponseDbCenter saveDictionarys(HttpServletRequest req, HttpServletResponse rsp,
                                            @ApiParam(value = "数据字典类型", required = true) @RequestParam(required = true) String ttype,
                                            @ApiParam(value = "数据字典类型名称", required = true) @RequestParam(required = true) String ttypeName,
                                            @ApiParam(value = "数据字典值，多个用逗号分隔", required = true) @RequestParam(required = true) String values) {
        // 当前登录用户的Id
        String loginUserId = (String) req.getAttribute("loginUserId");
        Map<String, Object> loginUserMap = userService.selectUserById(loginUserId);

        if (StringUtils.isBlank(ttype) || StringUtils.isBlank(values) || StringUtils.isBlank(ttypeName)) {

            return ResponseConstants.MISSING_PARAMTER;
        }

        String[] vs = values.split(",");


        try {

            for (int i = 0; i < vs.length; i++) {

                List<Dictionary> checkDictionarys = dictionaryService.selectDictionaryByTtypeValue(ttype, vs[i], (String) loginUserMap.get("pcCompanyId"));

                if (checkDictionarys != null && checkDictionarys.size() > 0) {

                    return ResponseConstants.DICTIONARY_HAS_EXISTS;
                }

                String id = UUID.randomUUID().toString();

                Integer maxLevel = dictionaryService.selectMaxLevelByTtype(ttype, (String) loginUserMap.get("pcCompanyId"));

                String module = dictionaryService.selectModuleByTtype(ttype);

                if (maxLevel == null) {
                    if ("1".equals(loginUserMap.get("roleId"))) { //如果是超级管理员，则添加的一定是公共数据字典
                        dictionaryService.saveDictionarys(id, ttype, ttypeName, vs[i], 1, null, module);
                    } else {
                        dictionaryService.saveDictionarys(id, ttype, ttypeName, vs[i], 1, (String) loginUserMap.get("pcCompanyId"), module);
                    }
                } else {
                    if ("1".equals(loginUserMap.get("roleId"))) {//如果是超级管理员，则添加的一定是公共数据字典

                        dictionaryService.saveDictionarys(id, ttype, ttypeName, vs[i], maxLevel.intValue() + 1, null, module);
                    } else {
                        dictionaryService.saveDictionarys(id, ttype, ttypeName, vs[i], maxLevel.intValue() + 1, (String) loginUserMap.get("pcCompanyId"), module);

                    }

                    /**
                     *维护到缓存中去
                     */
                    Map<String,Object> m=new HashMap<>();
                    m.put(id,vs[i]);
                    redisCacheUtil.setCacheMap(RedisTableKey.DICTIONARY,m);
                }


            }

        } catch (Exception e) {

            log.error("异常栈:",e);
            return ResponseConstants.FUNC_SERVERERROR;
        }

        ResponseDbCenter responseDbCenter = new ResponseDbCenter();

        return responseDbCenter;
    }

    /**
     * 编辑数据字典顺序
     *
     * @param req
     * @param rsp
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/updateDictionaryLevels", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "更新数据字典排序")
    public ResponseDbCenter updateDictionaryLevels(HttpServletRequest req, HttpServletResponse rsp,
                                                   @ApiParam(value = "更新数据字典排序  格式如： dictionarys: [{\"ttypeName\":\"优先级\",\"ttype\":\"priority\",\"id\":\"c333770c-002e-11e8-b59e-00163e0afd35\",\"useCount\":0,\"value\":\"高\",\"parentId\":\"0\",\"level\":1},{\"ttypeName\":\"优先级\",\"ttype\":\"priority\",\"id\":\"c33cd6a5-002e-11e8-b59e-00163e0afd35\",\"useCount\":0,\"value\":\"低\",\"parentId\":\"0\",\"level\":2},{\"ttypeName\":\"优先级\",\"ttype\":\"priority\",\"id\":\"c337ce5b-002e-11e8-b59e-00163e0afd35\",\"useCount\":0,\"value\":\"中\",\"parentId\":\"0\",\"level\":3}]", required = true) @RequestParam(required = true) String dictionarys) {


        if (StringUtils.isBlank(dictionarys)) {

            return ResponseConstants.MISSING_PARAMTER;
        }

        try {

            List<Map<String, Object>> results = (List<Map<String, Object>>) JSON.parseObject(dictionarys, Object.class);

            for (Map<String, Object> map : results) {

                if ((map.get("id") == null) || (map.get("level") == null)) {

                    continue;
                }

                String id = (String) map.get("id");
                Integer level = (Integer) map.get("level");

                dictionaryService.updateDictionaryLevel(id, level);
            }

        } catch (Exception e) {

            log.error("异常栈:",e);
            return ResponseConstants.FUNC_SERVERERROR;
        }

        ResponseDbCenter responseDbCenter = new ResponseDbCenter();

        return responseDbCenter;
    }


    @ResponseBody
    @RequestMapping(value = "/selectDictionaryById", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "根据Id查询数据字典")
    public ResponseDbCenter selectDictionaryById(HttpServletRequest req, HttpServletResponse rsp,
                                                 @ApiParam(value = "id", required = true) @RequestParam(required = true) String id) {


        if (StringUtils.isBlank(id)) {

            return ResponseConstants.MISSING_PARAMTER;
        }

        Dictionary dictionarie = null;

        try {

            dictionarie = dictionaryService.selectDictionaryById(id);

        } catch (Exception e) {

            System.out.println(e);
            return ResponseConstants.FUNC_SERVERERROR;
        }

        ResponseDbCenter responseDbCenter = new ResponseDbCenter();
        responseDbCenter.setResModel(dictionarie);

        return responseDbCenter;
    }


    @ResponseBody
    @RequestMapping(value = "/deleteDictionarysByIds", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "根据Id删除数据字典")
    public ResponseDbCenter deleteDictionarysByIds(HttpServletRequest req, HttpServletResponse rsp,
                                                   @ApiParam(value = "ids  多个id用逗号分隔", required = true) @RequestParam(required = true) String ids) {


        if (StringUtils.isBlank(ids)) {

            return ResponseConstants.MISSING_PARAMTER;
        }

        String[] iids = ids.split(",");

        try {

            for (int i = 0; i < iids.length; i++) {

                Dictionary dictionary = dictionaryService.selectDictionaryById(iids[i]);

                if (dictionary.getTtype() != null && StringUtils.isNotBlank(dictionary.getValue())) {

                    String deleteSql = "delete from dc_dictionary where id = '" + iids[i] + "'";

                    dictionaryService.clearColumnData(deleteSql);

                }

                dictionaryService.deleteDictionaryById(iids[i]);
                /**
                 * 维护到缓存中去
                 */
                redisCacheUtil.delete(RedisTableKey.DICTIONARY,iids[i]);
            }


        } catch (Exception e) {
            log.error("异常栈:",e);
            return ResponseConstants.FUNC_SERVERERROR;
        }

        ResponseDbCenter responseDbCenter = new ResponseDbCenter();

        return responseDbCenter;
    }


    @ResponseBody
    @RequestMapping(value = "/selectAllDictionary", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "查询所有数据字典")
    public ResponseDbCenter selectAllDictionary(HttpServletRequest req, HttpServletResponse rsp) {

        ResponseDbCenter responseDbCenter = new ResponseDbCenter();
        // 当前登录用户的Id
        String loginUserId = (String) req.getAttribute("loginUserId");
        Map<String, Object> loginUserMap = userService.selectUserById(loginUserId);
        try {
            Map<String, List<Map<String, Object>>> map = new HashMap<>();
            List<Map<String, Object>> mapList=null;
            if ("1".equals(loginUserMap.get("roleId"))){
                mapList = dictionaryService.selectAllDictionaryExcludeIndustry(null);
            }else {
                mapList=dictionaryService.selectAllDictionaryExcludeIndustry((String) loginUserMap.get("pcCompanyId"));
            }
            for (int i = 0; i < mapList.size(); i++) {
                Map<String, Object> dicMap = mapList.get(i);
                String module = (String) dicMap.get("module");
                List<Map<String, Object>> list = map.get(module);
                if (list == null) {
                    list = new ArrayList<>();
                    map.put(module, list);
                }
                if (!list.contains(dicMap)) {
                    list.add(dicMap);
                }
            }

            Set<String> keySet = map.keySet();
            Iterator<String> iterator = keySet.iterator();
            List<Map<String, Object>> moduleList = new ArrayList<>();
            while (iterator.hasNext()) {
                String moduleKey = iterator.next();
                Map<String, Object> moduleMap = new HashMap<>();
                moduleMap.put("module", moduleKey);
                Map<String, List<Map<String, Object>>> ttypeMap = new HashMap<>();
                List<Map<String, Object>> dicList = map.get(moduleKey);
                List<Map<String, Object>> innerList = new ArrayList<>();
                moduleMap.put("list", innerList);
                for (int i = 0; i < dicList.size(); i++) {
                    Map<String, Object> dM = dicList.get(i);
                    String ttype = (String) dM.get("ttype");
                    List<Map<String, Object>> dList = ttypeMap.get(ttype);
                    if (dList == null) {
                        dList = new ArrayList<>();
                        ttypeMap.put(ttype, dList);
                    }

                    if (!dList.contains(dM)) {
                        dList.add(dM);
                    }
                }

                Set<String> ttypeKeySet = ttypeMap.keySet();
                Iterator<String> ttypeIterator = ttypeKeySet.iterator();
                while (ttypeIterator.hasNext()) {
                    String k = ttypeIterator.next();
                    Map<String, Object> innerMap = new HashMap<>();
                    innerMap.put("ttype", k);
                    List<Map<String, Object>> ll = ttypeMap.get(k);
                    innerMap.put("list", ll);
                    if (ll != null && ll.size() > 0) {
                        innerMap.put("ttypeName", ll.get(0).get("ttypeName"));
                    }
                    innerList.add(innerMap);
                }


                moduleList.add(moduleMap);


            }

            responseDbCenter.setResModel(moduleList);
        } catch (Exception e) {

            System.out.println(e);
            return ResponseConstants.FUNC_SERVERERROR;
        }

        return responseDbCenter;
    }

}
