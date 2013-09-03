package com.first.nfc.apduql;

import java.util.HashMap;
import java.util.Map;

public class Applet {
	private String nom; 
	private String AID; 
	private Map<Command, Byte> classes; 
	private Map<String, Byte> instructions; 

	public Applet() {
		classes = new HashMap<Command, Byte>(); 
		instructions = new HashMap<String, Byte>(); 
	}

	/**
	 * @return the nom
	 */
	public String getNom() {
		return nom;
	}

	/**
	 * @param nom the nom to set
	 */
	public void setNom(String nom) {
		this.nom = nom;
	}

	/**
	 * @return the aID
	 */
	public byte[] getAID() {
		return Utils.hexStringToByteArray(AID);
	}

	/**
	 * @return the classes
	 */
	public Map<Command, Byte> getClasses() {
		return classes;
	}



	/**
	 * @return the instructions
	 */
	public Map<String, Byte> getInstructions() {
		return instructions;
	}


	/**
	 * @param aID the aID to set
	 */
	public void setAID(String aID) {
		AID = aID;
	}


	public void addInstruction(String name, String value) throws Exception{
		byte[] bytes = Utils.hexStringToByteArray(value);
		if(bytes.length>1)
			throw new Exception("Wrong data format");
		this.instructions.put(name, bytes[0]);
	}
	
	public void addClass(Command clazz, String value) throws Exception{
		byte[] bytes = Utils.hexStringToByteArray(value);
		if(bytes.length>1)
			throw new Exception("Wrong data format");
		this.classes.put(clazz, bytes[0]);
	}
	

}
