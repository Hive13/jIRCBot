package org.hive13.jircbotx.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hive13.jircbotx.JircBotX;
import org.hive13.jircbotx.JircBotX.eMsgTypes;
import org.hive13.jircbotx.ListenerThreadX;
import org.hive13.jircbotx.JircBotX.eLogLevel;
import org.hive13.jircbotx.support.BotDataCache;
import org.hive13.jircbotx.support.BotProperties;
import org.hive13.jircbotx.support.UrlTools;
import org.pircbotx.User;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterSearch extends ListenerThreadX {
   private long lastSentID = 0;
   
   private String commandName = "";
   private String searchString = "";
   
   private String omitList[] = null;
   
   public TwitterSearch(JircBotX bot, String commandName, String channelList, String searchString, String omitList) {
      this(bot, commandName, channelList, searchString, omitList, ListenerThreadX.LOOP_DELAY_DEFAULT);
   }

   public TwitterSearch(JircBotX bot, String commandName, String channelList, String searchString, String omitList, long loopDelay) {
      super(bot, channelList, loopDelay);
      this.commandName = commandName;
      this.searchString = searchString;
      lastSentID = BotDataCache.getInstance().getLatestTweetID(getCommandName());
      
      this.omitList = omitList.split(",");
      if(this.omitList != null)
      {
         for(int i = 0; i<this.omitList.length; ++i)
         {
            this.searchString += " -" + this.omitList[i];
         }
      }
   }
   
   @Override
   public void loop() {
      AccessToken accessToken = new AccessToken(BotProperties.getInstance().getTwitterAccessToken(),BotProperties.getInstance().getTwitterAccessSecret());
      Twitter twitter = new TwitterFactory().getInstance();
      twitter.setOAuthConsumer(BotProperties.getInstance().getTwitterKey(), BotProperties.getInstance().getTwitterSecret());
      twitter.setOAuthAccessToken(accessToken);
      
      Query query = new Query(searchString);
      try {
         //AccessToken accessToken = null;
         // Prepare the query to Search for the specific string.
         // and retrieve all the search results.
         List<Status> tweetList = null;
         QueryResult result;
         do {
            result = twitter.search(query);
            if(tweetList == null)
               tweetList = result.getTweets();
            else
               tweetList.addAll(result.getTweets());
         } while ((query = result.nextQuery()) != null);

         // We have all the results, now lets validate that we DO
         // in fact have some results, then sort the list.
         if(tweetList != null && !tweetList.isEmpty())
         {
            Collections.sort(tweetList, new StatusComparator());
            
            Status lastTweet = tweetList.get(0);
            if(lastTweet.getId() > lastSentID)
            {
               sendMessage(getCommandName() + " @" + lastTweet.getUser().getScreenName() 
                     + ": " + lastTweet.getText() 
                     + " [" + getTweetURL(lastTweet) + "]", eMsgTypes.publicMsg);
               lastSentID = lastTweet.getId();
               BotDataCache.getInstance().setLatestTweetID(getCommandName(), lastSentID);
            }
         }
         else
         {
            if(tweetList == null)
               bot.log(getCommandName() + " resulted in 'null' tweets?", eLogLevel.error);
            else
               bot.log(getCommandName() + " resulted in empty list of tweets.", eLogLevel.error);
         }
      } catch (TwitterException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   @Override
   public String getCommandName() {
      return commandName;
   }

   @Override
   public String getHelp() {
      // TODO Auto-generated method stub
      return "The following are valid uses of this command: !" + getCommandName() + " help ;" +
              " !" + getCommandName() + " start ; !" + getCommandName() + " stop";
   }
   
   public static String getTweetURL(Status status)
   {
      return UrlTools.generateShortURL("https://twitter.com/" + status.getUser().getId() + "/status/" + status.getId());
   }
   
   // TODO: Implement functionality to re-generate the access token
   @SuppressWarnings("unused")
   private AccessToken generateNewAccessToken(User commUser)
   {
      AccessToken resultToken = null;
      try {
         Twitter twitter = new TwitterFactory().getInstance();
         twitter.setOAuthConsumer("qCM8KltcE7VMDeHjpo3XtA", "EBSkKpnjOWngyZj4ekQNDQD6brmMDDgsLvMImIM1RE");
         try {
            // get request token.
            // this will throw IllegalStateException if access token is already available
            RequestToken requestToken = twitter.getOAuthRequestToken();
            commUser.sendMessage("Got request token.");
            commUser.sendMessage("Request token: " + requestToken.getToken());
            commUser.sendMessage("Request token secret: " + requestToken.getTokenSecret());

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (null == resultToken) {
               commUser.sendMessage("Open the following URL and grant aviaccess to your account:");
               commUser.sendMessage(requestToken.getAuthorizationURL());
               commUser.sendMessage("Enter the PIN(if available) and hit enter after you granted access.[PIN]:");
               // TODO: Add code here to wait for a private message response.
               String pin = br.readLine();
               try {
                  if (pin.length() > 0) {
                     resultToken = twitter.getOAuthAccessToken(requestToken, pin);
                  } else {
                     resultToken = twitter.getOAuthAccessToken(requestToken);
                  }
               } catch (TwitterException te) {
                  if (401 == te.getStatusCode()) {
                     System.out.println("Unable to get the access token.");
                  } else {
                     te.printStackTrace();
                  }
               }
            }
            commUser.sendMessage("Got access token.");
            commUser.sendMessage("Access token: " + resultToken.getToken());
            commUser.sendMessage("Access token secret: " + resultToken.getTokenSecret());
         } catch (IllegalStateException ie) {
            // access token is already available, or consumer key/secret is not set.
            if (!twitter.getAuthorization().isEnabled()) {
               commUser.sendMessage("OAuth consumer key/secret is not set.");
            }
         }
         // Do a quick query to verify the result;
         Query query = new Query("hive13");
         QueryResult result;
         do {
             result = twitter.search(query);
             List<Status> tweets = result.getTweets();
             for (Status tweet : tweets) {
                commUser.sendMessage("@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
             }
         } while ((query = result.nextQuery()) != null);
         System.exit(0);
      } catch (TwitterException te) {
         te.printStackTrace();
         commUser.sendMessage("Failed to get timeline: " + te.getMessage());
      } catch (IOException ioe) {
         ioe.printStackTrace();
         commUser.sendMessage("Failed to read the system input.");
      }
      return resultToken;
   }
   
   class StatusComparator implements Comparator<Status> {
      public int compare(Status o1, Status o2) {
         int statusDateCompare = 0;
         if(o1.getCreatedAt() != null && o2.getCreatedAt() != null)
            statusDateCompare = o2.getCreatedAt().compareTo(o1.getCreatedAt());
         return statusDateCompare;
      }
   }
}
