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
        bot.abShouldQuit.set(true);
        bot.quitServer("Cya losers!");
        
    }

}
