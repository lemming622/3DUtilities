package edu.ncsa.model.signatures;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.image.*;
import edu.ncsa.matrix.*;
import edu.ncsa.utility.*;
import java.util.*;

/**
 * A spin image signature used to describe a 3D mesh [Johnson et al., PAMI 1999].
 * This version accepts partial matches, however, while still rotation/translation invariant it 
 * is no longer scale invariant.
 * @author Kenton McHenry
 */
public class MeshSignature_SpinImages extends MeshSignature
{
  private int bins = 11;
  private int bins_half = bins / 2;
  private double max_distance = 10;	//The maximum distance between considered points in a spin image
  private double scale_alpha = ((double)(bins-1)) / (2.0*max_distance);
  private double scale_beta = ((double)(bins_half)) / (2.0*max_distance);

  public MeshSignature_SpinImages() {}
  
  /**
   * Get the type of mesh signature this is.
   * @return the type of mesh signature
   */
  public String getType()
  {
  	return "SpinImages";
  }
  
  /**
   * Clone this signature.
   */
  public MeshSignature_SpinImages clone()
  {
  	MeshSignature_SpinImages ms = new MeshSignature_SpinImages();
    ms.mesh = mesh;
    ms.signature = (Vector<double[]>)Utility.deepCopy(signature);
  	ms.bins = bins;
  	ms.bins_half = bins_half;
  	ms.max_distance = max_distance;
  	ms.scale_alpha = scale_alpha;
  	ms.scale_beta = scale_beta;
	  	
	  return ms;
  }
  
  /**
   * Construct the signature from the given model.
   * @param m the 3D model
   */
  public void setSignature(Mesh m)
  {
    mesh = m;

    //Build a spin image for each vertex
    Vector<Point> vertices = mesh.getVertices();
    Vector<Point> vertex_normals = mesh.getVertexNormals();
    Point p, x, n, xp;
    double[] simg;   
    double nxp, xpM, alpha, beta;
    double tmpd;
    int tmpx, tmpy;
    
    for(int i=0; i<vertices.size(); i++){
      simg = new double[bins*bins];
      p = vertices.get(i);
      n = vertex_normals.get(i);
      
      for(int j=0; j<vertices.size(); j++){
        if(i != j){
          x = vertices.get(j);
          xp = x.minus(p);
          xpM = xp.magnitude();

          if(xpM <= max_distance){
	          nxp = n.times(xp);
	          
	          alpha = Math.sqrt(xpM*xpM - nxp*nxp);
	          beta = nxp;
	          
	          tmpx = (int)Math.round(scale_alpha*alpha);
	          tmpy = (int)Math.round(scale_beta*beta) + bins_half;
	          //System.out.println(tmpx + ", " + tmpy);
	          
	          if(true){   //Safety check, SHOULDN'T NEED THIS!
	            if(tmpx < 0) tmpx = 0;
	            if(tmpx >= bins) tmpx = bins-1;
	            if(tmpy < 0) tmpy = 0;
	            if(tmpy >= bins) tmpy = bins-1;
	          }
	          
	          simg[tmpy*bins+tmpx]++;
          }
        }
      }
      
      //Normalize
      tmpd = MatrixUtility.norm(simg);
      
      for(int j=0; j<simg.length; j++){
        simg[j] /= tmpd;
      }
      
      signature.add(simg);
    }
    
	  //ImageViewer.show(signature, bins, bins, "Spin Images [" + mesh.getMetaData("Name") + "]");
  }
  
  /**
	 * Compare the model from which this signature is derived to another signature from another model.
	 * @param ms another mesh signature
	 * @return the difference in models based on the signature (larger values indicate more different models)
	 */
	public double compareTo(MeshSignature ms)
	{
		MeshSignature_SpinImages mssi = (MeshSignature_SpinImages)ms;
	  double mind, tmpd;
	  double da = 0;
	  double db = 0;
	  
	  //Compare all of this mesh's vertices
	  for(int i=0; i<signature.size(); i++){
	  	mind = Double.MAX_VALUE;
	  	
	  	for(int j=0; j<mssi.signature.size(); j++){
	  		tmpd = MatrixUtility.distance(signature.get(i), mssi.signature.get(j));
	  		if(tmpd < mind) mind = tmpd;
	  	}
	  	
	  	da += mind;
	  }
	  
	  //Compare all of the other mesh's vertices
	  for(int i=0; i<mssi.signature.size(); i++){
	  	mind = Double.MAX_VALUE;
	  	
	  	for(int j=0; j<signature.size(); j++){
	  		tmpd = MatrixUtility.distance(mssi.signature.get(i), signature.get(j));
	  		if(tmpd < mind) mind = tmpd;
	  	}
	  	
	  	db += mind;
	  }
	  	  
	  //Return the small value
	  if(da < db){
	  	return da;
	  }else{
	  	return db;
	  }
	}
}