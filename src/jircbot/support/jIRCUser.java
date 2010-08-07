package jircbot.support;

import java.util.HashMap;

/**
 * This class is not used... yet?
 * @author vincenpt
 *
 */
public class jIRCUser {
    private String _username;
    private HashMap<String, String> _channels;
    private boolean _authorized;
    
    public jIRCUser(String username) {
        _channels = new HashMap<String, String>();
        this._username = username;
        setAuthorized(false);
    }
    
    public void addChannel(String channel) {
        _channels.put(channel, channel);
    }
    
    public boolean isInChannel(String channel) {
        return _channels.containsKey(channel);
    }
    
    public boolean removeChannel(String channel) {
        return (_channels.remove(channel) != null);
    }
    
    public String[] getChannels() {
        return (String[]) _channels.values().toArray();
    }

    public String getUsername() {
        return _username;
    }

    public void setAuthorized(boolean _authorized) {
        this._authorized = _authorized;
    }

    public boolean isAuthorized() {
        return _authorized;
    }
    
}
