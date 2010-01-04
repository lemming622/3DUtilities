package edu.ncsa.model.descriptors;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.image.*;
import edu.ncsa.utility.*;
import java.util.*;

/**
 * A descriptor used to describe a 3D mesh based on lightfields [Chen et al., 2003].
 *  @author Kenton McHenry
 */
public class MeshDescriptor_LightField extends MeshDescriptor
{
  /**
   * Get the type of mesh descriptor this is.
   *  @return the type of mesh descriptor
   */
  public String getType()
  {
  	return "LightField";
  }
  
  /**
   * Clone this descriptor.
   */
	public MeshDescriptor_LightField clone()
	{
  	MeshDescriptor_LightField lfmd = new MeshDescriptor_LightField();
    lfmd.mesh = mesh;
    lfmd.descriptor = (Vector<double[]>)Utility.deepCopy(descriptor);
    
    return lfmd;
	}
	
  /**
   * Construct the descriptor from the given model.
   *  @param m the 3D model
   */
	public void setDescriptor(Mesh m)
	{
		mesh = m;
		
		Vector<Point> vertices = mesh.getVertices();
		Vector<Face> faces = mesh.getFaces();
		Point center = mesh.getCenter();
		double radius = mesh.getRadius();
		Vector<Point> PC = mesh.getPC();
	
		int w = 200;
		int h = w;
		int halfw = (int)Math.floor(w/2.0)-1;
		int[] front = new int[w*h];
		int[] side = new int[w*h];
		int[] top = new int[w*h];
		double[] front_g, side_g, top_g;
		Pixel p0, p1, p2;
		Face face;
		
		//Transform the vertices so that they are aligned with XYZ axis and occupy the center of the target image.
		vertices = Point.transform(vertices, center, radius, PC);
		vertices = Point.transform(vertices, halfw, halfw);
		
		//Orthographically render lightfields from various sides
		for(int f=0; f<faces.size(); f++){
			face = faces.get(f);
			
			//Draw triangles orthographically from front
			if(face.v.length == 3){
				p0 = new Pixel(vertices.get(face.v[0]).x, vertices.get(face.v[0]).y);
				p1 = new Pixel(vertices.get(face.v[1]).x, vertices.get(face.v[1]).y);
				p2 = new Pixel(vertices.get(face.v[2]).x, vertices.get(face.v[2]).y);
				
				ImageUtility.drawTriangle(front, w, h, p0, p1, p2, 0x00ffffff);
			}else{	//Assume polygons are simple
				for(int i=1; i<face.v.length-1; i++){
					p0 = new Pixel(vertices.get(face.v[0]).x, vertices.get(face.v[0]).y);
					p1 = new Pixel(vertices.get(face.v[i]).x, vertices.get(face.v[i]).y);
					p2 = new Pixel(vertices.get(face.v[i+1]).x, vertices.get(face.v[i+1]).y);
					
					ImageUtility.drawTriangle(front, w, h, p0, p1, p2, 0x00ffffff);
				}
			}
			
			//Draw triangles orthographically from side
			if(face.v.length == 3){
				p0 = new Pixel(vertices.get(face.v[0]).z, vertices.get(face.v[0]).y);
				p1 = new Pixel(vertices.get(face.v[1]).z, vertices.get(face.v[1]).y);
				p2 = new Pixel(vertices.get(face.v[2]).z, vertices.get(face.v[2]).y);
				
				ImageUtility.drawTriangle(side, w, h, p0, p1, p2, 0x00ffffff);
			}else{	//Assume polygons are simple
				for(int i=1; i<face.v.length-1; i++){
					p0 = new Pixel(vertices.get(face.v[0]).z, vertices.get(face.v[0]).y);
					p1 = new Pixel(vertices.get(face.v[i]).z, vertices.get(face.v[i]).y);
					p2 = new Pixel(vertices.get(face.v[i+1]).z, vertices.get(face.v[i+1]).y);
					
					ImageUtility.drawTriangle(side, w, h, p0, p1, p2, 0x00ffffff);
				}
			}
			
			//Draw triangles orthographically from top
			if(face.v.length == 3){
				p0 = new Pixel(vertices.get(face.v[0]).x, vertices.get(face.v[0]).z);
				p1 = new Pixel(vertices.get(face.v[1]).x, vertices.get(face.v[1]).z);
				p2 = new Pixel(vertices.get(face.v[2]).x, vertices.get(face.v[2]).z);
				
				ImageUtility.drawTriangle(top, w, h, p0, p1, p2, 0x00ffffff);
			}else{	//Assume polygons are simple
				for(int i=1; i<face.v.length-1; i++){
					p0 = new Pixel(vertices.get(face.v[0]).x, vertices.get(face.v[0]).z);
					p1 = new Pixel(vertices.get(face.v[i]).x, vertices.get(face.v[i]).z);
					p2 = new Pixel(vertices.get(face.v[i+1]).x, vertices.get(face.v[i+1]).z);
					
					ImageUtility.drawTriangle(top, w, h, p0, p1, p2, 0x00ffffff);
				}
			}
		}
		
		//Set the descriptor
		front_g = ImageUtility.argb2g(front, w, h);
		side_g = ImageUtility.argb2g(side, w, h);
		top_g = ImageUtility.argb2g(top, w, h);

		front_g = ImageUtility.g2bw(front_g, w, h, 0.5);
		side_g = ImageUtility.g2bw(side_g, w, h, 0.5);
		top_g = ImageUtility.g2bw(top_g, w, h, 0.5);
		
		descriptor.clear();
		descriptor.add(front_g);
		descriptor.add(side_g);
		descriptor.add(top_g);
		
		//View the lightfields
		if(false){
			ImageViewer viewer = new ImageViewer();
			
			viewer.add(front_g, w, h, false);
			viewer.add(side_g, w, h, false);
			viewer.add(top_g, w, h, false);
		}
	}
}