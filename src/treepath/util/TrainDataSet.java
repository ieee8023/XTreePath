package treepath.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;

import treepath.readers.JtidyDom4jXPathReader;

public class TrainDataSet {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		Map<String, List<DataModel>> data = DataSetProcessor.getExtractors("data/swde-university", null);

		for (String domain : data.keySet()) {
			
			System.out.println("=============================================");
			System.out.println(domain);

			List<DataModel> datamodels = data.get(domain);
			
			Set<Element> targetElements = new HashSet<Element>();
			
			for (DataModel datamodel : datamodels){
				if (datamodel.items.size() == 0)
					throw new Exception("Domain: " + datamodel.domain + " does not have any item files. Please create one");
				
				for (Item i : datamodel.items) {
					System.out.println(" Checking Item: " + i.name + " of " + datamodel.domain);
	
					JtidyDom4jXPathReader der = new JtidyDom4jXPathReader();
					der.setHtml(datamodel.html);
					
					for (Region r : i.regions){
						Element targetElement = getElement(der, r.xpath);
						
						if (targetElement == null){
							
							System.out.println("You were looking for: " + r.label  + " in " + datamodel.domain);
							System.out.println("I tried with XPath: " + r.xpath);
							System.out.println("But it didn't work so please update the .item file with the correct values");
							der.listXPaths();
							System.out.println("Done listing XPaths");
							System.exit(-9);
						}
						
					}
				}
			}
	}
	}
		
		public static org.dom4j.Element getElement(JtidyDom4jXPathReader der, String xpath) throws Exception {
			
			org.dom4j.Node result = der.getNode(xpath);
			
			if (result instanceof Element)
				return (Element) result;
			else
				return null;
			
		}
		
		
		public static String readXPath(String html, String xpath) throws Exception {
			
			
			JtidyDom4jXPathReader der = new JtidyDom4jXPathReader();
			der.setHtml(html);
			String result = der.readString(xpath);
			
			result = StringUtil.cleanString(result);
			

			return result;
			
		}
		
}
