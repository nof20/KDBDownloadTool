package Wikipedia;

import java.util.Properties;

public class WikipediaDownloader {
	
	String baseURLString;
	String pageName;
	String frequency;
	String startDate;
	String endDate;
	String finalURLString;
	
	public WikipediaDownloader(Properties props) {
		baseURLString = props.getProperty("wikipedia.baseURLString");
		pageName = props.getProperty("pageName");
		frequency = props.getProperty("frequency");
		startDate = props.getProperty("startDate"); // NB preserve String type for URL
		endDate = props.getProperty("endDate");
		finalURLString = baseURLString + "/" + pageName + "/" + frequency + "/" + startDate + "/" + endDate;
	}
	
	public void run() {
		ItemList il = (ItemList) KDBDownloadTool.JSONAPIService.getObject(finalURLString, ItemList.class);
		
	}
	
}
