/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jircbot;

import jircbot.Commands.jIBCommand;
import jircbot.Commands.jIBTimeCmd;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import jircbot.Commands.jIBQuitCmd;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;

/**
 *
 * @author vincenpt
 */
public class jIRCBot extends PircBot {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Properties config = new Properties();
        try {
            config.load(new FileInputStream("pbdemo.properties"));
        } catch (IOException ex) {
            Logger.getLogger(jIRCBot.class.getName()).log(Level.SEVERE, null, ex);
        }

       new jIRCBot(config);
    }

    // Store the commands
    private final List<jIBCommand> commands;

    // The character which tells the bot we're talking to it and not anyone/anything else.
    private final String prefix = "!";
    
    private jIRCBot(Properties config) {
        commands = new ArrayList<jIBCommand>();

        setVerbose(true);

        // Add all commands
        commands.add(new jIBTimeCmd());
        commands.add(new jIBQuitCmd());

        // Connect to IRC
        setAutoNickChange(true);
        setName(config.getProperty("nick", "Hive13Bot"));
        try {
            connect(config.getProperty("server", "irc.freenode.net"));
            joinChannel(config.getProperty("channel", "#Hive13_test"));
        } catch (NickAlreadyInUseException ex) {
            Logger.getLogger(jIRCBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IrcException ex) {
            Logger.getLogger(jIRCBot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(jIRCBot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onMessage(String channel, String sender, String login,
            String hostname, String message) {

        // Find out if the message was for this bot
        if(message.startsWith(prefix)) {
            message = message.replace(prefix, "");

            // Find out the command given (do simply)
            for(jIBCommand cmd : commands) {
                // If the msg starts w/ the cmd the bot reponds to, remove the
                // cmd from the message and pass the event along to the botCmd
                if(message.startsWith(cmd.getCommandName())) {
                    cmd.handleMessage(this, channel, sender, message.replace(cmd.getCommandName(), "").trim());
                }
            }
        }
    }

    @Override
    public void onDisconnect() {
        System.exit(0);
    }
}
