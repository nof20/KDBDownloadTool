package coinbase;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import KDBDownloadTool.Downloader;
import KDBDownloadTool.c;

public class CoinbaseDownloader extends Downloader {

	final static Logger log = Logger.getLogger("WikipediaDownloader");

	String baseURLString;
	String start;
	String end;
	String granularity;
	String finalURLString;
	String kdbTable;
	c c;
	
	public CoinbaseDownloader(Properties props, c c) {
		super(props, c);
		baseURLString = props.getProperty("coinbase.baseURLString");
		start = props.getProperty("coinbase.start");
		end = props.getProperty("coinbase.end");
		granularity = props.getProperty("coinbase.granularity"); // in seconds
		kdbTable = props.getProperty("coinbase.kdbTable");
		finalURLString = baseURLString+"?start="+start+"&end="+end+"&granularity="+granularity;
		this.c = c;
	}
	
	public void run() {
		Double[][] results = (Double[][]) KDBDownloadTool.JSONAPIService.getObject(finalURLString, Double[][].class);
		// Connect to KDB, add table if not already present, then save data
		try {
			// NB Table name is parameterized, but schema isn't, because mapping to POJO must be maintained
			c.k("$[`"+kdbTable+" in key `.;;("+kdbTable+":([time:`datetime$()]low:`float$();high:`float$();open:`float$();close:`float$();volume:`float$());)]");
			Iterator<Double[]> it = Arrays.asList(results).iterator();
			while (it.hasNext()) {
				Double[] values = it.next();
				// Parse date
				Long epoch = 1000*Math.round(values[0]); // Decimal SECONDS since epoch, not millis
				java.util.Date d = new java.util.Date(epoch);
				// Send to KDB
				log.info("About to send "+Arrays.toString(values)+" (timestamp "+d.toString()+")");
				Object[] data = new Object[] {d, values[1], values[2], values[3], values[4], values[5]};
				Object[] updArray = new Object[] {".u.upd", kdbTable, data};
				c.ks(updArray);
			}
		} catch (Exception e) {
			log.severe(e.getMessage());
			e.printStackTrace();
		}
		
	}

}
