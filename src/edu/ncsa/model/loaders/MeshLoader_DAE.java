package edu.ncsa.model.loaders;
import edu.ncsa.model.loaders.dae.*;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.image.*;
import edu.ncsa.utility.*;
import org.jdom.*;
import org.jdom.input.*;
import java.io.*;
import java.util.*;
import java.awt.image.*;
import javax.imageio.*;

/**
 * A mesh loader for *.dae files.
 *  @author Kenton Mchenry
 */
public class MeshLoader_DAE extends MeshLoader
{	
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "dae";
	}

	 /**
   * Load a collada *.dae model.
   *  @param filename the file to load
   *  @return the loaded mesh
   */
	public Mesh load(String filename)
	{
		Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
		Vector<Point> vertices;
		Vector<Face> faces;	
		String path = "";
		int tmpi;
		
		tmpi = filename.lastIndexOf('/');
		if(tmpi != -1)	path = filename.substring(0, tmpi+1);
		
		try{
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(new File(filename));
			Element root = document.getRootElement();
			Element library_images = root.getChild("library_images", DAEUtility.namespace);
			Element library_materials = root.getChild("library_materials", DAEUtility.namespace);
			Element library_effects = root.getChild("library_effects", DAEUtility.namespace);
			Element library_geometries = root.getChild("library_geometries", DAEUtility.namespace);
			Element library_nodes = root.getChild("library_nodes", DAEUtility.namespace);
			Element library_visual_scenes = root.getChild("library_visual_scenes", DAEUtility.namespace);
			
			List<Element> geometries, images;
			TreeMap<String,Vector<Pair<double[][],TreeMap<String,String>>>> modifiers = new TreeMap<String,Vector<Pair<double[][],TreeMap<String,String>>>>();
			TreeMap<String,Integer> tid = new TreeMap<String,Integer>();
			Vector<Pair<double[][],TreeMap<String,String>>> instances;
			double[][] RT;
			Vector<Vector<Face>> mesh_groups;
			Vector<String> materials;
			TreeMap<String,String> textures;
			Iterator<Element> itr;
			Element e, e1, geometry, image;
			Stack<DAENode> stk = new Stack<DAENode>();
			DAENode scene = null;
			DAENode vscene = null;
			DAENode node;
			String id, name, buffer, material, texture;
			ImageViewer viewer = null;
			BufferedImage bufferedimage;
			int[] img;
			int w, h, n;
			
			//new XMLTree(filename);
			
			//Load images
			if(library_images != null){
				images = library_images.getChildren("image", DAEUtility.namespace);
				itr = images.iterator();
				
				while(itr.hasNext()){
					image = itr.next();
					id = image.getAttributeValue("id");
					e = image.getChild("init_from", DAEUtility.namespace);
					buffer = ((Text)e.getContent().get(0)).getText();
					
					//Load the image
					try{
						bufferedimage = ImageIO.read(new File(path + buffer));
	          w = bufferedimage.getWidth(null);
	          h = bufferedimage.getHeight(null);
	          img = new int[w*h];
	          bufferedimage.getRGB(0, 0, w, h, img, 0, w);
	          
	          //Make sure image dimensions are a power of two
	        	n = (w > h) ? w : h;
	        	n = (int)Math.round(Math.pow(2, Math.ceil(Math.log(n)/Math.log(2))));
	          img = ImageUtility.resize(img, w, h, n, n);
	          w = n; h = n;
	          
	          tid.put(id, mesh.addTexture(Utility.getFilenameName(Utility.unixPath(buffer)), img, w));
	          
	          if(false){		//View loaded images
		          if(viewer == null) viewer = new ImageViewer();
		          viewer.add(img, w, h, false);
	          }	          
					}catch(IIOException ex){
						tid.put(id, -1);
					}
				}
			}
			
			//Build geometry modifiers from scene graph
			if(library_nodes != null){
				scene = new DAENode(library_nodes, null);
			}
			
			if(library_visual_scenes != null){
			  vscene = new DAENode(library_visual_scenes, null);
			}
			
			if(vscene != null){	  
				if(scene == null){
			  	scene = vscene;
				}else{
				  scene = DAENode.merge(scene, vscene);
				}
			}
			
			if(scene != null){
				DAENode.setInstances(scene);

				stk.push(scene);
				
				while(!stk.isEmpty()){
		  		node = stk.pop();
		  				  		
		  		if(node.instance_geometry != null && node.INSTANTIATED){
		  			RT = DAENode.getTransformation(node);
		  			textures = null;
		  			
		  			try{
			  			if(node.instance_materials != null){
			  				for(int i=0; i<node.instance_materials.size(); i++){
				  				e = DAEUtility.getElementById(library_materials, node.instance_materials.get(i).second, true);
				  				e = e.getChild("instance_effect", DAEUtility.namespace);
				  				id = e.getAttributeValue("url").substring(1);
				  				e1 = DAEUtility.getElementById(library_effects, id, true);
				  				
				  				//Find texture image
				  				e = DAEUtility.getElement(e1, "surface");
				  				
				  				if(e != null){
					  				e = e.getChild("init_from", DAEUtility.namespace);
					  				material = node.instance_materials.get(i).first;
					  				texture = ((Text)e.getContent().get(0)).getText();
					  				
					  				if(textures == null) textures = new TreeMap<String,String>();
					  				textures.put(material, texture);
				  				}
				  				
				  				//Find diffuse color (TODO: this should be combined with the texture instance and not seperate!)
				  				e = DAEUtility.getElement(e1, "diffuse");
				  				
				  				if(e != null){
					  				e = e.getChild("color", DAEUtility.namespace);
					  				
					  				if(e != null){
						  				material = node.instance_materials.get(i).first;
						  				texture = "#" + ((Text)e.getContent().get(0)).getText();
						  				
						  				if(textures == null) textures = new TreeMap<String,String>();
						  				textures.put(material, texture);
					  				}
				  				}
			  				}
			  			}
		  			}catch(Exception ex) {ex.printStackTrace();}
	  				
			  		instances = modifiers.get(node.instance_geometry);
			  		
			  		if(instances == null){
			  			instances = new Vector<Pair<double[][],TreeMap<String,String>>>();
			  			modifiers.put(node.instance_geometry, instances);
			  		}
			  		
			  		instances.add(new Pair<double[][],TreeMap<String,String>>(RT, textures));
		  		}
		  		
		  		for(int i=0; i<node.children.size(); i++){
		  			stk.push(node.children.get(i));
		  		}		  		
				}
			}
			
			//Load geometry
			geometries = library_geometries.getChildren("geometry", DAEUtility.namespace);
			itr = geometries.iterator();
			
			while(itr.hasNext()){
				geometry = itr.next();
				id = geometry.getAttributeValue("id");
				instances = modifiers.get(id);
				
				e = geometry.getChild("mesh", DAEUtility.namespace); //println(e);
				vertices = DAEUtility.getVertices(e);
				Pair<Vector<Vector<Face>>,Vector<String>> pair = DAEUtility.getFaces(e);
				mesh_groups = pair.first;
				materials = pair.second;
				
				if(instances == null){														//If no instances then create once!
					for(int i=0; i<mesh_groups.size(); i++){
						faces = mesh_groups.get(i);
						mesh.addData((Vector<Point>)Utility.deepCopy(vertices), faces, null, id);
					}
				}else{
					for(int i=0; i<instances.size(); i++){
						RT = instances.get(i).first;
						textures = instances.get(i).second;
	
						name = id;
						if(instances.size() > 1) name += "-" + (i+1);
						
						for(int j=0; j<mesh_groups.size(); j++){
							faces = mesh_groups.get(j);
							texture = textures.get(materials.get(j));

							if(instances.get(i).second != null && texture != null){
								if(texture.charAt(0) == '#'){		//A color
									Scanner sc = new Scanner(texture.substring(1));
									Color color = new Color(Float.valueOf(sc.next()), Float.valueOf(sc.next()), Float.valueOf(sc.next()));
									
									mesh.addData(Point.transform(RT, vertices), faces, new Material(color), name);
								}else{													//An actual texture
									mesh.addData(Point.transform(instances.get(i).first, vertices), faces, new Material(tid.get(texture)), name);
								}
							}else{
								mesh.addData(Point.transform(RT, vertices), faces, null, name);
							}
						}
					}
				}
			}
		}catch(Exception exception) {exception.printStackTrace();}
		
		mesh.initialize();
		
		return mesh;
	}

	public boolean save(String filename, Mesh mesh) {return false;}
}