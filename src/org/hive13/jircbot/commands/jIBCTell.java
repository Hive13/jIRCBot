package org.hive13.jircbot.commands;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.hive13.jircbot.jIRCBot;

public class jIBCTell extends jIBCommand {

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

	private final int MAX_MSG_QUEUE = 25;
	private HashMap<String, ArrayList<storedMsg>> msgMap = new HashMap<String, ArrayList<storedMsg>>();

	@Override
	public String getCommandName() {
		return "tell";
	}

	public String getHelp() {
		return "This command stores a message for a user.  The next time" +
				" that user sends a message in the chat room I will send" +
				" them the messages stored for them. Ex. !" + getCommandName() +
				" jimboJones Remember to bring that thing with you.";
	}
	
	@Override
	public void handleMessage(jIRCBot bot, String channel, String sender,
			String message) {
		String[] splitMsg = message.split(" ", 3);
		if (splitMsg[0].equals(getCommandName())) {
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
					bot.sendMessage(sender, "I will tell " + splitMsg[1]
							+ "'" + splitMsg[2] + "'" 
							+ " the next time they talk in channel.");
				} else {
					bot.sendMessage(sender, splitMsg[1]
							+ " already has the max (" + MAX_MSG_QUEUE
							+ ") number of messages saved for them.");
				}
			} else {
				bot.sendMessage(sender,
						"The correct syntax is: !tell username Remember the milk");
			}
		}
		// Now we need to check to see if sender has any waiting messages.
		ArrayList<storedMsg> msgList;
		if ((msgList = msgMap.remove(sender.toLowerCase())) != null) {
			Iterator<storedMsg> i = msgList.iterator();
			while (i.hasNext()) {
				storedMsg curMsg = i.next();
				bot.sendMessage(sender,
						curMsg.sender + " sent the following to you on "
								+ curMsg.storedDate.toString() + " : "
								+ curMsg.message);
			}
		}

	}

}
