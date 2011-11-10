package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.plg files.
 *  @author Daniel Long
 */
public class MeshLoader_PLG extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "plg";
	}
	
	/**
	 * Load a REND386 model.
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
			sc.next(); //First token is the object's name
			while(sc.hasNextInt() == false){
				//Skip comment and blank lines
				sc.close();
				sc = new Scanner(ins.readLine());
				if(sc.hasNext() == false){
					continue;
				}
				sc.next();
			}
			int vertexCount = sc.nextInt();
			int facetCount = sc.nextInt();
			
			//Next are vertexCount lines giving the coordinates of the vertices
			while(vertexCount > 0){
				sc.close();
				sc = new Scanner(ins.readLine());
				if(sc.hasNextFloat() == false){
					continue;
				}
				float x = sc.nextFloat();
				float y = sc.nextFloat();
				float z = sc.nextFloat();
				vertices.add(new Point(x, y, z));
				vertexCount--;
			}
			
			//Next are facetCount lines describing the facets
			while(facetCount > 0){
				sc.close();
				sc = new Scanner(ins.readLine());
				if(sc.hasNext() == false){
					continue;
				}
				sc.next(); //The first token can be ignored
				if(sc.hasNextInt() == false){
					continue;
				}
				vertexCount = sc.nextInt(); //The next token is the number of vertices in this face
				
				//The remaining tokens on this line give the indexes of the vertices
				//in this face
				ArrayList<Integer> al = new ArrayList<Integer>();
				while(vertexCount > 0){
					al.add(sc.nextInt());
					vertexCount--;
				}
				
				faces.add(new Face(al));
				facetCount--;
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