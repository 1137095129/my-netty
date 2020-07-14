package com.wjf.github;

import io.netty.channel.Channel;

import java.io.Serializable;

public class ChannelAndToken implements Serializable {
	private String token;
	private Channel channel;

	public ChannelAndToken() {
	}

	public ChannelAndToken(String token, Channel channel) {
		this.token = token;
		this.channel = channel;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
}
