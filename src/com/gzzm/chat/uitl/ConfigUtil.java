package com.gzzm.chat.uitl;

import java.io.FileInputStream;
import java.util.Properties;

public class ConfigUtil {
	private static ConfigUtil configUtil = null;

	private volatile Properties properties = null;

	private ConfigUtil() {
		try {
			properties = new Properties();
			properties.load(new FileInputStream("res/config.properties"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ConfigUtil getInstance() {
		if (configUtil == null) {
			synchronized (ConfigUtil.class) {
				if (configUtil == null) {
					configUtil = new ConfigUtil();
				}
			}
		}
		return configUtil;
	}

	public String getValue(String key) {
		return properties.getProperty(key);
	}

}
