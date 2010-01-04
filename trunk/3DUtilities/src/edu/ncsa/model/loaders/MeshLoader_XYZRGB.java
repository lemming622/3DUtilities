package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.xyzrgb files.
 *  @author Kenton McHenry
 */
public class MeshLoader_XYZRGB extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "xyzrgb";
	}
	
	/**
   * Load vertices from a text file where each line contains an "x y z r g b".
   *  @param filename the file to load
   *  @return the loaded mesh
   */
  public Mesh load(String filename)
  {
  	Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
  	Vector<Point> vertices = new Vector<Point>();
  	Vector<Face> faces = new Vector<Face>();
  	Vector<Color> vertex_colors = new Vector<Color>();
    
    try{
      Scanner ins = new Scanner(new File(filename));
      String tmp;
      int r, g, b;
      
      while(ins.hasNext()){
        Point tmpv = new Point();          
        tmp = ins.next();
        tmpv.x = Double.valueOf(tmp);
        tmp = ins.next();
        tmpv.y = Double.valueOf(tmp);
        tmp = ins.next();
        tmpv.z = Double.valueOf(tmp);
        vertices.add(tmpv);
        
        Color tmpc = new Color();
        tmp = ins.next();
        r = Integer.valueOf(tmp);
        tmp = ins.next();
        g = Integer.valueOf(tmp);
        tmp = ins.next();
        b = Integer.valueOf(tmp);
        tmpc.setRGB(r, g, b);
        vertex_colors.add(tmpc);
      }
      
      ins.close();
    }catch(Exception e){
      e.printStackTrace();
      return null;
    }
    
    mesh.setVertices(vertices);
    mesh.setFaces(faces);
    mesh.setVertexColors(vertex_colors);
    mesh.initialize();
    
    return mesh;
  }
	
  public boolean save(String filename, Mesh mesh) {return false;}
}