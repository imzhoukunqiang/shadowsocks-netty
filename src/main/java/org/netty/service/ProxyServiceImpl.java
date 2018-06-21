package org.netty.service;

import org.netty.config.PacLoader;
import org.netty.core.PropertiesConfig;
import org.netty.core.Result;
import org.netty.core.ResultGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Project <shadowsocks-netty>
 * Created by zkq on 2018/6/20 10:21.
 */
@Service
public class ProxyServiceImpl implements ProxyService {

    public static final String PROXY_PROMOTE_KEY = "proxy.promote";
    public static final String DEFAULT_URL = "https://www.2345.com/?k78966851";

    @Autowired
    private PropertiesConfig config;

    @Override
    public void refreshProperties() {
        config.refresh();
    }

    @Override
    public String getProxyPromote() {
        Optional<String> s = Optional.ofNullable(config.get(PROXY_PROMOTE_KEY));
        return s.orElse(DEFAULT_URL);
    }

    @Override
    public Result<String> pacScript() {
        String str = getPacStr();
        String prefix = config.get("proxy.srcipt.prefix");
        String suffix = config.get("proxy.srcipt.suffix");
        return ResultGenerator.genSuccessResult(prefix + str + suffix);
    }

    private String getPacStr() {
        List<String> domainList = PacLoader.getDomainList();
        String str = "'" + String.join("' : 1 , '", domainList);
        str += "' : 1";
        return str;
    }


}
