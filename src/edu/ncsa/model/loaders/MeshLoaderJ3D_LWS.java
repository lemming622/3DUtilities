package edu.ncsa.model.loaders;
import edu.ncsa.model.Mesh;
import edu.ncsa.model.graphics.j3d.*;
import com.sun.j3d.loaders.*;
import com.sun.j3d.loaders.lw3d.*;

/**
 * A mesh file loader for *.lws files.
 *  @author Kenton McHenry
 */
public class MeshLoaderJ3D_LWS extends MeshLoaderJ3D
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "lws";
	}
	
  /**
   * Load a 3D model via an external loader.
   *  @param filename the file to load
   *  @return the loaded mesh
   */
  public Mesh load(String filename)
  {
    try{
      Lw3dLoader loader = new Lw3dLoader();
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