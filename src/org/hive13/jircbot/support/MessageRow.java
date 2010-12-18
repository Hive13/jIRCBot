package org.hive13.jircbot.support;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.hive13.jircbot.support.jIRCTools.eMsgTypes;

public class MessageRow {
	public int pk_MessageID, fk_ChannelID;
	public String vcUsername, vcMessage;
	public eMsgTypes msgType;
	public Date tsMsgTime;
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
	public MessageRow() {}
	
	public MessageRow(ResultSet rs) throws SQLException {
		this.fk_ChannelID = rs.getInt("fk_ChannelID");
		this.pk_MessageID = rs.getInt("pk_MessageID");
		this.vcUsername = rs.getString("vcUsername");
		this.vcMessage = rs.getString("vcMessage");
		this.msgType = eMsgTypes.valueOf(rs.getString("vcMsgType"));
		this.tsMsgTime = rs.getDate("tsMsgTime");
	}
	
}
