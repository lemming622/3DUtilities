package edu.ncsa.model.loaders.j3d;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.model.matrix.GMatrixUtility;
import edu.ncsa.model.matrix.MatrixUtility;
import java.util.*;
import javax.vecmath.*;
import javax.media.j3d.*;
import com.sun.j3d.loaders.*;
import com.sun.j3d.utils.geometry.*;
//import com.sun.j3d.utils.universe.*;
import org.web3d.vrml.renderer.j3d.browser.*;

/**
 * An abstract class which provides an interface for file format loaders into the mesh class.
 *  @author Kenton McHenry
 */
public abstract class MeshLoaderJ3D extends MeshLoader
{
	/**
	 * Load a Java3D scene into the mesh.
	 *  @param scene the Java3D scene
	 *  @return the loaded mesh
	 */
	public Mesh loadScene(Scene scene)
	{
		Mesh mesh = new Mesh();
		
    Node node;
    double[][] M = null;
    boolean DEBUG = false;
    
    BranchGroup bg = scene.getSceneGroup();
    bg.compile();
    //new Java3DViewer(bg);
    
    Stack<Node> stk_nodes = new Stack<Node>();
    Stack<double[][]> stk_transformations = new Stack<double[][]>();
    stk_nodes.push(bg);
    stk_transformations.push(MatrixUtility.eye(4));
    
    while(stk_nodes.size() > 0){
      node = stk_nodes.pop();
      M = stk_transformations.pop();
      if(DEBUG) System.out.print("\nJava 3D node: " + node.getClass().toString());
      
      if(node.getClass() == TransformGroup.class){
        Transform3D T = new Transform3D();
        ((TransformGroup)node).getTransform(T);
        double[] arr = new double[16];
        T.get(arr);
        
        for(int i=0; i<((TransformGroup)node).numChildren(); i++){
          stk_nodes.push(((TransformGroup)node).getChild(i));
          stk_transformations.push(GMatrixUtility.transform(M, MatrixUtility.to2D(4, 4, arr)));
        }
      }else if(node.getClass() == VRMLBranchGroup.class){
        for(int i=0; i<((VRMLBranchGroup)node).numChildren(); i++){
          stk_nodes.push(((VRMLBranchGroup)node).getChild(i));
          stk_transformations.push(M);
        }
      }else if(node.getClass() == BranchGroup.class){
        for(int i=0; i<((BranchGroup)node).numChildren(); i++){
          stk_nodes.push(((BranchGroup)node).getChild(i));
          stk_transformations.push(M);
        }
      }else if(node.getClass() == Group.class){
        for(int i=0; i<((Group)node).numChildren(); i++){
          stk_nodes.push(((Group)node).getChild(i));
          stk_transformations.push(M);
        }
      }else if(node.getClass() == Link.class){
        SharedGroup sg = ((Link)node).getSharedGroup();
        for(int i=0; i<sg.numChildren(); i++){
          stk_nodes.push(sg.getChild(i));
          stk_transformations.push(M);
        }
      }else if(node.getClass() == Box.class){
        appendShape3D(mesh, ((Box)node).getShape(Box.LEFT), M);
        appendShape3D(mesh, ((Box)node).getShape(Box.RIGHT), M);
        appendShape3D(mesh, ((Box)node).getShape(Box.TOP), M);
        appendShape3D(mesh, ((Box)node).getShape(Box.BOTTOM), M);
        appendShape3D(mesh, ((Box)node).getShape(Box.FRONT), M);
        appendShape3D(mesh, ((Box)node).getShape(Box.BACK), M);
      }else if(node.getClass() == Cylinder.class){
        appendShape3D(mesh, ((Cylinder)node).getShape(Cylinder.BODY), M);
        appendShape3D(mesh, ((Cylinder)node).getShape(Cylinder.TOP), M);
        appendShape3D(mesh, ((Cylinder)node).getShape(Cylinder.BOTTOM), M);
      }else if(node.getClass() == Shape3D.class){
        appendShape3D(mesh, (Shape3D)node, M);
      }else{
        System.out.print("\t[Not Supported!]");
      }
    }
    
    if(DEBUG) System.out.println();
    mesh.removeDuplicateVertices();
    mesh.removeErroneousFaces();
    
    return mesh;
	}
	
	/**
   * Add a Java3D shape to our mesh.
   *  @param mesh the mesh to append too
   *  @param shp the Java3D shape
   *  @param M a transformation matrix to apply to this shape
   */
  public void appendShape3D(Mesh mesh, Shape3D shp, double[][] M)
  {
  	Vector<Point> vertices = new Vector<Point>();
  	Vector<Face> faces = new Vector<Face>();
  	
    for(int i=0; i<shp.numGeometries(); i++){
      Geometry g = shp.getGeometry(i);
      int off = vertices.size();    //Current vertex offset for faces!
      
      if(g != null){
        if(g.getClass() == TriangleArray.class){
          TriangleArray ta = (TriangleArray)g;
          GeometryInfo gi = new GeometryInfo(ta);
          Point3f[] p = gi.getCoordinates();
      
          for(int j=0; j<p.length; j++){
            Point tmpv = new Point(p[j].x, p[j].y, p[j].z);
            tmpv = Point.transform(M, tmpv);
            vertices.add(tmpv);
          
            if(j%3 == 0){
              Face tmpf = new Face();
              tmpf.v[0] = off + j;
              tmpf.v[1] = off + j+1;
              tmpf.v[2] = off + j+2;
              faces.add(tmpf);
            }
          }
        }else if(g.getClass() == QuadArray.class){
          QuadArray qa = (QuadArray)g;
          GeometryInfo gi = new GeometryInfo(qa);
          Point3f[] p = gi.getCoordinates();
      
          for(int j=0; j<p.length; j++){
            Point tmpv = new Point(p[j].x, p[j].y, p[j].z);
            tmpv = Point.transform(M, tmpv);
            vertices.add(tmpv);
          
            if(j%4 == 0){
              Face tmpf = new Face(4);
              tmpf.v[0] = off + j;
              tmpf.v[1] = off + j+1;
              tmpf.v[2] = off + j+2;
              tmpf.v[3] = off + j+3;
              faces.add(tmpf);
            }
          }
        /*
        }else if(g.getClass() == LineArray.class){
          LineArray la = (LineArray)g;
          GeometryInfo gi = new GeometryInfo(la);   //FIX: Unsupported geometry type!
          Point3f[] p = gi.getCoordinates();
      
          for(int j=0; j<p.length; j++){
            Vertex tmpv = new Vertex(p[j].x, p[j].y, p[j].z);
            tmpv = Vertex.applyTransformation(M, tmpv);
            vertices.add(tmpv);
          
            if(j%2 == 0){
              Face tmpf = new Face(2);
              tmpf.v[0] = off + j;
              tmpf.v[1] = off + j+1;
              faces.add(tmpf);
            }
          }
        }else if(g.getClass() == LineStripArray.class){
          LineStripArray lsa = (LineStripArray)g;
          GeometryInfo gi = new GeometryInfo(lsa);  //FIX: Unsupported geometry type!
          Point3f[] p = gi.getCoordinates();
          
          int[] arr = new int[1024];
          lsa.getStripVertexCounts(arr);
          int at = 0;   //Current position in point array.
          
          for(int j=0; j<lsa.getNumStrips(); j++){
            for(int k=0; k<arr[j]; k++){
              Vertex tmpv = new Vertex(p[at].x, p[at].y, p[at].z);
              tmpv = Vertex.applyTransformation(M, tmpv);
              vertices.add(tmpv);
  
              if(k > 0){
                Face tmpf = new Face(2);
                tmpf.v[0] = off + at;
                tmpf.v[1] = off + at-1;
                faces.add(tmpf);
              }
              
              at++;
            }
          }
        */
        }else if(g.getClass() == TriangleStripArray.class){
          TriangleStripArray tsa = (TriangleStripArray)g;
          GeometryInfo gi = new GeometryInfo(tsa);
          Point3f[] p = gi.getCoordinates();
          
          int[] arr = new int[1024];
          tsa.getStripVertexCounts(arr);
          int at = 0;   //Current position in point array.
          
          for(int j=0; j<tsa.getNumStrips(); j++){
            for(int k=0; k<arr[j]; k++){
              Point tmpv = new Point(p[at].x, p[at].y, p[at].z);
              tmpv = Point.transform(M, tmpv);
              vertices.add(tmpv);
  
              if(k > 1){
                Face tmpf = new Face();
                
                if(k%2==0){
                  tmpf.v[0] = off + at;
                  tmpf.v[2] = off + at-1;
                  tmpf.v[1] = off + at-2;
                }else{
                  tmpf.v[0] = off + at;
                  tmpf.v[1] = off + at-1;
                  tmpf.v[2] = off + at-2;
                }
                
                faces.add(tmpf);
              }
              
              at++;
            }
          }
        }else if(g.getClass() == TriangleFanArray.class){
          TriangleFanArray tfa = (TriangleFanArray)g;
          GeometryInfo gi = new GeometryInfo(tfa);
          Point3f[] p = gi.getCoordinates();
          
          int[] arr = new int[1024];
          tfa.getStripVertexCounts(arr);
          int at = 0;   //Current position in point array.
          
          for(int j=0; j<tfa.getNumStrips(); j++){
            for(int k=0; k<arr[j]; k++){
              Point tmpv = new Point(p[at+k].x, p[at+k].y, p[at+k].z);
              tmpv = Point.transform(M, tmpv);
              vertices.add(tmpv);
  
              if(k > 1){
                Face tmpf = new Face();
                tmpf.v[0] = off + at+k;
                tmpf.v[1] = off + at+k-1;
                tmpf.v[2] = off + at;
                faces.add(tmpf);
              }
            }
            
            at += arr[j];
          }
        }else if(g.getClass() == IndexedLineStripArray.class){
          IndexedLineStripArray ilsa = (IndexedLineStripArray)g;
          GeometryInfo gi = new GeometryInfo(ilsa);
          Point3f[] p = gi.getCoordinates();
          int[] ind = gi.getCoordinateIndices();
          
          for(int j=0; j<p.length; j++){
            Point tmpv = new Point(p[j].x, p[j].y, p[j].z);
            tmpv = Point.transform(M, tmpv);
            vertices.add(tmpv);
          }

          int[] arr = new int[ilsa.getNumStrips()];
          ilsa.getStripIndexCounts(arr);
          int at = 0;   //Current position in index array
          
          for(int j=0; j<ilsa.getNumStrips(); j++){
            for(int k=0; k<arr[j]; k++){
              if(k > 0){
                Face tmpf = new Face(2);
                tmpf.v[0] = off + ind[at];
                tmpf.v[1] = off + ind[at-1];
                faces.add(tmpf);
              }
              
              at++;
            }
          }
        }else{
          System.out.println("Warning: Java 3D shape - " + g.getClass().toString() + " not supported.");
        }
      }
    }
    
    mesh.addData(vertices, faces, null);
  }
}