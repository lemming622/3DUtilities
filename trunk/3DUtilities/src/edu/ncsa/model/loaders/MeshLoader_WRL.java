package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import kgm.utility.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.wrl files.
 *  @author Kenton McHenry
 */
public class MeshLoader_WRL extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "wrl";
	}
	
  /**
   * Load a VRML 1.0 model.
   *  @param filename the file to load
   *  @return the loaded mesh
   */
  public Mesh load(String filename)
  {
  	Mesh mesh = null;
  	
    if(Utility.getLine(filename, 1).equals("#VRML V1.0 ascii")){
	    mesh = new Mesh(); mesh.addFileMetaData(filename);
	    Vector<Point> vertices = new Vector<Point>();
	    Vector<Face> faces = new Vector<Face>();
	    
	    try{
	    	InputStream is = Utility.getInputStream(filename);
	      Scanner ins = new Scanner(is);
	      String tmp;
	      
	      ins.useDelimiter("[\\s\\{\\}\\[\\]]+");
	      
	      while(ins.hasNext()){
	        tmp = ins.next();
	        
	        if(tmp.equalsIgnoreCase("Coordinate3")){
	          tmp = ins.next();
	          
	          if(tmp.equalsIgnoreCase("point")){
	            tmp = ins.findWithinHorizon("(?s)\\[.*?\\]", 0);
	            Scanner sc = new Scanner(tmp);
	            sc.useDelimiter("[\\s\\[\\],]+");
	
	            while(sc.hasNext()){
	              Point tmpv = new Point();
	              tmpv.x = sc.nextFloat();
	              tmpv.y = sc.nextFloat();
	              tmpv.z = sc.nextFloat();
	              vertices.add(tmpv);
	            }
	          }
	        }else if(tmp.equalsIgnoreCase("IndexedFaceSet")){
	          tmp = ins.next();
	          
	          if(tmp.equalsIgnoreCase("coordIndex")){
	            tmp = ins.findWithinHorizon("(?s)\\[.*?\\]", 0);
	            Scanner sc = new Scanner(tmp);
	            sc.useDelimiter("[\\s\\[\\],]+");
	
	            while(sc.hasNext()){
	              Face tmpf = new Face();
	              tmpf.v[0] = sc.nextInt();
	              tmpf.v[1] = sc.nextInt();
	              tmpf.v[2] = sc.nextInt();
	              sc.nextInt();
	              faces.add(tmpf);
	            }
	          }
	        }
	      }
	    }catch(Exception e){
	    	return null;
	    }
	    
	    mesh.setVertices(vertices);
	    mesh.setFaces(faces);
	    mesh.initialize();
    }
	    
	  return mesh;
  }
  
  /**
   * Save a VRML 1.0 file.
   *  @param filename the file to save to
   *  @param mesh the mesh to save
   *  @return true if succesfull
   */
  public boolean save(String filename, Mesh mesh)
  {
  	Vector<Point> vertices = mesh.getVertices();
    Vector<Face> faces = mesh.getFaces();
    
    try{
      BufferedWriter outs = new BufferedWriter(new FileWriter(filename));
      
      outs.write("#VRML v1.0 ascii"); outs.newLine();
      outs.newLine();
      outs.write("Separator {"); outs.newLine();
      outs.write("Coordinate3 { point ["); outs.newLine();
      
      for(int i=0; i<vertices.size(); i++){
        outs.write(vertices.get(i).x + " " + vertices.get(i).y + " " + vertices.get(i).z + ",");
        outs.newLine();
      }
      
      outs.write("] }"); outs.newLine();
      outs.newLine();
      outs.write("IndexedFaceSet { coordIndex ["); outs.newLine();
      
      for(int i=0; i<faces.size(); i++){
        outs.write(faces.get(i).v[0] + "," + faces.get(i).v[1] + "," + faces.get(i).v[2] + ",-1,");
        outs.newLine();
      }
      
      outs.write("] }");
      outs.newLine();
      outs.write("}");
      outs.close();
    }catch(Exception e){
    	return false;
    }
    
    return true;
  }
}