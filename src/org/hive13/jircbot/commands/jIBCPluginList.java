package org.hive13.jircbot.commands;

import java.util.HashMap;
import java.util.Iterator;

import org.hive13.jircbot.jIRCBot;

public class jIBCPluginList extends jIBCommand {

	private HashMap<String, jIBCommand> commands;
	
	public jIBCPluginList(HashMap<String, jIBCommand> commands) {
		this.commands = commands;
	}

	@Override
	public String getCommandName() {
		return "plugins";
	}

    @Override
    protected void handleMessage(jIRCBot bot, String channel, String sender,
            String message) {
    	// Find sender's Auth Level
        String resultMsg = sender + ": ";
        Iterator<jIBCommand> i = commands.values().iterator();
        while(i.hasNext()) {
        	jIBCommand curCmd = i.next();
            resultMsg += curCmd.getCommandName();
            if(i.hasNext())
                resultMsg += ", ";
        }
        bot.sendMessage(channel, resultMsg);
        
    }

	

}
