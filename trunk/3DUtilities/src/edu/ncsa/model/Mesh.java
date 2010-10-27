package edu.ncsa.model;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.model.MeshLoader.ProgressEvent;
import edu.ncsa.model.loaders.*;
import edu.ncsa.image.*;
import edu.ncsa.matrix.*;
import edu.ncsa.utility.*;
import java.util.*;

/**
 * A representation of a 3D surface.
 * @author Kenton McHenry
 */
public class Mesh
{		
	public static enum DrawOption{DISABLED, ENABLED, MATERIAL, DECAL, MODULATE};
	
  protected Vector<Point> vertices = new Vector<Point>();
  protected Vector<Face> faces = new Vector<Face>();
    
  private Vector<Point> vertex_normals = new Vector<Point>();		//Needed by others (e.g. spin images)!
  private Vector<Color> vertex_colors = new Vector<Color>();		//Needed for point clouds!
  private Vector<Texture> textures = new Vector<Texture>();
  public boolean UNBOUND_TEXTURES = false;
  
  private Vector<Vector<Integer>> vertex_incident_faces = new Vector<Vector<Integer>>();
  private Vector<Vector<Integer>> vertex_neighboring_vertices = new Vector<Vector<Integer>>();
  private Vector<Edge> edges = new Vector<Edge>();   
  private Vector<Vector<Integer>> edge_incident_faces = new Vector<Vector<Integer>>();
  private Vector<Vector<Integer>> vertex_incident_edges = new Vector<Vector<Integer>>();
  private Vector<Double> edge_dihedral_angles = new Vector<Double>();
  
  //Data used by metallic illustration rendering
  private Vector<Point> axis = new Vector<Point>();
  private Vector<Point> curvature = new Vector<Point>();
  private Vector<Double> stripes = new Vector<Double>();
  public boolean INITIALIZED_METAL = false;
  
  protected Point center = new Point();
  protected double radius;
  private Vector<Point> PC = null;  
  
  private Class Signature = null;
  private MeshSignature signature = null;
  private TreeMap<String,Vector<Integer>> groups = new TreeMap<String,Vector<Integer>>();
  protected TreeMap<String,String> metadata = new TreeMap<String,String>();   
  private static Vector<MeshLoader> loaders = null;
  
  /** A list of supported formats. */
  public static Vector<String> formats = new Vector<String>();
  
  /**
   * Class constructor.
   */
  public Mesh()
  {
  	if(loaders == null){
	  	loaders = new Vector<MeshLoader>();
	  	
	  	try{loaders.add(new MeshLoader_XYZRGB());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_PC());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_OBJ_BIN1());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_OBJ_BIN2());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_OBJ());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_PLY());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_WRL());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_STP());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_VH());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_DAE());}catch(Throwable t) {}
	  	//try{loaders.add(new MeshLoader_DWG());}catch(Throwable t) {}
	  	
	  	try{loaders.add(new MeshLoader_STL());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_OFF());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_GTS());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_AC());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_RAW());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_GEO());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_TRI());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_BYU());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_NFF());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_MS3D());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_PLG());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_PHD());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_Q3O());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_SDML());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_TET());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_TM());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_KMZ());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_X());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_3DS());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_DXF());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_COB());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_IOB());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoader_VTK());}catch(Throwable t) {}
	  	
	  	try{loaders.add(new MeshLoaderJ3D_3DS());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoaderJ3D_X3D());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoaderJ3D_OBJ());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoaderJ3D_DXF());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoaderJ3D_LWS());}catch(Throwable t) {}
	  	try{loaders.add(new MeshLoaderJ3D_WRL());}catch(Throwable t) {}

	  	//Identify loadable file types
	  	formats.clear();
	  	
	  	for(int i=0; i<loaders.size(); i++){
	  	  formats.add(loaders.get(i).type());	
	  	}
  	}
  }
  
  /**
   * Class constructor.
   * @param v a vector of vertices to use
   * @param f a vector of faces to use
   */
  public Mesh(Vector<Point> v, Vector<Face> f)
  {
  	this();
    vertices = v;
    faces = f;
    initialize();
  }
  
  /**
	 * Copy the contents of another mesh to this mesh.
	 * @param m the mesh to assign values from
	 */
	public void deepCopy(Mesh m)
	{
		vertices = (Vector<Point>)Utility.deepCopy(m.vertices);
	  faces = (Vector<Face>)Utility.deepCopy(m.faces);
	  
	  vertex_colors = (Vector<Color>)Utility.deepCopy(m.vertex_colors);
	  vertex_normals = (Vector<Point>)Utility.deepCopy(m.vertex_normals); 
	  textures = (Vector<Texture>)Utility.deepCopy(m.textures);
	  UNBOUND_TEXTURES = m.UNBOUND_TEXTURES;
	  
	  vertex_incident_faces = (Vector<Vector<Integer>>)Utility.deepCopy(m.vertex_incident_faces);
	  vertex_neighboring_vertices = (Vector<Vector<Integer>>)Utility.deepCopy(m.vertex_neighboring_vertices);
	  edges = (Vector<Edge>)Utility.deepCopy(m.edges);   
	  edge_incident_faces = (Vector<Vector<Integer>>)Utility.deepCopy(m.edge_incident_faces);
	  vertex_incident_edges = (Vector<Vector<Integer>>)Utility.deepCopy(m.vertex_incident_edges);
	  edge_dihedral_angles = (Vector<Double>)Utility.deepCopy(m.edge_dihedral_angles);
	  
	  axis = (Vector<Point>)Utility.deepCopy(m.axis);
	  curvature = (Vector<Point>)Utility.deepCopy(m.curvature);
	  stripes = (Vector<Double>)Utility.deepCopy(m.stripes);
	  INITIALIZED_METAL = m.INITIALIZED_METAL;
	  
	  center = new Point(m.center);
	  radius = m.radius;
	  if(m.PC != null) PC = (Vector<Point>)Utility.deepCopy(m.PC);
	  
	  Signature = m.Signature;
	  
	  if(m.signature != null){
	  	signature = m.signature.clone();
	  }else{
	  	signature = null;
	  }
	
	  groups = (TreeMap<String,Vector<Integer>>)Utility.deepCopy(m.groups);
	  metadata = (TreeMap<String,String>)Utility.deepCopy(m.metadata);
	}

	/**
	 * Transfer the contents of another mesh to this mesh, allocating new memory for the
	 * other mesh to hold it's future data.
	 * @param m the mesh to assign values from
	 */
	public void transfer(Mesh m)
	{
		vertices = m.vertices; m.vertices = new Vector<Point>();
	  faces = m.faces; m.faces = new Vector<Face>();
	  
	  vertex_colors = m.vertex_colors; m.vertex_colors = new Vector<Color>();
	  vertex_normals = m.vertex_normals; m.vertex_normals = new Vector<Point>();
	  textures = m.textures; m.textures = new Vector<Texture>();
	  UNBOUND_TEXTURES = m.UNBOUND_TEXTURES;
	  
	  vertex_incident_faces = m.vertex_incident_faces; m.vertex_incident_faces = new Vector<Vector<Integer>>();
	  vertex_neighboring_vertices = m.vertex_neighboring_vertices; m.vertex_neighboring_vertices = new Vector<Vector<Integer>>();
	  edges = m.edges; m.edges = new Vector<Edge>(); 
	  edge_incident_faces = m.edge_incident_faces; m.edge_incident_faces = new Vector<Vector<Integer>>();
	  vertex_incident_edges = m.vertex_incident_edges; m.vertex_incident_edges = new Vector<Vector<Integer>>();
	  edge_dihedral_angles = m.edge_dihedral_angles; m.edge_dihedral_angles = new Vector<Double>();
	  
	  axis = m.axis; m.axis = new Vector<Point>();
	  curvature = m.curvature; m.curvature = new Vector<Point>();
	  stripes = m.stripes; m.stripes = new Vector<Double>();
	  INITIALIZED_METAL = m.INITIALIZED_METAL;
	  
	  center = m.center; m.center = new Point();
	  radius = m.radius;
	  PC = m.PC; m.PC = null;    
	  
	  Signature = m.Signature; m.Signature = null;
	  signature = m.signature; m.signature = null;
	  groups = m.groups; m.groups = new TreeMap<String,Vector<Integer>>();
	  metadata = m.metadata; m.metadata = new TreeMap<String,String>();
	}

	/**
	 * Clear the mesh.
	 */
	public void clear()
	{
	  vertices.clear();
	  faces.clear();
	  
	  vertex_colors.clear();
	  vertex_normals.clear();
	  textures.clear();
	  UNBOUND_TEXTURES = false;
	  
	  vertex_incident_faces.clear();
	  vertex_neighboring_vertices.clear();
	  edges.clear();   
	  edge_incident_faces.clear();
	  vertex_incident_edges.clear();
	  edge_dihedral_angles.clear();
	
	  axis.clear();
	  curvature.clear();
	  stripes.clear();
	  INITIALIZED_METAL = false;
	  
	  center = new Point();
	  radius = 0;
	  PC = null;
	  
	  groups.clear();
	  metadata.clear();
	}

	/**
   * Compare this mesh to another mesh.
   * @param mesh the mesh to compare to
   * @return a value such that larger numbers indicate a bigger difference
   */
  public double compareTo(Mesh mesh)
  {
  	if(signature != null){
  		return signature.compareTo(mesh.signature);
  	}else{
  		return -1;
  	}
  }
  
  /**
	 * Load 3D model from a file
	 * @param filename the absolute file name
	 * @param progressCallBack the callback handling progress updates
	 * @return true if successful
	 */
	public boolean load(String filename, ProgressEvent progressCallBack)
	{
		Mesh mesh = null;
		
		if(loaders != null){
	  	for(int i=0; i<loaders.size(); i++){
	  		if(filename.contains("." + loaders.get(i).type())){
	  			loaders.get(i).setProgressCallBack(progressCallBack);
	  			mesh = loaders.get(i).load(filename);
	  			
	  			if(mesh != null){
		  			transfer(mesh);
		  			//print();
		  			return true;
	  			}
	  		}
	  	}
		}
		
		return false;
	}

	/**
	 * Load 3D model from a file
	 * @param filename the absolute file name
	 * @return true if successful
	 */
	public boolean load(String filename)
	{
		return load(filename, null);
	}

	/**
	 * Save a 3D model to a file
	 * @param filename the absolute file name
	 * @return true if succesfull
	 */
	public boolean save(String filename)
	{
		if(loaders != null){	  	
	  	for(int i=0; i<loaders.size(); i++){
	  		if(filename.contains("." + loaders.get(i).type())){
	  			if(loaders.get(i).save(filename, this)) return true;
	  		}
	  	}
		}
		
		return false;
	}

	/**
	 * Initialize model specific topology data.
	 */
	public void initialize_topology()
	{
	  int v0, v1, tmpi;
	  int initial_capacity = 100000;
	  boolean FOUND;
	  
	  //Set vertex incident faces
	  vertex_incident_faces.clear();
	  vertex_incident_faces.ensureCapacity(vertices.size());
    
	  for(int i=0; i<vertices.size(); i++){
      vertex_incident_faces.add(new Vector<Integer>());
    }
    
    for(int i=0; i<faces.size(); i++){
      for(int j=0; j<faces.get(i).v.length; j++){
        vertex_incident_faces.get(faces.get(i).v[j]).add(i);
      }
    }
	  
	  //Set vertex neighboring vertices
	  vertex_neighboring_vertices.clear();
	  vertex_neighboring_vertices.ensureCapacity(vertices.size());
	  
	  if(true){
	    if(!faces.isEmpty()){
	      for(int i=0; i<vertices.size(); i++){
	        Vector<Integer> neighbors = new Vector<Integer>();
	  
	        for(int j=0; j<vertex_incident_faces.get(i).size(); j++){
	          for(int k=0; k<faces.get(vertex_incident_faces.get(i).get(j)).v.length; k++){
	            FOUND = false;
	            
	            if(i != faces.get(vertex_incident_faces.get(i).get(j)).v[k]){
	              for(int l=0; l<neighbors.size(); l++){   //Make sure not already there
	                if(neighbors.get(l) == faces.get(vertex_incident_faces.get(i).get(j)).v[k]){
	                  FOUND = true;
	                  break;
	                }
	              }
	              
	              if(!FOUND) neighbors.add(faces.get(vertex_incident_faces.get(i).get(j)).v[k]);
	            }
	          }
	        }
	  
	        vertex_neighboring_vertices.add(neighbors);
	      }
	    }
	  }
	  
	  //Set edges
	  edges.clear();
	  edges.ensureCapacity(initial_capacity);
	  edge_incident_faces.clear();
	  edge_incident_faces.ensureCapacity(initial_capacity);
	  
	  if(!faces.isEmpty()){
	    Vector<Vector<Integer>> edge_list = new Vector<Vector<Integer>>();
	    Vector<Vector<Vector<Integer>>> edge_list_neighbors = new Vector<Vector<Vector<Integer>>>();
	
	    for(int i=0; i<vertices.size(); i++){
	      edge_list.add(new Vector<Integer>());
	      edge_list_neighbors.add(new Vector<Vector<Integer>>());
	    }
	
	    for(int i=0; i<faces.size(); i++){
	      for(int j=0; j<faces.get(i).v.length; j++){
	        v0 = faces.get(i).v[j];
	        v1 = faces.get(i).v[(j+1)%faces.get(i).v.length];
	        FOUND = false;
	        
	        if(v1 < v0){
	          tmpi = v0;
	          v0 = v1;
	          v1 = tmpi;
	        }
	
	        for(int k=0; k<edge_list.get(v0).size(); k++){
	          if(edge_list.get(v0).get(k) == v1){
	            edge_list_neighbors.get(v0).get(k).add(i);   //Note: should never be duplicats unless face has duplicate vertices!
	            FOUND = true;
	            break;
	          }
	        }
	
	        if(!FOUND){
	          edge_list.get(v0).add(v1);
	          edge_list_neighbors.get(v0).add(new Vector<Integer>());
	          edge_list_neighbors.get(v0).get(edge_list_neighbors.get(v0).size()-1).add(i);
	        }
	      }
	    }
	
	    for(int i=0; i<edge_list.size(); i++){
	      for(int j=0; j<edge_list.get(i).size(); j++){
	        edges.add(new Edge(i, edge_list.get(i).get(j)));
	        edge_incident_faces.add(edge_list_neighbors.get(i).get(j));
	      }
	    }
	  }
	
	  //Set incident edges to a vertex
	  vertex_incident_edges.clear();
	  vertex_incident_edges.ensureCapacity(vertices.size());
	  
	  if(!faces.isEmpty()){
	    for(int i=0; i<vertices.size(); i++){
	      vertex_incident_edges.add(new Vector<Integer>());
	    }
	
	    for(int i=0; i<edges.size(); i++){
	      vertex_incident_edges.get(edges.get(i).v0).add(i);
	      vertex_incident_edges.get(edges.get(i).v1).add(i);
	    }
	  }
	}

	/**
	 * Initialize model specific position data.
	 */
	public void initialize_positions()
	{
	  Point norm;
	  Point tmpv;
	  double tmpd;
	  
	  //Calculate vertex information
	  center = Point.getCentroid(vertices);
	  radius = Point.getRadius(vertices, center);
	  try{PC = Point.getPC(vertices);}catch(Throwable t) {}	//Don't crash if matrix support isn't available.
	
	  //Calculate face centers
	  if(!faces.isEmpty()){
	    for(int i=0; i<faces.size(); i++){
	      tmpv = new Point(0, 0, 0);
	      
	      for(int j=0; j<faces.get(i).v.length; j++){
	        tmpv.plusEquals(vertices.get(faces.get(i).v[j]));
	      }
	      
	      tmpv.divideEquals(faces.get(i).v.length);
	      faces.get(i).center = tmpv;
	    }
	  }
	
	  //Calculate face normals
	  if(!faces.isEmpty()){
	    for(int i=0; i<faces.size(); i++){
	      if(faces.get(i).v.length >= 3){
	        norm = Face.normal(vertices.get(faces.get(i).v[0]), vertices.get(faces.get(i).v[1]), vertices.get(faces.get(i).v[2]));
	      }else{
	        norm = new Point(0, 0, 0);
	      }
	      
	      faces.get(i).normal = norm;
	    }
	  }
	
	  //Calculate vertex normals
	  vertex_normals.clear();
	  vertex_normals.ensureCapacity(vertices.size());
	  
	  if(!faces.isEmpty()){
	    for(int i=0; i<vertices.size(); i++){
	      norm = new Point(0, 0, 0);
	
	      for(int j=0; j<vertex_incident_faces.get(i).size(); j++){
	        if(faces.get(vertex_incident_faces.get(i).get(j)).v.length >= 3){
	          norm.plusEquals(faces.get(vertex_incident_faces.get(i).get(j)).normal);
	        }
	      }
	      
	      norm.divideEquals(vertex_incident_faces.get(i).size());   //Should not ever be zero
	      norm.divideEquals(norm.magnitude());
	      if(!norm.isValid()) norm = new Point(1, 0, 0);
	      
	      vertex_normals.add(norm);
	    }
	  }
	  
	  //Apply vertex normals to faces if they are not set
	  if(!faces.isEmpty()){
	  	for(int i=0; i<faces.size(); i++){
	  		if(faces.get(i).vn == null){
	    		faces.get(i).vn = new Point[faces.get(i).v.length];
	    		
	    		for(int j=0; j<faces.get(i).vn.length; j++){
	    			faces.get(i).vn[j] = vertex_normals.get(faces.get(i).v[j]);
	    		}
	  		}
	  	}
	  }
	  
	  //Set edge dihedral angles
	  edge_dihedral_angles.clear();
	  edge_dihedral_angles.ensureCapacity(edges.size());
	  
	  if(!faces.isEmpty()){
	    for(int i=0; i<edges.size(); i++){
	      if(edge_incident_faces.get(i).size() < 2){
	        tmpd = 1;
	      }else{
	        tmpd = faces.get(edge_incident_faces.get(i).get(0)).normal.times(faces.get(edge_incident_faces.get(i).get(1)).normal);
	      }
	
	      edge_dihedral_angles.add(Math.acos(tmpd) * 180.0/Math.PI);
	    }
	  }
	}

	/**
	 * Initialize data required for metallic rendering.
	 */
	public void initialize_metal()
	{
	  double tmpd;
	  
	  //Build curvature with respect to vertex normal
	  curvature.clear();
	  
	  if(!faces.isEmpty()){
	    int nghr;
	    double minc;    
	  
	    for(int v=0; v<vertices.size(); v++){
	      Point curv = new Point(0, 0, 0);
	      minc = -Double.MAX_VALUE;
	  
	      for(int f=0; f<vertex_incident_edges.get(v).size(); f++){
	        if(edges.get(vertex_incident_edges.get(v).get(f)).v0 == v){
	          nghr = edges.get(vertex_incident_edges.get(v).get(f)).v1;
	        }else{
	          nghr = edges.get(vertex_incident_edges.get(v).get(f)).v0;
	        }
	  
	        Point tmpv = new Point();
	        tmpv.x = vertices.get(nghr).x - vertices.get(v).x;
	        tmpv.y = vertices.get(nghr).y - vertices.get(v).y;
	        tmpv.z = vertices.get(nghr).z - vertices.get(v).z;
	  
	        tmpd = Math.sqrt(tmpv.x*tmpv.x + tmpv.y*tmpv.y + tmpv.z*tmpv.z);
	        tmpv.x /= tmpd;
	        tmpv.y /= tmpd;
	        tmpv.z /= tmpd;
	  
	        tmpd = tmpv.x*vertex_normals.get(v).x + tmpv.y*vertex_normals.get(v).y + tmpv.z*vertex_normals.get(v).z;
	        tmpd = Math.acos(tmpd) * 180.0/Math.PI;
	  
	        if(tmpd > minc){
	          minc = tmpd;
	          curv = tmpv;
	        }       
	      }
	  
	      curvature.add(curv);
	    }
	  }
	
	  //Build axis
	  axis.clear();
	
	  if(!faces.isEmpty()){
	   double theta;
	   double radius;
	   double amp = 8;
	    
	    for(int i=0; i<vertices.size(); i++){
	      tmpd = curvature.get(i).x * vertex_normals.get(i).x + 
	             curvature.get(i).y * vertex_normals.get(i).y +
	             curvature.get(i).z * vertex_normals.get(i).z;
	
	      theta = Math.PI - Math.acos(tmpd);
	      radius = 0.5 / Math.cos(theta);
	
	      //Build virtual axis
	      Point tmpv = new Point();
	      tmpv.x = vertices.get(i).x - (amp * radius * vertex_normals.get(i).x);
	      tmpv.y = vertices.get(i).y - (amp * radius * vertex_normals.get(i).y);
	      tmpv.z = vertices.get(i).z - (amp * radius * vertex_normals.get(i).z);
	
	      axis.add(tmpv);
	    }
	  }
	
	  //Build metal map
	  stripes.clear();    
	  
	  if(!faces.isEmpty()){
	    Vector<Double> tmpvd = new Vector<Double>();
	    int num = 20;
	    double b = -0.1;
	    double a = 0.5; //0.4
	
	    //Generate black and white lines
	    for(int i=0; i<num; i++){
	      tmpvd.add(b + (Math.random() * a));  
	    }
	
	    //Filter lines with 1-5-1 weighting scheme
	    for(int i=0; i<num; i++){
	      tmpd = tmpvd.get((i-1 < 0)?(num-1):(i-1)) + 5.0*tmpvd.get(i) + tmpvd.get((i+1)%num) / 7.0;
	      if(tmpd < 0) tmpd = 0;
	      if(tmpd > 1) tmpd = 1;
	      stripes.add(tmpd);
	    }
	
	    //Debug
	    //for(int i=0; i<num; i++) System.out.println(" " + stripes.get(i));
	
	    //Resample lines
	    double span = 10;
	    double prev = 0;
	    double next = stripes.get(0);
	    double wght;
	    int at = 0;
	    tmpvd.clear();
	
	    for(int i=0; i<(span*num); i++){
	      wght = i % (int)span;
	
	      if(wght == 0){
	        at++;
	        if(at >= stripes.size()) at = stripes.size()-1;
	        
	        prev = next;
	        next = stripes.get(at);
	      }
	
	      tmpvd.add((1-wght/(span-1))*prev + (wght/(span-1))*next);
	    }
	
	    stripes = tmpvd;
	  }
	}

	/**
	 * Initialize model specific data after a new mesh is loaded.
	 * Note: desired vertices and faces should be set prior to calling this method.
	 */
	public void initialize()
	{
		initialize_topology();
		initialize_positions();
	  
	  INITIALIZED_METAL = false;
	  
	  //Add geometry based meta data
	  addMetaData("Vertices", Integer.toString(vertices.size()));
	  addMetaData("Faces", Integer.toString(faces.size()));
	  addMetaData("Radius", Utility.round(getRadius(),2));
	}

	/**
	 * Set the center of mass of the vertices to the origion.
	 * @param new_radius the desired radial extreme of the vertices from the origin
	 */
	public void center(float new_radius)
	{
		Point point;
	  double scale = new_radius/radius;
	  
	  for(int i=0; i<vertices.size(); i++){
	    vertices.get(i).x = (vertices.get(i).x - center.x) * scale;
	    vertices.get(i).y = (vertices.get(i).y - center.y) * scale;
	    vertices.get(i).z = (vertices.get(i).z - center.z) * scale;
	  }
	  
	  //Fix face centers
	  for(int i=0; i<faces.size(); i++){
	  	if(faces.get(i).center != null){
	  		point = faces.get(i).center;
	  		point.minusEquals(center);
	  		point.timesEquals(scale);
	  	}
	  }
	  
	  //Update values
	  radius = new_radius;    
	  center.x = 0;
	  center.y = 0;
	  center.z = 0;
	}

	/**
	 * Print the vertex coordinates and face indices of the currently loaded model.
	 */
	public void print()
	{
	  System.out.println();
	  
	  for(int i=0; i<vertices.size(); i++){
	    System.out.println("v: " + vertices.get(i).x + ", " + vertices.get(i).y + ", " + vertices.get(i).z);
	  }
	  
	  for(int i=0; i<faces.size(); i++){
	    System.out.print("f: ");
	    for(int j=0; j<faces.get(i).v.length; j++){
	      System.out.print(faces.get(i).v[j]);
	    }
	    System.out.println();
	  }
	}

	/**
   * Get the vector of vertices in the mesh.
   * @return the vector of vertices
   */
  public Vector<Point> getVertices()
  {
    return vertices;
  }
  
  /**
   * Get the vertices at the given indices.
   * @param vertex_indices the vertex indices
   * @return the vertices at those indices
   */
  public Vector<Point> getVertices(Vector<Integer> vertex_indices)
  {
  	Vector<Point> tmpv = new Vector<Point>();
  	
  	for(int i=0; i<vertex_indices.size(); i++){
  		tmpv.add(vertices.get(vertex_indices.get(i)));
  	}
  	
  	return tmpv;
  }
  
  /**
   * Get the ith vertex in the mesh.
   * @param i the index of the desired vertex
   * @return the ith vertex
   */
  public Point getVertex(int i)
  {
    return vertices.get(i);
  }
  
  /**
   * Set the vector of vertices in the mesh.
   * @param v the vector of vertices
   */
  public void setVertices(Vector<Point> v)
  {
    vertices = v;
  }
  
  /**
   * Set the vector of vertices in the mesh.
   * @param v an ArrayList of vertices
   */
  public void setVertices(ArrayList<Point> v)
  {
  	vertices.clear();
    vertices.addAll(v);
  }
  
  /**
   * Get the vector of vertex colors.
   * @return the vector of colors
   */
  public Vector<Color> getVertexColors()
  {
    return vertex_colors;  
  }
  
  /**
   * Set the colors for the vertices in the mesh.
   * @param c the vertex colors
   */
  public void setVertexColors(Vector<Color> c)
  {
  	vertex_colors = c;
  }
  
  /**
   * Get the vector of vertex normals.
   * @return the vertex normals
   */
  public Vector<Point> getVertexNormals()
  {
    return vertex_normals;
  }
  
  /**
   * Get the ith vertex normal.
   * @param i the index of the desired vertex normal
   * @return the ith vertex normal
   */
  public Point getVertexNormal(int i)
  {
    return vertex_normals.get(i);
  }
  
  /**
   * Get the vector of vertex incident faces.
   * @return the vertex incident faces
   */
  public Vector<Vector<Integer>> getVertexIncidentFaces()
  {
    return vertex_incident_faces;
  }
  
  /**
   * Get the vector of faces in the mesh.
   * @return the vector of faces
   */
  public Vector<Face> getFaces()
  {
    return faces;
  }
  
  /**
   * Get the faces at the given indices.
   * @param face_indices the face indices
   * @return the faces at those indices
   */
  public Vector<Face> getFaces(Vector<Integer> face_indices)
  {
  	Vector<Face> tmpv = new Vector<Face>();
  	
  	for(int i=0; i<face_indices.size(); i++){
  		tmpv.add(faces.get(face_indices.get(i)));
  	}
  	
  	return tmpv;
  }
  
  /**
   * Get the ith face in the mesh.
   * @param i the index of the desired face
   * @return the ith face
   */
  public Face getFace(int i)
  {
    return faces.get(i);
  }
  
  /**
	 * Get the normal for the ith face in the mesh.
	 * @param i the index of the desired face's normal
	 * @return the normal
	 */
	public Point getFaceNormal(int i)
	{
		return faces.get(i).normal;
	}

	/**
	 * Get the material for the ith face in the mesh.
	 * @param i the index of the desired face's material
	 * @return the material
	 */
	public Material getFaceMaterial(int i)
	{
	  return faces.get(i).material;
	}
  
  /**
   * Set the vector of faces in the mesh.
   * @param f the vector of faces
   */
  public void setFaces(Vector<Face> f)
  {
    faces = f;
  }
  
  /**
   * Set the vector of faces in the mesh.
   * @param f an ArrayList of faces
   */
  public void setFaces(ArrayList<Face> f)
  {
  	faces.clear();
    faces.addAll(f);
  }
  
  /**
   * Get the vector of edges in the mesh.
   * @return the vector of edges
   */
  public Vector<Edge> getEdges()
  {
  	return edges;
  }
  
  /**
   * Get the list of edge incident faces.
   * @return the list of edge incident faces
   */
  public Vector<Vector<Integer>> getEdgeIncidentFaces()
  {
  	return edge_incident_faces;
  }
  
  /**
   * Get the list of edge dihedral angles.
   * @return the list of edge dihedral angles
   */
  public Vector<Double> getEdgeDihedralAngles()
  {
  	return edge_dihedral_angles;
  }
  
  /**
	 * Get the face groups in the mesh.
	 * @return the groups (key=name, value=vector of face indices)
	 */
	public TreeMap<String,Vector<Integer>> getGroups()
	{
		return groups;
	}

	/**
	 * Get the specified face group in the mesh.
	 * @param name the name of the group
	 * @return the group face indices
	 */
	public Vector<Integer> getGroup(String name)
	{
		return groups.get(name);
	}

	/**
	 * Get the indices of vertices incident to the given groups faces.
	 * @param name the name of the group
	 * @return the incident vertex indices
	 */
	public Vector<Integer> getGroupVertices(String name)
	{
		Vector<Integer> group_vertices = new Vector<Integer>();
		TreeSet<Integer> vertex_set = new TreeSet<Integer>();
		Vector<Integer> group = groups.get(name);
		Face face;
		
		for(int i=0; i<group.size(); i++){
			face = faces.get(group.get(i));
			
			for(int j=0; j<face.v.length; j++){
				vertex_set.add(face.v[j]);
			}
		}
		
		group_vertices.addAll(vertex_set);
		
		return group_vertices;
	}

	/**
	 * Set the face groups in the mesh.
	 * @param g the groups (key=name, value=vector of face indices)
	 */
	public void setGroups(TreeMap<String,Vector<Integer>> g)
	{
	  groups = g;
	}

	/**
	 * Toggle faces in the given group.
	 * @param name the name of the group
	 */
	public void toggleGroup(String name)
	{
		Vector<Integer> group = groups.get(name);
		
		for(int i=0; i<group.size(); i++){
			faces.get(group.get(i)).VISIBLE = !faces.get(group.get(i)).VISIBLE;
		}
	}

	/**
	 * Add texture to mesh.
	 * @param name the name of the texture
	 * @param argb the image pixels
	 * @param wh the width/height (should be the same)
	 * @return the mesh specific texture id for this mesh
	 */
	public int addTexture(String name, int[] argb, int wh)
	{
		textures.add(new Texture(name, argb, wh));
		UNBOUND_TEXTURES = true;
		
		return textures.size()-1;
	}

	/**
	 * Get textures.
	 * @return the textures
	 */
	public Vector<Texture> getTextures()
	{
		return textures;
	}

	/**
	 * Set default material values to faces without materials.
	 */
	public void setMaterials()
	{
	  if(!faces.isEmpty()){
	    for(int i=0; i<faces.size(); i++){
	    	if(faces.get(i).material == null) faces.get(i).material = new Material();
	    	if(faces.get(i).material.diffuse == null) faces.get(i).material.diffuse = new Color(0.7f, 0.7f, 0.7f);
	    	faces.get(i).material.emissive = new Color(0, 0, 0);
	    	faces.get(i).material.specular = new Color(0.2f, 0.2f, 0.2f);
	      //faces.get(i).material.transmissive = new Color(0.5f, 0.5f, 0.5f);
	    	faces.get(i).material.shininess = 1.0f;
	    	faces.get(i).material.index_of_refraction = 1.5f;
	    }
	  }
	}

	/**
	 * Get a meta data entry.
	 * @param key the key of the desired meta data entry
	 * @return the associated value of the meta data entry
	 */
	public String getMetaData(String key)
	{
		return metadata.get(key);
	}

	/**
	 * Get the meta data as an HTML marked up string.
	 * @param order the suggested order of some of the keys
	 * @param GET_ALL obtain meta data not suggested in the order list
	 * @return the meta data in HTML
	 */
	public String getMetaDataHTML(String[] order, boolean GET_ALL)
	{
		String output = "";
		Set<String> suggested_keys = new TreeSet<String>();
		Set<String> keys = metadata.keySet();
		Iterator<String> itr;
		String key;
		
		//Output suggested keys first
		for(int i=0; i<order.length; i++){
			key = order[i];
			suggested_keys.add(key);
			output += "<b>" + key + ": </b>" + metadata.get(key) + "<br>";
		}
		
		//Output everything else in sorted order
		if(GET_ALL){
	  	itr = keys.iterator();
	  	
	  	while(itr.hasNext()){
	  		key = itr.next();
	  		
	  		if(!suggested_keys.contains(key)){
	  			output += "<b>" + key + ": </b>" + metadata.get(key) + "<br>";
	  		}
	  	}
		}
		
		return output;
	}

	/**
	 * Add a meta data entry to the mesh.
	 * @param key the key for the meta data entry
	 * @param value the associated value of the meta data entry
	 */
	public void addMetaData(String key, String value)
	{
		if(!key.isEmpty() && !value.isEmpty()){
	  	if(metadata.containsKey(key)){
	  		metadata.remove(key);
	  	}
	  	
	  	metadata.put(key, value);
		}
	}

	/**
	 * Assign meta data derived from the given file.
	 * @param filename the file's absolute name
	 */
	public void addFileMetaData(String filename)
	{
	  String name = "";
	  String path = "";
	  String extension = "";
	  int index;
	  
	  if(!filename.isEmpty()){
	  	index = filename.lastIndexOf("/");
	    
	    if(index >= 0){
	    	path = filename.substring(0, index+1);
	    	name = filename.substring(index+1);
	    }else{
	    	name = filename;
	    }
	    
	    if(name.charAt(0) == '.') name = name.substring(1);  //Ignore preceding '.' if present
	    index = name.indexOf(".");
	    
	    if(index >= 0){
	    	extension = name.substring(index+1);
	    	name = name.substring(0, index);
	    }
	
	    addMetaData("Name", name);
	    addMetaData("File", filename);
	    addMetaData("Type", extension);
	  }
	}

	/**
	 * Clear all meta data.
	 */
	public void clearMetaData()
	{
		metadata.clear();
	}

	/**
	 * Compute a signature that can be used to compare 3D models.
	 * @param Signature the type of signature to use
	 */
	public void setSignature(Class Signature)
	{
		this.Signature = Signature;
		
		try{
	  	signature = (MeshSignature)Signature.newInstance();
	    signature.setSignature(this);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Compute a signature that can be used to compare 3D models.
	 * @param Signature the type of signature to use
	 * @param filename the file where the signature will be stored
	 * @param REBUILD true if we rebuild the signature whether or not it already exists
	 */
	public void setSignature(Class Signature, String filename, boolean REBUILD)
	{
		this.Signature = Signature;
	  REBUILD = !Utility.exists(filename) || REBUILD;
	  
		try{
	  	signature = (MeshSignature)Signature.newInstance();
	
	    if(REBUILD){
	      signature.setSignature(this);
	      
	      if(filename != null){
	        signature.save(filename);
	      }
	    }else{
	      signature.load(filename);
	    }
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Get the mesh signature.
	 */
	public MeshSignature getSignature()
	{
		return signature;
	}

	/**
	 * Get the centroid of the mesh.
	 * @return the center of mass of this mesh
	 */
	public Point getCenter()
	{
		return center;
	}

	/**
	 * Get the extreme radius from the centroid of the currently loaded model.
	 * @return the radius
	 */
	public double getRadius()
	{
	  return radius;
	}

	/**
	 * Get the principal components of this mesh.
	 * @return the principal components of this mesh
	 */
	public Vector<Point> getPC()
	{
		return PC;
	}

	/**
	 * Get the estimated axis for each point for the purpose of metallic rendering.
	 * @return the estimated axis for each point
	 */
	public Vector<Point> getMetallicAxis()
	{
		return axis;
	}

	/**
	 * Get the stripe intensities for metallic rendering.
	 * @return the stripe intensities for metallic rendering
	 */
	public Vector<Double> getMetallicStripes()
	{
		return stripes;
	}

	/**
	 * Set the mesh data.
	 * @param v vertices
	 * @param c vertex colors (can be null)
	 * @param f faces (can be null)
	 * @param INITIALIZE true if the mesh should be initialized
	 */
	public void setData(Vector<Point> v, Vector<Color> c, Vector<Face> f, boolean INITIALIZE)
	{
		clear();
		vertices = v;
		if(c != null) vertex_colors = c;
		if(f != null) faces = f;
		if(INITIALIZE) initialize();
	}

	/**
	 * Add data to the mesh.
	 * @param v vertices
	 * @param f faces referencing the given vertices (can be null)
	 * @param color the color of the vertices (can be null)
	 */
	public void addData(Vector<Point> v, Vector<Face> f, Color color)
	{
	  int n = vertices.size();
	  
	  for(int i=0; i<v.size(); i++){
	    vertices.add(v.get(i));
	    if(color != null) vertex_colors.add(color);
	  }
	  
	  if(f != null){
	    for(int i=0; i<f.size(); i++){
	    	faces.add(f.get(i).plus(n));
	    }
	  }
	}

	/**
	 * Add data to the mesh.
	 * @param v vertices
	 * @param f faces referencing the given vertices (can be null)
	 * @param tid a texture id (can be -1)
	 * @param name the name of the group (can be null)
	 */
	public void addData(Vector<Point> v, Vector<Face> f, int tid, String name)
	{
		Vector<Integer> group;
		Face face;
	  int nv = vertices.size();
	  int nf = faces.size();
	  
	  for(int i=0; i<v.size(); i++){
	    vertices.add(v.get(i));
	  }
	  
	  if(f != null){
	    for(int i=0; i<f.size(); i++){
	    	face = f.get(i).plus(nv);
	    	
	    	if(tid != -1){
	    		if(face.material == null) face.material = new Material();
	    		face.material.tid = tid;
	    	}
	    	
	    	faces.add(face);
	    }
	  }
	  
	  if(name != null){
	  	group = groups.get(name);
	  	
	  	if(group == null){
	  		group = new Vector<Integer>();
	  		groups.put(name, group);
	  	}
	  	
	  	for(int i=0; i<f.size(); i++){
	  		group.add(i+nf);
	  	}
	  }
	}

	/**
	 * Add data to the mesh.
	 * @param v vertices
	 * @param f faces referencing the given vertices (can be null)
	 * @param material the face material (can be null)
	 * @param name the name of the group (can be null)
	 */
	public void addData(Vector<Point> v, Vector<Face> f, Material material, String name)
	{
		Vector<Integer> group;
		Face face;
	  int nv = vertices.size();
	  int nf = faces.size();
	  
	  for(int i=0; i<v.size(); i++){
	    vertices.add(v.get(i));
	  }
	  
	  if(f != null){
	    for(int i=0; i<f.size(); i++){
	    	face = f.get(i).plus(nv);
	    	face.material = material;
	    	faces.add(face);
	    }
	  }
	  
	  if(name != null){
	  	group = groups.get(name);
	  	
	  	if(group == null){
	  		group = new Vector<Integer>();
	  		groups.put(name, group);
	  	}
	  	
	  	for(int i=0; i<f.size(); i++){
	  		group.add(i+nf);
	  	}
	  }
	}

	/**
	 * Add data from another mesh to this mesh
	 * @param mesh the mesh data to add
	 * @param R a rotation that should be applied to the mesh to be merged
	 * @param tx a translation to the merged mesh in the x-direction
	 * @param ty a translation to the merged mesh in the y-direction
	 * @param tz a translation to the merged mesh in the z-direction
	 * @param scl a scaling to the merged mesh
	 */
	public void addData(Mesh mesh, double[][] R, double tx, double ty, double tz, double scl)
	{
		Vector<Integer> group, mesh_group;
		Vector<Point> mesh_vertices;
		Face tmpf;
		String group_name;
		int nv = vertices.size();
		int nf = faces.size();
		int nt = textures.size();
		
		//Verify default materials set (in case some models have materials)
		for(int i=0; i<faces.size(); i++){
			if(faces.get(i).material == null){
				faces.get(i).material = new Material(new Color(0.5f, 0.5f, 0.5f));
			}
		}
		
		//Verify groups are set
		if(groups.isEmpty()){
			group = new Vector<Integer>();
			groups.put(getMetaData("Name"), group);
		
	  	for(int i=0; i<faces.size(); i++){
	  		group.add(i);
	  	}
		}
		
		//Transform and copy vertices
		mesh_vertices = Point.transform(R, mesh.vertices);
		
		for(int i=0; i<mesh_vertices.size(); i++){
			vertices.add(mesh_vertices.get(i).times(scl).plus(tx, ty, tz));
		}
		
		//Copy textures
		for(int i=0; i<mesh.textures.size(); i++){
			textures.add(mesh.textures.get(i));
		}
		  	
		//Copy faces
		for(int i=0; i<mesh.faces.size(); i++){
			tmpf = mesh.faces.get(i).plus(nv);
			if(tmpf.material == null) tmpf.material = new Material(new Color(0.5f, 0.5f, 0.5f));
			if(tmpf.material.tid != -1) tmpf.material.tid += nt;
			faces.add(tmpf);
		}
		
		//Add groups
		if(mesh.groups.isEmpty()){
			group_name = mesh.getMetaData("Name");
	   	group = groups.get(group_name);
	  	
	  	if(group == null){
	  		group = new Vector<Integer>();
	  		groups.put(group_name, group);
	  	}
	  	
			for(int i=0; i<mesh.faces.size(); i++){
				group.add(i + nf);
			}
		}else{
			Iterator<String> itr = mesh.groups.keySet().iterator();
			
			while(itr.hasNext()){
				group_name = itr.next();
				mesh_group = mesh.groups.get(group_name);
		   	group = groups.get(group_name);
	    	
	    	if(group == null){
	    		group = new Vector<Integer>();
	    		groups.put(group_name, group);
	    	}
	  	
				for(int i=0; i<mesh_group.size(); i++){
					group.add(mesh_group.get(i) + nf);
				}
			}
		}
	}

	/**
	 * Add the given camera to the scene stored in the mesh.
	 * @param camera the camera
	 * @param scl the scaling factor for the cameras
	 * @param c0 the starting color of the gradient for the camera
	 * @param c1 the ending color of the gradient for the camera
	 */
	public void addCamera(Camera camera, double scl, Color c0, Color c1)
	{
		double[][] K = camera.getK();;
	  double[][] RTi = camera.getRTi();
	  double aspect = K[0][0] / K[1][1];
	  int v0;
	  
	  //Draw camera location
	  if(false){  
	    vertices.add(new Point(RTi[0][3], RTi[1][3], RTi[2][3]));
	    vertex_colors.add(new Color(1, 0, 0));
	  }
	  
	  //Draw camera view cone
	  if(true){ 
	    double xy = scl * 0.1;
	    double z = scl * 0.25;
	    //aspect = 1.5;
	
	    Point[] points = new Point[5];
	    points[0] = new Point(0, 0, 0);
	    points[1] = new Point(aspect*xy, xy, -z);
	    points[2] = new Point(-aspect*xy, xy, -z);
	    points[3] = new Point(-aspect*xy, -xy, -z);
	    points[4] = new Point(aspect*xy, -xy, -z);
	    
	    //Add the cones points
	    v0 = vertices.size();
	    
	    for(int j=0; j<points.length; j++){
	      vertices.add(Point.transform(RTi, points[j]));
	      
	      if(j==0){
	        vertex_colors.add(c0);
	      }else{
	        vertex_colors.add(c1);
	      }
	    }
	    
	    //Add edges radiating from center
	    for(int j=0; j<4; j++){
	      Face tmpf = new Face(2);
	      tmpf.v[0] = v0;
	      tmpf.v[1] = v0 + 1 + j;
	      faces.add(tmpf);
	    }
	    
	    //Add edges along circumfrence
	    for(int j=0; j<4; j++){
	      Face tmpf = new Face(2);
	      tmpf.v[0] = v0 + 1 + j;
	      tmpf.v[1] = v0 + 1 + (1+j)%4;
	      faces.add(tmpf);
	    }
	  }
	  
	  //Draw axis
	  if(true){
	  	//Set up axis points in canonical position
	    Color red = new Color(1, 0, 0);
	    Color green = new Color(0, 1, 0);
	    Color blue = new Color(0, 0, 1);
	    double xyz = 0.1 * scl;
	    
	    //Add the cones points
	    v0 = vertices.size();
	    vertices.add(Point.transform(RTi, new Point(0, 0, 0)));
	    vertex_colors.add(red);
	    vertices.add(Point.transform(RTi, new Point(xyz, 0, 0)));
	    vertex_colors.add(red);
	    vertices.add(Point.transform(RTi, new Point(0, 0, 0)));
	    vertex_colors.add(green);
	    vertices.add(Point.transform(RTi, new Point(0, xyz, 0)));
	    vertex_colors.add(green);
	    vertices.add(Point.transform(RTi, new Point(0, 0, 0)));
	    vertex_colors.add(blue);
	    vertices.add(Point.transform(RTi, new Point(0, 0, xyz)));
	    vertex_colors.add(blue);
	    
	    //Add edges
	    for(int j=0; j<3; j++){
	      Face tmpf = new Face(2);
	      tmpf.v[0] = v0 + 2*j;
	      tmpf.v[1] = v0 + 2*j + 1;
	      faces.add(tmpf);
	    }
	  }
	}

	/**
	 * Add the given cameras to the scene stored in the mesh.
	 * @param cameras the camera external parameters!
	 * @param scl the scaling factor for the cameras
	 * @param c0 the starting color of the gradient for the camera
	 * @param c1 the ending color of the gradient for the camera
	 */
	public void addCameras(Vector<Camera> cameras, double scl, Color c0, Color c1)
	{
		for(int i=0; i<cameras.size(); i++){
			addCamera(cameras.get(i), scl, c0, c1);
		}
	}

	/**
   * Get the vertices incident to faces within a given selection area.
   * @param RT a rigid transformation on the model
   * @param minx the minimum x-coordinate of the selection bounding box
   * @param maxx the maximum x-coordinate of the selection bounding box
   * @param miny the minimum y-coordinate of the selection bounding box
   * @param maxy the maximum y-coordinate of the selection bounding box
   * @param visible_faces a list of visible face indices, passed in to allow pre-calculation/reuse (can be null)
   * @return the indices of the incident vertices
   */
  public Vector<Integer> getSelectionVertices(double[][] RT, double minx, double maxx, double miny, double maxy, TreeSet<Integer> visible_faces)
  {
  	Vector<Integer> selection_vertices = new Vector<Integer>();
  	TreeSet<Integer> vertex_set = new TreeSet<Integer>();
  	Vector<Point> transformed_vertices;
  	Polygon bbox = new Polygon();
  	PolygonGroup pg;
  	
  	ImageViewer debug_viewer = null; //new ImageViewer();
  	int[] debug_img = null; int w = 600; int h = 600; 
  	
  	//Get view transformed vertices
  	transformed_vertices = Point.transform(RT, vertices);
  
  	for(int i=0; i<transformed_vertices.size(); i++){
  		transformed_vertices.get(i).z = 0;
  	}
  	
  	//Setup bounding box polyglon
  	bbox.add(new Point(minx, miny, 0));
  	bbox.add(new Point(maxx, miny, 0));
  	bbox.add(new Point(maxx, maxy, 0));
  	bbox.add(new Point(minx, maxy, 0));
  
  	//Find intersecting faces
  	for(int i=0; i<faces.size(); i++){
  		if(visible_faces == null || visible_faces.contains(i)){
    		pg = PolygonGroup.getPlanarIntersection(new PolygonGroup(bbox), new PolygonGroup(faces.get(i).getPolygon(transformed_vertices)));
    		
    		//If within bounds store incident vertices
    		if(!pg.isEmpty() && !pg.get(0).isEmpty()){
  	  	  for(int j=0; j<faces.get(i).v.length; j++){
  	  	  	vertex_set.add(faces.get(i).v[j]);
  	  	  }
    		}
    		
    		if(debug_viewer != null){
    			if(debug_img == null) debug_img = ImageUtility.getNewARGBImage(w, h, 0x00ffffff);
    			Point.drawPolygon(debug_img, w, h, bbox.transform(1, -1, w/2, h/2).getVertices(), 0x000000ff);
    			Point.drawPolygon(debug_img, w, h, faces.get(i).getPolygon(transformed_vertices).transform(1, -1, w/2, h/2).getVertices(), 0x00000000);
    			//if(!pg.isEmpty()) Point.drawPolygon(debug_img, w, h, pg.get(0).transform(1, -1, w/2, h/2).getVertices(), 0x000000ff);
    			if(!pg.isEmpty()) Point.drawPolygon(debug_img, w, h, faces.get(i).getPolygon(transformed_vertices).transform(1, -1, w/2, h/2).getVertices(), 0x000000ff);
    			debug_viewer.set(debug_img, w, h);
    		}
  		}
  	}
  	
  	selection_vertices.addAll(vertex_set);
  	  	
  	return selection_vertices;
  }

	/**
   * Get the vertices incident to faces on the given point.
   * @param RT a rigid transformation on the model
   * @param x the x-coordinate of the selection point
   * @param y the y-coordinate of the selection point
   * @param visible_faces a list of visible face indices, passed in to allow pre-calculation/reuse (can be null)
   * @return the indices of the incident vertices
   */
	public Vector<Integer> getSelectionVertices(double[][] RT, double x, double y, TreeSet<Integer> visible_faces)
  {
  	Vector<Integer> selection_vertices = new Vector<Integer>();
  	TreeSet<Integer> vertex_set = new TreeSet<Integer>();
  	Vector<Point> transformed_vertices;
  	Point point = new Point(x, y, 0);
  	Polygon polygon;
  	
  	//Get view transformed vertices
  	transformed_vertices = Point.transform(RT, vertices);

  	for(int i=0; i<transformed_vertices.size(); i++){
  		transformed_vertices.get(i).z = 0;
  	}
  	
  	//Search through the faces
  	for(int i=0; i<faces.size(); i++){
  		if(visible_faces == null || visible_faces.contains(i)){
	  		polygon = faces.get(i).getPolygon(transformed_vertices);
	  		
	  		if(point.isWithin(polygon)){
		  	  for(int j=0; j<faces.get(i).v.length; j++){
		  	  	vertex_set.add(faces.get(i).v[j]);
		  	  }
	  		}
  		}
  	}
  	
  	selection_vertices.addAll(vertex_set);
  	
  	return selection_vertices;
  }

	/**
   * Get the mesh depth map under the given view.
   * @param RT a rigid transformation on the mesh (capturing a viewing direction)
   * @param w the width of the viewport image
   * @param h the height of the viewport image
   * @return the face/depth map under this view
   */
  public Pair<int[],double[]> getDepthMap(double[][] RT, int w, int h)
  {
  	Vector<Point> transformed_vertices;
  	Face face;
  	Point p0, p1, p2;
  	int[] img = null;
  	double[] zbuf = null;
  	ImageViewer debug_viewer = null; //new ImageViewer();
  	
  	//Get view transformed vertices
  	transformed_vertices = Point.transform(RT, vertices);
  	transformed_vertices = Point.transform(transformed_vertices, 1, -1, 1, w/2, h/2, 0);
  	
  	//Initialize image and Z-buffer
  	img = ImageUtility.getNewARGBImage(w, h, -1);
  	
  	if(false){
  		zbuf = MatrixUtility.vector(w*h, -Double.MAX_VALUE);
  	}else{
  		double minz = Double.MAX_VALUE;
  		
  		for(int i=0; i<transformed_vertices.size(); i++){
  			if(transformed_vertices.get(i).z < minz){
  				minz = transformed_vertices.get(i).z;
  			}
  		}
  		  		
  		zbuf = MatrixUtility.vector(w*h, minz-1);
  	}
  	
  	//Draw faces to a temporary image using a depth buffer test
  	for(int f=0; f<faces.size(); f++){
  		face = faces.get(f);
  		
  		if(face.v.length == 3){
  			p0 = transformed_vertices.get(face.v[0]);
  			p1 = transformed_vertices.get(face.v[1]);
  			p2 = transformed_vertices.get(face.v[2]);
  				
  			//ImageUtils.drawTriangle(img, w, h, new Pixel(p0.x, p0.y), new Pixel(p1.x, p1.y), new Pixel(p2.x, p2.y), 0x0000ff);
  			Point.drawTriangle(img, zbuf, w, h, p0, p1, p2, f);
  			
  			/*
  			Point.drawPoint(img, w, h, p0, 0x00ff0000);
  			Point.drawPoint(img, w, h, p1, 0x00ff0000);
  			Point.drawPoint(img, w, h, p2, 0x00ff0000);
  			*/
  		}else if(face.v.length > 3){	//Assume polygons are simple
  			for(int i=1; i<face.v.length-1; i++){
  				p0 = transformed_vertices.get(face.v[0]);
  				p1 = transformed_vertices.get(face.v[i]);
  				p2 = transformed_vertices.get(face.v[i+1]);
  				
  				Point.drawTriangle(img, zbuf, w, h, p0, p1, p2, f);
  			}
  		}
  	}
  	
  	if(debug_viewer != null){
  		debug_viewer.add(img, w, h, true);
  		debug_viewer.add(zbuf, w, h, true);
  	}
  	
  	return new Pair<int[],double[]>(img,zbuf);
  }

	/**
   * Get a list of faces that are visible given a viewing direction.
   * @param RT a rigid transformation on the mesh (capturing a viewing direction)
   * @param w the width of the viewport image
   * @param h the height of the viewport image
   * @return a set of visible face indices
   */
  public TreeSet<Integer> getVisibleFaces(double[][] RT, int w, int h)
  {
  	TreeSet<Integer> visible_faces = new TreeSet<Integer>();
  	Pair<int[],double[]> pair;
  	int[] img;

  	pair = getDepthMap(RT, w, h);
  	img = pair.first;
  	
  	//Record visible faces
  	for(int i=0; i<img.length; i++){
  		if(img[i] >= 0){
  			visible_faces.add(img[i]);
  		}
  	}
  		
  	return visible_faces;
  }
  
	/**
   * Retrieve the faces incident to the given set of vertices.
   * @param selected_vertices the indices of the selected vertices
   * @return the indices of incident faces to the selected vertices
   */
  public Vector<Integer> getIncidentFaces(Vector<Integer> selected_vertices)
  {
  	Vector<Integer> incident_faces = new Vector<Integer>();
  	TreeSet<Integer> vertex_set = new TreeSet<Integer>();
  	TreeSet<Integer> face_set = new TreeSet<Integer>();
  	
  	vertex_set.addAll(selected_vertices);
  	
  	for(int i=0; i<faces.size(); i++){
  		for(int j=0; j<faces.get(i).v.length; j++){
  			if(vertex_set.contains(faces.get(i).v[j])){
	  			face_set.add(i);
	  			break;
  			}
  		}
  	}
  	
    incident_faces.addAll(face_set);
  	
  	return incident_faces;
  }

	/**
   * Retrieve the faces enclosed by the given set of vertices.
   * @param selected_vertices the indices of the selected vertices
   * @return the indices of faces enclosed by the selected vertices
   */
  public Vector<Integer> getEnclosedFaces(Vector<Integer> selected_vertices)
  {
  	Vector<Integer> enclosed_faces = new Vector<Integer>();
  	TreeSet<Integer> vertex_set = new TreeSet<Integer>();
  	TreeSet<Integer> face_set = new TreeSet<Integer>();
  	boolean FOUND;
  	
  	vertex_set.addAll(selected_vertices);
  	
  	for(int i=0; i<faces.size(); i++){
  		FOUND = false;
  		
  		for(int j=0; j<faces.get(i).v.length; j++){
  			if(!vertex_set.contains(faces.get(i).v[j])){
  				FOUND = true;
  				break;
  			}
  		}
  	
  		if(!FOUND) face_set.add(i);
  	}
  	
    enclosed_faces.addAll(face_set);
  	
  	return enclosed_faces;
  }
  
  /**
   * Group vertices into connected components.
   * @return the indices of vertices within the connected components
   */
  public Vector<Vector<Integer>> getConnectedComponents()
  {
  	if(vertex_neighboring_vertices==null || vertex_neighboring_vertices.isEmpty()) return null;
  	
  	Vector<Vector<Integer>> components = new Vector<Vector<Integer>>();
  	Vector<Integer> component;
  	Vector<Boolean> visited = new Vector<Boolean>();
  	Stack<Integer> stack = new Stack<Integer>();
  	int at, next;
  	  	
  	//Initialize visited flags
  	for(int i=0; i<vertices.size(); i++){
  		visited.add(false);
  	}
  	
  	//Traverse connected components
  	for(int i=0; i<vertices.size(); i++){
  		if(!visited.get(i)){
  			component = new Vector<Integer>();
  			visited.set(i, true);
  			component.add(i);
  			stack.push(i);
  			
  			//Traverse neighbors
  			while(!stack.isEmpty()){
  				at = stack.pop();
  				
  				for(int j=0; j<vertex_neighboring_vertices.get(at).size(); j++){
  					next = vertex_neighboring_vertices.get(at).get(j);
  					
  					if(!visited.get(next)){
  						visited.set(next, true);
  						component.add(next);
  						stack.add(next);
  					}
  				}
  			}
  			
  			if(!component.isEmpty()){
  				components.add(component);
  			}
  		}
  	}
  	
  	return components;
  }
  
  /**
   * Return the connected mesh component containing the given vertices.
   * @param component_vertices vertices within the desired component
   * @return the indices of all vertices within the connected component containing the given vertices
   */
  public Vector<Integer> getConnectedComponent(Vector<Integer> component_vertices)
  {
  	if(vertex_neighboring_vertices==null || vertex_neighboring_vertices.isEmpty()) return null;
  	
  	Vector<Integer> component = new Vector<Integer>();
  	Vector<Boolean> visited = new Vector<Boolean>();
  	Stack<Integer> stack = new Stack<Integer>();
  	int at, next;
  	
  	component.addAll(component_vertices);
  	
  	//Initialize visited flags
  	for(int i=0; i<vertices.size(); i++){
  		visited.add(false);
  	}
  	
  	for(int i=0; i<component_vertices.size(); i++){
  		visited.set(component_vertices.get(i), true);
  		stack.add(component_vertices.get(i));
  	}
  	
		//Traverse neighbors of connected component
		while(!stack.isEmpty()){
			at = stack.pop();
			
			for(int j=0; j<vertex_neighboring_vertices.get(at).size(); j++){
				next = vertex_neighboring_vertices.get(at).get(j);
				
				if(!visited.get(next)){
					visited.set(next, true);
					component.add(next);
					stack.add(next);
				}
			}
  	}
  	
  	return component;
  }
  
  /**
   * Use the given set of vertices to cut a mesh.
   * @param cut_vertices the vertices used to split the mesh
   * @return the indices of vertices within the seperate connected components
   */
  public Vector<Vector<Integer>> getCutComponents(Vector<Integer> cut_vertices)
  {
  	if(vertex_neighboring_vertices==null || vertex_neighboring_vertices.isEmpty()) return null;
  	
  	Vector<Vector<Integer>> components = new Vector<Vector<Integer>>();
  	Vector<Integer> component;
  	Vector<Boolean> visited = new Vector<Boolean>();
  	Stack<Integer> stack = new Stack<Integer>();
  	TreeSet<Integer> cut_set  = new TreeSet<Integer>();
  	TreeSet<Integer> tmp_set = new TreeSet<Integer>();
  	Vector<Integer> cut_neighbors = new Vector<Integer>();
  	int index, at, next;
  	  	
  	cut_set.addAll(cut_vertices);
  	
  	//Find cut vertices neighbors
  	for(int i=0; i<cut_vertices.size(); i++){
  		index = cut_vertices.get(i);
  		
			for(int j=0; j<vertex_neighboring_vertices.get(index).size(); j++){
				next = vertex_neighboring_vertices.get(index).get(j);
				if(!cut_set.contains(next)) tmp_set.add(next);
			}
  	}
  	
  	cut_neighbors.addAll(tmp_set);
  	
  	//Initialize visited flags
  	for(int i=0; i<vertices.size(); i++){
  		visited.add(false);
  	}
  	
  	for(int i=0; i<cut_vertices.size(); i++){
  		visited.set(cut_vertices.get(i), true);
  	}
  	
  	//Traverse connected components on each side of the cut
  	for(int i=0; i<cut_neighbors.size(); i++){
  		index = cut_neighbors.get(i);
  		
  		if(!visited.get(index)){
  			component = new Vector<Integer>();
  			visited.set(index, true);
  			component.add(index);
  			stack.push(index);
  			
  			//Traverse neighbors
  			while(!stack.isEmpty()){
  				at = stack.pop();
  				
  				for(int j=0; j<vertex_neighboring_vertices.get(at).size(); j++){
  					next = vertex_neighboring_vertices.get(at).get(j);
  					
  					if(!visited.get(next)){
  						visited.set(next, true);
  						component.add(next);
  						stack.add(next);
  					}
  				}
  			}
  			
  			if(!component.isEmpty()){
  				components.add(component);
  			}
  		}
  	}
  	
  	return components;
  }
  
  /**
   * Get the index of the selected component.
   * @param component_faces the indices of the component faces
   * @param RT a rigid transformation to first apply to the model
   * @param x the x-coordinate of the selection point
   * @param y the y-coordinate of the selection point
   * @return the index of the selected component
   */
  public int getSelectionComponent(Vector<Vector<Integer>> component_faces, double[][] RT, double x, double y)
  {
  	Vector<Point> transformed_vertices = Point.transform(RT, vertices);
  	Point point = new Point(x, y, 0);
  	Face face;
  	int index = -1;
  	
  	for(int i=0; i<component_faces.size(); i++){
  		for(int j=0; j<component_faces.get(i).size(); j++){
  			face = faces.get(component_faces.get(i).get(j));
  			
  			if(point.isWithin(face.getPolygon(transformed_vertices))){
  				index = i;
  				break;
  			}
  		}
  	}
  	
  	return index;
  }

	/**
   * Get the center of mass of the given vertices.
   * @param selected_vertices a list of vertex indices
   * @return the center of mass of the given vertices
   */
  public Point getCentroid(Vector<Integer> selected_vertices)
  {
  	Point centroid = new Point();
  	
  	if(!selected_vertices.isEmpty()){
	  	for(int i=0; i<selected_vertices.size(); i++){
	  		centroid.plusEquals(vertices.get(selected_vertices.get(i)));
	  	}
	  	
	  	centroid.divideEquals(selected_vertices.size());
  	}
  	
  	return centroid;
  }
  
  /**
   * Delete a group of vertices (along with any associated faces).
   * @param component the indices of the vertices to delete
   */
  public void deleteVertices(Vector<Integer> component)
  {
  	Vector<Boolean> valid_vertices = new Vector<Boolean>();
  	Vector<Integer> vmap = new Vector<Integer>();
  	Vector<Point> vertices_new = new Vector<Point>();
  	Vector<Face> faces_new = new Vector<Face>();
  	int index, count;
  	boolean FOUND;
  	
  	//Create valid vertex map
  	for(int i=0; i<vertices.size(); i++){
  		valid_vertices.add(true);
  	}
  	
  	for(int i=0; i<component.size(); i++){
  		index = component.get(i);
  		valid_vertices.set(index, false);
  	}
  	
  	count = 0;
  	
  	for(int i=0; i<valid_vertices.size(); i++){
  		if(valid_vertices.get(i)){
  			vmap.add(count++);
  		}else{
  			vmap.add(-1);
  		}
  	}
  	
  	//Delete vertices
  	for(int i=0; i<vertices.size(); i++){
  		if(valid_vertices.get(i)){
  			vertices_new.add(vertices.get(i));
  		}
  	}
  	
  	vertices = vertices_new;
  	
  	//Delete faces that use these vertices
  	for(int i=0; i<faces.size(); i++){
  		FOUND = false;
  		
  		for(int j=0; j<faces.get(i).v.length; j++){
  			if(!valid_vertices.get(faces.get(i).v[j])){
  				FOUND = true;
  				break;
  			}
  		}
  		
  		if(!FOUND){
  			faces_new.add(faces.get(i));
  		}
  	}
  	
  	faces = faces_new;
  	
  	//Adjust face vertex indices
  	for(int i=0; i<faces.size(); i++){
  		for(int j=0; j<faces.get(i).v.length; j++){
  			faces.get(i).v[j] = vmap.get(faces.get(i).v[j]);
  		}
  	}
  }
  
  /**
   * Transform a group of vertices.
   * @param component	the indices of the vertices to transform
   * @param RT a rigid transformation to first apply to all vertices
   * @param origin the assumed origin to use for all rotations
   * @param rx the rotation in the x-axis
   * @param ry the rotation in the y-axis
   * @param rz the rotation in the z-axis
   * @param sx the scale factor in the x-axis
   * @param sy the scale factor in the y-axis
   * @param sz the scale factor in the z-axis
   * @param smoothness smooth the transformation at the joint/origin [0=none, 1=near joint only, 2=everything]
   */
  public void transformVertices(Vector<Integer> component, double[][] RT, Point origin, double rx, double ry, double rz, double sx, double sy, double sz, int smoothness)
  {
  	double[][] RTi = JAMAMatrixUtility.inverse(RT);
  	double[][] R = MatrixUtility.rotateXYZ(rx, ry, rz);
  	double[][] S = MatrixUtility.eye(4); S[0][0] = sx; S[1][1] = sy; S[2][2] = sz;
  	double[][] RS = MatrixUtility.mtimes(R, S);
  	Vector<Point> transformed_points = new Vector<Point>();
  	Point point;
  	int index;
  	double distance, alpha;
  	double mind = Double.MAX_VALUE;
  	double maxd = -Double.MAX_VALUE;
  	
  	origin = Point.transform(RT, origin);
  	
  	//Transform component points and center at origin
  	for(int i=0; i<component.size(); i++){
  		index = component.get(i);
  		point = vertices.get(index);
  		point = Point.transform(RT, point);
  		point = point.minus(origin);
  		transformed_points.add(point);
  		
  		//Record closest/furthest point distances
  		distance = point.magnitude();
  		if(distance < mind) mind = distance;
  		if(distance > maxd) maxd = distance;
  	}
  		
  	//Apply second transformation about the origin and return to original coordinate space
  	for(int i=0; i<component.size(); i++){
  		index = component.get(i);
  		point = transformed_points.get(i);
  		distance = point.magnitude();
  		
  		point = Point.transform(RS, point);
  		point = point.plus(origin);
  		point = Point.transform(RTi, point);
  		
  		if(smoothness > 0){
  			distance = (distance - mind) / (maxd - mind);		//Set to be between 0 and 1
  			alpha = Math.exp(-distance*distance);
  			if(smoothness==1 && distance>0.2) alpha = 0;
  			
    		point.timesEquals(1-alpha);
    		point.plusEquals(vertices.get(index).times(alpha));
  		}
  		
  		vertices.set(index, point);
  	}
  }
  
  /**
   * Smooth the given vertex positions so they are more like their neighbors.
   * @param selected_vertices the vertices to smooth relative to their neighbors
   */
  public void smoothVertices(Vector<Integer> selected_vertices)
  {
  	Vector<Point> smoothed_vertices = new Vector<Point>();
  	Point point;
  	int index, next;
  	
  	//Get smoothed vertex positions
  	for(int i=0; i<selected_vertices.size(); i++){
  		index = selected_vertices.get(i);
  		point = new Point();
  		
  		for(int j=0; j<vertex_neighboring_vertices.get(index).size(); j++){
  			next = vertex_neighboring_vertices.get(index).get(j);
  			point.plusEquals(vertices.get(next));
  		}
  		
  		point.divideEquals(vertex_neighboring_vertices.get(index).size());
  		smoothed_vertices.add(point);
  	}
  	
  	//Replace old vertex positions with smoothed vertex positions
  	for(int i=0; i<selected_vertices.size(); i++){
  		index = selected_vertices.get(i);
  		vertices.set(index, smoothed_vertices.get(i));
  	}
  }
  
  /**
   * Remove duplicate vertices.
   */
  public void removeDuplicateVertices()
  {
    Map<Point,Integer> set = new TreeMap<Point,Integer>();
    Map<Integer,Integer> map = new TreeMap<Integer,Integer>();
    Vector<Point> vertices_new = new Vector<Point>();
    
    for(int i=0; i<vertices.size(); i++){
      if(set.containsKey(vertices.get(i))){
        map.put(i, set.get(vertices.get(i)));
      }else{
        set.put(vertices.get(i), vertices_new.size());
        map.put(i, vertices_new.size());
        vertices_new.add(vertices.get(i));
      }
    }
    
    vertices = vertices_new;
    
    for(int i=0; i<faces.size(); i++){
      for(int j=0; j<faces.get(i).v.length; j++){
        faces.get(i).v[j] = map.get(faces.get(i).v[j]);
      }
    }
  }

	/**
   * Remove faces that have duplicate vertices.
   */
  public void removeErroneousFaces()
  {
    Vector<Face> faces_new = new Vector<Face>();
    Face tmpf;
    
    for(int i=0; i<faces.size(); i++){
      tmpf = faces.get(i);
      
      if(tmpf.v.length < 3 || (tmpf.v[0]!=tmpf.v[1] && tmpf.v[1]!=tmpf.v[2] && tmpf.v[2]!=tmpf.v[0])){
        faces_new.add(tmpf);
      }
    }
    
    faces = faces_new;
  }

	/**
   * Subdivide faces (assumes they are convex).
   */
  public void subdivideFaces()
  {
  	Vector<Face> faces_new = new Vector<Face>();
  	Face face;
  
  	for(int i=0; i<faces.size(); i++){
  		face = faces.get(i);
  		
  		for(int j=0; j<face.v.length; j++){
  			faces_new.add(new Face(face.v[j], face.v[(j+1)%face.v.length], vertices.size()));
  		}
  		
  		vertices.add(face.center);
  	}
  	
  	faces = faces_new;
  	
  	initialize();
  }
  
	/**
   * Subdivide faces along edges.
   */
  public void subdivideEdges()
  {
  	Vector<Face> faces_new = new Vector<Face>();
  	Face face;
  	Point v0, v1, v2;
  
  	for(int i=0; i<faces.size(); i++){
  		face = faces.get(i);
  		
  		if(face.v.length == 3){		//Only subdivide triangles
	  		faces_new.add(new Face(face.v[0], vertices.size(), vertices.size()+2));
	  		faces_new.add(new Face(face.v[1], vertices.size(), vertices.size()+1));
	  		faces_new.add(new Face(face.v[2], vertices.size()+1, vertices.size()+2));
	  		faces_new.add(new Face(vertices.size(), vertices.size()+1, vertices.size()+2));

	  		v0 = vertices.get(face.v[0]);
  			v1 = vertices.get(face.v[1]);
  			v2 = vertices.get(face.v[2]);
  			
	  		vertices.add(v0.plus(v1).divide(2));
	  		vertices.add(v1.plus(v2).divide(2));
	  		vertices.add(v2.plus(v0).divide(2));
  		}else{
  			faces_new.add(face);
  		}
  	}
  	
  	faces = faces_new;
  	
  	initialize();
  }

	/**
   * Weld vertices that are near each other.
   * @param threshold the distance at which vertices are considered to be the same
   */
  public void weldVertices(double threshold)
  {
  	int n = vertices.size();
  	double[][] D = new double[n][n];
  	double tmpd;
  	
  	for(int i=0; i<n; i++){
  		for(int j=i+1; j<n; j++){
  			tmpd = vertices.get(i).distance(vertices.get(j));
  			D[i][j] = tmpd;
  			D[j][i] = tmpd;
  		}
  	}
  	
  	//TODO!!
  }

	/**
   * Simplify a mesh by removing edges with small dihedral angles.
   */
  public void simplify()
  {
  	Vector<Pair<Double,Integer>> tmpv = new Vector<Pair<Double,Integer>>();
  	double alpha = 0.1;
  	
  	for(int i=0; i<edges.size(); i++){
  		tmpv.add(new Pair<Double,Integer>(edge_dihedral_angles.get(i), i));
  	}
  	
  	Collections.sort(tmpv);
  	
  	for(int i=0; i<alpha*tmpv.size(); i++){
  		//TODO
  	}
  }
}