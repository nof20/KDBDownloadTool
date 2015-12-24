package wikipedia;

public class Item {
	
	String project;
	String article;
	String granularity;
	String timestamp;
	String access;
	String agent;
	Long views;
	
	Item (String project, String article, String granularity, String timestamp, String access, String agent, Long views) {
		this.project = project;
		this.article = article;
		this.granularity = granularity;
		this.timestamp = timestamp;
		this.access = access;
		this.agent = agent;
		this.views = views;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[Project "+project+", ");
		sb.append("article "+article+", ");
		sb.append("granularity "+granularity+", ");
		sb.append("timestamp "+timestamp+", ");
		sb.append("access "+access+", ");
		sb.append("agent "+agent+", ");
		sb.append("views "+views+"]");
		return sb.toString();
	}
}
