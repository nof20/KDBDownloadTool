package KDBDownloadTool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import KDBDownloadTool.c.KException;

public class KDBDownloadTool {

	final static Logger log = Logger.getLogger("KDBDownloadTool");
	
	public static void main(String[] args) {
		
		String kdbHost;
		Integer kdbPort;
		c c = null;
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
		
		// Connect to KDB or fail fast

		kdbHost = props.getProperty("kdbHost");
		kdbPort = Integer.parseInt(props.getProperty("kdbPort"));
		log.info("Connecting to KDB at "+kdbHost+":"+kdbPort);
		try {
			c = new c(kdbHost, kdbPort);
			c.k(".u.upd:upsert");
		} catch (KException e) {
			log.severe("A KException was thrown:");
			e.printStackTrace();
		} catch (IOException e) {
			log.severe("An IOException was thrown:");
			e.printStackTrace();
		}
		
		switch (builderType) {
		case "Wikipedia":
			Wikipedia.WikipediaDownloader w = new Wikipedia.WikipediaDownloader(props, c);
			w.run();
			break;
			
		}
		

		

	}
}
