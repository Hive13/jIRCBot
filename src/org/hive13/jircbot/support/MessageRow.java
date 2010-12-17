package org.hive13.jircbot.support;

import java.sql.ResultSet;
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
	
	public MessageRow(ResultSet rs) {
		
	}
	
}
