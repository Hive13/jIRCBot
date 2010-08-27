/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jircbot.commands;

import java.util.Date;
import org.jibble.pircbot.PircBot;

/**
 * A simple time command. Tells the bot to give
 * the current time when it is given "time".
 *
 * @author AMcBain ( http://www.asmcbain.net/ ) @ 2009
 */
public class jIBCTimeCmd extends jIBCommand {

    public jIBCTimeCmd() {
    }

    public String getCommandName() {
        return "time";
    }

    public void handleMessage(PircBot bot, String channel, String sender, String message) {
        bot.sendMessage(channel, sender + ": The time is now " + (new Date()));
    }

}
