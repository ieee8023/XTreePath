package treepath.util;

public class SystemInfo {

	
	
	public static String getMemory(){
		
		Runtime runtime = Runtime.getRuntime();

	    StringBuilder sb = new StringBuilder();
	    long maxMemory = runtime.maxMemory();
	    long allocatedMemory = runtime.totalMemory();
	    long freeMemory = runtime.freeMemory();

	    sb.append("free memory: " + humanReadableByteCount(freeMemory) + ",");
	    sb.append("allocated memory: " + humanReadableByteCount(allocatedMemory) + ",");
	    sb.append("max memory: " + humanReadableByteCount(maxMemory) + ",");
	    sb.append("total free memory: " + humanReadableByteCount((freeMemory + (maxMemory - allocatedMemory))));
	    
	    return sb.toString();
	}
	
	public static String humanReadableByteCount(long bytes) {
		boolean si = true;
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + "B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f%sB", bytes / Math.pow(unit, exp), pre);
	}
	
	
}
