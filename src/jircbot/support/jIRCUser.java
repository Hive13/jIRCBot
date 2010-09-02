package jircbot.support;

import java.util.HashMap;
import java.util.Iterator;

/**
 * This class is different from the pIRCBot's User class in that
 * the pIRCBot's user is specific to a certain channel while this
 * class is representing a single user across multiple channels.
 * @author vincenpt
 *
 */
public class jIRCUser {
    /**
     * Specifies the different levels of authorization
     * that are possible.
     */
    public enum eAuthLevels {
        /**
         * User has no permissions.
         */
        unauthorized,
        /**
         * User may have permissions,
         * but they are limited. If the channel
         * enforces voice this user will be 
         * voiced.
         */
        known,
        /**
         * User may change basic bot settings
         * and will be elevated to channel
         * operator.
         */
        operator,
        /**
         * User may change all bot settings
         * and will be elevated to channel
         * operator.
         */
        admin
    }
    
    private String _username;
    private HashMap<String, String> _channels;
    private eAuthLevels _authLevel;
    
    public jIRCUser(String username) {
        _channels = new HashMap<String, String>();
        this._username = username;
        setAuthorized(eAuthLevels.unauthorized);
    }
    
    /**
     * Add a channel to the list of channels
     * that the user is present in.
     * @param channel   The name of the channel the user has just joined.
     */
    public void addChannel(String channel) {
        _channels.put(channel, channel);
    }
    
    /**
     * Checks if the user is in the specified channel.
     * @param channel   The channel to check for.
     * @return  Returns true if the user is in the specified channel.
     */
    public boolean isInChannel(String channel) {
        return _channels.containsKey(channel);
    }
    
    /**
     * Removes the user from the specified channel.
     * @param channel   The channel to remove the user from.
     * @return  Returns true if the user was actually in
     *          the specified channel.
     */
    public boolean removeChannel(String channel) {
        return (_channels.remove(channel) != null);
    }
    
    /**
     * Gets an array of channels that the user is
     * currently known to be in.  This will be
     * restricted to the channels the bot and
     * the user are present in.
     * @return  An array of channel names that the 
     *          user is known to be in.
     */
    public String[] getChannelArray() {
        return (String[]) _channels.values().toArray();
    }

    /**
     * Returns an iterator of the list of channels.
     * @return  Returns an iterator of the list of channels
     *          that we know the user is present in.
     */
    public Iterator<String> getChannelIterator() {
        return _channels.values().iterator();
    }
    
    /**
     * Returns the number of channels that we know the user is
     * present in.
     * @return  The number of channels that we know the user is
     *          present in.
     */
    public int getChannelCount() {
        return _channels.size();
    }
    /**
     * Returns the user's known username.
     * @return  The user's known username.
     */
    public String getUsername() {
        return _username;
    }

    /**
     * Change the user's username.
     * @param username  New username for this user.
     */
    public void setUsername(String username) {
        this._username = username;
    }
    /**
     * Set the user's authorization level.
     * @param authLevel The user's knew authorization
     *                  level.
     */
    public void setAuthorized(eAuthLevels authLevel) {
        this._authLevel = authLevel;
    }

    /**
     * Returns the user's current authorization level.
     * @return  Returns the user's current authorization
     *          level.
     */
    public eAuthLevels getAuthLevel() {
        return _authLevel;
    }
    
}
