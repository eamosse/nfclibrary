package com.first.nfc.apduql;

import java.util.Map;

public interface ApduCallBack {
	public abstract  void onNotConnected();
	public abstract  void onConnected();
	public abstract  void onNoSecureElement();
	public abstract  void onNoReader();
	public abstract  void onNoApplet();
	public abstract  void onAPDUError(ApduError error);
	public abstract  void onIOException(String message);
	public abstract  void onResponse(Map<String,Object> results, int code);
	public abstract  void onPINRequired();
	public abstract  void onBadRequest(String message);

	}

