package com.wjf.github;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class NettyProperties {

	public static final int head_version;
	public static final int length_offset;
	public static final int head_length;
	public static final int length_count;
	public static final long reader_idle_time;
	public static final long writer_idle_time;
	public static final long all_idle_time;
	public static final TimeUnit time_unit;

	static {
		InputStream resource = NettyProperties.class.getClassLoader().getResourceAsStream("netty.properties");
		Properties properties = new Properties();
		try {
			properties.load(resource);
		} catch (IOException e) {
			e.printStackTrace();
		}
		head_version = Integer.parseInt(properties.getProperty("head_version"));
		length_offset = Integer.parseInt(properties.getProperty("length_offset"));
		head_length = Integer.parseInt(properties.getProperty("head_length"));
		length_count = Integer.parseInt(properties.getProperty("length_count"));
		reader_idle_time = Long.parseLong(properties.getProperty("reader_idle_time"));
		writer_idle_time = Long.parseLong(properties.getProperty("writer_idle_time"));
		all_idle_time = Long.parseLong(properties.getProperty("all_idle_time"));
		time_unit=TimeUnit.valueOf(properties.getProperty("time_unit"));
	}
}
