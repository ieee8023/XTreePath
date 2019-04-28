package treepath.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.codehaus.jettison.json.JSONObject;


public class DataSetProcessor {

	private static int INSTANCELIMIT = 10000;//100;
	
	private static final Pattern nonASCII = Pattern.compile("[^\\x00-\\x7f]");
	
	/**
	 * 
	 * 
	 * @param datasetlocation
	 * @return a Map from domain to a set of datamodels with regions
	 * @throws Exception
	 */
	public static Map<String,List<DataModel>> getExtractors(String datasetlocation, String datasetdomainlocation) throws Exception {
		
		Map<String,List<DataModel>> datasets = new TreeMap<String,List<DataModel>>();
		
		File[] domains;
		
		if (datasetdomainlocation == null)
			domains = new File(datasetlocation).listFiles(new NoSVNFileFilter());
		else
			domains = new File[]{new File(datasetdomainlocation)};
		
		for (File domainFolder : domains){
			//System.out.println("=============================");
			System.out.println("Domain: " + domainFolder.getName());
			List<DataModel> datamodels = new ArrayList<DataModel>();
			for (File instanceFolder : domainFolder.listFiles(new NoSVNFileFilter())){
				//System.out.println(" Instance: " + instanceFolder.getName());
				
				try{
					DataModel dataset = new DataModel();
					if (!instanceFolder.isFile()){
						//we have a folder
						//System.out.println("=========");
						//System.out.println("domain=" + folder.getName());
						dataset.domain = domainFolder.getName() + "/" + instanceFolder.getName();
						
						// get the data.html file

						File datafile = new File(instanceFolder.getAbsolutePath() + "/data.htm");
						dataset.html = FileUtils.readFileToString(datafile);
						
						
						
						if (dataset.html == null)
							throw new Exception("Can't read data file in the \"" + instanceFolder.getName() + "\" folder");
						
						// get the regions
						for (File f : instanceFolder.listFiles(new NoSVNFileFilter()))
							if (f.getName().endsWith(".item")){
								
								JSONObject jo = new JSONObject(FileUtils.readFileToString(f));
								
								Item item = new Item();
								item.name = f.getName();
								dataset.items.add(item);
								
								for(int i = 0 ; i < jo.names().length() ; i++){
									String name = jo.names().getString(i);
									String target = jo.getString(name);
									//System.out.println(" " + name + "=" + target);
									
									Region region = new Region();
									region.label = name;
									region.xpath = target;
									item.regions.add(region);
								}
							}
					}
					datamodels.add(dataset);
				}catch(Exception e){
						System.out.println("Error: " + instanceFolder + " - " + e.getCause() + " " + e.getMessage());
						e.printStackTrace();
					
					
				}
				
			}
			
			if (datamodels.size() > INSTANCELIMIT){
				System.err.println("Reducing " + datamodels.size() + " to " + INSTANCELIMIT + " instances");
				// to reduce the size of the input
				while(datamodels.size() > INSTANCELIMIT){
					datamodels.remove((int)(Math.random()*datamodels.size()));
				}
			}else{
				System.err.println("Not reducing num instances. Total: " + datamodels.size());
			}
				
			datasets.put(domainFolder.getName(), datamodels);
		}
		
		return datasets;
	}

}


class NoSVNFileFilter implements FilenameFilter{

	@Override
	public boolean accept(File dir, String name) {

		return !name.contains(".svn");
	}
}
