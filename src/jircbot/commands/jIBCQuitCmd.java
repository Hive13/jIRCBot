/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jircbot.commands;

/**
 *
 * @author vincenpt
 */
public class jIBCQuitCmd extends jIBCommand{

    public String getCommandName() {
        return "quit";
    }

    @Override
    public void run() {
        bot.quitServer("Cya losers!");
    }
}
