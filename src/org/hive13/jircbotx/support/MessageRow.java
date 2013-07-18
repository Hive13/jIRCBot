package org.hive13.jircbotx.support;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.hive13.jircbotx.JircBotX.eMsgTypes;


public class MessageRow {
   public int pk_MessageID = -1, fk_ChannelID = -1;
   public String vcUsername = null, vcMessage = null;
   public eMsgTypes msgType = null;
   public Date tsMsgTime = null;
   
   public MessageRow(int pk_MessageID, int fk_ChannelID,
         String vcUsername, String vcMessage, eMsgTypes msgType,
         Date tsMsgTime) {
      this.pk_MessageID = pk_MessageID;
      this.fk_ChannelID = fk_ChannelID;
      this.vcUsername = vcUsername;
      this.vcMessage = vcMessage;
      this.msgType = msgType;
      this.tsMsgTime = tsMsgTime;
   }
   
   public MessageRow(ResultSet rs) throws SQLException {
       this(rs, true);
   }
   
   /**
    * This constructor is nifty, it can either construct a
    * MessageRow object from a partial ResultSet or it can
    * check to make sure that all of the column names
    * exist in the result set.
    * 
    * @param rs               ResultSet to parse a MessageRow from.
    * @param validateNames    True forces all column names to exist,
    *                         False allows for partial column data.
    * @throws SQLException
    */
   public MessageRow(ResultSet rs, boolean validateNames) throws SQLException {
        if(validateNames || BotDatabase.isValidColumn(rs, "fk_ChannelID"))
            this.fk_ChannelID = rs.getInt("fk_ChannelID");
        
        if(validateNames || BotDatabase.isValidColumn(rs, "pk_MessageID"))
            this.pk_MessageID = rs.getInt("pk_MessageID");
        
        if(validateNames || BotDatabase.isValidColumn(rs, "vcUsername"))
            this.vcUsername = rs.getString("vcUsername");
        
        if(validateNames || BotDatabase.isValidColumn(rs, "vcMessage"))
            this.vcMessage = rs.getString("vcMessage");
        
        if(validateNames || BotDatabase.isValidColumn(rs, "vcMsgType"))
            this.msgType = eMsgTypes.valueOf(rs.getString("vcMsgType"));
        
        if(validateNames || BotDatabase.isValidColumn(rs, "tsMsgTime"))
            this.tsMsgTime = rs.getDate("tsMsgTime");
    }
   
}

