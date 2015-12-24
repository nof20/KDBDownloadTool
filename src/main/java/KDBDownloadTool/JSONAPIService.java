package KDBDownloadTool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import com.google.gson.Gson;

public class JSONAPIService {
	
	final static Logger log = Logger.getLogger("JSONAPIService");
	static Gson gson = new Gson();
		
	public static Object getObject(String URLString, Class<?> cl) {
		log.info("About to poll URL: "+URLString);
		Object o = null;
		try {
			URL url = new URL(URLString);
			URLConnection uc = url.openConnection();
			//uc.setRequestProperty("Accept-Charset", java.nio.charset.StandardCharsets.UTF_8.name());
			//uc.setRequestProperty("Accept", "application/json");
			//uc.setRequestProperty("User-Agent", "KDBDownloadTool (http://github.com/nof20/KDBDownloadTool)");
			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String line = in.readLine();
			log.info("Received: "+line);
			o = gson.fromJson(line, cl);
		} catch (MalformedURLException e) {
			log.severe(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			log.severe(e.getMessage());
			e.printStackTrace();
		}
		return o;
	}
}
