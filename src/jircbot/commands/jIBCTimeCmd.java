/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jircbot.commands;

import java.util.Date;

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

    @Override
    public void run() {
        bot.sendMessage(channel, sender + ": The time is now " + (new Date()));
        
    }

}
