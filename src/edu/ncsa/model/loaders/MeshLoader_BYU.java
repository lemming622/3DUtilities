package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.byu files.
 *  @author Daniel Long
 */
public class MeshLoader_BYU extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "byu";
	}
	
	/**
	 * Load a BYU model.
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
			sc = new Scanner(ins.readLine());
			sc.nextInt(); //Number of "parts"
			int vertexCount = sc.nextInt();
			int polygonCount = sc.nextInt();
			//The next line contains two integers per "part"; this doesn't appear to be relevant.
			
			ins.readLine();
			//Next we have three floats per vertex (x, y, z). There are either
			//one or two triplets per line
			sc.close();		
			sc = new Scanner(ins.readLine());
			while(vertexCount > 0){
				if(sc.hasNextFloat() == false){
					sc.close();
					sc = new Scanner(ins.readLine());
				}
				float x = sc.nextFloat();
				float y = sc.nextFloat();
				float z = sc.nextFloat();
				vertices.add(new Point(x, y, z));
				vertexCount--;
			}
			
			// The remainder of the file gives the indexes of the vertices which make up each face
			// These indexes are 1-based and the last one in a face is negated
			//sc = new Scanner(ins.readLine());
			ArrayList<Integer> al = new ArrayList<Integer>();
			sc.close();
			sc = new Scanner(ins.readLine());
			while(polygonCount > 0){
				if(sc.hasNextInt() == false){
					sc.close();
					sc = new Scanner(ins.readLine());
				}
				int index = sc.nextInt();
				if(index < 0){
					//Last vertex
					al.add(-index - 1);
					faces.add(new Face(al));
					al.clear();
					polygonCount--;
				}else{
					al.add(index - 1);
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