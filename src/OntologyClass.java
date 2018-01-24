

import java.util.ArrayList;


public class OntologyClass {
	//This JAVA-class define the "Class" of an Ontology
	private String name;
	private ArrayList<String> types;
	private ArrayList<OntologyAttribute> attributes;
	
	public OntologyClass(String name,  ArrayList<String> types, ArrayList<OntologyAttribute> attributes) {
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
	

	public ArrayList<OntologyClass> getParentClasses(ArrayList<OntologyClass> oc){	
		//takes in input a list of class; 
		//it search for the parents of this.class 
		//it returns an array of OntologyClass matched with the list of class taken in input
		ArrayList<OntologyAttribute> attribute_result = new ArrayList<OntologyAttribute>();
		ArrayList<OntologyClass> class_result = new ArrayList<OntologyClass>();
		if (this.getAttributes().size()!=0){
		for (int i = 0; i < this.getAttributes().size(); i++){
			if (this.getAttributes().get(i).getName().equals("rdfs:subClassOf") &&
					!this.getAttributes().get(i).getValue().equals("owl:Thing")){
				attribute_result.add(this.getAttributes().get(i));
			}
		}
		}
		
			for (int j = 0; j < attribute_result.size(); j++){
				for (int k = 0; k < oc.size(); k++){
					if (attribute_result.get(j).getValue().equals(oc.get(k).getName())){
						class_result.add(oc.get(k));
					}
				}
			
		}
			/* TESTING THE NUMBER OF PARENTS AND THE FIRST PARENT
			System.out.println("parsing"+this.getName()+" - "+attribute_result.size() + " - " + class_result.size());
			if (class_result.size()!=0){
				System.out.println("    "+class_result.get(0).getName());
			}
			*/
		return class_result;
	}
	
	public int getNumberOfParentClasses(){	
		//not used
		//TODO: can be used for code optimization, possible mismatching with attribute/class results, must be tested!
		ArrayList<OntologyAttribute> attribute_result = new ArrayList<OntologyAttribute>();
	
		if (this.getAttributes().size()!=0){
		for (int i = 0; i < this.getAttributes().size(); i++){
			if (this.getAttributes().get(i).getName().equals("rdfs:subClassOf") &&
					!this.getAttributes().get(i).getValue().equals("owl:Thing")){
				attribute_result.add(this.getAttributes().get(i));
			}
		}
		}
		
	return attribute_result.size();
	
	}

	public String getNameWithoutPrefix() {
		String[] arraySplittate = this.name.split(":");
		return arraySplittate[1];
	}
	
	public String getLabel(){
		String result = "";
		for (int i = 0; i < attributes.size(); i++){
			if (attributes.get(i).getName().equals("rdfs:label")){
				result = attributes.get(i).getValue();
			}
		}
		return result.replaceAll("\"","");
	}
}

