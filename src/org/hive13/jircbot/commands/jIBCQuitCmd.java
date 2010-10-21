package org.hive13.jircbot.commands;

import org.hive13.jircbot.jIRCBot;
import org.hive13.jircbot.support.jIRCUser.eAuthLevels;

public class jIBCQuitCmd extends jIBCommand {
	
    /**
     * @param reqAuthLevel
     */
    public jIBCQuitCmd() {
        super(eAuthLevels.admin);
    }

    @Override
    public String getCommandName() {
        return "quit";
    }

    public String getHelp() {
    	return "This command safely shuts the bot down. Ex. !" + getCommandName();
    }
    
    @Override
    public void handleMessage(jIRCBot bot, String channel, String sender,
            String message) {
    	// There is an 'onDisconnect' method in the main bot that gets triggered
    	// when the bot disconnects from the server.  I only want the bot to
    	// shut down when called from this command, and Disconnects can happen
    	// in a variety of ways.  In most cases the bot should attempt to reconnect
    	// after a Disconnect, however the quit command works by triggering a
    	// disconnect.  To ensure that the bot will try to reconnect unless
    	// called from this method, I added a thread safe AtomicBoolean that
    	// is checked before the bot disconnects.  If the AtomicBoolean is not
    	// set to 'true' then the bot will try to reconnect.
        bot.abShouldQuit.set(true);
        bot.quitServer("Cya losers!");
        
    }

}
