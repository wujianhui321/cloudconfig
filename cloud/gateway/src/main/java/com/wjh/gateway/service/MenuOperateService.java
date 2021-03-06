package com.wjh.gateway.service;

import com.wjh.common.model.ResponseModel;
import com.wjh.menuoperateservicemodel.model.OperateVo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(value = "menuoperateservice")
public interface MenuOperateService {
    @RequestMapping(value = "/operate/listAll", method = RequestMethod.GET)
    public ResponseModel<List<OperateVo>> listAll();
}
