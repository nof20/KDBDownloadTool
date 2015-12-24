package wikipedia;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import KDBDownloadTool.Downloader;
import KDBDownloadTool.c;

public class WikipediaDownloader extends Downloader {

	final static Logger log = Logger.getLogger("WikipediaDownloader");
	
	String baseURLString;
	String pageName;
	String frequency;
	String startDate;
	String endDate;
	String finalURLString;
	String kdbTable;
	c c;
	
	public WikipediaDownloader(Properties props, c c) {
		super(props, c);
		baseURLString = props.getProperty("wikipedia.baseURLString");
		pageName = props.getProperty("wikipedia.pageName");
		frequency = props.getProperty("wikipedia.frequency");
		startDate = props.getProperty("wikipedia.startDate"); // NB preserve String type for URL
		endDate = props.getProperty("wikipedia.endDate");
		kdbTable = props.getProperty("wikipedia.kdbTable");
		finalURLString = baseURLString + "/" + pageName + "/" + frequency + "/" + startDate + "/" + endDate;
		this.c = c;
	}
	
	public void run() {
		ItemList il = (ItemList) KDBDownloadTool.JSONAPIService.getObject(finalURLString, ItemList.class);
		// Connect to KDB, add table if not already present, then save data
		try {
			// NB Table name is parameterized, but schema isn't, because mapping to POJO must be maintained
			c.k("$[`"+kdbTable+" in key `.;;("+kdbTable+":([project:`$();article:`$();granularity:`$();timestamp:`datetime$();access:`$();agent:`$()]vws:`long$());)]");
			Iterator<Item> it = il.list.iterator();
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
		} catch (Exception e) {
			log.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	
}
