package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.stl files.
 *  @author Daniel Long
 */
public class MeshLoader_STL extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "stl";
	}
	
	/**
	 * Load a stereolithograph model.
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
		String token;
		int faceCount = 0;
		
		// At this time, I am assuming the file is properly formatted. Error handling can be added later
		try{
			BufferedReader ins = new BufferedReader(new FileReader(filename));
			sc = new Scanner(ins.readLine());
			token = sc.next();
			if("solid".equalsIgnoreCase(token)){ // ASCII Format
				while(true){ // This loop will be broken with break statements
					sc.close();
					sc = new Scanner(ins.readLine()); // If the file is properly formatted, we don't have to worry about readLine() returning null
					token = sc.next();
					if(("endsolid".equalsIgnoreCase(token)) || ("end".equalsIgnoreCase(token))){ // We've reached the end of the file
						break;
					}else{ // Implicitly: else if("facet".equals(token)) . This is the only other valid possibility
						// The rest of the line is "normal" followed by three coordinates
						// We don't need any of this
						ins.readLine(); // The next line MUST be "outer loop"
						// The next three lines are vertex statements
						for(int i = 0; i < 3; i++){
							sc.close();
							sc = new Scanner(ins.readLine());
							sc.next(); // This token is always "vertex"
							// The three remaining tokens are the vertex coordinates
							float vx = sc.nextFloat();
							float vy = sc.nextFloat();
							float vz = sc.nextFloat();
							vertices.add(new Point(vx, vy, vz));
						}
						ins.readLine(); // This line MUST be "endloop"
						ins.readLine(); // This line MUST be "endfacet"
						faces.add(new Face(3 * faceCount, 3 * faceCount + 1, 3 * faceCount + 2));
						faceCount++;
					}
				}
				ins.close();
			}else{ // Binary format
				ins.close(); // Reopen the file with FileInputStream
				FileInputStream fis = new FileInputStream(filename);
				fis.skip(80); // The first 80 bytes are the header, which we don't need
				
				byte[] buffer = new byte[4];
				
				// The next 4 bytes represent an unsigned int which is the
				// number of facets in the file
				fis.read(buffer);
				int facets = bytesToInt(buffer);
				// The standard doesn't specify big-endian vs. little-endian. Assuming big-endian.
				
				for(int i = 0; i < facets; i++){
					fis.skip(12); // 12 bytes for the normals. We don't need these
					
					// Next we have 3 floats representing a vertex (x, y, z)
					// This is repeated two more times
					
					for(int j = 0; j < 3; j++){
						fis.read(buffer);
						float x = Float.intBitsToFloat(bytesToInt(buffer));
						fis.read(buffer);
						float y = Float.intBitsToFloat(bytesToInt(buffer));
						fis.read(buffer);
						float z = Float.intBitsToFloat(bytesToInt(buffer));
						vertices.add(new Point(x, y, z));
					}
					
					faces.add(new Face(3 * i, 3 * i + 1, 3 * i + 2));
					
					fis.skip(2);
					// These last two bytes are the attribute byte count
					// We don't need this information
				}
				
				fis.close();
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
	 * Convert an array of 4 bytes to an int
	 * @param array the array to convert
	 * @return the array converted to an int
	 */
	private static int bytesToInt(byte[] array)
	{
		// array is assumed to consist of 4 bytes
		return 256 * 256 * 256 * array[0] + 256 * 256 * array[1] + 256 * array[2] + array[3];
	}
}
