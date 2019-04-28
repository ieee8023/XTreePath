package treepath.readers;

import java.util.List;

public interface XPathReader {

	public void setHtml(String html) throws Exception;
	
	public List<String> read(String xpath) throws Exception;
	
}
