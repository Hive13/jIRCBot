package org.hive13.jircbot.commands;

import org.hive13.jircbot.jIRCBot;

public abstract class jIBCommand {
    
    public abstract String getCommandName();
    
    public void runCommand(jIRCBot bot, String channel, String sender, String message) {
        new Thread(new jIBCommandRunnable(bot, channel, sender, message)).start();
    }
    
    protected abstract void handleMessage(jIRCBot bot, String channel, 
            String sender, String message);
    
    protected class jIBCommandRunnable implements Runnable {
        private jIRCBot bot;
        private String channel;
        private String sender;
        private String message;
        
        public jIBCommandRunnable(jIRCBot bot, String channel, String sender,
                String message) {
            super();
            this.bot = bot;
            this.channel = channel;
            this.sender = sender;
            this.message = message;
        }
        
        @Override
        public void run() {
            handleMessage(bot, channel, sender, message);
        }
        
    }
}
