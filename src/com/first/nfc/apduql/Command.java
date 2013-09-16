package com.first.nfc.apduql;

public enum  Command {
	SELECT ("select"), INSERT("insert"),DELETE ("delete"), UPDATE("update"),DESCRIBE ("describe");
	 String name; 
	Command(String name){
		this.name = name;
	}
	
	public String value(){
		return name;
	}
}
