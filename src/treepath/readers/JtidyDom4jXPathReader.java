package treepath.readers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;
import org.w3c.tidy.Tidy;

public class JtidyDom4jXPathReader{

	protected String html;
	protected Document doc;

	public void setDocument(Document dom){
		doc = dom;
	}
	
	
	public void setHtml(String html) throws Exception {
		this.html = html;

		InputStream input = new ByteArrayInputStream(new String(html).getBytes());
		
		Tidy tidy = new Tidy();
		tidy.setXmlOut(true);
		tidy.setShowErrors(0);
		tidy.setShowWarnings(false);
		tidy.setErrfile("tidy.err");
		tidy.setOnlyErrors(true);
		tidy.setQuiet(true);
		org.w3c.dom.Document domDocument = tidy.parseDOM(input, null);
		
		DOMReader domReader = new DOMReader();
		doc =  domReader.read(domDocument);

	}

	
	//public Node readNode(String xpath_str) throws Exception{
		
		
	//}
	

	public String readString(String xpath_str) throws Exception {

		//xpath_str = "/html/body/div/div/table[1]/tr[2]/td[1]/div[3]/table[1]/tr[3]/td[16]/a[1]";
		//xpath_str = "//tr[3]/td[16]/a";
		///html/body/div[4]/div[2]/table/tbody/tr[2]/td/div[3]/table/tbody/tr[3]/td[16]/a
		//List list = document.selectNodes( "//foo/bar" );
		
		Node node = doc.selectSingleNode(xpath_str);
		
		if (node == null || "".equals(node.getStringValue().trim())){
			 //print(doc.getRootElement(), "");
			 System.out.println("Please select the correct xpath to work with dom4j.  This xpath didn't work: " + xpath_str);
			 //System.exit(-9);
			 throw new Exception("Xpath does not work");
		}

		return node.getStringValue().trim();


	}
	
	public Node getNode(String xpath_str) throws Exception {

		//xpath_str = "/html/body/div/div/table[1]/tr[2]/td[1]/div[3]/table[1]/tr[3]/td[16]/a[1]";
		//xpath_str = "//tr[3]/td[16]/a";
		///html/body/div[4]/div[2]/table/tbody/tr[2]/td/div[3]/table/tbody/tr[3]/td[16]/a
		//List list = document.selectNodes( "//foo/bar" );
		
		Node node = doc.selectSingleNode(xpath_str);
		if (node == null || node.getStringValue().trim().equals(""))
			return null;
		else
			return node;

	}
	
	
	public void listXPaths(){
		
		 print(doc.getRootElement(), "");

	}
	
	//save last result for speed
	static Map<String,String> lastResult  = new HashMap<String, String>();
	public String searchXPaths(String search, String attribute) throws Exception{
		Map<String,String> xpaths = new HashMap<String,String>();
		
		
		// check last xpath. Maybe it works
		try{
			String lastXpath = lastResult.get(attribute);
			if (search.equals(this.readString(lastXpath).trim())){
					//System.out.println("last result worked!");
					//System.exit(0);
					return lastXpath;
			}
		}catch(Exception e){}
		
		//gather all xpaths
		search(doc.getRootElement(), "", xpaths);

		for (String x :  xpaths.keySet()){
			if (search.equalsIgnoreCase(xpaths.get(x).trim())){
					//System.out.println(x + " " + xpaths.get(x));
					lastResult.put(attribute, x);
					return x;
			}
		}
		
		
		throw new Exception("No xpath found");
	}
	
	
    public void search(Element node, String indent, Map<String,String> xpaths) {
    	if (!"".equals(node.getTextTrim())){
    		
    		List<String> newXPaths = getXPaths(node);
			String xpath = node.getUniquePath().replace("/*[name()='html']", "/html");
			//System.out.println("UniqueXPath:" + xpath);
			newXPaths.add(xpath);
    		
    		for (String s : newXPaths){
    			
    			
    			// filter out non-absolute xpaths
    			if (s.contains("/html")){
    			
					if (doc.selectSingleNode(s) != null){
						String newXPathResult = doc.selectSingleNode(s).getStringValue();
						
						//System.out.println("Relookup:" + StringUtil.cleanString(newXPathResult));
						
						
						if (newXPathResult.contains(node.getTextTrim()) || newXPathResult.contains(node.getStringValue())){
							//System.out.println("XPath matches!!!!!!!!!!!!!!!!!!: " + doc.selectSingleNode(s).getText());
							
							newXPathResult = newXPathResult.replace('\n', ' ');
							//System.out.println(s + " = " + newXPathResult.substring(0, Math.min(100, newXPathResult.length())));
							
							xpaths.put(s, newXPathResult);
						}
					}
    			}
    		}

    	}
    	
    	
    	final Element el = ((Element)node);
    	Iterator<Element> iter = el.elementIterator();
    	
    	while(iter.hasNext()){
    		search(iter.next(), indent+" ", xpaths);
    	}
    	
//        for (Element e : new Iterable<Element>() {
//
//			@Override
//			public Iterator<Element> iterator() {
//				return el.elementIterator();
//			}
//        	 
//        	
//		}){
//        	search(e, indent+" ", xpaths);
//        }
    }
	
	
	
	
	
	
	
    public void print(Element node, String indent) {
    	if (!"".equals(node.getTextTrim())){
    		
    		List<String> newXPaths = getXPaths(node);
			String xpath = node.getUniquePath().replace("/*[name()='html']", "/html");
			//System.out.println("UniqueXPath:" + xpath);
			newXPaths.add(xpath);
    		
    		for (String s : newXPaths){
    			
    			
    			
				if (doc.selectSingleNode(s) != null){
					String newXPathResult = doc.selectSingleNode(s).getStringValue();
					
					//System.out.println("Relookup:" + StringUtil.cleanString(newXPathResult));
					
					
					if (newXPathResult.contains(node.getTextTrim()) || newXPathResult.contains(node.getStringValue())){
						//System.out.println("XPath matches!!!!!!!!!!!!!!!!!!: " + doc.selectSingleNode(s).getText());
						
						newXPathResult = newXPathResult.replace('\n', ' ');
						System.out.println(s + " = " + newXPathResult.substring(0, Math.min(100, newXPathResult.length())));
					}
				}
    		}

    	}
    	
    	
    	final Element el = ((Element)node);
        for (Element e : new Iterable<Element>() {

			@Override
			public Iterator<Element> iterator() {
				return el.elementIterator();
			}
        	 
        	
		}){
        	print(e, indent+" ");
        }
    }
    
    
    public static List<String> getXPaths(Element node){
    	
    	List<String> xpaths = new ArrayList<String>();
    	
    	getXPath(node,"", xpaths);
    	
    	return xpaths;
    }
    
	public static String getXPath(Element node, String suffix,  List<String> xpaths){
		//System.out.println(node.getName());
		
		if  (node.getParent() != null){
			
			suffix = "/" + node.getName() + "[" + (node.getParent().elements().indexOf(node)+1)  + "]" + suffix;
			
			String partialxpath = getXPath(node.getParent(), suffix,  xpaths);
			xpaths.add(partialxpath + suffix);
			//System.out.println(partialxpath + suffix);
			return partialxpath;
		}
		else{
			return "/";
			
		}
	}


	public String getProcessedHtml() {

		String xhtml = doc.asXML();
		return xhtml;
	}
    
	
}







