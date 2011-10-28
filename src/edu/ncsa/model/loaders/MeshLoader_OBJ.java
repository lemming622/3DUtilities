package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import kgm.image.*;
import kgm.utility.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;

/**
 * A mesh file loader for *.obj files.
 *  @author Kenton McHenry
 */
public class MeshLoader_OBJ extends MeshLoader
{
	private int initial_capacity = 100000;
	private boolean ENABLE_MATERIAL_GROUPS = false;
	
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "obj";
	}
	
	 /**
   * Load the material library from the specified file.
   *  @param filename the filename of the material library
   *  @return the material map
   */
  public static TreeMap<String,Material> loadMaterialLibrary(String filename, Mesh mesh)
  {
    TreeMap<String,Material> mtllib = new TreeMap<String,Material>();
  	InputStream is = Utility.getInputStream(filename);
    BufferedReader ins = new BufferedReader(new InputStreamReader(is));
    String line;
    String[] tokens;
    String path = Utility.getFilenamePath(filename);
    String tmp;
    Material material = null;
    ImageViewer viewer = null; //new ImageViewer();
    BufferedImage bufferedimage;
    int[] img;
    int w, h, n, i0;
    
    try{
      while((line=ins.readLine()) != null){
      	if(!line.isEmpty()){
      		tokens = line.trim().split("\\s+");
          
          if(tokens[0].equals("newmtl")){																						//New material
          	material = new Material(tokens[1]);
          	mtllib.put(tokens[1], material);
          }else if(tokens[0].equals("Kd")){																					//Diffuse component
            Color tmpc = new Color();
            
            tmpc.r = Float.valueOf(tokens[1]);
            tmpc.g = Float.valueOf(tokens[2]);
            tmpc.b = Float.valueOf(tokens[3]);
            
            material.diffuse = tmpc;
          }else if(tokens[0].equals("map_Kd") || tokens[0].equals("map_refl")){			//Texture map
          	tmp = tokens[1];
          	i0 = 2;
          	
          	if(tmp.equals("-s")){		//Read in inline parameters
          		tmp = tokens[5];
          		i0 = 6;
          	}
          	
          	for(int i=i0; i<tokens.length; i++) tmp += " " + tokens[i];	//Handle the case where file name have spaces!
          	//System.out.print("\nLoading: " + tmp + " ");
          	
          	//Load the image
  					bufferedimage = ImageIO.read(new File(path + tmp));
            w = bufferedimage.getWidth(null);
            h = bufferedimage.getHeight(null);
            img = new int[w*h];
            bufferedimage.getRGB(0, 0, w, h, img, 0, w);
            
            //Make sure image dimensions are a power of two
          	n = (w > h) ? w : h;
          	n = (int)Math.round(Math.pow(2, Math.ceil(Math.log(n)/Math.log(2))));
            img = ImageUtility.resize(img, w, h, n, n);
            w = n; h = n;
            if(viewer != null) viewer.add(img, w, h, false);
            
            material.tid = mesh.addTexture(Utility.getFilenameName(Utility.unixPath(tmp)), img, w);
          }
      	}
      }
      
      ins.close();
    }catch(Exception e) {e.printStackTrace();}
    
    return mtllib;
  }

	/**
   * Load a wavefront *.obj model.
   *  @param filename the file to load
   *  @return the loaded mesh
   */
  public Mesh load(String filename)
  {
  	Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
  	Vector<Point> vertices = new Vector<Point>(initial_capacity);
  	Vector<UV> uv = new Vector<UV>(initial_capacity);
  	Vector<Point> normals = new Vector<Point>(initial_capacity);
  	Vector<Face> faces = new Vector<Face>(initial_capacity);
  	TreeMap<String,Vector<Integer>> groups = new TreeMap<String,Vector<Integer>>();
  	Vector<Integer> group = null;
  	TreeMap<String,Material> mtllib = null;
  	Material material = null;
  	Point tmpv;
  	Face tmpf;
    
  	BufferedReader ins;
    String[] tokens;
    String[] subtokens;
    String line;
    String tmp;
    int lines_total = 0;
    int lines_read = 0;
    
    try{
    	//Count the number of lines in the file for progress monitoriing
    	if(progressCallBack != null){
       	ins = new BufferedReader(new InputStreamReader(Utility.getInputStream(filename)));
        while((line=ins.readLine()) != null) lines_total++;
    	}
    	
    	//Parse the file
    	ins = new BufferedReader(new InputStreamReader(Utility.getInputStream(filename)));
    	
      while((line=ins.readLine()) != null){
      	//Update progress
      	if(progressCallBack != null){
      		lines_read++;
      		
      		if(lines_read%2==0 || lines_read==lines_total){
      			progressCallBack.progressEvent(lines_read, lines_total);
      		}
      	}
      	
      	//Parse line
      	if(!line.isEmpty()){
      		tokens = line.trim().split("\\s+");
	        
	        if(tokens[0].equals("v")){						//Vertices
	          tmpv = new Point();
	          
	          tmpv.x = Float.valueOf(tokens[1]);
	          tmpv.y = Float.valueOf(tokens[2]);
	          tmpv.z = Float.valueOf(tokens[3]);
	          
	          vertices.add(tmpv);
	        }else if(tokens[0].equals("vt")){			//Texture coordiantes
	          UV tmpuv = new UV();
	          
	          tmpuv.u = Float.valueOf(tokens[1]);
	          tmpuv.v = Float.valueOf(tokens[2]);
	          
	          uv.add(tmpuv);
	        }else if(tokens[0].equals("vn")){			//Normals
	          tmpv = new Point();
	          
	          tmpv.x = Float.valueOf(tokens[1]);
	          tmpv.y = Float.valueOf(tokens[2]);
	          tmpv.z = Float.valueOf(tokens[3]);
	          
	          normals.add(tmpv);
	        }else if(tokens[0].equals("f")){			//Faces
	          tmpf = new Face(tokens.length-1);
	          
	          for(int i=0; i<tmpf.v.length; i++){
	          	subtokens = tokens[i+1].split("/");
	            tmpf.v[i] = Integer.valueOf(subtokens[0])-1;
	            
	            if(subtokens.length>1 && !subtokens[1].isEmpty()){
	            	if(tmpf.uv == null) tmpf.uv = new UV[tokens.length-1];
	            	tmpf.uv[i] = uv.get(Integer.valueOf(subtokens[1])-1);
	            
		            if(subtokens.length>2 && !subtokens[2].isEmpty()){
		            	if(tmpf.vn == null) tmpf.vn = new Point[tokens.length-1];
		            	tmpf.vn[i] = normals.get(Integer.valueOf(subtokens[2])-1);
		            }
	            }
	          }
	          
	          if(material != null){
	          	tmpf.material = material;
	          }
	          
	          faces.add(tmpf);
	          
	          if(group != null){
	          	group.add(faces.size()-1);
	          }
	        }else if(tokens[0].equals("g")){			//Groups of faces
	        	if(tokens.length > 1){
	        		tmp = tokens[1];
	        	}else{
	        		tmp = "";
	        	}
	        	
	        	group = groups.get(tmp);
	        	
	        	if(group == null){
	        		group = new Vector<Integer>(initial_capacity);
	        		groups.put(tmp, group);
	        	}
	        }else if(tokens[0].equals("mtllib")){	//Matierial library
	        	mtllib = loadMaterialLibrary(Utility.getFilenamePath(filename) + tokens[1], mesh);
	        }else if(tokens[0].equals("usemtl")){	//Material
	        	if (mtllib != null) {
		        	material = mtllib.get(tokens[1]);
		        	
		        	if(ENABLE_MATERIAL_GROUPS){		//Create material groups
			        	group = groups.get(material.name);
			        	
			        	if(group == null){
			        		group = new Vector<Integer>(initial_capacity);
			        		groups.put(material.name, group);
			        	}
		        	}
	        	} else {
	        		System.out.println("No material file loaded for material " + tokens[1]);
	        	}
	        }else if(tokens[0].charAt(0) == '#'){
	        }else{
	        	System.out.println("Unrecognized token: " + tokens[0]);
	        }
      	}
      }
      
      ins.close();
    }catch(Exception e){
      e.printStackTrace();
    	return null;      
    }
    
    mesh.setVertices(vertices);
    mesh.setFaces(faces);
    mesh.setGroups(groups);
    mesh.initialize();
    
    return mesh;
  }
  
  /**
   * Save a material library
   * @param filename the name of the material file
   * @param materials a set of materials
   * @param textures the textures associated with the set of materials
   */
  public static void saveMaterialLibrary(String filename, Set<Material> materials, Vector<Texture> textures)
  {
  	String path = Utility.getFilenamePath(filename);
    Material material;

  	try{	
      BufferedWriter outs = new BufferedWriter(new FileWriter(filename));
	    Iterator<Material> itr_mtl = materials.iterator();
	    
	    while(itr_mtl.hasNext()){
	    	material = itr_mtl.next();
	    	
	    	outs.write("newmtl " + material.name);
	    	outs.newLine();
	    	
	    	if(material.diffuse != null){
	    		outs.write("\tKd " + material.diffuse.toString());
	    		outs.newLine();
	    	}
	    	
	    	if(material.tid != -1){
	    		outs.write("\tmap_Kd " + "maps/" + textures.get(material.tid).name + ".jpg");
	    		outs.newLine();
	    	}
	    	
	    	if(itr_mtl.hasNext()) outs.newLine();
	    }
	    
	    outs.close();
	    
	    //Save textures
	    if(!textures.isEmpty()){
		    BufferedImage image;
		    		    
		    if(!Utility.exists(path + "maps/")) new File(path + "maps/").mkdir();
		    
		    for(int i=0; i<textures.size(); i++){
		      image = new BufferedImage(textures.get(i).w, textures.get(i).h, BufferedImage.TYPE_INT_RGB);
		      image.setRGB(0, 0, textures.get(i).w, textures.get(i).h, textures.get(i).argb, 0, textures.get(i).w);
		      ImageIO.write(image, "jpeg", new File(path + "maps/" + textures.get(i).name + ".jpg"));
		    }
	    }
  	}catch(Exception e) {e.printStackTrace();}
  }
  
  /**
   * Save a wavefront *.obj file.
   *  @param filename the file to save to
   *  @param mesh the mesh to save
   *  @return true if saved succesfully
   */
  public boolean save(String filename, Mesh mesh)
  {    
  	String path = Utility.getFilenamePath(filename);
    String name = Utility.getFilenameName(filename);     
  	Vector<Point> vertices = mesh.getVertices();
    Vector<Face> faces = mesh.getFaces();
    TreeMap<String,Vector<Integer>> groups = mesh.getGroups();
    Set<String> group_keys = groups.keySet();
    Iterator<String> itr_str;
    Vector<Integer> values;
    String key;
    boolean[] valid_vertex = new boolean[vertices.size()];
    int[] vertex_map = new int[vertices.size()];
    Set<UV> uvs = new TreeSet<UV>();
    TreeMap<UV,Integer> uv_map = new TreeMap<UV,Integer>();
    Iterator<UV> uv_itr;
    UV uv;    
    Set<Point> normals = new TreeSet<Point>();
    TreeMap<Point,Integer> normal_map = new TreeMap<Point,Integer>();
    Iterator<Point> v_itr;
    Point v;
    Set<Material> materials = new TreeSet<Material>();
    Material material;
    Vector<Texture> textures = mesh.getTextures();
    int index, count;
    Integer tmpi;
    boolean FOUND;
    
    //Build valid vertex map
    for(int i=0; i<faces.size(); i++){
    	if(faces.get(i).VISIBLE){
    		for(int j=0; j<faces.get(i).v.length; j++){
    			valid_vertex[faces.get(i).v[j]] = true;
    		}
    	}
    }
    
    count = 0;
    
    for(int i=0; i<vertices.size(); i++){
    	if(valid_vertex[i]){
    		vertex_map[i] = count;
    		count++;
    	}else{
    		vertex_map[i] = -1;
    	}
    }
        
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
    
    //If there are materials create a subdirectory and save material/textures
    if(!materials.isEmpty()){
    	path += name + "/";
    	if(!Utility.exists(path)) new File(path).mkdir();
    	saveMaterialLibrary(path + name + ".mtl", materials, textures);
    }
    
    try{ 
    	BufferedWriter outs = new BufferedWriter(new FileWriter(path + name + ".obj"));
	    
    	if(!materials.isEmpty()){
    		outs.write("mtllib " + name + ".mtl");
    		outs.newLine();
    		outs.newLine();
    	}
    	
      //Save vertices
      for(int i=0; i<vertices.size(); i++){
      	if(valid_vertex[i]){
	        outs.write("v " + vertices.get(i).x + " " + vertices.get(i).y + " " + vertices.get(i).z);
	        outs.newLine();
      	}
      }
      
      outs.newLine();
      
      //Save texture coordinates
      if(!uvs.isEmpty()){
	      uv_itr = uvs.iterator();
	      
	      while(uv_itr.hasNext()){
	      	uv = uv_itr.next();
	      	
	      	outs.write("vt " + uv.u + " " + uv.v);
	      	outs.newLine();
	      }
	      
	      outs.newLine();
      }
      
      //Save normals
      if(!normals.isEmpty()){
	      v_itr = normals.iterator();
	      
	      while(v_itr.hasNext()){
	      	v = v_itr.next();
	      	
	      	outs.write("vn " + v.x + " " + v.y + " " + v.z);
	      	outs.newLine();
	      }
	      
	      outs.newLine();
      }
      
      //Save faces
      if(groups.isEmpty()){
      	material = null;
      	
	      for(int i=0; i<faces.size(); i++){
	      	if(faces.get(i).material != material){
	      		material = faces.get(i).material;
	      		
	      		outs.newLine();
	      		outs.write("usemtl " + material.name);
	      		outs.newLine();
	      	}
	      	
	        outs.write("f ");
	        
    			for(int j=0; j<faces.get(i).v.length; j++){
    				outs.write("" + (vertex_map[faces.get(i).v[j]]+1));
    				
    				if(faces.get(i).uv!=null && (tmpi=uv_map.get(faces.get(i).uv[j]))!=null){
    					outs.write("/" + (tmpi+1));
    				}else{
    					outs.write("/");
    				}
    				
    				if(faces.get(i).vn!=null && (tmpi=normal_map.get(faces.get(i).vn[j]))!=null){
    					outs.write("/" + (tmpi+1));
    				}else{
    					outs.write("/");
    				}
    				
    				outs.write(" ");
    			}
	        
	        outs.newLine();
	      }
      }else{	//Save faces by groups
      	itr_str = group_keys.iterator();
      	
      	while(itr_str.hasNext()){
      		key = itr_str.next();
      		values = groups.get(key);
      		material = null;
      		FOUND = false;
      		
      		//Check if this group has any visible faces at the moment
      		for(int i=0; i<values.size(); i++){
      			index = values.get(i);
      			
      			if(faces.get(index).VISIBLE){
      				FOUND = true;
      				break;
      			}
      		}
      		
      		if(FOUND){
        		outs.write("g " + key);
        		outs.newLine();
        		
        		for(int i=0; i<values.size(); i++){
        			index = values.get(i);
        			
        			if(faces.get(index).VISIBLE){
      	      	if(faces.get(index).material != material){
      	      		material = faces.get(index).material;
      	      		
      	      		outs.newLine();
      	      		outs.write("usemtl " + material.name);
      	      		outs.newLine();
      	      	}
      	      	
  	      			outs.write("f ");
  	      			
  	      			for(int j=0; j<faces.get(index).v.length; j++){
  	      				outs.write("" + (vertex_map[faces.get(index).v[j]]+1));
  	      				
  	      				if(faces.get(index).uv!=null && (tmpi=uv_map.get(faces.get(index).uv[j]))!=null){
  	      					outs.write("/" + (tmpi+1));
  	      				}else{
  	      					outs.write("/");
  	      				}
  	      				
  	      				if(faces.get(index).vn!=null && (tmpi=normal_map.get(faces.get(index).vn[j]))!=null){
  	      					outs.write("/" + (tmpi+1));
  	      				}else{
  	      					outs.write("/");
  	      				}
  	      				
  	      				outs.write(" ");
  	      			}
  	      			
  	      			outs.newLine();
        			}
        		}
      		}
      		
      		outs.newLine();
      	}
      }
      
      outs.close();
    }catch(Exception e){
    	e.printStackTrace();
    	return false;
    }
    
    return true;
  }
}