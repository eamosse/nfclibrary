package fr.unice.mbds.nfc.library;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.Session;

import android.content.Context;

public  class NfcController  implements SEService.CallBack{
	protected SEService seService;
	private Context context; 
	ApduCallBack callback;
	private byte[] appletAID; 
	private byte CLA;
	Channel channel; 




	public byte getCLA() {
		return CLA;
	}

	public void setCLA(byte cLA) {
		CLA = cLA;
	}

	public NfcController(Context context, ApduCallBack callback){
		this.callback = callback; 
		this.context = context;
		//initService();
	}
	
	public NfcController(Context context){
		this.context = context;
		//initService();
	}


    public  void initService(){
    	System.out.println(this.appletAID);
    	for(byte b: appletAID)
    		System.out.println(b);
		seService = new SEService(context, this);
	}

	/*public void destroy(){
		if (seService != null && seService.isConnected()) {
			seService.shutdown();
			
		}
	}*/


	@Override
	public void serviceConnected(SEService service) {
		if(service.isConnected())
			try {
				//getPrincipalReader();
				channel= openChannel(); 
				callback.onConnected();
			} catch (NoReaderException e) {
				callback.onNoReader();
			} catch (NoSecureElementException e) {
				callback.onNoSecureElement();
			} catch (NoSuchElementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				callback.onNoSecureElement();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else
			callback.onNotConnected();


	}

	public ApduCallBack getCallback() {
		return callback;
	}

	public void setCallback(ApduCallBack callback) {
		this.callback = callback;
	}

	public void sendApdu(String command){
		command = command.trim().toLowerCase(Locale.ENGLISH);

		if(command.startsWith("select")){
			performSelect(command);
		}else if(command.startsWith("insert")){
			performInsert(command);
		}

	}

	private static byte[] convertToBytes(String[] strings) {
		byte[] data = new byte[strings.length];
		for (int i = 0; i < strings.length; i++) {
			String string = strings[i];
			data[i] = Byte.valueOf(string);
		}
		return data;
	}

	private byte[] stringToHex(String str) { 
		char[] chars = str.toCharArray();
		byte[] values = new byte[chars.length]; 
		StringBuffer strBuffer = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			strBuffer.append(Integer.toHexString((int) chars[i]));
			values[i] = Byte.valueOf(Integer.toHexString((int) chars[i])); 
		}
		return values;
	}

	private void performInsert(String commande){
		//insert into applet (ins1,ins2) values(value1,value2)
		String[] response;
		int index_inst_open = commande.indexOf("("); 
		int index_inst_close = commande.indexOf(")"); 
		if(!(index_inst_open!=-1 && index_inst_close!=-1)) return;
		System.out.println("Level 2");
		int index_val_open = commande.indexOf("(", index_inst_open+1);
		int index_val_close = commande.indexOf(")", index_inst_close+1);
		if (!(index_val_open!=-1 && index_val_close!=-1)) return; 
		//get instructions bytes 
		String inst = commande.substring(index_inst_open+1,index_inst_close).trim();
		System.out.println("Inst " + inst);
		//byte[] instructions; 
		String[] tabInstructions = inst.split(",");

		//get values to insert 
		String values = commande.substring(index_val_open+1,index_val_close);
		System.out.println("Values " + values);
		String[] tabvalues = values.split(",");
		if(tabvalues.length==0)
		{
			return;
		}	
		//now for each instruction and it's value we send an apdu command to save the value in the applet 
		response = new String[tabInstructions.length];
		int iter=0;
		byte[] vl= new byte[]{(byte)0x50,(byte)0x50};
		boolean isErrorOccurs=true;
		for(String instruction : tabInstructions){
			//[0, 80, 0, 0, 5, 97, 103, 101, 110, 116, 0]
			//new byte[]{0x00,vl[1],0x00,0x00,0x04,0x31,0x32,0x33,0x34};//; 
			byte[] command =setCommand(CLA, vl[iter], "1234");
			try {
				byte[] resp = channel.transmit(command);
				System.out.println(resp[resp.length-2]);
				System.out.println(resp[resp.length-1]);
				if(!isResponseSucceded(resp)){
					if(isPinRequired(resp)) {
						callback.onPINRequired();
					}else{
						ApduError error = new ApduError("Wrong APDU response", getResponseCode(resp));
						callback.onAPDUError(error);
					}
				}else{
					//String r  = getResponseBody(resp);
					response[iter] = tabvalues[iter] + "has been succesfully inserted "; 
					//callback.onResponse(r);
					iter++;
					isErrorOccurs= false; 
				}

			} catch (IOException e) {
				callback.onIOException(e.getMessage());
			} catch (NoSuchElementException e) {
				callback.onNoApplet();
			}catch(Exception e ){
				System.out.println("Exception " + e.getMessage());
				e.printStackTrace();
			}
			finally{
				if(isErrorOccurs)
					break;
			}

		}
		if(!isErrorOccurs)
			callback.onResponse(response);
	}

	private void performSelect(String commande){
		//select ins1, ins2 from applet where ins1=abc  and  ins2=4562
		String[] response;
		int indexFrom = commande.indexOf("from"); 
		int indexWhere = commande.indexOf("where");
		String col = commande.substring(6, indexFrom).trim(); 
		String[] whereValues = null;
		if (indexWhere!=-1){
			indexWhere+=5; //move to 5 chars after the w in where  
			String[] whereClauses = commande.substring(indexWhere).trim().split(" and ");
			if(whereClauses.length!=0){
				whereValues = new String[whereClauses.length];
				int index= 0; 
				for(String st : whereClauses){
					st = st.trim(); 
					String[] val = st.split("=");
					if(val.length==2){
						whereValues[index] = val[1]; 
						index++; 
					}else{
						//TODO: handle bad syntax of request 
						return;
					}
					
				}
			}
			
		}
		//Channel channel = null;
		try {
			//channel = openChannel();		
		String[] tab = col.split(",");
		response = new String[tab.length];
		int iter=0;
		boolean isErrorOccurs=true;
		//get data in the select command   	
		for(String st : tab){
			String data = null; 
			if(whereValues!=null && (iter<=whereValues.length-1)) 
				data = whereValues[iter]; 
			byte[] command = setCommand(CLA, Byte.valueOf(st), data);
			try {
				//byte[] resp =sendApdu(command);
				//private byte[] sendApdu(byte[] apduCommand) throws IOException, NoSuchElementException, NoReaderException, NoSecureElementException{
					
					byte[] resp = channel.transmit(command);
					
					System.out.println(resp[resp.length-2]);
					System.out.println(resp[resp.length-1]);
				
				if(!isResponseSucceded(resp)){
					if(isPinRequired(resp)) {
						callback.onPINRequired();
					}else{
						ApduError error = new ApduError("Wrong APDU response", getResponseCode(resp));
						callback.onAPDUError(error);
					}
				}else{
					
					String r  = getResponseBody(resp);
					response[iter] = r; 
					callback.onResponse(r);
					iter++;
					isErrorOccurs= false; 
				}

			} catch (IOException e) {
				callback.onIOException(e.getMessage());
			} catch (NoSuchElementException e) {
				callback.onNoApplet();
			} finally{
				if(isErrorOccurs)
					break;
			}
		}
		if(!isErrorOccurs)
			callback.onResponse(response);
		} catch (NoSuchElementException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally
		{
			//if(channel!=null)
			//channel.getSession().close();
		}
	}



	public byte[] getAppletAID() {
		return appletAID;
	}

	public void setAppletAID(byte[] appletAID) {
		this.appletAID = appletAID;
	}

	private byte[] setCommand(byte CLA, byte INS, String data){
		System.out.println("Data : " +data);
		boolean hasData = data==null ? false: true; 
		byte[] dataByte = null;
		int lenght=4; 
		if(hasData){
			dataByte = stringToHex(data);//;.getBytes(); 
			lenght+= dataByte.length + 1;
		} 
		byte[] command = new byte[lenght];
		command[0] = CLA; 
		command[1] = INS; 
		command[2] = 0x00; 
		command[3] = 0x00; 
		//command[command.length-1] = 0;
        System.out.println("Inst : " + INS);
		if(hasData){
			command[4] = (byte)dataByte.length;
			System.arraycopy(dataByte, 0, command, 5, dataByte.length);
		}
		for(byte b: command)
			System.out.println(b);
		return command;
	}

//	public void sendApdu(ApduCommand apdu, int code){
//		appletAID = apdu.appletAID;
//		boolean hasData = apdu.data==null ? false: true; 
//		byte[] data = null;
//		int lenght=5; 
//		if(hasData){
//			data = apdu.data.getBytes(); 
//			lenght+= data.length + 1;
//		} 
//		byte[] command = new byte[lenght];
//		command[0] = apdu.CLA; 
//		System.out.println("APDu.CLA is : "+apdu.CLA);
//		command[1] = apdu.INS; 
//		command[2] = 0x00; 
//		command[3] = 0x00; 
//		command[command.length-1] = (byte)apdu.responseLengh;
//
//		if(hasData){
//			command[4] = (byte)data.length;
//			System.arraycopy(data, 0, command, 5, data.length);
//		}
//
//		try {
//			byte[] resp =sendApdu(command);
//			if(!isResponseSucceded(resp)){
//				ApduError error = new ApduError("Wrong APDU response", getResponseCode(resp));
//				callback.onAPDUError(error);
//			}else{
//				String response  = getResponseBody(resp);
//				callback.onResponse(response);
//			}
//
//		} catch (IOException e) {
//			callback.onIOException(e.getMessage());
//		} catch (NoSuchElementException e) {
//			callback.onNoApplet();
//		} catch (NoReaderException e) {
//			callback.onNoReader();
//		} catch (NoSecureElementException e) {
//			callback.onNoSecureElement();
//		}
//
//	}



	class NoReaderException extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}

	class NoSecureElementException extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}
	
	
	public boolean isServiceConnected() {
		
		return (seService!=null) && seService.isConnected();
	}



	private Reader getPrincipalReader() throws NoReaderException, NoSecureElementException 	{
		Reader reader = null;
		Reader[] readers = seService.getReaders();
		
		if(readers.length==0)
			throw new NoReaderException();
		reader = readers[0];
		if(!reader.isSecureElementPresent())
			throw new NoSecureElementException();

		return reader;
	}

	@SuppressWarnings("unused")
	private Session openSession() throws NoReaderException, IOException, NoSecureElementException{
		Session session = getPrincipalReader().openSession(); 
		return session; 
	}

	private Channel openChannel() throws IOException, NoSuchElementException, NoReaderException, NoSecureElementException{
		Reader reader = getPrincipalReader();
		if(reader != null){
			Session session = getPrincipalReader().openSession();
			Channel channel = session.openLogicalChannel(appletAID); 
			//callback.onConnected();
			System.out.println("return channel");
			return channel;
		}
		throw new NoSuchElementException("No secure element present");

	}

	private byte[] sendApdu(byte[] apduCommand) throws IOException, NoSuchElementException, NoReaderException, NoSecureElementException{
		Channel channel = openChannel(); 
		byte[] resp = channel.transmit(apduCommand);
		channel.getSession().close();
		System.out.println(resp[resp.length-2]);
		System.out.println(resp[resp.length-1]);
		return resp;
	}

	private byte[] getResponseCode(byte[] resp) throws NoSuchElementException{
		return new byte[]{resp[resp.length-2],resp[resp.length-1]}; 
	}

	private boolean isResponseSucceded(byte[] resp){
		return ((resp[resp.length-2]== (byte)0x90) && (resp[resp.length-1] == (byte)0x00));
	}
	
	private boolean isPinRequired(byte[] resp){
		return ((resp[resp.length-2]== (byte)0x63) && (resp[resp.length-1] == (byte)0x01));
	}

	private String getResponseBody(byte[] resp){
		int size = 0;
		byte[] response = new byte[resp.length-2];
		for(int i =0;i<resp.length -2; i++)
			if(resp[i] != 0x00){
				response[size] = resp[i];
				size++;
			}
		try {
			return new String(response,0,size,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}

