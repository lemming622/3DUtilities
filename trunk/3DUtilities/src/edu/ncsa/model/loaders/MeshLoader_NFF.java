package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.nff files.
 *  @author Daniel Long
 */
public class MeshLoader_NFF extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "nff";
	}
	
	/**
	 * Load a NFF model.
	 *  @param filename the file to load
	 *  @return the loaded mesh
	 */
	public Mesh load(String filename)
	{
		Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
   
		//Open file and read in vertices/faces
		Scanner sc;
		String line;
		
		try{
			BufferedReader ins = new BufferedReader(new FileReader(filename));
			ins.readLine(); //ID; always "nff"
			ins.readLine(); //Version
			
			//We need to move through the header to find the start of the first object
			//This will be the first line that contains text that does not start with
			//"viewpos" or "viewdir" (optional features) or "//" (comment)
			while(true){
				line = ins.readLine();
				sc = new Scanner(line);
				
				if(sc.hasNext() == true){
					String token = sc.next();
					
					if(("viewpos".equalsIgnoreCase(token) == false) && 
							("viewdir".equalsIgnoreCase(token) == false) &&
							("//".equals(token.substring(0, 2)) == false)){
						break;
					}
				}
				sc.close();
			}
			
			sc.close();
			
			//The rest of the file is filled up with objects
			while(true){
				sc = new Scanner(line);
				String name = sc.next();
				sc.close();
				
				sc = new Scanner(ins.readLine());

				Vector<Point> vertices = new Vector<Point>();
				Vector<Face> faces = new Vector<Face>();
				
				int vertexCount = sc.nextInt();
				
				//We have vertexCount lines next with 3 floats per line
				while(vertexCount > 0){
					sc.close();
					sc = new Scanner(ins.readLine());
					
					if(sc.hasNextFloat() == false){
						continue;
					}else{
						float x = sc.nextFloat();
						float y = sc.nextFloat();
						float z = sc.nextFloat();
						vertices.add(new Point(x, y, z));
						vertexCount--;
					}
				}
				
				sc.close();
				sc = new Scanner(ins.readLine());
				int polygonCount = sc.nextInt();
				
				//We have polygonCount lines defining the faces
				//The first int on each line gives the number of
				//vertices making up that particular face
				//Then the indexes of those vertices are listed
				while(polygonCount > 0){
					sc.close();
					sc = new Scanner(ins.readLine());
					
					if(sc.hasNextInt() == false){
						continue;
					}else{
						vertexCount = sc.nextInt();
						ArrayList<Integer> al = new ArrayList<Integer>();
						
						while(vertexCount > 0){
							al.add(sc.nextInt());
							vertexCount--;
						}
						
						faces.add(new Face(al));
						polygonCount--;
					}
				}
				
				mesh.addData(vertices, faces, -1, name);
				
				//The last thing we need to do is read up to and including
				//the next object's name or until we reach the end of the file
				
				while((line = ins.readLine()) != null){
					sc.close();
					sc = new Scanner(line);
					
					//The next non-blank line that doesn't start with "//" is the start of the next object
					if((sc.hasNext() == true) && (sc.next().substring(0, 2).equals("//") == false)){
						break;
					}
				}
				sc.close();
				if(line == null){ //EOF
					break;
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