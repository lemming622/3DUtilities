package edu.ncsa.model;
import edu.ncsa.model.MeshAuxiliary.Point;
import edu.ncsa.model.MeshLoader.ProgressEvent;
import edu.ncsa.model.loaders.*;
import java.util.*;

public class AnimatedMesh extends Mesh
{
	private static Vector<AnimatedMeshLoader> animation_loaders = null;
	private AnimatedMeshLoader animation_loader = null;
	private Point animation_offset = new Point(0, 0, 0);
	private double animation_scale = 1;
	
	public AnimatedMesh() 
	{
  	super();
  	
  	if(animation_loaders == null){
	  	animation_loaders = new Vector<AnimatedMeshLoader>();
	  		  	
	  	try{animation_loaders.add(new AnimatedMeshLoader_TEEVE_STREAM());}catch(Throwable t) {}
	  	try{animation_loaders.add(new AnimatedMeshLoader_TEEVE());}catch(Throwable t) {}
	  	
	  	//Identify loadable file types
	  	for(int i=0; i<animation_loaders.size(); i++){
	  	  formats.add(animation_loaders.get(i).type());	
	  	}
  	}		
	}
	
  /**
   * Class constructor.
   * @param animation_loader the animated loader where frame data can be retrieved from
   */
  public AnimatedMesh(AnimatedMeshLoader animation_loader)
  {
  	this();
  	this.animation_loader = animation_loader;
  }
  
  /**
   * Get the meshes animation loader.
   * @return the animated mesh loader
   */
  public AnimatedMeshLoader getAnimationLoader()
  {
  	return animation_loader;
  }
  
	/**
   * Transfer the contents of another mesh to this mesh, allocating new memory for the
   * other mesh to hold it's future data.
   *  @param m the mesh to assign values from
   */
  public void transfer(AnimatedMesh m)
  {
  	super.transfer(m);
  	animation_loader = m.animation_loader; m.animation_loader = null;
    animation_offset = m.animation_offset; m.animation_offset = new Point(0, 0, 0);
    animation_scale = m.animation_scale; m.animation_scale = 1;
  }
  
	/**
   * Load 3D model from a file
   *  @param filename the absolute file name
   *  @param progressCallBack the callback handling progress updates
   *  @return true if successful
   */
  public boolean load(String filename, ProgressEvent progressCallBack)
  {
  	AnimatedMesh mesh = null;
  	
  	if(animation_loaders != null){
    	for(int i=0; i<animation_loaders.size(); i++){
    		if(filename.contains("." + animation_loaders.get(i).type())){
    			animation_loaders.get(i).setProgressCallBack(progressCallBack);
    			mesh = animation_loaders.get(i).load(filename);
    			
    			if(mesh != null){
  	  			transfer(mesh);
  	  			while(vertices.isEmpty()) setMesh();
  	  			//print();
  	  			return true;
    			}
    		}
    	}
  	}
  	
  	return super.load(filename, progressCallBack);
  }
  
	/**
   * Load 3D model from a file
   *  @param filename the absolute file name
   *  @return true if successful
   */
  public boolean load(String filename)
  {
  	return load(filename, null);
  }
  
	/**
   * Set the center of mass of the vertices to the origin.
   * @param new_radius the desired radial extreme of the vertices from the origin
   */
  public void center(float new_radius)
  {
  	if(animation_loader != null){
	  	animation_offset = center;
	    animation_scale = new_radius/radius;
  	}else{
  		super.center(new_radius);
  	}
  }
  
  /**
   * Get the animation length.
   * @return
   */
  public double length()
  {
  	if(animation_loader != null){
  		return animation_loader.length();
  	}else{
  		return 0;
  	}
  }
  
  /**
   * Set the mesh to the next mesh from the animation loader.
   */
  public void setMesh()
  {
  	if(animation_loader != null){
  		super.transfer(animation_loader.getMesh());
			vertices = (Point.transform(vertices, animation_offset, animation_scale, null));
  	}
  }
  
  /**
   * Set the mesh to the mesh at time t from the animation loader.
   * @param t the time instance for which to retrieve the mesh
   */
  public void setMesh(double t)
  {
  	if(animation_loader != null){
  		super.transfer(animation_loader.getMesh(t));
  	}
  }
}