/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jircbot.commands;

import org.jibble.pircbot.PircBot;

/**
 * This is the definition for all BotCommands. Anything
 * defined here must be implemented by a BotCommand.
 *
 * This specifies a method to be called on events and
 * a method to of determining the "name" of the command.
 * A more complex BotCommand could define other methods
 * to handle more events than the simple message event.
 *
 * See the included QuitCommand and TimeCommand for
 * simple sample implementations of BotCommand.
 *
 * @author AMcBain ( http://www.asmcbain.net/ ) @ 2009
 */
public abstract class jIBCommand implements Runnable {
    protected PircBot bot;
    protected String channel;
    protected String sender;
    protected String message;

    /**
     * Each BotCommand implementor will return the command name to which they respond.
     */
    public abstract String getCommandName();

    /** 
     * The method where each BotCommand implementor will handle the event
     * @param bot       The bot to which this command belongs.
     * @param channel   The channel the message originated on.
     * @param sender    The initiator of the message we are handling.
     * @param message   The message that we are handling.
     */
    public void handleMessage(PircBot bot, String channel, String sender, String message) {
        this.bot = bot;
        this.channel = channel;
        this.sender = sender;
        this.message = message;
        new Thread(this).start();
    }
    
    public abstract void run();
}
