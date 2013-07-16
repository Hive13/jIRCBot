package org.hive13.jircbotx.listener;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.Random;

public class Magic8Ball extends ListenerAdapter<PircBotX> {
   private static Random r = new Random();
   private String strResponses[] = { "It is certain",
           "It is decidedly so", "Without a doubt",
           "Yes - definitely", "You may rely on it",
           "As I see it, yes", "Most likely",
           "Outlook good", "Signs point to yes",
           "Yes", "Reply hazy, try again",
           "Ask again later", "Better not tell you now",
           "Cannot predict now", "Concentrate and ask again",
           "Don't count on it", "My reply is no",
           "My sources say no", "Outlook not so good",
           "Very doubtful"};
   

   public void onMessage(MessageEvent<PircBotX> event) throws Exception {
      String message = event.getMessage().toLowerCase();
      if((message.startsWith(event.getBot().getNick().toLowerCase()) && message.endsWith("?")) ||
            message.startsWith("!eightball") ||
            message.startsWith("!magiceightball") ||
            message.startsWith("!m8b") ||
            message.startsWith("!magic8ball"))
      {
         event.respond(strResponses[r.nextInt(20)]);
      }
   }
}
