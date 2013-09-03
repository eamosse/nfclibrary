package com.first.nfc.apduql;

public class ApduError {

	private String message;
	private byte[] apduError;
	

	public ApduError() {
	}
	
	public ApduError(String message, byte[] apduError) {
		this.message = message;
		this.apduError = apduError;
	}

	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public byte[] getApduError() {
		return apduError;
	}
	public void setApduError(byte[] apduError) {
		this.apduError = apduError;
	}
	
	
	

}
