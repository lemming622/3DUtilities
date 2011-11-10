package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.q3o files.
 *  @author Daniel Long
 */
public class MeshLoader_Q3O extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "q3o";
	}
	
	/**
	 * Load a Quick3D model.
	 *  @param filename the file to load
	 *  @return the loaded mesh
	 */
	public Mesh load(String filename)
	{
		Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
		
		try{
			//Everything DataInputStream reads in is big endian, but the file
			//format stores things in little endian format, so everything we
			//read in will need to be byteswapped.
			DataInputStream dis = new DataInputStream(new FileInputStream(filename));
			
			//First 22 bytes are the header
			dis.skip(10); //Signature and version
			int meshCount = byteswapInt(dis.readInt());
			dis.skip(4); //Material count
			int textureCount = byteswapInt(dis.readInt());
			Boolean textures;
			if(textureCount == 0){
				textures = false;
			}else{
				textures = true;
			}
			
			//Next come the meshes
			dis.skip(1); //Mesh header ('m')
			
			while(meshCount > 0){
				Vector<Point> vertices = new Vector<Point>();
				Vector<Face> faces = new Vector<Face>();
				int vertexCount = byteswapInt(dis.readInt());
				
				//The vertices are defined next
				while(vertexCount > 0){
					float x = Float.intBitsToFloat(byteswapInt(dis.readInt()));
					float y = Float.intBitsToFloat(byteswapInt(dis.readInt()));
					float z = Float.intBitsToFloat(byteswapInt(dis.readInt()));
					vertices.add(new Point(x, y, z));
					vertexCount--;
				}
				
				int faceCount = byteswapInt(dis.readInt());
				
				//Next we have faceCount shorts defining the number
				//of vertices in each face
				ArrayList<Integer> verticesInFace = new ArrayList<Integer>();
				
				for(int i = 0; i < faceCount; i++){
					verticesInFace.add(byteswapUnsignedShort(dis.readUnsignedShort()));
				}
				
				//Next each face is defined
				for(int i = 0; i < faceCount; i++){
					vertexCount = verticesInFace.get(i);
					ArrayList<Integer> al = new ArrayList<Integer>();
					
					while(vertexCount > 0){
						al.add(byteswapInt(dis.readInt()));
						vertexCount--;
					}
					
					faces.add(new Face(al));
				}
				
				//We've gotten all the info out of the
				//mesh that we need, but still need to
				//navigate through the rest of the data
				//to the end
				dis.skip(4 * faceCount); //Material
				int normalsCount = byteswapInt(dis.readInt());
				dis.skip(normalsCount * 12); //Normals
				textureCount = byteswapInt(dis.readInt());
				
				//If this file has no textures, the following
				//parameters do not appear
				if(textures == true){				
					dis.skip(8 * textureCount); //Textures
					
					//More texture info: one int
					//per vertex per face
					for(int i = 0; i < faceCount; i++){
						dis.skip(4 * verticesInFace.get(i));
					}
				}
					
				dis.skip(12); //Center of mass
				dis.skip(4 * 6);//Bounding box	
				meshCount--;
				mesh.addData(vertices, faces, -1, "");
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
