package org.hive13.jircbotx;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hive13.jircbotx.JircBotX.eMsgTypes;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;

public abstract class ListenerThreadX extends ListenerAdapterX {
   protected static final int LOOP_DELAY_DEFAULT = 30000; // 30 seconds
   
   protected JircBotX bot = null;
   protected long loopDelay = LOOP_DELAY_DEFAULT;
   
   protected ListenerThreadRunnable listenerThreadChild = null;

   public ListenerThreadX(JircBotX bot)
   {
      this(bot, "", LOOP_DELAY_DEFAULT);
   }
   public ListenerThreadX(JircBotX bot, String channelList)
   {
      this(bot, channelList, LOOP_DELAY_DEFAULT);
   }
   public ListenerThreadX(JircBotX bot, String channelList, long loopDelay)
   {
      super(channelList);
      this.bot = bot;
      this.loopDelay = loopDelay;
   }

   
   /* (non-Javadoc)
    * @see org.pircbotx.hooks.ListenerAdapter#onJoin(org.pircbotx.hooks.events.JoinEvent)
    */
   @Override
   public void onJoin(JoinEvent<JircBotX> event) throws Exception {
      super.onJoin(event);
      if(shouldListenToChannel(event.getChannel().getName()))
      {
         this.startCommandThread(true);
      }
   }
   
   /**
    * This method is run every "loopDelay" milliseconds. WARNING! This method
    * WILL be called by asynchronous threads. Everything this function touches
    * MUST be thread safe.
    */
   public abstract void loop();
   
   /**
    * handleMessage in this class is used to start // stop the thread which performs
    * its task every 'loopDelay' ms via the 'loop()' function.
    * 
    * Program flow at this point is:
    * 1. PircBot calls "onMessage" in ListenerAdapterX
    * 2. ListenerAdapterX checks if we should be listening to this channel and does a quick check to see if we should return the 'help' for this command.
    * 3. ListenerAdapterX then calls this 'handleMessage' which is described above.
    */
   @Override
   protected void handleMessage(MessageEvent<JircBotX> event) throws Exception {
      String splitMessage[] = event.getMessage().toLowerCase().split(" ");
      
      if (splitMessage.length > 1 && splitMessage[0].equals("!" + getCommandName().toLowerCase())) {
         if (splitMessage[1].equals("stop")) {
            stopCommandThread();
         } else if (splitMessage[1].equals("start")){
            startCommandThread();
         }
      } 
   }

   /**
    * This is a wrapper for the bot.sendMessage command. It automatically sends
    * any messages to the correct channel. It also acts as a way to prevent the
    * command from sending messages before it is connected to the server.
    * 
    * @param message
    *            A text message to send to the channel the command is based in.
    */
   public void sendMessage(String message) {
      this.sendMessage(message, eMsgTypes.LogFreeMsg);
   }
   
   /**
    * This is a wrapper for the bot.sendMessage command. It automatically sends
    * any messages to the correct channel. It also acts as a way to prevent the
    * command from sending messages before it is connected to the server.
    * 
    * @param message
    *            A text message to send to the channel the command is based in.
    */
   public void sendMessage(String message, eMsgTypes msgType) {
      if (botIsInChannels()) {
         Iterator<String> chanIt = ListenerChannelList.iterator();
         while(chanIt.hasNext())
         {
            String chan = chanIt.next();
            bot.sendMessage(chan, message);
            bot.logMessage(chan, message, eMsgTypes.publicMsg);
         }
      } else {
         Logger.getLogger(ListenerThreadX.class.getName()).log(Level.INFO, getCommandName() + " tried to send [" + message + "] to its channels but the bot reported it was not connected to those channels.");
         // Do nothing...  old reconnect & warn code below
         /*
         if(bot.abConnecting.get()) {
            bot.log("Bot currently connecting, tried to send: " + message,
                  eLogLevel.warning);
         } else {
            // The bot is both not connected currently and is not
            // trying to connect.  Tell it to try to connect.
            bot.connectBot();
            bot.log("Bot not connected, tried to send: " + message,
                  eLogLevel.warning);
         }//*/
      }
   }
   
   
   public void startCommandThread(boolean beQuiet)
   {
      if (listenerThreadChild == null) {
         listenerThreadChild = new ListenerThreadRunnable(loopDelay);
      }
      if (!listenerThreadChild.getIsRunning()) {
         if(!beQuiet) sendMessage("Starting the " + getCommandName() + " thread.", eMsgTypes.publicMsg);
         new Thread(listenerThreadChild).start();
      } else {
         if(!beQuiet) sendMessage("The " + getCommandName() + " thread is already running.", eMsgTypes.publicMsg);
      }
      
   }
   
   /**
    * This method will safely start an instance of the command thread. If an
    * instance of the commandThread exists but is not running, it restarts that
    * instance.
    */
   public void startCommandThread() {
      startCommandThread(false);
   }

   /**
    * This method is more of a 'pause' commandThread. It stops the running java
    * Thread but does not delete the instance of the commandThread.
    */
   public void stopCommandThread() {
      if (listenerThreadChild != null && listenerThreadChild.getIsRunning()) {
         sendMessage("Stopping the " + getCommandName() + " thread.", eMsgTypes.publicMsg);
         listenerThreadChild.stop();
      } else {
         sendMessage("The " + getCommandName() + " thread is not running.", eMsgTypes.publicMsg);
      }
   }
   
   private boolean botIsInChannels()
   {
      boolean result = bot.isConnected();
      
      Iterator<String> itChanJoinList = ListenerChannelList.iterator();
      
      while(result && itChanJoinList.hasNext())
      {
         boolean foundChan = false;
         String channel = itChanJoinList.next();
         Iterator<String> itChanInList = bot.getChannelsNames().iterator();
         while(!foundChan && itChanInList.hasNext())
         {
            if(itChanInList.next().equalsIgnoreCase(channel))
               foundChan = true;
         }
         result &= foundChan;
      }
      
      return result;
   }
   
   /**
    * This is the fancy part.  I broke my brain figuring this out and had
    * to re-write this class several times.
    * 
    * Here is the order of operations.
    * 
    * 1. Someone calls "runCommand" on a jIBCmdThrd object
    * 2. This triggers the jIBCommand.runCommand()
    * 3. jIBCommand.runCommand() launches a jIBCommandRunnable thread, we are now asynchronous from the main thread.
    * 4. This jIBCommandRunnable calls the jIBCmdThrd.handleMessage().
    * 5. This checks to see if this is a "start | stop" command.  If it is a start...
    * 5.1. jIBCmdThrd calls jIBCmdThrd.startCommandThread() which starts a commandThreadRunnable object.
    * 5.2. The initial jIBCommandRunnable now exits leaving the commandThreadRunnable running in the background in the jIBCmdThrd object.
    * 5.3. The commandThreadRunnable is set up in a semi-infinite loop that calls "jIBCmdThrd.loop()" every DELAY seconds.
    * 6. If it was instead a "stop" command...
    * 6.1. jIBCmdThrd calls jIBCmdThrd.stopCommandThread() which takes its instance of the commandThreadRunnable
    *      and sends it the "stop" command.
    * 6.2. The initial jIBCommandRunnable now exits with the commandThreadRunnable set up to exit.
    * 6.3. The commandThreadRunnable will eventually finish its "sleep" cycle and check to see if it should exit,
    *      the "stop" boolean is set, so it exits cleanly.
    *      
    * 
    * @author vincentp
    *
    */
   protected class ListenerThreadRunnable implements Runnable {
      private AtomicBoolean isRunning;
      private long delay;

      public ListenerThreadRunnable(long delay) {
         this.isRunning = new AtomicBoolean(false);
         this.delay = delay;
      }

      public void run() {
         setIsRunning(true);
         //bot.log("Started " + getCommandName(), eLogLevel.info);
         while (getIsRunning()) {
            loop();
            try {
               Thread.sleep(delay);
            } catch (InterruptedException e) {
               //bot.log("commandThread " + getCommandName()
               //      + " interrupted.\n", eLogLevel.error);
               e.printStackTrace();
            }
         }
         setIsRunning(false);
         //bot.log("Stopped " + getCommandName(), eLogLevel.info);

      }

      public void stop() {
         //bot.log("Stopping " + getCommandName(), eLogLevel.info);
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
