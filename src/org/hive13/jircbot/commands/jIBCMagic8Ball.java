package org.hive13.jircbot.commands;

import java.util.Random;

import org.hive13.jircbot.jIRCBot;

public class jIBCMagic8Ball extends jIBCommand {

	private static Random r = new Random();
	private String strResponses[] = { "It is certain",
			  "It is decidedly so", "Without a doubt",
			  "Yes - definitely", "You may rely on it",
			  "As I see it, yes", "Most likely",
			  "Outlook good", "Signs point to yes",
			  "Yes", "Reply hazy, try again",
			  "Ask again later", "Better not tell you now",
			  "Cannot predict now", "Concentrate and ask again",
			  "Don't count on it", "My reply is no",
			  "My sources say no", "Outlook not so good",
			  "Very doubtful"};
	
	@Override
	public String getCommandName() {
		return "eightball";
	}

	@Override
	public String getHelp() {
		return "";	// Return a blank string to ensure we do not confuse the "Help" command, we will handle this ourselves.
	}

	@Override
	protected void handleMessage(jIRCBot bot, String channel, String sender,
			String message) {

		// - !eightball, !m8b, !magiceightball, !magic8ball (only eightball & question currently implemented)
		String[] splitMsg = message.split(" ", 2);
		if(splitMsg[0].equals(getCommandName()) ||	// If the command was directly invoked...
				(message.toLowerCase().startsWith(bot.getName().toLowerCase()) && message.endsWith("?"))) { // or the bot was asked a question.
			bot.sendMessage(channel, sender + ": " + strResponses[r.nextInt(20)]);
		}
	}

}
