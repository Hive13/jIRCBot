package org.hive13.jircbotx.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

public class Tell extends ListenerAdapter<PircBotX> {
   private class storedMsg {
      Date storedDate;
      String message;
      String sender;

      public storedMsg(Date storedDate, String message, String sender) {
         super();
         this.storedDate = storedDate;
         this.message = message;
         this.sender = sender;
      }
   }
   
   private final int MAX_MSG_QUEUE = 10;
   private HashMap<String, ArrayList<storedMsg>> msgMap = new HashMap<String, ArrayList<storedMsg>>();

   private String helpMessage = "This command stores a message for a user.  The next time" +
         " that user sends a message in the chat room I will send" +
         " them the messages stored for them. Ex. !Tell" +
         " jimboJones Remember to bring that thing with you.";
   
   public void onMessage(MessageEvent<PircBotX> event) throws Exception {
      String message = event.getMessage();
      String sender = event.getUser().getNick();
      
      String[] splitMsg = message.split(" ", 3);
      if (splitMsg[0].toLowerCase().equals("!tell")) {
         if (splitMsg.length == 3) {
            // Add the tell message
            // - [command] [target user] [message]
            ArrayList<storedMsg> msglist;
            if ((msglist = msgMap.get(splitMsg[1])) == null) {
               msglist = new ArrayList<storedMsg>();
               msgMap.put(splitMsg[1].toLowerCase(), msglist);
            }
            if (msglist.size() <= MAX_MSG_QUEUE) {
               msglist.add(new storedMsg(new Date(), splitMsg[2], sender));

               // Tell the sender that the 'tell' was added for 'target
               // user'
               event.getUser().sendMessage("I will tell " + splitMsg[1]
                     + " '" + splitMsg[2] + "'" 
                     + " the next time they talk in channel.");
            } else {
               event.getUser().sendMessage(splitMsg[1]
                     + " already has the max (" + MAX_MSG_QUEUE
                     + ") number of messages saved for them.");
            }
         } else if(splitMsg.length == 2 && (splitMsg[1].equalsIgnoreCase("help") || splitMsg[1].equalsIgnoreCase("h"))){
            // We handle help ourselves, see GitHub Issue #6
            event.getUser().sendMessage(helpMessage);
         } else {
            event.getUser().sendMessage("The correct syntax is: !tell username Remember the milk");
         }
      }
      
      // Now we need to check to see if sender has any waiting messages.
      ArrayList<storedMsg> msgList;
      if ((msgList = msgMap.remove(sender.toLowerCase())) != null) {
         Iterator<storedMsg> i = msgList.iterator();
         while (i.hasNext()) {
            storedMsg curMsg = i.next();
            event.getUser().sendMessage( curMsg.sender + " sent the following to you on "
                                          + curMsg.storedDate.toString() + " : "
                                          + curMsg.message);
         }
      }
   }
}
