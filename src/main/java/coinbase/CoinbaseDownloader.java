package coinbase;

import java.util.Properties;
import java.util.logging.Logger;

import KDBDownloadTool.c;

public class CoinbaseDownloader implements Runnable {

	final static Logger log = Logger.getLogger("WikipediaDownloader");

	String baseURLString;
	String start;
	String end;
	String granularity;
	String finalURLString;
	String kdbTable;
	c c;
	
	CoinbaseDownloader(Properties props, c c) {
		baseURLString = props.getProperty("coinbase.baseURLString");
		start = props.getProperty("coinbase.start");
		end = props.getProperty("coinbase.end");
		granularity = props.getProperty("coinbase.granularity"); // in seconds
		kdbTable = props.getProperty("coinbase.kdbTable");
		finalURLString = baseURLString+"?start="+start+"&end="+end+"&granularity="+granularity;
		this.c = c;
	}
	
	public void run() {
		
		
	}

}
