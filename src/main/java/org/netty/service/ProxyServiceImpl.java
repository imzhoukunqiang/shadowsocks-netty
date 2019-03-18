package org.netty.service;

import io.netty.handler.traffic.TrafficCounter;
import org.netty.SocksServer;
import org.netty.config.PacLoader;
import org.netty.core.PropertiesConfig;
import org.netty.core.Result;
import org.netty.core.ResultGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Project <shadowsocks-netty>
 * Created by zkq on 2018/6/20 10:21.
 */
@Service
public class ProxyServiceImpl implements ProxyService {

    private static final String PROXY_PROMOTE_KEY = "proxy.promote";
    private static final String DEFAULT_URL = "https://www.2345.com/?k78966851";
    private static final double KB = 1024.0;
    private static final double MB = 1024 * KB;
    private static final double GB = 1024 * MB;
    private static final DecimalFormat DECIMA_LFORMAT = new DecimalFormat("#.##");


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

    @Override
    public Result<Map<String, String>> trafficStatistics() {
        TrafficCounter trafficCounter = SocksServer.getInstance().getTrafficCounter();
        String downloadSpeed = formatNumber(trafficCounter.lastWriteThroughput()) + "/S";
        String uploadSpeed = formatNumber(trafficCounter.lastReadThroughput()) + "/S";
        long download = trafficCounter.cumulativeWrittenBytes();
        String totalDownload;

        long upload = trafficCounter.cumulativeReadBytes();
        String totalUpload;
        totalDownload = formatNumber(download);
        totalUpload = formatNumber(upload);
        HashMap<String, String> map = new HashMap<>();
        map.put("downloadSpeed", downloadSpeed);
        map.put("uploadSpeed", uploadSpeed);
        map.put("totalDownload", totalDownload);
        map.put("totalUpload", totalUpload);
        return ResultGenerator.genSuccessResult(map);
    }

    @Override
    public String otherPacScript() {
        String[] proxies = config.get("proxy.other.pac.script").split(";");
        ArrayList<String> list = new ArrayList<>(Arrays.asList(proxies));
        Collections.shuffle(list);
        StringBuilder result = new StringBuilder("function FindProxyForURL(url, host) { ")
                .append(" return \"")
                .append(String.join(";", list))
                .append(";DIRECT\" }");
        return result.toString();
    }

    private String formatNumber(long number) {
        if (number < MB) {
            String format = DECIMA_LFORMAT.format(number / KB);
            return format + " KB";
        } else if (number < GB) {
            String format = DECIMA_LFORMAT.format(number / MB);
            return format + "MB";
        } else {
            String format = DECIMA_LFORMAT.format(number / GB);
            return format + "GB";
        }
    }

    private String getPacStr() {
        List<String> domainList = PacLoader.getDomainList();
        String str = "'" + String.join("' : 1 , '", domainList);
        str += "' : 1";
        return str;
    }


}
