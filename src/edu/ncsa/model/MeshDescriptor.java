package edu.ncsa.model;
import edu.ncsa.matrix.*;
import java.io.*;
import java.util.*;

/**
 * The interface for mesh descriptors used to describe and compare 3D models.
 *  @author Kenton McHenry
 */
abstract public class MeshDescriptor
{
  protected Mesh mesh;
  protected Vector<double[]> descriptor = new Vector<double[]>();
  
  public MeshDescriptor() {}
  
  abstract public String getType();
  abstract public MeshDescriptor clone();
  abstract public void setDescriptor(Mesh m);
  
  /**
   * Save the descriptor to a file.
   *  @param filename the file to save to
   */
  public void save(String filename)
  {
    try{
      BufferedWriter outs = new BufferedWriter(new FileWriter(filename));
      
      for(int i=0; i<descriptor.size(); i++){
        for(int j=0; j<descriptor.get(i).length; j++){
          outs.write(descriptor.get(i)[j] + " ");
        }
        
        outs.newLine();       
      }
      
      outs.close();
    }catch(Exception e) {}
  }
  
  /**
   * Load the descriptor from a file.
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
      
      descriptor.clear();
            
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
	        
	        descriptor.add(arr);
	      }
	      
	      ins.close();
      }
    }catch(Exception e){
    	e.printStackTrace();
    }
  }
  
  /**
   * Compare the model from which this descriptor is derived to another descriptor from another model.
   *  @param md another mesh descriptor
   *  @return the difference in models based on the descriptor (larger values indicate more different models)
   */
  public double compareTo(MeshDescriptor md)
  {
    double tmpd = 0;
    
    for(int i=0; i<descriptor.size(); i++){
      tmpd += MatrixUtility.distance(descriptor.get(i), md.descriptor.get(i));
    }
    
    return tmpd;
  }
  
  /**
   * Get the magnitude of this descriptor (treating it like one long vector).
   */
  public double magnitude()
  {
    double tmpd = 0;
    
    for(int i=0; i<descriptor.size(); i++){
      tmpd += MatrixUtility.distance(descriptor.get(i), new double[descriptor.get(i).length]);
    }
    
    return tmpd;
  }
  
  /**
   * Get a string representation of this descriptor.
   */
  public String toString()
  {
  	String str = "";
  	
  	for(int i=0; i<descriptor.size(); i++){
  	  str += "[" + i + "]: ";
  	  
  	  for(int j=0; j<descriptor.get(i).length; j++){
  	  	str += descriptor.get(i)[j];
  	  	if(j < descriptor.get(i).length-1) str += ", ";
  	  }
  	  
  	  str += "\n";
  	}
  	
  	return str;
  }
}