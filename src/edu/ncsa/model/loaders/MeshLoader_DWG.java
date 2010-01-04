package edu.ncsa.model.loaders;
import edu.ncsa.model.loaders.dwg.*;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.util.Vector;

/**
 * A mesh file loader for *.dwg files.
 *  @author Kenton McHenry
 */
public class MeshLoader_DWG extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "dwg";
	}
	
	 /**
   * Load an AutoCAD *.dwg file.
   *  @param filename the file to load
   *  @return the loaded mesh
   */
  public Mesh load(String filename)
  {
  	Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
  	Vector<Point> vertices = new Vector<Point>();
  	Vector<Face> faces = new Vector<Face>();
  	
  	BinaryFile bf = new BinaryFile(filename);
  	
  	System.out.println("Version ID: " + bf.getString(0, 5));
  	
    mesh.setVertices(vertices);
    mesh.setFaces(faces);
    mesh.initialize();
    
    return mesh;
  }
  
  public boolean save(String filename, Mesh mesh) {return false;}
}