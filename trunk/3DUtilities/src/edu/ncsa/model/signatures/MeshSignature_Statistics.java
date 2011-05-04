package edu.ncsa.model.signatures;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import kgm.utility.*;
import java.util.*;

/**
 * A signature used to describe a 3D mesh based on its vertex statistics.
 *  @author Kenton McHenry
 */
public class MeshSignature_Statistics extends MeshSignature
{
  /**
   * Get the type of mesh signature this is.
   *  @return the type of mesh signature
   */
  public String getType()
  {
  	return "Statistics";
  }
  
  /**
   * Clone this signature.
   */
	public MeshSignature_Statistics clone()
	{
  	MeshSignature_Statistics smd = new MeshSignature_Statistics();
    smd.mesh = mesh;
    smd.signature = (Vector<double[]>)Utility.deepCopy(signature);
    
    return smd;
	}
	
  /**
   * Construct the signature from the given model.
   *  @param m the 3D model
   */
	public void setSignature(Mesh m)
	{
		mesh = m;
		
		Vector<Point> vertices = mesh.getVertices();
		double mean_x = 0;
		double mean_y = 0;
		double mean_z = 0;
		double std_x = 0;
		double std_y = 0;
		double std_z = 0;
		double tmpd;
		
		//Compute mean
		for(int i=0; i<vertices.size(); i++){
			mean_x += vertices.get(i).x;
			mean_y += vertices.get(i).y;
			mean_z += vertices.get(i).z;
		}
		
		mean_x /= vertices.size();
		mean_y /= vertices.size();
		mean_z /= vertices.size();
		
		//Compute standard deviation
		for(int i=0; i<vertices.size(); i++){
			tmpd = vertices.get(i).x - mean_x;
			std_x += tmpd*tmpd;
			
			tmpd = vertices.get(i).y - mean_y;
			std_y += tmpd*tmpd;
			
			tmpd = vertices.get(i).z - mean_z;
			std_z += tmpd*tmpd;
		}
		
		std_x /= vertices.size();
		std_y /= vertices.size();
		std_z /= vertices.size();
		
		signature.clear();
		signature.add(new double[]{mean_x, mean_y, mean_z, std_x, std_y, std_z});
	}
}