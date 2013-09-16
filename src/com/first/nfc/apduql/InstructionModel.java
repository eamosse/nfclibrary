package com.first.nfc.apduql;


//@XmlRootElement
public class InstructionModel {
	private Command clazz; 
	private String value=""; 
	
	public InstructionModel(){
		
	}

    public Command getClazz() {
        return clazz;
    }

    public String getValue() {
        return value;
    }
	
	public InstructionModel(Command command, String value){
		this.clazz = command; 
		this.value= value; 
	}

    public void setClazz(Command clazz) {
        this.clazz = clazz;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
