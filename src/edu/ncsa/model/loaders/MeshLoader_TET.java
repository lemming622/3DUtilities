package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.tet files.
 *  @author Daniel Long
 */
public class MeshLoader_TET extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "tet";
	}
	
	/**
	 * Load an tet model.
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
			
			while((line = ins.readLine()) != null){
				
				//Every line is of the same form:
				//T:x0 y0 z0:x1 y1 z1:x2 y2 z2:x3 y3 z3:Tn
				String[] parts = line.split(":");
				
				//Vertices 0, 1, 2, and 3 are at indexes 1,
				//2, 3, and 4
				for(int i = 1; i <= 4; i++){
					sc = new Scanner(parts[i]);
					float x = sc.nextFloat();
					float y = sc.nextFloat();
					float z = sc.nextFloat();
					vertices.add(new Point(x, y, z));
				}
				
				//A tetrahedron has 4 faces, 1 for
				//each possible combination of 3 of
				//the 4 vertices
				faces.add(new Face(4 * lineNumber, 4 * lineNumber + 1, 4 * lineNumber + 2)); //(0, 1, 2)
				faces.add(new Face(4 * lineNumber, 4 * lineNumber + 1, 4 * lineNumber + 3)); //(0, 1, 3)
				faces.add(new Face(4 * lineNumber, 4 * lineNumber + 2, 4 * lineNumber + 3)); //(0, 2, 3)
				faces.add(new Face(4 * lineNumber + 1, 4 * lineNumber + 2, 4 * lineNumber + 3)); //(1, 2, 3)
				
				lineNumber++;
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