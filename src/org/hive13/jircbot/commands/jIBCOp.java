package org.hive13.jircbot.commands;

import org.hive13.jircbot.jIRCBot;
import org.hive13.jircbot.jIRCBot.eLogLevel;
import org.hive13.jircbot.support.jIRCUser;
import org.hive13.jircbot.support.jIRCUser.eAuthLevels;

public class jIBCOp extends jIBCommand {

	public String getHelp() {
		return "This command has two uses.  If you are not an operator" +
				" but think you are on the approved list then the following" +
				" command will recheck your authorization: !op . If you are" +
				" a channel operator then this command can op other users."
				+ " Ex. !op Username";
	}

	@Override
	public String getCommandName() {
		return "Op";
	}

	@Override
	protected void handleMessage(jIRCBot bot, String channel, String sender,
			String message) {
		// We do not want anything that has spaces, so grab the first word.
		String[] splitMsg = message.trim().split(" ", 2);
		message = splitMsg[0];

		
		jIRCUser temp = bot.userListGetSafe(sender);
		if(temp.getAuthLevel().ordinal() < eAuthLevels.operator.ordinal()) {
			bot.startAuthForUser(temp);
		} else {
			temp = bot.userListGetSafe(message);
			if (temp != null) {
				boolean opped = false;
				// Check to make sure we are not downgrading someone from admin.
				if (temp.getAuthLevel().ordinal() < eAuthLevels.operator.ordinal()) {
					temp.setAuthorized(eAuthLevels.operator);
					bot.userListPutSafe(temp);
					opped = true;
				}
				// Check to make sure the target user is not already opped.
				if (!bot.getUser(channel, message).isOp()) {
					bot.op(channel, message);
					opped = true;
				}
	
				// If any action was actually performed, alert the user and log the
				// event.
				if (opped) {
					bot.sendMessage(message, sender
							+ " granted you operator priviledges.");
					bot.log(sender + " granted " + message
							+ " operator priviledges.", eLogLevel.info);
				}
			} else {
				bot.sendMessage(sender, "Could not find user (" + message + "). "
						+ getHelp());
			}
		}
	}

}
