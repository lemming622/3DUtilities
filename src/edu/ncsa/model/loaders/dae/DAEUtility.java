package edu.ncsa.model.loaders.dae;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.model.Utility.*;
import java.util.*;
import org.jdom.*;

/**
 * A class containing methods which are useful for loading data from *.dae files.
 *  @author Kenton McHenry
 */
public class DAEUtility
{
	public static Namespace namespace = Namespace.getNamespace("http://www.collada.org/2005/11/COLLADASchema");

	/**
	 * Print information about the given element.
	 *  @param e the element in question
	 */
	public static void println(Element e)
	{
		Iterator<Element> itr = e.getChildren().iterator();
		
		System.out.print(e.getName() + ": " );
		
		while(itr.hasNext()){
			System.out.print(itr.next().getName() + " ");
		}
		
		System.out.println();
	}
	
	/**
	 * Search a DOM subtree for an element with the given id.
	 *  @param root the root of the subtree to search
	 *  @param id the id of the desired element
	 *  @param RECURSE recurse into subtrees
	 *  @return the target element
	 */
	public static Element getElementById(Element root, String id, boolean RECURSE)
	{
		Queue<Element> q = new LinkedList<Element>(root.getChildren());
		String value;
		Element e;
  	
		while(!q.isEmpty()){
			e = q.remove();
			value = e.getAttributeValue("id");
			
		  if(value != null && value.equals(id)){
		  	return e;
		  }else if(RECURSE){
		  	q.addAll(e.getChildren());
		  }
		}
		
		return null;
	}
	
	/**
	 * Search a DOM subtree for elements with the given name.
	 *  @param root the root of the subtree to search
	 *  @param name the name of the desired elements
	 *  @return the target elements
	 */
	public static List<Element> getElements(Element root, String name)
	{
		List<Element> list = new LinkedList<Element>();
		Queue<Element> q = new LinkedList<Element>(root.getChildren());
		Element e;
  	
		while(!q.isEmpty()){
			e = q.remove();
			q.addAll(e.getChildren());
			
		  if(e.getName().equals(name)) list.add(e);
		}
		
		return list;
	}
	
	/**
	 * Search a DOM subtree for an element with the given name.
	 *  @param root the root of the subtree to search
	 *  @param name the name the desired element
	 *  @return the target element
	 */
	public static Element getElement(Element root, String name)
	{
		Queue<Element> q = new LinkedList<Element>(root.getChildren());
		Element e;
  	
		while(!q.isEmpty()){
			e = q.remove();
			
		  if(e.getName().equals(name)){
		  	return e;
		  }else{
		  	q.addAll(e.getChildren());
		  }
		}
		
		return null;
	}
	
	/**
	 * Get the contents of a matrix element.
	 *  @param e the matrix element
	 *  @return the data
	 */
	public static double[][] getMatrix(Element e)
	{
		if(e.getName().equals("matrix")){
			double[][] mat = new double[4][4];
			String buffer = ((Text)e.getContent().get(0)).getText();
			Scanner sc = new Scanner(buffer);
			
			for(int j=0; j<4; j++){			
				for(int i=0; i<4; i++){
					mat[j][i] = sc.nextDouble();
				}
			}
			
			return mat;
		}else{
			return null;
		}
	}
	
	/**
	 * Get the contents of a float array element.
	 *  @param e the float array element
	 *  @return the float array data
	 */
	public static float[] getFloatArray(Element e)
	{
		if(e.getName().equals("float_array")){
			int n = Integer.valueOf(e.getAttributeValue("count"));
			float[] arr = new float[n];
			String buffer = ((Text)e.getContent().get(0)).getText();
			Scanner sc = new Scanner(buffer);
			int i = 0;
						
			while(sc.hasNextFloat()){
				arr[i] = Float.valueOf(sc.nextFloat());
				i++;
			}
						
			return arr;
		}else{
			return null;
		}
	}
	
	/**
	 * Get the contents of a p element.
	 *  @param e the p element
	 *  @return the data
	 */
	public static int[] getP(Element e)
	{
		if(e.getName().equals("p")){
			int[] arr;
			Vector<Integer> vec = new Vector<Integer>();
			String buffer = ((Text)e.getContent().get(0)).getText();
			Scanner sc = new Scanner(buffer);
			
			while(sc.hasNextInt()){
				vec.add(Integer.valueOf(sc.nextInt()));
			}
			
			arr = new int[vec.size()];
			for(int i=0; i<vec.size(); i++) arr[i] = vec.get(i);
			
			return arr;
		}else{
			return null;
		}
	}
	
	/**
	 * Get the verices contained within a mesh element.
	 *  @param mesh the mesh element
	 *  @return the vertices
	 */
	public static Vector<Point> getVertices(Element mesh)
	{
		Vector<Point> vertices = new Vector<Point>();
		float[] arr;		
		Element e;
		String id;
		
		e = mesh.getChild("vertices", namespace);
		e = e.getChild("input", namespace);
		id = e.getAttributeValue("source").substring(1);
		e = getElementById(mesh, id, false);
		e = e.getChild("float_array", namespace);
		arr = getFloatArray(e);
		
		for(int i=0; i<arr.length; i+=3){
			vertices.add(new Point(arr[i], arr[i+1], arr[i+2]));
		}
		
		return vertices;
	}
	
	/**
	 * Get the texture coordinates contained within the given mesh source element.
	 *  @param mesh the mesh element
	 *  @param id the id of the source element containing the texture coordinates within the mesh
	 *  @return the texture coordinates
	 */
	public static Vector<UV> getUV(Element mesh, String id)
	{
		Vector<UV> uv = new Vector<UV>();
		float[] arr;		
		Element e;
		
		e = getElementById(mesh, id, false);
		e = e.getChild("float_array", namespace);
		arr = getFloatArray(e);
		
		for(int i=0; i<arr.length; i+=2){
			uv.add(new UV(arr[i], arr[i+1]));
		}
		
		return uv;
	}
	
	/**
	 * Get the faces contained within a mesh element (Note: currently assumes they are represented as triangles!).
	 *  @param mesh the mesh element
	 *  @return the groups of faces and the materials they use
	 */
	public static Pair<Vector<Vector<Face>>,Vector<String>> getFaces(Element mesh)
	{
		Vector<Vector<Face>> groups = new Vector<Vector<Face>>();
		Vector<Face> faces;
		Vector<String> materials = new Vector<String>();
		int[] arr;
		List<Element> list;
		Iterator<Element> itr1, itr2;
		Element triangles, e;
		String material;
		String uv_id = "";
		int offv, offn, offt, offset, offset2, offset3;
		
		list = mesh.getChildren("triangles", namespace);
		
		if(list != null){
			itr1 = list.iterator();
			
			while(itr1.hasNext()){
				triangles = itr1.next();
				material = triangles.getAttributeValue("material");
				
				//Determine what information is referenced by each vertex
				offv = -1; offn = -1; offt = -1; offset = 0;
				itr2 = triangles.getChildren("input", namespace).iterator();

				while(itr2.hasNext()){
					e = itr2.next();
					
					if(e.getAttributeValue("semantic").equals("VERTEX")){
						offv = Integer.valueOf(e.getAttributeValue("offset"));
						offset++;
					}
					
					if(e.getAttributeValue("semantic").equals("NORMAL")){
						offn = Integer.valueOf(e.getAttributeValue("offset"));
						offset++;
					}
					
					if(e.getAttributeValue("semantic").equals("TEXCOORD")){
						offt = Integer.valueOf(e.getAttributeValue("offset"));
						uv_id = e.getAttributeValue("source").substring(1);
						offset++;
					}
				}

				offset2 = offset + offset;
				offset3 = offset2 + offset;
				
				//Read off the triangles and texture coordiantes
				e = triangles.getChild("p", namespace);
				arr = getP(e);
				
				if(offv >= 0){
					faces = new Vector<Face>();
					
					for(int i=offv; i<arr.length; i+=offset3){
						faces.add(new Face(arr[i], arr[i+offset], arr[i+offset2]));
					}
				
					if(offt >= 0){
						Vector<UV> tmpv = getUV(mesh, uv_id);
						Vector<UV[]> uv = new Vector<UV[]>();
	
						for(int i=offt; i<arr.length; i+=offset3){
						  uv.add(new UV[]{tmpv.get(arr[i]), tmpv.get(arr[i+offset]), tmpv.get(arr[i+offset2])});	
						}
											
						if(faces.size() == uv.size()){
							for(int i=0; i<uv.size(); i++){
								faces.get(i).uv = uv.get(i);
							}
						}else{
							System.out.println("Warning: found broken textured faces, " + faces.size() + " -> " + uv.size());
						}						
					}
					
					groups.add(faces);
					materials.add(material);
				}
			}
		}
		
		return new Pair<Vector<Vector<Face>>,Vector<String>>(groups, materials);
	}
}