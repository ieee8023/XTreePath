package experiments;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.dom4j.Element;

import treepath.readers.JtidyDom4jXPathReader;

public class XPathTest {

	public static void main(String[] args) throws Exception {
		
		String htmlPath = "data/swde-book/abebooks/0097/data.htm";
		String xpath = "/html/body/div[2]/div[2]/div[2]/div[1]/div[3]/strong[3]";
		
		String html = FileUtils.readFileToString(new File(htmlPath));
		
		JtidyDom4jXPathReader der = new JtidyDom4jXPathReader();
		der.setHtml(html);
		//der.listXPaths();
		
		org.dom4j.Node result = der.getNode(xpath);
		System.out.println(result);
		System.out.println(result.getStringValue());
		

	}

	
	
	
	private static org.dom4j.Element getElement(JtidyDom4jXPathReader der, String xpath) throws Exception {
		
		org.dom4j.Node result = der.getNode(xpath);
		
		if (result instanceof Element)
			return (Element) result;
		else
			return null;
		
	}
}
