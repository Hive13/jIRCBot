package org.hive13.jircbot.commands;
import org.hive13.jircbot.jIRCBot;
import org.hive13.jircbot.jIRCBot.eLogLevel;
import org.hive13.jircbot.support.jIRCProperties;
import org.hive13.jircbot.support.jIRCUser.eAuthLevels;


/**
 * This classes main purpose is to perform regular maintenance tasks that
 * might crop in while the bot is running.
 * 
 * Examples of these tasks:
 * - Check if we are still connected
 * - Check the 'op' status of the users.
 * 
 * @author vincentp
 *
 */
public class jIBCTMaintThread extends jIBCommandThread {

	private static long dfLoopDelay = 120000; // 120 seconds, 2 minutes.
	
	public jIBCTMaintThread(jIRCBot bot, String commandName, String channel) {
		this(bot, commandName, channel, dfLoopDelay, eAuthLevels.admin);
	}

	public jIBCTMaintThread(jIRCBot bot, String commandName, String channel,
			long loopDelay, eAuthLevels authLevel) {
		super(bot, commandName, channel, loopDelay, authLevel);
	}

	@Override
	public void loop() {
		// Check if the bot is currently connected and is not currently connecting.
		if(!bot.isConnected() && !bot.abConnecting.get()) {
			bot.log(this.commandName + " - Bot is not connected.  Initiating Reconnected.", eLogLevel.error);
			bot.connectBot();
			if(bot.isConnected()) {
				bot.log(this.commandName + " - Bot is reconnected! Huzzah!", eLogLevel.info);
			} else {
				bot.log(this.commandName + " - Bot failed to reconnect... see you in " + Long.toString((dfLoopDelay / 1000)) + " seconds", eLogLevel.severe);
			}
		}
		
		// Check if we are currently an operator
		jIRCProperties.getInstance().getOpChannels();
		boolean botIsOp = bot.getUser(channel, bot.getNick()).isOp();
		if(!botIsOp) {
			// For some reason we are not... lets try to op ourself.
			bot.sendMessage("chanserv", "op " + channel);
		} else {
			// Ok, so lets wait for the maint thread to tick around again before doing this.
			// Now that we are operator, lets check on the peon's in the channel with us.
			
		}
		
	}

	@Override
	public String getHelp() {
		String cmdName = getSimpleCommandName();
		return "The following are valid uses of this command: !" + cmdName + " help ;" +
				" !" + cmdName + " start ; !" + cmdName + " stop";
	}

}
