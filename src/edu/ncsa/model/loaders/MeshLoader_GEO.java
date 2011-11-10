package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.geo files.
 *  @author Daniel Long
 */
public class MeshLoader_GEO extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "geo";
	}
	
	/**
	 * Load a Videoscape 3D model
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
			line = ins.readLine();
			// The first line identifies the exact format the file takes
			if("3DG1".equalsIgnoreCase(line)){
				sc = new Scanner(ins.readLine());
				int vertexCount = sc.nextInt();
				// The next vertexCount lines give the vertices
				while(vertexCount > 0){
					sc.close();
					sc = new Scanner(ins.readLine());
					float x = sc.nextFloat();
					float y = sc.nextFloat();
					float z = sc.nextFloat();
					vertices.add(new Point(x, y, z));
					vertexCount--;
				}
				// The remaining lines give the indexes of the vertices which
				// make up each face, as well as the color of that face
				sc.close();
				line = ins.readLine();
				while(line != null){
					sc = new Scanner(line);
					if(sc.hasNextInt() == true){
						// The first parameter gives the number of vertices in this face
						vertexCount = sc.nextInt();
						ArrayList<Integer> al = new ArrayList<Integer>();
						while(vertexCount > 0){
							al.add(sc.nextInt());
							vertexCount--;
						}
						// There is a remaining int that gives the color
						// of the face (in BGR format, not RGB), but I'm
						// not sure how to implement this
						faces.add(new Face(al));
					}
					sc.close();
					line = ins.readLine();
				}
			}else if("GOUR".equalsIgnoreCase(line)){
				sc = new Scanner(ins.readLine());
				int vertexCount = sc.nextInt();
				// The next vertexCount lines give the vertices followed by the color
				while(vertexCount > 0){
					sc.close();
					sc = new Scanner(ins.readLine());
					float x = sc.nextFloat();
					float y = sc.nextFloat();
					float z = sc.nextFloat();
					vertices.add(new Point(x, y, z));
					vertexCount--;
				}
				// The remaining lines give the indexes of the vertices which
				// make up each face
				sc.close();
				line = ins.readLine();
				while(line != null){
					sc = new Scanner(line);
					if(sc.hasNextInt() == true){
						// The first parameter gives the number of vertices in this face
						vertexCount = sc.nextInt();
						ArrayList<Integer> al = new ArrayList<Integer>();
						while(vertexCount > 0){
							al.add(sc.nextInt());
							vertexCount--;
						}
						faces.add(new Face(al));
					}
					sc.close();
					line = ins.readLine();
				}
			}else if("3DG2".equalsIgnoreCase(line)){
				// This is a lamp file
				// It appears to deal with lighting only - no models at all
				return null;
			}else if("3DG3".equalsIgnoreCase(line)){
				// This case involves bezier curves and NURBs
				// I'm not sure how to implement these at this time
				return null;
			}else{
				// Unrecognized format
				return null;
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
