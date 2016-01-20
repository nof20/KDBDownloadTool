package alchemyDataNews;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import KDBDownloadTool.Downloader;
import KDBDownloadTool.c;
import KDBDownloadTool.c.KException;

public class AlchemyDataNewsDownloader extends Downloader {

	final static Logger log = Logger.getLogger("AlchemyDataNewsDownloader");
	
	String baseURLString;
	String topic;
	String startString;
	String endString;
	String finalURLString;
	String kdbTable;
	String apiKeyFilename;
	String apiKey;
	String RETURN_FIELDS;
	c c;
	
	public AlchemyDataNewsDownloader(Properties props, KDBDownloadTool.c c) {
		super(props, c);
		baseURLString = props.getProperty("alchemyDataNewsDownloader.baseURLString");
		topic = props.getProperty("alchemyDataNewsDownloader.topic");
		startString = props.getProperty("alchemyDataNewsDownloader.start"); // NB preserve String type for URL
		endString = props.getProperty("alchemyDataNewsDownloader.end");
		apiKeyFilename = props.getProperty("alchemyDataNewsDownloader.apiKeyFilename");
		kdbTable = props.getProperty("alchemyDataNewsDownloader.kdbTable");
		RETURN_FIELDS = "enriched.url.title,enriched.url.url,enriched.url.publicationDate,enriched.url.enrichedTitle.docSentiment";

		// Load API key from filename
		try {
			FileReader fr = new FileReader(apiKeyFilename);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while((line = br.readLine()) != null) { // i.e., if there's two, take the last
                apiKey = line;
            } 
			br.close();
		} catch (FileNotFoundException e) {
			log.severe("API Key file ("+apiKeyFilename+") not found.");
			e.printStackTrace();
		} catch (IOException e) {
			log.severe("Caught IOExcpetion trying to open API Key file "+apiKeyFilename+":");
			e.printStackTrace();
		}
		
		// Set start/date times
		Instant startInstant = Instant.parse(startString);
		Instant endInstant = Instant.parse(endString);
		Long startEpoch = Math.round((double) startInstant.toEpochMilli() / 1000L);
		Long endEpoch = Math.round((double) endInstant.toEpochMilli() / 1000L);
		
		// Build final URL
		finalURLString = baseURLString + "?apikey=" + apiKey + "&return=" + RETURN_FIELDS + "&start=" + startEpoch + "&end=" + endEpoch + "&q.enriched.url.cleanedTitle=" + topic + "&outputMode=json&count=100";
		this.c = c;
	}

	@Override
	public void run() {
		// Connect to KDB, add table if not already present
		// NB Table name is parameterized, but schema isn't, because mapping to POJO must be maintained
		// Empty type in creation of URL, ID column etc. - String.toCharArray creates KDB string (undocumented!)
		try {
			c.k("$[`"+kdbTable+" in key `.;;("+kdbTable+":([]date:`date$();time:`time$();id:();docSentimentMixed:`boolean$();docSentimentScore:`float$();docSentimentType:`$();title:();url:();timestamp:`datetime$());)]");
		} catch (KException e1) {
			log.severe("Caught KException whilst checking for presence of table:");
			e1.printStackTrace();
		} catch (IOException e1) {
			log.severe("Caught IOException while checking for presence of table:");
			e1.printStackTrace();
		}

		Boolean hasNextPage = true;
		String nextPageClause = "";
		while (hasNextPage) {
			AlchemyNewsResponse anr = (AlchemyNewsResponse) KDBDownloadTool.JSONAPIService.getObject(finalURLString+nextPageClause, AlchemyNewsResponse.class);
			try {
				// Loop over results on this page
				Iterator<Document> it = anr.result.docs.iterator();
				log.info("Processing "+anr.result.docs.size()+" objects...");
				while (it.hasNext()) {
					Document d = it.next();
					// Map explicit data types
					Boolean docSentimentMixed = (d.source.enriched.url.enrichedTitle.docSentiment.mixed == 1) ? true : false;
					Object sqlDate;
					Object sqlTime;
					try {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
						Date publicationDate = sdf.parse(d.source.enriched.url.publicationDate.date);
						sqlDate = new java.sql.Date(publicationDate.getTime()); // Date component only
						sqlTime = new java.sql.Time(publicationDate.getTime()); // Time component only
					} catch (ParseException e) {
						log.info("Publication date '"+d.source.enriched.url.publicationDate.date+"' invalid, substituting NULL");
						sqlDate = KDBDownloadTool.c.NULL('d');
						sqlTime = KDBDownloadTool.c.NULL('t');
					}
					Date timestamp = new Date(1000L * d.timestamp);
									
					// Send to KDB
					Object[] data = new Object[] {sqlDate, sqlTime, d.id.toCharArray(), docSentimentMixed, d.source.enriched.url.enrichedTitle.docSentiment.score, d.source.enriched.url.enrichedTitle.docSentiment.type, d.source.enriched.url.title.toCharArray(), d.source.enriched.url.url.toCharArray(), timestamp};
					Object[] updArray = new Object[] {".u.upd", kdbTable, data};
					c.ks(updArray);
				}
			} catch (Exception e) {
				log.severe(e.getMessage());
				e.printStackTrace();
			}
			// Test for next page and set clause as appropriate
			if ((anr.next != null) && (anr.status != "ERROR")) {
				log.info("Results have more pages, continuing");
				hasNextPage = true;
				nextPageClause = "&next="+anr.next;
			} else {
				hasNextPage = false;
			}
		}
	}
}
