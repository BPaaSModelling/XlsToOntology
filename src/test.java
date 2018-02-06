import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.UUID;

public class test {

	public static void main(String[] args) throws FileNotFoundException {
				
		String pathExcel = ".\\resources\\ExcelFiles\\all.xlsx";
		
		String pathOntology = ".\\resources\\Ontology\\V3\\";
		
		
		Operation op = new Operation();
		
		op.parseOntology(pathOntology + "fbpdo.ttl");
		op.parseOntology(pathOntology + "bpaas.ttl");
		op.parseOntology(pathOntology + "apqc.ttl");
		op.parseOntology(pathOntology + "questionnaire.ttl");
		op.parseOntology(pathOntology + "questiondata.ttl");
		op.parseOntology(pathOntology + "bdata.ttl");
		
		
		ArrayList<OntologyInstance> instance;

		op.parseExcelFile(pathExcel);
		
		
		PrintStream out = new PrintStream(new FileOutputStream(".\\resources\\Output\\bdata.ttl"));
		System.setOut(out);
		String imports="# baseURI: http://ikm-group.ch/archiMEO/bdata\n" + 
				"# imports: http://ikm-group.ch/archimeo/bpaas\n" +
				"# imports: http://ikm-group.ch/archimeo/fbpdo\n" +
				"# imports: http://ikm-group.ch/archiMEO/questionnaire\n" +
				"# imports: http://ikm-group.ch/archimeo/apqc\n" +
				"# prefix: bdata\n" + 
				"\n" + 
				"@prefix apqc: <http://ikm-group.ch/archimeo/apqc#> .\n" + 
				"@prefix bdata: <http://ikm-group.ch/archiMEO/bdata#> .\n" + 
				"@prefix bpaas: <http://ikm-group.ch/archimeo/bpaas#> .\n" + 
				"@prefix fbpdo: <http://ikm-group.ch/archimeo/fbpdo#> .\n" + 
				"@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" + 
				"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" + 
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" + 
				"@prefix top: <http://ikm-group.ch/archiMEO/top#> .\n" + 
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" + 
				"@prefix questionnaire: <http://ikm-group.ch/archiMEO/questionnaire#> .\n"+
				"\n" + 
				"<http://ikm-group.ch/archiMEO/bdata>\n" + 
				"  rdf:type owl:Ontology ;\n" + 
				"  owl:imports <http://ikm-group.ch/archimeo/bpaas> ;\n" + 
				"  owl:versionInfo \"Created with TopBraid Composer\" ;\n" + 
				".";
		System.out.println(imports);
		
		for (int i = 0; i < op.getServices().size(); i++){
			//System.out.println("bdata:" +""+op.getServices().get(i).getName());
			String name=op.getServices().get(i).getName();
			name=name.trim();
			String firstLetter=name.substring(0, 1);
			if (firstLetter.matches("\\d.*")){
				name="cs"+name;
			};
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
		//TODO: STORE THE DATA INTO A PHYSICAL FILE IN FOLDER  ".\esources\\Output
		
		
		for(int i = 0; i < op.getInstances().size();i++)
		{
			 instance = op.getInstances();
			 	 
			
		}
		
	}

} 
