package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.phd files.
 *  @author Daniel Long
 */
public class MeshLoader_PHD extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "phd";
	}
	
	/**
	 * Load a PolyHedral Database model.
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
			
			//The file is divided into several field. Each field has a header
			//consisting of ':' followed by a string identifying which field
			//it is. We are interested in the vertices (":vertices"), and the
			//faces (":solid").
			
			while((line = ins.readLine()) != null){
				if(":vertices".equalsIgnoreCase(line)){
					
					//The first line gives the number of vertices
					sc = new Scanner(ins.readLine());
					int vertexCount = sc.nextInt();
					sc.close();
					
					//Next we have vertexCount lines giving the
					//coordinates of each vertex. Each coordinate
					//is a float which is optionally followed
					//(without spaces) by an expression in square
					//brackets which represents the exact value of
					//the previous float. We don't need this
					//expression.
					
					while(vertexCount > 0){
						sc = new Scanner(ins.readLine());
						float x = extractCoordinate(sc.next());
						float y = extractCoordinate(sc.next());
						float z = extractCoordinate(sc.next());
						vertices.add(new Point(x, y, z));
						vertexCount--;
					}
					
				}else if(":solid".equalsIgnoreCase(line)){
					sc = new Scanner(ins.readLine());
					int faceCount = sc.nextInt();
					sc.close();
					
					//Next we have faceCount lines giving the number
					//of vertices in each face and the indexes of those
					//vertices
					while(faceCount > 0){
						sc = new Scanner(ins.readLine());
						int vertexCount = sc.nextInt();
						ArrayList<Integer> al = new ArrayList<Integer>();
						
						while(vertexCount > 0){
							al.add(sc.nextInt());
							vertexCount--;
						}
						
						faces.add(new Face(al));
						faceCount--;
						sc.close();
					}
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
	
	/**
	 * Reads in the approximate coordinate of a vertex
	 * @param str The string to extract the coordinate from
	 * @return The approximate value of the coordinate
	 */
	private float extractCoordinate(String str)
	{
		//Find the index within str of the
		//expression (exact value) of the
		//coordinate. The approximate value
		//will be the portion of the string
		//prior to this index
		
		int index = str.indexOf('[');
		if(index != -1){
			//Chop off the unneeded part of the string
			str = str.substring(0, index);
		}
		return Float.parseFloat(str); //Convert the string to float
	}
}