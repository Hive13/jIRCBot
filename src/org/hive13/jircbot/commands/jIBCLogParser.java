package org.hive13.jircbot.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.hive13.jircbot.jIRCBot;
import org.hive13.jircbot.support.jIRCTools;
import org.hive13.jircbot.support.jIRCTools.eMsgTypes;

public class jIBCLogParser extends jIBCommand {

	@Override
	public String getCommandName() {
		return "logParser";
	}

	public String getHelp() {
		return "This command is non-functional.";
	}
	
	protected void handleMessage(jIRCBot bot, String channel, String sender,
			String message) {
		bot.sendMessage(channel, "Starting to parse log...", eMsgTypes.publicMsg);
		boolean result = translateLogToSQL(message);
		if (result)
			bot.sendMessage(channel, "Finished parsing log.", eMsgTypes.publicMsg);
		else
			bot.sendMessage(channel, "Failed to parse log.", eMsgTypes.publicMsg);
	}

	Date logDate;
	Calendar logCalendar = Calendar.getInstance();

	public boolean translateLogToSQL(String pathToLog) {
		logCalendar.clear();
		logCalendar.set(2009, Calendar.AUGUST, 6);
		logDate = logCalendar.getTime();

		boolean result = true;
		File file = new File(pathToLog);
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;

			while ((text = reader.readLine()) != null) {
				result &= parseLine(text);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			result = false;
		} catch (IOException e) {
			e.printStackTrace();
			result = false;
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public boolean parseLine(String logLine) {
		boolean result = false;
		/*
		 * Different possibilities:
		 * 
		 * Date changes: Events:
		 */
		if (logLine.substring(0, 3).equals("---")) {
			SimpleDateFormat sdf;
			logLine = logLine.substring(15).trim();
			if (logLine.substring(13, 14).equals(":")) {
				// --- Log opened Thu Aug 06 15:06:11 2009
				sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy",
						Locale.US);
			} else {
				// --- Day changed Sat Mar 06 2010
				sdf = new SimpleDateFormat("EEE MMM dd yyyy", Locale.US);
			}
			try {
				logDate = sdf.parse(logLine);
				result = true;
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if (logLine.substring(6, 7).equals("<")) {
			// We are a regular message
			// 00:40 <@davemenninger> but it doesn't feel right
			// 12:53 < Hive13-bench-pc> a likely story
			String h = logLine.substring(0, 2);
			String m = logLine.substring(3, 5);

			int endIndex = logLine.indexOf(">", 8);
			String username = logLine.substring(8, endIndex);

			String message = logLine.substring(endIndex + 2);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String date = sdf.format(logDate) + " " + h + ":" + m + ":00";

			jIRCTools.insertMessage("#hive13", "irc.freenode.net", username,
					message, jIRCTools.eMsgTypes.publicMsg, date);
			result = true;
		} else if (logLine.substring(7, 10).equals(">>>")) {
			// We are a "me" command
			// 17:09 >>> Paul_Hive13 is bored
			String h = logLine.substring(0, 2);
			String m = logLine.substring(3, 5);

			int endIndex = logLine.indexOf(" ", 11);
			String username = logLine.substring(11, endIndex);
			String message = logLine.substring(endIndex + 1);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String date = sdf.format(logDate) + " " + h + ":" + m + ":00";

			jIRCTools.insertMessage("#hive13", "irc.freenode.net", username,
					message, jIRCTools.eMsgTypes.actionMsg, date);
			result = true;
		} else if (logLine.substring(6, 9).equals(":::")) {
			// We are a name change
			// We are a join
			// We are a quit
			// We are a part
			String h = logLine.substring(0, 2);
			String m = logLine.substring(3, 5);

			int endIndex = logLine.indexOf(" ", 10);
			String username = logLine.substring(10, endIndex);

			String msg = "";

			jIRCTools.eMsgTypes msgType = jIRCTools.eMsgTypes.nickChange;
			if (logLine.contains("is now known as")) {
				// 03:50 ::: KuRoHa_ is now known as KuRoHa
				msgType = jIRCTools.eMsgTypes.nickChange;

				String temp = logLine.substring(logLine.lastIndexOf(" "));
				msg = username;
				username = temp;
			} else if (logLine.contains("has joined")) {
				// 00:57 ::: strages
				// [n=strages@c-76-29-231-141.hsd1.al.comcast.net] has joined
				// #hive13
				msgType = jIRCTools.eMsgTypes.joinMsg;
			} else if (logLine.contains("has left")) {
				// 16:42 ::: ultraj61 [n=name1@74.83.28.90] has left #hive13 []
				msgType = jIRCTools.eMsgTypes.partMsg;
			} else if (logLine.contains("has quit")) {
				// 10:16 ::: strages
				// [n=strages@c-76-29-231-141.hsd1.al.comcast.net] has quit
				// [Connection timed out]
				msgType = jIRCTools.eMsgTypes.quitMsg;
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String date = sdf.format(logDate) + " " + h + ":" + m + ":00";

			jIRCTools.insertMessage("#hive13", "irc.freenode.net", username,
					msg, msgType, date);
			result = true;
		} else {
			result = true;
		}
		/*
		if (!result) {
			int i = 0;
			i++;
		}*/
		return result;
	}

}
