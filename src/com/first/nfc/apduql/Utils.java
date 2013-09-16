package com.first.nfc.apduql;

import java.math.BigInteger;

public class Utils {
	public static byte[] hexStringToByteArray(String s) throws Exception {
		System.out.println("Data to convert " + s);
		if(s.length()==1)
			s= "0"+s;
		byte[] bts = new BigInteger(s, 16).toByteArray();
		if(s.length()%2!=0 || s.length()==0)
			throw new Exception("Wrong format! " + s + " is not a valid hexadecimal string");
		int lenght = s.length()/2; 
		byte[] data = new byte[lenght];
		if(lenght<bts.length)
		System.arraycopy(bts, 1, data, 0, data.length);
		else
		System.arraycopy(bts, 0, data, 0, data.length);
		return data;
	}
}
