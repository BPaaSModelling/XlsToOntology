import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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

		// TODO: REORDER AND ADJUCT CASES TO THE NEW ONTOLOGY
		// The functional requirements should be linked to fbpdo
		// The non functional has to be linked to bpaas
		// any appearence of yes, no, not specified can be linked to
		// questionnaire:Yes, questionnaire:No, questionnaire:Not_specified
		// use the excel file "mapping for the updated excel" in folder
		// Resources\REFERENCE as main reference.
		// AN EXAMPLE CLOUDSERVICE is available in bdata:Drive_1 or Gmail_1
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


					//TODO: IF THE FIRST ROW IS THE MENU, IT SHOULD NOT SCAN IT AND ADD IT TO THE ONTOLOGY. CHECK IF THE CELL A1 AND B1, IF IT'S THE MENU, SKIP THE LINE
					//TODO: bpass:cloudServiceHasProvider Bridgeway Security Solutions. THIS SHOULD BE bpass:cloudServiceHasProvider "Bridgeway Security Solutions" SAME FOR LABEL AND ALL THE TEXTUALS ATTRIBUTES
					if (row.getRowNum() > 0){

						if (true) {
							maxcount = row.getLastCellNum();

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
									desc=desc.replace("", "");
									{
										cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasDescription",
												"\""+desc + "\" ;"));
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


										if (i==0) {
											String validated=validatedAl.get(i);
											String cutted= validated.substring(validated.length()-1);

											//|| cutted =="2" ||cutted =="3" ||cutted =="4" ||cutted =="5" ||cutted =="6" ||cutted =="7" ||cutted =="8" ||cutted =="9"
											if (cutted.contains("1")||cutted.contains("2")||cutted.contains("3")||cutted.contains("4")||cutted.contains("5")||cutted.contains("6")||cutted.contains("7")||cutted.contains("8")||cutted.contains("9")) {

												validated="<http://ikm-group.ch/archimeo/apqc#"+validated.substring(0, validated.length()-3);
												cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAPQC", validated +"> ;"));

											}else {
												//if there's only one apqc
												validated="<http://ikm-group.ch/archimeo/apqc#"+validated;
												cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAPQC", validated +"> ;"));
											}
										}else {

											String validated=validatedAl.get(i);

											String cutted= validated.substring(validated.length()-1);
											String prevCutted= validatedAl.get(i-1).substring(validatedAl.get(i-1).length()-1);

											if (cutted.contains("1")||cutted.contains("2")||cutted.contains("3")||cutted.contains("4")||cutted.contains("5")||cutted.contains("6")||cutted.contains("7")||cutted.contains("8")||cutted.contains("9")) {

												validated="<http://ikm-group.ch/archimeo/apqc#"+prevCutted+validated.substring(0, validated.length()-3);
												cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAPQC", validated +"> ;"));

											}else {
												//if there's only one apqc
												validated="<http://ikm-group.ch/archimeo/apqc#"+prevCutted+validated;
												cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAPQC", validated +"> ;"));

											}
										}
									}

								}
								break;

							case 7:

								String cellValues=cell.toString();
								cellValues=validateFbpdo(cellValues.replace(" ",""));

								ArrayList<String> validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								for (int i = 0; i < validatedAl.size(); i++) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAction", "fbpdo:"+validatedAl.get(i) +" ;"));

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
								cellValues=validateFbpdo(cellValues.replace(" ",""));

								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								for (int i = 0; i < validatedAl.size(); i++) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasObject", "fbpdo:"+validatedAl.get(i) +" ;"));

								}


								// Here i am parsing Object
								//								ArrayList<String> matchedClasses = new ArrayList<String>();
								//								matchedClasses = getMatchedClasses(validateFbpdo(cell.toString()));
								//
								//								for (int i = 0; i < matchedClasses.size(); i++) {
								//									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasObject", (validateString(matchedClasses.get(i))+" ;")));
								//								}

								break;

							case 9://ok
								cellValues=cell.toString();
								cellValues=cellValues.replace(" ","");

								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								for (int i = 0; i < validatedAl.size(); i++) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasPaymentPlan", validateString(validatedAl.get(i)) +" ;"));

								}


								//								// Here I am parsing the Payment Plan
								//								
								//								ArrayList<String> matchInstance_PaymentPlan = new ArrayList<String>();
								//								matchInstance_PaymentPlan = getMatchedInstances(cell.toString());
								//								for (int i = 0; i < matchInstance_PaymentPlan.size(); i++) {
								//									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasPaymentPlan", validateString(matchInstance_PaymentPlan.get(i)) + " ;"));
								//
								//								}
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

								for (int i = 0; i < validatedAl.size(); i++) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasEncryptionType", validateString(validatedAl.get(i).replace(" ","_")) +" ;"));

								}
								// Here I am parsing Encryption Type
								//								ArrayList<String> matchInstanceEncryption = new ArrayList<String>();
								//								matchInstanceEncryption = getMatchedInstances(validateString(cell.toString()));
								//								for (int i = 0; i < matchInstanceEncryption.size(); i++) {
								//
								//									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasEncryptionType",
								//											validateString(matchInstanceEncryption.get(i)) + " ;"));
								//
								//								}

								break;

							case 12:

								cellValues=cell.toString();

								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								for (int i = 0; i < validatedAl.size(); i++) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasStoredDataLocation", validateString(validatedAl.get(i).replace(" ","_")) +" ;"));

								}
								// Here I am parsing StoredDataLocation
								//
								//								ArrayList<String> matchClasses_location = new ArrayList<String>();
								//								matchClasses_location = getMatchedClasses(validateString(cell.toString()));
								//								for (int i = 0; i < matchClasses_location.size(); i++){
								//									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasStoredDataLocation", validateString(matchClasses_location.get(i)) + " ;"));
								//								}

								break;

							case 13:

								if (cell.toString() != null && validateNullCellString(validateBooleanCellString(cell.toString()))) {
									cs.properties
									.add(new CloudServiceProperty("bpaas:cloudServiceHasSecurityStandardInPlace",
											validateBooleanCellString(cell.toString())+ " ;"));
								} else {
									cs.properties
									.add(new CloudServiceProperty("bpaas:cloudServiceHasSecurityStandardInPlace",
											validateBooleanCellString(cell.toString())+ " ;"));
								}
								break;

								// automated password management system

							case 14:

								// Here I am parsing password management system
								// automated

								if (cell.toString() != null && validateNullCellString(validateBooleanCellString(cell.toString()))) {

									cs.properties.add(new CloudServiceProperty(
											"bpaas:cloudServiceHasAutomatedPasswordManagmentSystem",
											validateBooleanCellString(cell.toString())+ " ;"));
								} else {

									// System.out.println("++++++++++++++++"+cell.toString()+"bbbbbbbbbbbbbb");
									cs.properties.add(new CloudServiceProperty(
											"bpaas:cloudServiceHasAutomatedPasswordManagmentSystem",
											validateBooleanCellString(cell.toString())+ " ;"));
								}

								break;

							case 15:
								// performanceManagementSystem in place

								if (cell.toString() != null && validateNullCellString(validateBooleanCellString(cell.toString()))) {
									cs.properties.add(new CloudServiceProperty(
											"bpaas:cloudServiceHasPerformanceManagementSystemInPlace",
											validateBooleanCellString(cell.toString())+ " ;"));
								} else

									cs.properties.add(new CloudServiceProperty(
											"bpaas:cloudServiceHasPerformanceManagementSystemInPlace",
											validateBooleanCellString(cell.toString())+ " ;"));

								break;

							case 16:
								// different performance plan available

								if (cell.toString() != null && validateNullCellString(validateBooleanCellString(cell.toString()))) {
									cs.properties.add(new CloudServiceProperty(
											"bpaas:cloudServiceHasDifferentPerformancePlanAvailable",
											validateBooleanCellString(cell.toString())+ " ;"));
								} else
									cs.properties.add(new CloudServiceProperty(
											"bpaas:cloudServiceHasDifferentPerformancePlanAvailable",
											validateBooleanCellString(cell.toString())+ " ;"));
								break;

							case 17:
								// response time in millisecond

								if (cell.toString() != null && validateNullCellString(cell.toString())) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasResponseTime_in_ms",
											validateNumeric(cell).toString() + " ;"));}
								break;

							case 18:
								// computing processing power power scalable
								if (cell.toString() != null && validateNullCellString(cell.toString())) {
									cs.properties
									.add(new CloudServiceProperty("bpaas:cloudServiceHasScalableProcessingPower",
											validateBooleanCellString(cell.toString())+ " ;"));
								} else
									cs.properties
									.add(new CloudServiceProperty("bpaas:cloudServiceHasScalableProcessingPower",
											validateBooleanCellString(cell.toString())+ " ;"));

								break;

							case 19:
								// data storage scalable

								if (cell.toString() != null && validateNullCellString(cell.toString())) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasScalableStorage",
											validateBooleanCellString(cell.toString())+ " ;"));
								} else
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasScalableStorage",
											validateBooleanCellString(cell.toString())+ " ;"));
								break;

							case 20:
								// here i am parsing data storage in BG

								if (cell.toString() != null && validateNullCellString(cell.toString())) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasDataStorageInGB",
											validateNumeric(cell).toString() + " ;"));}
								break;

							case 21:

								// Here I am parsing simultaneous user
								if (cell.toString() != null && validateNullCellString(cell.toString())) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasSimultaneousUsers",
											validateNumeric(cell).toString() + " ;"));}

								break;

							case 22:

								// Here I am parsing percentage

								// if(cell.toString() !=null &&
								// !null_values_string.contains(cell.toString().trim()))
								// {
								//
								// String d =
								// validateDecimalString(cell.getNumericCellValue()+"");
								// //System.out.println("wala: "+d);
								// cs.properties.add(new
								// CloudServiceProperty("bpaas:cloudServiceHasAvailabilityInPercent",String.valueOf(d)+"
								// ;"));
								// }
								// else
								// cs.properties.add(new
								// CloudServiceProperty("bpaas:cloudServiceHasAvailabilityInPercent",String.valueOf(validateDecimalString(cell.getStringCellValue()+"
								// ;"))));
								//
								//
								break;

							case 23:
								// Here I am parsing Access Log Availability in
								// months

								if (!validateNullCellString(cell.toString())) {

									cs.properties.add(			
											new CloudServiceProperty("bpaas:cloudServiceHasAccessLogAvailabilityInMonths",
													validateNumeric(cell) + " ;"));
								}else {
									new CloudServiceProperty("bpaas:cloudServiceHasAccessLogAvailabilityInMonths", "questionnaire:Not_Specified ;");
								}

								break;

							case 24:
								// Here I am parsing Access Log Retention Period in
								// months
								if (cell.toString() != null && validateNullCellString(cell.toString())) {

									cs.properties.add(new CloudServiceProperty(
											"bpaas:cloudServiceHasAccessLogRetentionPeriodInMonths",
											validateNumeric(cell) + " ;"));
								}

								else

									cs.properties.add(new CloudServiceProperty(
											"bpaas:cloudServiceHasAccessLogRetentionPeriodInMonths",
											validateNumeric(cell) + " ;"));

								break;

							case 25:
								// here i am parsing AuditLogAvailabilityin Months
								if (cell.toString() != null && validateNullCellString(cell.toString())) {

									cs.properties.add(
											new CloudServiceProperty("bpaas:cloudServiceHasAuditLogAvailabilityInMonth",
													validateNumeric(cell) + " ;"));
								}
								break;

							case 26:
								// here i am parsing AuditLogRetentionPeriodinMonths
								if (cell.toString() != null && validateNullCellString(cell.toString())) {

									cs.properties.add(
											new CloudServiceProperty("bpaas:cloudServiceHasAuditLogRetentionPeriodInMonths",
													validateNumeric(cell) + " ;"));
									System.out.println(cell.toString()+" -------------------------------------->"+validateNumeric(cell));
								}
								break;

							case 27:
								cellValues=cell.toString();

								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								for (int i = 0; i < validatedAl.size(); i++) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasBackupFrequency", validateString(validatedAl.get(i).replace(" ","_")) +" ;"));

								}

								// here I am parsing the BackupFrequecny
								//								ArrayList<String> matchInstance_backupFrequency;// = new ArrayList<String>();
								//								matchInstance_backupFrequency = getMatchedInstances(cell.toString());
								//								for (int i = 0; i < matchInstance_backupFrequency.size(); i++) {
								//									//System.out.println("////////////////////////////////////////////////"+cell.toString());
								//									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasBackupFrequency", 
								//											validateString(matchInstance_backupFrequency.get(i)) + " ;"));
								//
								//								}
								break;

							case 28:
								// here I am parsing BackupRetentionTime
								cellValues=cell.toString();

								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								for (int i = 0; i < validatedAl.size(); i++) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasBackupRetentionTime", validateString(validatedAl.get(i)) +" ;"));

								}


								//								ArrayList<String> matchInstances_backupTime = new ArrayList<String>();
								//								matchInstances_backupTime = getMatchedInstances(cell.toString());
								//
								//								for (int i = 0; i < matchInstances_backupTime.size(); i++) {
								//
								//									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasBackupRetentionTime",
								//											validateString(matchInstances_backupTime.get(i)) + " ;"));
								//
								//								}
								break;
							case 29: 
								//MediaType Data Export Import format

								cellValues=cell.toString();

								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								for (int i = 0; i < validatedAl.size(); i++) {
									cs.properties.add(new CloudServiceProperty("bpaas:CloudServiceHasMediaTypeImportExport", validateString(validatedAl.get(i)) +" ;"));
								}

								//								ArrayList<String> matchInstances_exportImport = new ArrayList<String>();
								//								matchInstances_exportImport = getMatchedInstances(cell.toString());
								//
								//								for (int i = 0; i < matchInstances_exportImport.size(); i++) {
								//
								//									cs.properties.add(new CloudServiceProperty("bpaas:CloudServiceHasMediaTypeImportExport",
								//											validateString(matchInstances_exportImport.get(i)) + " ;"));
								//
								//								}
								break;
								//	case 30 is useless, merged into 29

							case 31:
								// data migration

								if (cell.toString() != null && validateNullCellString(cell.toString())) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasDataMigration",
											validateBooleanCellString(cell.toString())+ " ;"));
								} else
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasDataMigration",
											validateBooleanCellString(cell.toString())+ " ;"));
								break;

							case 32:
								// API

								if (cell.toString() != null && validateNullCellString(cell.toString())) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAPI",
											validateBooleanCellString(cell.toString())+ " ;"));
								} else
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAPI",
											validateBooleanCellString(cell.toString())+ " ;"));
								break;

							case 33:
								
								cellValues=cell.toString();
															
								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								for (int i = 0; i < validatedAl.size(); i++) {
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasServiceSupportResponsiveness", validateString(validatedAl.get(i)) +" ;"));
									//System.out.println(cellValues+" -------------------------------------->"+validateString(validatedAl.get(i)));
									
								}


								// ServiceSupportResponsiveness

								//								ArrayList<String> matchInstance_ServiceResponsive;// = new ArrayList<String>();
								//								matchInstance_ServiceResponsive = getMatchedInstances(cell.toString());
								//								//System.out.println("nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn"+cell.toString());
								//								for (int i = 0; i < matchInstance_ServiceResponsive.size(); i++) {
								//
								//									cs.properties.add(
								//											new CloudServiceProperty("bpaas:cloudServiceHasServiceSupportResponsiveness",
								//													validateString(matchInstance_ServiceResponsive.get(i)) + " ;"));
								//
								//								}

								break;

							case 34:
								// service support
								cellValues=cell.toString();

								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								for (int i = 0; i < validatedAl.size(); i++) {
									String validated= validatedAl.get(i).replace(" ", "_");
									
									validated = validated.substring(0,1).toUpperCase() + validated.substring(1);
									validated=validateString(validated);
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasServiceSupport", validated +" ;"));
									//System.out.println(cellValues+" -------------------------------------->"+validateString(validatedAl.get(i)));
								}
								//								
								//
								//								ArrayList<String> matchInstance_serviceSupport = new ArrayList<String>();
								//								matchInstance_serviceSupport = getMatchedInstances(cell.toString());
								//
								//								for (int i = 0; i < matchInstance_serviceSupport.size(); i++) {
								//
								//									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasServiceSupport",
								//											validateString(matchInstance_serviceSupport.get(i)) + " ;"));
								//
								//								}
								//
								break;

							case 35:
								// SupportChannel
								cellValues=cell.toString();

								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								for (int i = 0; i < validatedAl.size(); i++) {
									String validated= validatedAl.get(i).replace(" ", "_");
									//System.out.println(validated);
									validated = validated.substring(0,1).toUpperCase() + validated.substring(1);
									//System.out.println(validated);
									validated=validateString(validated);
									//System.out.println(validated);
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasSupportChannel", validated +" ;"));
									//System.out.println(cellValues+" -------------------------------------->"+validated);
								}
								break;

							case 36:
								// TargetMarket
								cellValues=cell.toString();

								validatedAl=new ArrayList<String>(Arrays.asList(cellValues.split((","))));

								for (int i = 0; i < validatedAl.size(); i++) {

									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasTargetMarket",
											validateString(validatedAl.get(i).replace(" ","_")) +" ;"));

								}

								break;

								//
							}

						}
					}			
				}
				if (cs.getName() != null) {
					services.add(cs);
				}
			}

		} catch (EncryptedDocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

		if (cell.equals("not specified") ||cell.equals("not specfied") || cell.equals("Not specified")|| cell.equals("not_specified") || cell.equals("Not_specified") || cell.equals("") ||cell.equals(" ") || cell.equals(null) || null_values_string.contains(cell.toString())) {
			return "questionnaire:Not_Specified";
		}else {
			String s="questionnaire:Not_Specified";
			String start= cell.toString().substring(0,1);
			if (start.contains(" ")||start.contains("_")) {
				s =cell.toString().substring(1,cell.toString().length());	
				return s;
			}else {
				return cell.toString();
			}

		}

		/* what is it?
		if (null_values_string.contains(cell.toString())) {
			return cell.toString();
		} else {
			if (cell.toString().indexOf(".") > -1)
				return cell.toString().substring(0, cell.toString().indexOf("."));
			else
				return cell.toString();

		}*/
	}

	private String validateBooleanCellString(String cell) {
		boolean result;
		if (cell.equals("not specified") ||cell.equals("not specfied") || cell.equals("Not specified")|| cell.equals("not_specified") || cell.equals("Not_specified") || cell.equals("") ||cell.equals(" ") || cell.equals(null) || null_values_string.contains(cell.toString())) {
			return "questionnaire:Not_Specified";
		} else if (cell.equals("Any")||cell.equals("Any")) {
			return "questionnaire:Any";
		} else if (cell.equals("No")||cell.equals("Not")|| cell.equals("no") || cell.equals("not")) {
			return "questionnaire:No";
		} else if (cell.equals("Yes")||cell.equals("yes")) {
			return "questionnaire:Yes";
		} else
			return cell.toString();

	}

	private String validateString(String cell) {

		if (cell.startsWith(" ")||cell.startsWith("_") ) {
			cell=cell.substring(1,cell.length());
		}
		cell = cell.substring(0,1).toUpperCase() + cell.substring(1);
		cell=cell.replace("(","");
		cell=cell.replace(")","");


		boolean result;
		if (cell.equals("not specified") ||cell.equals("not defined") ||cell.equals("not specfied") || cell.equals("Not specified")|| cell.equals("not_specified") || cell.equals("Not_specified") || cell.equals("") ||cell.equals(" ") || cell.equals(null) || null_values_string.contains(cell.toString())) {
			return "questionnaire:Not_Specified";
		} else if (cell.equals("Any")||cell.equals("Any")) {
			return "questionnaire:Any";
		} else if (cell.equals("No")||cell.equals("Not")|| cell.equals("no") || cell.equals("not") || cell.equals("None")|| cell.equals("none")) {
			return "questionnaire:No";
		} else if (cell.equals("Yes")||cell.equals("yes")) {
			return "questionnaire:Yes";

			//PAYMENT PLAN
		}else if (cell.equals("Customizable Plan")) {
			return "bpaas:CustomizablePlan";
		}else if (cell.equals("Free of Charge")) {
			return "bpaas:FreeofCharge";
		}else if (cell.equals("Monthly Fee")) {
			return "bpaas:MonthlyFee";
		}else if (cell.equals("Any")||cell.equals("Any")) {
			return "questionnaire:Any";
		}else if (cell.equals("Prepaid Annual Plan")) {
			return "bpaas:PrepaidAnnualPlan";
		}else if (cell.equals("Try Free First")) {
			return "bpaas:TryfreeFirst";


			//ENCRYPTION TYPE
		}else if (cell.equals("TLS VPN")) {
			return "bpaas:TLS_VPN";
		}else if (cell.equals("IP Filtering")) {
			return "bpaas:IP_Filtering";
		} else if (cell.equals("SSL (Secure Sockets Layer)")) {
			return "bpaas:SSL";
		} else if (cell.equals("ISO:27001")) {
			return "bpaas:ISO27001";
		}  


		//STORED DATA LOCATION
		else if (cell.equals("European Economic Area (EEA)")) {
			return "bpaas:European_Economic_Area_EEA";

			//SERVICE SUPPORT
		}else if (cell.equals("at_most_1_working_days")) {
			return "bpaas:at_most_1_working_day";
		} else if (cell.equals("24-7")) {
			return "bpaas:Twenty4seven";
		}else if (cell.equals("7_days_a_week")) {
			return "bpaas:sevenDaysAWeek";
		}else if (cell.equals("24-5")) {
			return "bpaas:Twenty4five";
		}else if (cell.equals("9 AM - 5 PM")) {
			return "bpaas:nineToFive";
		}

		//Target Market 
		else if (cell.equals("Culture/Archeology")) {
			return "bpaas:cultureArcheology";
		}else if (cell.equals("No Target")) {
			return "bpaas:NoTarget";
		} 



		else {
			return "bpaas:"+cell.toString().replace(" ","_");
		}
	}

	private String validateFbpdo(String cell) {
		if (cell.startsWith(" ")||cell.startsWith("_") ) {
			cell=cell.substring(1,cell.length());
		}
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
			ArrayList<String> notAllowed= new ArrayList<String>();
			notAllowed.add("1");
			notAllowed.add("2");
			notAllowed.add("3");
			notAllowed.add("4");
			notAllowed.add("5");
			notAllowed.add("6");
			notAllowed.add("7");
			notAllowed.add("8");
			notAllowed.add("9");
			notAllowed.add(" ");
			notAllowed.add(".");


			for (int i=0; i<notAllowed.size();i++) {
				if (validated.contains(notAllowed.get(i))){
					validated=validated.replace(notAllowed.get(i),"");
				}	
			}

			return validated;
		}
	}

	private ArrayList<String> getMatchedClasses(String label) {
		ArrayList<String> result = new ArrayList<String>();
		String[] sublabels = label.split(", ");

		for (int i = 0; i < sublabels.length; i++) {
			boolean found = false;
			for (int j = 0; j < classes.size(); j++) {
				if (label.trim().equals(classes.get(j).getLabel())) {
					result.add(classes.get(j).getName());
					found = true;
				}

			}
			if (!found) {
				System.out.println("WARNING > " + sublabels[i] + " CLASS NOT FOUND!");
			}
		}

		return result;
	}

	private ArrayList<String> getMatchedInstances(String label) {
		ArrayList<String> result = new ArrayList<String>();
		String[] sublabels = label.split(",");
		for (int i = 0; i < sublabels.length; i++) {
			boolean found = false;
			for (int j = 0; j < instances.size(); j++) {
				if (sublabels[i].trim().equals(instances.get(j).getLabel())) {
					//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"+sublabels[i].trim().equals(instances.get(j).getLabel()));
					result.add(instances.get(j).getName());
					found = true;
				}

			}
			if (!found) {
				System.out.println("WARNING > " + sublabels[i] + " INSTANCE NOT FOUND!");
			}
		}

		return result;
	}
}
