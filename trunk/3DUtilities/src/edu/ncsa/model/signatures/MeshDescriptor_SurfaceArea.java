package edu.ncsa.model.descriptors;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.utility.*;
import java.util.*;

/**
 * A descriptor used to describe a 3D mesh based on its surface area.
 *  @author Kenton McHenry
 */
public class MeshDescriptor_SurfaceArea extends MeshSignature
{
  /**
   * Get the type of mesh descriptor this is.
   *  @return the type of mesh descriptor
   */
  public String getType()
  {
  	return "SurfaceArea";
  }
  
  /**
   * Clone this descriptor.
   */
	public MeshDescriptor_SurfaceArea clone()
	{
  	MeshDescriptor_SurfaceArea samd = new MeshDescriptor_SurfaceArea();
    samd.mesh = mesh;
    samd.signature = (Vector<double[]>)Utility.deepCopy(signature);
    
    return samd;
	}
	
  /**
   * Construct the descriptor from the given model.
   *  @param m the 3D model
   */
	public void setSignature(Mesh m)
	{
		mesh = m;
		
		Vector<Face> faces = mesh.getFaces();
		Vector<Point> vertices = mesh.getVertices();
		double area = 0;
		double tmpd;
		
		for(int i=0; i<faces.size(); i++){
			tmpd = faces.get(i).getArea(vertices);
			
			if(!Double.isInfinite(tmpd) && !Double.isNaN(tmpd)){
				area += tmpd;
			}
		}
		
		signature.clear();
		signature.add(new double[]{area});
	}
}