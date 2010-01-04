package edu.ncsa.model;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

/**
 * Utility functions used by the classes within this package.
 *  @author Kenton McHenry
 */
public class Utility
{
  /**
   * A simple structure to hold two objects.
   *  @param <T1> the type of the first object
   *  @param <T2> the type of the second object
   */
  public static class Pair<T1,T2> implements  Comparable, Serializable
  {
    public T1 first;
    public T2 second;
    
    public Pair() {}
    
    /**
     * Class constructor.
     *  @param a the first value
     *  @param b the second value
     */
    public Pair(T1 a, T2 b)
    {
      first = a;
      second = b;
    }
    
    /**
     * Compare this pair to another based on the first element.
     * Note: currently only supports doubles as the first element!
     *  @param o the pair to compare to
     *  @return the result (-1=less, 0=equal, 1=greater)
     */
    public int compareTo(Object o)
    {
    	if(o instanceof Pair && ((Pair)o).first instanceof Double){
    		return ((Double)first).compareTo((Double)((Pair)o).first);
    	}else{
    		return 0;
    	}
    }
  }
  
  /**
   * A simple structure to hold three objects.
   *  @param <T1> the type of the first object
   *  @param <T2> the type of the second object
   *  @param <T3> the type of the third object
   */
  public static class Triple<T1,T2,T3> implements  Comparable, Serializable
  {
    public T1 first;
    public T2 second;
    public T3 third;
    
    public Triple() {}
    
    /**
     * Class constructor.
     *  @param a the first value
     *  @param b the second value
     *  @param c the third value
     */
    public Triple(T1 a, T2 b, T3 c)
    {
      first = a;
      second = b;
      third = c;
    }
    
    /**
     * Compare this triple to another based on the first element.
     * Note: currently only supports doubles as the first element!
     *  @param o the pair to compare to
     *  @return the result (-1=less, 0=equal, 1=greater)
     */
    public int compareTo(Object o)
    {
    	if(o instanceof Triple && ((Triple)o).first instanceof Double){
    		return ((Double)first).compareTo((Double)((Pair)o).first);
    	}else{
    		return 0;
    	}
    }
  }

  /**
   * A class implementing a comparator for file names.
   */
  public static class FileNameComparer implements Comparator
  {
    /**
     * The method to compare two file names.
     *  @param obj1 the first object to compare
     *  @param obj2 the second object to compare
     *  @return the result (-1=less, 0=equal, 1=greater)
     */
    public int compare(Object obj1, Object obj2)
    {
      String str1 = (String)obj1;
      String str2 = (String)obj2;
      int tmpi;
      
      tmpi = str1.lastIndexOf('/');
      if(tmpi >= 0) str1 = str1.substring(tmpi+1);
      tmpi = str2.lastIndexOf('/');
      if(tmpi >= 0) str2 = str2.substring(tmpi+1);
      
      return str1.compareTo(str2);
    }
  }
  
  /**
   * An interface to allow for progress update callbacks.
   */
  public interface ProgressEvent
  {
  	public void progressEvent(double value, double total);
  }
  
  /**
   * Check if a file exists.
   *  @param filename the file name of the file to check
   *  @return true if the file exists
   */
  public static boolean exists(String filename)
  {
    File tmpf = new File(filename);
    return tmpf.exists();
  }
  
  /**
   * Get the specified line from the file indicated.
   *  @param filename the file to load from
   *  @param n the line number (starts at 1!)
   *  @return the line retrieved
   */
  public static String getLine(String filename, int n)
  {
    String line = "";
    
    try{
      BufferedReader ins = new BufferedReader(new FileReader(filename));
      int count = 0;
      
      while((line=ins.readLine()) != null){
        count++;
        if(count == n) break;
      }
      
      ins.close();
    }catch(Exception e) {}
    
    return line;
  }
  
  /**
   * Load a files contents into a string.
   *  @param filename the file to load
   *  @return the resulting string
   */
  public static String loadFile(String filename)
  {
    StringBuffer buffer = new StringBuffer();
      
    try{
      FileReader ins = new FileReader(filename);
      int c;
      
      while((c=ins.read()) != -1){
        buffer.append((char)c);
      }
      
      ins.close();
    }catch(Exception e){}
    
    return buffer.toString();
  }
  
  /**
   * Save a string to a file.
   *  @param filename the file to save to
   *  @param buffer the string to save
   */
  public static void saveFile(String filename, String buffer)
  {
    try{
      FileWriter outs = new FileWriter(filename);
      
      for(int i=0; i<buffer.length(); i++){
        outs.write(buffer.charAt(i));
      }
      
      outs.close();
    }catch(Exception e){}
  }
  
  /**
   * Copy a file from one location to another.
   *  @param source the source file location
   *  @param destination the destination file location
   */
  public static void copyFile(String source, String destination)
  {
    try{
       FileInputStream ins = new FileInputStream(source);
       Vector<Integer> buffer = new Vector<Integer>();
       int b;
       
       while((b=ins.read()) != -1){
         buffer.add(b);
       }
       
       ins.close();
       FileOutputStream outs = new FileOutputStream(destination);
       
       for(int i=0; i<buffer.size(); i++){
         outs.write(buffer.get(i));
       }
       
       outs.close();
    }catch(Exception e){}
  }
  
  /**
   * Convert all '\' to '/' so as to be consistent with Unix/Java paths.
   *  @param input the path to convert
   *  @return the convert path
   */
  public static String unixPath(String input)
  {
    String output = "";
    
    for(int i=0; i<input.length(); i++){
      if(input.charAt(i) == '\\'){
        output += '/';
      }else{
        output += input.charAt(i);
      }
    }
    
    return output;
  }
  
  /**
   * Convert an absolute path to a relative path.
   *  @param path the current path from which we want the relative path to the target file
   *  @param target the absolute name of the target file
   *  @return the relative path to the file
   */
  public static String relativePath(String path, String target)
  {
    String relative_path = "";
    String path_next = "";
    String target_next = "";
    
    Scanner sc_path = new Scanner(path);
    sc_path.useDelimiter("/");
    Scanner sc_target = new Scanner(target);
    sc_target.useDelimiter("/");
    
    //Skip all directories that are in common
    while(sc_path.hasNext() && sc_target.hasNext()){
      path_next = sc_path.next();
      target_next = sc_target.next();
      if(!path_next.equals(target_next)) break;  
    }
    
    //If we entered a different subtree, go back
    if(!path_next.equals(target_next)){
      relative_path += "../"; 
    }
    
    //Continue going back for each additional sub-directory
    while(sc_path.hasNext()){ 
      sc_path.next(); 
      relative_path += "../";
    } 
    
    //Add on the differing sub-directory we entered
    relative_path += target_next + "/";
    
    //Add on all addtional sub-directories in the new sub-tree
    while(sc_target.hasNext()){
      relative_path += sc_target.next() + "/";
    }
    
    return relative_path;
  }
  
  /**
   * Create an input stream for a file.
   *  @param filename the file to open
   *  @return the stream for this file
   */
  public static InputStream getInputStream(String filename)
  {
    InputStream is = null;
    
    try{
    	if(filename.contains("http://")){
    		is = (new URL(null, filename)).openStream();
    	}else{
    		if(filename.contains("file:/")){
    			filename = filename.substring(6);
    		}
    		
	      if(filename.contains(".gz")){
	        is = new GZIPInputStream(new FileInputStream(filename));
	      }else if(filename.contains(".zip")){
	        is = new ZipInputStream(new FileInputStream(filename));
	        ((ZipInputStream)is).getNextEntry();
	      }else{
	        is = new FileInputStream(filename);
	      }
    	}
    }catch(Exception e){
      e.printStackTrace();
    }
    
    return is;
  }
  
  /**
   * Sleep for the specfied number of mill-seconds.
   *  @param ms number of milli-seconds to sleep
   */
  public static void pause(int ms)
  {
    try{
      Thread.sleep(ms);
    }catch(Exception e) {e.printStackTrace();}
  }
  
  /**
   * Read the contents of the specified URL and store it in a string.
   *  @param url the URL to read
   *  @return a string containing the URL contents
   */
  public static String readURL(String url)
  {
    HttpURLConnection.setFollowRedirects(false);
    HttpURLConnection conn = null;
    String line;
    String data = "";
    
    try{
      conn = (HttpURLConnection)new URL(url).openConnection();
      conn.connect();
      
      BufferedReader ins = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      
      while((line = ins.readLine()) != null){
        data += line + "\n";
      }
      
      conn.disconnect();
    }catch(Exception e){
      e.printStackTrace();
    }finally{
      if(conn != null) conn.disconnect();
    }
    
    return data;
  }
  
  /**
   * Convert a string to an URL safe version (e.g. spaces -> %20).
   *  @param str the string to convert
   *  @return the URL safe version
   */
  public static String urlEncode(String str)
  {
    String str_new = "";
    
    for(int i=0; i<str.length(); i++){
      if(str.charAt(i) == ' '){
        str_new += "%20";
      }else if(str.charAt(i) == '#'){
        str_new += "%23";
      }else{
        str_new += str.charAt(i);
      }
    }
    
    return str_new;
  }
  
  /**
   * Convert an integer representing some number of bytes into a human readable string.
   *  @param x the number of bytes
   *  @return the human readable string
   */
  public static String getBytes(int x)
  {
    String str = "";
    
    if(x < 1024){
      str = Integer.toString(x) + " B";
    }else{
      x /= 1024;
      
      if(x < 1024){
        str = Integer.toString(x) + " KB";
      }else{
        x /= 1024;
        str = Integer.toString(x) + " MB";
      }
    }

    return str;
  }
  
  /**
   * Check if the specified URL exists.
   *  @param url the URL to check
   *  @return true if the URL exists
   */
  public static boolean existsURL(String url)
  {
    HttpURLConnection.setFollowRedirects(false);
    HttpURLConnection conn = null;
    boolean SUCCESS = false;
    
    try{
      conn = (HttpURLConnection)new URL(url).openConnection();
      conn.connect();
      
      //Try to open the stream, if it doesn't exist it will cause an exception.
      DataInputStream ins = new DataInputStream(new BufferedInputStream(conn.getInputStream()));
      ins.close();
      
      conn.disconnect();
      SUCCESS = true;
    }catch(FileNotFoundException e){
    }catch(Exception e){
      e.printStackTrace();
    }finally{
      if(conn != null) conn.disconnect();
    }
    
    return SUCCESS;
  }
  
  /**
   * Convert an integer to a string padding it to occupy the desired number of characters.
   *  @param i the integer to convert
   *  @param n the number of characters the result should have
   *  @return the string representation of the integer
   */
  public static String toString(int i, int n)
  {
    String tmp = Integer.toString(i);
    
    while(tmp.length() < n){
      tmp = "0" + tmp;
    }
    
    return tmp;
  }
  
  /**
   * Read lines from a stream until one is found that does not start with a '#'.
   *  @param ins the input stream
   *  @return a line from the stream
   */
  public static String nextUncommentedLine(BufferedReader ins)
  {
    String line;
    
    try{
      while((line=ins.readLine()) != null){
        if(line.charAt(0) != '#') return line;
      }
    }catch(Exception e){
      e.printStackTrace();
    }
    
    return null;
  }
  
  /**
   * Seperate a string into a vector of strings (seperated by the specified delimiter).
   *  @param line the string to split
   *  @param delimiter the delimiter to split on
   *  @param DROP_EMPTY_STRINGS true if we should ignore empty strings
   *  @return the vector of strings seperated from the given string
   */
  public static Vector<String> split(String line, char delimiter, boolean DROP_EMPTY_STRINGS)
  {
    Vector<String> tmpv = new Vector<String>();
    String tmp = "";
    
    for(int i=0; i<line.length(); i++){
      if(line.charAt(i)==delimiter){
        if(!tmp.isEmpty() || !DROP_EMPTY_STRINGS){
          tmpv.add(tmp);
          tmp = "";
        }
      }else{
        tmp += line.charAt(i);
      }
    }
    
    if(!tmp.isEmpty()) tmpv.add(tmp);
    
    return tmpv;
  }
  
  /**
   * Increment an array's index so as to always keep it withing bounds.
   *  @param index the current index
   *  @param inc the increment to the index (positive or negative)
   *  @param size the size of the array
   *  @return the new incremented index
   */
  public static int increment(int index, int inc, int size)
  {
    index += inc;
    
    while(index < 0){
      index = size + index;
    }
    
    index = index % size;
    
    return index;
  }
  
  /**
   * Determine if given double contains a valid value.
   *  @param d the double value to check
   *  @return true if not infinity or NaN
   */
  public static boolean isValid(double d)
  {
    if(Double.isNaN(d) || Double.isInfinite(d)){
      return false;
    }
    
    return true;
  }
  
  /**
   * Perform a deep copy of the given object.
   *  @param obj the object to copy
   *  @return the deep copy of the object
   */
  public static Object deepCopy(Object obj)
  {
  	Object obj_copy = null;
  	
  	try{
	  	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	  	ObjectOutputStream oos = new ObjectOutputStream(baos);
	  	oos.writeObject(obj);
	  	
	  	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	  	ObjectInputStream ois = new ObjectInputStream(bais);
  	  obj_copy =  ois.readObject();
  	}catch(Exception e){
  		e.printStackTrace();
  	}
  	
  	return obj_copy;
  }
  
  /**
   * Get the name of a file (i.e. no path)
   *  @param filename the absolute file name
   *  @return the name of the file (with extension)
   */
  public static String getFilename(String filename)
  {
  	String name = filename;
  	int tmpi;
  	
  	//Remove path
  	tmpi = filename.lastIndexOf('/');
  	
  	if(tmpi >= 0){
  	  name = filename.substring(tmpi+1);
  	}
  	
  	return name;
  }
  
  /**
   * Get the name of a file (i.e. no path and no extension)
   *  @param filename the absolute file name
   *  @return the name of the file
   */
  public static String getFilenameName(String filename)
  {
  	String name = filename;
  	int tmpi;
  	
  	//Remove path
  	tmpi = filename.lastIndexOf('/');
  	
  	if(tmpi >= 0){
  	  name = filename.substring(tmpi+1);
  	}
  	
  	//Remove extension
  	tmpi = name.lastIndexOf('.');
  	  
	  if(tmpi >= 0){
	  	name = name.substring(0, tmpi);
	  }
  	
  	return name;
  }
  
  /**
   * Get the extension of a file
   *  @param filename the absolute file name
   *  @return the extension of the file
   */
  public static String getFilenameExtension(String filename)
  {
  	String ext = "";
  	int tmpi = filename.lastIndexOf('.');
  	
  	if(tmpi >= 0){
  	  ext = filename.substring(tmpi+1);
  	}
  	
  	return ext;
  }
  
  /**
   * Get the path of a file
   *  @param filename the absolute file name
   *  @return the path to the file
   */
  public static String getFilenamePath(String filename)
  {
  	String path = "";
  	int tmpi = filename.lastIndexOf('/');
  	
  	if(tmpi >= 0){
  	  path = filename.substring(0, tmpi+1);
  	}
  	
  	return path;
  }
  
  /**
   * Return the union of unique elements within two vectors.
   * @param vector1 a vector of elements
   * @param vector2 a vector of elements
   * @return the union of the two vectors
   */
  public static Vector union(Vector vector1, Vector vector2)
  {
  	Vector vector = new Vector();
  	TreeSet set = new TreeSet();
  	
  	if(vector1 != null) set.addAll(vector1);
  	if(vector2 != null) set.addAll(vector2);
  	
  	vector.addAll(set);
  	
  	return vector;
  }
}