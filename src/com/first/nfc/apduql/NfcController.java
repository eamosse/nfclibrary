package com.first.nfc.apduql;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.Session;

import android.content.Context;


public class NfcController implements SEService.CallBack {

	protected SEService seService;

	private Context context;

	ApduCallBack callback;

	Channel channel;

	public NfcController(Context context, ApduCallBack callback) {

		this.callback = callback;

		this.context = context;

	}

	/**
	 * 
	 * function to check if secure element and applet are present
	 */

	public void sayHello(String appletName, int commandHelloId) {

		String[] response = { "Hello" };

		try {
			Applet applet = Configuration.getAppletByName(appletName);

			openChannel(applet);

			byte[] hello = channel.getSelectResponse();

			if (isResponseSucceded(hello))

				callback.onResponse(response, commandHelloId);

		} catch (NoSuchElementException e) {

			callback.onNoApplet();

		} catch (IOException e) {

			callback.onNoSecureElement();

		} catch (NoReaderException e) {

			callback.onNoReader();

		} catch (NoSecureElementException e) {

			callback.onNoSecureElement();

		}

	}

	public NfcController(Context context) {

		this.context = context;

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

	public void execute(String command, int commandId) {
		// Let us be sure that we don't have unnecessary characters in the
		// request (like blank space)
		command.replaceAll("\\s+", " ");
		System.out.println(command);
		try {

			if (command.startsWith("select")) {

				performSelect(command, commandId);

			} else if (command.startsWith("insert")) {

				performInsert(command, commandId);

			}

		} catch (IOException e) {

			callback.onIOException(e.getMessage());

		} catch (NoReaderException e) {

			callback.onNoReader();

		} catch (NoSecureElementException e) {

			callback.onNoSecureElement();

		} catch (Exception e) {
			callback.onBadRequest(e.getMessage());
			System.out.println(e.getMessage());

		}

	}

	private void performInsert(String commande, int commandId)
			throws NoSuchElementException, IOException, NoReaderException,
			NoSecureElementException, BadRequestException {

		byte[][] commands = RequestParser.parseInsert(commande);

		Applet applet = Configuration.currentApplet;
		for(byte b : applet.getAID())
			System.out.println(b);
		// open connection to the applet
		channel = openChannel(applet);

		String[] response = new String[commands.length];
		boolean isErrorOccurs = true;
		for (int i = 0; i < commands.length; i++) {

			byte[] command = commands[i];

			try {

				byte[] resp = channel.transmit(command);

				System.out.println(resp[resp.length - 2]);

				System.out.println(resp[resp.length - 1]);

				if (!isResponseSucceded(resp)) {

					if (isPinRequired(resp)) {

						callback.onPINRequired();

					} else {

						ApduError error = new ApduError("Wrong APDU response",

						getResponseCode(resp));

						callback.onAPDUError(error);

					}

				} else {

					// String r = getResponseBody(resp);

					response[i] = " OK ";

					isErrorOccurs = false;

				}

			} catch (IOException e) {

				callback.onIOException(e.getMessage());

			} catch (NoSuchElementException e) {

				callback.onNoApplet();

			} catch (Exception e) {

				System.out.println("Exception " + e.getMessage());

				e.printStackTrace();

			} finally {

				if (isErrorOccurs)

					break;

			}

		}

		if (!isErrorOccurs)
			callback.onResponse(response, commandId);

	}

		private void performSelect(String commande, int commandId) throws Exception {
	
			byte[][] commands = RequestParser.parseSelect(commande);
			String[] response = new String[commande.length()];
			Applet applet = Configuration.currentApplet;
	
			boolean isErrorOccurs = true;
			channel = openChannel(applet);
			// get data in the select command
	
			for (int i = 0; i < commands.length; i++) {
	
				byte[] command = commands[i];
	
				try {
	
					byte[] resp = channel.transmit(command);
	
					System.out.println(resp[resp.length - 2]);
	
					System.out.println(resp[resp.length - 1]);
	
					if (!isResponseSucceded(resp)) {
	
						if (isPinRequired(resp)) {
	
							callback.onPINRequired();
	
						} else {
	
							ApduError error = new ApduError(
	
							"Wrong APDU response",
	
							getResponseCode(resp));
	
							callback.onAPDUError(error);
	
						}
	
					} else {
						String r = getResponseBody(resp);
						response[i] = r;
						isErrorOccurs = false;
					}
	
				} catch (IOException e) {
	
					callback.onIOException(e.getMessage());
	
				} catch (NoSuchElementException e) {
	
					callback.onNoApplet();
	
				} finally {
	
					if (isErrorOccurs)
	
						break;
				}
			}
	
			if (!isErrorOccurs)
	
				callback.onResponse(response, commandId);
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
			NoSuchElementException, NoReaderException, NoSecureElementException {

		// Check if a channel is opened
		if (channel != null && !channel.isClosed()) {
			// if a channel is already opened for this applet
		
			if (Configuration.currentApplet !=null && (applet.getNom().equalsIgnoreCase(Configuration.currentApplet.getNom())))
				return channel;
			else
				// we must close the opened channel and open a new one for the
				// new applet
				channel.close();
		}

		Reader reader = getPrincipalReader();

		if (reader != null) {

			Session session = getPrincipalReader().openSession();

			for (byte b : applet.getAID())

				System.out.println(b);

			try {
				channel = session.openLogicalChannel(applet.getAID());
				// If the channel is opened we change the current selected
				// applet
				if (isResponseSucceded(channel.getSelectResponse()))
					Configuration.currentApplet = applet;
				return channel;
			} catch (SecurityException e) {
				callback.onNoSecureElement();
			}
		}
		throw new NoSuchElementException("No secure element present");

	}

	private byte[] getResponseCode(byte[] resp) {

		return new byte[] { resp[resp.length - 2], resp[resp.length - 1] };

	}

	private boolean isResponseSucceded(byte[] resp) {

		return ((resp[resp.length - 2] == (byte) 0x90) && (resp[resp.length - 1] == (byte) 0x00));

	}

	private boolean isPinRequired(byte[] resp) {

		return ((resp[resp.length - 2] == (byte) 0x63) && (resp[resp.length - 1] == (byte) 0x01));

	}

	private String getResponseBody(byte[] resp) {

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
