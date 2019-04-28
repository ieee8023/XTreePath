package treepath.extractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.dom4j.Element;

import treepath.readers.JtidyDom4jXPathReader;
import treepath.util.DataModel;
import treepath.util.FpException;
import treepath.util.Region;
import treepath.util.StringUtil;

public class XPathDataExtractor implements DataExtractor {

	static boolean DEBUG = true;
	
	List<ExampleXPath> trainingModels = new ArrayList<ExampleXPath>();
	
	@Override
	public void train(Collection<DataModel> training){
		
		for (DataModel datamodel : training){
			ExampleXPath x = new ExampleXPath();
			for (Region r : datamodel.items.iterator().next().regions){
				x.label2XPaths.put(r.label, r.xpath);
			}
			if (DEBUG) System.out.println("-- trained on " + x);
			trainingModels.add(x);
		}
	}
	
	@Override
	public Map<String, String> extract(DataModel d) throws Exception{
		
		JtidyDom4jXPathReader der = new JtidyDom4jXPathReader();
		der.setHtml(d.html);
		
		
		for (ExampleXPath x: trainingModels){
			
			Map<String, String> result = new TreeMap<String, String>();
			boolean success = true;
			
			for (String label : x.label2XPaths.keySet()){
				
				Element targetElement = getElement(der, x.label2XPaths.get(label));
				
				if (targetElement != null){
					String targetString = targetElement.getStringValue();
					targetString = targetString.trim();
					if (DEBUG) System.out.println(label + ": " + targetElement.getStringValue());
					
					result.put(label, targetString);
				}else{
					if (DEBUG) System.out.println("Missed value:" + label);
					success = false;
				}
			}
			
			if (success){
				return result;
			}
		}
		throw new Exception("Not found");
		
	}
	
	private static org.dom4j.Element getElement(JtidyDom4jXPathReader der, String xpath) throws Exception {
		
		org.dom4j.Node result = der.getNode(xpath);
		
		if (result instanceof Element)
			return (Element) result;
		else
			return null;
		
	}
	
	private static class ExampleXPath{
		
		Map<String, String> label2XPaths = new HashMap<String, String>();
		
		@Override
		public String toString() {

			return label2XPaths.toString();
		}
	}
	
}

