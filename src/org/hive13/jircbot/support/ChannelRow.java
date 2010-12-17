package org.hive13.jircbot.support;

public class ChannelRow {
	public int pk_ChannelID;
	public String vcServer, vcChannel;
	public ChannelRow(int pk_ChannelID, String vcServer, String vcChannel) {
		this.pk_ChannelID = pk_ChannelID;
		this.vcServer = vcServer;
		this.vcChannel = vcChannel;
	}
	public ChannelRow() {}
}
