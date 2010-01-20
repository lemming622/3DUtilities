package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.model.loaders.teeve.*;
import edu.ncsa.utility.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
 * An animated mesh loader for *.teeve files.
 * @author Kenton McHenry
 */
public class AnimatedMeshLoader_TEEVE extends AnimatedMeshLoader
{ 
	private String filename = null;
	private ByteBuffer file_buffer;
	private int initial_capacity = 100000;
	
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "teeve";
	}
	
	 /**
   * Load a *.teeve animated model.
   *  @param filename the file to load
   *  @return the loaded animated mesh
   */
	public AnimatedMesh load(String filename)
	{
		AnimatedMeshLoader_TEEVE animation_loader = new AnimatedMeshLoader_TEEVE();	//Create an instance of this loader to become apart of the returned animated mesh.
		AnimatedMesh mesh = new AnimatedMesh(animation_loader); mesh.addFileMetaData(filename);
		
		//Initialize the file loader for the animated mesh
		animation_loader.filename = filename;
		
  	try{
  		FileChannel ins = new FileInputStream(animation_loader.filename).getChannel();
  		animation_loader.file_buffer = ByteBuffer.allocate((int)ins.size());
  		animation_loader.file_buffer.rewind();
  		ins.read(animation_loader.file_buffer);
  		animation_loader.file_buffer.rewind();
  	}catch(Exception e) {e.printStackTrace();}
  	
		return mesh;
	}
	
	/**
	 * Get the length of the animation.
	 */
	public double length()
	{
		return -1;
	}
	
	/**
	 * Retrieve the next mesh.
	 * @return the next mesh
	 */
	public Mesh getMesh()
	{		
  	Mesh mesh = new Mesh();
  	Vector<Point> vertices = new Vector<Point>(initial_capacity);
  	Vector<Color> colors = new Vector<Color>(initial_capacity);
  	byte[] buffer;
  	int buffer_length;
  	
		buffer_length = Utility.bytesToInt(file_buffer.get(), file_buffer.get(), file_buffer.get(), file_buffer.get(), false);
		buffer = new byte[buffer_length];
		file_buffer.get(buffer);
  	Packetizer.depacketize(vertices, colors, buffer, buffer.length);
  	
    //Transfer loaded data to a mesh and initialize auxiliary data structures
    mesh.setVertices(vertices);
    mesh.setVertexColors(colors);
    mesh.initialize();
  	
  	return mesh;
	}
	
	public Mesh getMesh(double t) {return null;}
	public boolean save(String filename, AnimatedMesh mesh) {return false;}
}