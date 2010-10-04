/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hive13.jircbot;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hive13.jircbot.commands.jIBCLinkify;
import org.hive13.jircbot.commands.jIBCQuitCmd;
import org.hive13.jircbot.commands.jIBCTRssReader;
import org.hive13.jircbot.commands.jIBCTell;
import org.hive13.jircbot.commands.jIBCTimeCmd;
import org.hive13.jircbot.commands.jIBCommand;
import org.hive13.jircbot.commands.jIBCommandThread;
import org.hive13.jircbot.support.jIRCProperties;
import org.hive13.jircbot.support.jIRCTools;
import org.hive13.jircbot.support.jIRCTools.eMsgTypes;
import org.hive13.jircbot.support.jIRCUser;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;


/**
 * 
 * @author vincenpt
 */
public class jIRCBot extends PircBot {
    // Store the commands
    /* 
     * Important Stuff for threadedCommands
     *  - We can have multiple cT w/ the same name. 
     *  - What is unique about a tC then?
     *   - Name + channel, we can only have 1 tC per channel.
     *      - Fixed: Each command 
     *   - Going to need to change the name for each instance per channel. 
     *   - Each tC has the following:
     *      - tC implementation
     */
    private final HashMap<String, jIBCommand> commands;
    private final ArrayList<jIBCommand> lineParseCommands;

    // The character which tells the bot we're talking to it and not
    // anyone/anything else.
    private final String prefix = "!";
    // Server to join
    private String serverAddress = "";
    // Username to use
    private String botName = "";
    
    // List of channels to join
    private final List<String> channelList;

    // List of Authorized usernames
    private final List<String> authedUserList;
    
    public AtomicBoolean abShouldQuit = new AtomicBoolean(false);
    
    /*
     * Ok, the userList is a bit hackish at the moment
     * and there is the potential that it will leak some
     * memory.  Basically I try to programatically keep
     * track of what users are in the channels the bot lives
     * in and what their current usernames are.
     * 
     * The basic problem is this: I only remove users
     * from the list when they leave a channel or quit
     * the server.  If they leave through other means
     * (kick, ban, other?) they will get stuck in the
     * userList until the bot leaves & rejoins the channel.
     */
    
    /*         !!!!!!!!! WARNING !!!!!!!!!!!!!!
     * The userList is accessed from multiple threads
     * Simultaneously.  ONLY access it through approved
     * safe methods.  If you need to access a function
     * that is not exposed through the one of these
     * functions, you may write a new one using the
     * userListMutex to ensure that only one thread is 
     * actively accessing the userList.
     */
    private final HashMap<String, jIRCUser> userList;
    private Semaphore userListMutex;
    
    /**
     * @param args  the command line arguments
     */
    public static void main(String[] args) {
        Properties config = new Properties();
        try {
            config.load(new FileInputStream("jIRCBot.properties"));
        } catch (IOException ex) {
            Logger.getLogger(jIRCBot.class.getName()).log(Level.SEVERE, null,
                    ex);
        }

        new jIRCBot();
    }

    private jIRCBot() {

        // Initialize lists
        commands = new HashMap<String, jIBCommand>();

        lineParseCommands = new ArrayList<jIBCommand>();
        
        channelList = new ArrayList<String>();

        userList = new HashMap<String, jIRCUser>();
        userListMutex = new Semaphore(1);
        
        authedUserList = new ArrayList<String>();
        authedUserList.add("pvince");
        
        // Grab configuration information.
        botName = jIRCProperties.getInstance().getBotName();
        serverAddress = jIRCProperties.getInstance().getServer();

        // Parse the list of channels to join.
        String strChannels[] = jIRCProperties.getInstance().getChannels();
        for (int i = 0; i < strChannels.length; i++) {
            String channel = strChannels[i].trim();
            if (channel.length() > 0) {
                channelList.add(channel);
            }
        }

        // Make it so that the bot outputs lots of information when run.
        setVerbose(true);

        // Add commands that parse every single line
        // WARNING!! Be sparing with these commands
        //           their code should be run asynchronously
        addLineParseCommand(new jIBCTell());
        addLineParseCommand(new jIBCLinkify());
        
        // Add passive commands
        addCommand(new jIBCTimeCmd());
        addCommand(new jIBCQuitCmd());
        //addCommand(new jIBCPluginList(commands));
        //addCommand(new jIBCLogParser());

        try {
            // Add all command threads.
            addCommandThread(new jIBCTRssReader(this, "WikiFeed", channelList.get(0),
                    "[commandName] - [Title|c50] - [Author|c20] ( [Link] )",
                    "http://wiki.hive13.org/index.php?title=Special:RecentChanges&feed=rss&hideminor=1"));
            
            addCommandThread(new jIBCTRssReader(this, "Hive13Blog", channelList.get(0),
                    "http://www.hive13.org/?feed=rss2"));
            
            addCommandThread(new jIBCTRssReader(this, "Hive13List", channelList.get(0),
                    "[commandName] - [Title|c50] - [Author|c20|r\\(.+\\)] ( [Link] )",
                    "http://groups.google.com/group/cincihackerspace/feed/rss_v2_0_msgs.xml"));
            
            // PTV: Flickr feed has been known to have issues... 
            addCommandThread(new jIBCTRssReader(this, "Hive13Flickr", channelList.get(0),
                    "http://api.flickr.com/services/feeds/photos_public.gne?tags=hive13&lang=en-us&format=rss_200",
                    15*60*1000)); // 15 minutes (15 * 60 seconds)
            
            addCommandThread(new jIBCTRssReader(this, "Hive13Twitter", channelList.get(0),
                    "[commandName] - [Title|c30] - [Author|c20|r\\(.+\\)] ( [Link] )",
                    "http://search.twitter.com/search.atom?q=hive13"));
        } catch (MalformedURLException ex) {
            Logger.getLogger(jIRCBot.class.getName()).log(Level.SEVERE, null,
                    ex);
            log("jIRCBot()" + ex.toString(), eLogLevel.severe);
        }

        // Connect to IRC
        setAutoNickChange(true);
        setName(botName);
        try {
            // Connect to the config server
            connect(serverAddress);

            // Connect to all channels listed in the config.
            for (Iterator<String> i = channelList.iterator(); i.hasNext();) {
                joinChannel(i.next());
            }
        } catch (NickAlreadyInUseException ex) {
            Logger.getLogger(jIRCBot.class.getName()).log(Level.SEVERE, null,
                    ex);
            this.log("Error: jIRCBot()" + ex.toString());
        } catch (IrcException ex) {
            Logger.getLogger(jIRCBot.class.getName()).log(Level.SEVERE, null,
                    ex);
            this.log("Error: jIRCBot()" + ex.toString());
        } catch (IOException ex) {
            Logger.getLogger(jIRCBot.class.getName()).log(Level.SEVERE, null,
                    ex);
            this.log("Error: jIRCBot()" + ex.toString());
        }
    }

    public void onMessage(String channel, String sender, String login,
            String hostname, String message) {
        jIRCTools.insertMessage(channel, this.getServer(), sender, message, eMsgTypes.publicMsg);
        // Find out if the message was directed as a command.
        if (message.startsWith(prefix)) {
            message = message.replace(prefix, "");

            jIBCommand cmd;
            // Check to see if it is a known standard command.
            String[] sCmd = message.split(" ", 2);
            if ((cmd = commands.get(sCmd[0].toLowerCase())) != null ||
                (cmd = commands.get(sCmd[0].toLowerCase() + channel)) != null)
                if(sCmd.length == 2)
                    cmd.runCommand(this, channel, sender, sCmd[1].trim());
                else
                    cmd.runCommand(this, channel, sender, "");
        }
        
        /* Run the commands that run with every message
         * It is especially important to make sure that these
         * commands are thread safe.  All commands are run
         * as new threads, however these commands have a greater
         * potential to still be running if several messages
         * come in fast.
         */
        Iterator<jIBCommand> i = lineParseCommands.iterator();
        while(i.hasNext()) {
            i.next().runCommand(this, channel, sender, message);
        }
    }

    /*
     * If the bot is disconnected from the server, quit the bot.
     */
    @Override
    public void onDisconnect() {
        if(abShouldQuit.get()) {
            log("We are shutting down properly!", eLogLevel.info);
            System.exit(0);
        } else {
            log("Accidental disconnect detected, initiating reconnect.", eLogLevel.warning);
            try {
                // Connect to the config server
                connect(serverAddress);

                // Connect to all channels listed in the config.
                for (Iterator<String> i = channelList.iterator(); i.hasNext();) {
                    joinChannel(i.next());
                }
            } catch (NickAlreadyInUseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IrcException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Various log levels for when the bot is writing to the log.
     * This is primarily used for formatting the log message.
     * @author vincentp
     *
     */
    public enum eLogLevel{
    	/** Just an alert message giving a bot status update. */
        info,
        /** Either a minor error or slightly unexpected event has occurred.
         *  this is not really serious and the bot will continue normal operations
         */
        warning,
        /** A more serious event has occurred.  The bot will probably recover
         * from this error and continue normal operations, however it is not guaranteed.
         */
        error,
        /** A major error has occurred that will effect bot actions and operations.
         *  This should never occur in the course of bot operation unless it has been
         *  incorrectly configured or compiled.  The bot can not continue normal operations
         *  and will probably act very strangely until it is restarted.  This error is 
         *  thrown when a key process is interrupted or throws an error.
         */
        severe
    }
    
    public void log(String line, eLogLevel logLevel) { 
        switch(logLevel) {
        case info:
            line = "<Info> " + line;
            break;
        case warning:
            line = "<Warning> " + line;
            break;
        case error:
            line = "<Error> " + line;
            break;
        case severe:
            line = "<Severe Error> " + line;
            break;
        }
        
        super.log(line);
    }
    
    /**
     * Checks to see if the bot is actively in the specified channel.
     * @param channel   Name of the channel to check for.
     * @return          Returns true if the bot is in the channel.
     */
    public boolean inChannel(String channel) {
        String channelList[] = this.getChannels();
        for(String _channel : channelList) {
            if(_channel.equals(channel))
                return true;
        }
        return false;
    }
    
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // The following is only used for message logging purposes right now.
    public void onAction(String sender, String login, String hostname, String target, String action) {
        jIRCTools.insertMessage(target, this.getServer(), sender, action, eMsgTypes.actionMsg);
    }
    
    public void onUserList(String channel, User[] users) {
        // TODO: This method currently has the potential to allow for duplicate channels in the list.
        for(int i = 0; i < users.length; i++) {
            jIRCUser user;
            if((user = userListGet(users[i].getNick())) != null) {
                // yes! Update it.
                user.addChannel(channel);
            }
            else {
                // no! Add it.
                user = new jIRCUser(users[i].getNick());
                user.addChannel(channel);
                userListPut(user);
            }
        }
    }
    
    public void onJoin(String channel, String sender, String login, String hostname) {
        // Do we know about this user?
        if(sender.equals(this.getName())) {
            // We just joined a channel, get the list of users in this channel.
            // *UPDATE* This does not work... use the "OnUserList" method.
        } else {
            // This is someone else joining the channel.
            jIRCUser user;
            if((user = userListGet(sender)) != null) {
                // yes! Update it.
                user.addChannel(channel);
            }
            else {
                // no! Add it.
                user = new jIRCUser(sender);
                user.addChannel(channel);
                userListPut(user);
            }
            jIRCTools.insertMessage(channel, this.getServer(), sender, "", eMsgTypes.joinMsg);
        }
    }
    
    public void onPart(String channel, String sender, String login, String hostname) {
        // Did we just leave the channel?
        if(sender.equals(this.getName())) {
            // Yes, remove the list of users we know in this channel.
            //  ** NOTE ** This is going to be horribly in-efficient.
            //             We need to get each user, then go through
            //             each user's list of channels and remove the
            //             channel from that list.
            //             So I think that makes this take about n^x where we
            //             we have n users and x channels.
        	//			   The one saving grace of this method is that while n might
        	//			   be large, x should be small, and in most cases only 1.
            Iterator<Entry<String, jIRCUser>> it = userListEntrySet().iterator();
            while(it.hasNext()) {
                Entry<String, jIRCUser> e = it.next();
                jIRCUser user = e.getValue();
                // Remove any channel with the name in this list.
                user.removeChannel(channel);
                
                // If that was the only channel, remove the item.
                if(user.getChannelCount() == 0)
                    userListRemove(e.getKey());
            }
        } else {
            // No, it was someone else.
            jIRCUser user;
            if((user = userListGet(sender)) != null) {
                user.removeChannel(channel);
                
                // Was this the only channel the user as in?
                if(user.getChannelCount() == 0)
                   userListRemove(sender);
            }
            jIRCTools.insertMessage(channel, this.getServer(), sender, "", eMsgTypes.partMsg);
        }
    }
    public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
        // This could be us, but if we quit, who cares?
        jIRCUser user;
        if((user = userListRemove(sourceNick)) != null) {
            Iterator<String> i = user.getChannelIterator();
            while(i.hasNext()) {
                jIRCTools.insertMessage(i.next(), this.getServer(), sourceNick, reason, eMsgTypes.quitMsg);
            }
        }
    }
    
    public void onNickChange(String oldNick, String login, String hostname, String newNick) {
        jIRCUser user;
        if((user = userListRemove(oldNick)) != null) {
            Iterator<String> i = user.getChannelIterator();
            while(i.hasNext()) {
                jIRCTools.insertMessage(i.next(), this.getServer(), oldNick, newNick, eMsgTypes.nickChange);
            }
            user.setUsername(newNick);
            userListPut(user);
        }
    }
    
    /* Ok, time to look into how to authorize users again.
     * Here is the basic sequence of steps that need to occur:
     * 1. Send "info <Username>" message to 'nickserv' user.
     * 2. Parse returned response from 'nickserv' user to determine if the user is indeed authorized.
     * 
     * Perhaps best to try to do the 'Auth' requests as users are created, then just act as if the requests
     * have gone through?
     * 
     * This presents the potential that multiple threads will be accessing and mutating the 'userList' variable.
     * This may require that I encapsulate accessor & mutator methods for it.
     */
    public void startAuthForUser(jIRCUser user) {
        this.sendMessage("paul_hive13", "info " + user.getUsername());
    }
    public void onPrivateMessage(String sender, String login, String hostname, String message) {
        /* Ok, this is going to be interesting, and I think in retrospect all of that work
         * I did to make sure that userList was thread safe might have been a waste of time.
         * 
         */
    }
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // Utility functions for member variables.
    /**
     * Adds a command to the list of known commands.
     * @param cmd   Command to add to the command list.
     */
    public void addCommand(jIBCommand cmd) {
        commands.put(cmd.getCommandName().toLowerCase(), cmd);
    }

    /**
     * Adds a commandThread to the list of known commands.
     * @param cmd   CommandThread to add to the command list.
     */
    public void addCommandThread(jIBCommandThread cmd) {
        commands.put(cmd.getCommandName().toLowerCase(), cmd);
    }
    
    /**
     * Adds a command that parses each line received.
     * @param cmd   Command to add to the line parsing command.
     */
    public void addLineParseCommand(jIBCommand cmd) {
        lineParseCommands.add(cmd);
    }

    /**
     * Safely returns the jIRCUser associated with the passed
     * in username from the userList.
     * 
     * @param username  Key for the userList variable.
     * @return          The jIRCUser associated with the passed
     *                  in username.
     */
    public jIRCUser userListGet(String username) {
        jIRCUser result = null;
        try {
            userListMutex.acquire();
            result = userList.get(username);
        } catch (InterruptedException e) {
            Logger.getLogger(jIRCBot.class.getName()).log(Level.SEVERE, null,
                    e);
            e.printStackTrace();
        } finally {
            userListMutex.release();
        }
        
        return result;
    }
    
    /**
     * Safely adds the passed in user to the userList.
     * @param user  jIRCUser to add to the userList.
     */
    public void userListPut(jIRCUser user) {
        try {
            userListMutex.acquire();
            userList.put(user.getUsername(), user);
        } catch (InterruptedException e) {
            Logger.getLogger(jIRCBot.class.getName()).log(Level.SEVERE, null,
                    e);
            e.printStackTrace();
        } finally {
            userListMutex.release();
        }
    }

    /**
     * Safely wraps the userList.remove(String) method.
     * 
     * @param username  Username of the user to remove.
     * @return          The removed jIRCUser.
     */
    public jIRCUser userListRemove(String username) {
        jIRCUser result = null;
        try {
            userListMutex.acquire();
            result = userList.remove(username);
        } catch (InterruptedException e) {
            Logger.getLogger(jIRCBot.class.getName()).log(Level.SEVERE, null,
                    e);
            e.printStackTrace();
        } finally {
            userListMutex.release();
        }
        
        return result;
    }
    
    /**
     * Safely wraps the userList.EntrySet() method.
     * 
     * @return  The set of Keys & entries for userList.
     */
    public Set<Entry<String, jIRCUser>> userListEntrySet() {
        Set<Entry<String, jIRCUser>> result = null;
        try {
            userListMutex.acquire();
            result = userList.entrySet();
        } catch (InterruptedException e) {
            Logger.getLogger(jIRCBot.class.getName()).log(Level.SEVERE, null,
                    e);
            e.printStackTrace();
        } finally {
            userListMutex.release();
        }
        return result;
    }
}
