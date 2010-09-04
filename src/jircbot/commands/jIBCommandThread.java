/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jircbot.commands;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jibble.pircbot.PircBot;

/**
 * This abstract class framework is used for implementing asynchronous commands
 * that run in the background and can react to externally generated events.
 * @author vincenpt
 */
public abstract class jIBCommandThread extends jIBCommand {

    protected PircBot bot = null;
    protected String channel = "";
    
    protected long loopDelay = 1000;
    protected String commandName = "";

    protected Thread childThread = null;
    protected commandThreadChild childThreadRunnable = null;
    /**
     * Constructor method.
     * @param bot           The bot to which this command belongs.
     * @param commandName   The name of this command.  This should be unique to its channel.
     * @param channel       The channel that this command will run on.
     */
    public jIBCommandThread(PircBot bot, String commandName, String channel) {
        this(bot, commandName, channel, 1000);
    }

    /**
     * Constructor method.
     * @param bot           The bot to which this command belongs.
     * @param commandName   The name of this command.  This should be unique to its channel.
     * @param channel       The channel that this command will run on.
     * @param delay         The delay between times that the "loop()" method is called.
     */
    public jIBCommandThread(PircBot bot, String commandName, String channel, long delay) {
        this(bot, commandName, channel, delay, true);
    }

    /**
     * Constructor method.
     * @param bot           The bot to which this command belongs.
     * @param commandName   The name of this command.  This should be unique to its channel.
     * @param channel       The channel that this command will run on.
     * @param delay         The delay between times that the "loop()" method is called.
     * @param isRunning     Does this command start out running?
     */
    public jIBCommandThread(PircBot bot, String commandName, String channel,
            long delay, boolean isRunning) {
        this.bot = bot;
        this.commandName = commandName;
        this.channel = channel;
        this.loopDelay = delay;
        if(isRunning)
        	startCommandThread();
    }
    
    /**
     * This is a wrapper for the bot.sendMessage command.  It automatically sends any
     * messages to the correct channel.  It also acts as a way to prevent the command
     * from sending messages before it is connected to the server.
     * 
     * @param message   A text message to send to the channel the command is based in.
     */
    public void sendMessage(String message) {
        if (bot.isConnected()) // Warning! This does not check to see if we are in channel.
        {
            bot.sendMessage(channel, message);
        } else {
            bot.log("WARNING! Bot not connected, tried to send: " + message);
        }
    }

    /**
     * This method is run every "delay" milliseconds.
     * WARNING! This method WILL be called by asynchronous
     * 			threads.  Everything this function touches
     * 			MUST be thread safe.
     */
    protected abstract void loop();


    /**
     * Gets the channel the commandThread is running in.
     * @return
     */
    public String getChannel() {
        return channel;
    }

    /** 
     * Gets this commandThreads name.
     */
    public String getCommandName() {
        //TODO: commandThread::getCommandName() - Figure out a method for unique naming commandThreads.
        return commandName + getChannel();
    }
    
    /**
     * Returns a simple command name, minus the channel.
     * @return
     */
    public String getSimpleCommandName() {
    	return commandName;
    }
    
    public void startCommandThread() {
    	if(childThread == null || !childThread.isAlive())
    		if(childThreadRunnable == null)
    			childThreadRunnable = new commandThreadChild(true, loopDelay);
    		childThread = new Thread(childThreadRunnable);
    }
    
    public void stopCommandThread() {
    	if(childThread != null && childThread.isAlive()
		   && childThreadRunnable != null && childThreadRunnable.getIsRunning()) {
    		childThreadRunnable.stop();
    	}
    }
    
    private class commandThreadChild implements Runnable {
    	private AtomicBoolean isRunning;
    	private long		  delay;
    	
    	public commandThreadChild(boolean isRunning, long delay) {
    		this.isRunning = new AtomicBoolean(isRunning);
    		this.delay = delay;
    	}
    	
		public void run() {
			setIsRunning(true);
			bot.log("Started " + getCommandName());
			while(getIsRunning()) {
				loop();
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					bot.log("Warning! - commandThread " + getCommandName()
							+ " interupted.\n");
					e.printStackTrace();
				}
			}
			// In case there was some error that has caused us to jump out there.
			setIsRunning(false); 
			bot.log("Stopped " + getCommandName());
		}
		
		public void stop() {
			bot.log("Stopping " + getCommandName());
			setIsRunning(false);
		}
		
		public boolean getIsRunning() {
			return isRunning.get();
		}
		
		public void setIsRunning(boolean isRunning) {
			this.isRunning.set(isRunning);
		}
	}
}
