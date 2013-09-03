package com.first.nfc.apduql;

import java.io.UnsupportedEncodingException;

public class ResponseHelper {

	public static byte[] getResponseCode(byte[] resp) {

		return new byte[] { resp[resp.length - 2], resp[resp.length - 1] };
	}

	public static boolean isResponseSucceded(byte[] resp) {

		return ((resp[resp.length - 2] == (byte) 0x90) && (resp[resp.length - 1] == (byte) 0x00));

	}

	public static boolean isPinRequired(byte[] resp) {

		return ((resp[resp.length - 2] == (byte) 0x63) && (resp[resp.length - 1] == (byte) 0x01));

	}

	public static String getResponseBody(byte[] resp) {

		int size = 0;

		byte[] response = new byte[resp.length - 2];

		for (int i = 0; i < resp.length - 2; i++)

			if (resp[i] != 0x00) {
				response[size] = resp[i];
				size++;
			}

		try {

			return new String(response, 0, size, "UTF-8");

		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		}

		return null;

	}

}
