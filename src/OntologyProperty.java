

import java.util.ArrayList;

public class OntologyProperty {
	//This JAVA-class define a property of an instance in the Ontology
	private String name;
	private ArrayList<String> types;
	private ArrayList<OntologyAttribute> attributes;
	
	public OntologyProperty(String name, ArrayList<String> types, ArrayList<OntologyAttribute> attributes) {
		super();
		this.name = name;
		this.types = types;
		this.attributes = attributes;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<String> getTypes() {
		return types;
	}
	public void setTypes(ArrayList<String> types) {
		this.types = types;
	}
	public ArrayList<OntologyAttribute> getAttributes() {
		return attributes;
	}
	public void setAttributes(ArrayList<OntologyAttribute> attributes) {
		this.attributes = attributes;
	}
	
	
	public boolean isAnObjectProperty(){
		boolean result = false;
		for (int i = 0; i < this.getTypes().size();i++){
			if (this.getTypes().get(i).equals("owl:ObjectProperty")){
				result = true;
				break;
			}
		}
		return result;
	}
	
	public boolean isADataTypeProperty(){
		boolean result = false;
		for (int i = 0; i < this.getTypes().size();i++){
			if (this.getTypes().get(i).equals("owl:DatatypeProperty")){
				result = true;
				break;
			}
		}
		return result;
	}
	
	public String getNameWithoutPrefix() {
		String[] arraySplittate = this.name.split(":");
		return arraySplittate[1];
	}
	
	}
	
