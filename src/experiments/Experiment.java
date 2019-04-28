package experiments;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import treepath.extractor.DataExtractor;
import treepath.extractor.GroundTruthDataExtractor;
import treepath.extractor.XPathDataExtractor;
import treepath.extractor.XTPathDataExtractor;
import treepath.util.DataModel;
import treepath.util.DataSetProcessor;
import treepath.util.SystemInfo;

public class Experiment {

	//static String DATASET = "data/swde-book/borders";
	//static String DATASET = "data/swde-book/barnesandnoble";
	static String DATASET = "data/swde-book/abebooks";   //1934/1999  v 1999/1999
	//static String DATASET = "data/swde-book/bookdepository";
	static boolean DEBUG = true;
	
	public static void main(String[] args) throws Exception {
		
		try{
			DATASET = args[0];
		}catch(Exception e){
			System.out.println("CLI args failed, Using Defaults");
		}
		
		//runExp(XPathDataExtractor.class);
		//System.gc();
		runExp(XTPathDataExtractor.class);
		
	}

	private static void runExp(Class<? extends DataExtractor> dataextractor) throws Exception {
		
		Map<String, List<DataModel>> data = DataSetProcessor.getExtractors(null,DATASET);
		
		System.err.println(SystemInfo.getMemory());
		
		
		for (final String domain : data.keySet()) {
		
			//DataExtractor de = new XPathDataExtractor(); //13/1999     // with 2 1978/1998
			//DataExtractor de = new TPathDataExtractor();
			//DataExtractor de = new XTPathDataExtractor();    //1360/1999
			
			
			DataExtractor de = dataextractor.newInstance();
			
			String method = de.getClass().getSimpleName();
			System.out.println("Using " + method);
			
			DataExtractor gt = new GroundTruthDataExtractor();
			
			if (DEBUG) System.out.print("============================== " + domain);
			
			final List<DataModel> datamodels = data.get(domain);
			// take the first half
			
			Set<DataModel> trainingModels = new HashSet<DataModel>();
			if (DEBUG) System.out.println(" Training on:");
			Iterator<DataModel> dmi = datamodels.iterator();
			//dmi.next();
			for (int i = 0; i < 1; i++){
				DataModel trainingDatamodel = dmi.next();
				//datamodels.remove(trainingDatamodel);
				if (DEBUG) System.out.println("  ----" + trainingDatamodel.domain);
				trainingModels.add(trainingDatamodel);
			}
			
			de.train(trainingModels);
			
			// now test
			int success = 0;
			int falsepositive = 0;
			int total = datamodels.size();
			long startTime = System.currentTimeMillis();
			for (final DataModel d : datamodels) {

				try{
					Map<String, String> res = de.extract(d);
					Map<String, String> gtres = gt.extract(d);
					
					if (res.equals(gtres))
						success++;
					else{
						if (DEBUG) System.out.println(d.domain + res + "\n" +d.domain + gtres);
						falsepositive++;
					}
					
				}catch(Exception e){
					//e.printStackTrace();
				}
			
			}
			long endTime = System.currentTimeMillis();
			long seconds = (endTime - startTime)/1000;
			System.out.println("method,domain,total,success,falsepositive,seconds");
			System.out.printf("# %s,%s,%s,%s,%s,%s\n", 
								method,domain,total,success,falsepositive,seconds);
			
			//System.out.println("success " + success + "/" + total);
			//System.out.println("fp " + fp + "/" + total);
			
		}
		
	}
	
	
}
