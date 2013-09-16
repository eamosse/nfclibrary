package com.first.nfc.apduql;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class Configuration {
	static final String FILENAME = "config.xml";
	public static File FILEDIRECTORY = null;
	
	public static List<Applet> getApplets(){
		try {
			Config config = load();
			if(config!=null) {
				if(config.getApplets()!=null)
					return config.getApplets();
			}
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return new ArrayList<Applet>();
	}

	static Config load() throws Exception {
		Serializer serializer = new Persister();
		File result = getFile();
		Config configs = serializer.read(Config.class, result);
	return configs;
	}

	private static File getFile() throws IOException {
		File result;
		if (FILEDIRECTORY != null)
			result = new File(FILEDIRECTORY, FILENAME);
		else
			result = new File(FILENAME);
		return result;
	}

	public static Applet getAppletByName(String appletName)
			throws BadRequestException {
		for (Applet applet : getApplets()){
			System.out.println("Applet " + applet + " Other " + appletName); 
			if (applet.getName().equalsIgnoreCase(appletName)) {
				return applet;
			}
		}
		throw new BadRequestException("Bad Request! This applet doesn't exist!");
	}

	public static boolean remove(Applet applet) throws Exception {
		System.out.println(applet + " should be removed after this");
		Config config = load();
		int i = 0;
		List<Applet> applets = getApplets(); 
		for (Applet a : applets) {
			if (a.equals(applet)) {
				applets.remove(i);
				System.out.println(applet + " is removed");
			}
			i++;
		}
		config.setApplets(applets);
		return persist(config);
	}

	static boolean persist(Config config) throws Exception {
		Serializer serializer = new Persister();
		File f = getFile();
		serializer.write(config, f);
		System.out.println("Saved......");
		return true;
	}

	public static boolean save(Applet applet) throws Exception {
		System.out.println("Saving......");
		Config config;
		try{
		 config = load();
		}catch(Exception e){
			config= new Config();
		}
		List<Applet> applets = config.getApplets();
		if(applets==null) applets = new ArrayList<Applet>();
		applets.add(applet);
		System.out.println("Saving Persist......");
		return persist(config);
	}

}
