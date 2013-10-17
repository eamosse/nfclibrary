package com.first.nfc.apduql;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.Session;

import com.first.nfc.apduql.exceptions.ApduError;
import com.first.nfc.apduql.exceptions.BadRequestException;
import com.first.nfc.apduql.exceptions.NoReaderException;
import com.first.nfc.apduql.exceptions.NoSecureElementException;

import android.content.Context;


public class NfcController implements SEService.CallBack {

	protected SEService seService;

	private Context context;

	ApduCallBack callback;

	Channel channel;
	Applet currentApplet;

	public NfcController(Context context, ApduCallBack callback) {
		this.callback = callback;
		this.context = context;
		Configuration.FILEDIRECTORY = context.getFilesDir();
		Configuration.context = context;
	}

	/**
	 * 
	 * function to check if secure element and applet are present
	 */

	public void sayHello(String appletName, int commandHelloId) {
		Map<String, Object> response = new HashMap<String, Object>();

		try {
			Applet applet = Configuration.getAppletByName(appletName);

			openChannel(applet);

			byte[] hello = channel.getSelectResponse();

			if (ResponseHelper.isResponseSucceded(hello)) {
				response.put("hello", "Applet is succesfully selected!");
				callback.onResponse(response, commandHelloId);
			} else {
				callback.onNoApplet();
			}
		} catch (NoSuchElementException e) {

			callback.onNoApplet();

		} catch (IOException e) {

			callback.onNoSecureElement();

		} catch (NoReaderException e) {

			callback.onNoReader();

		} catch (NoSecureElementException e) {

			callback.onNoSecureElement();

		} catch (BadRequestException e) {
			// TODO Auto-generated catch block
			callback.onBadRequest(e.getMessage());
		}

	}

	public NfcController(Context context) {
		this.context = context;
		Configuration.context = context;

	}

	public void initService() {

		if (seService == null || !isServiceConnected())

			seService = new SEService(context, this);

	}

	public void destroy() {

		if (channel != null)

			channel.getSession().close();

		if (seService != null && seService.isConnected()) {

			seService.shutdown();

		}

	}

	@Override
	public void serviceConnected(SEService service) {

		if (service.isConnected())

			callback.onConnected();

		else

			callback.onNotConnected();

	}

	public ApduCallBack getCallback() {

		return callback;

	}

	public void setCallback(ApduCallBack callback) {

		this.callback = callback;

	}

	public void execute(final String command, final int commandId) {
		//new Thread(){
			//public void run(){
		Map<String, Object> maps = null;
		try {

			if (command.startsWith("select")) {
				maps = RequestParser.parseSelect(command);
			} else if (command.startsWith("insert")) {
				maps = RequestParser.parseInsert(command);
			}
			if (maps == null)
				throw new BadRequestException("Command not supported!");
			executeCommand(maps, commandId);

		} catch (BadRequestException ex) {
			ex.printStackTrace();
			callback.onBadRequest(ex.getMessage());
		} catch (NoSuchElementException ex) {
			ex.printStackTrace();
			callback.onNoApplet();
		} catch (NoSecureElementException ex) {
			ex.printStackTrace();
			callback.onNoSecureElement();
		} catch (NoReaderException ex) {
			ex.printStackTrace();
			callback.onNoReader();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			callback.onBadRequest(e.getMessage());
		}
			//}
		//}.start();
	}

	private void executeCommand(Map<String, Object> maps, int commandId)
			throws BadRequestException, NoSecureElementException,
			NoSuchElementException, NoReaderException, IOException {
		// byte[][] commands = RequestParser.parseSelect(commande);
		Map<String, Object> response = new HashMap<String, Object>();
		Applet applet = null;
		Object a = maps.get("applet");

		if (a instanceof Applet) {
			System.out.println("00000000000000000 " + a + " Test "
					+ (a instanceof Applet));
			applet = (Applet) a;
		}
		if (applet == null) {
			callback.onNoApplet();
			System.out.println("Ahhhhhhhhhh " + a + " Test "
					+ (a instanceof Applet));
			return;
		}
		boolean isErrorOccurs = true;
		// channel = serviceConnected(applet);
		// get data in the select command
		channel = openChannel(applet);
		byte[][] commands = (byte[][]) maps.get("command");
		String[] fields = (String[]) maps.get("fields");
		for (int i = 0; i < commands.length; i++) {
			byte[] command = commands[i];
			byte[] resp = sendCommand(command);
			System.out.println("Response");
			for(byte b : resp){
				System.out.println(b);
			}
			if (!ResponseHelper.isResponseSucceded(resp)) {
				if (ResponseHelper.isPinRequired(resp)) {
					callback.onPINRequired();
				} else {
					ApduError error = new ApduError("Wrong APDU response",
							ResponseHelper.getResponseCode(resp));
					callback.onAPDUError(error);
				}
			} else {
				String r = ResponseHelper.getResponseBody(resp);
				response.put(fields[i], r);
				isErrorOccurs = false;
			}

			if (isErrorOccurs) {
				break;
			}
		}
		if (!isErrorOccurs) {
			System.out.println("Response sending...........");
			callback.onResponse(response, commandId);
		}
	}

	public byte[] sendCommand(byte[] command) throws IOException {
		return channel.transmit(command);
	}

	public boolean isServiceConnected() {

		return (seService != null) && seService.isConnected();

	}

	private Reader getPrincipalReader() throws NoReaderException,

	NoSecureElementException {

		Reader reader = null;

		Reader[] readers = seService.getReaders();

		if (readers.length == 0)

			throw new NoReaderException();

		for (Reader r : readers)

			System.out.println(r.getName());

		reader = readers[0];

		if (!reader.isSecureElementPresent())

			throw new NoSecureElementException();

		return reader;

	}

	@SuppressWarnings("unused")
	private Session openSession() throws NoReaderException, IOException,

	NoSecureElementException {

		Session session = getPrincipalReader().openSession();

		return session;

	}

	private Channel openChannel(Applet applet) throws IOException,
			NoSuchElementException, NoReaderException,
			NoSecureElementException, BadRequestException {

		// Check if a channel is opened
		if (channel != null && !channel.isClosed()) {
			// if a channel is already opened for this applet

			if (currentApplet != null
					&& (applet.getName().equalsIgnoreCase(currentApplet
							.getName())))
				return channel;
			else
				// we must close the opened channel and open a new one for the
				// new applet
				channel.close();
		}

		Reader reader = getPrincipalReader();

		if (reader != null) {
			Session session = getPrincipalReader().openSession();
			byte[] aid;
			try {
				aid = Utils.hexStringToByteArray(applet.getAID());
				for (byte b : aid)
					System.out.println(b);
				channel = session.openLogicalChannel(aid);
				// If the channel is opened we change the current selected
				// applet
				if (ResponseHelper.isResponseSucceded(channel
						.getSelectResponse()))
					currentApplet = applet;
				System.out.println("Return channel");
				return channel;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw new BadRequestException(e.getMessage());

			}

		}
		throw new NoSuchElementException("No secure element present");

	}

}
