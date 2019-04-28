package treepath.extractor;

import java.util.Collection;
import java.util.Map;
import treepath.util.DataModel;

public class XTPathDataExtractor implements DataExtractor {

	static boolean DEBUG = false;
	
	private XPathDataExtractor xde = new XPathDataExtractor();
	private TPathDataExtractor tde = new TPathDataExtractor();
	
	@Override
	public void train(Collection<DataModel> training) throws Exception{
		
		xde.train(training);
		tde.train(training);
		
	}
	
	@Override
	public Map<String, String> extract(DataModel d) throws Exception{
		
		try{
			return xde.extract(d);
		}catch(Exception e){
			if (DEBUG)System.out.println("Trying TPath");
			return tde.extract(d);
		}
		
	}

	
}

