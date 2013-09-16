package com.first.nfc.apduql;

//@XmlRootElement
public class FieldModel {

    public FieldModel(String name, String value, int length) {
        this.length = length;
        this.name = name;
        this.value =     value;
    }
    
    public FieldModel(){
    	
    }
    private String name = "";
    private String value = "";
    private int length;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
   
}
