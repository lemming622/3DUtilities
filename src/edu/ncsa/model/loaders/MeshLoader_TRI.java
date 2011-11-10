package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.tri files.
 *  @author Daniel Long
 */
public class MeshLoader_TRI extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "tri";
	}
	
	/**
	 * Load a .tri file
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
		
		try{
			BufferedReader ins = new BufferedReader(new FileReader(filename));
			sc = new Scanner(ins.readLine());
			// The first line contains two integers; the first is the number
			// of vertices and the second is the number of faces (all of 
			// which are triangles)
			int vertexCount = sc.nextInt();
			int triangleCount = sc.nextInt();
			// The next vertexCount line give the
			// coordinates of the vertices
			while(vertexCount > 0){
				sc.close();
				sc = new Scanner(ins.readLine());
				// The first three floats are the coordinates of this vertex
				float x = sc.nextFloat();
				float y = sc.nextFloat();
				float z = sc.nextFloat();
				vertices.add(new Point(x, y, z));
				vertexCount--;
			}
			// The next triangleCount lines give the indexes
			// of the three vertices which make up each face
			while(triangleCount > 0){
				sc.close();
				sc = new Scanner(ins.readLine());
				int v1 = sc.nextInt();
				int v2 = sc.nextInt();
				int v3 = sc.nextInt();
				faces.add(new Face(v1, v2, v3));
				triangleCount--;
			}
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
