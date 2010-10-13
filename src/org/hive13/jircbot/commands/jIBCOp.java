package org.hive13.jircbot.commands;

import org.hive13.jircbot.jIRCBot;
import org.hive13.jircbot.support.jIRCUser.eAuthLevels;

public class jIBCOp extends jIBCommand {

    public jIBCOp() {
        super(eAuthLevels.operator);
    }
    
    public String getHelp() {
        return "";
    }
    
    @Override
    public String getCommandName() {
        return "Op";
    }

    @Override
    protected void handleMessage(jIRCBot bot, String channel, String sender,
            String message) {
        //String splitMsg
        bot.op(channel, message);
    }

}
