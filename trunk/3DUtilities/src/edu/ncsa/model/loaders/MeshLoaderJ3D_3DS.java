package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.graphics.j3d.*;
import com.sun.j3d.loaders.*;
import ncsa.j3d.loaders.ModelLoader;
//import com.microcrowd.loader.java3d.max3ds.*; //Microcrowd 3Ds loader

/**
 * A mesh file loader for *.3ds files.
 *  @author Kenton McHenry
 */
public class MeshLoaderJ3D_3DS extends MeshLoaderJ3D
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
   * Load a 3D model via an external loader.
   *  @param filename the file to load
   *  @return the loaded mesh
   */
  public Mesh load(String filename)
  {
    try{
      ModelLoader loader = new ModelLoader();
      //Loader3DS loader = new Loader3DS();        
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