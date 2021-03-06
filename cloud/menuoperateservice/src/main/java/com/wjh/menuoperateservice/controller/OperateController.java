package com.wjh.menuoperateservice.controller;

import com.wjh.common.model.ResponseConstant;
import com.wjh.common.model.ResponseModel;
import com.wjh.common.model.ServiceIdConstant;
import com.wjh.menuoperateservice.service.OperateService;
import com.wjh.menuoperateservicemodel.model.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(description = "权限相关接口")
@RefreshScope
@RestController
@RequestMapping("/operate")
public class OperateController {

    Logger logger = LogManager.getLogger();

    @Autowired
    OperateService operateService;


    @ApiOperation(value = "搜索权限")
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ResponseModel<List<OperateVo>> search(@ApiParam(value = "权限名称", required = false) @RequestParam(required = false) String operateName,
                                @ApiParam(value = "当前页", required = true) @RequestParam(required = true) Integer currentPage,
                                @ApiParam(value = "每页多少条记录", required = true) @RequestParam(required = true) Integer pageSize) {


        logger.debug("request parameters: operateName=>{},currentPage=>{},pageSize=>{}", operateName, currentPage, pageSize);

        try {
            List<OperateVo> list = operateService.search(operateName, currentPage, pageSize);
            ResponseModel responseModel = new ResponseModel();
            responseModel.setResModel(list);
            return responseModel;
        } catch (Exception e) {
            logger.error(ServiceIdConstant.menuoperateservice, e);
            return ResponseConstant.SYSTEM_EXCEPTION;
        }

    }


    @ApiOperation(value = "列出所有权限")
    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    public ResponseModel<List<OperateVo>> listAll() {
        try {
            List<OperateVo> list = operateService.listAll();
            ResponseModel responseModel = new ResponseModel();
            responseModel.setResModel(list);
            return responseModel;
        } catch (Exception e) {
            logger.error(ServiceIdConstant.menuoperateservice, e);
            return ResponseConstant.SYSTEM_EXCEPTION;
        }

    }




    @ApiOperation(value = "搜索权限")
    @RequestMapping(value = "/selectByIds", method = RequestMethod.POST)
    public ResponseModel<List<OperateVo>> selectByIds(@ApiParam(value = "id列表", required = true) @RequestBody(required = true) List<Long> idList) {


        logger.debug("request parameters: idList=>{}", idList);

        try {
            List<OperateVo> list = operateService.selectByIds(idList);
            ResponseModel responseModel = new ResponseModel();
            responseModel.setResModel(list);
            return responseModel;
        } catch (Exception e) {
            logger.error(ServiceIdConstant.menuoperateservice, e);
            return ResponseConstant.SYSTEM_EXCEPTION;
        }

    }




    @ApiOperation(value = "添加权限")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ResponseModel add(@ApiParam(value = "权限") @RequestBody(required = true) OperateAddDto operateAddDto,
                             HttpServletRequest httpServletRequest) {


        logger.debug("request parameters: operateAddDto=>{}", operateAddDto);

        String loginUserId = httpServletRequest.getHeader("loginUserId");
        try {
            OperatePo operatePo = new OperatePo();
            BeanUtils.copyProperties(operatePo, operateAddDto);
            OperatePo operatePoReturn = operateService.insert(operatePo, Long.valueOf(loginUserId));
            ResponseModel responseModel = new ResponseModel();
            responseModel.setResModel(operatePoReturn);
            return responseModel;
        } catch (Exception e) {
            logger.error(ServiceIdConstant.menuoperateservice, e);
            return ResponseConstant.SYSTEM_EXCEPTION;
        }

    }


    @ApiOperation(value = "修改权限")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public ResponseModel update(@ApiParam(value = "权限") @RequestBody(required = true) OperateUpdateDto operateUpdateDto,
                                HttpServletRequest httpServletRequest) {


        logger.debug("request parameters: operateUpdateDto=>{}", operateUpdateDto);
        String loginUserId = httpServletRequest.getHeader("loginUserId");
        try {
            OperatePo operatePo = new OperatePo();
            BeanUtils.copyProperties(operatePo, operateUpdateDto);
            OperatePo operatePoReturn = operateService.update(operatePo, Long.valueOf(loginUserId));
            ResponseModel responseModel = new ResponseModel();
            responseModel.setResModel(operatePoReturn);
            return responseModel;
        } catch (Exception e) {
            logger.error(ServiceIdConstant.menuoperateservice, e);
            return ResponseConstant.SYSTEM_EXCEPTION;
        }

    }


    @ApiOperation(value = "删除权限")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public ResponseModel delete(@ApiParam(value = "权限Id", required = true) @RequestParam(required = true) Long id,
                                HttpServletRequest httpServletRequest) {


        logger.debug("request parameters: id=>{}", id);
        String loginUserId = httpServletRequest.getHeader("loginUserId");
        try {
            operateService.delete(id);
            ResponseModel responseModel = new ResponseModel();
            return responseModel;
        } catch (Exception e) {
            logger.error(ServiceIdConstant.menuoperateservice, e);
            return ResponseConstant.SYSTEM_EXCEPTION;
        }

    }


}
