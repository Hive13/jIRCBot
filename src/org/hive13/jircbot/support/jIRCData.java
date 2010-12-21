package org.hive13.jircbot.support;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

public class jIRCData {
    private static final String dataFileName = "jIRCBot.data";
    private static jIRCData instance = null;
    
    private Properties data;
    
    private final String defaultObfuscatedWords = "";
    
    private static ArrayList<String> obfuscatedWords;
    
    protected jIRCData() {
        data = new Properties();
        try {
            data.load(new FileInputStream(dataFileName));
        } catch (IOException ex) {
            System.err.println(ex);
        }
        obfuscatedWords = null;
    }

    /**
     * Get an instance of jIRCData.
     * @return  The singleton instance of jIRCData.
     */
    public synchronized static jIRCData getInstance() {
        if (instance == null) {
            instance = new jIRCData();
        }
        return instance;
    }

    /**
     * Refresh information from the data file.
     * @return
     */
    public synchronized static jIRCData refresh() {
        instance = null; // Kill the current instance.
        return getInstance();
    }
    
    public String getProp(String key, String defaultString) {
        return data.getProperty(key, defaultString);
    }
    
    public void setProp(String key, String value) {
        data.setProperty(key, value);
        try {
            data.store(new FileOutputStream(dataFileName), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Find the list of obfuscated words.
     */
    public ArrayList<String> getObfuscatedWords() {
        // Since we only read the properties once, it does not make sense
        // to repeatedly re-parse the string.
        if(obfuscatedWords == null) {
            String words  = getProp("obfuscatedWords", defaultObfuscatedWords).toLowerCase();
            String splitWords[] = words.split(", ");
            obfuscatedWords = new ArrayList<String>();
            // Check to make sure we are not adding an empty string.
            for(int i = 0; i < splitWords.length; i++) {
                String addWord = "";
                if(!(addWord = splitWords[i].trim()).isEmpty()) {
                    // Added toLowerCase in case someone tried to be clever and edit the dat file directly.
                    obfuscatedWords.add(addWord.toLowerCase());
                }
            }
        }
        return obfuscatedWords;
    }
    
    /**
     * Adds a word to the obfuscated words list.
     * @param Word to add to the obfuscation list.
     */
    public void addObfuscatedWord(String word) {
        if(obfuscatedWords == null)
            getObfuscatedWords();
        
        if(obfuscatedWords.indexOf(word.toLowerCase()) == -1) {
            obfuscatedWords.add(word);
            
            Iterator<String> it = obfuscatedWords.iterator();
            String outProperty = "";
            
            boolean isFirst = true;
            while(it.hasNext()) {
                String outProp = it.next();
                if(!outProp.trim().isEmpty()) {
                    if(!isFirst)
                        outProperty += ", ";
                    else
                        isFirst = false;
                    outProperty += outProp;
                }
            }
            setProp("obfuscatedWords", outProperty.toLowerCase());
        }
    }
    
}
