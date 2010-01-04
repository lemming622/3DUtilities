package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
 * A mesh file loader for *.obj files stored in a binary format.
 * This version supports materials.
 *  @author Kenton McHenry
 */
public class MeshLoader_OBJ_BIN2 extends MeshLoader
{
	private int initial_capacity = 100000;

	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "obj_bin2";
	}
	
	 /**
   * Load a *.obj_bin2 model.
   *  @param filename the file to load
   *  @return the loaded mesh
   */
  public Mesh load(String filename)
  {
  	String path = Utility.getFilenamePath(filename);
    String name = Utility.getFilenameName(filename); 
  	Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
  	Vector<Point> vertices = new Vector<Point>(initial_capacity);
  	Vector<UV> uv = new Vector<UV>(initial_capacity);
  	Vector<Point> normals = new Vector<Point>(initial_capacity);
  	Vector<Face> faces = new Vector<Face>(initial_capacity);
  	TreeMap<String,Material> mtllib = null;
  	Vector<Material> materials = null;
  	Material material;
  	boolean USE_SHORTS = false;
  	
  	ByteBuffer buffer = null;
  	Point tmpv;
  	UV tmpuv;
  	Face tmpf;
  	Integer tmpi;
  	int nv, nt, nn, nm, nf, nr;
  	
  	//Check for materials
  	if(Utility.exists(path + name + ".mtl")){
    	mtllib = MeshLoader_OBJ.loadMaterialLibrary(path + name + ".mtl", mesh);
    	materials = new Vector<Material>();
    	
    	Iterator<String> itr = mtllib.keySet().iterator();
    	
    	while(itr.hasNext()){
    		materials.add(mtllib.get(itr.next()));
    	}
  	}
  	
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
    nt = buffer.getInt();
    nn = buffer.getInt();
    nm = buffer.getShort();
    nf = buffer.getInt();
    if(nv <= 32768) USE_SHORTS = true;
    
    vertices.ensureCapacity(nv);
    faces.ensureCapacity(nf);
    
    //Read in vertices
    for(int i=0; i<nv; i++){
    	tmpv = new Point(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
    	vertices.add(tmpv);
    }
    
    //Read in texture coordinates
    if(nt > 0){
	    for(int i=0; i<nt; i++){
	    	tmpuv = new UV(buffer.getFloat(), buffer.getFloat());
	    	uv.add(tmpuv);
	    }
    }
    
    //Read in normals
    if(nn > 0){
	    for(int i=0; i<nn; i++){
	    	tmpv = new Point(buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
	    	normals.add(tmpv);
	    }
    }
    
    //Read in faces
    for(int i=0; i<nf; i++){
    	material = null;
    	
    	if(nm > 0){
    		tmpi = (int)buffer.getShort();
    		if(tmpi>=0 && tmpi<materials.size()) material = materials.get(tmpi);
    	}
    	
    	nr = buffer.getShort();
    	tmpf = new Face(nr);
    	if(nt > 0) tmpf.uv = new UV[nr];
    	if(nn > 0) tmpf.vn = new Point[nr];
    	tmpf.material = material;
    	
    	if(USE_SHORTS){
    		for(int j=0; j<nr; j++){
	    		tmpf.v[j] = buffer.getShort();
	    		
	    		if(nt > 0){
	    			tmpi = (int)buffer.getShort();
	    			if(tmpi>=0 && tmpi<uv.size()) tmpf.uv[j] = uv.get(tmpi);
    			}
	    		
	    		if(nn > 0){
	    			tmpi = (int)buffer.getShort();
	    			if(tmpi>=0 && tmpi<normals.size()) tmpf.vn[j] = normals.get(tmpi);
	    		}
	    	}
    	}else{
	    	for(int j=0; j<nr; j++){
	    		tmpf.v[j] = buffer.getInt();
	    		
	    		if(nt > 0){
	    			tmpi = buffer.getInt();
	    			if(tmpi>=0 && tmpi<uv.size()) tmpf.uv[j] = uv.get(tmpi);
    			}
	    		
	    		if(nn > 0){
	    			tmpi = buffer.getInt();
	    			if(tmpi>=0 && tmpi<normals.size()) tmpf.vn[j] = normals.get(tmpi);
	    		}
	    	}
    	}
    	
    	//Check face for all null uv/normal lists (essential as many things assume the list will be null if this isn't needed!)
    	if(nt > 0){
	    	for(int j=0; j<nr; j++){
	    		if(tmpf.uv[j] == null){
	    			tmpf.uv = null;
	    			break;
	    		}
	    	}
    	}
    	
    	if(nn > 0){
	    	for(int j=0; j<nr; j++){
	    		if(tmpf.vn[j] == null){
	    			tmpf.vn = null;
	    			break;
	    		}
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
   * Save a *.obj_bin2 file.
   *  @param filename the file to save to
   *  @param mesh the mesh to save
   *  @return true if saved succesfully
   */
  public boolean save(String filename, Mesh mesh)
  {
  	String path = Utility.getFilenamePath(filename);
    String name = Utility.getFilenameName(filename); 
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
    Set<Material> materials = new TreeSet<Material>();
    TreeMap<Material,Integer> material_map = new TreeMap<Material,Integer>();
    Iterator<Material> mtl_itr;
    Vector<Texture> textures = mesh.getTextures();
    Set<UV> uvs = new TreeSet<UV>();
    TreeMap<UV,Integer> uv_map = new TreeMap<UV,Integer>();
    Iterator<UV> uv_itr;
    UV uv;    
    Set<Point> normals = new TreeSet<Point>();
    TreeMap<Point,Integer> normal_map = new TreeMap<Point,Integer>();
    Iterator<Point> v_itr;
    Point v;
    int vertex_references, visible_faces, size, index, count;
    Integer tmpi;
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
        
    //Build uv list
    for(int i=0; i<faces.size(); i++){
    	if(faces.get(i).VISIBLE && faces.get(i).uv!=null){
    		for(int j=0; j<faces.get(i).uv.length; j++){
    			uvs.add(faces.get(i).uv[j]);
    		}
    	}
    }
    
    uv_itr = uvs.iterator();
    count = 0;
    
    while(uv_itr.hasNext()){
    	uv_map.put(uv_itr.next(), count);
    	count++;
    }
    
    //Build normal list
    for(int i=0; i<faces.size(); i++){
    	if(faces.get(i).VISIBLE && faces.get(i).vn!=null){
    		for(int j=0; j<faces.get(i).vn.length; j++){
    			normals.add(faces.get(i).vn[j]);
    		}
    	}
    }
    
    v_itr = normals.iterator();
    count = 0;
    
    while(v_itr.hasNext()){
    	normal_map.put(v_itr.next(), count);
    	count++;
    }
    
    //Build material library
    for(int i=0; i<faces.size(); i++){
    	if(faces.get(i).material != null){
    		materials.add(faces.get(i).material);
    	}
    }
    
    mtl_itr = materials.iterator();
    count = 0;
    
    while(mtl_itr.hasNext()){
    	material_map.put(mtl_itr.next(), count);
    	count++;
    }
    
    //Count the number of visible faces and vertex references
    if(groups.isEmpty()){
    	visible_faces = faces.size();
    	vertex_references = 0;

      for(int i=0; i<faces.size(); i++){
      	vertex_references += faces.get(i).v.length;
      }
    }else{
    	visible_faces = 0;
    	vertex_references = 0;
    	itr_str = group_keys.iterator();
    	
    	while(itr_str.hasNext()){
    		key = itr_str.next();
    		values = groups.get(key);
    		
    		for(int i=0; i<values.size(); i++){
    			index = values.get(i);
    			
    			if(faces.get(index).VISIBLE){
    				visible_faces++;
          	vertex_references += faces.get(index).v.length;
    			}
    		}
    	}
    }
    
    //Set the size of the ByteBuffer
    size = 0;
    size += 4;												//Number of vertices
    size += 4;												//Number of texture coordinates
    size += 4;												//Number of normals
    size += 2;												//Number of materials
    size += 4;												//Number of faces
    size += 4*3*used_vertices.size();	//The vertices
    size += 4*2*uvs.size();						//The texture coordinates
    size += 4*3*normals.size();				//The normals
    
    //The materials and lengths of the faces
    if(!materials.isEmpty()){
    	size += 2*2*visible_faces;
    }else{
    	size += 2*visible_faces;
    }
    
    //The references of the faces
    if(USE_SHORTS){				
    	if(!uvs.isEmpty() && !normals.isEmpty()){
    		size += 2*3*vertex_references;				
    	}else if(!uvs.isEmpty() || !normals.isEmpty()){
    		size += 2*2*vertex_references;				
    	}else{
    		size += 2*1*vertex_references;				
    	}
    }else{
    	if(!uvs.isEmpty() && !normals.isEmpty()){
    		size += 4*3*vertex_references;				
    	}else if(!uvs.isEmpty() || !normals.isEmpty()){
    		size += 4*2*vertex_references;				
    	}else{
    		size += 4*1*vertex_references;				
    	}
    }
    
    //Allocate and write to the ByteBuffer
    ByteBuffer buffer = ByteBuffer.allocate(size);
    buffer.putInt(used_vertices.size());
    buffer.putInt(uvs.size());
    buffer.putInt(normals.size());
    buffer.putShort((short)materials.size());
    buffer.putInt(visible_faces);
    
    //Write out vertices
    for(int i=0; i<used_vertices.size(); i++){
      buffer.putFloat((float)used_vertices.get(i).x);
      buffer.putFloat((float)used_vertices.get(i).y);
      buffer.putFloat((float)used_vertices.get(i).z);
    }
    
    //Write out texture coordinates
    if(!uvs.isEmpty()){
      uv_itr = uvs.iterator();
      
      while(uv_itr.hasNext()){
      	uv = uv_itr.next();
      	buffer.putFloat((float)uv.u);
      	buffer.putFloat((float)uv.v);
      }
    }
    
    //Write out normals
    if(!normals.isEmpty()){
      v_itr = normals.iterator();
      
      while(v_itr.hasNext()){
      	v = v_itr.next();
      	buffer.putFloat((float)v.x);
      	buffer.putFloat((float)v.y);
      	buffer.putFloat((float)v.z);
      }
    }
    
    //Write out faces
    if(groups.isEmpty()){
	    for(int i=0; i<faces.size(); i++){
	    	if(!materials.isEmpty()){
	    		tmpi = null;
	    		if(faces.get(i).material != null) tmpi = material_map.get(faces.get(i).material);
	    		if(tmpi == null) tmpi = -1;
	    		buffer.putShort(tmpi.shortValue());
	    	}
	    	
	    	buffer.putShort((short)faces.get(i).v.length);
	    	
	    	if(USE_SHORTS){
		    	for(int j=0; j<faces.get(i).v.length; j++){
		        buffer.putShort((short)vertex_map[faces.get(i).v[j]]);
		        
		        //Check for texture coordinates
		        if(!uvs.isEmpty()){
			        tmpi = null;
			        if(faces.get(i).uv != null) tmpi = uv_map.get(faces.get(i).uv[j]);
			        if(tmpi == null) tmpi = -1;
			        buffer.putShort(tmpi.shortValue());
		        }
		        
		        //Check for normals
    				if(!normals.isEmpty()){
    					tmpi = null;
    					if(faces.get(i).vn != null) tmpi = normal_map.get(faces.get(i).vn[j]);
    					if(tmpi == null) tmpi = -1;
		        	buffer.putShort(tmpi.shortValue());
    				}
		    	}
	    	}else{
		    	for(int j=0; j<faces.get(i).v.length; j++){   	
		        buffer.putInt(vertex_map[faces.get(i).v[j]]);
		        
		        //Check for texture coordinates
		        if(!uvs.isEmpty()){
			        tmpi = null;
			        if(faces.get(i).uv != null) tmpi = uv_map.get(faces.get(i).uv[j]);
			        if(tmpi == null) tmpi = -1;
			        buffer.putInt(tmpi);
		        }
		        
		        //Check for normals
    				if(!normals.isEmpty()){
    					tmpi = null;
    					if(faces.get(i).vn != null) tmpi = normal_map.get(faces.get(i).vn[j]);
    					if(tmpi == null) tmpi = -1;
		        	buffer.putInt(tmpi);
    				}
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
    	    	if(!materials.isEmpty()){
    	    		tmpi = null;
    	    		if(faces.get(index).material != null) tmpi = material_map.get(faces.get(index).material);
    	    		if(tmpi == null) tmpi = -1;
    	    		buffer.putShort(tmpi.shortValue());
    	    	}
    	    	
    	    	buffer.putShort((short)faces.get(index).v.length);
    	    	
    	    	if(USE_SHORTS){
    		    	for(int j=0; j<faces.get(index).v.length; j++){
    		        buffer.putShort((short)vertex_map[faces.get(index).v[j]]);
    		        
    		        //Check for texture coordinates
    		        if(!uvs.isEmpty()){
    			        tmpi = null;
    			        if(faces.get(index).uv != null) tmpi = uv_map.get(faces.get(index).uv[j]);
    			        if(tmpi == null) tmpi = -1;
    			        buffer.putShort(tmpi.shortValue());
    		        }
    		        
    		        //Check for normals
        				if(!normals.isEmpty()){
        					tmpi = null;
        					if(faces.get(index).vn != null) tmpi = normal_map.get(faces.get(index).vn[j]);
        					if(tmpi == null) tmpi = -1;
    		        	buffer.putShort(tmpi.shortValue());
        				}
    		    	}
    	    	}else{
    		    	for(int j=0; j<faces.get(index).v.length; j++){
    		        buffer.putInt(vertex_map[faces.get(index).v[j]]);
    		        
    		        //Check for texture coordinates
    		        if(!uvs.isEmpty()){
    			        tmpi = null;
    			        if(faces.get(index).uv != null) tmpi = uv_map.get(faces.get(index).uv[j]);
    			        if(tmpi == null) tmpi = -1;
    			        buffer.putInt(tmpi);
    		        }
    		        
    		        //Check for normals
        				if(!normals.isEmpty()){
        					tmpi = null;
        					if(faces.get(index).vn != null) tmpi = normal_map.get(faces.get(index).vn[j]);
        					if(tmpi == null) tmpi = -1;
    		        	buffer.putInt(tmpi);
        				}
    		    	}
    	    	}
    			}
    		}
    	}
    }
    
    //If there are textures place all files within a subdirectory
    if(!materials.isEmpty()){
    	path += name + "/";
    	if(!Utility.exists(path)) new File(path).mkdir();
    	MeshLoader_OBJ.saveMaterialLibrary(path + name + ".mtl", materials, textures);
    }
    
    //Save the ByteBuffer to the specified FileChannel
    buffer.flip();
    
    try{
    	FileChannel outs = new FileOutputStream(path + name + ".obj_bin2").getChannel();
    	outs.write(buffer);
    	outs.close();
    }catch(Exception e) {}
    
    return true;
  }
}