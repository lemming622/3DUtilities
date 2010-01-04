package edu.ncsa.model.loaders;
import edu.ncsa.model.Mesh;
import edu.ncsa.model.loaders.j3d.*;
import com.sun.j3d.loaders.*;
import org.web3d.j3d.loaders.*;

/**
 * A mesh file loader for *.x3d files.
 *  @author Kenton McHenry
 */
public class MeshLoaderJ3D_X3D extends MeshLoaderJ3D
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "x3d";
	}
	
  /**
   * Load a 3D model via an external loader.
   *  @param filename the file to load
   *  @return the loaded mesh
   */
  public Mesh load(String filename)
  {
    try{
      Loader loader = new X3DLoader();
      Scene scene = loader.load(filename);
      
      Mesh mesh = loadScene(scene);
      mesh.addFileMetaData(filename);
      mesh.initialize();
    
      return mesh;
    }catch(Exception e){
      e.printStackTrace();
      return null;
    }
  }
	
	public boolean save(String filename, Mesh mesh) {return false;}
}