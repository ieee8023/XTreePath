package treepath.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import treepath.readers.JtidyDom4jXPathReader;

public class ConvertSWDE {

	public static void main(String[] args) throws Exception {
	
		// TODO: please change to your root directory of ground-truth files
		
		String root = "data/SWDE_Dataset";
		//String root = "/Users/ieee8023/Desktop/data/SWDE_Dataset";
		
        String root_groundtruth = root + "/groundtruth";
        String root_webpages = root + "/webpages";

        Collection<File> groundtruth = FileUtils.listFiles(new File(root_groundtruth), 
           new IOFileFilter() {
			@Override
			public boolean accept(File dir, String name) { return false; }
			@Override
			public boolean accept(File file) {
				
				if (file.getName().endsWith(".txt"))
					return true;
				else
					return false;
			}
		}, new IOFileFilter() {
			@Override
			public boolean accept(File dir, String name) { return true; }
			@Override
			public boolean accept(File file) { return true; }
		});
        
        /////////////////////////////////////////////
        System.out.println("getting gt keys");
        
        Map<String, String> vertical2website = new HashMap<String,String>();
        Map<String, String> website2vertical = new HashMap<String,String>();
        Map<String, Set<String>> website2attribute = new HashMap<String,Set<String>>();
        
        for (File f : groundtruth){
        	
        	String s = f.getName();
        	
        	String[] parsed = s.split("[-.]");
        	
            String verticalName = parsed[0];
            String websiteName = parsed[1];
            String attributeName = parsed[2];
            
            
            // ignore missing parts of dataset
            //if (verticalName.equals("auto") || verticalName.equals("book"))
            //	continue;
            
            vertical2website.put(verticalName, websiteName);
            website2vertical.put(websiteName, verticalName);
            
            Set<String> attributes = website2attribute.get(websiteName);
            if (attributes == null) attributes = new HashSet<String>();
            attributes.add(attributeName);
            website2attribute.put(websiteName, attributes);
            
        }
        
        
        
        
        
        
        //////////////////////////////
        System.out.println("getting webpages");
        
        Map<String, Set<String>> website2pages = new HashMap<String,Set<String>>();
        
        for (String virticalName : vertical2website.keySet()){
        	
        	System.out.println(virticalName);
        	
        	
            Collection<File> webpages = FileUtils.listFiles(new File(root_webpages + "/" + virticalName), 
                    new IOFileFilter() {
         			@Override
         			public boolean accept(File dir, String name) { return false; }
         			@Override
         			public boolean accept(File file) {
         				
         				if (file.getName().endsWith(".htm"))
         					return true;
         				else
         					return false;
         			}
         		}, new IOFileFilter() {
         			@Override
         			public boolean accept(File dir, String name) { return true; }
         			@Override
         			public boolean accept(File file) { return true; }
         		});
        	
        	for (File page : webpages){
        		
        		String webpagename = page.getAbsolutePath();
        		webpagename = webpagename.substring(webpagename.indexOf(virticalName + "-") + (virticalName + "-").length(),webpagename.length());
        		webpagename = webpagename.substring(0,webpagename.indexOf("("));
        		
        		//String contents = FileUtils.readFileToString(page);
        		
        		Set<String> pages = website2pages.get(webpagename);
                if (pages == null) pages = new HashSet<String>();
                pages.add(page.getAbsolutePath());
                website2pages.put(webpagename, pages);
        		
        		
        	}
        	//System.exit(0);
        }
        
        
        
        
        
        
        
     
        
      

int fails=0;
int success = 0;       
for(String websiteName : website2attribute.keySet()){    
	
	String verticalName = website2vertical.get(websiteName);
	
	for (String page : website2pages.get(websiteName)){
		
		try{
			//System.out.println(page);
			JtidyDom4jXPathReader der = new JtidyDom4jXPathReader();
			String html = FileUtils.readFileToString(new File(page));
			der.setHtml(html);
			
	
			String id = page.substring(page.length()-8,page.length()-4);
			
			//System.out.println(id);
			
			String pageFolder = "data/swde-" + verticalName + "/" + websiteName + "/" + id;
			
			String attributes = "{\n";
			
			for(String attributeName : website2attribute.get(websiteName)){
	    		//System.out.println(" " + attributeName);

	    		String groundtruthFile = String.format("%s/%s/%s-%s-%s.txt",root_groundtruth, verticalName, verticalName, websiteName, attributeName);
	    		
	    		GroundtruthContainer gc = GroundtruthContainer.LoadFromCache(groundtruthFile);
	            
	           // System.out.println(gc._pageId_attValList.get(id).get(0));
	    		
	            try{
	            
	            	String xpath = der.searchXPaths(gc._pageId_attValList.get(id).get(0).trim(), attributeName);
	            	
	            	//System.out.println(xpath);
	            	attributes += "\"" + attributeName + "\": '" + xpath + "',\n";
	            	success++;
	            }catch(Exception e){
	            	fails++;
	            	
	            }
	
	            //System.out.println(success + "/" + fails);
	    	}
			
			attributes += "}\n";
			
			
			new File(pageFolder).mkdirs();
			
			FileUtils.writeStringToFile(new File(pageFolder + "/data.htm"), html);
			
			
			FileUtils.writeStringToFile(new File(pageFolder + "/1.item"), attributes);
		}catch (Exception e){
			
		}
	}
}
System.out.println(success + "/" + fails);
System.exit(0);        
      
        
        
        
        
        
        
        
        
        
        
//////////////////////////////
System.out.println("building gt");        
        
        
        //for (String v: vertical2website.keySet())
        for(String websiteName : website2attribute.keySet()){
        	System.out.println(websiteName);
        	for(String attributeName : website2attribute.get(websiteName)){
        		System.out.println(" " + attributeName);

        		String verticalName = website2vertical.get(websiteName);
        		
        		String groundtruthFile = String.format("%s/%s/%s-%s-%s.txt",root_groundtruth, verticalName, verticalName, websiteName, attributeName);
        		
        		GroundtruthContainer gc = new GroundtruthContainer();
                gc.LoadFromFile(groundtruthFile);
        		
                //Pattern.compile("(\\d-?){13}").matcher("").group();

        		
        		
        	}
        }
        
        System.exit(0);
        
        // specify vertical, website, and attribute
        String verticalName = "book";
        String websiteName = "buy";
        String attributeName = "author";

        // locate the corresponding ground-truth file
        String groundtruthFile = String.format("%s/%s/%s-%s-%s.txt",root_groundtruth, verticalName, verticalName, websiteName, attributeName);

        // create a ground-truth container and load data from the file
		GroundtruthContainer gc = new GroundtruthContainer();
        gc.LoadFromFile(groundtruthFile);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        // get ground-truth attribute value(s) by specifying a page-ID
        for (;;)
        {
            System.out.println("Please specify a page-ID: ");
            String pageId = br.readLine();
            System.out.println("trying to find " + pageId);
            if (pageId == "exit")
            {
                break;
            }

            List<String> attValListInPage = gc.GetGroundtruthByPageId(pageId);

            if (attValListInPage != null && attValListInPage.size() > 0)
            {
                for (String val : attValListInPage)
                {
                    System.out.println(val);
                }
            }
        }
	}
	
	
	
	
	
	
	
	
	
	
	
	static class GroundtruthContainer
    {
		
		
		static Map<String,GroundtruthContainer> cache = new HashMap<String, ConvertSWDE.GroundtruthContainer>();
		
		
		public static GroundtruthContainer LoadFromCache(String file) throws IOException{
		
			GroundtruthContainer ret = cache.get(file);
			
	    	if (ret == null){
	    		ret = new GroundtruthContainer();
	    		ret.LoadFromFile(file);	
	    	}
	    	
	    	cache.put(file, ret);
	    	
	    	return ret;
		}
		
        public void LoadFromFile(String file) throws IOException {

        	
            if (!(new File(file).exists()))
            {
                System.out.println("Error: cannot find ground-truth file " + file);
                return;
            }

            // initialize container
            this._pageId_attValList = new HashMap<String, List<String>>();

            //System.out.println("looking at " + file);
            // read ground-truth information from the given file
            BufferedReader sr = new BufferedReader(new FileReader(file));
           // using (StreamReader sr = new StreamReader(file, Encoding.UTF8))
            {
                // read names of vertical, website, and attribute
                String[] header = sr.readLine().split("\t");
                if (header.length == 3)
                {
                    _verticalName = header[0];
                    _websiteName = header[1];
                    _attributeName = header[2];
                    //System.out.println(" Vertical = " + header[0]);
                    //System.out.println("  Website = " + header[1]);
                    //System.out.println("Attribute = " + header[2]);
                }
                else
                {
                    System.out.println("Error: fail to read header");
                }

                // read related statistics
                String[] statistics = sr.readLine().split("\t");
                if (statistics.length == 4)
                {
                    //System.out.format("\n%s out of %s pages contain:", statistics[1], statistics[0]);
                    //System.out.format("%s attribute values (%s unique)", statistics[2], statistics[3]);
                }
                else
                {
                    System.out.println("Error: fail to read statistics");
                }

                // read ground-truth attribute values in each page
                String line;
                while ((line = sr.readLine()) != null)
                {
                    String[] infoLine = line.split("\t");
                    if (infoLine.length > 2)
                    {
                        String pageId = infoLine[0];
                        int numAttValsInPage = Integer.parseInt(infoLine[1]);
                        
                        List<String> attValListInPage;
                        if (numAttValsInPage > 0){
                        	
                        	attValListInPage = Arrays.asList(infoLine).subList(2, 2+numAttValsInPage);
                        	
                        	//attValListInPage = new Arraylist(infoLine).;
                        	
                        	//.GetRange(2, numAttValsInPage)
                        }else{
                        	attValListInPage = new ArrayList<String>();
                        }
                        
                        _pageId_attValList.put(pageId, attValListInPage);
                    }
                }
            }

            // optionally, check result
            int numPagesWithAttVals = 0;
            int numAttVals = 0;
            for (List<String> attValList : _pageId_attValList.values())
            {
                if (attValList.size() > 0)
                {
                    ++numPagesWithAttVals;
                    numAttVals += attValList.size();
                }
            }
            System.out.format(" Loaded ground-truth: %s attribute values contained in %s out of %s pages\n", numAttVals, numPagesWithAttVals, _pageId_attValList.size());
            sr.close();
        }

        public List<String> GetGroundtruthByPageId(String pageId)
        {
            if (_pageId_attValList == null)
            {
                System.out.println("Error: invalid ground-truth data");
                return null;
            }

            List<String> attValList = null;
            if (pageId != null && _pageId_attValList.containsKey(pageId)) // find attribute value(s)
            {
            	attValList = _pageId_attValList.get(pageId);
            	
                System.out.format("Loaded %s ground-truth attribute value(s) for page %s", attValList.size(), pageId);
            }
            else
            {
                System.out.format("Error: the given page-ID %s is invalid", pageId);
            }
            return attValList;
        }

        // container for attribute values of each web page
        private Map<String, List<String>> _pageId_attValList = null; // pageId ~ {attributeValue}

        // names of vertical, website, and attribute
        private String _verticalName;
        private String _websiteName;
        private String _attributeName;
    }
	
	
	
	
	
	

}
