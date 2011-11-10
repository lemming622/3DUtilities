package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.loaders._3ds.Loader3ds;
import edu.ncsa.model.loaders.vtk.VTKLoader;
import java.io.*;

/**
 * A mesh file loader for *.vtk files.
 *  @author Daniel Long
 */
public class MeshLoader_VTK extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "vtk";
	}
	
	/**
	 * Load a VTK model.
	 *  @param filename the file to load
	 *  @return the loaded mesh
	 */
	public Mesh load(String filename)
	{	
		try{
			VTKLoader vtkloader = new VTKLoader();
			java.io.FileReader reader = new java.io.FileReader(filename);
            Mesh mesh = vtkloader.load(reader, 1);
            if(mesh != null){
	    		mesh.addFileMetaData(filename);
	    		mesh.initialize();
            }
    		return mesh;
		}catch(Exception e){
			e.printStackTrace();
			return null;  
		}
	}
  
	public Mesh load(InputStream is) {return null;}
	public boolean save(String filename, Mesh mesh) {return false;}
}
