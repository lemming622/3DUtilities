package edu.ncsa.model;
import edu.ncsa.model.matrix.*;
import java.util.*;

/**
 * Cluster a vector of N-dimensional points into K groups.
 * In this version points are represented as double arrays.
 *  @author Kenton McHenry
 */
public class KMeans
{
  /**
   * Cluster the given points.
   *  @param K the number of clusters
   *  @param X a vector N-dimensional points
   *  @param iterations the number of iterations to re-cluster
   *  @return a vector of vectors containing the points within the groups
   */
  public static Vector<Vector<double[]>> cluster(int K, Vector<double[]> X, int iterations)
  {
    if(X != null && X.size() > 0){
      Vector<double[]> C = new Vector<double[]>();
      Vector<Vector<double[]>> CX = new Vector<Vector<double[]>>();
      int d = X.get(0).length;
      int minc;
      double mind, tmpd;
      
      //Set random initial cluster centers
      Vector<double[]> minmax = MatrixUtility.minmax(X);
      
      for(int i=0; i<K; i++){
        C.add(MatrixUtility.random(d, minmax));
        CX.add(new Vector<double[]>());
      }
      
      //Cluster points
      for(int it=1; it<=iterations; it++){
        //System.out.println("Iteration: " + it);
        
        //Clear previous points
        for(int i=0; i<K; i++){
          CX.get(i).clear();  
        }
        
        //Assign points to nearest cluster
        for(int i=0; i<X.size(); i++){
          minc = 0;
          mind = Double.MAX_VALUE;
          
          for(int j=0; j<C.size(); j++){
            tmpd = MatrixUtility.ssd(X.get(i), C.get(j));
            
            if(tmpd < mind){
              minc = j;
              mind = tmpd;
            }
          }
          
          CX.get(minc).add(X.get(i));
        }
        
        //Calculate new cluster centers
        for(int i=0; i<K; i++){
          C.set(i, MatrixUtility.mean(CX.get(i)));
          
          if(C.get(i) == null){
            C.set(i, MatrixUtility.random(d, minmax));
          }
        }
      }
      
      return CX;
    }
    
    return null;
  }
  
  /**
   * A convenient method to generate other useful data from the groups of points returned by cluster.
   *  @param CX the groups of points returned by cluster
   *  @param C the centers of each cluster
   *  @param E the extremes in each dimension of each cluster
   *  @param N the number of points in each cluster
   */
  public static void getInfo(Vector<Vector<double[]>> CX, Vector<double[]> C, Vector<Vector<double[]>> E, Vector<Integer> N)
  {
    int K = CX.size();
    int d = 0;
    
    //Determine d (in case some clusters have no points!)
    for(int i=0; i<CX.size(); i++){
      if(CX.get(i)!=null && CX.get(i).size()>0){
        if(CX.get(i).get(0).length > d){
          d = CX.get(i).get(0).length;
        }
      }
    }
    
    //Set centers
    if(C != null){
      double[] arr;
      C.clear();
      
      for(int i=0; i<K; i++){
        arr = MatrixUtility.mean(CX.get(i));
        
        if(arr == null){
          //System.out.println("Found null!");
          arr = MatrixUtility.vector(d, 0);
        }
        
        C.add(arr);
      }
    }
    
    //Set extrema
    if(E!=null){
      E.clear();
      
      for(int i=0; i<K; i++){
       E.add(MatrixUtility.minmax(CX.get(i))); 
      }
    }
    
    //Set number of points in cluster
    if(N!=null){
      N.clear();
      
      for(int i=0; i<K; i++){
        N.add(CX.get(i).size());
      }
    }
  }
}