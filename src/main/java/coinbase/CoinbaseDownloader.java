package coinbase;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import KDBDownloadTool.Downloader;
import KDBDownloadTool.c;

public class CoinbaseDownloader extends Downloader {

	final static Logger log = Logger.getLogger("WikipediaDownloader");
	static String GRANULARITY = "60"; // i.e., minutes

	String baseURLString;
	String startString;
	String endString;
	String finalURLString;
	String kdbTable;
	c c;
	
	public CoinbaseDownloader(Properties props, c c) {
		super(props, c);
		baseURLString = props.getProperty("coinbase.baseURLString");
		startString = props.getProperty("coinbase.start");
		endString = props.getProperty("coinbase.end");
		// granularity = props.getProperty("coinbase.granularity");
		kdbTable = props.getProperty("coinbase.kdbTable");
		this.c = c;
	}
	
	public void run() {
		// Check KDB table exists
		try {
			c.k("$[`"+kdbTable+" in key `.;;("+kdbTable+":([time:`datetime$()]low:`float$();high:`float$();open:`float$();close:`float$();volume:`float$());)]");
		} catch (Exception e1) {
			log.severe("Caught an error checking for KDB table:");	
			e1.printStackTrace();
		} 
		
		// Check number of points to be returned
		Instant startInstant = Instant.parse(startString);
		Instant endInstant = Instant.parse(endString);
		Long minutes = Duration.between(startInstant, endInstant).toMinutes(); // Implicitly rounded down
		log.info(minutes+" minutes time period configured");
		Long minsElapsed = (long) 0;
		
		while (minsElapsed < minutes) {
			Long sectionDuration;
			Instant sectionStart = startInstant.plus(Duration.ofMinutes(minsElapsed));
			if ((minutes - minsElapsed) > 199) {
				sectionDuration = (long) 199;
			} else {
				sectionDuration = minutes - minsElapsed;
			}
			Instant sectionEnd = sectionStart.plus(Duration.ofMinutes(sectionDuration));
			log.info("Processing "+sectionStart.toString()+" to "+sectionEnd.toString());
			
			finalURLString = baseURLString+"?start="+sectionStart.toString()+"&end="+sectionEnd.toString()+"&granularity="+GRANULARITY;
			Double[][] results = (Double[][]) KDBDownloadTool.JSONAPIService.getObject(finalURLString, Double[][].class);
			try {
				Iterator<Double[]> it = Arrays.asList(results).iterator();
				while (it.hasNext()) {
					Double[] values = it.next();
					// Parse date
					Long epoch = 1000*Math.round(values[0]); // Decimal SECONDS since epoch, not millis
					java.util.Date d = new java.util.Date(epoch);
					// Send to KDB
					//log.info("About to send "+Arrays.toString(values)+" (timestamp "+d.toString()+")");
					Object[] data = new Object[] {d, values[1], values[2], values[3], values[4], values[5]};
					Object[] updArray = new Object[] {".u.upd", kdbTable, data};
					c.ks(updArray);
				}
				log.info(results.length+" records sent to KDB");
			} catch (Exception e) {
				log.severe(e.getMessage());
				e.printStackTrace();
			}
			
			minsElapsed += sectionDuration;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.severe("Interrupted during sleep to avoid rate limit: ");
				e.printStackTrace();
			}
		}
	}
}
