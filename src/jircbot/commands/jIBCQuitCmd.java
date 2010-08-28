/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jircbot.commands;

import org.jibble.pircbot.PircBot;

/**
 *
 * @author vincenpt
 */
public class jIBCQuitCmd extends jIBCommand{

    public String getCommandName() {
        return "quit";
    }

    @Override
    public void runHandleMessage(PircBot bot, String channel, String sender, String message) {
        bot.quitServer("Cya losers!");
    }
}
