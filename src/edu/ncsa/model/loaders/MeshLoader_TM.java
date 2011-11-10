package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.tm files.
 *  @author Daniel Long
 */
public class MeshLoader_TM extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "tm";
	}
	
	/**
	 * Load an tm model.
	 *  @param filename the file to load
	 *  @return the loaded mesh
	 */
	public Mesh load(String filename)
	{
		Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
		Vector<Point> vertices = new Vector<Point>();
		Vector<Face> faces = new Vector<Face>();
   
		//Open file and read in vertices/faces
		Scanner sc;
		String line;
		int lineNumber = 0;
		
		try{
			BufferedReader ins = new BufferedReader(new FileReader(filename));
			
			sc = new Scanner(ins.readLine());
			int vertexCount = sc.nextInt();
			int faceCount = sc.nextInt();
			sc.close();
			
			//Next are vertexCount lines defining the vertices
			while(vertexCount > 0){
				sc = new Scanner(ins.readLine());
				float x = sc.nextFloat();
				float y = sc.nextFloat();
				float z = sc.nextFloat();
				vertices.add(new Point(x, y, z));
				vertexCount--;
				sc.close();
			}
			
			//Finally, we have faceCount lines defining the faces
			//Each face is a triangle, so there are three indexes
			//per line. These indexes are one-based and the last
			//one is negated
			while(faceCount > 0){
				sc = new Scanner(ins.readLine());
				int v1 = sc.nextInt() - 1;
				int v2 = sc.nextInt() - 1;
				int v3 = -sc.nextInt() - 1;
				faces.add(new Face(v1, v2, v3));
				faceCount--;
			}
			
			ins.close();
		}catch(Exception e){
			e.printStackTrace();
			return null;  
		}

		mesh.setVertices(vertices);
		mesh.setFaces(faces);
		mesh.initialize();
    
		return mesh;
	}
  
	public Mesh load(InputStream is) {return null;}
	public boolean save(String filename, Mesh mesh) {return false;}
}