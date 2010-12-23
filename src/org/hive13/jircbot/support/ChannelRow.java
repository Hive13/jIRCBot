package org.hive13.jircbot.support;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ChannelRow {
	public int pk_ChannelID;
	public String vcServer, vcChannel;
	public ChannelRow(int pk_ChannelID, String vcServer, String vcChannel) {
		this.pk_ChannelID = pk_ChannelID;
		this.vcServer = vcServer;
		this.vcChannel = vcChannel;
		
	}
	public ChannelRow() {}
	public ChannelRow(ResultSet rs) throws SQLException {
		this(rs, true);
	}
	public ChannelRow(ResultSet rs, boolean validateNames) throws SQLException {
		if(validateNames || jIRCTools.isValidColumn(rs, "pk_ChannelID"))
            this.pk_ChannelID = rs.getInt("pk_ChannelID");
		if(validateNames || jIRCTools.isValidColumn(rs, "vcServer"))
            this.vcServer = rs.getString("vcServer");
		if(validateNames || jIRCTools.isValidColumn(rs, "vcChannel"))
            this.vcChannel = rs.getString("vcChannel");
	}
}
