package org.hive13.jircbot.commands;

import org.hive13.jircbot.jIRCBot;
import org.hive13.jircbot.support.MtGoxTicker;
import org.hive13.jircbot.support.jIRCTools;

import com.google.gson.Gson;

public class jIBCBitcoin extends jIBCommand {
	private final String MT_GOX_URL = "https://mtgox.com/code/data/ticker.php"; 
		
        @Override
        public String getCommandName() {
                return "bitcoin";
        }

        @Override
        public String getHelp() {
                return "";
        }

        @Override
        protected void handleMessage(jIRCBot bot, String channel, String sender,
                        String message) {
        	Object content = null;
        	Gson gson = new Gson();
        	if(message.toLowerCase().equals(getCommandName())) {
        		content = jIRCTools.getUrlContent(MT_GOX_URL);
        		if(content != null) {
        			String json = content.toString();
        			// Massage json to fit our class a bit
        			json = json.substring(10, json.lastIndexOf('}'));
        			MtGoxTicker ticker = gson.fromJson(json, MtGoxTicker.class);
        			bot.sendMessage(channel, "Mt. Gox: Last=" + ticker.last + " High=" + ticker.high + " Low=" + ticker.low);
        		} else {
        			bot.sendMessage(channel, "Mt. Gox appears to be down right now");
        		}
        	}
	}
}
