package org.hive13.jircbot.commands;

import java.util.HashMap;
import java.util.Iterator;

import org.hive13.jircbot.jIRCBot;
import org.hive13.jircbot.support.jIRCUser.eAuthLevels;

public class jIBCPluginList extends jIBCommand {

	private HashMap<String, jIBCommand> commands;
	private HashMap<String, jIBCommand> lineParseCmds;

	public jIBCPluginList(HashMap<String, jIBCommand> commands,
			HashMap<String, jIBCommand> lineParseCmds) {
		this.commands = commands;
		this.lineParseCmds = lineParseCmds;
	}

	@Override
	public String getCommandName() {
		return "plugins";
	}

	public String getHelp() {
		return "This command returns the list of all commands that you are "
				+ "able to run. Ex. !" + getCommandName();
	}

	@Override
	protected void handleMessage(jIRCBot bot, String channel, String sender,
			String message) {
		// Find sender's Auth Level
		eAuthLevels userAuthLevel = bot.userListGetSafe(sender).getAuthLevel();

		boolean processedLineParse = false;
		boolean foundOne = false;
		String resultMsg = sender + ": ";

		// Start iterating through the standard list of commands.
		Iterator<jIBCommand> i = commands.values().iterator();
		while (i.hasNext()) {
			jIBCommand curCmd = i.next();
			// Does the user have permissions to see that this command exists?
			// Are we hiding this command from everyone? (aka Linkify)
			if (curCmd.getReqAuthLevel().ordinal() <= userAuthLevel.ordinal()
					&& !curCmd.isHidden()) {

				// Add command to the result list.
				if (curCmd instanceof jIBCommandThread) {
					if(((jIBCommandThread) curCmd).getChannel().equals(channel)) {
						// 2nd if statement eliminates commands for other channels.
						
						// Should we prepend this with a comma?
						if (foundOne)
							resultMsg += ", ";
						else
							foundOne = true;
						
						// cmdThreads have their channel in the name by default.
						// getSimpleCmdName ensures we get the root name of the cmd.
						resultMsg += ((jIBCommandThread) curCmd).getSimpleCommandName();
					}
				} else {
					// Should we prepend this with a comma?
					if (foundOne)
						resultMsg += ", ";
					else
						foundOne = true;
					
					resultMsg += curCmd.getCommandName();
				}
			}

			// We have finished w/ the list of commands,
			// Have we processed the LineParser commands?
			if (!i.hasNext() && !processedLineParse) {
				// Start processing the lineParse commands.
				i = lineParseCmds.values().iterator();
				processedLineParse = true;
			}
		}
		// We have built the result message, send it directly
		// to the calling user.
		bot.sendMessage(sender, resultMsg);

	}

}
