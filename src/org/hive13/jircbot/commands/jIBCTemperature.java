/**
 * 
 */
package org.hive13.jircbot.commands;

import org.hive13.jircbot.jIRCBot;

/**
 * @author vincentp
 *
 */
public class jIBCTemperature extends jIBCommand {

   /* (non-Javadoc)
    * @see org.hive13.jircbot.commands.jIBCommand#getCommandName()
    */
   @Override
   public String getCommandName() {
      return "Temp";
   }

   /* (non-Javadoc)
    * @see org.hive13.jircbot.commands.jIBCommand#getHelp()
    */
   @Override
   public String getHelp() {
      return "Returns the temperature inside Hive13 and the local temperature. Ex. !" + getCommandName();
   }

   /* (non-Javadoc)
    * @see org.hive13.jircbot.commands.jIBCommand#handleMessage(org.hive13.jircbot.jIRCBot, java.lang.String, java.lang.String, java.lang.String)
    */
   @Override
   protected void handleMessage(jIRCBot bot, String channel, String sender,
         String message) {
      // Retrieve the current Hive13 temperature
      // Retrieve the current local temperature
      // Send a message to the channel w/ the current Hive13 & Local temperature.

   }

}
