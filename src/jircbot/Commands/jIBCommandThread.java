/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jircbot.Commands;

import org.jibble.pircbot.PircBot;

/**
 *
 * @author vincenpt
 */
public abstract class jIBCommandThread implements jIBCommand, Runnable {

    protected PircBot bot = null;
    protected String channel = "";
    protected String sender = "";
    protected boolean isRunning = true;

    public jIBCommandThread(PircBot bot, String channel, String sender) {
        this(bot, channel, sender, true);
    }

    public jIBCommandThread(PircBot bot, String channel, String sender,
            boolean isRunning) {
        this.bot = bot;
        this.channel = channel;
        this.sender = sender;
        this.isRunning = isRunning;
    }

    public void handleMessage(PircBot bot, String channel, String sender, String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void stop() {
        isRunning = false;
    }
}
