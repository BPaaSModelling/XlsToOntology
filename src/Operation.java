import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.formula.functions.Value;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.microsoft.schemas.office.visio.x2012.main.CellType;

public class Operation {

	public static int maxcount = 0;
	private ArrayList<OntologyClass> classes; // it contains all the classes of
	// the ontology
	private ArrayList<String> null_values_string;

	private HashMap<String, OntologyClass> APQC;
	private HashMap<String, OntologyClass> fbpdo;
	private HashMap<String, OntologyClass> bpaas;
	private HashMap<String, OntologyClass> questionnaire;

	public ArrayList<OntologyClass> getClasses() {
		return classes;
	}

	public void setClasses(ArrayList<OntologyClass> classes) {
		this.classes = classes;
	}

	public ArrayList<OntologyProperty> getProperties() {
		return properties;
	}

	public void setProperties(ArrayList<OntologyProperty> properties) {
		this.properties = properties;
	}

	public ArrayList<OntologyInstance> getInstances() {
		return instances;
	}

	public void setInstances(ArrayList<OntologyInstance> instances) {
		this.instances = instances;
	}

	public ArrayList<String> getOntologyPreamble() {
		return ontologyPreamble;
	}

	public void setOntologyPreamble(ArrayList<String> ontologyPreamble) {
		this.ontologyPreamble = ontologyPreamble;
	}

	public ArrayList<CloudService> getServices() {
		return services;
	}

	public void setServices(ArrayList<CloudService> services) {
		this.services = services;
	}

	public ArrayList<String> getNull_values_string() {
		return null_values_string;
	}

	public void setNull_values_string(ArrayList<String> null_values_string) {
		this.null_values_string = null_values_string;
	}

	private ArrayList<OntologyProperty> properties; // it contains all the
	// properties of the
	// ontology
	private ArrayList<OntologyInstance> instances; // it contains all the
	// instances of the ontology
	private ArrayList<String> ontologyPreamble;
	private ArrayList<CloudService> services;

	public Operation() {
		super();
		classes = new ArrayList<OntologyClass>();
		properties = new ArrayList<OntologyProperty>();
		instances = new ArrayList<OntologyInstance>();
		APQC= new HashMap<String, OntologyClass>() ;
		fbpdo= new HashMap<String, OntologyClass>() ;
		bpaas= new HashMap<String, OntologyClass>() ;
		questionnaire= new HashMap<String, OntologyClass>() ;

		ontologyPreamble = new ArrayList<String>();
		services = new ArrayList<CloudService>();
		null_values_string = new ArrayList<String>();
		null_values_string.add("N/A");
		null_values_string.add("not_specified");
		null_values_string.add("not specified");
		null_values_string.add("not defined");
		null_values_string.add("Not specified");
		null_values_string.add("");
	}

	public int[] parseOntology(String path_file) {// this method parse the
		// ontology
		// takes in input the path of the file and it fills the four arrays
		// stored this java-class (classes, properties, instances and the
		// ontologyPreamble)
		// returns an array with the number of classes, properties and instances
		// loaded
		String line = null;
		FileReader reader = null;

		try {
			reader = new FileReader(path_file);
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());

		}
		Scanner scanner = new Scanner(reader);
		// Set the variable "startPreamble" to detect when the preamble finish
		boolean preamble = true;
		ArrayList<String> temp_type = null;
		String temp_name = null;
		ArrayList<OntologyAttribute> temp_attributes = null;
		boolean titleLine = true;
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();

			if (preamble) {
				// here we are in the preamble
				if (!line.startsWith(".")) {
					ontologyPreamble.add(line);
				} else {
					// here we are in the first dot
					// where the ontology elements starts
					preamble = false;

				}
				// here we are still in the preamble
				// where the line doesn't start with "."

			} else {
				// here we are not in the preamble and we start to analyse
				// the elements of the ontology
				if (titleLine && !line.startsWith(".")) {
					// first line of the object of the ontology
					// here we can find prefix:name of the object
					// and we instantiate all the data structures
					temp_type = new ArrayList<String>();
					temp_name = new String();
					temp_attributes = new ArrayList<OntologyAttribute>();

					temp_name = line;
					titleLine = false;
				} else if (!titleLine && line.trim().startsWith(".")) {
					// end of the object of ontology
					titleLine = true;
					for (int t = 0; t < temp_type.size(); t++) {
						if (temp_type.get(t).equals("owl:Class")) {

							// Parsing a Class
							OntologyClass c = new OntologyClass(temp_name, temp_type, temp_attributes);
							if (c.getName().contains("bpaas:")) {
								bpaas.put(c.getName(), c);
								//System.out.println(c.getName()+" to bpaas");
							}else if(c.getName().contains("fbpdo:")){
								fbpdo.put(c.getName(), c);
								//System.out.println(c.getName()+" to fbpdo");
							}else if(c.getName().contains("apqc#")){
								APQC.put(c.getName(), c);
								//System.out.println(c.getName()+" to APQC");
							}

							classes.add(c);
							break;
						} else if (temp_type.get(t).equals("owl:AnnotationProperty")
								|| temp_type.get(t).equals("owl:DatatypeProperty")
								|| temp_type.get(t).equals("owl:DeprecatedProperty")
								|| temp_type.get(t).equals("owl:FunctionalProperty")
								|| temp_type.get(t).equals("owl:ObjectProperty")) {
							// Parsing a Property
							OntologyProperty p = new OntologyProperty(temp_name, temp_type, temp_attributes);
							properties.add(p);
							break;

						} else if (t == temp_type.size() - 1) {
							// Parsing an Istance
							OntologyInstance i = new OntologyInstance(temp_name, temp_type, temp_attributes);
							OntologyClass c = new OntologyClass(temp_name, temp_type, temp_attributes);
							if (i.getName().contains("bpaas:")) {
								bpaas.put(i.getName(), c);
								//System.out.println(i.getName()+" to bpaas");
							}else if(i.getName().contains("fbpdo:")){
								fbpdo.put(i.getName(), c);
								//System.out.println(i.getName()+" to fbpdo");
							}else if(c.getName().contains("apqc#")){
								APQC.put(i.getName(), c);
								//System.out.println(i.getName()+" to APQC");
							}
							instances.add(i);
						}
					}

				} else {
					// body of the object of the ontology
					String[] arraySplittate = parseAttributeName(line);
					if (arraySplittate[0].equals("rdf:type")) {
						temp_type.add(arraySplittate[1].replaceAll(";", "").trim());
					} else {
						// parsing attributes
						OntologyAttribute oa;
						String[] arraySplittate2 = arraySplittate[1].trim().split("\\^\\^"); // splitting
						// the
						// string
						// with
						// "^^"
						// so
						// we
						// can
						// define
						// type
						// and
						// value
						if (arraySplittate2.length == 2) { // if the split has
							// type and value
							oa = new OntologyAttribute(arraySplittate[0].replaceAll(";", "").trim(),
									arraySplittate2[1].replaceAll(";", "").trim(),
									arraySplittate2[0].replaceAll("\"", "").replaceAll(";", "").trim()); // name,type,value

						} else {
							oa = new OntologyAttribute(arraySplittate[0].replaceAll(";", "").trim(), "",
									arraySplittate2[0].replaceAll(";", "").trim()); // name,type,value
						}
						temp_attributes.add(oa);
					}
				}

			} // end not preamble

		} // end while scanner
		scanner.close();

		// -------------TESTING THE WHOLE ONTOLOGY-------------
		/*
		 * System.out.println("Number of lines of preamble: " +
		 * ontologyPreamble.size()); System.out.println("Number of classes: "
		 * +classes.size()); for (int i = 0; i < classes.size(); i++){
		 * System.out.print("    "
		 * +classes.get(i).getPrefix()+":"+classes.get(i).getName());
		 * System.out.print(" - Type: "); for(int
		 * j=0;j<classes.get(i).getTypes().size();j++){
		 * System.out.print(classes.get(i).getTypes().get(j)+" ,"); }
		 * System.out.println(""); } System.out.println("Number of properties: "
		 * +properties.size()); for (int i = 0; i < properties.size(); i++){
		 * System.out.print("    "
		 * +properties.get(i).getPrefix()+":"+properties.get(i).getName());
		 * System.out.print(" - Type: "); for(int
		 * j=0;j<properties.get(i).getTypes().size();j++){
		 * System.out.print(properties.get(i).getTypes().get(j)+" ,"); }
		 * System.out.println(""); } System.out.println("Number of instances: "
		 * +instances.size()); for (int i = 0; i < instances.size(); i++){
		 * System.out.print("    "
		 * +instances.get(i).getPrefix()+":"+instances.get(i).getName());
		 * System.out.print(" - Type: "); for(int
		 * j=0;j<instances.get(i).getTypes().size();j++){
		 * System.out.print(instances.get(i).getTypes().get(j)+" ,"); }
		 * System.out.println(""); }
		 */

		// --------------------------TESTING THE CLASSES OF
		// ONTOLOGY---------------------
		/*
		 * System.out.println("Classes:"); for (int i = 0; i < classes.size();
		 * i++){ System.out.println("   Name: "
		 * +classes.get(i).getPrefix()+":"+classes.get(i).getName());
		 * System.out.println("     Types:"); for (int j = 0; j <
		 * classes.get(i).getTypes().size();j++){ System.out.println("       "
		 * +classes.get(i).getTypes().get(j)); } System.out.println(
		 * "     Attributes:"); for (int j = 0; j <
		 * classes.get(i).getAttributes().size(); j++){ System.out.println(
		 * "       name: "+classes.get(i).getAttributes().get(j).getName());
		 * System.out.println("       type: "
		 * +classes.get(i).getAttributes().get(j).getType());
		 * System.out.println("       value: "
		 * +classes.get(i).getAttributes().get(j).getValue());
		 * System.out.println("       -------"); }
		 * System.out.println("--------------------"); }
		 * 
		 * //--------------------------TESTING THE PROPERTIES OF
		 * ONTOLOGY---------------------
		 * 
		 * System.out.println("Property:"); for (int i = 0; i <
		 * properties.size(); i++){ System.out.println("   Name: "
		 * +properties.get(i).getPrefix()+":"+properties.get(i).getName());
		 * System.out.println("     Types:"); for (int j = 0; j <
		 * properties.get(i).getTypes().size();j++){ System.out.println(
		 * "       "+properties.get(i).getTypes().get(j)); } System.out.println(
		 * "     Attributes:"); for (int j = 0; j <
		 * properties.get(i).getAttributes().size(); j++){ System.out.println(
		 * "       name: "+properties.get(i).getAttributes().get(j).getName());
		 * System.out.println("       type: "
		 * +properties.get(i).getAttributes().get(j).getType());
		 * System.out.println("       value: "
		 * +properties.get(i).getAttributes().get(j).getValue());
		 * System.out.println("       -------"); }
		 * System.out.println("--------------------"); }
		 * 
		 * //--------------------------TESTING THE PROPERTIES OF
		 * ONTOLOGY--------------------- System.out.println("Instances:"); for
		 * (int i = 0; i < instances.size(); i++){ System.out.println(
		 * "   Name: "
		 * +instances.get(i).getPrefix()+":"+instances.get(i).getName());
		 * System.out.println("     Types:"); for (int j = 0; j <
		 * instances.get(i).getTypes().size();j++){ System.out.println("       "
		 * +instances.get(i).getTypes().get(j)); } System.out.println(
		 * "     Attributes:"); for (int j = 0; j <
		 * instances.get(i).getAttributes().size(); j++){ System.out.println(
		 * "       name: "+instances.get(i).getAttributes().get(j).getName());
		 * System.out.println("       type: "
		 * +instances.get(i).getAttributes().get(j).getType());
		 * System.out.println("       value: "
		 * +instances.get(i).getAttributes().get(j).getValue());
		 * System.out.println("       -------"); }
		 * System.out.println("--------------------"); }
		 */
		return new int[] { classes.size(), properties.size(), instances.size() };

	}// end method

	private String[] parseAttributeName(String attribute_line) { // this method
		// parse the
		// name-value
		// of an
		// Ontology
		// attribute
		attribute_line.replaceAll(";", "").trim(); // we remove the final ";"
		String[] arraySplittate = attribute_line.trim().split(" ");
		String temp_value = "";
		for (int i = 1; i < arraySplittate.length; i++) {
			temp_value = temp_value + " " + arraySplittate[i].trim();
		}
		return new String[] { arraySplittate[0], temp_value };
	}

	public void parseExcelFile(String path_file) {
		Workbook workbook;

		try {
			workbook = WorkbookFactory.create(new File(path_file));
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				// parsing the rows
				Row row = rowIterator.next();
				CloudService cs = new CloudService();
				Iterator<Cell> cellIterator = row.cellIterator();
				int t = 0;
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					if (row.getRowNum() >0 ){
						//System.out.println("READING CELL COL " + cell.getColumnIndex()+ " ROW "+ cell.getRowIndex() +" WITH VALUE: " + cell.toString());
					}



					if (row.getRowNum() > 0){

						if (true) {
							maxcount = row.getLastCellNum();
							String validated="questionnaire:Not_Specified";
							switch (cell.getColumnIndex()) {

							case 0:
								cs.setName(cell.toString());
								//System.out.println("Name of cloud service: "+cs.getName());
								break;

								// here i am parsing provider
							case 1:
								if (!validateNullCellString(cell.toString())) {
									//cs.properties.add(new CloudServiceProperty("bpass:cloudServiceHasProvider",
									//	"\""+String.valueOf(cell.toString()) + "\" ;"));
								}
								break;

							case 2:
								// here i am parsing cloud service has description
								if (!validateNullCellString(cell.toString())) {
									String desc=String.valueOf(cell.toString());
									desc=desc.replace("\n", "");
									desc=desc.replace("(", "");
									desc=desc.replace(")", "");
									desc=desc.replace(" ", "_");
									desc=desc.replaceAll("\\W+","");
									desc=desc.replace("\"","");
									{
										cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasDescription", "\""+desc + "\" ;"));
									}
								}
								break;	

							case 6:
								// here I am parsing APQC category
								if (!validateNullCellString(cell.toString())) {

									String cellValues=cell.toString();

									cellValues=cellValues.replace(" ","_");
									cellValues=cellValues.replace(".","_");

									ArrayList<String> validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split(("(?<=,_[0-9])"))));

									for (int i=0; i<validatedAl.size();i++) {

										validated=validatedAl.get(i);
										String cutted= validated.substring(validated.length()-1);

										validated= addAPQCNumber(validated);


										ArrayList<String> APQCGerarchy= addAPQCGerarchy(validated);
										for (int j=0; j<APQCGerarchy.size();j++) {
											cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAPQC", APQCGerarchy.get(j) +" ;"));	
										}
										//cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAPQC", validated +" ;"));
										//System.out.println(cell.toString()+" -------------------------------------->"+validated );
										//								
									}

								}
								break;

							case 7:

								String cellValues=cell.toString();
								cellValues=cellValues.replace(" ","");

								ArrayList<String> validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));



								for (int i = 0; i < validatedAl.size(); i++) {
									validated= validateFbpdo(validatedAl.get(i));

									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAction", "fbpdo:"+ validated +" ;"));

								}

								// here I am parsing action class
								//								ArrayList<String> matchedClasses_forAction;// = new ArrayList<String>();
								//								
								//								matchedClasses_forAction = getMatchedClasses(validateFbpdo(cell.toString()));
								//								
								//								for (int i = 0; i < matchedClasses_forAction.size(); i++) {
								//									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAction",(validateString(matchedClasses_forAction.get(i))+" ;")));
								//								}

								break;
							case 8:

								cellValues=cell.toString();
								cellValues=cellValues.replace(" ","");

								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								for (int i = 0; i < validatedAl.size(); i++) {
									validated= validateFbpdo(validatedAl.get(i));
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasObject", "fbpdo:"+ validated +" ;"));

								}

								break;

							case 9://ok
								cellValues=cell.toString();
								//cellValues=cellValues.replace(" ","");

								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								for (int i = 0; i < validatedAl.size(); i++) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasPaymentPlan", validateString(validatedAl.get(i)) +" ;"));

								}


								break;

							case 10://ok
								// Here I am parsing additional costs
								//								if (cell.toString() != null) {
								//									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAdditionalCosts",
								//											validateBooleanCellString(String.valueOf(cell.toString()))));
								//								}
								break;

							case 11:
								cellValues=cell.toString();

								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								ArrayList<String> elL= new  ArrayList<String>();
								elL.add("bpaas:SSL");
								elL.add("bpaas:HIPAA");
								elL.add("bpaas:SHA256");
								
								ArrayList<String> elM= new  ArrayList<String>();
								elM.add("bpaas:SOX");
								elM.add("bpaas:FDA");
								elM.add("bpaas:ISO27001");
								elM.add("bpaas:IAAS");
								elM.add("bpaas:PSN");
								elM.add("bpaas:FIPS");
								
								ArrayList<String> elH= new  ArrayList<String>();
								elH.add("bpaas:AES");
								elH.add("bpaas:TLS");
								elH.add("bpaas:TLS_VPN");
								elH.add("bpaas:Ipsec");
								
								if (validatedAl.size()==1 && validateString(validatedAl.get(0)).equals("questionnaire:Not_Specified") ) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasStoredDataLocation", validated +" ;"));
									//System.out.println("###############"+cell.toString()+"###### "+validated);
								}else {
									for (int i=0;i<validatedAl.size();i++) {
										String validating=validateString(validatedAl.get(i).toString());
										if (elL.contains(validating)) {
											cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasEncryptionLevel", "bpaas:Low" +" ;"));
											//System.out.println(validating+"---------------------> bpaas:Low");
										}else if (elM.contains(validating)) {
											cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasEncryptionLevel", "bpaas:Medium " +" ;"));
											//System.out.println(validating+"---------------------> bpaas:Medium");
										}else if (elH.contains(validating)) {
											cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasEncryptionLevel", "bpaas:Medium " +" ;"));
											//System.out.println(validating+"---------------------> bpaas:High");
										}else {
											System.out.println(validating+" not mapped");
										}
									}
								}


								//								for (int i = 0; i < validatedAl.size(); i++) {
								//									validated= validateString(validatedAl.get(i));
								//									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasEncryptionType", validated +" ;"));
								//									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								//								}
								//						
								break;

							case 12: //cloudServiceHasStoredDataLocation

								cellValues=cell.toString();

								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								ArrayList<String> sd= new  ArrayList<String>();
								sd.add("bpaas:Finland");
								sd.add("bpaas:The_Netherlands");
								sd.add("bpaas:Germany");
								sd.add("bpaas:Italy");
								sd.add("bpaas:France");
								sd.add("bpaas:Austria");
								sd.add("bpaas:Spain");
								sd.add("bpaas:France");
								sd.add("bpaas:Denmark");

								Boolean foundEu=false;

								if (validatedAl.size()==1 && validateString(validatedAl.get(0)).equals("questionnaire:Not_Specified") ) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasStoredDataLocation", validated +" ;"));
									//System.out.println("###############"+cell.toString()+"###### "+validated);
								}else {
									//System.out.println("###############"+cell.toString()+"######");
									boolean found=false;

									for (int i=0;i<validatedAl.size();i++) {

										String validating=validateString(validatedAl.get(i).toString());
										//System.out.println("Currently validating: "+validating);
										if (sd.contains(validating)) {
											found=true;
											//System.out.println("Found ="+ found);
										}else {
											if (validating.equals("bpaas:Europe")){
												foundEu=true;
												//System.out.println("FoundUE ="+ foundEu);
											}
										}
									}
									if (!foundEu && !found) {
										//System.out.println("not in/not europe [cell value=] "+cellValues.toString());

									}else {
										if (!foundEu && found) {
											cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasStoredDataLocation", "bpaas:Europe" +" ;"));
											//System.out.println(" -------------------------------------->added "+"bpaas:Europe");
										}
									}
									for (int i=0 ;i<validatedAl.size();i++) {

										validated=validateString(validatedAl.get(i).toString());
										cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasStoredDataLocation", validated +" ;"));
										//System.out.println(" -------------------------------------->"+validated);


									}

								}

								break;

							case 13:
								validated=validateBooleanCellString(cell.toString());
								if (cell != null) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasSecurityStandardInPlace", validated + " ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								}
								break;

								// automated password management system

							case 14:

								// Here I am parsing password management system
								// automated

								if (cell != null ) {
									validated=validateBooleanCellString(cell.toString());
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAutomatedPasswordManagmentSystem",	validated + " ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								}

								break;

							case 15:
								// performanceManagementSystem in place

								if (cell != null ) {
									validated=validateBooleanCellString(cell.toString());
									cs.properties.add(new CloudServiceProperty( "bpaas:cloudServiceHasPerformanceManagementSystemInPlace", validated + " ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								}

								break;

							case 16:
								// different performance plan available

								if (cell != null ) {
									validated=validateBooleanCellString(cell.toString());
									cs.properties.add(new CloudServiceProperty( "bpaas:cloudServiceHasDifferentPerformancePlanAvailable", validated + " ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								}
								break;

							case 17://ok
								// response time in millisecond

								if (cell != null) {
									validated=validateNumeric(cell).toString();
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasResponseTime_in_ms",
											validated + " ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);	
								}
								break;

							case 18: //ok
								// computing processing power power scalable
								if (cell != null) {
									validated=validateBooleanCellString(cell.toString());
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasScalableProcessingPower", validated+ " ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);	
								}
								break;

							case 19://ok
								// data storage scalable

								if (cell != null) {
									validated=validateBooleanCellString(cell.toString());
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasScalableStorage",validated + " ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								}
								break;

							case 20://ok
								// here i am parsing data storage in BG

								if (cell!= null) {
									validated=validateNumeric(cell).toString();
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasDataStorageInGB",validated + " ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								}

								break;

							case 21:

								// Here I am parsing simultaneous user
								if (cell !=null) {
									validated = validateNumeric(cell);									
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasSimultaneousUsers", validated + " ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								}
								break;

							case 22:
								if (cell !=null) {
									validated = validateAvailability(cell.toString()).toString();

									//cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAvailabilityInPercent", validated + " ;"));


									if (validated.contains("bpaas:")||validated.contains("questionnaire:")) {
										System.out.println("no downtime set");

									}else {
										cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasDowntimePerMonthInMin", validated+ " ;"));
										//System.out.println(cell.toString()+" -------------------------------------->"+validated);
									}
								}					
								break;

							case 23:
								// Here I am parsing Access Log Availability in
								// months
								if (cell !=null) {
									validated = validateNumeric(cell);									
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAccessLogAvailabilityInMonths", validated + " ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								}					

								break;

							case 24:
								// Here I am parsing Access Log Retention Period in
								// months
								if (cell !=null) {
									validated = validateNumeric(cell);									
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAccessLogRetentionPeriodInMonths", validated + " ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								}	
								break;

							case 25:
								// here i am parsing AuditLogAvailabilityin Months
								if (cell !=null) {
									validated = validateNumeric(cell);									
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAuditLogAvailabilityInMonth", validated + " ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								}	
								break;

							case 26:
								// here i am parsing AuditLogRetentionPeriodinMonths 
								if (cell !=null) {
									validated = validateNumeric(cell);									
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAuditLogRetentionPeriodInMonths", validated + " ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								}	
								break;

							case 27: //to discuss
								cellValues=cell.toString();
								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								ArrayList<String> bf= new  ArrayList<String>();
								bf.add("Five_years");
								bf.add("Longer_than_1_year");
								bf.add("Up_to_1_year");
								bf.add("Up_to_6_months");
								bf.add("Up_to_2_month");
								bf.add("Monthly");
								bf.add("Up_to_1_month");
								bf.add("Up_to_2_weeks");
								bf.add("twenty8_days");
								bf.add("Weekly");
								bf.add("Up_to_1_week");
								bf.add("Daily");
								bf.add("Up_to_1_day");
								bf.add("Hourly");

								if (validatedAl.size()==1 && validateString(validatedAl.get(0)).equals("questionnaire:Not_Specified") ) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasBackupRetentionTime", validated +" ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								}else {

									boolean found=false;
									int highest=0;
									for (int i=0;i<validatedAl.size();i++) {
										for(int j=0; j<bf.size();j++) {

											String validating=validateString(validatedAl.get(i).toString());
											String currentR="bpaas:"+bf.get(j);
											//System.out.println(validating+" "+currentR);
											if (currentR.equals(validating)&& highest<=j ) {
												found=true;
												highest=j;
												//System.out.println("new highest"+highest);
												//System.out.println(validating+" "+currentR);
											}

										}
									}
									if (!found) {
										//System.out.println("not found: "+validateString(validatedAl.toString()));
										cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasBackupRetentionTime", validateString(validatedAl.toString()) +" ;"));
										//System.out.println(cell.toString()+" -------------------------------------->"+validated);
									}else {
										//System.out.println("highest=" + highest);
										for (int i=highest ;i<bf.size();i++) {
											validated=validateString(bf.get(i));
											cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasBackupRetentionTime", validated +" ;"));
											//System.out.println(cell.toString()+" -------------------------------------->"+validated);
										}	
									}

								}
								//
								//
								break;


							case 28: //to discuss
								// here I am parsing BackupRetentionTime
								cellValues=cell.toString();
								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								ArrayList<String> br= new  ArrayList<String>();
								br.add("Five_years");
								br.add("Longer_than_1_year");
								br.add("Up_to_1_year");
								br.add("Up_to_6_months");
								br.add("Up_to_2_month");
								br.add("Up_to_1_month");
								br.add("Monthly");
								br.add("Up_to_four_weeks");
								br.add("Up_to_two_weeks");
								br.add("twenty8_days");
								br.add("Up_to_1_week");
								br.add("Up_to_1_day");

								if (validatedAl.size()==1 && validateString(validatedAl.get(0)).equals("questionnaire:Not_Specified") ) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasBackupRetentionTime", validated +" ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								}else {

									boolean found=false;
									int highest=0;
									for (int i=0;i<validatedAl.size();i++) {

										for(int j=0; j<br.size();j++) {

											String validating=validateString(validatedAl.get(i));

											String currentR="bpaas:"+br.get(j);
											//		System.out.println(validating+" "+currentR);
											if (currentR.equals(validating)&& highest<=j ) {
												found=true;
												highest=j;
												//System.out.println("new highest"+highest);
												//System.out.println(validating+" "+currentR);
											}

										}
									}
									if (!found) {
										//System.out.println(cell.toString()+"cell to string");
										System.out.println("not found: "+validateString(validatedAl.toString()));
									}else {
										//System.out.println("highest=" + highest);
										for (int i=highest ;i<br.size();i++) {
											validated=validateString(br.get(i));
											cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasBackupRetentionTime", validated +" ;"));
											//System.out.println(cell.toString()+" -------------------------------------->"+validated);
										}	
									}

								}


								break;
								//

								//								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));
								//
								//								for (int i = 0; i < validatedAl.size(); i++) {
								//									validated=validateString(validatedAl.get(i));
								//									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasBackupRetentionTime", validated +" ;"));
								//									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								//								}
								//
								//
								//								break;

							case 29: //ok
								//MediaType Data Export Import format

								cellValues=cell.toString();

								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								for (int i = 0; i < validatedAl.size(); i++) {
									validated=validateString(validatedAl.get(i));
									cs.properties.add(new CloudServiceProperty("bpaas:CloudServiceHasMediaTypeImportExport", validated +" ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								}

								break;
								//	case 30 is useless, merged into 29

							case 31: //ok
								// data migration

								if (cell != null) {
									validated=validateBooleanCellString(cell.toString());
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasDataMigration", validated+ " ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								} 
								break;

							case 32:
								// API //ok
								if (cell != null) {
									validated=validateBooleanCellString(cell.toString());
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAPI", validated+ " ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								} 
								break;

							case 33://ok cloudServiceHasServiceSupportResponsiveness

								cellValues=cell.toString();

								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								ArrayList<String> r= new  ArrayList<String>();
								r.add("At_most_15_minutes");
								r.add("At_most_30_minutes");
								r.add("At_most_40_minutes");
								r.add("At_most_1_hour");
								r.add("At_most_1_5_hours");
								r.add("At_most_2_hours");
								r.add("At_most_3_hours");
								r.add("At_most_4_hours");
								r.add("At_most_5_hours");
								r.add("At_most_6_hours");
								r.add("At_most_8_hours");
								r.add("At_most_1_working_day");
								r.add("At_most_12_hours");
								r.add("At_most_13_hours");
								r.add("At_most_16_hours");
								r.add("At_most_2_working_days");
								r.add("At_most_24_hours");
								r.add("At_most_3_working_days");
								r.add("At_most_4_working_days");
								r.add("At_most_40_hours");
								r.add("At_most_5_working_days");
								r.add("At_most_50_hours");
								r.add("At_most_7_working_days");
								r.add("At_most_120_hours");
								r.add("At_most_1_month");
								r.add("Up_to_two_weeks");
								r.add("Up_to_four_weeks");

								if (validatedAl.size()==1 && validateString(validatedAl.get(0)).equals("questionnaire:Not_Specified") ) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasServiceSupportResponsiveness", validated +" ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								}else {

									boolean found=false;
									int highest=0;
									for (int i=0;i<validatedAl.size();i++) {
										for(int j=0; j<r.size();j++) {

											String validating=validateString(validatedAl.get(i).toString());
											String currentR="bpaas:"+r.get(j);
											//System.out.println(validating+" "+currentR);
											if (currentR.equals(validating)&& highest<=j ) {
												found=true;
												highest=j;
												//System.out.println("new highest"+highest);
												//System.out.println(validating+" "+currentR);
											}

										}
									}
									if (!found) {
										System.out.println("not found: "+validateString(validatedAl.toString()));
									}else {
										for (int i=highest ;i<r.size();i++) {
											validated=validateString(r.get(i));
											cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasServiceSupportResponsiveness", validated +" ;"));
											//System.out.println(cell.toString()+" -------------------------------------->"+validated);
										}	
									}

								}


								break;

							case 34://ok
								// service support
								cellValues=cell.toString();
								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								ArrayList<String> ss= new  ArrayList<String>();
								ss.add("Twenty4Seven");
								ss.add("SevenDaysAWeek");
								ss.add("Mon-Sun");
								ss.add("Mon-Sat");
								ss.add("Mon-Fri");
								ss.add("Twenty4five");
								ss.add("EightThirtytToFive");
								ss.add("NineToFive");

								if (validatedAl.size()==1 && validateString(validatedAl.get(0)).equals("questionnaire:Not_Specified") ) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasServiceSupport", validated +" ;"));
									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								}else {

									boolean found=false;
									int highest=0;
									for (int i=0;i<validatedAl.size();i++) {
										for(int j=0; j<ss.size();j++) {

											String validating=validateString(validatedAl.get(i).toString());
											String currentR="bpaas:"+ss.get(j);
											//System.out.println(validating+" "+currentR);
											if (currentR.equals(validating)&& highest<=j ) {
												found=true;
												highest=j;
												//System.out.println("new highest"+highest);
												//System.out.println(validating+" "+currentR);
											}

										}
									}
									if (!found) {
										System.out.println("not found: "+validateString(validatedAl.toString()));
									}else {
										for (int i=highest ;i<ss.size();i++) {
											validated=validateString(ss.get(i));
											cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasServiceSupport", validated +" ;"));
											//System.out.println(cell.toString()+" -------------------------------------->"+validated);
										}	
									}

								}


								break;
								//

								//								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));
								//
								//								for (int i = 0; i < validatedAl.size(); i++) {
								//									validated= validateString(validatedAl.get(i));
								//									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasServiceSupport", validated +" ;"));
								//									//System.out.println(cell.toString()+" -------------------------------------->"+validated);
								//								}
								//								
								//								break;

							case 35://ok
								// SupportChannel
								cellValues=cell.toString();

								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								for (int i = 0; i < validatedAl.size(); i++) {
									validated=validateString(validatedAl.get(i));
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasSupportChannel", validated +" ;"));
									//System.out.println(cellValues+" -------------------------------------->"+validated);
								}
								break;

							case 36://ok
								// TargetMarket
								cellValues=cell.toString();

								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								for (int i = 0; i < validatedAl.size(); i++) {
									validated= validateString(validatedAl.get(i));
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasTargetMarket",	validated +" ;"));
									//System.out.println(cellValues+" -------------------------------------->"+validated);
								}

								break;

							}

						}
					}			
				}
				if (cs.getName() != null) {
					services.add(cs);
				}
			}

		} catch (EncryptedDocumentException e) {

			e.printStackTrace();
		} catch (InvalidFormatException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private Float validateAvailability(String cell) {
		cell=cell.trim();

		if (cell.equals("not_specified")|| cell.equals("Not Specified") ||cell.equals("N/A") ||cell.equals("not specfied") || cell.equals("Not specified")|| cell.equals("not_specified") || cell.toString().equals("Not_specified") || cell.equals("") ||cell.toString().equals(" ") || cell.toString().equals(null) || null_values_string.contains(cell.toString())) {
			return (float)0;
		}else {
			Float newValidated;
			String validated=cell.replace("%","");
			newValidated= Float.valueOf(validated);

			if (newValidated <= (float) 100.0) {
				if (newValidated <=(float)1.0) {
					newValidated=newValidated*(float)100;
				}
				newValidated=((float) 100.0 - newValidated)*(float)43200;
			}else {
				System.out.println("no downtime set--> "+ cell.toString());
			}


			return newValidated;

		}

	}

	private ArrayList<String> addAPQCGerarchy(String validated) {

		ArrayList<String> validatingAPQC=new ArrayList<String>(Arrays.asList(validated.split(("(?<=_[0-9])"))));
		ArrayList<String> APQCGerarchy= new ArrayList<String>();

		String parent=validatingAPQC.get(0).replace("<http://ikm-group.ch/archimeo/apqc#", "").replace("_",".");
		APQCGerarchy.add(parent);
		//System.out.println("Gerarchy "+parent);
		int size=validatingAPQC.size();

		for (int i=1; i<size-2;i++) {
			parent=parent+validatingAPQC.get(i).replace("_",".");

			//System.out.println("Gerarchy "+ parent);
			APQCGerarchy.add(parent);
		}
		//System.out.println(validated);
		//System.out.println(APQCGerarchy.toString());

		ArrayList<String>  APQClist=getAPQCfromGerarchy(APQCGerarchy);

		return APQClist;
	}

	private ArrayList<String> getAPQCfromGerarchy(ArrayList<String> APQCGerarchy) {
		ArrayList<String> matchingList=new ArrayList<String>();
		//System.out.println(APQCGerarchy);
		//System.out.println(APQC);

		for (Entry<String, OntologyClass> entry : APQC.entrySet()) {
			OntologyClass APQCClass = entry.getValue();

			ArrayList<OntologyAttribute> attributes = APQCClass.getAttributes();
			//System.out.println(attributes);
			for (int i=0; i<attributes.size();i++) {
				OntologyAttribute attributeI = attributes.get(i);
				if (attributeI.getName().equals("apqc:hasHierarchyID")) {
					String relativeValue = attributeI.getValue().replace("\"","");
					for (int k=0; k<APQCGerarchy.size();k++) {
						if (relativeValue.equals(APQCGerarchy.get(k))) {
							//System.out.println("Entry:"+entry.getKey());
							matchingList.add(entry.getKey());
						}	
					}	
				}				
			}
		}		
		//System.out.println(matchingList);
		return matchingList;
	}

	private boolean validateNullCellString(String cell) {
		//		boolean found = false;
		//		for (int i = 0; i < this.null_values_string.size(); i++) {
		//			// if it is true, then you set found as true
		//			if (cell.equals(null_values_string.get(i))) {
		//				found = true;
		//			}
		//		}
		return this.null_values_string.contains(cell.trim());
	}

	private String validateNumeric(Cell cell) {

		String validated="";
		//System.out.println(cell.toString());
		if (cell.toString().equals("not specified") ||cell.toString().equals("N/A") ||cell.toString().equals("not specfied") || cell.toString().equals("Not Specified")|| cell.toString().equals("not_specified") || cell.toString().equals("Not_specified") || cell.equals("") ||cell.toString().equals(" ") || cell.toString().equals(null) || null_values_string.contains(cell.toString())) {
			return "questionnaire:Not_Specified";}
		else if (cell.toString().equals("No")||cell.toString().equals("no")||cell.toString().equals("NO")) {
			return "questionnaire:No";
		}else if (cell.toString().equals("Yes")||cell.toString().equals("yes")||cell.toString().equals("YES")|| cell.toString().matches(".*[^a-z].*") ) {
			return "questionnaire:Yes";
		}	else {
			validated=cell.toString().replace("%"," ");
			Float newValidated= Float.valueOf(validated);
			if (newValidated <= (float) 100.0) {
				if (newValidated <=(float)1.0) {
					newValidated=newValidated*(float)100;
				}
				newValidated=((float) 100.0 - newValidated)*(float)43200;
			}else {
				//System.out.println("no downtime set--> "+ cell.toString());
			}


			//return cell.toString();
			//System.out.println("cell.toString():"+cell.toString() );
			return "questionnaire:Yes";


		}
	}

	private String validateBooleanCellString(String cell) {
		boolean result;
		if (cell.equals("not specified") ||cell.equals("Not Specified") ||cell.equals("not specfied") || cell.equals("Not specified")|| cell.equals("not_specified") || cell.equals("Not_specified") || cell.equals("") ||cell.equals(" ") || cell.equals(null) || null_values_string.contains(cell.toString())) {
			return "questionnaire:Not_Specified";
		} else if (cell.equals("Any")||cell.equals("Any")) {
			return "questionnaire:Any";
		} else if (cell.equals("No")||cell.equals("Not")|| cell.equals("no") || cell.equals("not")) {
			return "questionnaire:No";
		} else if (cell.equals("Yes")||cell.equals("yes")) {
			return "questionnaire:Yes";
		} else
			if (checkMatching(cell.toString())) {
				return cell.toString();	
			}else {
				return "questionnaire:Not_specified";
			}


	}

	private String validateString(String cell) {
		cell=cell.replace("(","");
		cell=cell.replace(")","");
		cell=cell.replace("[","");
		cell=cell.replace("]","");
		cell=cell.trim();
		cell=cell.replace(" ", "_");
		String start=(String) cell.subSequence(0, 1);
		cell = cell.substring(0,1).toUpperCase() + cell.substring(1);


		boolean result;
		if (cell.equals("not_specified") || cell.equals("NotSpecified")||cell.equals("Not_Specified")||cell.equals("Not_defined") || cell.equals("Not_specified")|| cell.equals("not_specified") || cell.equals("Not_specified") || cell.equals("") ||cell.equals("_") || cell.equals(null) || null_values_string.contains(cell.toString())) {
			return "questionnaire:Not_Specified";
		} else if (cell.equals("Any")||cell.equals("Any")) {
			return "questionnaire:Any";
		} else if (cell.equals("No")||cell.equals("Not")|| cell.equals("no") || cell.equals("not") || cell.equals("None")|| cell.equals("none")) {
			return "questionnaire:No";
		} else if (cell.equals("Yes")||cell.equals("yes")) {
			return "questionnaire:Yes";

			//PAYMENT PLAN
		}else if (cell.equals("Resource_based_pricing")) {
			return "bpaas:Resourcebasedpricing";
		}else if (cell.equals("Try_Free_First")) {
			return "bpaas:TryFreeFirst";
		}else if (cell.equals("Prepaid_Annual_Plan")) {
			return "bpaas:PrepaidAnnualPlan";
		}else if (cell.equals("Monthly_Fee")) {
			return "bpaas:MonthlyFee";
		}else if (cell.equals("Customizable_Plan")) {
			return "bpaas:CustomizablePlan";
		}else if (cell.equals("Free_of_Charge")) {
			return "bpaas:FreeofCharge";
		}else if (cell.equals("Monthly_Fee")) {
			return "bpaas:MonthlyFee";
		}else if (cell.equals("Any")||cell.equals("Any")) {
			return "questionnaire:Any";
		}else if (cell.equals("FixedSubscription")) {
			return "bpaas:Fixed_Subscription";
		}else if (cell.equals("InitialBaseFee")) {
			return "bpaas:bpaas:Initial_Base_Fee";
		}else if (cell.equals("Per-terrabyte")) {
			return "bpaas:Per-terabyte";


			//Media type
		}else if (cell.equals("XML")) {
			return "bpaas:Xml";
		}else if (cell.equals("RDF")) {
			return "bpaas:Rdf";
		}else if (cell.equals("HTML")) {
			return "bpaas:Html";

			//ENCRYPTION TYPE
		}else if (cell.equals("TLS_VPN")) {
			return "bpaas:TLS_VPN";
		}else if (cell.equals("IP_Filtering")) {
			return "bpaas:IP_Filtering";
		} else if (cell.equals("SSL_Secure_Sockets_Layer")) {
			return "bpaas:SSL";
		} else if (cell.equals("ISO:27001")) {
			return "bpaas:ISO27001";
		}  

		//service support responsiveness
		else if (cell.equals("5_hours")) {
			return "bpaas:At_most_5_hours";
		}else if (cell.equals("15_minutes")||cell.equals("At_most_15_minutes")) {
			return "bpaas:At_most_15_minutes";
		}else if (cell.equals("At_most_30_working_days")) {
			return "bpaas:At_most_1_month";
		}else if (cell.equals("At_most_1.5_hours")) {
			return "bpaas:At_most_1_5_hours";
		}else if (cell.equals("30_minutes")) {
			return "bpaas:At_most_30_minutes";
		}else if (cell.equals("Twenty8_days")) {
			return "bpaas:twenty8_days";
		}



		//channels
		else if (cell.equals("On-site")) {
			return "bpaas:On_Site_Support";
		}
		else if (cell.equals("Online_ticketing")) {
			return "bpaas:Online_Ticketing";
		}


		//STORED DATA LOCATION
		else if (cell.equals("European_Economic_Area_EEA")) {
			return "bpaas:Europe";


			//SERVICE SUPPORT
		}else if (cell.equals("At_most_1_working_days")) {
			return "bpaas:At_most_1_working_day";
		} else if (cell.equals("24-7")) {
			return "bpaas:Twenty4Seven";
		}else if (cell.equals("7_days_a_week")) {
			return "bpaas:SevenDaysAWeek";
		}else if (cell.equals("24-5")) {
			return "bpaas:Twenty4five";
		}else if (cell.equals("9_AM_-_5_PM")) {
			return "bpaas:NineToFive";
		}else if (cell.equals("8:30-17:00")) {
			return "bpaas:EightThirtyToFive";
		}

		//Target Market 
		else if (cell.equals("Culture/Archeology")) {
			return "bpaas:CultureArcheology";
		}else if (cell.equals("No_Target")) {
			return "bpaas:NoTarget";
		}else if (cell.equals("Business")) {
			return "bpaas:Businesses";
		}else if (cell.equals("Public_sector")) {
			return "bpaas:Public_Sector";
		}

		//BACKUP FREQUENCY
		else if (cell.equals("30_days")) {
			return "bpaas:Monthly";
		}

		//BackupRetentionTime
		else if (cell.equals("5_years")) {
			return "bpaas:Five_years";
		}else if (cell.equals("12.0")) {
			return "bpaas:Up_to_1_year";
		}else if (cell.equals("28_days")) {
			return "bpaas:twenty8_days";
		}  



		else {

			String validated="bpaas:"+cell.toString().replace(" ","_");
			Boolean found=checkMatching(validated);
			if (found) {
				return validated;	
			}else {
				System.out.println(cell.toString());
				return "missingbpaas:" + cell.toString();
			}

		}
	}

	private String validateFbpdo(String cell) {
		cell=cell.trim();
		cell=cell.replace(" ", "_");
		String start=(String) cell.subSequence(0, 1);
		cell = cell.substring(0,1).toUpperCase() + cell.substring(1);
		cell=cell.replace("(","");
		cell=cell.replace(")","");
		cell=cell.replaceAll("\\d" ,"");

		boolean result;
		if (cell.equals("not specified") ||cell.equals("not specfied") || cell.equals("Not specified")|| cell.equals("not_specified") || cell.equals("Not_specified") || cell.equals("") ||cell.equals(" ") || cell.equals(null) || null_values_string.contains(cell.toString())) {
			return "questionnaire:Not_Specified";
		} else if (cell.equals("Any")||cell.equals("Any")) {
			return "questionnaire:Any";
		} else if (cell.equals("No")||cell.equals("Not")|| cell.equals("no") || cell.equals("not")) {
			return "questionnaire:No";
		} else if (cell.equals("Yes")||cell.equals("yes")) {
			return "questionnaire:Yes";
		} else {
			String validated=cell.toString();


			checkMatching("fbpdo:"+validated);
			return validated;
		}
	}

	//	private ArrayList<String> getMatchedClasses(String label) {
	//		ArrayList<String> result = new ArrayList<String>();
	//		String[] sublabels = label.split(", ");
	//
	//		for (int i = 0; i < sublabels.length; i++) {
	//			boolean found = false;
	//			for (int j = 0; j < classes.size(); j++) {
	//				if (label.trim().equals(classes.get(j).getLabel())) {
	//					result.add(classes.get(j).getName());
	//					found = true;
	//				}
	//
	//			}
	//			if (!found) {
	//				System.out.println("WARNING > " + sublabels[i] + " CLASS NOT FOUND!");
	//			}
	//		}
	//
	//		return result;
	//	}
	//
	//	private ArrayList<String> getMatchedInstances(String label) {
	//		ArrayList<String> result = new ArrayList<String>();
	//		String[] sublabels = label.split(",");
	//		for (int i = 0; i < sublabels.length; i++) {
	//			boolean found = false;
	//			for (int j = 0; j < instances.size(); j++) {
	//				if (sublabels[i].trim().equals(instances.get(j).getLabel())) {
	//					//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"+sublabels[i].trim().equals(instances.get(j).getLabel()));
	//					result.add(instances.get(j).getName());
	//					found = true;
	//				}
	//
	//			}
	//			if (!found) {
	//				System.out.println("WARNING > " + sublabels[i] + " INSTANCE NOT FOUND!");
	//			}
	//		}
	//
	//		return result;
	//	}

	private String addAPQCNumber(String validated) {

		boolean found = false;

		String validatedOld=validated;
		validated=validated.trim();
		validated=validated.replace("_"," ");
		validated=validated.replaceAll("\\d" ,"");
		//System.out.println("validated----------------"+validated);

		for (int j = 0; j < classes.size(); j++) {

			String name=classes.get(j).getLabel();

			ArrayList<OntologyAttribute> attributeList = classes.get(j).getAttributes();
			//System.out.println(name);

			if (validated.contains(name)) {
				for (int i=0; i<attributeList.size();i++) {
					if(attributeList.get(i).getName().equals("apqc:hasPCFID") && !found) {

						String validatedName=classes.get(j).getName();
						//System.out.println("found label :"+ validatedName);
						validated=validatedName;
						//	System.out.println("found :"+ validated);
						found=true;
					}	
				}

			}

		}
		if (!found) {
			System.out.println("APQC WARNING " + validatedOld +"  " + validated + " CLASS NOT FOUND!");
		}

		return validated;
	}
	private boolean checkMatching(String validated) {

		boolean found = false;

		String validatedOld=validated;
		//		validated=validated.replace("_"," ");
		//		validated=validated.replaceAll("\\d" ,"");
		//		validated=validated.trim();

		if (APQC.containsKey(validated)) {
			found=true;
		}else if(fbpdo.containsKey(validated)) {
			found=true;
			//System.out.println("fbpdo----------------"+fbpdo.toString());
		}else if (bpaas.containsKey(validated)) {
			found=true;
		}else
			System.out.println("WARNING Check Matching> " + validatedOld + " CLASS NOT FOUND!");

		return found;
	}
}
