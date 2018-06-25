package org.netty.controller;

import org.netty.core.Result;
import org.netty.core.ResultGenerator;
import org.netty.service.ProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Project <shadowsocks-netty>
 * Created by zkq on 2018/6/20 10:00.
 */
@RestController
@RequestMapping("/proxy")
public class ProxyController {

    @Autowired
    private ProxyService proxyService;

    @RequestMapping("/promoteList")
    public Result<String> promoteList() {
        return ResultGenerator.genSuccessResult(proxyService.getProxyPromote());
    }


    @RequestMapping("/refresh")
    public Result refresh() {
        proxyService.refreshProperties();
        return ResultGenerator.genSuccessResult();
    }

    @RequestMapping("/pacScript")
    public Result<String> pacScript() {
        return proxyService.pacScript();
    }


    @RequestMapping("/trafficStatistics")
    public Result<Map<String, String>> trafficStatistics() {
        return proxyService.trafficStatistics();
    }


}
