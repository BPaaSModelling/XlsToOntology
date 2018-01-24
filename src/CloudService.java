import java.util.ArrayList;

public class CloudService {

	String name;
	ArrayList<CloudServiceProperty> properties;
	
	public CloudService() {
		this.properties = new ArrayList<CloudServiceProperty>();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<CloudServiceProperty> getProperties() {
		return properties;
	}
	public void setProperties(ArrayList<CloudServiceProperty> properties) {
		this.properties = properties;
	}
	
}
