package com.first.nfc.apduql;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class Config {
	@ElementList(name="applets")
	List<Applet> applets = new ArrayList<Applet>();

	/**
	 * @return the applets
	 */
	public List<Applet> getApplets() {
		return applets;
	}

	/**
	 * @param applets the applets to set
	 */
	public void setApplets(List<Applet> applets) {
		this.applets = applets;
	} 
	
	public void add(Applet applet){
		this.applets.add(applet);
	}
}
