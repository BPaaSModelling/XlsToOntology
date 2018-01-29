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
		op.parseOntology(pathOntology + "apqc.ttl");
		op.parseOntology(pathOntology + "bdata.ttl");
		op.parseOntology(pathOntology + "bpaas.ttl");
		op.parseOntology(pathOntology + "fbpdo.ttl");
		op.parseOntology(pathOntology + "questiondata.ttl");
		op.parseOntology(pathOntology + "questionnaire.ttl");
		ArrayList<OntologyInstance> instance;

		op.parseExcelFile(pathExcel);
		//System.out.println(op.getServices().size());
		PrintStream out = new PrintStream(new FileOutputStream(".\\resources\\Output\\bdata"+UUID.randomUUID()+".ttl"));
		System.setOut(out);
		System.out.println(".");
		
		for (int i = 0; i < op.getServices().size(); i++){
			//System.out.println("bdata:" +""+op.getServices().get(i).getName());
			System.out.println("bdata:"+ UUID.randomUUID());
			System.out.println("rdf:type bpaas:CloudService"+" "+";");
			
			for (int j = 0; j < op.getServices().get(i).getProperties().size(); j++){
				System.out.println(op.getServices().get(i).getProperties().get(j).name + " " + op.getServices().get(i).getProperties().get(j).value);
			}
			System.out.println("rdfs:label " + op.getServices().get(i).getName()+" ;");
			System.out.println(".");
		}
		//TODO: STORE THE DATA INTO A PHYSICAL FILE IN FOLDER  ".\\resources\\Output
		
		
		for(int i = 0; i < op.getInstances().size();i++)
		{
			 instance = op.getInstances();
			 	 
			
		}
		
	}

} 
