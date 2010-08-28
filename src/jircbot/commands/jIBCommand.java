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
    private PircBot _bot;
    private String _channel;
    private String _sender;
    private String _message;

    public synchronized PircBot getBot() {
		return _bot;
	}

	public synchronized void setBot(PircBot bot) {
		this._bot = bot;
	}

	public synchronized String getChannel() {
		return _channel;
	}

	public synchronized void setChannel(String channel) {
		this._channel = channel;
	}

	public synchronized String getSender() {
		return _sender;
	}

	public synchronized void setSender(String sender) {
		this._sender = sender;
	}

	public synchronized String getMessage() {
		return _message;
	}

	public synchronized void setMessage(String message) {
		this._message = message;
	}

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
    	setBot(bot);
    	setChannel(channel);
    	setSender(sender);
    	setMessage(message);
        new Thread(this).start();
    }
    
    public void run() {
    	runHandleMessage(getBot(), getChannel(), getSender(), getMessage());
    }
    
    public abstract void runHandleMessage(PircBot bot, String channel, String sender, String message);
}
