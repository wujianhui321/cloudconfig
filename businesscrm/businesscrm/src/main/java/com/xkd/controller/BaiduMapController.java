package com.xkd.controller;

import com.xkd.model.LngLatData;
import com.xkd.model.ResponseDbCenter;
import com.xkd.utils.BaiduAddressUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by dell on 2017/12/21.
 */
@Api(description = "百度地图相关接口")
@RequestMapping("/baiduMap")
@Controller
public class BaiduMapController extends BaseController {


    @ApiOperation(value = "根据详细地址获得经纬度")
    @ResponseBody
    @RequestMapping(value = "/addressToLongitudeAndAltitude",method = {RequestMethod.POST})
    public ResponseDbCenter addressToLongitudeAndAltitude(HttpServletRequest req, HttpServletResponse rsp,
                                           @ApiParam(value = "地址",required = true)@RequestParam(required = true) String address) throws Exception {


        LngLatData lngLatData=BaiduAddressUtil.parseAddressToLngLat(address);

        ResponseDbCenter responseDbCenter = new ResponseDbCenter();
        responseDbCenter.setResModel(lngLatData);
        return responseDbCenter;
    }
}
