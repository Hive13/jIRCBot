package jircbot.commands;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.jibble.pircbot.PircBot;

/*
 * This is an interesting thought that I just had.  I do not think any of our
 * commands NEED to by run synchronously, why not just have a "CommandLauncher"
 * class or something similar so that longer running commands do not lock
 * up the processes but are instead launched off in their own threads.
 */
public class jIBCTTell extends jIBCommand {

	private final int MAX_MSG_QUEUE = 25;
	
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
	private HashMap<String, ArrayList<storedMsg>> msgMap = new HashMap<String, ArrayList<storedMsg>>();
	
	@Override
	public String getCommandName() {
		return "tell";
	}

	@Override
	public void handleMessage(PircBot bot, String channel, String sender,
			String message) {
		String[] splitMsg = message.split(" ", 3);
		if(splitMsg[0].equals(getCommandName())) {
			if(splitMsg.length == 3) {
				// Add the tell message
				// - [command] [target user] [message]
				ArrayList<storedMsg> msglist;
				if((msglist = msgMap.get(splitMsg[1])) == null) {
					msglist = new ArrayList<storedMsg>();
					msgMap.put(splitMsg[1], msglist);
				}
				if(msglist.size() <= MAX_MSG_QUEUE) {
					msglist.add(new storedMsg(new Date(), splitMsg[2], sender));
				
					// Tell the sender that the 'tell' was added for 'target user'
					bot.sendMessage(sender, "I will tell " + splitMsg[1] + " the next time he talks in channel.");
				} else {
					bot.sendMessage(sender, splitMsg[1] + " already has the max (" +
							MAX_MSG_QUEUE + ") number of messages saved for him.");
				}
			} else {
				bot.sendMessage(sender, "The correct syntax is: !tell username Remember the milk");
			}
		}
		// Now we need to check to see if sender has any waiting messages.
		ArrayList<storedMsg> msgList;
		if((msgList = msgMap.remove(sender)) != null) {
			Iterator<storedMsg> i = msgList.iterator();
			while(i.hasNext()) {
				storedMsg curMsg = i.next();
				bot.sendMessage(sender, curMsg.sender 
						+ " sent the following to you on " 
						+ curMsg.storedDate.toString()
						+ " : " + curMsg.message);
			}
		}
		
		
	}

}
