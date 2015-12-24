package KDBDownloadTool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;

import com.google.gson.Gson;

public class KDBDownloadTool {

	final static Logger log = Logger.getLogger("KDBDownloadTool");
	
	public static void main(String[] args) {
		
		String builderType;
		
		log.info("Starting up KDB Download Tool...");
		// Load logging properties
		try {
			FileInputStream fis =  new FileInputStream("logging.properties");
			LogManager.getLogManager().readConfiguration(fis);
		    fis.close();
		} catch (FileNotFoundException e) {
			log.severe("Cannot find logging properties file: "+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			log.severe("Caught IOException trying to load logging properties: "+e.getMessage());
			e.printStackTrace();
		}
		
		// Load properties
		Properties props = new Properties();
		try {
			props.load(new FileInputStream("KDBDownloadTool.properties"));
		} catch (FileNotFoundException e) {
			log.severe("Cannot find properties file: "+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			log.severe("Caught IOException trying to load properties: "+e.getMessage());
			e.printStackTrace();
		}
		builderType = props.getProperty("builderType");
		
		switch (builderType) {
		case "Wikipedia":
			Wikipedia.WikipediaDownloader w = new Wikipedia.WikipediaDownloader(props);
			break;
			
		}
		
		
		
		final String baseURLString = "https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/en.wikipedia/all-access/all-agents";
		final String pageName = "Bitcoin";
		final String frequency = "daily";
		final String startDate = "20150601";
		final String endDate = "20151215";
		final String kdbTable = "Wikipedia";
		final String kdbHost = "localhost";
		final Integer kdbPort = 2001;
		
		Gson gson = new Gson();
		c c = new c();
		
		ItemList il = new ItemList();
		

		
		// Connect to KDB, add table if not already present, then save data
		try {
			c = new c(kdbHost, kdbPort);
			c.k("$[`Wikipedia in key `.;;(Wikipedia:([project:`$();article:`$();granularity:`$();timestamp:`datetime$();access:`$();agent:`$()]vws:`long$());)]");
			c.k(".u.upd:upsert");
			Iterator<Item> it = il.items.iterator();
			while (it.hasNext()) {
				Item i = it.next();
				log.info("About to send "+i.toString());
				// Parse date
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				String shortDateString = i.timestamp.substring(0, 8);
				Date date = sdf.parse(shortDateString);
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(i.timestamp.substring(8, 10)));
				cal.setTime(date);
				// Send to KDB
				Object[] data = new Object[] {i.project, i.article, i.granularity, date, i.access, i.agent, i.views};
				Object[] updArray = new Object[] {".u.upd", kdbTable, data};
				c.ks(updArray);
			}
			c.close();
		} catch (Exception e) {
			log.severe(e.getMessage());
			e.printStackTrace();
		}
	}
}
