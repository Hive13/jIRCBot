package org.hive13.jircbot.commands;

import java.util.Date;

import org.hive13.jircbot.jIRCBot;

/**
 * A simple time command. Tells the bot to give
 * the current time when it is given "time".
 *
 * @author AMcBain ( http://www.asmcbain.net/ ) @ 2009
 */
public class jIBCTimeCmd extends jIBCommand {

    @Override
    public String getCommandName() {
        return "time";
    }

    @Override
    public void handleMessage(jIRCBot bot, String channel, String sender,
            String message) {
        bot.sendMessage(channel, sender + ": The time is now " + (new Date()));

    }

}
