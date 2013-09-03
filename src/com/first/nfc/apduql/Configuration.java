package com.first.nfc.apduql;

import java.util.HashMap;
import java.util.Map;

public class Configuration {
	public static Map<String, Applet> applets;
	public static Applet currentApplet;
	static{
		applets = new HashMap<String, Applet>();
	}
	
	public static Applet getAppletByName(String appletName) {
		Applet applet = applets.get(appletName);
		if(applet!=null)
			currentApplet = applet;
		return applet;
	}
}
