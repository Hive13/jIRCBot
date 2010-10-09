package org.hive13.jircbot.commands;

import org.hive13.jircbot.jIRCBot;
import org.hive13.jircbot.jIRCBot.eLogLevel;
import org.hive13.jircbot.support.jIRCUser.eAuthLevels;

/**
 * A basic framework for implementing commands.
 * 
 * @author vincentp
 *
 */
public abstract class jIBCommand {
    protected eAuthLevels reqAuthLevel;
    
    public jIBCommand() {
        this(eAuthLevels.unauthorized);
    }
    
    public jIBCommand(eAuthLevels reqAuthLevel) {
        this.reqAuthLevel = reqAuthLevel;
    }
    
    public eAuthLevels getReqAuthLevel() {
        return reqAuthLevel;
    }
    
	/**
	 * This method returns a unique name for the command.
	 * 
	 * @return A unique name for the command.
	 */
    public abstract String getCommandName();
    
    /**
     * This method is called to activate the command.
     * 
     * @param bot		The bot that the command will be run against.
     * @param channel	The channel that this command will run in.
     * @param sender	The user that caused this command to be activated.
     * @param message	The parameters (including the commandName) that were
     * 					received when this command was activated.
     */
    public void runCommand(jIRCBot bot, String channel, String sender, String message) {
        eAuthLevels userAuthLevel = bot.userListGet(sender).getAuthLevel();
        if(userAuthLevel.ordinal() >= getReqAuthLevel().ordinal())
            new Thread(new jIBCommandRunnable(bot, channel, sender, message)).start();
        else {
            bot.sendMessage(sender, "You do not have permission to activate this command.");
            bot.log(sender + " just tried to use " + getCommandName() 
                    + " but their AuthLevel is only " + userAuthLevel, eLogLevel.warning);
        }
    }
    
    
    /**
     * This method is internal to the commands.  It is handled in a separate thread
     * so any calls from this method MUST be thread safe.
     * 
     * @param bot		The bot that the command will run against.
     * @param channel	The channel that this command will run in.
     * @param sender	The user that caused this command to be activated.
     * @param message	The parameters (including the commandName) that were received
     * 					when this command was activated.
     */
    protected abstract void handleMessage(jIRCBot bot, String channel, 
            String sender, String message);
    
    /**
     * This class is used to launch off an asynchronous
     * thread to prevent Commands from blocking the main thread.
     */
    protected class jIBCommandRunnable implements Runnable {
        private jIRCBot bot;
        private String channel;
        private String sender;
        private String message;
        
        public jIBCommandRunnable(jIRCBot bot, String channel, String sender,
                String message) {
            super();
            this.bot = bot;
            this.channel = channel;
            this.sender = sender;
            this.message = message;
        }
        
        @Override
        public void run() {
            handleMessage(bot, channel, sender, message);
        }
        
    }
}
