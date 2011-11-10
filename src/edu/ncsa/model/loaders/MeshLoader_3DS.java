package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.loaders._3ds.Loader3ds;
import java.io.*;

/**
 * A mesh file loader for *.3ds files.
 *  @author Daniel Long
 */
public class MeshLoader_3DS extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "3ds";
	}
	
	/**
	 * Load an 3DS model.
	 *  @param filename the file to load
	 *  @return the loaded mesh
	 */
	public Mesh load(String filename)
	{
		Mesh mesh;
		
		try{
	        Loader3ds loader3ds = new Loader3ds(filename);
	        mesh = loader3ds.Load3ds();
		}catch(Exception e){
			e.printStackTrace();
			return null;  
		}

		mesh.addFileMetaData(filename);
		mesh.initialize();
    
		return mesh;
	}
  
	public Mesh load(InputStream is) {return null;}
	public boolean save(String filename, Mesh mesh) {return false;}
}
