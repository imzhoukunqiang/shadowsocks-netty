package org.netty.core;

import java.io.*;
import java.util.Properties;

/**
 * Project <shadowsocks-netty>
 * Created by zkq on 2018/6/20 10:25.
 */
public class PropertiesConfig {

    private File file;
    private Properties properties;

    public PropertiesConfig(String path) throws IOException {


        File f = new File(path);
        if (!f.exists()) {
            throw new IllegalArgumentException("invalid file:" + path);
        }
        this.file = f;
        properties = new Properties();
        properties.load(new FileInputStream(file));
    }

    public String get(String key) {
        if (properties != null) {
            return properties.getProperty(key);
        } else {
            return null;
        }
    }

    public synchronized boolean refresh() {
        try {
            properties.load(new FileInputStream(file));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean update(String key, String value) {

        try {
            OutputStream fos = new FileOutputStream(file.getPath());
            properties.setProperty(key, value);
            properties.store(fos, "Update value");
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
