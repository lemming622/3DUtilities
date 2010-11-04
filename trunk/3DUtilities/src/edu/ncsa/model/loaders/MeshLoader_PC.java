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
			
			if(true){		//Flip cameras
				for(int i=0; i<cameras.size(); i++){
					cameras.get(i).flipAxes();
				}
			}
			
			Camera.printCameras(cameras);
		}
    
		//Set the mesh
    mesh.setVertices(vertices);
    
    if(true && cameras.size() <= 6){		//Color code cameras
    	if(cameras.size() > 0) mesh.addCamera(cameras.get(0), 1000, new Color(0.5f, 0.5f, 1), new Color(1, 0, 0));
    	if(cameras.size() > 1) mesh.addCamera(cameras.get(1), 1000, new Color(0.5f, 0.5f, 1), new Color(0, 1, 0));
    	if(cameras.size() > 2) mesh.addCamera(cameras.get(2), 1000, new Color(0.5f, 0.5f, 1), new Color(0, 0, 1));
    	if(cameras.size() > 3) mesh.addCamera(cameras.get(3), 1000, new Color(0.5f, 0.5f, 1), new Color(1, 0, 1));
    	if(cameras.size() > 4) mesh.addCamera(cameras.get(4), 1000, new Color(0.5f, 0.5f, 1), new Color(1, 1, 0));
    	if(cameras.size() > 5) mesh.addCamera(cameras.get(5), 1000, new Color(0.5f, 0.5f, 1), new Color(0, 1, 1));
    }else{
    	mesh.addCameras(cameras, 1000, new Color(0.5f, 0.5f, 1), new Color(1, 1, 0));
    }
    
    mesh.initialize();
    
		return mesh;
	}

	public boolean save(String filename, Mesh mesh) {return false;}
}