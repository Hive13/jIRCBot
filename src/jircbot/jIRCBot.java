/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jircbot;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import jircbot.commands.jIBCLogger;
import jircbot.commands.jIBCPluginList;
import jircbot.commands.jIBCTRssReader;
import jircbot.commands.jIBCommand;
import jircbot.commands.jIBCommandThread;
import jircbot.commands.jIBQuitCmd;
import jircbot.commands.jIBTimeCmd;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import jircbot.support.jIRCTools;
import jircbot.support.jIRCTools.eMsgTypes;

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

    // The character which tells the bot we're talking to it and not
    // anyone/anything else.
    private final String prefix = "!";
    // Server to join
    private String serverAddress = "";
    // Username to use
    private String botName = "";
    
    // List of channels to join
    private final List<String> channelList;

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
    private final HashMap<String, List<String>> userList;
    
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

        new jIRCBot(config);
    }

    private jIRCBot(Properties config) {

        // Initialize lists
        commands = new HashMap<String, jIBCommand>();

        channelList = new ArrayList<String>();

        userList = new HashMap<String, List<String>>();
        // Grab configuration information.
        botName = config.getProperty("nick", "Hive13Bot");
        serverAddress = config.getProperty("server", "irc.freenode.net");
        
        // If we have bit.ly information, grab it. This is used
        // to shorten URLs
        jIRCTools.bitlyName = config.getProperty("bitlyName", "");
        jIRCTools.bitlyAPIKey = config.getProperty("bitlyAPI", "");

        // If we have jdbc information, grab it. This is used
        // to log the chat room.
        jIRCTools.jdbcURL = config.getProperty("jdbcURL", "");
        jIRCTools.jdbcUser = config.getProperty("jdbcUsername", "");
        jIRCTools.jdbcPass = config.getProperty("jdbcPassword", "");
            // If there is no URL or no username, then jdbc will not be enabled.
        jIRCTools.jdbcEnabled = (jIRCTools.jdbcURL.length() > 0 && jIRCTools.jdbcUser.length() > 0);
        if(!jIRCTools.jdbcEnabled) {
            this.log("@@@ ALERT @@@ - JDBC Logging is disabled!");
        }
        
        // Parse the list of channels to join.
        String strChannels = config.getProperty("channels", "#Hive13_test");
        String splitChannels[] = strChannels.split(",");
        for (int i = 0; i < splitChannels.length; i++) {
            String channel = splitChannels[i].trim();
            if (channel.length() > 0) {
                channelList.add(channel);
            }
        }

        // Make it so that the bot outputs lots of information when run.
        setVerbose(true);

        // Add all commands
        addCommand(new jIBTimeCmd());
        addCommand(new jIBQuitCmd());
        addCommand(new jIBCPluginList(commands));
        addCommand(new jIBCLogger());

        try {
            // Add all command threads.
            addCommandThread(new jIBCTRssReader(this, "[commandName] - [Title] - [Author] [Link]",
                    "WikiFeed", channelList.get(0),
                    "http://wiki.hive13.org/index.php?title=Special:RecentChanges&feed=rss&hideminor=1"));
            
            addCommandThread(new jIBCTRssReader(this, "Hive13Blog", channelList.get(0),
                    "http://www.hive13.org/?feed=rss2"));
            
            addCommandThread(new jIBCTRssReader(this, "[commandName] - [Title] - [Author] [Link]", 
            		"Hive13List", channelList.get(0), 
            		"http://groups.google.com/group/cincihackerspace/feed/rss_v2_0_msgs.xml"));
            
            // PTV: Flickr feed has been known to have issues... 
            addCommandThread(new jIBCTRssReader(this, "Hive13Flickr", channelList.get(0),
                    "http://api.flickr.com/services/feeds/photos_public.gne?tags=hive13&lang=en-us&format=rss_200"));
            
            addCommandThread(new jIBCTRssReader(this, "Hive13Twitter", channelList.get(0),
                    "http://twitter.com/statuses/user_timeline/39281942.rss"));
        } catch (MalformedURLException ex) {
            Logger.getLogger(jIRCBot.class.getName()).log(Level.SEVERE, null,
                    ex);
            this.log("Error: jIRCBot()" + ex.toString());
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

    /**
     * Adds a command to the list of known commands.
     * @param cmd   Command to add to the command list.
     */
    public void addCommand(jIBCommand cmd) {
        commands.put(cmd.getCommandName(), cmd);
    }

    /**
     * Adds a commandThread to the list of known commands.
     * @param cmd   CommandThread to add to the command list.
     */
    public void addCommandThread(jIBCommandThread cmd) {
        commands.put(cmd.getCommandName(), cmd);
        new Thread(cmd).start();
    }

    public void onMessage(String channel, String sender, String login,
            String hostname, String message) {
        jIRCTools.insertMessage(channel, this.getServer(), sender, message, eMsgTypes.publicMsg);
        // Find out if the message was for this bot
        if (message.startsWith(prefix)) {
            message = message.replace(prefix, "");

            jIBCommand cmd;
            // Check to see if it is a standard command.
            String[] sCmd = message.split(" ", 2);
            if ((cmd = commands.get(sCmd[0])) != null)
                if(sCmd.length == 2)
                    cmd.handleMessage(this, channel, sender, sCmd[1].trim());
                else
                    cmd.handleMessage(this, channel, sender, "");
                    
            // It was not a standard command, is it for a threaded one?
            else if ((cmd = commands.get(sCmd[0] + channel)) != null) {
                jIBCommandThread cmdT = (jIBCommandThread) cmd;
                if (cmdT.getIsRunning())
                    cmdT.stop();
                else
                    /*
                     * We are just restarting the previously stopped command.
                     * But was it actually stopped? This is a curious method. We
                     * are certainly not referencing a new command, but it was
                     * running in an infinite while loop, when stop() is called,
                     * we set a boolean to false, which kills the while loop,
                     * but the member variables will still be the same as when
                     * the commandThread was initialized.
                     */
                    new Thread(cmdT).start();

            }
        }
    }

    /*
     * If the bot is disconnected from the server, quit the bot.
     */
    @Override
    public void onDisconnect() {
        System.exit(0);
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
            List<String> channels;
            if((channels = userList.get(users[i].getNick())) != null) {
                // yes! Update it.
                channels.add(channel);
            }
            else {
                // no! Add it.
                channels = new ArrayList<String>();
                channels.add(channel);
                userList.put(users[i].getNick(), channels);
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
            List<String> channels;
            if((channels = userList.get(sender)) != null) {
                // yes! Update it.
                channels.add(channel);
            }
            else {
                // no! Add it.
                channels = new ArrayList<String>();
                channels.add(channel);
                userList.put(sender, channels);
            }
            jIRCTools.insertMessage(channel, this.getServer(), sender, "", eMsgTypes.joinMsg);
        }
    }
    
    public void onPart(String channel, String sender, String login, String hostname) {
        // Did we just leave the channel?
        if(sender.equals(this.getName())) {
            // Yes, remove the list of users we know in this channel.
            //  ** NOTE ** This is going to be horribly in-efficient.
            Iterator<Entry<String, List<String>>> it = userList.entrySet().iterator();
            while(it.hasNext()) {
                Entry<String, List<String>> e = it.next();
                List<String> chanList = e.getValue();
                // Remove any channel with the name in this list.
                chanList.remove(channel);
                
                // If that was the only channel, remove the item.
                if(chanList.isEmpty())
                    userList.remove(e.getKey());
            }
        } else {
            // No, it was someone else.
            List<String> channels;
            if((channels = userList.get(sender)) != null) {
                channels.remove(channel);
                
                // Was this the only channel the user as in?
                if(channels.isEmpty())
                   userList.remove(sender);
            }
            jIRCTools.insertMessage(channel, this.getServer(), sender, "", eMsgTypes.partMsg);
        }
    }
    public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
        // This could be us, but if we quit, who cares?
        List<String> channels;
        if((channels = userList.remove(sourceNick)) != null) {
            Iterator<String> i = channels.iterator();
            while(i.hasNext()) {
                jIRCTools.insertMessage(i.next(), this.getServer(), sourceNick, reason, eMsgTypes.quitMsg);
                
            }
        }
    }
    
    public void onNickChange(String oldNick, String login, String hostname, String newNick) {
        List<String> channels;
        if((channels = userList.remove(oldNick)) != null) {
            Iterator<String> i = channels.iterator();
            while(i.hasNext()) {
                jIRCTools.insertMessage(i.next(), this.getServer(), oldNick, newNick, eMsgTypes.nickChange);
            }
            
            userList.put(newNick, channels);
        }
    } 
    
    public boolean isUserKnownToNickServ(String username) {
        /*
         * Ok, This is going to be a bit hackish.  Current implementation
         * requires that if we want to know if a user is on our "Auth" list
         * we need to send the following message:
         * ./msg nickserv info <username>
         * 
         * To which we get the response:
         * -NickServ(NickServ@services.)- Information on cjdavis (account cjdavis):
         * -NickServ(NickServ@services.)- Registered : Jun 22 00:14:39 2008 (2 years, 6 weeks, 4 days, 18:37:47 ago)
         * -NickServ(NickServ@services.)- Last addr  : ~cjdavis@72.49.163.38
         * -NickServ(NickServ@services.)- Last seen  : now
         *  -NickServ(NickServ@services.)- Flags      : HideMail
         * -NickServ(NickServ@services.)- *** End of Info ***

         */
        return false;
    }
}
