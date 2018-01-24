import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
	private ArrayList<OntologyClass> classes;					//it contains all the classes of the ontology
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

	private ArrayList<OntologyProperty> properties;				//it contains all the properties of the ontology
	private ArrayList<OntologyInstance> instances;				//it contains all the instances of the ontology
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
		null_values_string.add(" ");
	}

	public int[] parseOntology(String path_file){//this method parse the ontology
		//takes in input the path of the file and it fills the four arrays
		//stored this java-class (classes, properties, instances and the ontologyPreamble)
		//returns an array with the number of classes, properties and instances loaded
		String line = null;
		FileReader reader = null;
		
		try {
			reader = new FileReader(path_file);
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
			
		}
		Scanner scanner = new Scanner(reader);
		//Set the variable "startPreamble" to detect when the preamble finish
		boolean preamble = true;
		ArrayList<String> temp_type = null;
		String temp_name = null;
		ArrayList<OntologyAttribute> temp_attributes = null;
		boolean titleLine = true;
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();
			
			if (preamble){
				//here we are in the preamble
				if (!line.startsWith(".")){
				ontologyPreamble.add(line);
			}else {
				//here we are in the first dot
				//where the ontology elements starts
				preamble = false;
				
			}
			//here we are still in the preamble
			//where the line doesn't start with "."
			
			}else {
				//here we are not in the preamble and we start to analyse
				//the elements of the ontology
				if (titleLine && !line.startsWith(".")){
					//first line of the object of the ontology
					//here we can find prefix:name of the object
					//and we instantiate all the data structures
					temp_type = new ArrayList<String>();
					temp_name = new String();
					temp_attributes = new ArrayList<OntologyAttribute>();
		
					temp_name = line;
					titleLine = false;
				} else if (!titleLine && line.trim().startsWith(".")){
					//end of the object of ontology
					titleLine = true;
					for (int t = 0; t < temp_type.size();t++){
					if (temp_type.get(t).equals("owl:Class")){
						//Parsing a Class
						OntologyClass c = new OntologyClass(temp_name,temp_type,temp_attributes);
						classes.add(c);
						break;
					} else if (temp_type.get(t).equals("owl:AnnotationProperty") ||
								temp_type.get(t).equals("owl:DatatypeProperty") ||
								temp_type.get(t).equals("owl:DeprecatedProperty") ||
								temp_type.get(t).equals("owl:FunctionalProperty") ||
								temp_type.get(t).equals("owl:ObjectProperty")) {
						//Parsing a Property
						OntologyProperty p = new OntologyProperty(temp_name,temp_type,temp_attributes);
						properties.add(p);
						break;
					
					} else if (t == temp_type.size()-1){
						//Parsing an Istance
						OntologyInstance i = new OntologyInstance(temp_name,temp_type,temp_attributes);
						instances.add(i);
					}
					}
						
				} else {
					//body of the object of the ontology
					String[] arraySplittate = parseAttributeName(line);
					if (arraySplittate[0].equals("rdf:type")){
						temp_type.add(arraySplittate[1].replaceAll(";","").trim());
					} else {
						//parsing attributes
						OntologyAttribute oa;
						String[] arraySplittate2 = arraySplittate[1].trim().split("\\^\\^"); //splitting the string with "^^" so we can define type and value
						if (arraySplittate2.length == 2){	//if the split has type and value
						oa = new OntologyAttribute(
								arraySplittate[0].replaceAll(";","").trim(),
								arraySplittate2[1].replaceAll(";","").trim(),
								arraySplittate2[0].replaceAll("\"", "").replaceAll(";","").trim()); //name,type,value
						
						} else {
						oa = new OntologyAttribute(
								arraySplittate[0].replaceAll(";","").trim(),
								"",
								arraySplittate2[0].replaceAll(";","").trim());	//name,type,value
						}
						temp_attributes.add(oa);
					}
				}
				
		
			
	}//end not preamble
	
}//end while scanner
		scanner.close();
	
		//-------------TESTING THE WHOLE ONTOLOGY-------------
		/*
	 	System.out.println("Number of lines of preamble: " + ontologyPreamble.size());
		System.out.println("Number of classes: "+classes.size());
		for (int i = 0; i < classes.size(); i++){
			System.out.print("    "+classes.get(i).getPrefix()+":"+classes.get(i).getName());
			System.out.print(" - Type: ");
			for(int j=0;j<classes.get(i).getTypes().size();j++){
				System.out.print(classes.get(i).getTypes().get(j)+" ,");
			}
		System.out.println("");
		}
		System.out.println("Number of properties: "+properties.size());
		for (int i = 0; i < properties.size(); i++){
			System.out.print("    "+properties.get(i).getPrefix()+":"+properties.get(i).getName());
			System.out.print(" - Type: ");
			for(int j=0;j<properties.get(i).getTypes().size();j++){
				System.out.print(properties.get(i).getTypes().get(j)+" ,");
			}
		System.out.println("");
		}
		System.out.println("Number of instances: "+instances.size());
		for (int i = 0; i < instances.size(); i++){
			System.out.print("    "+instances.get(i).getPrefix()+":"+instances.get(i).getName());
			System.out.print(" - Type: ");
			for(int j=0;j<instances.get(i).getTypes().size();j++){
				System.out.print(instances.get(i).getTypes().get(j)+" ,");
			}
		System.out.println("");
		}
		*/
		
		//--------------------------TESTING THE CLASSES OF ONTOLOGY---------------------
		/*
		System.out.println("Classes:");
		for (int i = 0; i < classes.size(); i++){
			System.out.println("   Name: "+classes.get(i).getPrefix()+":"+classes.get(i).getName());
			System.out.println("     Types:");
			for (int j = 0; j < classes.get(i).getTypes().size();j++){
				System.out.println("       "+classes.get(i).getTypes().get(j));
			}
			System.out.println("     Attributes:");
			for (int j = 0; j < classes.get(i).getAttributes().size(); j++){
				 System.out.println("       name: "+classes.get(i).getAttributes().get(j).getName());
				 System.out.println("       type: "+classes.get(i).getAttributes().get(j).getType());
				 System.out.println("       value: "+classes.get(i).getAttributes().get(j).getValue());
				 System.out.println("       -------");
			}
			System.out.println("--------------------");
		}
		
		//--------------------------TESTING THE PROPERTIES OF ONTOLOGY---------------------
		
		System.out.println("Property:");
		for (int i = 0; i < properties.size(); i++){
			System.out.println("   Name: "+properties.get(i).getPrefix()+":"+properties.get(i).getName());
			System.out.println("     Types:");
			for (int j = 0; j < properties.get(i).getTypes().size();j++){
				System.out.println("       "+properties.get(i).getTypes().get(j));
			}
			System.out.println("     Attributes:");
			for (int j = 0; j < properties.get(i).getAttributes().size(); j++){
				 System.out.println("       name: "+properties.get(i).getAttributes().get(j).getName());
				 System.out.println("       type: "+properties.get(i).getAttributes().get(j).getType());
				 System.out.println("       value: "+properties.get(i).getAttributes().get(j).getValue());
				 System.out.println("       -------");
			}
			System.out.println("--------------------");
		}
		
		//--------------------------TESTING THE PROPERTIES OF ONTOLOGY---------------------
		System.out.println("Instances:");
		for (int i = 0; i < instances.size(); i++){
			System.out.println("   Name: "+instances.get(i).getPrefix()+":"+instances.get(i).getName());
			System.out.println("     Types:");
			for (int j = 0; j < instances.get(i).getTypes().size();j++){
				System.out.println("       "+instances.get(i).getTypes().get(j));
			}
			System.out.println("     Attributes:");
			for (int j = 0; j < instances.get(i).getAttributes().size(); j++){
				 System.out.println("       name: "+instances.get(i).getAttributes().get(j).getName());
				 System.out.println("       type: "+instances.get(i).getAttributes().get(j).getType());
				 System.out.println("       value: "+instances.get(i).getAttributes().get(j).getValue());
				 System.out.println("       -------");
			}
			System.out.println("--------------------");
		}
		*/
		return new int[] {classes.size(),properties.size(),instances.size()};
		
	}//end method
	
	
	
	private String[] parseAttributeName(String attribute_line){	//this method parse the name-value of an Ontology attribute
		attribute_line.replaceAll(";", "").trim();	//we remove the final ";"
		String[] arraySplittate = attribute_line.trim().split(" ");
		String temp_value = "";
		for (int i = 1; i < arraySplittate.length;i++){
		temp_value = temp_value + " " + arraySplittate[i].trim();
		}
		return new String[]{arraySplittate[0],temp_value};
	}
	

	public void parseExcelFile(String path_file){
		Workbook workbook;
		
		//TODO: REORDER AND ADJUCT CASES TO THE NEW ONTOLOGY
		// The functional requirements should be linked to fbpdo
		// The non functional has to be linked to bpaas
		// any appearence of yes, no, not specified can be linked to questionnaire:Yes, questionnaire:No, questionnaire:Not_specified
		// use the excel file "mapping for the updated excel" in folder Resources\REFERENCE as main reference.
		// AN EXAMPLE CLOUDSERVICE is available in bdata:Drive_1 or Gmail_1
		try {
			workbook = WorkbookFactory.create(new File(path_file));
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()){
				//parsing the rows
				Row row = rowIterator.next();
				CloudService cs = new CloudService();
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()){
					Cell cell = cellIterator.next();
					//if (row.getRowNum() > 1){
					if (row.getRowNum() ==1){
						maxcount = row.getLastCellNum();
						
					
						switch(cell.getColumnIndex()){
						case 0:
							cs.setName(cell.toString());
							break;
							
							//here i am parsing provider
						/*case 1:
							if(!validateNullCellString(cell.toString()))
							{
								cs.properties.add(new CloudServiceProperty("bpass:cloudServiceHasProvider", String.valueOf(cell.toString())+";"));
							}
							break;
							
							
						case 2:
							//here i am parsing cloud service has description
							if (!validateNullCellString(cell.toString())){
							
							{
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasDescription",String.valueOf(cell.toString())+";" ));
							}
							}
							break;
						
							case 6:
							//here I am parsing APQC category
							if(cell.toString() !=null && cell.getCellType() == cell.CELL_TYPE_STRING)
							{
							   cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAPQC", cell.getStringCellValue()));
							}
							break;*/
							
						
							case 7:
				
							//here I am parsing action class
							ArrayList<String> matchedClasses_forAction = new ArrayList<String>();
							matchedClasses_forAction = getMatchedClasses(cell.toString());
							
							for(int i = 0; i < matchedClasses_forAction.size(); i++)
							{
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAction", matchedClasses_forAction.get(i)));
								
							}
							
							break;
						case 8:
							//Here i am parsing Object
							ArrayList<String> matchedClasses = new ArrayList<String>();
							matchedClasses = getMatchedClasses(cell.toString());
							
							for (int i = 0; i < matchedClasses.size(); i++){
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasObject",matchedClasses.get(i)));
							}
									
						break;
						
						
							
							
							
						case 11:
							//Here I am parsing StoredDataLocation
							
							ArrayList<String> matchClasses_location = new ArrayList<String>();
							matchClasses_location = getMatchedClasses(cell.toString());
							for (int i = 0; i < matchClasses_location.size(); i++)
							
							{
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasStoredDataLocation",matchClasses_location.get(i)+" ;"));
							}
						
							break;
							
							
						case 12:
							// Here I am parsing media type
							ArrayList<String> matchInstances_media = new ArrayList<String>();
							matchInstances_media = getMatchedInstances(cell.toString());
							 for (int i = 0; i < matchInstances_media.size(); i++)
							
							{
										cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasMediaType",matchInstances_media.get(i)+" ;"));
									
							}
							
							
							break;
							
						case 13:
							//here I am parsing the BackupFrequecny
							ArrayList<String> matchInstance_backupFrequency = new ArrayList<String>();
							matchInstance_backupFrequency = getMatchedInstances(cell.toString());
							for(int i=0; i<matchInstance_backupFrequency.size();i++)
							{
								
										cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasBackupFrequency",matchInstance_backupFrequency.get(i)+" ;"));
									
								
							}
							break;
							
						case 14:
							//here I am parsing BackupRetentionTime
							
							ArrayList<String> matchInstances_backupTime = new ArrayList<String>();
							matchInstances_backupTime = getMatchedInstances(cell.toString());
							
							for(int i=0; i<matchInstances_backupTime.size();i++)
							{
							
										cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasBackupRetentionTime",matchInstances_backupTime.get(i)+" ;"));
									
								
							}
							break;
							
						case 9:
							//Here I am parsing the Payment Plan
							
							ArrayList<String> matchInstance_PaymentPlan = new ArrayList<String>();
							matchInstance_PaymentPlan = getMatchedInstances(cell.toString());
							for (int i = 0; i < matchInstance_PaymentPlan.size(); i++)
							{
										cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasPaymentPlan",matchInstance_PaymentPlan.get(i)+" ;"));
									
								
							}
							break;
							
						case 16:
							//here I am parsing log file retention policy
							
							ArrayList<String> matchInstance_RetentionPolicy = new ArrayList<String>();
							matchInstance_RetentionPolicy = getMatchedInstances(cell.toString());
							for(int i = 0; i < matchInstance_RetentionPolicy.size();i++)
							{
								
								
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAccessLogFileRetentionPolicy", matchInstance_RetentionPolicy.get(i)+" ;"));
								
								
							}
							
							break;
							
						/*case 17:
							
							// Here I am parsing Encryption Type
					         ArrayList<String> matchInstanceEncryption = new ArrayList<String>();
					         matchInstanceEncryption = getMatchedInstances(cell.toString()); 
							for(int i = 0; i < matchInstanceEncryption.size();i++)
							{
								
								
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasEncryptionType", matchInstanceEncryption.get(i)+" ;"));
		
								
							}
							
							break; */
						
							
						case 18:
							
							// here i am parsing service support responsiveness
							
							ArrayList<String> matchInstanceSupportResponsive = new ArrayList<String>();
							matchInstanceSupportResponsive = getMatchedInstances(cell.toString());
							for(int i = 0; i<matchInstanceSupportResponsive.size();i++)
							{
								
								
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasServiceSupportResponsiveness", matchInstanceSupportResponsive.get(i)+" ;"));
								
								
							}
							
							break;
							
						
						case 19:
							//here I am parsing service support
							
							ArrayList<String> matchInstanceServiceSupport = new ArrayList<String>();
							matchInstanceServiceSupport = getMatchedInstances(cell.toString());
							
							for(int i = 0; i< matchInstanceServiceSupport.size();i++)
							{
								
								
								
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasServiceSupport", matchInstanceServiceSupport.get(i)+" ;"));
								
							}
							
							break;
							
						case 20:
							// Here I am parsing Access Log Availability in months
							
							
									if (cell.toString() != null){
										
										
										
										cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAccessLogAvailabilityInMonths", validiateNumeric(cell)+" ;"));
									}
									
								
							
							
							break;
							
						case 21:
							//Here I am parsing Access Log Retention Period in months
							if(cell.toString() !=null)
							{
								
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAccessLogRetentionPeriodInMonths", validiateNumeric(cell)+" ;"));
							}
							
							
							break;
							
							
							
						case 22:
							//here i am parsing AuditLogAvailabilityin Months
							if(cell.toString() != null)
							{
								
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAuditLogAvailabilityInMonth", validiateNumeric(cell)+" ;"));
							}
							
							break;
							
							
						case 23:
							//here I am parsing audit log retention periods in months
							if(cell.toString() !=null)
							{
								
								
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAuditLogRetentionPeriodInMonths",validiateNumeric(cell)));
							}
							
							
							break;
							
						case 24:
							
							// Here I am parsing percentage
						
							if(cell.toString() !=null && !null_values_string.contains(cell.toString().trim()))
							{
								String d = validateDecimalString(cell.getNumericCellValue()+"");
								//System.out.println("wala:  "+d);
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAvailabilityInPercent",String.valueOf(d)));
							}
							else
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAvailabilityInPercent",String.valueOf(validateDecimalString(cell.getStringCellValue()))));
							
							
							break;
							
							
						case 25:
							//Here I am parsing backup restore in GB per hour
							if(cell.toString() !=null)
								
							{
								
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasBackupRestoreInGBPerHour", validiateNumeric(cell)+" ;"));
							}
							break;
						case 26:
							//here i am parsing data storage in BG
							
							if(cell.toString() !=null)
							{
								
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasDataStorageInGB", validiateNumeric(cell)+" ;"));
							}
							break;
							
						case 27:
							//here i am parsing response time in mili second
							
							if(cell.toString() !=null)
							{
								
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasResponseTime_in_ms", validiateNumeric(cell)+" ;"));
							}
							break;
							
						case 28:
							
							// Here I am parsing simultaneous user
							if(cell.toString() !=null)
							{
								
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasSimultaneousUsers",validateBooleanCellString(String.valueOf(cell.toString()))));
							}
							break;
						
							
						case 30:
							
							//I am parsing terms of contracts
							ArrayList<String> matchInstanceTermsofContract = new ArrayList<String>();
							matchInstanceTermsofContract = getMatchedInstances(cell.toString());
							for(int i = 0; i<matchInstanceTermsofContract.size();i++)
							{
								
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasTermsOfContract", matchInstanceTermsofContract.get(i)+" ;"));
								
								
							}
							
							break;
							
						case 31:
							// Here I am parsing additional costs
							if(cell.toString() !=null)
							{
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAdditionalCosts", validateBooleanCellString(String.valueOf(cell.toString()+" ;"))));
							}
							break;
							
						case 45:
							//Here I am parsing password management system automated 
							
							if(cell.toString() !=null)
							{
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAutomatedPasswordManagmentSystem", validateBooleanCellString(String.valueOf(cell.toString()+" ;"))));
							}
							
							break;
							
						case 33:
							//Here I am parsing Availabilty Level Optional
							if(cell.toString() !=null)
							{
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasAvailabiltyLevelOptional", validateBooleanCellString(String.valueOf(cell.toString()+" ;"))));
							}
							
							break;
							
						case 34:
							// Here I am parsing Ordered Self Service
							
							if(cell.toString() !=null)
							{
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasOrderedSelfService", validateBooleanCellString(String.valueOf(cell.toString()+" ;"))));
							}
							break;
						case 35:
							// Here I am parsing support channels
						
							ArrayList<String> matchInstanceSupoortChannels = new ArrayList<String>();
							matchInstanceSupoortChannels = getMatchedInstances(cell.toString());
							for(int i = 0; i<matchInstanceSupoortChannels.size();i++)
							{
								
									cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasSupportChannels", matchInstanceSupoortChannels.get(i)+" ;"));
								
								
							}
						case 36:
							//here I am parsing the TargetMarket
							ArrayList<String> matchInstances_Target = new ArrayList<String>();
							matchInstances_Target = getMatchedInstances(cell.toString());
							for (int i = 0; i < matchInstances_Target.size(); i++)
							{
										cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasTargetMarket",matchInstances_Target.get(i)+" ;"));
									
							}
							
								
							
							break;
						case 89:
							
							// Here I am parsing scalability offered to customer
							if(cell.toString() !=null)
							{
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasScalabilityOfferedToCustomer", validateBooleanCellString(String.valueOf(cell.toString()+" ;"))));
							}
							
							break;
							
						case 37:
							
							// Here I am parsing data export format available
							
							if(cell.toString() !=null)
							{
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServicehasDataExportFormatAvailable", validateBooleanCellString(String.valueOf(cell.toString()+" ;"))));
							}
							break;
							
						case 38:
							//Here I am parsing API Integrate Solution
							
							if(cell.toString() !=null)
							{
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasApiIntegrateSolution", validateBooleanCellString(String.valueOf(cell.toString()+" ;"))));
							}
							break;
						case 39:
							// Here I am parsing data migration independent
							
							if(cell.toString() !=null)
							{
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasDataMigrationIndependent", validateBooleanCellString(String.valueOf(cell.toString()+" ;"))));
							}
							break;
							
							
						case 40:
							
							// Here I am parsing document management system
							ArrayList<String> matchInstanceDocumentManagement = new ArrayList<String>();
							matchInstanceDocumentManagement = getMatchedInstances(cell.toString());
							
							for(int i=0; i<matchInstanceDocumentManagement.size();i++)
							{
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasDocumentManagementSystem",matchInstanceDocumentManagement.get(i)+" ;"));
									
								
							}
							
							break;
							
						case 41:
							// Here I am parsing security management system
							
							if(cell.toString() !=null)
							{
								cs.properties.add(new CloudServiceProperty("bpaas:cloudServiceHasSecurityManagementSystem", validateBooleanCellString(String.valueOf(cell.toString()+" ;"))));
							}
							break;
//							
						}
						
					}
				}
				if(cs.getName()!=null){
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
	
	private boolean validateNullCellString(String cell){
		boolean found = false;
		for (int i = 0; i < this.null_values_string.size(); i++){
			//if it is true, then you set found as true
			if(cell.equals(null_values_string.get(i)))
			{
				found = true;
			}
		}
		return found;
	}
	private String validateDecimalString(String cell)
	
	{
		{
			if(null_values_string.contains(cell))
			{			
				return "N/A^^xsd:boolean ;";
			}
			else
				return cell+"^^xsd:decimal";
			
		}
		
	}
	
	private String validiateNumeric(Cell cell)
	{
		if(null_values_string.contains(cell.toString()))
		{
			return cell.toString();
		}
		else
		{
			
			return
					
			cell.toString().substring(0,cell.toString().indexOf("."));
			
		}
	}
	
	
	private String validateBooleanCellString(String cell){
		boolean result;
		if (cell.equals("yes".trim())){
			return "\"true\"^^xsd:boolean ;";
		}else {
			return "\"false\"^^xsd:boolean ;";
		}
		
	}
	
	private ArrayList<String> getMatchedClasses(String label){
		ArrayList<String> result = new ArrayList<String>();
		String[] sublabels = label.split(",");
		for (int i = 0; i < sublabels.length; i++){
			boolean found = false;
			for (int j = 0; j < classes.size(); j++){
				if (label.trim().equals(classes.get(j).getLabel())){
					result.add(classes.get(j).getName());
					found = true;
				}
				
			}
			if (!found){
				System.out.println("WARNING > " + sublabels[i] + " CLASS NOT FOUND!");
			}
		}
		
		return result;
	}
	
	private ArrayList<String> getMatchedInstances(String label){
		ArrayList<String> result = new ArrayList<String>();
		String[] sublabels = label.split(",");
		for (int i = 0; i < sublabels.length; i++){
			boolean found = false;
			for (int j = 0; j < instances.size(); j++){
				if (label.trim().equals(instances.get(j).getLabel())){
					result.add(instances.get(j).getName());
					found = true;
				}
				
			}
			if (!found){
				System.out.println("WARNING > " + sublabels[i] + " INSTANCE NOT FOUND!");
			}
		}
		
		return result;
	}
}
