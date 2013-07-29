package org.hive13.jircbotx.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hive13.jircbotx.JircBotX;
import org.hive13.jircbotx.JircBotX.eLogLevel;
import org.hive13.jircbotx.support.UrlTools;
import org.hive13.jircbotx.support.WebFile.eUserAgent;
import org.pircbotx.hooks.events.MessageEvent;

public class GitFeed extends RssReader {
   private AtomicBoolean abForceRefresh;
   private String gitUsername, gitPassword, gitOrg;
   private Date lastGitUpdate;
   
   public GitFeed(JircBotX bot, String commandName, String channelList,
         String gitUsername, String gitPassword, String gitOrg) throws MalformedURLException {
      super(bot, commandName, channelList, "[commandName]: [Title|c50] ([Link])", getGitHubFeedURL(gitUsername, gitPassword, gitOrg));
      this.gitUsername = gitUsername;
      this.gitPassword = gitPassword;
      this.gitOrg = gitOrg;
      lastGitUpdate = Calendar.getInstance().getTime();
      abForceRefresh.set(false);
   }
   
   /* (non-Javadoc)
    * @see org.hive13.jircbotx.listener.RssReader#loop()
    */
   @Override
   public void loop() {
      Date curDate = Calendar.getInstance().getTime();
      if((curDate.getTime() - lastGitUpdate.getTime()) > (12 * 60 * 60 * 1000) || updateFailed || abForceRefresh.get())
      {
         bot.log(getCommandName() + " attempting to retrieve new github feed URL. (updateFailed:" + updateFailed + ", forceRefresh:" + abForceRefresh.get() +")", eLogLevel.info);
         try {
            feedURL = new URL(getGitHubFeedURL(gitUsername, gitPassword, gitOrg));
            
            // Reset the 'update failed' flag.
            updateFailed = false;
            abForceRefresh.set(false);
            lastGitUpdate = Calendar.getInstance().getTime();
         } catch (MalformedURLException ex) {
            updateFailed = true;
            Logger.getLogger(GitFeed.class.getName()).log(
                  Level.WARNING, null, ex);
            bot.log(getCommandName() + " failed to retrieve new github url.", eLogLevel.warning);
         }
      }
      
      if(!updateFailed)
         super.loop();
   }

   
   
   /* (non-Javadoc)
    * @see org.hive13.jircbotx.ListenerThreadX#handleMessage(org.pircbotx.hooks.events.MessageEvent)
    */
   @Override
   protected void handleMessage(MessageEvent<JircBotX> event) throws Exception {
      super.handleMessage(event);
      String splitMessage[] = event.getMessage().toLowerCase().split(" ");
      
      if (splitMessage.length > 1 && splitMessage[0].equals("!" + getCommandName().toLowerCase())) {
         if (splitMessage[1].equals("refresh")) {
            abForceRefresh.set(true);
         }
      } 
   }

   /**
    * Call this to programmaticaly find the current 'hive13' feed URL.
    * 
    * This is a dirty, terrible, slow, ugly method for doing this, but I did not feel like
    * figuring out OAuth.  Try not to call it that often, and don't trust the result too much
    * unless you have too.
    * 
    * @param username
    * @param password
    * @param org
    * @return
    */
   public static String getGitHubFeedURL(String username, String password, String org)
   {
      String result = null;
      String findFeeds = findURLfromGitAPI("https://api.github.com", username, password, "feeds_url");
      if(!findFeeds.isEmpty())
      {
         String orgURLBase = findURLfromGitAPI(findFeeds, username, password, "current_user_organization_url");
         result = orgURLBase.replace("{org}", org);
      }
      
      return result;
   }
   private static String findURLfromGitAPI(String URL, String username, String password, String search)
   {
      String result = "";
      try {
         Object content = UrlTools.getUrlContent(URL, username, password, eUserAgent.real);
         if(content instanceof String)
         {
            String[] splitContent = ((String)content).split(",");
            for(String line : splitContent)
            {
               if(line.endsWith(","))
                  line = line.substring(0, line.length()-1);
               if(line.startsWith("{"))
                  line = line.substring(1, line.length());
               
               String[] splitLine = line.split(":", 2);
               if(splitLine.length == 2)
               {
                  if(splitLine[0].contains(search))
                  {
                     result = splitLine[1].replaceAll("\"", "");
                     break; // jump out of the for loop since we have our result.
                  }
               }
            }
         }
            
      } catch (MalformedURLException e) {
         Logger.getLogger(GitFeed.class.getName()).log(
               Level.SEVERE, null, e);
         e.printStackTrace();
      } catch (IOException e) {
         Logger.getLogger(GitFeed.class.getName()).log(
               Level.SEVERE, null, e);
         e.printStackTrace();
      }
      return result;
   }

}
