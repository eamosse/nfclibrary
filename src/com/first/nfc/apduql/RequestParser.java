package com.first.nfc.apduql;

import java.util.HashMap;
import java.util.Map;

import com.first.nfc.apduql.exceptions.BadRequestException;

public class RequestParser {

	public static Map <String, Object> parseInsert(String commande)
			throws Exception {
		commande = commande.replaceAll("\\s+", " ");
		Map <String, Object> maps = new HashMap<String, Object>();
		// insert into applet (ins1,ins2) values(value1,value2)
		commande = commande.replaceAll("\\s+", " ");
		checkRequiredWords(commande, "insert", "into", "values");
		
		// get the applet name
		String appletName = nextWord(commande, "into");
		String instructions = wordBetween(commande, appletName, "values");

		if (instructions.length() == 0) {
			throw new BadRequestException(
					"Request mal formated! Missing instructions fields!");
		}

		if (!(instructions.startsWith("(") && instructions.endsWith(")"))) {
			throw new BadRequestException("Request mal formated! Unmatched (");
		}

		instructions = instructions.substring(instructions.indexOf("(") + 1,
				instructions.lastIndexOf(")"));
		System.out.println(instructions);

		// Let's get the fields on wich the requested ins projected
		instructions = instructions.replaceAll("\\s", "");
		String[] tabInstructions = instructions.split(",");
		maps.put("fields", tabInstructions);
		// Let see if we have this applet in metadatas
		Applet applet = Configuration.getAppletByName(appletName);
		maps.put("applet", applet);
		byte [] inst = checkFieldsBelongToApplet(tabInstructions, applet);
		byte clazz = applet.getClazz(Command.INSERT);

		// get the values to be inserted
		int indexValues = commande.indexOf("values");
		String values = commande.substring(indexValues);
		// values = values.substring(indexValues + 6).trim();
		values = values.substring(values.indexOf("(")+1, values.lastIndexOf(")"));
		System.out.println("Values " + values);

		String[] tabvalues = values.split(",");

		if (tabvalues.length == 0) {
			throw new BadRequestException(
					"Request mal formated! Missing the values to be inserted");

		}

		if (tabvalues.length != tabInstructions.length) {
			throw new BadRequestException(
					"Request mal formated! Values to be inserted should be exactely equal to the fields count!");

		}
		int iter = 0;

		byte[][] cmds = new byte[tabInstructions.length][];

		for (String value : tabvalues) {
int length = applet.getFieldLength(tabInstructions[iter]);
			byte[] command = setCommand(
					clazz,inst[iter],
					value, length);
			cmds[iter] = command;
			iter++;
		}
		maps.put("command", cmds);
		return maps;

	}

	private static byte[] checkFieldsBelongToApplet(String[] tabInstructions,
			Applet applet) throws BadRequestException {
		// Let's be sure that we have informations about the fields on wich the
		// request is projected on this applet
		byte[] resp = new byte[tabInstructions.length];
		int i =0; 
		for (String field : tabInstructions) {
			resp[i]= applet.getInstruction(field);
			i++;
		}
		return resp;
	}

	public static Map <String, Object> parseSelect(String commande)
			throws Exception {
		commande = commande.replaceAll("\\s+", " ");
		Map <String, Object> maps = new HashMap<String, Object>();
		checkRequiredWords(commande, "select", "from");

		// the selected columns are between select and from
		String col = wordBetween(commande, "select", "from"); // commande.substring(7,
		// now lets get the applet name (the next word after from)
		String appletName = nextWord(commande, "from");
		Applet applet = Configuration.getAppletByName(appletName);
		maps.put("applet", applet);
		col = col.replaceAll("\\s", "");
		String[] instructions = col.split(",");
		maps.put("fields", instructions);
		System.out.println("Instruction " + col);
		byte [] inst = checkFieldsBelongToApplet(instructions, applet);
		byte clazz = applet.getClazz(Command.SELECT);
		
		String[] whereValues = null;
		int indexWhere = commande.indexOf("where");
		if (indexWhere != -1) {

			indexWhere += 5; // move to 5 chars after the w in where

			int firstParenthesis = commande.indexOf("(", indexWhere);

			int secondParenthesis = commande.indexOf(")", indexWhere);

			if (firstParenthesis == -1 || secondParenthesis == -1)

				throw new BadRequestException("Request mal formatted");

			String[] whereClauses = commande

			.substring(firstParenthesis + 1, secondParenthesis).trim()

			.split(" and ");

			if (whereClauses.length != 0) {

				whereValues = new String[whereClauses.length];

				int index = 0;

				for (String st : whereClauses) {

					st = st.trim();

					String[] val = st.split("=");

					if (val.length == 2) {

						whereValues[index] = val[1];

						System.out.println("******************" + val[1]);

						index++;

					} else {
						// TODO: handle bad syntax of request
						throw new BadRequestException("Request mal format");
					}

				}

			}

		}
		int iter = 0;
		byte[][] cmds = new byte[instructions.length][];
		
		for (String st : instructions) {
			String data = null;
			int length = applet.getFieldLength(st);
			if (whereValues != null && (iter <= whereValues.length - 1))
				data = whereValues[iter];
			byte[] command = setCommand(
					clazz, inst[iter], data, length);
			cmds[iter] = command;
			iter++;
		}
		maps.put("command", cmds);
		return maps;
	}

	private static String wordBetween(String string, String first, String second) {
		int indexFirst = string.indexOf(first);
		int indexSecond = string.indexOf(second);
		return string.substring(first.length() + indexFirst, indexSecond)
				.trim();
	}

	private static String nextWord(String string, String word) {
		int index = string.indexOf(word);
		int idx1 = string.indexOf(" ", index + word.length() + 1);
		idx1 = idx1 != -1 ? idx1 : string.length();
		return string.substring(index + word.length() + 1, idx1);
	}

	private static boolean checkRequiredWords(String string, String... words)
			throws BadRequestException {
		for (String word : words) {
			int index = string.indexOf(word);
			if (index == -1)
				throw new BadRequestException("Request mal formatted! Missing "
						+ word);
			String fallowed = string.substring(word.length() + index,
					word.length() + index + 1);
			if (!fallowed.equals(" "))
				throw new BadRequestException(
						"Request mal formatted! Unknow the word "
								+ word.concat(fallowed) + " in command "
								+ string);
		}
		return true;

	}

	private static byte[] setCommand(byte CLA, byte INS, String data, int lc) throws Exception {

		System.out.println("Data : " + data);

		boolean hasData = data == null ? false : true;

		byte[] dataByte = null;

		int lenght =5;// lc!=0 ? 5 : 4;

		if (hasData) {

			dataByte = data.getBytes();

			lenght += dataByte.length + 1;

		}

		byte[] command = new byte[lenght];

		command[0] = (byte)CLA;

		command[1] = (byte)INS;

		command[2] = 0x00;

		command[3] = 0x00;

		command[command.length - 1] = Utils.hexStringToByteArray(Integer.toHexString(lc))[0];

		System.out.println("Inst : " + CLA + " " + (byte)CLA);
		System.out.println("Inst : " + INS + " " + (byte)INS);

		if (hasData) {

			command[4] = (byte) dataByte.length;

			System.arraycopy(dataByte, 0, command, 5, dataByte.length);

		}

		for (byte b : command)

			System.out.println(b);

		return command;

	}

}
