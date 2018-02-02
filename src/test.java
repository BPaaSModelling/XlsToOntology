import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.UUID;

public class test {

	public static void main(String[] args) throws FileNotFoundException {
		//String pathExcel = ".\\resources\\ExcelFiles\\testexcel.xlsm";
		//String pathOntology = ".\\resources\\Ontology\\old version - deprecated\\";
		
		String pathExcel = ".\\resources\\ExcelFiles\\pluerat_latestfile.xlsx";
		String pathOntology = ".\\resources\\Ontology\\V3\\";
		
		Operation op = new Operation();
		
		op.parseOntology(pathOntology + "fbpdo.ttl");
		op.parseOntology(pathOntology + "bpaas.ttl");
		//System.out.println(op.getClasses().toString());
		op.parseOntology(pathOntology + "apqc.ttl");
		op.parseOntology(pathOntology + "questionnaire.ttl");
		op.parseOntology(pathOntology + "questiondata.ttl");
		op.parseOntology(pathOntology + "bdata.ttl");
		
		
		ArrayList<OntologyInstance> instance;

		op.parseExcelFile(pathExcel);
		//System.out.println(op.getServices().size());
		PrintStream out = new PrintStream(new FileOutputStream(".\\resources\\Output\\bdata1.ttl"));
		System.setOut(out);
		System.out.println("# baseURI: http://ikm-group.ch/archiMEO/bdata\r\n" + 
				"# imports: http://ikm-group.ch/archimeo/bpaas\r\n" + 
				"# imports: http://ikm-group.ch/archimeo/questionnaire\r\n" + 
				"\r\n" + 
				"# prefix: bdata\r\n" + 
				"\r\n" + 
				"@prefix apqc: <http://ikm-group.ch/archimeo/apqc#> .\r\n" + 
				"@prefix bdata: <http://ikm-group.ch/archiMEO/bdata#> .\r\n" + 
				"@prefix bpaas: <http://ikm-group.ch/archimeo/bpaas#> .\r\n" + 
				"@prefix fbpdo: <http://ikm-group.ch/archimeo/fbpdo#> .\r\n" + 
				"@prefix questionnaire: <http://ikm-group.ch/archimeo/questionnaire#> .\r\n" + 
				"@prefix owl: <http://www.w3.org/2002/07/owl#> .\r\n" + 
				"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n" + 
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\r\n" + 
				"@prefix top: <http://ikm-group.ch/archiMEO/top#> .\r\n" + 
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\r\n" + 
				"\r\n" + 
				"<http://ikm-group.ch/archiMEO/bdata>\r\n" + 
				"  rdf:type owl:Ontology ;\r\n" + 
				"  owl:imports <http://ikm-group.ch/archimeo/bpaas> ;\r\n" + 
				"  owl:versionInfo \"Created with TopBraid Composer\" ;\r\n" + 
				"  .");
		
		for (int i = 0; i < op.getServices().size(); i++){
			//System.out.println("bdata:" +""+op.getServices().get(i).getName());
			String name=op.getServices().get(i).getName();
			name=name.replace("\n", "");
			name=name.replace("(", "");
			name=name.replace(")", "");
			name=name.replace(" ", "_");
			name=name.replaceAll("\\W+","");
			System.out.println("bdata:"+ name);
			System.out.println("rdf:type bpaas:CloudService"+" "+";");
			
			for (int j = 0; j < op.getServices().get(i).getProperties().size(); j++){
				System.out.println(op.getServices().get(i).getProperties().get(j).name + " " + op.getServices().get(i).getProperties().get(j).value);
			}
			
			
			System.out.println("rdfs:label " + "\"" +name + "\" ;");
			System.out.println(".");
		}
		//TODO: STORE THE DATA INTO A PHYSICAL FILE IN FOLDER  ".\\resources\\Output
		
		
		for(int i = 0; i < op.getInstances().size();i++)
		{
			 instance = op.getInstances();
			 	 
			
		}
		
	}

} 
