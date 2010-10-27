package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.utility.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for point/camera files.
 * @author Kenton McHenry
 */
public class MeshLoader_PC extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 * @return the type loaded
	 */
	public String type()
	{
		return "pc";
	}

	/**
   * Load points and cameras.
   * @param filename the file to load
   * @return the loaded mesh
   */
	public Mesh load(String filename)
	{
  	Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
  	Vector<Point> vertices = new Vector<Point>();
		Vector<Camera> cameras = new Vector<Camera>();
		
		if(Utility.exists(filename + "/points.txt")){
			try{
	      Scanner ins = new Scanner(new File(filename + "/points.txt"));
	      String tmp;
	      
	      while(ins.hasNext()){
	        Point tmpv = new Point();          
	        tmp = ins.next();
	        tmpv.x = Double.valueOf(tmp);
	        tmp = ins.next();
	        tmpv.y = Double.valueOf(tmp);
	        tmp = ins.next();
	        tmpv.z = Double.valueOf(tmp);
	        vertices.add(tmpv);
	      }
	      
	      ins.close();
	    }catch(Exception e){
	      e.printStackTrace();
	      return null;
	    }
		}
		
		if(Utility.exists(filename + "/cameras.txt")){
			cameras = Camera.loadCameras(filename + "/cameras.txt");
		}
    
    mesh.setVertices(vertices);
    mesh.addCameras(cameras, 1000, new Color(0.5f, 0.5f, 1), new Color(1, 1, 0));
    mesh.initialize();
    
		return mesh;
	}

	public boolean save(String filename, Mesh mesh) {return false;}
}