package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
 * A mesh file loader for *.obj files stored in a binary format.
 * Note: This is a simple version that only supports geometry.
 *  @author Kenton McHenry
 */
public class MeshLoader_OBJ_BIN1 extends MeshLoader
{
	private int initial_capacity = 100000;

	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "obj_bin1";
	}
	
	 /**
   * Load a *.obj_bin1 model.
   *  @param filename the file to load
   *  @return the loaded mesh
   */
  public Mesh load(String filename)
  {
  	Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
  	Vector<Point> vertices = new Vector<Point>(initial_capacity);
  	Vector<Face> faces = new Vector<Face>(initial_capacity);
  	boolean USE_SHORTS = false;
  	
  	ByteBuffer buffer = null;
  	Point tmpv;
  	Face tmpf;
  	int nv, nf, nr;
  	
  	//Load data from specified channel into a ByteBuffer
  	try{
  		FileChannel ins = new FileInputStream(filename).getChannel();
  		buffer = ByteBuffer.allocate((int)ins.size());
  		buffer.rewind();
  		ins.read(buffer);
  		buffer.rewind();
  	}catch(Exception e){
  		e.printStackTrace();
  		return null;
  	}
  	
  	//Read data sizes from ByteBuffer
  	nv = buffer.getInt();
    nf = buffer.getInt();
    if(nv <= 32768) USE_SHORTS = true;
    
    vertices.ensureCapacity(nv);
    faces.ensureCapacity(nf);
    
    //Read in vertices
    for(int i=0; i<nv; i++){
    	tmpv = new Point(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
    	vertices.add(tmpv);
    }
    
    for(int i=0; i<nf; i++){
    	nr = buffer.getShort();
    	tmpf = new Face(nr);
    	
    	if(USE_SHORTS){
    		for(int j=0; j<nr; j++){
	    		tmpf.v[j] = buffer.getShort();
	    	}
    	}else{
	    	for(int j=0; j<nr; j++){
	    		tmpf.v[j] = buffer.getInt();
	    	}
    	}
    	
    	faces.add(tmpf);
    }
    
    //Transfer loaded data to a mesh and initialize auxillary data structures
    mesh.setVertices(vertices);
    mesh.setFaces(faces);
    mesh.initialize();
    
    return mesh;
  }
  
  /**
   * Save a *.obj_bin1 file.
   *  @param filename the file to save to
   *  @param mesh the mesh to save
   *  @return true if saved succesfully
   */
  public boolean save(String filename, Mesh mesh)
  {
  	Vector<Point> vertices = mesh.getVertices();
  	Vector<Point> used_vertices = new Vector<Point>();
    Vector<Face> faces = mesh.getFaces();
    TreeMap<String,Vector<Integer>> groups = mesh.getGroups();
    Set<String> group_keys = groups.keySet();
    Iterator<String> itr_str;
    Vector<Integer> values;
    String key;
    boolean[] valid_vertex = new boolean[vertices.size()];
    int[] vertex_map = new int[vertices.size()];
    int vertex_references, visible_faces, size, index;
    boolean USE_SHORTS;
    
    //Build valid vertex map
    for(int i=0; i<faces.size(); i++){
    	if(faces.get(i).VISIBLE){
    		for(int j=0; j<faces.get(i).v.length; j++){
    			valid_vertex[faces.get(i).v[j]] = true;
    		}
    	}
    }
    
    for(int i=0; i<vertices.size(); i++){
    	if(valid_vertex[i]){
    		vertex_map[i] = used_vertices.size();
    		used_vertices.add(vertices.get(i));
    	}else{
    		vertex_map[i] = -1;
    	}
    }
    
    USE_SHORTS = used_vertices.size() <= 32768;
    
    //Count the number of vertex references in faces
    vertex_references = 0;
    
    for(int i=0; i<faces.size(); i++){
    	if(faces.get(i).VISIBLE){
    		vertex_references += faces.get(i).v.length;
    	}
    }
    
    //Count the number of visible faces
    if(groups.isEmpty()){
    	visible_faces = faces.size();
    }else{
    	visible_faces = 0;
    	itr_str = group_keys.iterator();
    	
    	while(itr_str.hasNext()){
    		key = itr_str.next();
    		values = groups.get(key);
    		
    		for(int i=0; i<values.size(); i++){
    			index = values.get(i);
    			
    			if(faces.get(index).VISIBLE){
    				visible_faces++;
    			}
    		}
    	}
    }
    
    //Set the size of the ByteBuffer
    size = 0;
    size += 4;												//Number of vertices
    size += 4;												//Number of faces
    size += 4*3*used_vertices.size();	//The vertices
    size += 2*visible_faces;					//The lengths of the faces
    
    //The references of the faces
    if(USE_SHORTS){				
    	size += 2*vertex_references;				
    }else{
    	size += 4*vertex_references;
    }
    
    //Allocate and write to the ByteBuffer
    ByteBuffer buffer = ByteBuffer.allocate(size);
    buffer.putInt(used_vertices.size());
    buffer.putInt(visible_faces);
    
    //Write out vertices
    for(int i=0; i<used_vertices.size(); i++){
      buffer.putFloat((float)used_vertices.get(i).x);
      buffer.putFloat((float)used_vertices.get(i).y);
      buffer.putFloat((float)used_vertices.get(i).z);
    }
    
    //Write out faces
    if(groups.isEmpty()){
	    for(int i=0; i<faces.size(); i++){
	    	buffer.putShort((short)faces.get(i).v.length);
	    	
	    	if(USE_SHORTS){
		    	for(int j=0; j<faces.get(i).v.length; j++){
		        buffer.putShort((short)vertex_map[faces.get(i).v[j]]);
		    	}
	    	}else{
		    	for(int j=0; j<faces.get(i).v.length; j++){
		        buffer.putInt(vertex_map[faces.get(i).v[j]]);
		    	}
	    	}
	    }
    }else{	//Save faces by groups
    	itr_str = group_keys.iterator();
    	
    	while(itr_str.hasNext()){
    		key = itr_str.next();
    		values = groups.get(key);

    		for(int i=0; i<values.size(); i++){
    			index = values.get(i);
    			
    			if(faces.get(index).VISIBLE){
    	    	buffer.putShort((short)faces.get(index).v.length);
    	    	
    	    	if(USE_SHORTS){
    		    	for(int j=0; j<faces.get(index).v.length; j++){
    		        buffer.putShort((short)vertex_map[faces.get(index).v[j]]);
    		    	}
    	    	}else{
    		    	for(int j=0; j<faces.get(index).v.length; j++){
    		        buffer.putInt(vertex_map[faces.get(index).v[j]]);
    		    	}
    	    	}
    			}
    		}
    	}
    }
    
    //Save the ByteBuffer to the specified FileChannel
    buffer.flip();
    
    try{
    	FileChannel outs = new FileOutputStream(filename).getChannel();
    	outs.write(buffer);
    	outs.close();
    }catch(Exception e) {}
    
    return true;
  }
}