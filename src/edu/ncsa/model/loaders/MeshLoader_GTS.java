package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import kgm.utility.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.gts files.
 *  @author Daniel Long
 */
public class MeshLoader_GTS extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "gts";
	}
	
	/**
	 * Load a GTS model.
	 *  @param filename the file to load
	 *  @return the loaded mesh
	 */
	public Mesh load(String filename)
	{
		Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
		Vector<Point> vertices = new Vector<Point>();
		Vector<Face> faces = new Vector<Face>();
		Vector<Edge> edges = new Vector<Edge>();
   
		//Open file and read in vertices/faces
		Scanner sc;
		
		try{
			//BufferedReader ins = new BufferedReader(new FileReader(filename));
			BufferedReader ins = new BufferedReader(new InputStreamReader(Utility.getInputStream(filename)));
			
			sc = new Scanner(ins.readLine());
			// The next line of importance contains three ints
			// Any other line should either be blank or be a comment
			// (starts with '#')
			while(sc.hasNextInt() == false){
				sc.close();
				sc = new Scanner(ins.readLine());
			}
			int vertexCount = sc.nextInt();
			int edgeCount = sc.nextInt();
			int faceCount = sc.nextInt();
			
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
			
			// Now we have edgeCount lines giving the indexes (1-based)
			// of the two vertices that make up each edge
			while(edgeCount > 0){
				sc.close(); 
				sc = new Scanner(ins.readLine());
				if(sc.hasNextInt() == true){
					int v1 = sc.nextInt();
					int v2 = sc.nextInt();
					edges.add(new Edge(v1 - 1, v2 - 1));
					edgeCount--;
				}
			}
			
			// Finally, we have faceCount lines giving the indexes of the edges 
			// which make up each face. These indexes are 1-based, not 0-based
			// Every face is made up of three edges
			while(faceCount > 0){
				sc.close();
				sc = new Scanner(ins.readLine());
				if(sc.hasNextInt() == true){
					int e1 = sc.nextInt();
					int e2 = sc.nextInt();
					int e3 = sc.nextInt();
					ArrayList<Integer> al = new ArrayList<Integer>();
					al.add(edges.get(e1 - 1).v0);
					al.add(edges.get(e1 - 1).v1);
					al.add(edges.get(e2 - 1).v0);
					al.add(edges.get(e2 - 1).v1);
					al.add(edges.get(e3 - 1).v0);
					al.add(edges.get(e3 - 1).v1);
					// We have 6 vertices, but they are really 3 vertices each repeated once
					// To extract them, sort the array list. Then al[0] = al[1], al[2] = al[3],
					// and al[4] = al[5] and we can get the three distinct vertices
					// easily
					Collections.sort(al);
					faces.add(new Face(al.get(0), al.get(2), al.get(4)));
					faceCount--;
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
