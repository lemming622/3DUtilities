package edu.ncsa.model.signatures;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import kgm.utility.*;
import java.util.*;

/**
 * A signature used to describe a 3D mesh based on its surface area.
 *  @author Kenton McHenry
 */
public class MeshSignature_SurfaceArea extends MeshSignature
{
  /**
   * Get the type of mesh signature this is.
   *  @return the type of mesh signature
   */
  public String getType()
  {
  	return "SurfaceArea";
  }
  
  /**
   * Clone this signature.
   */
	public MeshSignature_SurfaceArea clone()
	{
  	MeshSignature_SurfaceArea samd = new MeshSignature_SurfaceArea();
    samd.mesh = mesh;
    samd.signature = (Vector<double[]>)Utility.deepCopy(signature);
    
    return samd;
	}
	
  /**
   * Construct the signature from the given model.
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