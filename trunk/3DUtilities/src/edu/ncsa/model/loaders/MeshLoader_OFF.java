package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.off files.
 *  @author Daniel Long
 */
public class MeshLoader_OFF extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "off";
	}
	
	/**
	 * Load a Geomview model.
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
		
		try{
			BufferedReader ins = new BufferedReader(new FileReader(filename));
			ins.readLine(); // This line should always be "OFF"
			sc = new Scanner(ins.readLine());
			// The next line of importance contains three ints
			// Any other line should either be blank or be a comment
			// (starts with '#')
			while(sc.hasNextInt() == false){
				sc.close();
				sc = new Scanner(ins.readLine());
			}
			int vertexCount = sc.nextInt();
			int faceCount = sc.nextInt();
			// The last parameter is the number of edges and is unneeded
			
			// Next we have vertexCount lines giving the vertex coordinates
			while(vertexCount > 0){
				sc.close();
				sc = new Scanner(ins.readLine());
				if(sc.hasNextFloat() == true){
					float x = sc.nextFloat();
					float y = sc.nextFloat();
					float z = sc.nextFloat();
					vertices.add(new Point(x, y, z));
					vertexCount--;
				}
			}
			
			// Finally, we have faceCount lines giving the indexes of the vertices which make up each face
			while(faceCount > 0){
				sc.close();
				sc = new Scanner(ins.readLine());
				if(sc.hasNextInt() == true){
					int v = sc.nextInt(); // This parameter is the number of vertices that make up this face
					ArrayList<Integer> al = new ArrayList<Integer>(); // The vertices will be stored here
					while(v > 0){
						al.add(sc.nextInt());
						v--;
					}
					faces.add(new Face(al));
					faceCount--;
					// Any remaining information on this line appears to be unimportant (for instance, colors)
				}
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
