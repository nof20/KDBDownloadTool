package KDBDownloadTool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import com.google.gson.Gson;

public class JSONAPIService {
	
	final static Logger log = Logger.getLogger("JSONAPIService");
	static Gson gson = new Gson();
		
	public static Object getObject(String URLString, Class<?> cl) {
		log.info("About to poll URL: "+URLString);
		HttpURLConnection uc;
		Object o = null;
		Boolean suppressIOException = false;
		try {
			URL url = new URL(URLString);
			//uc.setRequestProperty("Accept-Charset", java.nio.charset.StandardCharsets.UTF_8.name());
			//uc.setRequestProperty("Accept", "application/json");
			//uc.setRequestProperty("User-Agent", "KDBDownloadTool (http://github.com/nof20/KDBDownloadTool)");
			uc = (HttpURLConnection) url.openConnection();
			if (uc.getResponseCode() == 429) {
				log.severe("Received HTTP response code 429 (Too Many Requests); backing off");
				suppressIOException = true;
				Thread.sleep(5000);
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			StringBuffer sb = new StringBuffer();
			while (in.ready()) {
				sb.append(in.readLine());
			}
			log.info("Received: "+sb.toString());
			o = gson.fromJson(sb.toString(), cl);
		} catch (MalformedURLException e) {
			log.severe(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			if (! suppressIOException) {
				log.severe(e.getMessage());
				e.printStackTrace();
			}
		} catch (InterruptedException e) {
			log.severe(e.getMessage());
			e.printStackTrace();
		}
		return o;
	}
}
