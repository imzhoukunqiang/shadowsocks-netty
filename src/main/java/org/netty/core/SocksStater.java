package org.netty.core;

import org.netty.SocksServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Project <shadowsocks-netty>
 * Created by zkq on 2018/6/20 15:10.
 */
@Component
public class SocksStater implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        SocksServer.getInstance().start();
    }
}
