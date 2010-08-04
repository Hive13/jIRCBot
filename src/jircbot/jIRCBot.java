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
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import jircbot.commands.jIBCPluginList;
import jircbot.commands.jIBCTRssReader;
import jircbot.commands.jIBCommand;
import jircbot.commands.jIBCommandThread;
import jircbot.commands.jIBQuitCmd;
import jircbot.commands.jIBTimeCmd;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;

import jircbot.jIRCTools.eMsgTypes;

/**
 * 
 * @author vincenpt
 */
public class jIRCBot extends PircBot {
    // Store the commands
    /* 
     * Important Stuff for threadedCommands - We can have multiple cT w/ the
     * same name. - What is unique about a tC then? - Name + channel, we can
     * only have 1 tC per channel. - Going to need to change the name for each
     * instance per channel. - Each tC has the following: - tC implementation
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

        try {
            // Add all command threads.
            addCommandThread(new jIBCTRssReader(this, "[commandName] - [Title] - [Author] [Link]",
                    "WikiFeed", channelList.get(0),
                    "http://wiki.hive13.org/index.php?title=Special:RecentChanges&feed=rss&hideminor=1"));
            
            addCommandThread(new jIBCTRssReader(this, "Hive13Blog", channelList.get(0),
                    "http://www.hive13.org/?feed=rss2"));
            
            addCommandThread(new jIBCTRssReader(this, "Hive13List", channelList.get(0),
                    "http://groups.google.com/group/cincihackerspace/feed/rss_v2_0_msgs.xml"));
            // Disabling the flickr feed.
            // addCommandThread(new jIBCTRssReader(this, "Hive13Flickr", channelList.get(0),
            //        "http://api.flickr.com/services/feeds/photos_public.gne?tags=hive13&lang=en-us&format=rss_200"));
            
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

    public void addCommand(jIBCommand cmd) {
        commands.put(cmd.getCommandName(), cmd);
    }

    public void addCommandThread(jIBCommandThread cmd) {
        commands.put(cmd.getCommandName(), cmd);
        new Thread(cmd).start();
    }

    @Override
    public void onMessage(String channel, String sender, String login,
            String hostname, String message) {
        
        jIRCTools.insertMessage(channel, this.getServer(), sender, message, eMsgTypes.publicMsg);
        
        // Find out if the message was for this bot
        if (message.startsWith(prefix)) {
            message = message.replace(prefix, "");

            jIBCommand cmd;
            // Check to see if it is a standard command.
            if ((cmd = commands.get(message)) != null)
                cmd.handleMessage(this, channel, sender,
                        message.replace(cmd.getCommandName(), "").trim());
            // It was not a standard command, is it for a threaded one?
            else if ((cmd = commands.get(message + channel)) != null) {
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

    public void onAction(String sender, String login, String hostname, String target, String action) {
        jIRCTools.insertMessage(target, this.getServer(), sender, action, eMsgTypes.actionMsg);
    }
    
    public void onJoin(String channel, String sender, String login, String hostname) {
        jIRCTools.insertMessage(channel, this.getServer(), login, "", eMsgTypes.joinMsg);
    }
    
    public void onPart(String channel, String sender, String login, String hostname) {
        jIRCTools.insertMessage(channel, this.getServer(), login, "", eMsgTypes.partMsg);
    }
    
    public void onNickChange(String oldNick, String login, String hostname, String newNick) {
        jIRCTools.insertMessage("", this.getServer(), login, oldNick, eMsgTypes.nickChange);
    }
    
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
}
