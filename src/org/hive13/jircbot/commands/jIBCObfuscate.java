package org.hive13.jircbot.commands;

import java.util.ArrayList;

import org.hive13.jircbot.jIRCBot;
import org.hive13.jircbot.support.MessageRow;
import org.hive13.jircbot.support.jIRCTools;
import org.hive13.jircbot.support.jIRCTools.eMsgTypes;

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
		// Determine the target:
        String targetUser = message;
        if(message.isEmpty())
            targetUser = sender;
        
		// Add word to 'Do Not Log' list.
	    //TODO: Do this step.

        // Initiate cleaning of back logs.
        // First we need the ID's of all the messages a user has sent
		ArrayList<MessageRow> allMsgs = jIRCTools.getMessagesByUser(targetUser);
		if(allMsgs.size() > 0) {
		    // We found some messages sent by the target.
    		bot.sendMessage(channel, "Found " + allMsgs.size() + " messages for target.", eMsgTypes.LogFreeMsg);
    		
    		// Then we need a list of random usernames that we have seen.
    		ArrayList<MessageRow> randomNames = jIRCTools.getRandomUsernames(targetUser, allMsgs.size());
            bot.sendMessage(channel, "Retrieved " + randomNames.size() + " random usernames." +
            		"Starting username nuke.",  eMsgTypes.LogFreeMsg);
    
            // Time to build a huge update statement to eradicate all references to the target.
            jIRCTools.updateAllTargetsUsernames(targetUser, allMsgs, randomNames);
    		bot.sendMessage(channel, "Nuke query finished, checking username count...",  eMsgTypes.LogFreeMsg);
    		
    		// Lets check to see if the previous query worked...
            allMsgs = jIRCTools.getMessagesByUser(targetUser);
            bot.sendMessage(channel, "Found " + allMsgs.size() + " messages for target.",  eMsgTypes.LogFreeMsg);
            if(allMsgs.size() > 0) {
                bot.sendMessage(channel, "Well crap, contact Paul with the username you are trying to nuke.",  eMsgTypes.LogFreeMsg);
            } else {
                bot.sendMessage(channel, "Username nuke appears to have worked.  Moving onto message reference nuke.",  eMsgTypes.LogFreeMsg);
            }
		} else {
		    bot.sendMessage(channel, "Did not find any messages from this username.  Moving onto the message check.",  eMsgTypes.LogFreeMsg);
		}
		
        // Ok, so hopefully we have managed to nuke all of the users direct messages.
        // Now let's try to 'fix' all messages sent TO the user.
		// So lets search for any messages that mention the targetUser.
        allMsgs = jIRCTools.searchMessagesForKeyword(targetUser);
        if(allMsgs.size() > 0) {
            // We found some messages.
            bot.sendMessage(channel, "Found " + allMsgs.size() + " references to target in messages." +
            		" Getting random usernames now.",  eMsgTypes.LogFreeMsg);
            
            // Now lets find some stuff to replace the names with.
            ArrayList<MessageRow> randomNames = jIRCTools.getRandomUsernames(targetUser, allMsgs.size());
            bot.sendMessage(channel, "Retreived " + randomNames.size() + " random usernames.  Starting" +
            		" obfuscation of messages, this may take some time.",  eMsgTypes.LogFreeMsg);
            
            // Ok, message obfuscation time...
            for(int i = 0; i < allMsgs.size(); i++) {
                String targetMessage = allMsgs.get(i).vcMessage;               
                targetMessage = jIRCTools.replaceAll(targetMessage, targetUser, randomNames.get(i).vcUsername);
                allMsgs.get(i).vcMessage = targetMessage;
            }
            
            // Now time to update the database with the obfuscated messages.
            jIRCTools.updateAllTargetsMessages(targetUser, allMsgs);
            bot.sendMessage(channel, "Message 'fix' finished, checking results...",  eMsgTypes.LogFreeMsg);
            
            // Check the result.
            allMsgs = jIRCTools.searchMessagesForKeyword(targetUser);
            if(allMsgs.size() > 0)
                bot.sendMessage(channel, "Replace failed for some reason. " + allMsgs.size() + 
                        " with target still exist.  Contact Paul to fix this.",  eMsgTypes.LogFreeMsg);
            else
                bot.sendMessage(channel, "Message replacement worked. " + allMsgs.size() + 
                        " messages found with target.",  eMsgTypes.LogFreeMsg);
            
        } else {
            bot.sendMessage(channel, "You must not be popular, no references found to target in messages.", 
                    eMsgTypes.LogFreeMsg);
        }
        bot.sendMessage(channel, "Finished.  Future messages from target will now be obfuscated." +
        		" If you wish me to stop obfuscating the target, contact Paul.",
        		eMsgTypes.LogFreeMsg);
		
	}

}
