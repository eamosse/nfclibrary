package com.first.nfc.apduql;


import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class Applet {

	@Attribute
	private String name;
	@Attribute
	private String AID;
	@ElementList
	private List<InstructionModel> classes;
	@ElementList
	private List<FieldModel> fields;

	public Applet() {
		classes = new ArrayList<InstructionModel>();
		fields = new ArrayList<FieldModel>();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public void setClasses(List<InstructionModel> classes) {
		this.classes = classes;
	}

	public void setFields(List<FieldModel> fields) {
		this.fields = fields;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String getAID() {
		return AID;
	}

	/**
	 * @param aID
	 *            the aID to set
	 */
	public void setAID(String aID) {
		AID = aID;
	}

	public void addFields(FieldModel model) {
		this.fields.add(model);
	}

	public void addClass(InstructionModel model) {

		this.classes.add(model);
	}

	public byte getClazz(Command clazz) throws BadRequestException {
		for (InstructionModel model : classes) {
			if (model.getClazz().equals(clazz)) {
				try {
					System.out.println("Instruction  " + model.getValue() + " Class " + model.getClazz());
					return Utils.hexStringToByteArray(model.getValue().replace("\\s", ""))[0];
				} catch (Exception e) {
					// TODO Auto-generated catch block
					throw new BadRequestException(e.getMessage());
				}
			}
		}
		throw new BadRequestException("Instruction of type " + clazz.value()
				+ " is not supported by this applet");
	}

	/**
	 * @return the classes
	 */
	public List<InstructionModel> getClasses() {
		return classes;
	}

	/**
	 * @return the fields
	 */
	public List<FieldModel> getFields() {
		return fields;
	}

	public byte getInstruction(String name) throws BadRequestException {
		for (FieldModel model : fields) {
			System.out.println("Instruction  " + model.getValue() + " Class " + model.getName());
			if (model.getName().equalsIgnoreCase(name)) {
				try {
					return Utils.hexStringToByteArray(model.getValue().replace("\\s", ""))[0];
				} catch (Exception e) {
					throw new BadRequestException(e.getMessage());
				}
			}
		}
		throw new BadRequestException("Field " + name
				+ " is not exist in this applet");
	}

	public int getFieldLength(String name) throws BadRequestException {
		for (FieldModel model : fields) {
			if (model.getName().equalsIgnoreCase(name)) {
				return model.getLength();
			}
		}
		throw new BadRequestException("Field " + name
				+ " is not exist in this applet");
	}

	public String toString() {
		return this.name;
	}
}
