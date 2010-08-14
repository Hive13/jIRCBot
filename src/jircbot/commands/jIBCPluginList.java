package jircbot.commands;

import java.util.HashMap;
import java.util.Iterator;

import org.jibble.pircbot.PircBot;

public class jIBCPluginList implements jIBCommand {

	private HashMap<String, jIBCommand> commands;
	
	public jIBCPluginList(HashMap<String, jIBCommand> commands) {
		this.commands = commands;
	}

	@Override
	public String getCommandName() {
		return "plugins";
	}

	@Override
	public void handleMessage(PircBot bot, String channel, String sender,
			String message) {
		String resultMsg = sender + ": ";
		Iterator<jIBCommand> i = commands.values().iterator();
		while(i.hasNext()) {
			resultMsg += i.next().getCommandName();
			if(i.hasNext())
			    resultMsg += ", ";
		}
		bot.sendMessage(channel, resultMsg);
	}

}
