package jircbot.commands;

import java.util.HashMap;
import java.util.Iterator;

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
    public void run() {
        String resultMsg = sender + ": ";
        Iterator<jIBCommand> i = commands.values().iterator();
        while(i.hasNext()) {
            resultMsg += i.next().getCommandName();
            if(i.hasNext())
                resultMsg += ", ";
        }
        bot.sendMessage(channel, resultMsg);
    }

	

}
