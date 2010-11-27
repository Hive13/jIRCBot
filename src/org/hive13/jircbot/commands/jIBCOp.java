package org.hive13.jircbot.commands;

import org.hive13.jircbot.jIRCBot;
import org.hive13.jircbot.jIRCBot.eLogLevel;
import org.hive13.jircbot.support.jIRCUser;
import org.hive13.jircbot.support.jIRCUser.eAuthLevels;

public class jIBCOp extends jIBCommand {

    private final int BULK_OP_COUNT = 4;
    
	public String getHelp() {
		return "This command has two uses.  If you are not an operator" +
				" but think you are on the approved list then the following" +
				" command will recheck your authorization: !op . If you are" +
				" a channel operator then this command can op other users."
				+ " Ex. !op Username";
	}

	@Override
	public String getCommandName() {
		return "Op";
	}

	@Override
	protected void handleMessage(jIRCBot bot, String channel, String sender,
			String message) {
	    
		// We do not want anything that has spaces, so grab the first word.
	    /* The proper structure for this command should be as follows:
	     * Two methods:
	     * - !op
	     *     - The bot will re-check your op status if you are not currently op.
	     * - !op <Username> <Username> <Username>
	     *     - The bot will give operator status to all of the specified usernames.
	     */
		String[] splitMsg = message.trim().split(" ");

		jIRCUser temp = bot.userListGetSafe(sender);
		if(temp.getAuthLevel().ordinal() < eAuthLevels.operator.ordinal()) {
			bot.startAuthForUser(temp);
		} else {
		    
            int modeCount = 0;
            String modeMsg = "";
            
		    for(String curTargetUsr: splitMsg) {
		        
		        temp = bot.userListGetSafe(curTargetUsr);
		        
	            if (temp != null) {
	                boolean opped = false;
	                // Check to make sure we are not downgrading someone from admin.
	                if (temp.getAuthLevel().ordinal() < eAuthLevels.operator.ordinal()) {
	                    temp.setAuthorized(eAuthLevels.operator);
	                    bot.userListPutSafe(temp);
	                    opped = true;
	                }
	                // Check to make sure the target user is not already opped.
	                if (!bot.getUser(channel, curTargetUsr).isOp()) {
	                    // We are going to perform bulk 'op' transactions
	                    // to cut down on channel noise.
	                    modeMsg = "o" + modeMsg + " " + curTargetUsr;
	                    modeCount++;
	                    
                        // Have we hit the bulk op limit?
	                    if((modeCount % BULK_OP_COUNT) == 0) {
	                        // op the people in the current list...
	                        bot.setMode(channel, "+" + modeMsg);
	                        // Clear the bulk mode buffer.
	                        modeMsg = "";
	                    }
	                    
	                    opped = true;
	                }
	    
	                // If any action was actually performed, alert the user and log the
	                // event.
	                if (opped) {
	                    bot.sendMessage(curTargetUsr, sender
	                            + " granted you operator priviledges.");
	                    bot.log(sender + " granted " + curTargetUsr
	                            + " operator priviledges.", eLogLevel.info);
	                }
	            } else {
	                bot.sendMessage(sender, "Could not find user (" + curTargetUsr + "). "
	                        + getHelp());
	            }
		    }
		    
		    // Were we left with a non-even number of users to op?
		    if((modeCount % BULK_OP_COUNT) != 0) {
		        bot.setMode(channel, "+" + modeMsg);
		    }
		}
	}
}