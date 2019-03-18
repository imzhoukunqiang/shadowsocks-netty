package org.netty.service;

import org.netty.core.Result;

import java.util.Map;

/**
 * Project <shadowsocks-netty>
 * Created by zkq on 2018/6/20 10:23.
 */
public interface ProxyService {
    void refreshProperties();

    String getProxyPromote();

    Result<String> pacScript();

    Result<Map<String, String>> trafficStatistics();

    String otherPacScript();
}
