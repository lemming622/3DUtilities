package edu.ncsa.model.signatures;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.image.*;
import edu.ncsa.utility.*;
import java.util.*;

/**
 * A signature used to describe a 3D mesh based on lightfields [Chen et al., 2003].
 *  @author Kenton McHenry
 */
public class MeshSignature_LightField extends MeshSignature
{
  /**
   * Get the type of mesh signature this is.
   *  @return the type of mesh signature
   */
  public String getType()
  {
  	return "LightField";
  }
  
  /**
   * Clone this signature.
   */
	public MeshSignature_LightField clone()
	{
  	MeshSignature_LightField lfmd = new MeshSignature_LightField();
    lfmd.mesh = mesh;
    lfmd.signature = (Vector<double[]>)Utility.deepCopy(signature);
    
    return lfmd;
	}
	
  /**
   * Construct the signature from the given model.
   *  @param m the 3D model
   */
	public void setSignature(Mesh m)
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
		
		//Set the signature
		front_g = ImageUtility.argb2g(front, w, h);
		side_g = ImageUtility.argb2g(side, w, h);
		top_g = ImageUtility.argb2g(top, w, h);

		front_g = ImageUtility.g2bw(front_g, w, h, 0.5);
		side_g = ImageUtility.g2bw(side_g, w, h, 0.5);
		top_g = ImageUtility.g2bw(top_g, w, h, 0.5);
		
		signature.clear();
		signature.add(front_g);
		signature.add(side_g);
		signature.add(top_g);
		
		//View the lightfields
		if(false){
			ImageViewer viewer = new ImageViewer();
			
			viewer.add(front_g, w, h, false);
			viewer.add(side_g, w, h, false);
			viewer.add(top_g, w, h, false);
		}
	}
}