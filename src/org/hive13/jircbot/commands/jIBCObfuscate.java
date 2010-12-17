package org.hive13.jircbot.commands;

import org.hive13.jircbot.jIRCBot;

public class jIBCObfuscate extends jIBCommand {

	@Override
	public String getCommandName() {
		return "anon";
	}

	@Override
	public String getHelp() {
		// TODO Finish off the help
		return "In development: This command will obfuscate a word (generally username) in the logs.";
	}

	@Override
	protected void handleMessage(jIRCBot bot, String channel, String sender,
			String message) {
		/* In order to fully obfuscate all instances of the users name in the past and future we need to do the following:
		 * 1. Add word // user's name to a "Do not log" list.
		 * 2. Process all previous logs to remove all instances of this word // user's name
		 * 
		 * Part #1 
		 * - This has two parts in itself.
		 * 	1. Adding this word // user's name to a 'Do Not Log' list.
		 *  2. Adding a function that checks all usernames and messages
		 *     for instances of words on the 'Do Not Log' list and then
		 *     proceeds to obfuscate them before they hit the database.
		 *     
		 * Part #2
		 * - Cleaning the back logs is going to be a very intensive process.
		 * 	 As of writing this we have almost 60,000 messages logged.
		 * - There are several stages to the cleaning process.
		 *   1. Replace all instances of the word as a 'vcUsername'
		 *      with another random username.  This 'other' username
		 *      should not be: Phergie, Hive13Bot, blank, or the word
		 *      we are replacing.
		 *   2. Replace all instances of the word where it is contained
		 *      in a message.
		 */
		
		// Add word to 'Do Not Log' list.
		
		// Initiate cleaning of back logs.
		

	}

}
