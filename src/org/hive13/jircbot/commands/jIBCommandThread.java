package org.hive13.jircbot.commands;

import java.util.concurrent.atomic.AtomicBoolean;

import org.hive13.jircbot.jIRCBot;
import org.hive13.jircbot.jIRCBot.eLogLevel;

/**
 * This abstract class framework is used for implementing asynchronous commands
 * that run in the background and can react to externally generated events.
 * @author vincenpt
 */
public abstract class jIBCommandThread extends jIBCommand {
    protected jIRCBot   bot         = null;
    protected String    commandName = "";
    protected String    channel     = "";
    protected long      loopDelay   = 30000;    // 30 seconds
    
    protected commandThreadRunnable commandThreadChild  = null;
    
    public jIBCommandThread(jIRCBot bot, String commandName, String channel) {
        this(bot, commandName, channel, 30000);
    }
    
    public jIBCommandThread(jIRCBot bot, String commandName, String channel,
            long loopDelay) {
        super();
        this.bot = bot;
        this.commandName = commandName;
        this.channel = channel;
        this.loopDelay = loopDelay;
        startCommandThread();
    }

    /**
     * This method is run every "delay" milliseconds.
     * WARNING! This method WILL be called by asynchronous
     *          threads.  Everything this function touches
     *          MUST be thread safe.
     */
    public abstract void loop();
    
    @Override
    public void handleMessage(jIRCBot bot, String channel, String sender,
            String message) {
        if(channel.equals(this.channel)) {
            if(commandThreadChild != null && commandThreadChild.getIsRunning()) {
                stopCommandThread();
            } else {
                startCommandThread();
            }
        } else {
           this.bot.log("commandThread - handleMessage called for channel: " + channel
                   + " when ct is actually in " + this.channel, eLogLevel.warning);
        }
    }
    
    /**
     * This is a wrapper for the bot.sendMessage command.  It automatically sends any
     * messages to the correct channel.  It also acts as a way to prevent the command
     * from sending messages before it is connected to the server.
     * 
     * @param message   A text message to send to the channel the command is based in.
     */
    public void sendMessage(String message) {
        if(bot.isConnected()) {
            bot.sendMessage(channel, message);
        } else {
            bot.log("Bot not connected, tried to send: " + message, eLogLevel.warning);
        }
    }
    
    
    public void startCommandThread() {
        if(commandThreadChild == null) {
            commandThreadChild = new commandThreadRunnable(loopDelay);
        }
        if(!commandThreadChild.getIsRunning()) {
            new Thread(commandThreadChild).start();
        }
    }
    
    public void stopCommandThread() {
        if(commandThreadChild != null && commandThreadChild.getIsRunning()) {
            commandThreadChild.stop();
        }
    }
    @Override
    public String getCommandName() {
        return commandName + channel;
    }

    /**
     * Gets the channel the commandThread is running in.
     * @return
     */
    public String getChannel() {
        return channel;
    }
    
    /**
     * Returns a simple command name, minus the channel.
     * @return
     */
    public String getSimpleCommandName() {
        return commandName;
    }
    
    
    protected class commandThreadRunnable implements Runnable {
        private AtomicBoolean   isRunning;
        private long            delay;
        
        public commandThreadRunnable(long delay) {
            this.isRunning = new AtomicBoolean(false);
            this.delay = delay;
        }
        
        @Override
        public void run() {
            setIsRunning(true);
            bot.log("Started " + getCommandName(), eLogLevel.info);
            while(getIsRunning()) {
                loop();
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    bot.log("commandThread " + getCommandName()
                              + " interrupted.\n", eLogLevel.error);
                    e.printStackTrace();
                }
                
            }
            setIsRunning(false);
            bot.log("Stopped " + getCommandName(), eLogLevel.info);
            
        }
        
        public void stop() {
            bot.log("Stopping " + getCommandName(), eLogLevel.info);
            setIsRunning(false);
        }
        
        public boolean getIsRunning() {
            return isRunning.get();
        }
        
        private void setIsRunning(boolean isRunning) {
            this.isRunning.set(isRunning);
        }
        
    }

}
