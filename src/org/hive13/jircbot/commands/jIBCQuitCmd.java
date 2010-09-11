package org.hive13.jircbot.commands;

import org.hive13.jircbot.jIRCBot;

public class jIBCQuitCmd extends jIBCommand {

    @Override
    public String getCommandName() {
        return "quit";
    }

    @Override
    public void handleMessage(jIRCBot bot, String channel, String sender,
            String message) {
        bot.abShouldQuit.set(true);
        bot.quitServer("Cya losers!");
        
    }

}
