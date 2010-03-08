package edu.ncsa.model;
import edu.ncsa.matrix.*;
import java.io.*;
import java.util.*;

/**
 * The interface for mesh signatures used to describe and compare 3D models.
 *  @author Kenton McHenry
 */
abstract public class MeshSignature
{
  protected Mesh mesh;
  protected Vector<double[]> signature = new Vector<double[]>();
  
  public MeshSignature() {}
  
  abstract public String getType();
  abstract public MeshSignature clone();
  abstract public void setSignature(Mesh m);
  
  /**
   * Save the signature to a file.
   *  @param filename the file to save to
   */
  public void save(String filename)
  {
    try{
      BufferedWriter outs = new BufferedWriter(new FileWriter(filename));
      
      for(int i=0; i<signature.size(); i++){
        for(int j=0; j<signature.get(i).length; j++){
          outs.write(signature.get(i)[j] + " ");
        }
        
        outs.newLine();       
      }
      
      outs.close();
    }catch(Exception e) {e.printStackTrace();}
  }
  
  /**
   * Load the signature from a file.
   *  @param filename the file to load from
   */
  public void load(String filename)
  {
    try{
      BufferedReader ins;
      Scanner sc;
      String line;
      double[] arr;
      int d = 0;
      int i;
      
      signature.clear();
            
      //Read file once to determine dimension
      ins = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));

      if((line=ins.readLine()) != null){
        sc = new Scanner(line);
        
        while(sc.hasNextDouble()){
        	sc.nextDouble();
        	d++;
        }
      }
      
      ins.close();
      
      //Read through a second time to get data
      if(d > 0){
	      ins = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
	
	      while((line=ins.readLine()) != null){
	        sc = new Scanner(line);
	        arr = new double[d];
	        i = 0;
	        
	        while(sc.hasNextDouble()){
	          arr[i++] = sc.nextDouble();
	        }
	        
	        signature.add(arr);
	      }
	      
	      ins.close();
      }
    }catch(Exception e){
    	e.printStackTrace();
    }
  }
  
  /**
   * Compare the model from which this signature is derived to another signature from another model.
   *  @param md another mesh signature
   *  @return the difference in models based on the signature (larger values indicate more different models)
   */
  public double compareTo(MeshSignature md)
  {
    double tmpd = 0;
    
    for(int i=0; i<signature.size(); i++){
      tmpd += MatrixUtility.distance(signature.get(i), md.signature.get(i));
    }
    
    return tmpd;
  }
  
  /**
   * Get the magnitude of this signature (treating it like one long vector).
   */
  public double magnitude()
  {
    double tmpd = 0;
    
    for(int i=0; i<signature.size(); i++){
      tmpd += MatrixUtility.distance(signature.get(i), new double[signature.get(i).length]);
    }
    
    return tmpd;
  }
  
  /**
   * Get a string representation of this signature.
   */
  public String toString()
  {
  	String str = "";
  	
  	for(int i=0; i<signature.size(); i++){
  	  str += "[" + i + "]: ";
  	  
  	  for(int j=0; j<signature.get(i).length; j++){
  	  	str += signature.get(i)[j];
  	  	if(j < signature.get(i).length-1) str += ", ";
  	  }
  	  
  	  str += "\n";
  	}
  	
  	return str;
  }
}