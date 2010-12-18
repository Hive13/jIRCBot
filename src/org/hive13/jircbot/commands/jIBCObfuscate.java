package org.hive13.jircbot.commands;

import java.util.ArrayList;

import org.hive13.jircbot.jIRCBot;
import org.hive13.jircbot.support.MessageRow;
import org.hive13.jircbot.support.jIRCTools;

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
	    // First we need the ID's of all the messages a user has sent  
	    String targetUser = "Hodapp";
		ArrayList<MessageRow> allMsgs = jIRCTools.getMessagesByUser(targetUser);
		bot.sendMessage(channel, "Found " + allMsgs.size() + " messages for target.");
		
		// TODO: Add check to stop if we do not find any messages.
		
		// Then we need a list of random usernames that we have seen.
		ArrayList<MessageRow> randomNames = jIRCTools.getRandomUsernames(targetUser, allMsgs.size());
        bot.sendMessage(channel, "Retrieved " + randomNames.size() + " random usernames.");
        bot.sendMessage(channel, "Starting username nuke, this may take a while.");

        jIRCTools.updateAllTargetsUsernames(targetUser, allMsgs, randomNames);
		bot.sendMessage(channel, "Nuke query finished, checking username count...");
		
        allMsgs = jIRCTools.getMessagesByUser(targetUser);
        bot.sendMessage(channel, "Found " + allMsgs.size() + " messages for target.");
        if(allMsgs.size() > 0) {
            bot.sendMessage(channel, "Well crap, contact Paul with the username you are trying to nuke.");
        } else {
            bot.sendMessage(channel, "Username nuke appears to have worked.  Moving onto message reference nuke.");
        }
		// Time to build a huge update statement to eradicate all references to the target.
		/*
		 * UPDATE messages
              SET vcUsername = CASE pk_MessageID
                WHEN 1 THEN (Find a random)
                WHEN 2 THEN (Find a random)
                ...
              END
            WHERE pk_MessageID in (1, 2, .....);
		 */
		
	}

}
