package edu.ncsa.model.loaders.dae;
import edu.ncsa.matrix.*;
import edu.ncsa.utility.*;
import java.util.*;
import org.jdom.*;

/**
 * A helper class to represent nodes in the collada scene graph.
 */
public class DAENode
{
	public String id = null;
  public DAENode parent = null;		
  public Vector<DAENode> children = new Vector<DAENode>();
  
  double[][] matrix = null;
  public String instance_node = null;
  public String instance_geometry = null;
  public Vector<Pair<String,String>> instance_materials = null;
  public boolean INSTANCES = false;				//Do other nodes declare instances of this node?
  public boolean INSTANTIATED = false;		//Identify this node as one that has been instantiated
  
  public DAENode() {}
  
  /**
   * Class constructor.
   *  @param e the node element
   */
  public DAENode(Element e, DAENode p)
  {
  	List<Element> list = e.getChildren();
  	Iterator<Element> itr1, itr2;
  	Element tmpe;
  	String symbol, target;
  	
  	id = e.getAttributeValue("id");
  	if(id == null) id = e.getAttributeValue("name");
  	if(id == null) id = "";
  	
  	parent = p;
  	
  	if(list != null){
  		itr1 = list.iterator();

	  	while(itr1.hasNext()){
	  	  tmpe = itr1.next();
	  	  
	  	  if(tmpe != null){
		  	  if(tmpe.getName().equals("node")){
		  	  	children.add(new DAENode(tmpe, this));
		  	  }else if(tmpe.getName().equals("matrix")){
		  	  	matrix = DAEUtility.getMatrix(tmpe);
		  	  }else if(tmpe.getName().equals("instance_node")){
		  	  	instance_node = tmpe.getAttributeValue("url").substring(1);
		  	  }else if(tmpe.getName().equals("instance_geometry")){
		  	  	instance_geometry = tmpe.getAttributeValue("url").substring(1);
		  	  	list = DAEUtility.getElements(tmpe, "instance_material");
		  	  	
		  	  	if(list != null){
		  	  		itr2 = list.iterator();
		  	  		instance_materials = new Vector<Pair<String,String>>();
		  	  		
		  	  		while(itr2.hasNext()){
		  	  			tmpe = itr2.next();
		  	  			symbol = tmpe.getAttributeValue("symbol");
		  	  			target = tmpe.getAttributeValue("target").substring(1);
		  	  			instance_materials.add(new Pair<String,String>(symbol,target));
		  	  		}
		  	  	}
		  	  }else{	//Just create a node as a place holder for this element.
		  	  	children.add(new DAENode(tmpe, this));
		  	  }
	  	  }
	  	}		  	
  	}
  }
  
  /**
   * Copy constructor.
   *  @param node the node to copy
   */
  public DAENode(DAENode node)
  {
  	id = node.id;
  	parent = node.parent;
  	children = node.children;
  	
  	if(node.matrix != null) matrix = MatrixUtility.copy(node.matrix);
  	instance_node = node.instance_node;
  	instance_geometry = node.instance_geometry;
  	instance_materials = node.instance_materials;
  }
  
  /**
   * Copy constructor.
   *  @param node the subtree to copy
   *  @param parent the new parent of this node
   */
  public DAENode(DAENode node, DAENode parent)
  {
  	id = node.id;
  	this.parent = parent;
  	
  	if(node.matrix != null) matrix = MatrixUtility.copy(node.matrix);
  	instance_node = node.instance_node;
  	instance_geometry = node.instance_geometry;
  	instance_materials = node.instance_materials;
  	
  	for(int i=0; i<node.children.size(); i++){
  		children.add(new DAENode(node.children.get(i), this));
  	}
  }
  
  /**
   * Merge two trees together by creating a new root and making the given roots children of that root.
   *  @param root1 the root of the first tree
   *  @param root2 the root of the second tree
   *  @return the merged tree
   */
  public static DAENode merge(DAENode root1, DAENode root2)
  {
  	DAENode root = new DAENode();
  	
  	root1.parent = root;
  	root2.parent = root;
  	root.children.add(root1);
  	root.children.add(root2);
  	
  	return root;
  }
  
  /**
   * Build a map of ID's to nodes.
   *  @param node the root of the subtree which to build the ID to node map
   *  @return the ID to node map for this subtree
   */
  public static TreeMap<String,DAENode> getIdToNodeMap(DAENode node)
  {
  	TreeMap<String,DAENode> id2node = new TreeMap<String,DAENode>();
  	Stack<DAENode> stk = new Stack<DAENode>();
  	
  	stk.push(node);
  	
  	while(!stk.empty()){
  		node = stk.pop();
  		
  		if(node.id != null){
  			id2node.put(node.id, node);
  		}
  		
  		for(int i=0; i<node.children.size(); i++){
  			stk.push(node.children.get(i));
  		}
  	}
  	
  	return id2node;
  }
  
  /**
   * Flag this subtree as one that has instances.
   *  @param node the root of the subtree to flag
   */
  public static void setInstancesFlag(DAENode node)
  {
  	Stack<DAENode> stk = new Stack<DAENode>();
  	
  	stk.push(node);
  	
  	while(!stk.isEmpty()){
  		node = stk.pop();
  		node.INSTANCES = true;
  		
  		for(int i=0; i<node.children.size(); i++){
  			stk.push(node.children.get(i));
  		}
  	}
  }
  
  /**
   * Flag this subtree as one that has been instantiated.
   *  @param node the root of the subtree to flag
   */
  public static void setInstantiatedFlag(DAENode node)
  {
  	Stack<DAENode> stk = new Stack<DAENode>();
  	
  	stk.push(node);
  	
  	while(!stk.isEmpty()){
  		node = stk.pop();
  		node.INSTANTIATED = true;
  		
  		for(int i=0; i<node.children.size(); i++){
  			stk.push(node.children.get(i));
  		}
  	}
  }
  
  /**
   * Modify the given subtree to contain the path to the root.
   *  @param node the subtree
   *  @return the modified subtree with the path to the root included
   */
  public static DAENode addPathToRoot(DAENode node)
  {
  	DAENode tmp;
  	
  	while(node.parent != null){
  		tmp = new DAENode(node.parent);
  		tmp.instance_node = null;
  		tmp.instance_geometry = null;
  		tmp.instance_materials = null;
  		tmp.children.clear();
  		tmp.children.add(node);
  		node.parent = tmp;
  		node = tmp;
  	}
  	
  	return node;
  }
  
  /**
   * Set the instances within the scene graph by copying the referenced subtrees locally.
   *  @param root the root of the subtree to instantiate instances from
   */
  public static void setInstances(DAENode root)
  {
  	TreeMap<String,DAENode> id2node = getIdToNodeMap(root);
  	Stack<DAENode> stk = new Stack<DAENode>();
  	DAENode node, instantiated_node;
  	
  	//Flag all nodes that are instanced by other nodes
  	stk.push(root);
  	
  	while(!stk.isEmpty()){
  		node = stk.pop();
  		
  		if(node.instance_node != null){
  			DAENode.setInstancesFlag(id2node.get(node.instance_node));
  		}
  		
  		for(int i=0; i<node.children.size(); i++){
  			stk.push(node.children.get(i));
  		}
  	}
  	
  	//Expand instantiations
  	stk.push(root);
  	
  	while(!stk.isEmpty()){
  		node = stk.pop();
  		if(!node.INSTANCES) node.INSTANTIATED = true;																//If no instances create one!	
  		
  		if(node.instance_node != null && node.INSTANTIATED){
  			instantiated_node = new DAENode(id2node.get(node.instance_node), node);
  			//instantiated_node = addPathToRoot(instantiated_node);
  			DAENode.setInstantiatedFlag(instantiated_node);
  			node.children.add(instantiated_node);
  			node.instance_node = null;
  		}
  		
  		for(int i=0; i<node.children.size(); i++){
  			stk.push(node.children.get(i));
  		}
  	}
  }
  
  /**
   * Get the transformation matrix for this node by walking up to the parent and applying all found transformations.
   *  @param node the node whose transformation we are setting
   *  @return the accumulated transformation for this node
   */
  public static double[][] getTransformation(DAENode node)
  {
  	double[][] RT = MatrixUtility.eye(4);

  	while(node != null){
  		if(node.matrix != null){
  			RT = GMatrixUtility.transform(node.matrix, RT);
  		}
  		
  		node = node.parent;
  	}
  		  	
  	return RT;
  }
}