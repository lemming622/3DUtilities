package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.sdml files.
 *  @author Daniel Long
 */
public class MeshLoader_SDML extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "sdml";
	}
	
	/**
	 * Load an SDML model.
	 *  @param filename the file to load
	 *  @return the loaded mesh
	 */
	public Mesh load(String filename)
	{
		Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
		Vector<Point> vertices = new Vector<Point>();
		Vector<Face> faces = new Vector<Face>();
   
		//Open file and read in vertices/faces
		Scanner sc = null;
		String line;
		int totalVertices = 0;
		
		try{
			BufferedReader ins = new BufferedReader(new FileReader(filename));
			
			while((line = ins.readLine()) != null){
				//Look for a line starting with "name"
				sc = new Scanner(line);
				if((sc.hasNext() == true) && ("name".equalsIgnoreCase(sc.next()))){
					break;
				}
				sc.close();
			}
			
			while(line != null){
				String name = sc.next();
				sc.close();
					
				//The next line of interest starts with "vl"
				while(true){
					line = ins.readLine();
					sc = new Scanner(line);
					if("vl".equalsIgnoreCase(sc.next())){
						break;
					}
					sc.close();
				}
					
				//The next token is one more than
				//the number of vertices
				int vertexCount = sc.nextInt() - 1;
				sc.close();
					
				//Now we have vertexCount lines defining the vertices
				while(vertexCount > 0){
					sc = new Scanner(ins.readLine());
					if(sc.hasNextFloat()){ //If false, line is blank or a comment
						float x = sc.nextFloat();
						float y = sc.nextFloat();
						float z = sc.nextFloat();
						vertices.add(new Point(x, y, z));
					}
					sc.close();
					vertexCount--;
				}
					
				//The remaining lines of interest start with "vi"		
				while(true){
					line = ins.readLine();
					if(line == null){ //EOF
						mesh.addData(vertices, faces, -1, name);
						break;
					}
					sc = new Scanner(line);
					if(sc.hasNext() == false){
						sc.close();
						continue;
					}
					
					String token = sc.next();
					if("name".equalsIgnoreCase(token)){ //Beginning of new layer
						mesh.addData(vertices, faces, -1, name);
						vertices.clear();
						faces.clear();
						totalVertices += vertexCount;
						break;
					}else if("vi".equalsIgnoreCase(token)){
						//Remainder of line consists of vertex indexes
						ArrayList<Integer> al = new ArrayList<Integer>();
						while(sc.hasNextInt()){
							al.add(sc.nextInt() - totalVertices);
						}
						faces.add(new Face(al));
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			return null;  
		}

		mesh.initialize();   
		return mesh;
	}
  
	public Mesh load(InputStream is) {return null;}
	public boolean save(String filename, Mesh mesh) {return false;}
}
