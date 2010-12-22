package org.hive13.jircbot.commands;

import org.hive13.jircbot.jIRCBot;

public class jIBCHelp extends jIBCommand {

	@Override
	public String getCommandName() {
		return "help";
	}

	@Override
	public String getHelp() {
		return "No.";
	}

	@Override
	protected void handleMessage(jIRCBot bot, String channel, String sender,
			String message) {
		bot.sendMessage(sender, "To get a list of available commands use '!plugins'. " +
				"To learn more about a command type '!plugins help'.");
	}

}
