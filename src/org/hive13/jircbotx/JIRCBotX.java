package org.hive13.jircbotx;

import org.pircbotx.PircBotX;

public class JIRCBotX {

   /**
    * @param args
    */
   public static void main(String[] args) throws Exception{
      PircBotX bot = new PircBotX();
      bot.setName("Hive13Bot_InTraining");
      bot.connect("irc.freenode.net");
      bot.joinChannel("#hive13");
   }

}
