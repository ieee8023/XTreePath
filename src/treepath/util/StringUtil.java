package treepath.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

	
	static Pattern returns = Pattern.compile("\n");
	static Pattern tags = Pattern.compile("<[^>]+>");
	static Pattern alphaneumeric = Pattern.compile("[^a-zA-Z0-9 :.$!@#%\";=-_+<>/\\^*()\\\\{\\}\\[\\]]*");
	
	public static String cleanString(String dirtystr){
		
		if (dirtystr == null)
			return dirtystr;

		Matcher matcher = alphaneumeric.matcher(dirtystr);
		dirtystr = matcher.replaceAll("");
		
		dirtystr = cleanSpaces(dirtystr);
		
		return dirtystr;
		
	}
	
public static String cleanSpaces(String dirtystr){
		
		dirtystr = dirtystr.trim();
		
		Matcher returnsmatcher = returns.matcher(dirtystr);
		dirtystr = returnsmatcher.replaceAll(" ");
	
		while(dirtystr.contains("  ")){
			//System.out.println("Replacing...");
			dirtystr = dirtystr.replace("  ", " ");
		}
		
		
		return dirtystr;
		
	}
	
	
	public static String stripTags(String str){
		
		Matcher returnsmatcher = tags.matcher(str);
		str = returnsmatcher.replaceAll("");
		
		return str;
		
	}
	
	
	public static List<String> cleanResults(List<String> results) throws Exception{
		
		for (int i = 0; i < results.size(); i++) {

			String ret = results.get(i);
			
			ret = extractImgSrc(ret);

			ret = StringUtil.cleanString(ret);
			ret = StringUtil.stripTags(ret);

			if (ret.equals(""))
				throw new Exception("Empty String");

			if (ret.equals("nil"))
				throw new Exception("nil string");

			results.set(i, ret);
		}
		
		return results;
		
	}
	
	public static String extractImgSrc(String str){
		
		if (str.contains("<img")) {

			int start = str.indexOf("<img");

			start = str.indexOf("src", start);
			start = str.indexOf("=", start);
			start = str.indexOf("\"", start) + 1;
			int end = str.indexOf("\"", start);

			str = str.substring(start, end);

		}
		
		return str;
	}
	
	public static String stripOuterTag(String str){
		
		
		//strip outer tag
		
		int start = str.indexOf("<");
		start = str.indexOf(">", start) + 1;
		
		int end = str.lastIndexOf("<");
		
		if (start == -1 || end == -1)
			return str;
		else
			return str.substring(start, end).trim();
		
	}
	
}
