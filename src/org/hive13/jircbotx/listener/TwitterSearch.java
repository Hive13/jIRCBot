package org.hive13.jircbotx.listener;

import java.util.List;

import org.hive13.jircbotx.JircBotX;
import org.hive13.jircbotx.ListenerThreadX;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TwitterSearch extends ListenerThreadX {

   public TwitterSearch(JircBotX bot) {
      super(bot);
   }

   @Override
   public void loop() {
      // TODO Auto-generated method stub
      Twitter twitter = new TwitterFactory().getInstance();
      try {
         Query query = new Query("");
         QueryResult result;
         do {
            result = twitter.search(query);
            List<Status> tweets = result.getTweets();
            for(Status tweet : tweets) {
               // do stuff for each tweet.
            }
            
         } while ((query = result.nextQuery()) != null);
      } catch (TwitterException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   @Override
   public String getCommandName() {
      // TODO Auto-generated method stub
      return "Tweet";
   }

   @Override
   public String getHelp() {
      // TODO Auto-generated method stub
      return null;
   }

}
