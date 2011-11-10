package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.ms3d files.
 *  @author Daniel Long
 */
public class MeshLoader_MS3D extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "ms3d";
	}
	
	/**
	 * Load a Milkshape 3D model.
	 *  @param filename the file to load
	 *  @return the loaded mesh
	 */
	public Mesh load(String filename)
	{
		Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
		Vector<Point> vertices = new Vector<Point>();
		Vector<Face> faces = new Vector<Face>();
		Vector<Face> triangles = new Vector<Face>();
		
		//At this time, I am assuming the file is properly formatted. Error handling can be added later
		try{
			//Everything DataInputStream reads in is big endian, but the file
			//format stores things in little endian format, so everything we
			//read in will need to be byteswapped.
			DataInputStream dis = new DataInputStream(new FileInputStream(filename));
			dis.skip(14); //First 14 bytes are the header
			
			//The next 2 bytes give the number of vertices
			int vertexCount = byteswapUnsignedShort(dis.readUnsignedShort());
			
			//The vertices are defined next
			while(vertexCount > 0){
				dis.skip(1); //First byte is a flag
				
				//Next we have the vertices (floats)
				float x = Float.intBitsToFloat(byteswapInt(dis.readInt()));
				float y = Float.intBitsToFloat(byteswapInt(dis.readInt()));
				float z = Float.intBitsToFloat(byteswapInt(dis.readInt()));
				
				dis.skip(2); //2 more unnecessary bytes
				vertexCount--;
				vertices.add(new Point(x, y, z));
			}

			//The next 2 bytes give the number of triangles
			int triangleCount = byteswapUnsignedShort(dis.readUnsignedShort());
			
			//The triangles are defined next
			while(triangleCount > 0){
				dis.skip(2);
				int v1 = byteswapUnsignedShort(dis.readUnsignedShort());
				int v2 = byteswapUnsignedShort(dis.readUnsignedShort());
				int v3 = byteswapUnsignedShort(dis.readUnsignedShort());
				dis.skip(4 * 3 * 3 + 4 * 3 + 4 * 3 + 1 + 1); //Skip the remaining parameters
				triangleCount--;
				triangles.add(new Face(v1, v2, v3));
			}
			
			//Finally, we have groups defined which are made up
			//of the above triangles
			int groupCount = byteswapUnsignedShort(dis.readUnsignedShort());
			
			while(groupCount > 0){
				dis.skip(1); //First byte contains flags
				String groupName = "";
				
				//The next 32 bytes are the group name
				byte[] charBuffer = new byte[32];
				dis.read(charBuffer);
				for(int i = 0; (i < 32) && (charBuffer[i] != 0); i++){
					groupName += (char) charBuffer[i];
				}
				
				//The next 2 bytes are the number
				//of triangles in this group
				triangleCount = byteswapUnsignedShort(dis.readUnsignedShort());
				
				//Next the indexes of the triangles
				//in this group are given
				while(triangleCount > 0){
					faces.add(triangles.get(byteswapUnsignedShort(dis.readUnsignedShort())));
					triangleCount--;
				}
				
				dis.skip(1); // Index of material
				mesh.addData(vertices, faces, -1, groupName);
				faces.clear();				
				groupCount--;
			}
			
			dis.close();
		}catch(Exception e){
			e.printStackTrace();
			return null;  
		}

		mesh.initialize();
		return mesh;
	}
  
	public Mesh load(InputStream is) {return null;}
	public boolean save(String filename, Mesh mesh) {return false;}
	
	/**
	 * Byteswap an unsigned short
	 * @param n the unsigned short to byteswap
	 * @return the unsigned short byteswapped
	 */
	private int byteswapUnsignedShort(int n)
	{
		int result = (n & 0xFF) << 8;
		result |= (n & 0xFF00) >> 8;
		return result;
	}
	
	/**
	 * Byteswap an int
	 *  @param n the int to byteswap
	 *  @return the byteswapped int
	 */
	private int byteswapInt(int n)
	{
		int result = (n & 0xFF) << 24;
		result |= ((n >> 8) & 0xFF) << 16;
		result |= ((n >> 16) & 0xFF) << 8;
		result |= (n >> 24) & 0xFF;
		return result;
	}
}
