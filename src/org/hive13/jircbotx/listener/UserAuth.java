package org.hive13.jircbotx.listener;


import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hive13.jircbotx.JircBotX;
import org.hive13.jircbotx.HiveBot;
import org.hive13.jircbotx.ListenerAdapterX;
import org.hive13.jircbotx.support.BotProperties;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.UserListEvent;

public class UserAuth extends ListenerAdapterX {
   private final int BULK_OP_COUNT = 4;
   
   List<String> opList;

   public UserAuth()
   {
      super();
      opList = BotProperties.getInstance().getOpUserList();
   }
   
   @Override
   public String getCommandName() {
      return "op";
   }

   @Override
   public String getHelp() {
      return "Use '!op' to have the bot recheck your op status. Use !op recheck to have the bot recheck the entire channels op status. If you are an op" +
      		" already use '!op <username> <username>...' to op users that are not on the whitelist.";
   }

   @Override
   protected void handleMessage(MessageEvent<JircBotX> event) 
   {      
      String[] splitMessage = event.getMessage().split(" ");
      if(splitMessage.length > 0 && splitMessage[0].equalsIgnoreCase("!" + getCommandName()))
      {
         if(splitMessage.length == 1)
         {  // Support for "!op" to recheck a user's op status.

            // Force a refresh of the op whitelist.
            opList = BotProperties.refresh().getOpUserList(true); 
            
            // Check the calling user's op status against the op whitelist.
            AuthCheckUser(event.getBot(), event.getChannel(), event.getUser());
         }
         else if(splitMessage.length == 2 && splitMessage[1].equals("recheck"))
         {  // Support for "!op recheck" to re-check all user's in a channel's op status.
            
            // force a refresh of the op userlist.
            opList = BotProperties.refresh().getOpUserList(); 
            
            // Go through the user's in the channel and re-check their authorization status.
            AuthCheckUsersInList(event.getBot(), event.getChannel(), event.getChannel().getUsers());
         }
         else if(splitMessage.length > 1 && event.getUser().getChannelsOpIn().contains(event.getChannel()))
         {  // Support for "!op <user> <user>..."
            // *Note* this command option requires the caller already be an operator in this channel
            //        also, this option DOES NOT USE THE WHITELIST.
            
            int modeCount = 0;
            String modeMsg = " ";
            for(String curTargetUsr: splitMessage)
            {
               boolean alreadyOp = false;
               for(User opUser : event.getChannel().getOps())
               {
                  alreadyOp = opUser.getNick().equalsIgnoreCase(curTargetUsr);
                  if(alreadyOp)
                     break;
               }
               
               // User is not an op.. so add them to the mode list.
               if(!alreadyOp)
               {
                  modeMsg = "o" + modeMsg + " " + curTargetUsr;
                  ++modeCount;
                  
                  // Check if we are at the limit of bulk mode changes.
                  if((modeCount % BULK_OP_COUNT) == 0)
                  {
                     event.getBot().setMode(event.getChannel(), "+" + modeMsg);
                     modeMsg = "";
                  }
                  
                  // Send a message to the op'ed user letting them know who op'ed them.
                  event.getBot().sendMessage(curTargetUsr, event.getUser().getNick() +
                                             " granted you operator priviledges");
               }
            }
            // Did we have a non-even # of users to op?
            if((modeCount % BULK_OP_COUNT) != 0)
            {
               event.getBot().setMode(event.getChannel(), "+" + modeMsg);
            }
         } // end "!op <user> <user>..."
         else
         {
            event.getUser().sendMessage(getHelp());
         }
      }
   }
   
   
   /* (non-Javadoc)
    * @see org.pircbotx.hooks.ListenerAdapter#onUserList(org.pircbotx.hooks.events.UserListEvent)
    */
   @Override
   public void onUserList(UserListEvent<JircBotX> event) throws Exception {
      super.onUserList(event);
      AuthCheckUsersInList(event.getBot(), event.getChannel(), event.getUsers());  
   }

   @Override
   public void onJoin(JoinEvent<JircBotX> event) throws Exception {
      super.onJoin(event);
      if(event.getUser().equals(event.getBot().getUserBot()))
      {
         // TODO: Find a better way for the bot to op itself.
         event.getBot().sendMessage("chanserv", "op " + event.getChannel().getName());
      }
      else
      {
         AuthCheckUser(event.getBot(), event.getChannel(), event.getUser());
      }
   }

   private void AuthCheckUsersInList(JircBotX bot, Channel authChannel, Set<User> userList)
   {
      Iterator<User> itUsers = userList.iterator();
      
      while(itUsers.hasNext())
      {
         AuthCheckUser(bot, authChannel, itUsers.next());
      }
   }
   private void AuthCheckUser(JircBotX bot, Channel authChannel, User authUser)
   {
      try {
         String regName = HiveBot.getRegisteredName(authUser.getNick()).toLowerCase();
         if(regName != null && opList.contains(regName) && !authUser.getChannelsOpIn().contains(authChannel))
         {
            bot.op(authChannel, authUser);
         }
      } catch (InterruptedException e) {
         // TODO: add logger support here.
         e.printStackTrace();
      }
   }
}
