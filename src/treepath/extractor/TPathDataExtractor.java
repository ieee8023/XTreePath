package treepath.extractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dom4j.Element;

import treepath.readers.JtidyDom4jXPathReader;
import treepath.util.DataModel;
import treepath.util.FpException;
import treepath.util.Region;
import treepath.util.Item;
import treepath.util.StringUtil;

public class TPathDataExtractor implements DataExtractor {

	static int PARENTAL_SEARCH_SPACE = 0;
	static int LOWEST_MATCH = 1;
	static boolean DEBUG = true;
	static boolean VERBOSE = false;

	List<ExampleTreePath> trainingModels = new ArrayList<ExampleTreePath>();
	
	@Override
	public void train(Collection<DataModel> training) throws Exception{
		
		for (DataModel datamodel : training){
			
				JtidyDom4jXPathReader der = new JtidyDom4jXPathReader();
				der.setHtml(datamodel.html);
				
				ExampleTreePath treePath = new ExampleTreePath();
				
				Set<Element> targetElements = new HashSet<Element>();
				for (Item i : datamodel.items) {
					if (DEBUG) System.out.println(" Item: " + i.name);

					for (Region r : i.regions){
						Element targetElement = getElement(der, r.xpath);
						// get element to find common element
						targetElements.add(targetElement);
					}
				}
				
				Element prefixElement = findPrefixElement(targetElements);
				String prefixString = prefixElement.getUniquePath();
				prefixString = prefixString.replace("/*[name()='html']", "/html"); // patch for lib
				if (DEBUG) System.out.println(" Prefix Element=" + prefixString);
				
				treePath.tree = prefixElement;

				for (Item i : datamodel.items)
					for (Region r : i.regions)
						treePath.label2Elements.put(r.label, getElement(der, r.xpath));
					
				if (DEBUG) System.out.println("-- trained on " + treePath);
				trainingModels.add(treePath);
			}
	}
	
	@Override
	public Map<String, String> extract(DataModel d) throws Exception{
		
		JtidyDom4jXPathReader der = new JtidyDom4jXPathReader();
		der.setHtml(d.html);
		
		for (ExampleTreePath treePath: trainingModels){
			
			try{
				return extractUsingTreeSuffixes(d,treePath);
			}catch (Exception e){
				
			}
		}
		
		throw new Exception("Not found");
		
	}
	
	
	
	private static Map<String, String> extractUsingTreeSuffixes(DataModel d, ExampleTreePath treeSuffix) throws Exception{
		
		Map<String, String> label2Data = new TreeMap<String, String>();
		
		if (VERBOSE) System.out.println("TPath Searching: " + d.domain);
		
		for (Item i : d.items) {
			if (DEBUG) System.out.println(" Item: " + i.name);

			JtidyDom4jXPathReader der = new JtidyDom4jXPathReader();
			der.setHtml(d.html);

			Element rootOfSearch = getElement(der, "//body");

			List<ValueElementResult2> matchingResults = new ArrayList<ValueElementResult2>();
			searchTree(matchingResults, rootOfSearch.elements(), treeSuffix.tree, PARENTAL_SEARCH_SPACE);
			
			ValueElementResult2[] results = new ValueElementResult2[0];
			results = matchingResults.toArray(results);
			Arrays.sort(results);

			//System.out.println("Search: " + Arrays.toString(results));
			
			for (int j = 0; j < Math.min(1, results.length); j++) {
				if (DEBUG) System.out.println("  Found Common Node: score=" + results[j].value + ", xpath=" + results[j].e.getUniquePath().replace("/*[name()='html']", "/html"));
				//String newPrefix = results[j].e.getUniquePath().replace("/*[name()='html']", "/html");
				
				
				//Element subject = getElement(d.html, newPrefix);
				
				for (Region r : d.items.iterator().next().regions){
					//System.out.println("Looking for " + newPrefix + r.suffix);

					Element targetElement;
					String targetString;

					if (DEBUG) System.out.println(" Looking for " + r.label);
					targetElement = searchWithTreeMatching( treeSuffix.tree,
															treeSuffix.label2Elements.get(r.label),
															results[j].e);
					
					//targetElement = getElement(der, newPrefix + treeSuffix.label2Suffixes.get(label));
					
					if (targetElement == null)	{
							targetString = "Fail";
							throw new Exception("null result");
					}else{
						targetString = targetElement.getStringValue().trim();
					}
					
					if (targetString.equals("")){
						throw new Exception("empty result");
					}
							
					if (VERBOSE) System.out.println("   " + r.label + " = " + targetString);
					label2Data.put(r.label, targetString);
				}
			}
		}
		
		return label2Data;
	}
	
	
	
	private static class ExampleTreePath{
		
		Element tree;
		Map<String, Element> label2Elements = new HashMap<String, Element>();
	}
	
	
	
	private static Element searchWithTreeMatching(Element prefixInTraining, Element targetInTraining, Element prefixInTest) throws Exception{
		
		if (targetInTraining == null)
			throw new Exception("targetInTraining is null!");
			
		if (DEBUG) System.out.println("  Searching for(from training): " + targetInTraining.getUniquePath().replace("/*[name()='html']", "/html"));
		
		List<Element> pathToTarget = new ArrayList<Element>();
		
		Element tempTarget = targetInTraining;
		while (tempTarget != prefixInTraining){
			pathToTarget.add(tempTarget);
			tempTarget = tempTarget.getParent();
		}
		
		Collections.reverse(pathToTarget); // we should have prefix+1 to target now
		
		//System.out.println("TreeArray for: " + targetInTraining.getTextTrim());
		//for (Element e : pathToTarget)
		//	System.out.println(" -" +e.getUniquePath());
		
		
		Element targetRootOfSearch = prefixInTest;
		
		for(Element e : pathToTarget){
			Element result = getMatchingElementBelow(targetRootOfSearch, e);
			targetRootOfSearch = result;
			
		}
		
		//System.out.println(targetRootOfSearch.getStringValue());
		

		return targetRootOfSearch;
	}
	
	
	private static Element getMatchingElementBelow(Element rootOfSearch, Element toMatch) throws Exception{
		
		List<ValueElementResult2> matchingResults = new ArrayList<ValueElementResult2>();
		searchTree(matchingResults, rootOfSearch.elements(), toMatch, PARENTAL_SEARCH_SPACE);
		
		ValueElementResult2[] results = new ValueElementResult2[0];
		results = matchingResults.toArray(results);
		Arrays.sort(results);
		
		//System.out.println("Searching level:" + Arrays.toString(results));
		
		if (DEBUG) System.out.println("  Found: score=" + results[0].value + ", xpath=" + results[0].e.getUniquePath().replace("/*[name()='html']", "/html"));


		
		if (results[0].value < LOWEST_MATCH)
			throw new Exception("Match Value is too small");
		
		return results[0].e;

	}
	
	
	private static Element findPrefixElement(Set<Element> elements){
		
		Map<Element, Integer> elementCounts = new HashMap<Element, Integer>();
		
		for (Element e: elements)
			while (e.getParent()!= null){
				if (elementCounts.get(e) == null){
					elementCounts.put(e, 1);
				}else{
					elementCounts.put(e, elementCounts.get(e) + 1);
				}
				e = e.getParent();
			}
				
		List<ValueElementResult> commonElements = new ArrayList<ValueElementResult>();
		for (Element e : elementCounts.keySet())
			if (elementCounts.get(e) == elements.size())
				commonElements.add(new ValueElementResult(e.getUniquePath().length(), e));
		
		ValueElementResult[] results = new ValueElementResult[0];
		results = commonElements.toArray(results);
		Arrays.sort(results);
		//System.out.println("  Common Element: " + results[0].e.getUniquePath());
		return results[0].e;
	}
	
	
	private static org.dom4j.Element getElement(String html, String xpath) throws Exception {
		
		JtidyDom4jXPathReader reader = new JtidyDom4jXPathReader();
		reader.setHtml(html);
		org.dom4j.Node result = reader.getNode(xpath);
		
		if (result instanceof Element)
			return (Element) result;
		else
			return null;
		
	}
	
	private static void searchTree(List<ValueElementResult2> matchingResults, List<Element> toSearch, Element target, int parentalSearchSpace) throws Exception{
				
		for (Element e : toSearch){
			matchingResults.add(new ValueElementResult2(htmlTreeMatch(e, target), e));
			//matchingResults.add(new ValueElementResult2(simpleTreeMatch(e, target), e));
			
			while (parentalSearchSpace > 0){
				e = e.getParent();
				parentalSearchSpace--;
			}
			
			searchTree(matchingResults, e.elements(), target, parentalSearchSpace);
		}
		
	}
	
	
	private static org.dom4j.Element getElement(JtidyDom4jXPathReader der, String xpath) throws Exception {
		
		org.dom4j.Node result = der.getNode(xpath);
		
		if (result instanceof Element)
			return (Element) result;
		else
			return null;
		
	}
	
	
	private static double htmlTreeMatch(org.dom4j.Element a, org.dom4j.Element b) throws Exception{
		
		if (!a.getName().equals(b.getName()))
			return 0;
		
		int m = a.elements().size();
		int n = b.elements().size();
		
		double[][] M = new double[m+1][n+1];
		
		for (int i = 0; i <= m; i++)
			M[i][0] = 0;
		
		for (int j = 0; j <= n; j++)
			M[0][j] = 0;
		
		for (int i = 1; i <= m; i++)
			for (int j = 1; j <= n; j++){
				
				double val1 = M[i][j-1];
				double val2 = M[i-1][j];
				double val3 = M[i-1][j-1];
				
				List<Element> aElements = a.elements();
				List<Element> bElements = b.elements();
				double val4 = htmlTreeMatch(aElements.get(i-1), bElements.get(j-1));
				 
				//if (Math.max(Math.max(val1, val2), val3 + val4) > 1)
				//	throw new Exception("over 1 = " + Math.max(Math.max(val1, val2), val3 + val4));
				double cost = Math.max(Math.max(val1, val2), val3 + val4);
				M[i][j] = cost;
			}
				
		//for (int i = 0; i < m; i++)
			//System.out.println(Arrays.toString(M[i]));	
		
		//calculate match based on elements
		
		String[] attsToMatch = new String[]{"class", "style", "id", "name"};
		
		double attrmatch = 0;
		for(String s : attsToMatch){
			String aa = a.attributeValue(s);
			String ba = b.attributeValue(s);
			if (aa != null && aa.equals(ba))
				attrmatch += 1.0/(attsToMatch.length*1.0);
		}
		
		double match = (attrmatch*0.5) + 1;
		
		return M[m][n] + match;  // was +1
	}	
	
	
	@SuppressWarnings("unchecked")
	private static int simpleTreeMatch(org.dom4j.Element a, org.dom4j.Element b) throws Exception{
		
		if (!a.getName().equals(b.getName()))
			return 0;
		
		int m = a.elements().size();
		int n = b.elements().size();
		
		int[][] M = new int[m+1][n+1];
		
		for (int i = 0; i <= m; i++)
			M[i][0] = 0;
		
		for (int j = 0; j <= n; j++)
			M[0][j] = 0;
		
		for (int i = 1; i <= m; i++)
			for (int j = 1; j <= n; j++){
				
				int val1 = M[i][j-1];
				int val2 = M[i-1][j];
				int val3 = M[i-1][j-1];
				
				List<Element> aElements = a.elements();
				List<Element> bElements = b.elements();
				int val4 = simpleTreeMatch(aElements.get(i-1), bElements.get(j-1));
				 
				//if (Math.max(Math.max(val1, val2), val3 + val4) > 1)
				//	throw new Exception("over 1 = " + Math.max(Math.max(val1, val2), val3 + val4));
					
				M[i][j] = Math.max(Math.max(val1, val2), val3 + val4);
			}
				
		//for (int i = 0; i < m; i++)
			//System.out.println(Arrays.toString(M[i]));	
		
		return M[m][n] + 1;
	}
	
	
	private static class ValueElementResult2 implements Comparable<ValueElementResult2> {

		double value;
		Element e;
		
		public ValueElementResult2(double value, Element e) {
			super();
			this.value = value;
			this.e = e;
		}

		@Override
		public int compareTo(ValueElementResult2 o) {
			
			return (int) ((o.value - this.value)*100);
		}
		
//		@Override
//		public String toString() {
//			
//			//System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
//			//System.exit(1);
//			return "[.\\node[draw]{" + this.value + ":" + this.e.getUniquePath().replace("/*[name()='html']", "/html") + "};]";
//
//		}
		
	}
	
	private static class ValueElementResult implements Comparable<ValueElementResult> {

		int value;
		Element e;
		
		public ValueElementResult(int value, Element e) {
			super();
			this.value = value;
			this.e = e;
		}

		@Override
		public int compareTo(ValueElementResult o) {
			
			return o.value - this.value;
		}
	}
	
}

