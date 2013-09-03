package com.first.nfc.apduql;

public enum  Command {
	SELECT ("select"), INSERT("insert");
	 String name; 
	Command(String name){
		this.name = name;
	}
}
