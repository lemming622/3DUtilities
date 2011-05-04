package edu.ncsa.model.signatures;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import kgm.image.*;
import kgm.matrix.*;
import kgm.ml.*;
import kgm.utility.*;
import java.util.*;

/**
 * A spin image signature used to describe a 3D mesh [Johnson et al., PAMI 1999].
 *  @author Kenton McHenry
 */
public class MeshSignature_SpinImage extends MeshSignature
{
  private int bins = 11;
  private int bins_half = bins / 2;
  private double scale_alpha;
  private double scale_beta;
  
  public MeshSignature_SpinImage() {}
  
  /**
   * Get the type of mesh signature this is.
   *  @return the type of mesh signature
   */
  public String getType()
  {
  	return "SpinImage";
  }
  
  /**
   * Clone this signature.
   */
  public MeshSignature_SpinImage clone()
  {
  	MeshSignature_SpinImage simd = new MeshSignature_SpinImage();
    simd.mesh = mesh;
    simd.signature = (Vector<double[]>)Utility.deepCopy(signature);
  	simd.bins = bins;
  	simd.bins_half = bins_half;
  	simd.scale_alpha = scale_alpha;
  	simd.scale_beta = scale_beta;
	  	
	  return simd;
  }
  
  /**
   * Construct the signature from the given model.
   *  @param m the 3D model
   */
  public void setSignature(Mesh m)
  {
    mesh = m;
    scale_alpha = ((double)(bins-1)) / (2.0*mesh.getRadius());
    scale_beta = ((double)(bins_half)) / (2.0*mesh.getRadius());

    //Build a spin image for each vertex
    Vector<Point> vertices = mesh.getVertices();
    Vector<Point> vertex_normals = mesh.getVertexNormals();
    Point p, x, n, xp;
    Vector<double[]> spin_images = new Vector<double[]>();
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
          nxp = n.times(xp);
          xpM = xp.magnitude();
          
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
      
      //Normalize
      tmpd = MatrixUtility.norm(simg);
      
      for(int j=0; j<simg.length; j++){
        simg[j] /= tmpd;
      }
      
      spin_images.add(simg);
    }
    
    //Cluster spin images
    Vector<Vector<double[]>> clusters = KMeans.cluster(spin_images, 1, 5);
    signature = KMeans.getCenters(clusters);
    
    if(false){	//View results
	    ImageViewer.show(spin_images, bins, bins, "Spin Images [" + mesh.getMetaData("Name") + "]");
	    ImageViewer.show(signature, bins, bins, "Signature [" + mesh.getMetaData("Name") + "]");
    }
  }
}