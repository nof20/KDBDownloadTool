package KDBDownloadTool;

import java.util.Properties;

public abstract class Downloader {
	
	Properties props;
	c c;
	
	public Downloader (Properties props, c c) {
		this.props = props;
		this.c = c;
	}
	
	public abstract void run();
	
}
