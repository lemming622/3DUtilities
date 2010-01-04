package edu.ncsa.model;
import edu.ncsa.model.Utility.*;
import edu.ncsa.model.ImageUtility.*;
import edu.ncsa.model.matrix.*;
import java.awt.image.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.JFrame;
import com.sun.opengl.util.*;
import gov.fpl.optimization.*;

/**
 * Auxillary structures used by the Mesh class.
 *  @author Kenton McHenry
 */
public class MeshAuxiliary
{
  /**
   * A 3-dimensional point that can also be treated as a 3D vector.
   */
  public static class Point implements Comparable, Serializable
  {
    public double x;
    public double y;
    public double z;
    
    /**
     * Class constructor.
     */
    public Point()
    {
      x = 0;
      y = 0;
      z = 0;
    }
    
    /**
     * Class constructor.
     *  @param x0 the x coordinate
     *  @param y0 the y coordinate
     *  @param z0 the z coordinate
     */
    public Point(double x0, double y0, double z0)
    {
      x = x0;
      y = y0;
      z = z0;
    }
    
    /**
     * Class copy constructor.
     *  @param v0 the vertex to copy
     */
    public Point(Point v0)
    {
      x = v0.x;
      y = v0.y;
      z = v0.z;
    }
    
    /**
     * Class constructor.
     *  @param p0 the point to copy
     */
    public Point(Pixel p0)
    {
    	x = p0.x;
    	y = p0.y;
    	z = 0;
    }
    
    /**
     * Class constructor.
     *  @param a0 the point stored within an array
     */
    public Point(int[] a0)
    {
    	x = a0[0];
    	y = a0[1];
    	if(a0.length > 2) z = a0[2];
    }
    
    /**
     * Return a string representation of this vertex.
     *  @return the string version of this vertex
     */
    public String toString()
    {
      String tmp = x + ", " + y + ", " + z;
      
      return tmp;
    }
    
    /**
     * Return an array representation of this vertex.
     *  @return the array version of this vertex
     */
    public double[] toArray()
    {
    	return new double[]{x, y, z};
    }
    
    /**
     * Determine if the vertex contains a valid value.
     *  @return true if x, y and z components are not infinity or NaN
     */
    public boolean isValid()
    {
      if(Double.isNaN(x) || Double.isInfinite(x)){
        return false;
      }else if(Double.isNaN(y) || Double.isInfinite(y)){
        return false;
      }else if(Double.isNaN(z) || Double.isInfinite(z)){
        return false;
      }else{
        return true;
      }
    }
    
    /**
     * Add another this vertex to another vertex.
     *  @param v the vertex to add
     *  @return the sum of this vertex and the given vertex
     */
    public Point plus(Point v)
    {
      return new Point(x+v.x, y+v.y, z+v.z);
    }
    
    /**
     * Translate this vertex.
     * @param tx the translation in the x-direction
     * @param ty the translation in the y-direction
     * @param tz the translation in the z-direction
     * @return the translated vertex
     */
    public Point plus(double tx, double ty, double tz)
    {
    	return new Point(x+tx, y+ty, z+tz);
    }
    
    /**
     * Add another vertex to this vertex.
     *  @param v the vertex to add
     */
    public void plusEquals(Point v)
    {
      x += v.x;
      y += v.y;
      z += v.z;
    }
    
    /**
     * Add another vertex to this vertex.
     *  @param v the vertex to add (represented as an array)
     */
    public void plusEquals(double[] v)
    {
      x += v[0];
      y += v[1];
      z += v[2];
    }
    
    /**
     * Subtract another this vertex to another vertex.
     *  @param v the vertex to subtract
     *  @return the difference of this vertex and the given vertex
     */
    public Point minus(Point v)
    {
      return new Point(x-v.x, y-v.y, z-v.z);
    }
    
    /**
     * Subtract another this vertex to another vertex.
     *  @param v the vertex to subtract (represented as an array)
     *  @return the difference of this vertex and the given vertex
     */
    public Point minus(double[] v)
    {
      return new Point(x-v[0], y-v[1], z-v[2]);
    }
    
    /**
     * Subtract another vertex from this one.
     *  @param v the vertex to subract
     */
    public void minusEquals(Point v)
    {
      x -= v.x;
      y -= v.y;
      z -= v.z;
    }
    
    /**
     * Subtract another vertex from this one.
     *  @param v the vertex to subract (represented as an array)
     */
    public void minusEquals(double[] v)
    {
      x -= v[0];
      y -= v[1];
      z -= v[2];
    }
    
    /**
     * The dot product between this vertex and another vertex
     *  @param v the vertex to multiply with
     *  @return the dot product
     */
    public double times(Point v)
    {
      return x*v.x + y*v.y + z*v.z;
    }
    
    /**
     * Multiply a double with this vertex, component-wise.
     *  @param d the multiplier
     *  @return the scaled vertex
     */
    public Point times(double d)
    {
      return new Point(x*d, y*d, z*d);
    }
    
    /**
     * Multiply a double with this vertex, component-wise.
     *  @param d the multiplier
     */
    public void timesEquals(double d)
    {
    	x *= d;
    	y *= d;
    	z *= d;
    }
    
    /**
     * Divide this vertex by the given float.
     *  @param f the divisor
     *  @return the scaled vertex
     */
    public Point divide(double f)
    {
      return new Point(x/f, y/f, z/f);
    }
    
    /**
     * Divide this vertex by the given float.
     *  @param f the divisor
     */
    public void divideEquals(double f)
    {
      x /= f;
      y /= f;
      z /= f;
    }
    
    /**
     * Assign or set this vertex to a different value.
     *  @param v the value to set to
     */
    public void assign(Point v)
    {
      x = v.x;
      y = v.y;
      z = v.z;
    }
    
    /**
     * Assign or set this vertex to a different value.
     *  @param x the x value to assign
     *  @param y the y value to assign
     *  @param z the z value to assign
     */
    public void assign(double x, double y, double z)
    {
      this.x = x;
      this.y = y;
      this.z = z;
    }
    
    /**
     * Calculate the magnitude of this vertex.
     *  @return the magnitude
     */
    public double magnitude()
    {
      return Math.sqrt(x*x + y*y + z*z);
    }
    
    /**
     * Normalize the vertex/vector to have unit norm.
     */
    public void normalize()
    {
    	divideEquals(magnitude());
    }
    
    /**
     * Calculate the distance of this vertex from the given one.
     *  @param v the vertex we are measuring the distance from
     *  @return the distance between the two vertices
     */
    public double distance(Point v)
    {
      double tmpx = x-v.x;
      double tmpy = y-v.y;
      double tmpz = z-v.z;
      
      return Math.sqrt(tmpx*tmpx + tmpy*tmpy + tmpz*tmpz);
    }
    
    /**
     * Calculate the distance of this vertex from the given line segment.
     * Note: Uses perpendicular distances to line segment.
     * TODO: Verify this code!
     *  @param line the line segment
     *  @return the distance from the line segment
     */
    public double distance(LineSegment line)
    {
    	double dx, dy, dz, dx0, dx1, dy0, dy1, dz0, dz1, length, t;
    	
    	dx0 = x - line.v0.x;
    	dy0 = y - line.v0.y;
    	dz0 = z - line.v0.z;
    	dx1 = line.v1.x - line.v0.x;
    	dy1 = line.v1.y - line.v0.y;
    	dz1 = line.v1.z - line.v0.z;
    	length = line.length();
    	t = (dx0*dx1 + dy0*dy1 + dz0*dz1) / (length*length);
    	
    	if(t <= 0){
    		return distance(line.v0);
    	}else if(t >= 1){
    		return distance(line.v1);
    	}else{
      	dx = line.v1.x - line.v0.x;
      	dy = line.v1.y - line.v0.y;
      	dz = line.v1.z - line.v0.z;
      	
    		return distance(new Point(line.v0.x+t*dx, line.v0.y+t*dy, line.v0.z+t*dz));
    	}
    }
    
    /**
     * Apply a transformation to this vertex.
     *  @param sx the scale in the x direction
     *  @param sy the scale in the y direction
     *  @param tx the translation in the x direction
     *  @param ty the translation in the y direction
     *  @return the transformed vertex
     */
    public Point transform(double sx, double sy, double tx, double ty)
    {
      return new Point(sx*x+tx, sy*y+ty, 0);
    }
    
    /**
     * Determine if this point is within the given planar polygon (note: ignores z-coordinate).
     *  @author Randolph Franklin, http://local.wasp.uwa.edu.au/~pbourke/geometry/insidepoly/
     *  @param p the polygon
     *  @return true if the point is inside the polygon
     */
    public boolean isWithin(Polygon p)
    {
      boolean internal = false;
      Point v0, v1;
      
      for(int i=0; i<p.size(); i++){
        v0 = p.get(i);
        v1 = p.get((i+1)%p.size());
      
        if((((v0.y<=y) && (y<v1.y)) || ((v1.y<=y) && (y<v0.y))) && (x < (v1.x-v0.x)*(y-v0.y)/(v1.y-v0.y)+v0.x)){
          internal = !internal;
        }
      }
      
      return internal;
    }
    
    /**
     * Determine if this point is within the face represented by the given three vertices.
     *  @author Randolph Franklin, http://local.wasp.uwa.edu.au/~pbourke/geometry/insidepoly/
     *  @param va a vertex on the face
     *  @param vb a vertex on the face
     *  @param vc a vertex on the face
     *  @return true if the point is inside the polygon
     */
    public boolean isWithin(Point va, Point vb, Point vc)
    {
      boolean internal = false;
      Point v0, v1;
      
      v0 = va;
      v1 = vb;
      if((((v0.y<=y) && (y<v1.y)) || ((v1.y<=y) && (y<v0.y))) && (x < (v1.x-v0.x)*(y-v0.y)/(v1.y-v0.y)+v0.x)) internal = !internal;
      
      v0 = vb;
      v1 = vc;
      if((((v0.y<=y) && (y<v1.y)) || ((v1.y<=y) && (y<v0.y))) && (x < (v1.x-v0.x)*(y-v0.y)/(v1.y-v0.y)+v0.x)) internal = !internal;
      
      v0 = vc;
      v1 = va;
      if((((v0.y<=y) && (y<v1.y)) || ((v1.y<=y) && (y<v0.y))) && (x < (v1.x-v0.x)*(y-v0.y)/(v1.y-v0.y)+v0.x)) internal = !internal;
      
      return internal;
    }
    
    /**
     * Determine if this point is within the given polygon group.
     * Note: does not consider the case where one of the polygons in the group
     * is actually a hole of another polygon.
     *  @param pg the polygon group
     *  @return true if the point is inside the polygon group
     */
    public boolean isWithin(PolygonGroup pg)
    {
      for(int i=0; i<pg.size(); i++){
        if(isWithin(pg.get(i))) return true;
      }
      
      return false;
    }
    
    /**
     * Compare this vertex to another.
     *  @param o the vertex to compare to
     *  @return the result (-1=less, 0=equal, 1=greater)
     */
    public int compareTo(Object o)
    {
    	if(this == o){
    		return 0;
    	}else{
	      Point v = (Point)o;
	      
	      if(x==v.x && y==v.y && z==v.z){
	        return 0;
	      }else{
	        if(x < v.x){
	          return -1;
	        }else if(x > v.x){
	          return 1;
	        }else{
	          if(y < v.y){
	            return -1;
	          }else if(y > v.y){
	            return 1;
	          }else{
	            if(z < v.z){
	              return -1;
	            }else{
	              return 1;
	            }
	          }
	        }
	      }
    	}
    }
    
    /**
     * Round the points values to the nearest integers.
     * @param p the point to round
     * @return the rounded point
     */
    public static Point round(Point p)
    {
    	Point tmpp = new Point();
    	
    	tmpp.x = Math.round(p.x);
    	tmpp.y = Math.round(p.y);
    	tmpp.z = Math.round(p.z);
    	
    	return tmpp;
    }

		/**
     * Normalize the given vertex/vector to have unit norm.
     *  @param v the vertex/vector
     *  @return the normalized vertex/vector
     */
    public static Point normalize(Point v)
    {
    	return v.divide(v.magnitude());
    }

		/**
     * Get the center and radius of the bounding sphere for the given set of vertices.
     *  @param vertices the vertices for which we want to know the bounding sphere
     *  @return the center and radius of the bounding sphere
     */
    public static Pair<Point,Double> getBoundingSphere(Vector<Point> vertices)
    {
    	Point center = new Point(0, 0, 0);
    	double radius = 0;
    	double tmpd;
    	
    	for(int i=0; i<vertices.size(); i++){
    	  center.plusEquals(vertices.get(i));
    	}
    	
    	center.divideEquals(vertices.size());
    	
    	for(int i=0; i<vertices.size(); i++){
    		tmpd = vertices.get(i).distance(center);
    		if(tmpd > radius) radius = tmpd;
    	}
    	
    	return new Pair<Point,Double>(center,radius);
    }
    
    /**
     * Calculate the cross product between the given 3 vertices.
     *  @param v0 the first vertex
     *  @param v1 the second vertex
     *  @param v2 the third vertex
     *  @return the cross product
     */
    public static Point cross(Point v0, Point v1, Point v2)
    {
      Point va = new Point();
      Point vb = new Point();
      Point vc = new Point();

      va.x = v1.x - v0.x;
      va.y = v1.y - v0.y;
      va.z = v1.z - v0.z;
      
      vb.x = v2.x - v1.x;
      vb.y = v2.y - v1.y;
      vb.z = v2.z - v1.z;

      vc.x = (va.y * vb.z) - (vb.y * va.z);
      vc.y = -(va.x * vb.z) + (vb.x * va.z);
      vc.z = (va.x * vb.y) - (vb.x * va.y);
      
      return vc;
    }
    
    /**
     * Calculate the cross product between the given vectors.
     *  @param va the first vector
     *  @param vb the second vector
     *  @return the cross product
     */
    public static Point cross(Point va, Point vb)
    {
      Point vc = new Point();

      vc.x = (va.y * vb.z) - (vb.y * va.z);
      vc.y = -(va.x * vb.z) + (vb.x * va.z);
      vc.z = (va.x * vb.y) - (vb.x * va.y);
      
      return vc;
    }
    
    /**
     * Zero out invalid vertices.
     *  @param vertices the list of vertices
     */
    public static void zeroInvalidVertices(Vector<Point> vertices)
    {    	
    	for(int i=0;i<vertices.size(); i++){
    		if(!vertices.get(i).isValid()){
    			vertices.set(i, new Point(0, 0, 0));
    		}
    	}
    }
    
    /**
     * Calculate the center of mass of a cloud of vertices.
     *  @param vertices the cloud of vertices
     *  @return the center of mass
     */
    public static Point getCentroid(Vector<Point> vertices)
    {
      Point center = new Point(0, 0, 0);
      
      for(int i=0; i<vertices.size(); i++){
        center.plusEquals(vertices.get(i));
      }

      center.divideEquals(vertices.size());
      
      return center;
    }
    
    /**
     * Calculate the radius of the bounding sphere of a cloud of vertices.
     *  @param vertices the cloud of vertices
     *  @param center the center of mass
     *  @return the radius
     */
    public static double getRadius(Vector<Point> vertices, Point center)
    {
      double maxr = 0;
      double tmpd;
      
      for(int i=0; i<vertices.size(); i++){
        tmpd = vertices.get(i).x - center.x;
        if(tmpd < 0) tmpd = -tmpd;
        if(tmpd > maxr) maxr = tmpd;

        tmpd = vertices.get(i).y - center.y;
        if(tmpd < 0) tmpd = -tmpd;
        if(tmpd > maxr) maxr = tmpd;

        tmpd = vertices.get(i).z - center.z;
        if(tmpd < 0) tmpd = -tmpd;
        if(tmpd > maxr) maxr = tmpd;
      }

      return maxr;
    }
    
    /**
     * Calculate the principal components of a cloud of vertices.
     *  @param vertices the cloud of vertices
     *  @return the principal components
     */
    public static Vector<Point> getPC(Vector<Point> vertices)
    {
    	Vector<Point> PC = new Vector<Point>();
      Vector<Vector<Double>> X = new Vector<Vector<Double>>();
      
      for(int i=0; i<vertices.size(); i++){
        X.add(new Vector<Double>());
        X.get(i).add(vertices.get(i).x);
        X.get(i).add(vertices.get(i).y);
        X.get(i).add(vertices.get(i).z);
      }
      
      double[][] E = JAMAMatrixUtility.pca(X);
      
      for(int i=0; i<3; i++){
        PC.add(new Point(E[0][i], E[1][i], E[2][i]));
      }
      
      return PC;
    }
    
    /**
     * Interpolate to find the depth of a point on a triangle.
     * @param p0 a point on a 3D triangle
     * @param p1 a point on a 3D triangle
     * @param p2 a point on a 3D triangle
     * @param x the x-coordinate of the desired depth
     * @param y the y-coordinate of the desired depth
     * @return the depth at the desired location
     */
    public static double getZ(Point p0, Point p1, Point p2, double x, double y)
    {
    	Point p = new Point(x, y, 0);
    	double z;
    	double z0 = p0.z;
    	double z1 = p1.z;
    	double z2 = p2.z;
    	double a, a0, a1, u, v, w;
    	
    	p0 = new Point(p0); p0.z = 0;
    	p1 = new Point(p1); p1.z = 0;
    	p2 = new Point(p2); p2.z = 0;
      
      a = Point.cross(p0, p1, p2).magnitude() / 2.0;
      a0 = Point.cross(p, p1, p2).magnitude() / 2.0;
      a1 = Point.cross(p0, p, p2).magnitude() / 2.0;
        
      u = a0 / a;
      v = a1 / a;
      w = 1 - u - v;
      z = u*z0 + v*z1 + w*z2;
      
      return z;
    }

		/**
     * Round a list of points cooridnates to the nearest integers.
     * @param p a list of points
     * @return a list of rounded points
     */
    public static Vector<Point> round(Vector<Point> p)
    {
      Vector<Point> p_round = new Vector<Point>();
      Point point;
      
      for(int i=0; i<p.size(); i++){
      	point = new Point();
      	point.x = Math.round(p.get(i).x);
      	point.y = Math.round(p.get(i).y);
      	point.z = Math.round(p.get(i).z);
      	p_round.add(point);
      }
      
      return p_round;
    }
    
    /**
     * Add random noise to a list of points
     * @param p a list of points
     * @param max the maximum amount of noise that will be added to any point coordinate
     * @return a list of noisy points
     */
    public static Vector<Point> addNoise(Vector<Point> p, double max)
    {
      Vector<Point> p_noisy = new Vector<Point>();
      Random random = new Random();
      Point point;
      
      for(int i=0; i<p.size(); i++){
      	point = new Point();
      	point.x = Math.round(p.get(i).x + max*random.nextDouble());
      	point.y = Math.round(p.get(i).y + max*random.nextDouble());
      	point.z = Math.round(p.get(i).z + max*random.nextDouble());
      	p_noisy.add(point);
      }
      
      return p_noisy;
    }
    
    /**
     * Make a deep copy of the given list of points.
     * @param p the list of points
     * @return the copy of the list of points
     */
    public static Vector<Point> copy(Vector<Point> p)
    {
    	Vector<Point> p_copy = new Vector<Point>();
    
    	for(int i=0; i<p.size(); i++){
    		p_copy.add(new Point(p.get(i)));
    	}
    	
    	return p_copy;
    }
    
    /**
     * Apply the given transformation to the given vertex.
     *  @param M the 4x4 transformation matrix
     *  @param p a vertex
     *  @return the transformed vertex
     */
    public static Point transform(double[][] M, Point p)
    {
      Point tmpv = new Point();
      double tmpd;
      
      tmpv.x = M[0][0]*p.x + M[0][1]*p.y + M[0][2]*p.z + M[0][3]*1.0f;
      tmpv.y = M[1][0]*p.x + M[1][1]*p.y + M[1][2]*p.z + M[1][3]*1.0f;
      tmpv.z = M[2][0]*p.x + M[2][1]*p.y + M[2][2]*p.z + M[2][3]*1.0f;
      tmpd   = M[3][0]*p.x + M[3][1]*p.y + M[3][2]*p.z + M[3][3]*1.0f;
      tmpv.x /= tmpd; tmpv.y /= tmpd; tmpv.z /= tmpd;
      
      return tmpv;
    }
    
    /**
     * Apply the given transformation to the given vector of vertices.
     *  @param M the 4x4 transformation matrix
     *  @param p the vector of vertices to transform
     *  @return the transformed vertices
     */
    public static Vector<Point> transform(double[][] M, Vector<Point> p)
    {
    	Vector<Point> pnew = new Vector<Point>();
      Point tmpv;
      double tmpd;
      
      for(int i=0; i<p.size(); i++){
      	tmpv = new Point();
        tmpv.x = M[0][0]*p.get(i).x + M[0][1]*p.get(i).y + M[0][2]*p.get(i).z + M[0][3]*1.0f;
        tmpv.y = M[1][0]*p.get(i).x + M[1][1]*p.get(i).y + M[1][2]*p.get(i).z + M[1][3]*1.0f;
        tmpv.z = M[2][0]*p.get(i).x + M[2][1]*p.get(i).y + M[2][2]*p.get(i).z + M[2][3]*1.0f;
        tmpd   = M[3][0]*p.get(i).x + M[3][1]*p.get(i).y + M[3][2]*p.get(i).z + M[3][3]*1.0f;
        tmpv.x /= tmpd; tmpv.y /= tmpd; tmpv.z /= tmpd;
      
        pnew.add(tmpv);
      }
      
      return pnew;
    }
    
    /**
     * Transform a polygon.
     *  @param points a polygon represented as an ordered sequence of vertices
     *  @param sx the scale factor in the x direction
     *  @param sy the scale factor in the y direction
     *  @param sz the scale factor in the z direction
     *  @param tx the translation in the x direction
     *  @param ty the translation in the y direction
     *  @param tz the translation in the z direction
     *  @return the transformed polygon
     */
    public static Vector<Point> transform(Vector<Point> points, double sx, double sy, double sz, double tx, double ty, double tz)
    {
      Vector<Point> points_new = new Vector<Point>();
      
      for(int i=0; i<points.size(); i++){
        points_new.add(new Point(sx*points.get(i).x+tx, sy*points.get(i).y+ty, sz*points.get(i).z+tz)); 
      }
      
      return points_new;
    }
    
    /**
     * Transform a polygon.
     *  @param points a polygon represented as an ordered sequence of vertices
     *  @param sx the scale factor in the x direction
     *  @param sy the scale factor in the y direction
     *  @param tx the translation in the x direction
     *  @param ty the translation in the y direction
     *  @return the transformed polygon
     */
    public static Vector<Point> transform(Vector<Point> points, double sx, double sy, double tx, double ty)
    {
      Vector<Point> points_new = new Vector<Point>();
      
      for(int i=0; i<points.size(); i++){
        points_new.add(new Point(sx*points.get(i).x+tx, sy*points.get(i).y+ty, 0)); 
      }
      
      return points_new;
    }
    
    /**
     * Transform a cloud of vertices into its cannonical form given its center of mass, the radius of its bounding sphere, and principal components.
     *  @param vertices the cloud of points
     *  @param center the center of mass
     *  @param radius the radius of the bounding sphere
     *  @param PC the principal components
     *  @return the transformed vertices
     */
    public static Vector<Point> transform(Vector<Point> vertices, Point center, double radius, Vector<Point> PC)
    {
    	Vector<Point> vertices_new = new Vector<Point>();
    	double[] ib, jb, kb;
    	
    	//Rotate principal components into XYZ axis.
    	if(PC != null){		
    		ib = PC.get(0).toArray();
    		jb = PC.get(1).toArray();
    		kb = PC.get(2).toArray();
    		
    		vertices = Point.transform(MatrixUtility.rotate(ib, jb, kb), vertices);
    		
    		//Recalculate bounding sphere
    		center = Point.getCentroid(vertices);
    		radius = Point.getRadius(vertices, center);
    	}
    	
    	//Center and scale points so that bounding sphere has radius 1.
    	for(int i=0; i<vertices.size(); i++){
    		vertices_new.add(vertices.get(i).minus(center).divide(radius));
    	}
    	
    	return vertices_new;
    }
    
    /**
     * Transform a cloud of vertices.
     * Note: assumes that the vertices are already centered and scaled to occupy the unit bounding sphere!
     *  @param vertices the cloud of vertices
     *  @param scale the desired bounding sphere radius of a cloud of vertices
     *  @param offset the desired offset along each axis for a cloud of vertices
     *  @return the transformed vertices
     */
    public static Vector<Point> transform(Vector<Point> vertices, double scale, double offset)
    {
      Vector<Point> vertices_new = new Vector<Point>();
      
      for(int i=0; i<vertices.size(); i++){
        vertices_new.add(new Point(scale*vertices.get(i).x+offset, scale*vertices.get(i).y+offset, scale*vertices.get(i).z+offset)); 
      }
      
      return vertices_new;
    }
    
    /**
     * Construct a 4x4 rotation matrix that rotates the coordinate space represented by the vectors (ib, jb, kb)
     * to the coordinate space represented by the vectors (ia, ja, ka), where ia = (1, 0, 0), 
     * ja = (0, 1, 0), and ka = (0, 0, 1)
     *  @param ib the x axis in coordinate space a
     *  @param jb the y axis in coordinate space a
     *  @param kb the z axis in coordinate space a
     *  @return the resulting rotation matrix
     */
    public static double[][] rotate(Point ib, Point jb, Point kb)
    {
      double[][] M = new double[4][4];
      
      M[0][0] = ib.x;
      M[0][1] = ib.y;
      M[0][2] = ib.z;
      M[0][3] = 0;
      
      M[1][0] = jb.x;
      M[1][1] = jb.y;
      M[1][2] = jb.z;
      M[1][3] = 0;
      
      M[2][0] = kb.x;
      M[2][1] = kb.y;
      M[2][2] = kb.z;
      M[2][3] = 0;
      
      M[3][0] = 0;
      M[3][1] = 0;
      M[3][2] = 0;
      M[3][3] = 1;
      
      return M;
    }
    
    /**
     * Construct a 4x4 rotation matrix that rotates the given unit vector direction into the z axis.
     *  @param ia the unit vector to rotate
     *  @return the resulting rotation matrix
     */
    public static double[][] rotateToZ(Point ia)
    {
      Point ja = new Point(0, 0, 1);
      Point ka;
            
      if(ia.z != 0){	//Create a vector that is orthogonal to ia
	      ja.x = 1;
	      ja.y = 1;
	      ja.z = (ia.x + ia.y) / -ia.z;
	      ja.divideEquals(ja.magnitude());
      }
        
      ka = Point.cross(ia, ja);
      ka.divideEquals(ka.magnitude());
      
      return rotate(ka, ja, ia);
    }
    
    /**
     * Determine if a ray intersects a line segment.
     *  @param a0 the starting point of the first line
     *  @param a1 the ending point of the first line
     *  @param b0 the starting point of the second line
     *  @param b1 the ending point of the second line
     *  @return the intersection point
     */
    public static Point getRayIntersection(Point a0, Point a1, Point b0, Point b1)
    {
      Point tmpv = null;
      double tmpd = (b1.y-b0.y)*(a1.x-a0.x) - (b1.x-b0.x)*(a1.y-a0.y);
      double x, y;
      
      if(tmpd != 0.0){
        double ta = ((b1.x-b0.x)*(a0.y-b0.y) - (b1.y-b0.y)*(a0.x-b0.x)) / tmpd;
        double tb = ((a1.x-a0.x)*(a0.y-b0.y) - (a1.y-a0.y)*(a0.x-b0.x)) / tmpd;
        
        if(tb >= 0.0 && tb <= 1.0){   //Assume first points are an infinite ray and ignore bounds
          x = a0.x + ta*(a1.x-a0.x);
          y = a0.y + ta*(a1.y-a0.y);
          tmpv = new Point(x, y, 0);
        }
      }
      
      return tmpv;
    }
    
    /**
     * Determine if the two line segments intersect.
     * Note: assumes they are planar in the XY-plane!
     *  @param a0 the starting point of the first line
     *  @param a1 the ending point of the first line
     *  @param b0 the starting point of the second line
     *  @param b1 the ending point of the second line
     *  @return the intersection point
     */
    public static Point getPlanarSegmentIntersection(Point a0, Point a1, Point b0, Point b1)
    {
      Point tmpv = null;
      double tmpd = (b1.y-b0.y)*(a1.x-a0.x) - (b1.x-b0.x)*(a1.y-a0.y);
      double x, y;
      
      if(tmpd != 0.0){    //If not parrallel!
        double ta = ((b1.x-b0.x)*(a0.y-b0.y) - (b1.y-b0.y)*(a0.x-b0.x)) / tmpd;
        double tb = ((a1.x-a0.x)*(a0.y-b0.y) - (a1.y-a0.y)*(a0.x-b0.x)) / tmpd;
        
        if(ta >= 0.0 && ta <= 1.0 && tb >= 0.0 && tb <= 1.0){
          x = a0.x + ta*(a1.x-a0.x);
          y = a0.y + ta*(a1.y-a0.y);
          tmpv = new Point(x, y, 0);
        }
      }
      
      return tmpv;
    }
    
    /**
     * Convert polygons represented as an ordered sequence of image points to one represented as real vertices.
     *  @param points the polygon
     *  @return the polygon using vertices
     */
    public static Vector<Vector<Point>> getVertices(Vector<Vector<Pixel>> points)
    {
      Vector<Vector<Point>> vertices = new Vector<Vector<Point>>();
      Pixel p;
      
      for(int i=0; i<points.size(); i++){
        vertices.add(new Vector<Point>());
        
        for(int j=0; j<points.get(i).size(); j++){
          p = points.get(i).get(j);
          vertices.get(i).add(new Point(p.x + 0.5, p.y + 0.5, 0));
        }
      }
      
      return vertices;
    }
    
    /**
     * Draw the given vertex onto the given image.
     *  @param img the image to draw on
     *  @param w the width of the image
     *  @param h the height of the image
     *  @param v the vertex to draw
     *  @param color the color of the points
     */
    public static void drawPoint(int[] img, int w, int h, Point v, int color)
    {
      int at = (int)Math.round(v.y)*w+(int)Math.round(v.x);   
      if(at < img.length) img[at] = color;
    }
    
    /**
     * Draw the given vector of points onto the given image.
     *  @param img the image to draw on
     *  @param w the width of the image
     *  @param h the height of the image
     *  @param points the points to draw
     *  @param color the color of the points
     */
    public static void drawPoints(int[] img, int w, int h, Vector<Point> points, int color)
    {
      int x, y, at;
      
      for(int i=0; i<points.size(); i++){
        x = (int)Math.round(points.get(i).x);
        y = (int)Math.round(points.get(i).y);
        at = y*w+x;   
        if(at < img.length) img[at] = color;
      }
    }
    
    /**
     * Draw the given vector of points onto the given image.
     *  @param img the image to draw on
     *  @param w the width of the image
     *  @param h the height of the image
     *  @param points the points to draw
     *  @param r the radius of the points (in pixels)
     *  @param color the color of the points
     */
    public static void drawThickPoints(int[] img, int w, int h, Vector<Point> points, int r, int color)
    {
      int x, y, at;
      int minu, maxu, minv, maxv;
      
      for(int i=0; i<points.size(); i++){
        x = (int)Math.round(points.get(i).x);
        y = (int)Math.round(points.get(i).y);
        
        minu = x - r;
        maxu = x + r;
        minv = y - r;
        maxv = y + r;
        
        if(minu < 0) minu = 0;
        if(maxu >= w) maxu = w-1;
        if(minv < 0) minv = 0;
        if(maxv >= h) maxv = h-1;
        
        for(int u=minu; u<=maxu; u++){
          for(int v=minv; v<=maxv; v++){
            at = v*w+u;   
            if(at < img.length) img[at] = color; 
          }
        }
      }
    }
    
    /**
     * Draw multiple groups of points to an image and return it.
     *  @param img the image to draw to
     *  @param points the groups of points to draw
     *  @param w the width of the desired image
     *  @param h the height of the desired image
     *  @param color the color of the points
     */
    public static void drawPointGroups(int[] img, Vector<Vector<Point>> points, int w, int h, int color)
    {
      for(int i=0; i<points.size(); i++){
        drawPoints(img, w, h, points.get(i), color);
      }
    }
    
    /**
     * Draw a polygon to an image.
     *  @param img the image to draw to
     *  @param w the width of the image
     *  @param h the height of the image
     *  @param points the polygon represented by an ordered sequence of vertices
     *  @param color the color of the polygon
     */
    public static void drawPolygon(int[] img, int w, int h, Vector<Point> points, int color)
    {
      Point v0;
      Point v1;
      
      for(int i=0; i<points.size(); i++){
        v0 = points.get(i);
        v1 = points.get((i+1)%points.size());
        ImageUtility.drawLine(img, w, h, v0.x, v0.y, v1.x, v1.y, color);
      }
    }
    
  	/**
     * Draw a triangle to an image via horizontal scan conversion and depth testing.
     *  @param img the image to draw to
     *  @param zbuf the Z-buffer
     *  @param w the width of the image
     *  @param h the height of the image
     *  @param p0 a point on the triangle
     *  @param p1 a point on the triangle
     *  @param p2 a point on the triangle
     *  @param color the color in ARGB
     */
    public static void drawTriangle(int[] img, double[] zbuf, int w, int h, Point p0, Point p1, Point p2, int color)
    {
    	Point p_miny, p_midy, p_maxy;
    	double m1i, m2i, tmpd1, tmpd2;
    	int minx, maxx;
    	double z;
    	int at;
    	
    	//Round floating point vertices to integer values
    	p0.x = Math.round(p0.x);
    	p0.y = Math.round(p0.y);
    	p1.x = Math.round(p1.x);
    	p1.y = Math.round(p1.y);
    	p2.x = Math.round(p2.x);
    	p2.y = Math.round(p2.y);
    	
    	//Set extreme points in the y-axis
    	p_miny = p0;
    	if(p1.y < p_miny.y) p_miny = p1;
    	if(p2.y < p_miny.y) p_miny = p2;
    	
    	p_maxy = p0;
    	if(p1.y > p_maxy.y) p_maxy = p1;
    	if(p2.y > p_maxy.y) p_maxy = p2;
    	
    	if(p0 != p_miny && p0 != p_maxy){
    		p_midy = p0;
    	}else if(p1 != p_miny && p1 != p_maxy){
    		p_midy = p1;
    	}else{
    		p_midy = p2;
    	}
    
    	//Scan traingle
    	if(p_miny.y == p_midy.y){		//Special case with horizontal top edge
    		m1i = (double)(p_maxy.x-p_miny.x) / (double)(p_maxy.y-p_miny.y);
    		m2i = (double)(p_maxy.x-p_midy.x) / (double)(p_maxy.y-p_midy.y);
    		tmpd1 = p_miny.x;
    		tmpd2 = p_midy.x;
    		
    		for(int y=(int)p_miny.y; y<p_maxy.y; y++){
    			if(tmpd1 < tmpd2){
    				minx = (int)Math.round(tmpd1);
    				maxx = (int)Math.round(tmpd2);
    			}else{
    				minx = (int)Math.round(tmpd2);
    				maxx = (int)Math.round(tmpd1);
    			}
    			
    			if(minx < 0) minx = 0;
    			if(maxx >= w) maxx = w-1;
    			
    			for(int x=minx; x<=maxx; x++){
    				z = getZ(p0, p1, p2, x, y);
    				
    				if(y>=0 && y<h){
	    				at = y*w+x;
	    				
	    				if(z > zbuf[at]){
	    					img[at] = color;
	    					zbuf[at] = z;
	    				}
    				}
    			}
    			
    			tmpd1 += m1i;
    			tmpd2 += m2i;
    		}
    	}else{											//Typical case
    		m1i = (double)(p_maxy.x-p_miny.x) / (double)(p_maxy.y-p_miny.y);
    		m2i = (double)(p_midy.x-p_miny.x) / (double)(p_midy.y-p_miny.y);
    		tmpd1 = p_miny.x;
    		tmpd2 = p_miny.x;
    		
    		for(int y=(int)p_miny.y; y<p_midy.y; y++){
    			if(tmpd1 < tmpd2){
    				minx = (int)Math.round(tmpd1);
    				maxx = (int)Math.round(tmpd2);
    			}else{
    				minx = (int)Math.round(tmpd2);
    				maxx = (int)Math.round(tmpd1);
    			}
    			
    			if(minx < 0) minx = 0;
    			if(maxx >= w) maxx = w-1;
    			
    			for(int x=minx; x<=maxx; x++){
    				z = getZ(p0, p1, p2, x, y);
    				
    				if(y>=0 && y<h){
	    				at = y*w+x;
	    				
	    				if(z > zbuf[at]){
	    					img[at] = color;
	    					zbuf[at] = z;
	    				}
    				}
    			}
    			
    			tmpd1 += m1i;
    			tmpd2 += m2i;
    		}
    		
    		m2i = (double)(p_maxy.x-p_midy.x) / (double)(p_maxy.y-p_midy.y);
    		
    		for(int y=(int)p_midy.y; y<p_maxy.y; y++){
    			if(tmpd1 < tmpd2){
    				minx = (int)Math.round(tmpd1);
    				maxx = (int)Math.round(tmpd2);
    			}else{
    				minx = (int)Math.round(tmpd2);
    				maxx = (int)Math.round(tmpd1);
    			}
    			
    			if(minx < 0) minx = 0;
    			if(maxx >= w) maxx = w-1;
    			
    			for(int x=minx; x<=maxx; x++){
    				z = getZ(p0, p1, p2, x, y);
    				
    				if(y>=0 && y<h){
	    				at = y*w+x;
	    				
	    				if(z > zbuf[at]){
	    					img[at] = color;
	    					zbuf[at] = z;
	    				}
    				}
    			}
    			
    			tmpd1 += m1i;
    			tmpd2 += m2i;
    		}
    	}
    }
  }
  
  /**
   * An edge between two referenced vertices.
   */
  public static class Edge implements Serializable
  {
    public int v0;
    public int v1;
    
    /**
     * Class constructor.
     */
    public Edge()
    {
      v0 = 0;
      v1 = 0;
    }
    
    /**
     * Class constructor.
     *  @param i0 index of starting vertex
     *  @param i1 index of ending vertex
     */
    public Edge(int i0, int i1)
    {
      v0 = i0;
      v1 = i1;
    }
  }
  
  /**
   * A line segment represented by two vertices. Pretty much a Pair<Vertex,Vertex> but allows for cleaner code.
   */
  public static class LineSegment implements Serializable
  {
  	public Point v0 = null;
  	public Point v1 = null;
  	
  	public LineSegment() {}
  	
  	/**
  	 * Class constructor.
  	 *  @param v0 starting vertex of the line segment
  	 *  @param v1 ending vertex of the line segment
  	 */
  	public LineSegment(Point v0, Point v1)
  	{
  	  this.v0 = v0;
  	  this.v1 = v1;
  	}
  	
  	/**
  	 * Class constructor.
  	 *  @param p a pair holding the start and end points of a line segment
  	 */
  	public LineSegment(Pair<Point,Point> p)
  	{
  		v0 = p.first;
  		v1 = p.second;
  	}
  	
  	/**
  	 * Return the length of the line segment.
  	 *  @return the length of the segment
  	 */
  	public double length()
  	{
  		return v0.distance(v1);
  	}
  }
  
  /**
   * A face between a number of referenced vertices.
   */
  public static class Face implements Serializable
  {
    public int[] v;
    public Point[] vn;
    public UV[] uv;
    public Material material = null;
    public Point center;
    public Point normal;
    public boolean VISIBLE = true;
    
    /**
     * Class constructor.
     */
    public Face()
    {
      v = new int[3];
    }
    
    /**
     * Class constructor.
     *  @param n the number of vertices in this face
     */
    public Face(int n)
    {
      v = new int[n];
    }
    
    /**
     * Class constructor.
     *  @param v0 the index of the first vertex in a face
     *  @param v1 the index of the second vertex in a face
     *  @param v2 the index of the third vertex in a face
     */
    public Face(int v0, int v1, int v2)
    {
      v = new int[3];
      v[0] = v0;
      v[1] = v1;
      v[2] = v2;
    }
    
    /**
     * Class constructor.
     *  @param c a collection of vertex indices representing a polygon
     */
    public Face(Collection<Integer> c)
    {
      Iterator<Integer> itr = c.iterator();
      int i;
      
      v = new int[c.size()];
      i = 0;
      
      while(itr.hasNext()){
      	v[i] = itr.next();
      	i++;
      }
    }
    
    /**
     * Class constructor.
     *  @param v0 the index of the first vertex in an edge
     *  @param v1 the index of the second vertex in an edge
     */
    public Face(int v0, int v1)
    {
      v = new int[2];
      v[0] = v0;
      v[1] = v1;
    }
    
    /**
     * Class copy constructor.
     */
    public Face(Face f)
    {
    	v = new int[f.v.length];
    	
    	for(int i=0; i<v.length; i++){
    		v[i] = f.v[i];
    	}
    	
    	if(f.vn != null){
    		vn = new Point[f.vn.length];
    		
	    	for(int i=0; i<vn.length; i++){
	    		vn[i] = f.vn[i];
	    	}
    	}
    	
    	if(f.uv != null){
    		uv = new UV[f.uv.length];
    		
	    	for(int i=0; i<uv.length; i++){
	    		uv[i] = f.uv[i];
	    	}
    	}

    	material = f.material;
    	center = f.center;
    	normal = f.normal;
    	VISIBLE = f.VISIBLE;
    }
    
    /**
     * Get the vertices used in this face.
     *  @param vertices the complete set of vertices that this face references too
     *  @return the vertices used in this face
     */
    public Vector<Point> getVertices(Vector<Point> vertices)
    {
    	Vector<Point> face_vertices = new Vector<Point>();
    	
    	for(int i=0; i<v.length; i++){
    		face_vertices.add(vertices.get(v[i]));
    	}
    	
    	return face_vertices;
    }
    
    /**
     * Calculate the area of this face.
     *  @param vertices the vertices which this faces references
     */
    public double getArea(Vector<Point> vertices)
    {
	  	Point p = new Point(0, 0, 0);
	  	Point n, v0, v1;
	  	
	  	if(v.length < 3){
	  		return 0;
	  	}else{
		  	n = Face.normal(vertices.get(v[0]), vertices.get(v[1]), vertices.get(v[2]));
		  			
		  	for(int i=0; i<v.length; i++){
		  		v0 = vertices.get(v[i]);
		  		v1 = vertices.get(v[(i+1)%v.length]);
		  		p.plusEquals(Point.cross(v0, v1));
		  	}
		  	
		  	return n.times(p.divide(2));
	  	}
  	}
    
    /**
     * Get the vertices used in this face, represented as a polygon.  Note, this performs a deep
     * copy of the used vertices.
     *  @param vertices the complete set of vertices that this face references too
     *  @return a polygon representing this face
     */
    public Polygon getPolygon(Vector<Point> vertices)
    {
    	Vector<Point> face_vertices = new Vector<Point>();
    	
    	for(int i=0; i<v.length; i++){
    		face_vertices.add(new Point(vertices.get(v[i])));
    	}
    	
    	return new Polygon(face_vertices);
    }
    
    /**
     * Add an offset to the referenced vertices.
     *  @param n the offset
     */
    public Face plus(int n)
    {
    	Face f = new Face(this);
    	f.plusEquals(n);
    	
    	return f;
    }
    
    /**
     * Add an offset to the referenced vertices.
     *  @param n the offset
     */
    public void plusEquals(int n)
    {
    	for(int i=0; i<v.length; i++){
    		v[i] += n;
    	}
    }
    
    /**
     * Calculate the center of this face.
     * @param vertices the vertices to which this face references
     * @return the face center
     */
    public Point center(Vector<Point> vertices)
    {
    	if(v.length > 0){
	    	Point center = new Point(0,0,0);
	    	
	    	for(int i=0; i<v.length; i++){
	    		center.plusEquals(vertices.get(v[i]));
	    	}
	    	
	    	center.divideEquals(v.length);
	    	
	    	return center;
    	}else{
    		return null;
    	}
    }
    
    /**
     * Calculate the normal of this face.
     * @param vertices the vertices to which this face references
     * @return the face normal
     */
    public Point normal(Vector<Point> vertices)
    {
    	if(v.length >= 3){
    		return normal(vertices.get(v[0]), vertices.get(v[1]), vertices.get(v[2]));
    	}else{
    		return null;
    	}
    }

		/**
     * Calculate the normal of the face represented by the given vertices.
     *  @param v0
     *  @param v1
     *  @param v2
     *  @return the normal
     */
    public static Point normal(Point v0, Point v1, Point v2)
    {
      Point norm = Point.cross(v0, v1, v2);
      norm.divideEquals(norm.magnitude());
      
      return norm;
    }

		/**
     * Add an offset to the references vertex indices in the given vector of faces.
     *  @param faces the faces to offset
     *  @param n the index offset
     *  @return the faces with offset vertex indices
     */
    public static Vector<Face> plus(Vector<Face> faces, int n)
    {
    	Vector<Face> faces_new = new Vector<Face>();
    	
    	for(int i=0; i<faces.size(); i++){
    		faces_new.add(faces.get(i).plus(n));
    	}
    	
    	return faces_new;
    }
  }
  
  /**
   * A color in RGB
   */
  public static class Color implements Serializable
  {
    public float r;
    public float g;
    public float b;
      
    /**
     * Class constructor.
     */
    public Color()
    {
      r = 0;
      g = 0;
      b = 0;
    }
      
    /**
     * Class constructor.
     *  @param r0 the red value
     *  @param g0 the green value
     *  @param b0 the blue value
     */
    public Color(float r0, float g0, float b0)
    {
      r = r0;
      g = g0;
      b = b0;
    }
    
    /**
     * Class constructor.
     * @param rgb the color represented as an int
     */
    public Color(int rgb)
    {
      r = ((float)((rgb>>16)&0xff)) / 255f;
      g = ((float)((rgb>>8)&0xff)) / 255f; 
      b = ((float)(rgb&0xff)) / 255f; 
    }
    
    /**
     * Get a string version of this color.
     */
    public String toString()
    {
    	String buffer = "";
    	
    	buffer += r + " ";
    	buffer += g + " ";
    	buffer += b;
    	
    	return buffer;
    }
    
    /**
     * Add this color to another color.
     *  @param c a color to add to
     *  @return the summed color
     */
    public Color plus(Color c)
    {
      return new Color(r+c.r, g+c.g, b+c.b);
    }
    
    /** 
     * Add another color to this color.
     *  @param c the color to add
     */
    public void plusEquals(Color c)
    {
      r += c.r;
      g += c.g;
      b += c.b;
    }
    
    /**
     * Multiply another color with this color (member-wise).
     *  @param c the color to multiple with
     *  @return the multiplied color
     */
    public Color times(Color c)
    {
      return new Color(r*c.r, g*c.g, b*c.b);
    }
    
    /**
     * Multiply this color by a float.
     *  @param f the multiplier
     *  @return the scaled color
     */
    public Color times(float f)
    {
      return new Color(r*f, g*f, b*f);
    }
    
    /**
     * Assign this color a different value.
     *  @param c the color containg the values to use
     */
    public void assign(Color c)
    {
      r = c.r;
      g = c.g;
      b = c.g;
    }
    
    /**
     * Cap the maximum value for each channel.
     * @param max the desired maximum value
     */
    public void cap(float max)
    {
      if(r > max) r = max;
      if(g > max) g = max;
      if(b > max) b = max;
    }
      
    /**
     * Get an integer representation of this color (in ARGB).
     * @return the integer version of this color
     */
    public int getInt()
    {
      return 0xff000000 | (int)(255*r)<<16 | (int)(255*g)<<8 | (int)(255*b);
    }
    
    /**
     * Set the RGB channels
     *  @param r0 the R component
     *  @param g0 the G component
     *  @param b0 the B component
     */
    public void setRGB(int r0, int g0, int b0)
    {
      r = ((float)r0)/255f;
      g = ((float)g0)/255f; 
      b = ((float)b0)/255f; 
    }
    
    /**
     * Set the color from YUV components
     *  @param y the Y component
     *  @param u the U component
     *  @param v the V component
     */
    public void setYUV(float y, float u, float v)
    {
      r = 1.164f*(y-16f) + 1.596f*(v-128f);
      r /= 255f;
      if(r < 0f) r = 0;
      if(r > 1f) r = 1;
      
      g = 1.164f*(y-16f) - 0.813f*(v-128f) - 0.391f*(u-128f);
      g /= 255f;
      if(g < 0f) g = 0;
      if(g > 1f) g = 1;
      
      b = 1.164f*(y-16f) + 2.018f*(u-128f);
      b /= 255f;
      if(b < 0f) r = 0;
      if(b > 1f) r = 1;
    }
  }
  
  /**
   * The texture coordiante of a vertex.
   */
  public static class UV implements Serializable, Comparable
  {
  	public float u;
  	public float v;
  	
  	public UV() {}
  	
  	/**
  	 * Class constructor.
  	 *  @param u the x texture coordinate
  	 *  @param v the y texture coordinate
  	 */
  	public UV(float u, float v)
  	{
  		this.u = u;
  		this.v = v;
  	}
  	
    /**
     * Compare this uv to another.
     *  @param o the uv to compare to
     *  @return the result (-1=less, 0=equal, 1=greater)
     */
    public int compareTo(Object o)
    {
      if(this == o){
      	return 0;
      }else{
      	UV uv = (UV)o;
      	
      	if(u < uv.u){
      		return -1;
      	}else if(u > uv.u){
      		return 1;
      	}else{
      		if(v < uv.v){
      			return -1;
      		}else if(v > uv.v){
      			return 1;
      		}else{
      			return 0;
      		}
      	}
      }
    }
  }
  
  /**
   * A texture image.
   */
  public static class Texture implements Serializable
  {
  	public String name = "";
  	public int[] argb;
  	public ByteBuffer buffer;
  	public int w;
  	public int h;
  	public int tid = -1;	//-1 indicates that this texture has not yet been bound by OpenGL
  	
  	public Texture() {}
  	
  	/**
  	 * Class constructor.
  	 *  @param name the texture name
  	 *  @param img the pixel data
  	 *  @param n the width/height (should be equal)
  	 */
  	public Texture(String name, int[] img, int n)
  	{
  		int rgb;
  		byte r, g, b;
  		
  		this.name = name;
  		argb = img;
  		buffer = BufferUtil.newByteBuffer(3*n*n);
  		w = n;
  		h = n;
  		
  		for(int y=h-1; y>=0; y--){
  			for(int x=0; x<w; x++){
          rgb = img[y*w+x];
	        r = (byte)((0x00ff0000 & rgb) >> 16);
	        g = (byte)((0x0000ffff & rgb) >> 8);
	        b = (byte)(0x000000ff & rgb);

  				buffer.put(r);
  				buffer.put(g);
  				buffer.put(b);
  			}
  		}
  		
  		buffer.flip();
  	}
  }
  
  /**
   * The material properties of a surface.
   */
  public static class Material implements Serializable, Comparable
  {
  	public String name = "";
    public Color emissive;
    public Color diffuse = null;
    public Color specular;
    public Color transmissive;
    public float shininess;
    public float index_of_refraction;
    public int tid = -1;
      	
    private static int instances = 0;

    public Material() {}
    
    /**
     * Class constructor.
     * @param name the name of this material
     */
    public Material(String name)
    {
    	this.name = name;
    }
    
    /**
     * Class constructor.
     * @param tid a texture id
     */
    public Material(int tid)
    {
    	name = "mateiral-" + Math.abs(new Random().nextInt()) + "-" + instances++;
    	this.tid = tid;
    }
    
    /**
     * Class constructor.
     * @param color the diffuse color
     */
    public Material(Color color)
    {
    	name = "mateiral-" + Math.abs(new Random().nextInt()) + "-" + instances++;
    	diffuse = color;
    }
    
    /**
     * Compare this material to another.
     *  @param o the material to compare to
     *  @return the result (-1=less, 0=equal, 1=greater)
     */
    public int compareTo(Object o)
    {
    	if(this == o){
    		return 0;
    	}

    	return name.compareTo(((Material)o).name);
    }
  }
  
  /**
   * A structure to represent a polygon (i.e. a vector of vertices).
   * The main purpose of this class is to simplify code that would otherwise
   * consist of chains of vectors of vertices.
   */
  public static class Polygon
  {
    private Vector<Point> vertices;
    
    /**
     * Class constructor.
     */
    public Polygon()
    {
      vertices = new Vector<Point>();
    }
    
    /**
     * Class constructor.
     *  @param vertices the polygon represented as a collection of ordered vertices
     */
    public Polygon(Collection<Point> vertices)
    {
      this.vertices = new Vector<Point>(vertices);
    }
    
    /**
     * Class copy constructor, deep copy.
     *  @param p the polygon to copy
     */
    public Polygon(Polygon p)
    {
    	vertices = new Vector<Point>();
    	
    	for(int i=0; i<p.vertices.size(); i++){
    		vertices.add(new Point(p.vertices.get(i)));
    	}
    }
    
    /**
     * Get the number of vertices in the polygon.
     *  @return the size of the polygon
     */
    public int size()
    {
      return vertices.size();
    }
    
    /**
     * Check if the polygon is empty.
     *  @return true if the polygon is empty
     */
    public boolean isEmpty()
    {
      return vertices.isEmpty();
    }
    
    /**
     * Convert polygon contents into a string representation.
     * @return the string representation of the polygon
     */
    public String toString()
    {
    	String buffer = "";
    	
    	for(int i=0; i<vertices.size(); i++){
    	  buffer += "(" + vertices.get(i).toString() + ")";
    	  if(i < vertices.size()-1) buffer += " -> ";	
    	}
    	
    	return buffer;
    }
    
    /**
     * Add a vertex to the polygon.
     *  @param v the vertex to add
     */
    public void add(Point v)
    {
      vertices.add(v);
    }
    
    /**
     * Get a vertex within the polygon.
     *  @param i the index of the vertex
     *  @return the ith vertex
     */
    public Point get(int i)
    {
      return vertices.get(i);
    }
    
    /**
     * Set a vertex within the polygon.
     * @param i the index of the vertex
     * @param v the new value of the vertex
     */
    public void set(int i, Point v)
    {
    	vertices.set(i, v);
    }
    
    /**
     * Clear the vertices currently stored in the polygon.
     */
    public void clear()
    {
    	vertices.clear();
    }
    
    /**
     * Get the vector of vertices.
     *  @return the vector of vertices
     */
    public Vector<Point> getVertices()
    {
      return vertices;
    }
    
    /**
     * Get the width and height of this polygon.
     *  @return the width/height
     */
    public Pair<Double,Double> getWidthHeight()
    {
      Point v;
      double minx = Double.MAX_VALUE;
      double maxx = -Double.MAX_VALUE;
      double miny = Double.MAX_VALUE;
      double maxy = -Double.MAX_VALUE;
      double w, h;

      for(int i=0; i<vertices.size(); i++){
        v = vertices.get(i);
        if(v.x < minx) minx = v.x;
        if(v.x > maxx) maxx = v.x;
        if(v.y < miny) miny = v.y;
        if(v.y > maxy) maxy = v.y;
      }
      
      w = maxx - minx;
      h = maxy - miny;
      
      return new Pair<Double,Double>(w, h);
    }
    
    /**
     * Translate this polygon.
     * @param tx the x-coordinate translation
     * @param ty the y-coordinate translation
     * @return the translated polygon
     */
    public Polygon plus(double tx, double ty)
    {
    	return new Polygon(Point.transform(vertices, 1, 1, tx, ty));
    }
    
    /**
     * Scale/translate this polygon.
     * @param sx the scaling factor in the x direction
     * @param xy the scaling factor in the y direction
     * @param tx the x-coordinate translation
     * @param ty the y-coordinate translation
     * @return the translated polygon
     */
    public Polygon transform(double sx, double sy, double tx, double ty)
    {
    	return new Polygon(Point.transform(vertices, sx, sy, tx, ty));
    }
    
    /**
     * Transform a polygon.
     *  @param M the transformation matrix
     *  @param p the polygon to transform
     *  @return the transformed polygon
     */
    public static Polygon transform(double[][] M, Polygon p)
    {
      Polygon q = new Polygon();
      
      for(int i=0; i<p.size(); i++){
        q.add(Point.transform(M, p.get(i)));
      }
      
      return q;
    }
    
    /**
     * Get the normal for this polygon (assumes the polygon is planar).
     *  @return the normal
     */
    public Point getNormal()
    {
    	Point n = null;
    	Point v0, v1;
    	double a, b, c;
    	
    	if(false){	//Three point method
	      if(vertices.size() > 2){
	      	n = Point.cross(vertices.get(0), vertices.get(1), vertices.get(2));
	      }
    	}else{			//Newell's method
    		a = 0;
    		b = 0;
    		c = 0;
    		
    	  for(int i=0; i<vertices.size(); i++){
    	    v0 = vertices.get(i);
    	    v1 = vertices.get((i+1)%vertices.size());
    	    
    	    a += (v0.y-v1.y)*(v0.z+v1.z);
    	    b += (v0.z-v1.z)*(v0.x+v1.x);
    	    c += (v0.x-v1.x)*(v0.y+v1.y);
    	  }
    	  
    	  n = new Point(a, b, c);
    	}
    	
      if(n != null) n.divideEquals(n.magnitude());
      
      return n;
    }
    
    /**
     * Get the area of this polygon.
     *  @return the area
     */
    public double getArea()
    {
    	Point p = new Point(0, 0, 0);
    	Point n = getNormal();
    	Point v0, v1;
    	
    	for(int i=0; i<vertices.size(); i++){
    		v0 = vertices.get(i);
    		v1 = vertices.get((i+1)%vertices.size());
    		p.plusEquals(Point.cross(v0, v1));
    	}
    	
    	return n.times(p.divide(2));
    }
    
    /**
     * Reverse the order of the given polygon.
     *  @param p the polygon to reverse
     *  @return the reversed polygon
     */
    public static Polygon reverse(Polygon p)
    {
    	Polygon pr = new Polygon();
    	
    	for(int i=p.size()-1; i>=0; i--){
    		pr.add(p.get(i));
    	}
    	
    	return pr;
    }
    
    /**
     * Get a list indicating which vertices are concave.
     *  @return a vector of boolean values indicating which vertices are concave
     */
    public Vector<Boolean> getConcaveVertexList()
    {
    	Vector<Boolean> concave = new Vector<Boolean>();
    	Point normal = getNormal();
    	Point v0, v1, v2, cp;
    	
    	for(int i=0; i<vertices.size(); i++){
    		v0 = vertices.get((i-1<0)?vertices.size()-1:i-1);
    		v1 = vertices.get(i);
    		v2 = vertices.get((i+1)%vertices.size());
    		cp = Point.cross(v0, v1, v2);
    		
    		cp.divideEquals(cp.magnitude());
    		
    		if(normal.times(cp) < 0){
    			concave.add(true);
    		}else{
    			concave.add(false);
    		}
    	}
    	
    	if(false){		//Check if the ordering of the vertices as clock wise (i.e. reversed)
	    	int count = 0;
	    	
	    	for(int i=0; i<concave.size(); i++){
	    	  if(concave.get(i)) count++;	
	    	}
	    	
	    	//If reversed then flip values
	    	if(count == concave.size()/2.0) System.out.println("Warning: ambiguous polygon.");
	
	    	if(count > concave.size()/2.0){
	    		for(int i=0; i<concave.size(); i++){
	    			concave.set(i, !concave.get(i));
	    		}
	    	}
    	}
    	    	
    	return concave;
    }
    
    /**
     * Determine if the given polygon is convex.
     * 	@return true if the polygon is convex
     */
    public boolean isConvex()
    {
    	Vector<Boolean> concave = getConcaveVertexList();
    	
    	for(int i=1; i<concave.size(); i++){
    		if(concave.get(i) != concave.get(i-1)){
    			return false;
    		}
    	}
    	
    	return true;
    }
    
    /**
     * Return a face representing each edge of the polygon.
     *  @return the faces representing the edges
     */
    public Vector<Face> getEdgeFaces()
    {
    	Vector<Face> faces = new Vector<Face>();
    	
    	for(int i=0; i<vertices.size(); i++){
    		faces.add(new Face(i, (i+1)%vertices.size()));
    	}
    	
    	return faces;
    }
    
    /**
     * Triangulate the polygon, assuming it to be convex.
     *  @return the faces of the triangulated polygon
     */
    public Vector<Face> getConvexPolygonFaces()
    {
      Vector<Face> faces = new Vector<Face>();
      
      for(int i=0; i<vertices.size()-2; i++){
        Face tmpf = new Face();
        tmpf.v[0] = 0;
        tmpf.v[1] = i + 1;
        tmpf.v[2] = i + 2;
        faces.add(tmpf);
      }
      
      return faces;
    }
    
    /**
     * Triangulate the polygon, assuming it to be simple.
     *  @return the faces of the triangulated polygon
     */
    public Vector<Face> getSimplePolygonFaces()
    {
    	Vector<Face> faces = new Vector<Face>();
    	LinkedList<Point> polygon_vertices = new LinkedList<Point>(vertices);
    	LinkedList<Integer> indices = new LinkedList<Integer>();
      ListIterator<Point> vertex_itr;
      ListIterator<Integer> index_itr;
      ListIterator<Point> itrccv;
      Point v0, v1, v2, ccv;
      int i0, i1, i2;
      boolean FOUND_EAR, FOUND_INTERNAL_VERTEX;
      
      //Setup up index list
      for(int i=0; i<vertices.size(); i++){
      	indices.add(i);
      }
      
      while(polygon_vertices.size() > 2){
      	//Update concave vertices
      	Vector<Boolean> concave = (new Polygon(polygon_vertices)).getConcaveVertexList();
      	Vector<Point> concave_vertices = new Vector<Point>();
      	vertex_itr = polygon_vertices.listIterator();
      	
      	for(int i=0; i<concave.size(); i++){
      	  if(concave.get(i)){
      	  	concave_vertices.add(vertex_itr.next());
      	  }else{
      	  	vertex_itr.next();
      	  }
      	}  
      	
      	//Setup iterators
      	v1 = polygon_vertices.getLast();
      	i1 = indices.getLast();
      	vertex_itr = polygon_vertices.listIterator();
      	index_itr = indices.listIterator();
      	
      	//Find an ear
      	FOUND_EAR = false;
      	
      	for(int i=0; i<concave.size(); i++){
      		v0 = v1;
      		i0 = i1;
      		v1 = vertex_itr.next();
      		i1 = index_itr.next();
      		
      		if(!concave.get(i)){			//If this vertex is convex
  	    		if(vertex_itr.hasNext()){
  	    			v2 = vertex_itr.next();
  	    			vertex_itr.previous(); vertex_itr.previous(); vertex_itr.next();
  	    			i2 = index_itr.next();
  	    			index_itr.previous(); index_itr.previous(); index_itr.next();
  	    		}else{
  	    			v2 = polygon_vertices.getFirst();
  	    			i2 = indices.getFirst();
  	    		}
      		
  	    		//Search for concave vertices inside this potential ear
  	    		FOUND_INTERNAL_VERTEX = false;		//Internal ConCave Vertex
  	    		itrccv = concave_vertices.listIterator();
  	    		
  	    		while(itrccv.hasNext()){
  	    			ccv = itrccv.next();
  	    			
  	    			if(v0 != ccv && v1 != ccv && v2 != ccv){
	  	    			if(ccv.isWithin(v0, v1, v2)){
	  	    				FOUND_INTERNAL_VERTEX = true;
	  	    				break;
	  	    			}
  	    			}
  	    		}
  	    		
  	    		if(!FOUND_INTERNAL_VERTEX){	//Found an ear!
  	    			faces.add(new Face(i0, i1, i2));
  	    			FOUND_EAR = true;
  	    			
  	    			vertex_itr.remove();
  	    			index_itr.remove();
    	    		
    	    		break;
  	    		}
      		}
      	}
      	
      	if(!FOUND_EAR){
		      //System.out.println("Warning: no ears found on polygon!");
		      //faces.clear();
		      break;
      	}
      }
      
      return faces;
    }
    
    /**
     * A method to display debugging information for the Polygon methods.
     */
    public static void debug()
    {
    	Polygon p;
    	
    	Polygon p_convex_ccw = new Polygon();
    	p_convex_ccw.add(new Point(0, 0, 0));
    	p_convex_ccw.add(new Point(10, 0, 0));
    	p_convex_ccw.add(new Point(10, 5, 0));
    	p_convex_ccw.add(new Point(0, 5, 0));
    	
    	Polygon p_concave_ccw = new Polygon();
    	p_concave_ccw.add(new Point(0, 0, 0));
    	p_concave_ccw.add(new Point(10, 0, 0));
    	p_concave_ccw.add(new Point(10, 5, 0));
    	p_concave_ccw.add(new Point(5, 1, 0));
    	p_concave_ccw.add(new Point(0, 5, 0));
    	
    	Polygon p_star_ccw = new Polygon();
    	p_star_ccw.add(new Point(  0,   5, 0));
    	p_star_ccw.add(new Point( -5,   0, 0));
    	p_star_ccw.add(new Point(-10,   0, 0));
    	p_star_ccw.add(new Point( -5,  -5, 0));
    	p_star_ccw.add(new Point( -5, -10, 0));
    	p_star_ccw.add(new Point(  0,  -5, 0));
    	p_star_ccw.add(new Point(  5, -10, 0));
    	p_star_ccw.add(new Point(  5,  -5, 0));
    	p_star_ccw.add(new Point( 10,   0, 0));
    	p_star_ccw.add(new Point(  5,   0, 0));

     	Polygon p_convex_cw = Polygon.reverse(p_convex_ccw);
     	Polygon p_concave_cw = Polygon.reverse(p_concave_ccw);
     	Polygon p_star_cw = Polygon.reverse(p_star_ccw);

     	if(false){		//Miscallaneous tests
	    	System.out.println(p_convex_ccw.getNormal().toString());
	    	System.out.println(p_convex_ccw.getArea());
	    	System.out.println(p_convex_cw.getArea());
	    	System.out.println(p_concave_ccw.getArea());
	    	System.out.println(p_concave_cw.getArea());
     	}
    	
    	if(false){		//Area test
	     	p = Polygon.transform(Point.rotateToZ(Point.normalize(new Point(1, 1, 1))),p_concave_ccw);
	     	System.out.println(p.getArea());
    	}
     	
     	if(true){		//Concave vertex test
	     	p = Polygon.transform(Point.rotateToZ(Point.normalize(new Point(1, 1, 1))), p_star_ccw);
	     	Vector<Boolean> concave = p.getConcaveVertexList();
	     	
	     	for(int i=0; i<concave.size(); i++){
	     		System.out.print(concave.get(i) + " ");
	     	}
	     	
	     	System.out.println();
    	}
     	
     	if(true){			//Tesselation test
	     	p = Polygon.transform(Point.rotateToZ(Point.normalize(new Point(1, 1, 1))), p_star_ccw);
	     	Vector<Face> faces = p.getSimplePolygonFaces();

	     	for(int i=0; i<faces.size(); i++){
	     		System.out.print(i + ": ");
	     		
	     		for(int j=0; j<faces.get(i).v.length; j++){
	     			System.out.print(faces.get(i).v[j] + " ");
	     		}
	     		
	     		System.out.println();
	     	}
     	}
    }
  }
  
  /**
   * A group of polygons.
   */
  public static class PolygonGroup
  {
    private Vector<Polygon> group;
    
    /**
     * Class constructor.
     */
    public PolygonGroup()
    {
      group = new Vector<Polygon>();
    }
    
    /**
     * Class constructor.
     *  @param group the initial collection of polygons
     */
    public PolygonGroup(Collection<Polygon> group)
    {
      this.group = new Vector<Polygon>(group);
    }
    
    /**
     * Class constructor, deep copy.
     *  @param polygon an initial polygon to add to the group
     */
    public PolygonGroup(Polygon polygon)
    {
    	group = new Vector<Polygon>();
    	group.add(new Polygon(polygon));
    }
   
    /**
     * Class copy constructor, deep copy.
     *  @param g the group to copy
     */
    public PolygonGroup(PolygonGroup g)
    {
    	group = new Vector<Polygon>();
    	
    	for(int i=0; i<g.size(); i++){
    		group.add(new Polygon(g.get(i)));
    	}
    }
    
    /**
     * Get the number of polygons stored in this group.
     *  @return the number of polygons
     */
    public int size()
    {
      return group.size();
    }
    
    /**
     * Check if the group is empty.
     *  @return true if the group is empty
     */
    public boolean isEmpty()
    {
      return group.isEmpty();
    }
    
    /**
     * Add a polygon to the group.
     *  @param p the polygon to add
     */
    public void add(Polygon p)
    {
      group.add(p);
    }
    
    /**
     * Get a polygon from the group.
     *  @param i the index of the polygon to get
     *  @return the ith polygon
     */
    public Polygon get(int i)
    {
      return group.get(i);
    }
    
    /**
     * Set a polygon in the group.
     *  @param i the index of the polygon
     *  @param p the new polygon to set at this index
     */
    public void set(int i, Polygon p)
    {
    	group.set(i, p);
    }
    
    /**
     * Get the vector of polygons.
     *  @return the vector of polygons
     */
    public Vector<Polygon> getPolygons()
    {
      return group;
    }
    
    /**
     * Transform a polygon group.
     *  @param M the transformation matrix
     *  @param p the polygon group to transform
     *  @return the transformed polygon group
     */
    public static PolygonGroup transform(double[][] M, PolygonGroup p)
    {
      PolygonGroup q = new PolygonGroup();
      
      for(int i=0; i<p.size(); i++){
        q.add(Polygon.transform(M, p.get(i)));
      }
      
      return q;
    }
    
    /**
     * This is a pre-processing function for edgesToPolygons that removes edges that are not connected
     * to other edges on both sides (i.e. dead ends).
     *  @param edges the list of edges
     *  @return the list of edges after pruning
     */
    private static LinkedList<LineSegment> pruneDeadEnds(LinkedList<LineSegment> edges)
    {
      LinkedList<LineSegment> edges_new;
      Iterator<LineSegment> itr1;
      Iterator<LineSegment> itr2;
      LineSegment e1;
      LineSegment e2;
      boolean FOUND_FIRST, FOUND_SECOND;
      boolean PRUNE = true;
      
      while(PRUNE){
        PRUNE = false;
        edges_new = new LinkedList<LineSegment>();
        itr1 = edges.iterator();
        
        while(itr1.hasNext()){
          e1 = itr1.next();
          itr2 = edges.iterator();
          FOUND_FIRST = false;
          FOUND_SECOND = false;
          
          while(itr2.hasNext()){
            e2 = itr2.next();
            
            if(e1 != e2){
              if(e1.v0 == e2.v0 || e1.v0 == e2.v1) FOUND_FIRST = true;
              if(e1.v1 == e2.v0 || e1.v1 == e2.v1) FOUND_SECOND = true;
              if(FOUND_FIRST && FOUND_SECOND) break;
            }
          }
          
          if(FOUND_FIRST && FOUND_SECOND){
            edges_new.add(e1);
          }else{
            PRUNE = true;
          }
        }
        
        edges = edges_new;
      }
      
      return edges;
    }
    
    /**
     * Convert a vector of edges into polygons.
     *  @param edges the vector of edges to connect
     *  @return a number of vectors of ordered vertices representing a polygon
     */
    public static PolygonGroup edgesToPolygons(LinkedList<LineSegment> edges)
    {
      PolygonGroup polygons = new PolygonGroup();
      Polygon polygon;
      LinkedList<Point> loop = new LinkedList<Point>();
      LineSegment edge;
      Point vertex;
      boolean PROGRESS;
            
      while(!edges.isEmpty()){
        loop.add(edges.getFirst().v0);
        loop.add(edges.getFirst().v1);
        edges.removeFirst();
      
        //Follow edges around polygon
        while(loop.getFirst() != loop.getLast()){
          if(edges.isEmpty()){
            System.out.println(" ! Warning: edgesToPolygons -> open polygon!");
            break;
          }
          
          Iterator<LineSegment> itr = edges.iterator();
          PROGRESS = false;
                    
          while(itr.hasNext()){
            edge = itr.next();
            
            if(loop.getFirst() == edge.v0){
              loop.addFirst(edge.v1);
              itr.remove();
              PROGRESS = true;
            }else if(loop.getFirst() == edge.v1){
              loop.addFirst(edge.v0);
              itr.remove();
              PROGRESS = true;
            }else if(loop.getLast() == edge.v0){
              loop.addLast(edge.v1);
              itr.remove();
              PROGRESS = true;
            }else if(loop.getLast() == edge.v1){
              loop.addLast(edge.v0);
              itr.remove();
              PROGRESS = true;
            }
          }
          
          if(!PROGRESS){
            System.out.println(" ! Warning: edgesToPolygons -> infinite loop!");
            break;
          }
        }
        
        //Read off list into a vector and store
        polygon = new Polygon();
        Iterator<Point> itr = loop.iterator();
        
        while(itr.hasNext()){
          vertex = itr.next();
          polygon.add(new Point(vertex.x, vertex.y, 0));
        }
        
        polygons.add(polygon);
        loop.clear();
      }
      
      return polygons;
    }
    
    /**
     * Return the intersection of two polygon groups. Note: must be planar and lie on the XY plane!
     *  @param pga the first polygon group
     *  @param pgb the second polygon group
     *  @return the intersection of the two polygon groups
     */
    public static PolygonGroup getPlanarIntersection(PolygonGroup pga, PolygonGroup pgb)
    {
      PolygonGroup pgc = new PolygonGroup();
      Point p0, p1, vi;
      Stack<LineSegment> stack = new Stack<LineSegment>();
      LinkedList<LineSegment> edges_a = new LinkedList<LineSegment>();
      LinkedList<LineSegment> edges_b = new LinkedList<LineSegment>();
      LinkedList<LineSegment> ea_parts = new LinkedList<LineSegment>();
      LinkedList<LineSegment> eb_parts = new LinkedList<LineSegment>();
      LinkedList<LineSegment> edges = new LinkedList<LineSegment>();
      Iterator<LineSegment> itra, itrb;
      LineSegment ea, eb;
      Point vmid;
          
      for(int i=0; i<pga.size(); i++){      	
        for(int j=0; j<pga.get(i).size(); j++){
          p0 = pga.get(i).get(j);
          p1 = pga.get(i).get((j+1)%pga.get(i).size());
          stack.push(new LineSegment(p0, p1));
        }
      }
          
      for(int i=0; i<pgb.size(); i++){
        for(int j=0; j<pgb.get(i).size(); j++){
          p0 = pgb.get(i).get(j);
          p1 = pgb.get(i).get((j+1)%pgb.get(i).size());
          edges_b.add(new LineSegment(p0, p1));
        }
      }
      
      //Build intersection-less list of edges for each polygon group
      while(!stack.isEmpty()){
        ea_parts.add(stack.pop());
        itrb = edges_b.iterator();
        
        while(itrb.hasNext()){
          eb = itrb.next();
          itra = ea_parts.iterator();
          
          //Check for an intersection between the parts of ea and this edge in polygon B.
          while(itra.hasNext()){
            ea = itra.next();
            vi = Point.getPlanarSegmentIntersection(ea.v0, ea.v1, eb.v0, eb.v1);
            
            if(vi != null){
            	itra.remove();            
              itrb.remove();
              
              ea_parts.add(new LineSegment(ea.v0, vi));
              ea_parts.add(new LineSegment(vi, ea.v1));
              eb_parts.add(new LineSegment(eb.v0, vi));
              eb_parts.add(new LineSegment(vi, eb.v1));
              break;  //Can only be one itersection among ea parts since they came form the same edge!
            }
          }
        }
          
        //Add the parts of ea to the list of edges for polygon group A
        itra = ea_parts.iterator();
        
        while(itra.hasNext()){
          edges_a.add(itra.next());
        }
        
        //Add newly split edges to list of edges for polygon group B
        itrb = eb_parts.iterator();
        
        while(itrb.hasNext()){
          edges_b.add(itrb.next());
        }
        
        ea_parts.clear();
        eb_parts.clear();
      }
      
      //Check if edges are within the other polygon
      itra = edges_a.iterator();
      
      while(itra.hasNext()){
        ea = itra.next();
        vmid = new Point((ea.v0.x+ea.v1.x)/2.0, (ea.v0.y+ea.v1.y)/2.0, 0);
        if(vmid.isWithin(pgb)) edges.add(ea);
      }
      
      itrb = edges_b.iterator();
      
      while(itrb.hasNext()){
        eb = itrb.next();
        vmid = new Point((eb.v0.x+eb.v1.x)/2.0, (eb.v0.y+eb.v1.y)/2.0, 0);
        if(vmid.isWithin(pga)) edges.add(eb);
      }
      
      edges = pruneDeadEnds(edges);
      pgc = edgesToPolygons(edges);
      
      return pgc;
    }
    
    /**
     * Get the intersection of the two given planar polygons using the Sutherland-Hodgkins method.
     * Note: must be in the XY plane
     *  @param cp a convex clipping polygon
     *  @param p an arbitrary polygon that will be clipped by the clipping polygon
     *  @return the intersection of the two polygons
     */
    public static PolygonGroup getPlanarIntersection(Polygon cp, Polygon p)
    {
    	Polygon tmpp = new Polygon();
    	Point cp0, cp1, p0, p1;
    	double a, b ,c, d, e, f, denom, x, y;
    	boolean P0_INSIDE, P1_INSIDE;
    	
    	for(int i=0; i<cp.size(); i++){
    	  cp0 = cp.get(i);
    	  cp1 = cp.get((i+1)%cp.size());
    	  
    	  a = cp1.y - cp0.y;
    	  b = cp0.x - cp1.x;
    	  c = -a*cp0.x - b*cp0.y;
    	  
    	  for(int j=0; j<p.size(); j++){
    	  	p0 = p.get(j);
    	  	p1 = p.get((j+1)%p.size());
    	  	
    	  	P0_INSIDE = a*p0.x + b*p0.y + c > 0;
    	  	P1_INSIDE = a*p1.x + b*p1.y + c > 0;

    	  	if(P0_INSIDE && P1_INSIDE){						//Inside to inside
    	  		tmpp.add(new Point(p1.x, p1.y, 0));
    	  	}else{
  	  		 	d = p1.y - p0.y;
	    	  	e = p0.x - p1.x;
	    	  	f = -d*p0.x - e*p0.y;
	    	  	denom = e*a - b*d;
	    	  	x = (b*f - e*c) / denom;
	    	  	y = (d*c - a*f) / denom; 
    	  		
	    	  	if(P0_INSIDE && !P1_INSIDE){				//Inside to outside
	    	  		tmpp.add(new Point(x, y, 0));
	    	  	}else if(!P0_INSIDE && P1_INSIDE){	//Outside to inside
	    	  		tmpp.add(new Point(x, y, 0));
	    	  		tmpp.add(new Point(p1.x, p1.y, 0));
	    	  	}
    	  	}
    	  }
    	}

    	return new PolygonGroup(tmpp);
    }
    
    /**
     * Return the intersection of two polygons.
     *  @param f the first polygon (MUST be planar!)
     *  @param p the second polygon
     *  @param threshold the threshold used to decide if two line segments in space intersect
     *  @return the intersection of the two polygon groups
     */
  	public static PolygonGroup getIntersection(Polygon f, Polygon p, double threshold)
  	{
  		Point N;  		
  		double[][] M, Mi;
  		Polygon Mf;
  		Polygon Mp;
  		Polygon Mpp = new Polygon();
  		Vector<Boolean> IN_PLANE = new Vector<Boolean>();
  		
  		//Transform polygon vertices to face plane
  		N = f.getNormal();
  		M = Point.rotateToZ(N);
  		M = GMatrixUtility.translate(M, -f.get(0).x, -f.get(0).y, -f.get(0).z);
  		Mi = GMatrixUtility.inverse(M);
  		Mf = Polygon.transform(M, new Polygon(f));
  		Mp = Polygon.transform(M, new Polygon(p));
  		  		
  		//Check which polygon vertices are actually on the face plane
  		for(int i=0; i<Mp.size(); i++){
  			if(Math.abs(Mp.get(i).z) < threshold){
  				IN_PLANE.add(true);
  			}else{
  				IN_PLANE.add(false);
  			}
  		}
  		
  		//Create a planar version of the polygon
  		for(int i=0; i<Mp.size(); i++){
  			if(IN_PLANE.get(i)){
  				Mpp.add(new Point(Mp.get(i)));	//TODO: Interpolate edge to find exact point where it leaves threshold of plane.
  				Mpp.get(Mpp.size()-1).z = 0;
  			}
  		}
  		
  		//return PolygonGroup.transform(Mi, PolygonGroup.getPlanarIntersection(new PolygonGroup(Mf), new PolygonGroup(Mpp)));
  		return PolygonGroup.transform(Mi, PolygonGroup.getPlanarIntersection(Mf, Mpp));
  	}
  }
  
  /**
   * A structure to store and modify a 4x4 matrix representing a camera.
   */
  public static class Camera
  {
    private double[][] K = MatrixUtility.eye(4);    //Internal parameters matrix (e.g. K, viewport)
    private double[][] RT = MatrixUtility.eye(4);   //External parameters matrix (e.g. RT, modelview)
    private double[][] RTi = MatrixUtility.eye(4);  //Inverse of external parameters matrix
    private double[][] M = null;                  //M = K*RT
    private double[][] Mi = null;                 //Inverse of M
    public String name = "";
    
    public Camera() {}
    
    /**
     * Class constructor.
     * @param K the internal camera transformation
     * @param RT the external camera transformation
     */
    public Camera(double[][] K, double[][] RT)
    {
    	this.K = K;
    	this.RT = RT;
    	
    	//Update other matrices
      RTi = GMatrixUtility.inverse(RT);
      M = GMatrixUtility.transform(K, RT);
      Mi = GMatrixUtility.inverse(M);
    }
    
    /**
     * Get the interal camera transformation.
     *  @return the K matrix
     */
    public double[][] getK()
    {
      return K;
    }
    
    /**
     * Get the external camera transformation
     *  @return the modelview matrix
     */
    public double[][] getRT()
    {
      return RT;
    }
    
    /**
     * Get the inverse of the external camera transformation.
     *  @return the inverse of the modelview matrix
     */
    public double[][] getRTi()
    {
      return RTi;
    }
    
    /**
     * Get the camera matrix
     *  @return the camera matrix
     */
    public double[][] getM()
    {
      return M;
    }
    
    /**
     * Get the inverse of the camera matrix.
     *  @return the inverse of the camera matrix
     */
    public double[][] getMi()
    {
      return Mi;
    }
    
    /**
     * Get the cameras position.
     *  @return the vertex representing the cameras position
     */
    public Point getPosition()
    {
      Point p = new Point(0, 0, 0);
      p = Point.transform(RTi, p);
      
      return p;
    }
    
    /**
     * Get a ray emanating from the camera through the given image point.
     *  @param x the x coordinate in the image
     *  @param y the y coordinate in the image
     *  @return the ray through this point in the world coordinate frame
     */
    public Utility.Pair<Point,Point> getRay(int x, int y)
    {
      Utility.Pair<Point,Point> r = new Utility.Pair<Point,Point>();
      Point p = new Point(0, 0, 0);
      Point q = new Point(((double)x)+0.5, ((double)y)+0.5, 0.0);
      Point d = new Point();

      p = Point.transform(RTi, p);
      q = Point.transform(Mi, q);
      d = q.minus(p);
      d.divideEquals(d.magnitude());
      
      r.first = p;
      r.second = d;
      
      return r;
    }
    
    /**
     * Get a ray emanating from the camera through the given image point.
     *  @param x the x coordinate in the image
     *  @param y the y coordinate in the image
     *  @return the ray through this point in the world coordinate frame
     */
    public Utility.Pair<Point,Point> getRay(double x, double y)
    {
      Utility.Pair<Point,Point> r = new Utility.Pair<Point,Point>();
      Point p = new Point(0, 0, 0);
      Point q = new Point(x, y, 0.0);
      Point d = new Point();

      p = Point.transform(RTi, p);
      q = Point.transform(Mi, q);
      d = q.minus(p);
      d.divideEquals(d.magnitude());
      
      r.first = p;
      r.second = d;
      
      return r;
    }
    
    /**
     * Set the internal camera matrix.
     *  @param A the internal matrix
     */
    public void setK(double[][] A)
    {
      //Copy the matrix values over
      for(int j=0; j<4; j++){
        for(int i=0; i<4; i++){
          K[j][i] = A[j][i];
        }
      }
      
      //Update other matrices
      M = GMatrixUtility.transform(K, RT);
      Mi = GMatrixUtility.inverse(M);
    }
    
    /**
     * Set the internal camera matrix.
     *  @param sx the scaling in the x direction
     *  @param sy the scaling in the y direction
     *  @param cx the center pixel in the x direction
     *  @param cy the center pixel in the y direction
     */
    public void setK(double sx, double sy, double cx, double cy)
    {
      //Build the transformation matrix
      K[0][0] = sx;
      K[0][1] = 0;
      K[0][2] = cx;
      K[0][3] = 0;

      K[1][0] = 0;
      K[1][1] = sy;
      K[1][2] = cy;
      K[1][3] = 0;
      
      K[2][0] = 0;
      K[2][1] = 0;
      K[2][2] = 1;
      K[2][3] = 1;
      
      K[3][0] = 0;
      K[3][1] = 0;
      K[3][2] = 1;
      K[3][3] = 0;
      
      //Update other matrices
      M = GMatrixUtility.transform(K, RT);
      Mi = GMatrixUtility.inverse(M);
    }
    
    /**
     * Set the external camera matrix.
     *  @param A the external matrix
     */
    public void setRT(double[][] A)
    {
      //Copy the matrix values over
      for(int j=0; j<4; j++){
        for(int i=0; i<4; i++){
          RT[j][i] = A[j][i];
        }
      }
      
      //Update other matrices
      RTi = GMatrixUtility.inverse(RT);
      M = GMatrixUtility.transform(K, RT);
      Mi = GMatrixUtility.inverse(M);
    }
    
    /**
     * Set the external camera matrix.
     *  @param center a 3D point/array representing the position of the camera
     *  @param forward a 3D array representing the forward looking direction of the camera
     *  @param up a 3D array representing the up direction of the camera
     */
    public void setRT(double[] center, double[] forward, double[] up)
    {
      //Calculate right direction vector
      double[] right = MatrixUtility.cross(forward, up);
      
      //Build the transformation matrix
      RT[0][0] = right[0];
      RT[0][1] = right[1];
      RT[0][2] = right[2];
      RT[0][3] = center[0];

      RT[1][0] = up[0];
      RT[1][1] = up[1];
      RT[1][2] = up[2];
      RT[1][3] = center[1];
      
      RT[2][0] = forward[0];
      RT[2][1] = forward[1];
      RT[2][2] = forward[2];
      RT[2][3] = center[2];
      
      RT[3][0] = 0;
      RT[3][1] = 0;
      RT[3][2] = 0;
      RT[3][3] = 1;
      
      //Update other matrices
      RTi = GMatrixUtility.inverse(RT);
      M = GMatrixUtility.transform(K, RT);
      Mi = GMatrixUtility.inverse(M);
    }
  
    /**
     * Set the external camera matrix.
     *  @param rx rotation in degrees about the x-axis
     *  @param ry rotation in degrees about the y-axis
     *  @param rz rotation in degrees about the z-axis
     *  @param tx translation in the x-axis
     *  @param ty translation in the y-axis
     *  @param tz translation in the z-axis
     */
    public void setRT(double rx, double ry, double rz, double tx, double ty, double tz)
    {
      rx = Math.PI * rx / 180.0;
      ry = Math.PI * ry / 180.0;
      rz = Math.PI * rz / 180.0;
  
      double c1 = Math.cos(rz);
      double c2 = Math.cos(ry);
      double c3 = Math.cos(rx);
      double s1 = Math.sin(rz);
      double s2 = Math.sin(ry);
      double s3 = Math.sin(rx);
      
      RT[0][0] = c1*c2;
      RT[0][1] = -c2*s1;
      RT[0][2] = s2;
      RT[0][3] = tx;

      RT[1][0] = c3*s1 + c1*s2*s3;
      RT[1][1] = c1*c3 - s1*s2*s3;
      RT[1][2] = -c2*s3;
      RT[1][3] = ty;
      
      RT[2][0] = s1*s3 - c1*c3*s2;
      RT[2][1] = c3*s1*s2 + c1*s3;
      RT[2][2] = c2*c3;
      RT[2][3] = tz;
      
      RT[3][0] = 0;
      RT[3][1] = 0;
      RT[3][2] = 0;
      RT[3][3] = 1;
      
      //Update other matrices
      RTi = GMatrixUtility.inverse(RT);
      M = GMatrixUtility.transform(K, RT);
      Mi = GMatrixUtility.inverse(M);
    }
  
    /**
     * Orient the camera so it faces the given group of points.
     * @param points points in the scene
     */
    public void orientTowards(Vector<Point> points)
    {
    	Pair<double[][],double[][]> tmpp;
    	tmpp = orientTowards(K, RT, points); K = tmpp.first; RT = tmpp.second;
    	
    	//Update other matrices
      RTi = GMatrixUtility.inverse(RT);
      M = GMatrixUtility.transform(K, RT);
      Mi = GMatrixUtility.inverse(M);
    }
    
    /**
     * Orient a camera towards a group of points.
     * @param K the internal parameter matrix of a camera
     * @param RT the external parameter matrix of a camera
     * @param points the group of points to face
     * @return the possibly modified camera matrices
     */
    public static Pair<double[][],double[][]> orientTowards(double[][] K, double[][] RT, Vector<Point> points)
    {
    	double[][] RTi = GMatrixUtility.inverse(RT);
    	Point centroid = Point.getCentroid(points);
    	Point center = Point.transform(RTi, new Point(0, 0, 0));
    	Point forward = Point.transform(RTi, new Point(0, 0, -1));    	
    	Point backward = Point.transform(RTi, new Point(0, 0, 1));
    	Point up, right;
    	double forward_distance, backward_distance;

    	forward_distance = forward.distance(centroid);
    	backward_distance = backward.distance(centroid);
    	//System.out.println("forward: " + forward_distance + ", backward: " + backward_distance);
    	
    	if(backward_distance < forward_distance){
    		System.out.println("Flipping camera!");
    		
    		forward.minusEquals(center);
    		forward.normalize();
    		backward.minusEquals(center);
    		backward.normalize();
    		
    		up = Point.transform(RTi, new Point(0, 1, 0));
    		up.minusEquals(center);
    		up.normalize();
    		
    		right = Point.cross(forward, up);
    		right.normalize();

    		RT = MatrixUtility.rotateCamera(backward.toArray(), up.toArray(), right.toArray());
  			center = Point.transform(RT, center);
    		RT[0][3] = -center.x;
    		RT[1][3] = -center.y;
    		RT[2][3] = -center.z;
    		
    		//Why do we need to do this?
    		K[0][0] = -K[0][0];
    	}
    	
    	return new Pair<double[][],double[][]>(K,RT);
    }

    /**
     * Apply a viewport transformaton to the given projection matrix.
     * @param projection the projection matrix
     * @param halfwidth the halfwidth of the viewportimage
     * @param halfheight the halfheight of the viewport/image
     * @return the resulting projection matrix
     */
    public static double[][] applyViewport(double[][] projection, int halfwidth, int halfheight)
    {
    	double[][] viewport = new double[4][4];
      double[][] K;
          
      viewport[0][0] = halfwidth;
      viewport[0][3] = halfwidth;
      viewport[1][1] = -halfheight;
      viewport[1][3] = halfheight;
      viewport[2][2] = 1;
      viewport[3][3] = 1;
          
      K = GMatrixUtility.transform(viewport, projection);
      
      if(true && K[3][2]!=0){   //Apply conventions if not orthographic!
        //Make sure we divide by a positive z
        K[0][0] /= K[3][2]; 
        K[0][2] /= K[3][2]; 
        K[1][1] /= K[3][2]; 
        K[1][2] /= K[3][2]; 
        K[3][2] /= K[3][2]; 
    
        //These values should not matter so set them to 1
        K[2][2] = 1;
        K[2][3] = 1;
      }
      
    	return K;
    }
    
		/**
     * Load 2D/3D points from a file.
     * @param filename the file with the point information
     * @return a vector of vertices
     */
    public static Vector<Point> loadPoints(String filename)
    {
    	Vector<Point> vertices = new Vector<Point>();
    	Point tmpv;
    	
    	try{
	      BufferedReader ins = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
	      String[] tokens;
	      String line;
	      
	      while((line=ins.readLine()) != null){
      		tokens = line.trim().split("\\s+");
      		
      		tmpv = new Point();
      		tmpv.x = Double.valueOf(tokens[0]);
      		tmpv.y = Double.valueOf(tokens[1]);
      		if(tokens.length > 2) tmpv.z = Double.valueOf(tokens[2]);
      		vertices.add(tmpv);
	      }
    	}catch(Exception e) {e.printStackTrace();}
    	
    	return vertices;
    }
    
    /**
     * Load camera matrices from a file.
     *  @param filename the file with the camera information
     *  @return the camera structures
     */
    public static Vector<Camera> loadCameras(String filename)
    {
      /*
       * Format convention:
       *  cN i m m1 m2 m3 ... m16
       *  cN i p sx sy cx cy
       *  cN e m m1 m2 m3 ... m16
       *  cN e v cx cy cz fx fy fz ux uy uz
       *  cN e l cx cy cz lx ly lz ux uy uz
       *  cN e p rx ry rz tx ty tz
       */
      
      TreeMap<String,Camera> cameras = new TreeMap<String,Camera>();
      Vector<String> tokens;
      String line;
      String name;
      double[][] M = new double[4][4];
      int at;
      
      try{    
        BufferedReader ins = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));

        while((line=ins.readLine()) != null){
          tokens = Utility.split(line, ' ', true);
          
          //Allocate space for this camera if not currently allocated        
          name = tokens.get(0);
          
          if(name.charAt(0) != '#'){
            if(cameras.get(name) == null){
              cameras.put(name, new Camera());
            }
     
            cameras.get(name).name = name;
            
            //Load parameters for this camera
            if(tokens.get(1).equals("i")){          //Internal parameters
              if(tokens.get(2).equals("m")){        //Matrix values (in row major order)
                at = 3;
                
                for(int j=0; j<4; j++){
                  for(int i=0; i<4; i++){
                    M[j][i] = Double.valueOf(tokens.get(at++));
                  }
                }
                
                cameras.get(name).setK(M);
              }else if(tokens.get(2).equals("p")){  //Parameters
                at = 3;
                double sx = Double.valueOf(tokens.get(at++));
                double sy = Double.valueOf(tokens.get(at++));
                double cx = Double.valueOf(tokens.get(at++));
                double cy = Double.valueOf(tokens.get(at++));
                
                cameras.get(name).setK(sx, sy, cx, cy);
                //System.out.print("\n" + MatrixUtils.toString(cameras.get(name).getK()));
              }
            }else if(tokens.get(1).equals("e")){    //External parameters
              if(tokens.get(2).equals("m")){        //Matrix values (in row major order)
                at = 3;
                
                for(int j=0; j<4; j++){
                  for(int i=0; i<4; i++){
                    M[j][i] = Double.valueOf(tokens.get(at++));
                  }
                }
                
                cameras.get(name).setRT(M);
              }else if(tokens.get(2).equals("v") || tokens.get(2).equals("l")){  //Vector/Look-At point directions
                at = 3;
                double[] center = {Double.valueOf(tokens.get(at++)), Double.valueOf(tokens.get(at++)), Double.valueOf(tokens.get(at++))};
                double[] forward = {Double.valueOf(tokens.get(at++)), Double.valueOf(tokens.get(at++)), Double.valueOf(tokens.get(at++))};
                double[] up = {Double.valueOf(tokens.get(at++)), Double.valueOf(tokens.get(at++)), Double.valueOf(tokens.get(at++))};
                
                //Make sure up vector is normalizes
                up = MatrixUtility.divide(up, MatrixUtility.norm(up));
                
                //Convert a look-at point to a forward direction vector
                if(tokens.get(2).equals("l")){
                  forward = MatrixUtility.minus(forward, center);
                  forward = MatrixUtility.divide(forward, MatrixUtility.norm(forward));
                }
                
                cameras.get(name).setRT(center, forward, up);
              }else if(tokens.get(2).equals("p")){  //Parameters, Euler angles/translations
                at = 3;
                double rx = Double.valueOf(tokens.get(at++));
                double ry = Double.valueOf(tokens.get(at++));
                double rz = Double.valueOf(tokens.get(at++));
                double tx = Double.valueOf(tokens.get(at++));
                double ty = Double.valueOf(tokens.get(at++));
                double tz = Double.valueOf(tokens.get(at++));
                
                cameras.get(name).setRT(rx, ry, rz, tx, ty, tz);
                //System.out.print("\n" + MatrixUtils.toString(cameras.get(name).getRT()));
              }
            }
          }
        }
      }catch(Exception e){
        //e.printStackTrace();
      }
      
      return new Vector<Camera>(cameras.values());
    }
    
	  /**
	   * Build camera matrices from images in this folder.
	   *  @param path the folder with the camera information
	   */
	  public static void constructCameras(String path)
	  {
	  	String output = "";
	    File[] files = (new File(path)).listFiles();
	    Vector<String> imagenames = new Vector<String>();
	    String filename;
	    BufferedImage image;
	    int w, h;
	    int[] pixels;
	    ImageViewer viewer = new ImageViewer();
	    
	    Vector<Vector<Point>> axis_points;
	    boolean FOUND_AXIS_POINTS = Utility.exists(path + "axis_points.txt");
	    Vector<Point> P, p;
	    Pair<double[][],double[][]> tmpp;
	    double[][] K;
	    double[][] RT, RTn;
	    
	    //Identify all image files
	    if(files != null){
	      for(int i=0; i<files.length; i++){
	      	filename = files[i].getName();
	      	
	      	if(filename.endsWith(".jpg") && !filename.contains("_axis")){
	      		imagenames.add(Utility.getFilenameName(filename));
	      	}
	      }
	    }
	    
	    System.out.print("\n + Building cameras: ");
	    
      for(int i=0; i<imagenames.size(); i++){
        filename = imagenames.get(i);
        
        if(Utility.exists(path + filename + "_axis.jpg")){
        	filename += "_axis.jpg";
        }else{
        	filename += ".jpg";
        }
        
        try{
          image = ImageIO.read(new File(path + filename));
          w = image.getWidth(null);
          h = image.getHeight(null);
          pixels = new int[w*h];
          image.getRGB(0, 0, w, h, pixels, 0, w);
          
          //Obtain point correspondences
          axis_points = null;
          if(FOUND_AXIS_POINTS) axis_points = loadAxis(path + "axis_points.txt", i);
          
          if(axis_points == null){
          	//axis_points = getAxis(pixels, w, h, 220, 200, 170);
          	axis_points = retrieveAxis(pixels, w, h, 2);
          
	          if(true){		//Save axis points
	            try{
	            	FileWriter outs = null;
	            	Point tmpv;
	            	
	            	if(i == 0){
	              	outs = new FileWriter(path + "axis_points.txt", false);
	            	}else{
	            		outs = new FileWriter(path + "axis_points.txt", true);
	            	}
	            	
	  	          for(int j=0; j<axis_points.get(0).size(); j++){
	  	          	tmpv = axis_points.get(0).get(j);
	  	          	outs.write(tmpv.x + " " + tmpv.y + " " + tmpv.z + "\t");
	  	          	
	  	          	tmpv = axis_points.get(1).get(j);
	  	          	outs.write(tmpv.x + " " + tmpv.y + "\n");
	  	          }
	  	          
	              outs.write("\n");
	              outs.close();
	            }catch(Exception e) {e.printStackTrace();}
	          }
          }
          
          P = axis_points.get(0);
          p = axis_points.get(1);

          //Solve for the camera
          //tmpp = computeCameraMatrices(P, p, w, h); K = tmpp.first; RT = tmpp.second;
          tmpp = computeKRT(P, p); K = tmpp.first; RT = tmpp.second;
          if(viewer != null) viewer.add(drawReprojection(K, RT, P, p), true);

          if(true){		//Negate rho
    	  		RTn = MatrixUtility.copy(RT);
    	  		
    	  		for(int j=0; j<3; j++){
    	  			RTn[1][j] = -RTn[1][j];
    	  			RTn[2][j] = -RTn[2][j];
    	  			RTn[j][3] = -RTn[j][3];
    	  		}
    	  		
    			  if(reprojectionError(K, RTn, P, p) < reprojectionError(K, RT, P, p)) RT = RTn;
    	  	}
    	  	
    	    if(true){		//Non-linear refinement
    		  	//K = refineK(K, RT, P, p, 0.01);  
    		  	//RT = refineRT(K, RT, P, p, 0.01);  
    		  	tmpp = refineKRT(K, RT, P, p, 0.01); K = tmpp.first; RT = tmpp.second;
            if(viewer != null) viewer.add(drawReprojection(K, RT, P, p), true);
    	    }
    	    
    	    if(true){		//Flip cameras
    		    tmpp = orientTowards(K, RT, P); K = tmpp.first; RT = tmpp.second;
    	    }
          
          if(false){
            MatrixUtility.println(K);
            MatrixUtility.println(RT);
          }
          
          //Write data to output   
          output += imagenames.get(i) + " i p " + K[0][0] + " " + K[1][1] + " " + K[0][2] + " " + K[1][2] + "\n";
          output += imagenames.get(i) + " e m ";
          
          for(int j=0; j<4; j++){
            for(int k=0; k<4; k++){
              output += (Double.toString(RT[j][k]) + " ");
            }
          }
          
          output += "\n";
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        System.out.print(".");
      }
      
      Utility.saveFile(path + "cameras.txt", output);
      System.out.print("\t[Complete!]");
	  }
  
	  /**
	   * Automatically segment an image containing an NCSA Origin to derive the axis points of a cameras
	   * reference frame.
	   *  @param img the image to segment
	   *  @param w the width of the image
	   *  @param h the height of the image
	   *  @param yr the red threshold for the yellow markers
	   *  @param yg the green threshold for the yellow markers
	   *  @param yb the blue threshold for the yellow markers
	   *  @return an array of the four points on the axis: origin, red or x, green or y, blue or z
	   */
	  public static Vector<Vector<Point>> retrieveAxis(int[] img, int w, int h, int yr, int yg, int yb)
	  {
	    Vector<Vector<Point>> axis_points = new Vector<Vector<Point>>();
	    Vector<Point> P = new Vector<Point>();
	    Vector<Point> p = new Vector<Point>();
	  	ImageViewer viewer = new ImageViewer();
	    double[][] mask;
	    Vector<Vector<Pixel>> groups;
	    Vector<Pixel> points;
	    Vector<Pixel> line;
	    int rgb, r, g, b;
	    double mean_x, mean_y, tmpd;
	    double redness, greenness, blueness;
	    
	    Pair<Integer,Integer> max_red_edge;
	    Pair<Integer,Integer> max_green_edge;
	    Pair<Integer,Integer> max_blue_edge;
	    Pair<Integer,Integer> tmpp;
	    double max_red_value;
	    double max_green_value;
	    double max_blue_value;
	    
	    Point Po = null; Point po = null;
	    Point Px = null; Point px = null;
	    Point Py = null; Point py = null;
	    Point Pz = null; Point pz = null;
	    
	    if(viewer != null) viewer.add(img, w, h, false);
	    
	    //Threshold yellow pixels
	    mask = new double[h][w];
	    
	    for(int x=0; x<w; x++){
	      for(int y=0; y<h; y++){
	        rgb = img[y*w+x];
	        r = (0x00ff0000 & rgb) >> 16;
	        g = (0x0000ffff & rgb) >> 8;
	        b =  0x000000ff & rgb;
	        
	        if(r >= yr && g >= yg && b <= yb){
	        	mask[y][x] = 1; 
	        }
	      }
	    }
	    
	    if(viewer != null) viewer.add(mask, w, h, false);
	    
	    //Group thresholded pixels and use centers as candidate axis points
	    points = new Vector<Pixel>();
	    groups = ImageUtility.getGroups(mask, 2);
	    //System.out.print("\nPoints: " + groups.size());
	    
	    for(int i=0; i<groups.size(); i++){
	    	mean_x = 0;
	    	mean_y = 0;
	    	
	    	for(int j=0; j<groups.get(i).size(); j++){
	    		mean_x += groups.get(i).get(j).x;
	    		mean_y += groups.get(i).get(j).y;
	    	}
	    	
	    	mean_x /= groups.get(i).size();
	    	mean_y /= groups.get(i).size();
	      points.add(new Pixel(Math.round(mean_x), Math.round(mean_y)));
	    }
	    
	    if(viewer != null){
	    	int[] img_points = img.clone();
	    	
	    	for(int i=0; i<points.size(); i++){
	    		ImageUtility.drawBox(img_points, w, h, points.get(i), 5, 0x00ff0000);
	    	}
	    	
	    	viewer.add(img_points, w, h, false);
	    }
	    
	    //Identify points based on the color of the line connecting it to other points
	    int[] img_lines = img.clone();
	    max_red_edge = null;
	    max_green_edge = null;
	    max_blue_edge = null;
	    max_red_value = -Double.MAX_VALUE;
	    max_green_value = -Double.MAX_VALUE;
	    max_blue_value = -Double.MAX_VALUE;
	    
	    for(int i=0; i<points.size(); i++){
	    	for(int j=i+1; j<points.size(); j++){
	  			redness = 0;
	  			greenness = 0;
	  			blueness = 0;
	  			
	  			line = ImageUtility.getLinePoints(img, w, h, points.get(i), points.get(j), 1);
	  			if(viewer != null) ImageUtility.drawPoints(img_lines, w, h, line, 0x00ff0000);
	  			
	  			for(int k=0; k<line.size(); k++){
	  				r = (line.get(k).rgb & 0x00ff0000) >> 16;
						g = (line.get(k).rgb & 0x0000ff00) >> 8;
	  				b =  line.get(k).rgb & 0x000000ff;
	  				tmpd = r + g + b;
	  				
	  				if(tmpd > 0){
	    				redness += r / tmpd;
	    				greenness += g / tmpd;
	    				blueness += b / tmpd;
	  				}
	  			}
	  			
	  			redness /= line.size();
	  			greenness /= line.size();
	  			blueness /= line.size();
	      	//System.out.print("\n(" + i + " to " + j + "): " + redness + ", " + greenness + ", " + blueness);
	  			
	  			//Track maximal edge for each color
	  			tmpp = new Pair<Integer,Integer>(i,j);
	  			
	  			if(redness > max_red_value){
	  				max_red_edge = tmpp;
	  				max_red_value = redness;
	  			}
	  			
	  			if(greenness > max_green_value){
	  				max_green_edge = tmpp;
	  				max_green_value = greenness;
	  			}
	  			
	  			if(blueness > max_blue_value){
	  				max_blue_edge = tmpp;
	  				max_blue_value = blueness;
	  			}
	    	}
	    }
	    
	    if(viewer != null) viewer.add(img_lines, w, h, false);
	    
	    //Remove duplicate edges among the maximal edges
	    if(max_red_edge != null && max_red_edge == max_green_edge){
	    	if(max_red_value > max_green_value){
	    		max_green_edge = null;
	    		max_green_value = -Double.MAX_VALUE;
	    	}else{
	    		max_red_edge = null;
	    		max_red_value = -Double.MAX_VALUE;
	    	}
	    }
	    
	    if(max_green_edge != null && max_green_edge == max_blue_edge){
	    	if(max_green_value > max_blue_value){
	    		max_blue_edge = null;
	    		max_blue_value = -Double.MAX_VALUE;
	    	}else{
	    		max_green_edge = null;
	    		max_green_value = -Double.MAX_VALUE;
	    	}
	    }
	    
	    if(max_blue_edge != null && max_blue_edge == max_red_edge){
	    	if(max_blue_value > max_red_value){
	    		max_red_edge = null;
	    		max_red_value = -Double.MAX_VALUE;
	    	}else{
	    		max_blue_edge = null;
	    		max_blue_value = -Double.MAX_VALUE;
	    	}
	    }
	    
	    //Remove weak edges among the maximal edges
	    if(max_red_value < 0.4) max_red_edge = null;
	    if(max_green_value < 0.4) max_green_edge = null;
	    if(max_blue_value < 0.4) max_blue_edge = null;
	    
	    //Find origin point first (i.e. the one in common among all edges)
	    int[] votes = new int[points.size()];
	    int max_votes = 0;
	    int index_origin = 0;
	
	    if(max_red_edge != null){
	      votes[max_red_edge.first]++;
	      votes[max_red_edge.second]++;
	    }
	    
	    if(max_green_edge != null){
	      votes[max_green_edge.first]++;
	      votes[max_green_edge.second]++;
	    }
	    
	    if(max_blue_edge != null){
	      votes[max_blue_edge.first]++;
	      votes[max_blue_edge.second]++;
	    }
	    
	    for(int i=0; i<votes.length; i++){
	    	if(votes[i] > max_votes){
	    		index_origin = i;
	    		max_votes = votes[i];
	    	}
	    }
	    
	    Po = new Point(0, 0, 0);
	    po = new Point(points.get(index_origin));
	
	    //Find remaing points
	    if(max_red_edge != null){
	    	Px = new Point(1, 0, 0);
	    	
	    	if(max_red_edge.first != index_origin){
	    		px = new Point(points.get(max_red_edge.first));
	    	}else{
	    		px = new Point(points.get(max_red_edge.second));
	    	}
	    }
	    
	    if(max_green_edge != null){
	    	Py = new Point(0, 1, 0);
	    	
	    	if(max_green_edge.first != index_origin){
	    		py = new Point(points.get(max_green_edge.first));
	    	}else{
	    		py = new Point(points.get(max_green_edge.second));
	    	}
	    }
	    
	    if(max_blue_edge != null){
	    	Pz = new Point(0, 0, 1);
	    	
	    	if(max_blue_edge.first != index_origin){
	    		pz = new Point(points.get(max_blue_edge.first));
	    	}else{
	    		pz = new Point(points.get(max_blue_edge.second));
	    	}
	    }
	    
	    if(viewer != null){
	    	int[] img_axis = img.clone();
	    	
	    	if(po != null) ImageUtility.drawBox(img_axis, w, h, new Pixel(po.x, po.y), 5, 0x00ffffff);
	    	if(px != null) ImageUtility.drawBox(img_axis, w, h, new Pixel(px.x, px.y), 5, 0x00ff0000);
	    	if(py != null) ImageUtility.drawBox(img_axis, w, h, new Pixel(py.x, py.y), 5, 0x0000ff00);
	    	if(pz != null) ImageUtility.drawBox(img_axis, w, h, new Pixel(pz.x, pz.y), 5, 0x000000ff);
	
	    	viewer.add(img_axis, w, h, false);
	    }
	    
	    if(po != null){P.add(Po); p.add(po);};
	    if(px != null){P.add(Px); p.add(px);};
	    if(py != null){P.add(Py); p.add(py);};
	    if(pz != null){P.add(Pz); p.add(pz);};  
	    
	    axis_points.add(P);
	    axis_points.add(p);
	    
	    return axis_points;
	  }
	  
	  /**
	   * Manually segment an image containing an NCSA Origin to derive the axis points of a cameras
	   * reference frame.
	   *  @param img the image to segment
	   *  @param w the width of the image
	   *  @param h the height of the image
	   *  @param midmarkers the number of midmarkers
	   *  @return an array of the four points on the axis: origin, red or x, green or y, blue or z
	   */
	  public static Vector<Vector<Point>> retrieveAxis(int[] img, int w, int h, int midmarkers)
	  {
	    Vector<Vector<Point>> axis_points = new Vector<Vector<Point>>();
	    Vector<Point> P = new Vector<Point>();
	    Vector<Point> p = new Vector<Point>();
	    ImageViewer viewer = new ImageViewer(img, w, h);
	    Pixel tmpp;	
	    	  	
	    System.out.println();
	    System.out.print("Click on the origin: ");
	  	tmpp = viewer.getClick();
	  	viewer.drawBox(tmpp, 5, 0x00ffffff);
	  	P.add(new Point(0, 0, 0));
	  	p.add(new Point(tmpp));
	  	System.out.println(" " + p.lastElement().toString());
	  	
	    System.out.print("Click on the x-axis marker (red): ");
	  	tmpp = viewer.getClick();
	  	viewer.drawBox(tmpp, 5, 0x00ff0000);
	  	P.add(new Point(1, 0, 0));
	  	p.add(new Point(tmpp));
	  	System.out.println(" " + p.lastElement().toString());
	  	
	  	for(int i=0; i<midmarkers; i++){
		    System.out.print("Click on x-mid marker " + (i+1) + " (red): ");
		  	tmpp = viewer.getClick();
		  	viewer.drawBox(tmpp, 5, 0x00ffaaaa);
		  	P.add(new Point((midmarkers-i)/(midmarkers+1.0), 0, 0));
		  	p.add(new Point(tmpp));
		  	System.out.println(" " + p.lastElement().toString());
	  	}
	  	
	    System.out.print("Click on the y-axis marker (green): ");
	  	tmpp = viewer.getClick();
	  	viewer.drawBox(tmpp, 5, 0x0000ff00);
	  	P.add(new Point(0, 1, 0));
	  	p.add(new Point(tmpp));
	  	System.out.println(" " + p.lastElement().toString());
	  	
	  	for(int i=0; i<midmarkers; i++){
		    System.out.print("Click on y-mid marker " + (i+1) + " (green): ");
		  	tmpp = viewer.getClick();
		  	viewer.drawBox(tmpp, 5, 0x00aaffaa);
		  	P.add(new Point(0, (midmarkers-i)/(midmarkers+1.0), 0));
		  	p.add(new Point(tmpp));
		  	System.out.println(" " + p.lastElement().toString());
	  	}
	  	
	    System.out.print("Click on the z-axis marker (blue): ");
	  	tmpp = viewer.getClick();
	  	viewer.drawBox(tmpp, 5, 0x000000ff);
	  	P.add(new Point(0, 0, 1));
	  	p.add(new Point(tmpp));
	  	System.out.println(" " + p.lastElement().toString());
	  	
	  	for(int i=0; i<midmarkers; i++){
		    System.out.print("Click on z-mid marker " + (i+1) + " (blue): ");
		  	tmpp = viewer.getClick();
		  	viewer.drawBox(tmpp, 5, 0x00aaaaff);
		  	P.add(new Point(0, 0, (midmarkers-i)/(midmarkers+1.0)));
		  	p.add(new Point(tmpp));
		  	System.out.println(" " + p.lastElement().toString());
	  	}
	  	
	    axis_points.add(P);
	    axis_points.add(p);
	    
	    return axis_points;
	  }
	  
	  /**
	   * Load the axis points of a cameras reference frame.
	   *  @param filename the filename to load
	   *  @param camera the desired camera
	   *  @return an array of the four points on the axis: origin, red or x, green or y, blue or z
	   */
	  public static Vector<Vector<Point>> loadAxis(String filename, int camera)
	  {
	  	Vector<Vector<Point>> axis_points = new Vector<Vector<Point>>();
	  	String line;
	  	Point tmpv;
	  	int count = 0;
	  	
	  	axis_points.add(new Vector<Point>());	//3D points
	  	axis_points.add(new Vector<Point>());	//Corresponding 2D points
	  	
	  	try{
	  		Scanner ins = new Scanner(new File(filename));
	  		
	  		while(ins.hasNext()){
	  			line = ins.nextLine();
	  			
	  			if(line.isEmpty()){
	  				count++;
	  			}else if(count == camera){
		  			Scanner sc = new Scanner(line);
		  			
		  			while(sc.hasNext()){
			  			tmpv = new Point();
			  			tmpv.x = sc.nextDouble();
			  			tmpv.y = sc.nextDouble();
			  			tmpv.z = sc.nextDouble();
			  			axis_points.get(0).add(tmpv);
			  			
			  			tmpv = new Point();
			  			tmpv.x = sc.nextDouble();
			  			tmpv.y = sc.nextDouble();
			  			axis_points.get(1).add(tmpv);
		  			}
	  			}
	  		}
	  		
	  		ins.close();
	  	}catch(Exception e) {e.printStackTrace();}
      
      return axis_points;
	  }
	  
	  /**
	   * Interpolate points between pairs of given points axis points assuming an orthographic projection.
	   * @param P the 3D axis points
	   * @param p the projected 2D axis points
	   * @param iterations the number of interpolation iterations
	   * @return the denser set of interpolated points
	   */
	  public static Pair<Vector<Point>,Vector<Point>> applyOrthographicInterpolation(Vector<Point> P, Vector<Point> p, int iterations)
	  {
	  	Vector<Point> P_new;
	  	Vector<Point> p_new;
	    Point P0, p0, P1, p1, Pd, pd, v_rand;
	    double epsilon = 1e-12;
	    
	    for(int it=0; it<iterations; it++){
	    	P_new = new Vector<Point>();
	    	p_new = new Vector<Point>();
	    	
		    for(int i=0; i<P.size(); i++){
		    	P0 = P.get(i);
		      p0 = p.get(i);
		      
		    	for(int j=i+1; j<P.size(); j++){
		        P1 = P.get(j);
		        p1 = p.get(j);
		        Pd = P1.minus(P0);
		        pd = p1.minus(p0);
		        
		        for(double t=0.0; t<=1.0; t+=0.1){
		        	v_rand = new Point(epsilon*Math.random(), epsilon*Math.random(), epsilon*Math.random());
		        	P_new.add(P0.plus(Pd.times(t)).plus(v_rand));
		        	p_new.add(p0.plus(pd.times(t)).plus(v_rand));
		        }
		    	}
		    }
		    
		    P = P_new;
		    p = p_new;
	    }
	    
	    return new Pair<Vector<Point>,Vector<Point>>(P, p);
	  }
	  
	  /**
	   * Get the best rotation matrix aproximating the given 3x3 matrix.
	   * @param Q the given 3x3 matrix
	   * @return the best rotation matrix aproximating Q
	   */
	  public static double[][] computeBestRotation(double[][] Q)
	  {
	  	double[][] R;
	  	double[][] U, V;
	  	Triple<double[][],double[][],double[][]> tmpt;
	  	
	  	tmpt = JAMAMatrixUtility.svd(Q);
	  	U = tmpt.first;
	  	V = tmpt.third;
	  	
	  	R = MatrixUtility.mtimes(U, MatrixUtility.transpose(V));
	  	
	  	return R;
	  }
	  
	  /**
	   * Create a rotation matrix from axis of rotation using the Rodrigues' formula or a quaternion.
	   * @param v the rotation vector
	   * @return the equivelent rotation matrix
	   */
	  public static double[][] computeRotationMatrix(double[] v)
	  {
	  	double[][] R = null;
	  	
	  	if(v.length == 3){
	  		double[] r = v;
		  	double[][] rr, rskew, tmpM;
		  	double[][] A, B, C;
		  	double theta, costheta, sintheta;
		  	
		  	theta = MatrixUtility.norm(r);
		  	r = MatrixUtility.divide(r, theta);
		  	
		  	costheta = Math.cos(theta);
		  	sintheta = Math.sin(theta);
		  	rskew = MatrixUtility.skew(r);

		  	tmpM = new double[][]{r};
		  	rr = MatrixUtility.mtimes(MatrixUtility.transpose(tmpM), tmpM);
		  	
		  	A = MatrixUtility.times(MatrixUtility.eye(3), costheta);
		  	B = MatrixUtility.times(rr, 1-costheta);
		  	C = MatrixUtility.times(rskew, sintheta);
		  	
		  	R = MatrixUtility.plus(MatrixUtility.plus(A, B), C);
	  	}else if(v.length == 4){
	  		double[] q = v;
	  		double[] qhat = new double[]{q[0], q[1], q[2]};
	  		double[][] qhatskew = MatrixUtility.skew(qhat);
	  		double[][] qhatqhat, tmpM;
	  		double[][] A, B, C;
	  		
		  	tmpM = new double[][]{qhat};
		  	qhatqhat = MatrixUtility.mtimes(MatrixUtility.transpose(tmpM), tmpM);
		  	
	  		A = MatrixUtility.times(MatrixUtility.eye(3), q[3]*q[3]-MatrixUtility.dot(qhat, qhat));
	  		B = MatrixUtility.times(qhatqhat, 2.0);
	  		C = MatrixUtility.times(qhatskew, -2.0*q[3]);
	  		
		  	R = MatrixUtility.plus(MatrixUtility.plus(A, B), C);
	  	}else{
	  		System.out.println("Error: computeRotationMatrix received an unknown vector!");
	  	}
	  	
	  	return R;
	  }
	  
	  /**
	   * Convert a rotation matrix into a rotation axis using the Rodrigues' formula.
	   * @param R the rotation matrix
	   * @return the equivelent axis of rotation
	   */
	  public static double[] computeRotationAxis(double[][] R)
	  {
	  	double[][] M;
	  	double[] r = new double[3];
	  	double theta, sintheta, costheta;
	  	
	  	M = MatrixUtility.minus(R, MatrixUtility.transpose(R));
	  	M = MatrixUtility.divide(M, 2);
	  	
	  	r = new double[]{M[2][1], M[0][2], M[1][0]};
	  	sintheta = MatrixUtility.norm(r);
	  	r = MatrixUtility.divide(r, sintheta);
	  	//costheta = Math.sqrt(1-sintheta*sintheta);
	  	costheta = 0.5 * (MatrixUtility.trace(R) - 1.0);

	  	//System.out.println("Debug: " + sintheta + ", " + costheta + "\n");

	  	theta = Math.acos(costheta);
	  	//if(Math.asin(sintheta) < 0) theta = 2.0*Math.PI - theta;
	  	r = MatrixUtility.times(r, theta);
	  	
	  	return r;
	  }
	  
	  /**
	   * Convert a rotation matrix into a quaternion.
	   * @param R the rotation matrix
	   * @return the equivelent 4D quaternion
	   */
	  public static double[] computeRotationQuaternion(double[][] R)
	  {
	  	double[] q = new double[4];
	  	double tmpd;
	  	
	  	q[3] = 0.5 * Math.sqrt(1.0 + R[0][0] + R[1][1] + R[2][2]);
	  	tmpd = 1.0 / (4.0* q[3]);
	  	q[0] = tmpd * (R[1][2] - R[2][1]);
	  	q[1] = tmpd * (R[2][0] - R[0][2]);
	  	q[2] = tmpd * (R[0][1] - R[1][0]);
	  	
	  	return q;
	  }
	  
	  /**
	   * Get the rotation that was used to project the given 3D world points into the given 2D image points.
	   *  @param P the 3D world points
	   *  @param p the projections of the 3D points
	   *  @return the rotation from the 3D world space to the 2D image space points
	   */
	  public static double[][] computeOrthographicRotation(Vector<Point> P, Vector<Point> p)
	  {
	  	double[][] R = new double[3][3];
	  	double[][] A;
	  	double[] x, b;
	    Point r1, r2, r3;
	    	    
	    A = new double[2*P.size()][6];
	    b = new double[2*p.size()];
	    
	    for(int i=0; i<P.size(); i++){
	    	A[2*i+0][0] = P.get(i).x;
	    	A[2*i+0][1] = P.get(i).y;
	    	A[2*i+0][2] = P.get(i).z;
	    	A[2*i+0][3] = 0;
	    	A[2*i+0][4] = 0;
	    	A[2*i+0][5] = 0;
	
	    	A[2*i+1][0] = 0;
	    	A[2*i+1][1] = 0;
	    	A[2*i+1][2] = 0;
	    	A[2*i+1][3] = P.get(i).x;
	    	A[2*i+1][4] = P.get(i).y;
	    	A[2*i+1][5] = P.get(i).z;
	    }
	    
	    for(int i=0; i<p.size(); i++){
	    	b[2*i+0] = p.get(i).x;
	    	b[2*i+1] = p.get(i).y;
	    }
	    
	    x = JAMAMatrixUtility.ldivide(A,b);
	    
	    if(false){
		    System.out.println();
		    System.out.println("A: ");
		    MatrixUtility.println(A);
		    System.out.println("b: ");
		    MatrixUtility.println(b);
		    System.out.println();
		    System.out.println("x: ");
		    MatrixUtility.println(x);
	    }
	    
	    r1 = new Point(x[0], x[1], x[2]);
	    r2 = new Point(x[3], x[4], x[5]);
	    r3 = Point.cross(r1, r2);
	    
	    r1.divideEquals(r1.magnitude());
	    r2.divideEquals(r2.magnitude());
	    r3.divideEquals(r3.magnitude());
	
	    R[0][0] = r1.x;
	    R[0][1] = r1.y;
	    R[0][2] = r1.z;
	    R[1][0] = r2.x;
	    R[1][1] = r2.y;
	    R[1][2] = r2.z;
	    R[2][0] = r3.x;
	    R[2][1] = r3.y;
	    R[2][2] = r3.z;
	    
	    if(true){	//Why is this needed?
	    	R[0][2] = -R[0][2];
	    	R[1][0] = -R[1][0];
	    	R[1][1] = -R[1][1];
	    	R[2][2] = -R[2][2];
	    }
	    
	    R = computeBestRotation(R);
	    
	    return R;
	  }

	  /**
     * Get the projection that was used to project the given 3D world points into the given 2D image points.
     *  @param P the 3D world points
     *  @param p the projections of the 3D points
     *  @return the 3x4 matrix transformation from the 3D world space to the 2D image space points
     */
    public static double[][] computeProjectiveTransformation(Vector<Point> P, Vector<Point> p)
    {
    	double[][] M = new double[3][4];
    	double[][] A;
    	double[] x;
    	int at;
            
      A = new double[2*P.size()][12];
      
      for(int i=0; i<P.size(); i++){
      	A[2*i+0][0] = P.get(i).x;
      	A[2*i+0][1] = P.get(i).y;
      	A[2*i+0][2] = P.get(i).z;
      	A[2*i+0][3] = 1;
      	A[2*i+0][4] = 0;
      	A[2*i+0][5] = 0;
      	A[2*i+0][6] = 0;
      	A[2*i+0][7] = 0;
      	A[2*i+0][8] = -p.get(i).x*P.get(i).x;
      	A[2*i+0][9] = -p.get(i).x*P.get(i).y;
      	A[2*i+0][10]= -p.get(i).x*P.get(i).z;
      	A[2*i+0][11]= -p.get(i).x*1;
      	
      	A[2*i+1][0] = 0;
      	A[2*i+1][1] = 0;
      	A[2*i+1][2] = 0;
      	A[2*i+1][3] = 0;
      	A[2*i+1][4] = P.get(i).x;
      	A[2*i+1][5] = P.get(i).y;
      	A[2*i+1][6] = P.get(i).z;
      	A[2*i+1][7] = 1;
      	A[2*i+1][8] = -p.get(i).y*P.get(i).x;
      	A[2*i+1][9] = -p.get(i).y*P.get(i).y;
      	A[2*i+1][10]= -p.get(i).y*P.get(i).z;
      	A[2*i+1][11]= -p.get(i).y*1;
      }
      	    
      x = JAMAMatrixUtility.ldivide(A);
      
      //Build transformation matrix
      at = 0;
      
      for(int j=0; j<3; j++){
      	for(int i=0; i<4; i++){
      		M[j][i] = x[at];
      		at++;
      	}
      }
      
      if(false){
        System.out.println();
        System.out.println("A: ");
        MatrixUtility.println(A);
        System.out.println("x: ");
        MatrixUtility.println(x);
        System.out.println();
        System.out.println("M: ");
        MatrixUtility.println(M);
      }
      	    
      return M;
    }

		/**
     * Determine a cameras parameters given a set of corresponding 3D/2D points.
     * @param P a set of 3D points
     * @param p a set of corresponding 2D image points
     * @param w the width of the image
     * @param h the height of the image
     * @return the K and RT matrices for the camera
     */
    public static Pair<double[][],double[][]> computeKRT(Vector<Point> P, Vector<Point> p, int w, int h)
    {
    	double[][] K = new double[4][4];
    	double[][] RT = null;
    	double[][] R;
    	double f = 5.6713;		//Assume all images were taken with the same focal length
      double a, u, v;
    	double tx, ty, tz;
      Vector<Point> p_c, p_cs;
      Point po, Ptmp, ptmp;
      double Ptmp_d, ptmp_d;
      int count;
      
      //**** Set up default intrinsic parameters ****
      a = w / (double)h;
      u = w / 2.0;
      v = h / 2.0;
      
      K[0][0] = -(f/a) * u;
      K[1][1] = f * v;
      K[0][2] = u;
      K[1][2] = v;
      K[2][2] = 1;
      K[2][3] = 1;
      K[3][2] = 1;
      
      //Center and scale points
      po = p.get(0);		//Assume this is the origin
      p_c = Point.transform(p, po, 1, null);
      p_cs = Point.transform(p, po, w/2.0, null);
    
      //Calculate the rotation
      R = computeOrthographicRotation(P, p_cs);
    
      //Determine depth of origin
      tz = 0;
      count = 0;
      
      for(int j=0; j<P.size(); j++){
        Ptmp = new Point(P.get(j));
        Ptmp.timesEquals(u/2.0);
        Ptmp = Point.transform(MatrixUtility.homogeneousRotation(R), Ptmp);
        
        ptmp = p_c.get(j);
    
        Ptmp_d = Math.sqrt(Ptmp.x*Ptmp.x+Ptmp.y*Ptmp.y);
        ptmp_d = Math.sqrt(ptmp.x*ptmp.x+ptmp.y*ptmp.y);
        
        if(ptmp_d != 0){
        	tz += Ptmp_d*f*u / ptmp_d;
        	count++;
        }
      }
      
      tz /= count;
      //System.out.println("Scale: " + tz + "\n");
      
      //Set extrinsic matrix
      tz = -tz;
      tx = -tz * (po.x - u) / (f*u);
      ty = tz * (po.y - v) / (f*u);
      
      RT = MatrixUtility.homogeneousRotation(R);
      RT[0][3] = tx;
      RT[1][3] = ty;
      RT[2][3] = tz;
      
    	return new Pair<double[][],double[][]>(K, RT);
    }

		/**
     * Determine a cameras parameters given a set of corresponding 3D/2D points.
     * @param P a set of 3D points
     * @param p a set of corresponding 2D image points
     * @return the K and RT matrices for the camera
     */
    public static Pair<double[][],double[][]> computeKRT(Vector<Point> P, Vector<Point> p)
    {
    	double[][] K;
    	double[][] Ki;
    	double[][] RT = null;
    	double[][] M;
    	double[] a1 = new double[3];
    	double[] a2 = new double[3];
    	double[] a3 = new double[3];
    	double[] b = new double[3];
    	double[] a1xa3, a2xa3;
    	double[] r1, r2, r3, t;
    	double rho, rho2, alpha, beta, u0, v0, a1xa3m, a2xa3m, costheta, theta;
    	
    	M = computeProjectiveTransformation(P, p);
    	
    	if(false){		//Debug with a simple/valid projection matrix
    	  K = MatrixUtility.eye(3);
      	K[0][0] = 5;
        K[1][1] = 5;
        K[0][2] = 200;
        K[1][2] = 200;	      
        
        RT = new double[3][4];
        
        RT[0][0] = 1;
        RT[1][1] = 1;
        RT[2][2] = 1;
        
        if(true){
          double[][] R = MatrixUtility.rotateXYZ(20, 5, 140);
          R = MatrixUtility.subsref(R, 0, 2, 0, 2);
          RT = MatrixUtility.subsasgn(RT, 0, 0, R);
        }
        
        RT[0][3] = 25;
        RT[1][3] = 80;
        RT[2][3] = 4;
        
        M = MatrixUtility.mtimes(K, RT);
    	}
    	
    	a1[0] = M[0][0];
    	a1[1] = M[0][1];
    	a1[2] = M[0][2];
    	
    	a2[0] = M[1][0];
    	a2[1] = M[1][1];
    	a2[2] = M[1][2];
    	
    	a3[0] = M[2][0];
    	a3[1] = M[2][1];
    	a3[2] = M[2][2];
    	
    	b[0] = M[0][3];
    	b[1] = M[1][3];
    	b[2] = M[2][3];
    	
    	//Extract internal/external parameters from projection matrix
    	rho = 1.0 / MatrixUtility.norm(a3);
    	rho2 = rho*rho;
    	r3 = MatrixUtility.times(a3, rho);
    	u0 = rho2*MatrixUtility.dot(a1, a3);
    	v0 = rho2*MatrixUtility.dot(a2, a3);
    
    	a1xa3 = MatrixUtility.cross(a1, a3);
    	a2xa3 = MatrixUtility.cross(a2, a3);
    	a1xa3m = MatrixUtility.norm(a1xa3);
    	a2xa3m = MatrixUtility.norm(a2xa3);
    	
    	costheta = -MatrixUtility.dot(a1xa3, a2xa3) / (a1xa3m*a2xa3m);
    	theta = Math.acos(costheta);
    	alpha = rho2*a1xa3m*Math.sin(theta);
    	beta = rho2*a2xa3m*Math.sin(theta);
    	
    	r1 = MatrixUtility.times(a2xa3, 1.0/a2xa3m);
    	r2 = MatrixUtility.cross(r3, r1);
    	
    	K = new double[3][3];
    	K[0][0] = alpha;
      K[1][1] = beta;
      K[0][2] = u0;
      K[1][2] = v0;
      K[2][2] = 1;
    
      Ki = JAMAMatrixUtility.inverse(K);
      t = MatrixUtility.times(MatrixUtility.mtimes(Ki, b), rho);
            
    	//Setup internal parameter matrix
    	K = new double[4][4];
      K[0][0] = alpha;
      K[1][1] = beta;
      K[0][2] = u0;
      K[1][2] = v0;
      K[2][2] = 1;
      K[2][3] = 1;
      K[3][2] = 1;
      
      //Setup external parameter matrix
      RT = new double[4][4];
      
      RT[0][0] = r1[0];
      RT[0][1] = r1[1];
      RT[0][2] = r1[2];
      RT[0][3] = t[0];
      
      RT[1][0] = r2[0];
      RT[1][1] = r2[1];
      RT[1][2] = r2[2];
      RT[1][3] = t[1];
      
      RT[2][0] = r3[0];
      RT[2][1] = r3[1];
      RT[2][2] = r3[2];
      RT[2][3] = t[2];
      
      RT[3][3] = 1;
      
      if(false){		//Debug: check projection matrix before/after decomposition
      	K = MatrixUtility.eye(3);
        K[0][0] = alpha;
        K[1][1] = beta;
        K[0][2] = u0;
        K[1][2] = v0;
        
        System.out.println();
        MatrixUtility.println(M);
        MatrixUtility.println(MatrixUtility.mtimes(K, MatrixUtility.subsref(RT, 0, 2, 0, 3)));
        System.exit(1);
      }
      
    	return new Pair<double[][],double[][]>(K, RT);
    }

		/**
     * Use non-linear refinement to adjust the K camera matrix.
     * @param K the internal camera parameter matrix
     * @param RT the external camera parameter matrix
     * @param P a set of 3D points
     * @param p a corresponding set of 2D image points
     * @param tolerance the maximum error allowed for refinement termination
     * @return the refined K matrix
     */
    public static double[][] refineK(double[][] K, double[][] RT, Vector<Point> P, Vector<Point> p, double tolerance)
    {    
      boolean DEBUG = false;
      final double[][] RTf = RT;
    	final Vector<Point> Pf = P;
    	final Vector<Point> pf = p;
    	double[] x, fvec;
    	int[] info;
    	double ab, u, v;
    	
    	//Create a temporary class to hold the error function for LM
    	final class ReprojectionError implements Lmdif_fcn
    	{
    		public void fcn(int m, int n, double[] x, double[] fvec, int[] iflag)
    		{
      		value(x, fvec);
    		}
    		
    		public double value(double x[], double e[])
    		{
    			Vector<Point> p;
    			double[][] K = new double[4][4];
    			double tmpx, tmpy;
    			double sum = 0;
    			
    	  	//Recover the matrices from the MINPACK parameters
          K[0][0] = x[1];
          K[0][2] = x[2];
          K[1][1] = x[1];
          K[1][2] = x[3];
          K[2][2] = 1;
          K[2][3] = 1;
          K[3][2] = 1;
    	  	
    	    //Transform points
    	    p = Point.transform(RTf, Pf);
    	    p = Point.transform(K, p);
    	    
    	    //Compute error for each point
    			for(int i=0; i<p.size(); i++){
    				tmpx = pf.get(i).x - p.get(i).x;
    				tmpy = pf.get(i).y - p.get(i).y;
    
    				e[i+1] = Math.sqrt(tmpx*tmpx + tmpy*tmpy);
    				sum += e[i+1];
    			}
    			
    			sum /= e.length - 1;
    			
    			return sum;
    		}
    	}
    	
    	ReprojectionError f = new ReprojectionError();
    
    	//Set up the parameters for the MINPACK call
    	ab = K[0][0];
    	u = K[0][2];
    	v = K[1][2];

      x = new double[]{0, ab, u, v};
    	fvec = new double[P.size()+1];	//Residuals
    	info = new int[2];
    	
    	//Call MINPACK
    	if(DEBUG){
    		System.out.println();
    
    		if(false){
      		f.value(x, fvec);
      		MatrixUtility.println(fvec);
    		}else{
    			System.out.println("e0: " + f.value(x, fvec));
    		}
    	}
    	
    	Minpack_f77.lmdif1_f77(f, P.size(), 3, x, fvec, tolerance, info);
    	
    	if(DEBUG){
    		if(false){
    			MatrixUtility.println(fvec);
    		}else{
    			System.out.println("e1: " + f.value(x, fvec));
    		}
    		
    		System.out.print("(MP:" + info[1] + ")");
    	}
    	
    	//Recover the matrices from the MINPACK parameters
      K[0][0] = x[1];
      K[0][2] = x[2];
      K[1][1] = x[1];
      K[1][2] = x[3];
      K[2][2] = 1;
      K[2][3] = 1;
      K[3][2] = 1;
    	
    	return K;
    }
    
		/**
     * Use non-linear refinement to adjust the external camera matrix.
     * @param K the internal camera parameter matrix
     * @param RT the external camera parameter matrix
     * @param P a set of 3D points
     * @param p a corresponding set of 2D image points
     * @param tolerance the maximum error allowed for refinement termination
     * @return the refined RT matrix
     */
    public static double[][] refineRT(double[][] K, double[][] RT, Vector<Point> P, Vector<Point> p, double tolerance)
    {    
      boolean DEBUG = false;
      final boolean QUATERNION = true;
    	final double[][] Kf = K;
    	final Vector<Point> Pf = P;
    	final Vector<Point> pf = p;
    	double[][] R;
    	double[] r, x, fvec;
    	int[] info;
    	
    	//Create a temporary class to hold the error function for LM
    	final class ReprojectionError implements Lmdif_fcn
    	{
    		public void fcn(int m, int n, double[] x, double[] fvec, int[] iflag)
    		{
      		value(x, fvec);
    		}
    		
    		public double value(double x[], double e[])
    		{
    			Vector<Point> p;
    			double[][] RT, R;
    			double[] r;
    			double tmpx, tmpy;
    			double sum = 0;
    			
    	  	//Recover the matrices from the MINPACK parameters
    			if(!QUATERNION){
      	  	r = new double[]{x[1], x[2], x[3]};
    			}else{
    				r = new double[]{x[1], x[2], x[3], x[4]};
    			}
    			
    	  	R = computeRotationMatrix(r);
    	  	RT = MatrixUtility.homogeneousRotation(R);
    	  	RT[0][3] = x[x.length-3];
    	  	RT[1][3] = x[x.length-2];
    	  	RT[2][3] = x[x.length-1];
    	  	
    	    //Transform points
    	    p = Point.transform(RT, Pf);
    	    p = Point.transform(Kf, p);
    	    
    	    //Compute error for each point
    			for(int i=0; i<p.size(); i++){
    				tmpx = pf.get(i).x - p.get(i).x;
    				tmpy = pf.get(i).y - p.get(i).y;
    
    				e[i+1] = Math.sqrt(tmpx*tmpx + tmpy*tmpy);
    				sum += e[i+1];
    			}
    			
    			sum /= e.length - 1;
    			
    			return sum;
    		}
    	}
    	
    	ReprojectionError f = new ReprojectionError();
    
    	//Set up the parameters for the MINPACK call
    	R = MatrixUtility.subsref(RT, 0, 2, 0, 2);
    	
    	if(!QUATERNION){
      	r = computeRotationAxis(R);
      	x = new double[]{0, r[0], r[1], r[2], RT[0][3], RT[1][3], RT[2][3]};
    	}else{
    		r = computeRotationQuaternion(R);
      	x = new double[]{0, r[0], r[1], r[2], r[3], RT[0][3], RT[1][3], RT[2][3]};
    	}
    	
    	fvec = new double[P.size()+1];	//Residuals
    	info = new int[2];
    	
    	//Call MINPACK
    	if(DEBUG){
    		System.out.println();
    
    		if(false){
      		f.value(x, fvec);
      		MatrixUtility.println(fvec);
    		}else{
    			System.out.println("e0: " + f.value(x, fvec));
    		}
    	}
    	
    	Minpack_f77.lmdif1_f77(f, P.size(), x.length-1, x, fvec, tolerance, info);
    	
    	if(DEBUG){
    		if(false){
    			MatrixUtility.println(fvec);
    		}else{
    			System.out.println("e1: " + f.value(x, fvec));
    		}
    		
    		System.out.print("(MP:" + info[1] + ")");
    	}
    	
    	//Recover the matrices from the MINPACK parameters
    	if(!QUATERNION){
      	r = new double[]{x[1], x[2], x[3]};
    	}else{
    		r = new double[]{x[1], x[2], x[3], x[4]};
    	}
    	
    	R = computeRotationMatrix(r);
    	RT = MatrixUtility.homogeneousRotation(R);
	  	RT[0][3] = x[x.length-3];
	  	RT[1][3] = x[x.length-2];
	  	RT[2][3] = x[x.length-1];
    	
    	return RT;
    }
    
		/**
     * Use non-linear refinement to adjust the camera matrices.
     * @param K the internal camera parameter matrix
     * @param RT the external camera parameter matrix
     * @param P a set of 3D points
     * @param p a corresponding set of 2D image points
     * @param tolerance the maximum error allowed for refinement termination
     * @return the refined K and RT matrices
     */
    public static Pair<double[][],double[][]> refineKRT(double[][] K, double[][] RT, Vector<Point> P, Vector<Point> p, double tolerance)
    {    
      boolean DEBUG = false;
      final boolean QUATERNION = true;
    	final Vector<Point> Pf = P;
    	final Vector<Point> pf = p;
    	double[][] R;
    	double[] r, x, fvec;
    	int[] info;
    	double ab, u, v;
    	
    	//Create a temporary class to hold the error function for LM
    	final class ReprojectionError implements Lmdif_fcn
    	{
    		public void fcn(int m, int n, double[] x, double[] fvec, int[] iflag)
    		{
      		value(x, fvec);
    		}
    		
    		public double value(double x[], double e[])
    		{
    			Vector<Point> p;
    			double[][] K = new double[4][4];
    			double[][] RT, R;
    			double[] r;
    			double tmpx, tmpy;
    			double sum = 0;
    			
    	  	//Recover the matrices from the MINPACK parameters
          K[0][0] = x[1];
          K[0][2] = x[2];
          K[1][1] = x[1];
          K[1][2] = x[3];
          K[2][2] = 1;
          K[2][3] = 1;
          K[3][2] = 1;
    			
    			if(!QUATERNION){
      	  	r = new double[]{x[4], x[5], x[6]};
    			}else{
    				r = new double[]{x[4], x[5], x[6], x[7]};
    			}
    			
    	  	R = computeRotationMatrix(r);
    	  	RT = MatrixUtility.homogeneousRotation(R);
    	  	RT[0][3] = x[x.length-3];
    	  	RT[1][3] = x[x.length-2];
    	  	RT[2][3] = x[x.length-1];
    	  	
    	    //Transform points
    	    p = Point.transform(RT, Pf);
    	    p = Point.transform(K, p);
    	    
    	    //Compute error for each point
    			for(int i=0; i<p.size(); i++){
    				tmpx = pf.get(i).x - p.get(i).x;
    				tmpy = pf.get(i).y - p.get(i).y;
    
    				e[i+1] = Math.sqrt(tmpx*tmpx + tmpy*tmpy);
    				sum += e[i+1];
    			}
    			
    			sum /= e.length - 1;
    			
    			return sum;
    		}
    	}
    	
    	ReprojectionError f = new ReprojectionError();
    
    	//Set up the parameters for the MINPACK call
    	ab = K[0][0];
    	u = K[0][2];
    	v = K[1][2];
    	
    	R = MatrixUtility.subsref(RT, 0, 2, 0, 2);
    	
    	if(!QUATERNION){
      	r = computeRotationAxis(R);
      	x = new double[]{0, ab, u, v, r[0], r[1], r[2], RT[0][3], RT[1][3], RT[2][3]};
    	}else{
    		r = computeRotationQuaternion(R);
      	x = new double[]{0, ab, u, v, r[0], r[1], r[2], r[3], RT[0][3], RT[1][3], RT[2][3]};
    	}
    	
    	fvec = new double[P.size()+1];	//Residuals
    	info = new int[2];
    	
    	//Call MINPACK
    	if(DEBUG){
    		System.out.println();
    
    		if(false){
      		f.value(x, fvec);
      		MatrixUtility.println(fvec);
    		}else{
    			System.out.println("e0: " + f.value(x, fvec));
    		}
    	}
    	
    	Minpack_f77.lmdif1_f77(f, P.size(), x.length-1, x, fvec, tolerance, info);
    	
    	if(DEBUG){
    		if(false){
    			MatrixUtility.println(fvec);
    		}else{
    			System.out.println("e1: " + f.value(x, fvec));
    		}
    		
    		System.out.print("(MP:" + info[1] + ")");
    	}
    	
    	//Recover the matrices from the MINPACK parameters
      K[0][0] = x[1];
      K[0][2] = x[2];
      K[1][1] = x[1];
      K[1][2] = x[3];
      K[2][2] = 1;
      K[2][3] = 1;
      K[3][2] = 1;
			
			if(!QUATERNION){
  	  	r = new double[]{x[4], x[5], x[6]};
			}else{
				r = new double[]{x[4], x[5], x[6], x[7]};
			}
			
	  	R = computeRotationMatrix(r);
	  	RT = MatrixUtility.homogeneousRotation(R);
	  	RT[0][3] = x[x.length-3];
	  	RT[1][3] = x[x.length-2];
	  	RT[2][3] = x[x.length-1];
    	
    	return new Pair<double[][],double[][]>(K, RT);
    }

		/**
	   * Calculate the reprojection of the given 3D points with reconstructed camera matrcies compared to the ground truth 2D image points. 
	   * @param K the internal parameter matrix
	   * @param RT the external parameter matrix
	   * @param P the 3D points
	   * @param p0 the ground trught 2D image points
	   */
    public static double reprojectionError(double[][] K, double[][] RT, Vector<Point> P, Vector<Point> p0)
    {
    	Vector<Point> p1;
    	double sum = 0;
    	double tmpx, tmpy;
    	
	    //Transform points
	    p1 = Point.transform(RT, P);
	    p1 = Point.transform(K, p1);
	    
	    //Compute error for each point
			for(int i=0; i<P.size(); i++){
				tmpx = p1.get(i).x - p0.get(i).x;
				tmpy = p1.get(i).y - p0.get(i).y;

				sum += Math.sqrt(tmpx*tmpx + tmpy*tmpy);
			}
			
			sum /= P.size();
			
    	return sum;
    }
    
		/**
	   * Draw the reprojection of the given 3D points with reconstructed camera matrcies on the ground truth 2D image points. 
	   * @param K the internal parameter matrix
	   * @param RT the external parameter matrix
	   * @param P the 3D points
	   * @param p0 the ground trught 2D image points
	   */
	  public static int[][] drawReprojection(double[][] K, double [][] RT, Vector<Point> P, Vector<Point> p0)
	  {
	  	Vector<Point> p1 = Point.transform(K, Point.transform(RT, P));
	  	double minx = Double.MAX_VALUE;
	  	double maxx = Double.MIN_VALUE;
	  	double miny = Double.MAX_VALUE;
	  	double maxy = Double.MIN_VALUE;
	  	int[] argb;
	  	int w, h;
	  	int margin = 10;
	  	
	  	//Determine image dimensions
	  	for(int i=0; i<p0.size(); i++){
	  		if(p0.get(i).x < minx) minx = p0.get(i).x;
	  		if(p0.get(i).x > maxx) maxx = p0.get(i).x;
	  		if(p0.get(i).y < miny) miny = p0.get(i).y;
	  		if(p0.get(i).y > maxy) maxy = p0.get(i).y;
	  	}
	  	
	  	for(int i=0; i<p1.size(); i++){
	  		if(p1.get(i).x < minx) minx = p1.get(i).x;
	  		if(p1.get(i).x > maxx) maxx = p1.get(i).x;
	  		if(p1.get(i).y < miny) miny = p1.get(i).y;
	  		if(p1.get(i).y > maxy) maxy = p1.get(i).y;
	  	}
	  	
	  	w = (int)Math.ceil(maxx - minx + 1 + 2*margin);
	  	h = (int)Math.ceil(maxy - miny + 1 + 2*margin);
	  	argb = ImageUtility.getNewARGBImage(w, h, 0x00ffffff);
	  	
	  	//Draw points to image
	  	for(int i=0; i<p0.size(); i++){
	  		ImageUtility.drawBox(argb, w, h, (int)(p0.get(i).x-minx+margin), (int)(p0.get(i).y-miny+margin), 2, 0x0000ff00);
	  	}
	  	
	  	for(int i=0; i<p1.size(); i++){
	  		ImageUtility.drawBox(argb, w, h, (int)(p1.get(i).x-minx+margin), (int)(p1.get(i).y-miny+margin), 4, 0x00ffff00);
	  	}
	  	
	  	return ImageUtility.to2D(argb, w, h);
	  }
	  
	  /**
	   * Debug the Levenberg-Marquart algorithm in MINPACK.
	   */
	  private static void debugLM()
	  {
	  	boolean VIEW_VARIABLE_ERRORS = false;
	  	double[] x, fvec;
	  	int[] info;
	  	
	  	//Create a temporary class to hold the error function for LM
	  	final class MyError implements Lmdif_fcn
	  	{
	  		public void fcn(int m, int n, double[] x, double[] fvec, int[] iflag)
	  		{
		  		value(x, fvec);
	  		}
	  		
	  		public double value(double x[], double e[])
	  		{
	  			double sum = 0;
	  			
	  	    //Compute error z = (x-10)^2 + (y-5)^2
  				e[1] = (x[1]-10)*(x[1]-10);
  				sum += e[1];
  				e[2] = (x[2]-5)*(x[2]-5);
  				sum += e[2];
  				
	  			sum /= e.length - 1;
	  			
	  			return sum;
	  		}
	  	}
	  	
	  	MyError f = new MyError();
	
	  	//Set up the parameters for the MINPACK call
		  x = new double[]{0, 100, 100};
	  	fvec = new double[2+1];	//Residuals
	  	info = new int[2];
	  	
	  	//Call MINPACK
  		System.out.println();

  		if(VIEW_VARIABLE_ERRORS){
	  		f.value(x, fvec);
	  		System.out.print("e0: "); MatrixUtility.println(fvec);
  		}else{
  			System.out.println("e0: " + f.value(x, fvec));
  		}
	  	
	  	Minpack_f77.lmdif1_f77(f, 2, 2, x, fvec, 0.1, info);
	  	
  		if(VIEW_VARIABLE_ERRORS){
  			System.out.print("e1: " ); MatrixUtility.println(fvec);
  		}else{
  			System.out.println("e1: " + f.value(x, fvec));
  		}
	  	
	  	System.out.println("Info: " + info[1]);
	  	System.out.print("x: "); MatrixUtility.println(x);
	  }
	  
	  /**
	   * Debug camera calibration using points/cameras at given location.
	   * @param path the path to the debug points/cameras
	   */
	  private static void debugCalibration(String path)
	  {	  	
	  	Vector<Camera> cameras = loadCameras(path + "cameras.txt");
	  	Vector<Point> P = loadPoints(path + "points.txt");
	  	Vector<Point> p = loadPoints(path + "01.txt");
	  	Vector<Point> pbak = p;
	  	Pair<double[][],double[][]> tmpp;
	  	double[][] K, Kn, RT, RTn;
	  	Mesh mesh = new Mesh();
	  	ImageViewer imageviewer = new ImageViewer();

	  	//Add points and ground truth camera
	  	mesh.addData(Point.copy(P), null, new Color(0, 0, 0));
	  	mesh.addCameras(cameras, 1000, new Color(0.5f, 0.5f, 1), new Color(0, 1, 0));
	    
	  	if(true){
	  		K = cameras.get(0).getK();
	  		RT = cameras.get(0).getRT();
		  	imageviewer.add(drawReprojection(K, RT, P, pbak), true);
		  	System.out.println("\nGround Truth (" + reprojectionError(K, RT, P, pbak)+ "): "); MatrixUtility.println(K); MatrixUtility.println(RT);
	  	}
	  	
	  	//Add noise
	  	if(true){
		  	p = Point.round(p);
	  		p = Point.addNoise(p, 5);
		  	p = Point.round(p);
	  	}
	  	
	  	//Compute camera
	  	//tmpp = computeCameraMatrices(P, p, 10, 10);  K = tmpp.first; RT = tmpp.second;
	  	tmpp = computeKRT(P, p); K = tmpp.first; RT = tmpp.second;
	  	
	  	if(false){	//Negate alpha
		  	Kn = MatrixUtility.copy(K); Kn[0][0] = -Kn[0][0];
		  	if(reprojectionError(Kn, RT, P, p) < reprojectionError(K, RT, P, p)) K = Kn;
	  	}
	  	
	  	if(true){		//Negate rho
	  		RTn = MatrixUtility.copy(RT);
	  		
	  		for(int i=0; i<3; i++){
	  			RTn[1][i] = -RTn[1][i];
	  			RTn[2][i] = -RTn[2][i];
	  			RTn[i][3] = -RTn[i][3];
	  		}
	  		
			  if(reprojectionError(K, RTn, P, p) < reprojectionError(K, RT, P, p)) RT = RTn;
	  	}
	  	
	  	imageviewer.add(drawReprojection(K, RT, P, pbak), true);	    
	  	System.out.println("Linear (" + reprojectionError(K, RT, P, pbak)+ "): "); MatrixUtility.println(K); MatrixUtility.println(RT);
	  	
	    if(true){		//Non-linear refinement
		  	//K = refineK(K, RT, P, p, 0.01);  
		  	//RT = refineRT(K, RT, P, p, 0.01);  
		  	tmpp = refineKRT(K, RT, P, p, 0.01); K = tmpp.first; RT = tmpp.second;
		    imageviewer.add(drawReprojection(K, RT, P, pbak), true);
		    System.out.println("Non-linear (" + reprojectionError(K, RT, P, pbak)+ "): "); MatrixUtility.println(K); MatrixUtility.println(RT);
	    }
	    
	    if(true){		//Flip cameras
		  	//mesh.addCamera(new Camera(K, RT), 1100, new Color(0.5f, 0.5f, 1), new Color(0.5f, 0.5f, 0.5f));
		    tmpp = orientTowards(K, RT, P); K = tmpp.first; RT = tmpp.second;
		    imageviewer.add(drawReprojection(K, RT, P, pbak), true);
	    }
	    
	    //Add constrcuted camera to mesh
	  	mesh.addCamera(new Camera(K, RT), 1100, new Color(0.5f, 0.5f, 1), new Color(1, 1, 0));
	  	mesh.initialize();

	    //Display 3D results
	    ModelViewer modelviewer = new ModelViewer("ModelViewer.ini", -1, -1, false, false);
	    modelviewer.setMesh(mesh);
	    modelviewer.setPoints(true);

	    JFrame frame = new JFrame("Model Viewer");
	    frame.setSize(modelviewer.width+9, modelviewer.height+35);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.add(modelviewer);
	    frame.setVisible(true);
	  }
	}  

  /**
   * A set of functions for creating meshes of primitive shapes.
   */
  public static class Primitive
  {
  	/**
  	 * Create a cylinder.
  	 *  @param r1 the major radius
  	 *  @param r2 the minor radius
  	 *  @param length the length of the cylinder
  	 *  @param RT the transformation matrix to apply to the cylinder
  	 *  @return a pair contained the vertices and faces of the created cylinder
  	 */
  	public static Pair<Vector<Point>,Vector<Face>> getCylinder(double r1, double r2, double length, double[][] RT)
  	{
  		Vector<Point> vertices = new Vector<Point>();
  		Vector<Face> faces = new Vector<Face>();
      double x, y, z, theta;
      double halflength = length/2;
      int n = 0;
      
      //Create the vertices (i.e. the circular ends)
      for(double i=0; i<360; i+=5){
        theta = (Math.PI * (i % 360.0)) / 180.0;
        x = r1 * Math.cos(theta);
        y = r2 * Math.sin(theta);
        z = halflength;

        vertices.add(Point.transform(RT, new Point(x, y, z)));
        n++;
      }
      
      for(double i=0; i<360; i+=5){
        theta = (Math.PI * (i % 360.0)) / 180.0;
        x = r1 * Math.cos(theta);
        y = r2 * Math.sin(theta);
        z = -halflength;

        vertices.add(Point.transform(RT, new Point(x, y, z)));
      }
      
      //Create the faces
      for(int i=0; i<n; i++){
      	int j = (i+1) % n;
      	
      	faces.add(new Face(i, j+n, i+n));
      	faces.add(new Face(i, j, j+n));
      }
      
      return new Pair<Vector<Point>,Vector<Face>>(vertices, faces);
  	}
  	
  	/**
  	 * Return the portion of the given surface, represented as vertices and faces, that is
  	 * bounded by the given polygon which lies on that surface.
  	 *  @param v the vertices of the surface
  	 *  @param f the faces of the surface
  	 *  @param p the bounded portion of the desired surface
  	 *  @param threshold the threshold to use when determining if an edge loop falls on a face
  	 *  @return a pair containing the vertices/faces of the bounded surface
  	 */
  	public static Pair<Vector<Point>,Vector<Face>> getBoundedSurface(Vector<Point> v, Vector<Face> f, Polygon p, double threshold)
  	{
  		Vector<Point> vertices = new Vector<Point>();
  		Vector<Face> faces = new Vector<Face>();
  		Pair<Point,Double> bounds = Point.getBoundingSphere(v);
  		PolygonGroup pg;
  		int n = 0;
  		
  		for(int i=0; i<f.size(); i++){
  			pg = PolygonGroup.getIntersection(f.get(i).getPolygon(v), p, threshold);
  			
  			for(int j=0; j<pg.size(); j++){
	  			n = vertices.size();
	  			vertices.addAll(pg.get(j).getVertices());
	  			faces.addAll(Face.plus(pg.get(j).getSimplePolygonFaces(), n));
  			}
  		}
  		
  		return new Pair<Vector<Point>,Vector<Face>>(vertices, faces);
  	}
  }
  
  /**
   * A structure to hold the paramaters of a rigid transformation.
   */
  public static class RigidTransformation
  {
  	public double tx = 0;
  	public double ty = 0;
  	public double tz = 0;
  	public double rx = 0;
  	public double ry = 0;
  	public double rz = 0;
  	public double scl = 1;
  }
  
  /**
   * A main for debugging purposes.
   */
  public static void main(String args[])
  {
  	if(false){		//Test Rodrigues' formula
  		double[][] R;
  		double[] r = new double[]{1, 2, 3};
  		double[] q;
  		double theta = 95;
  		
  		r = MatrixUtility.divide(r, MatrixUtility.norm(r));
  		r = MatrixUtility.times(r, theta*Math.PI/180.0);
  		MatrixUtility.println(r);
  		System.out.println("Theta: " + theta + "\n");
  		
  		R = Camera.computeRotationMatrix(r);
  		r = Camera.computeRotationAxis(R);
  		
  		if(true){
	  		q = Camera.computeRotationQuaternion(R);
	  		R = Camera.computeRotationMatrix(q);
	  		r = Camera.computeRotationAxis(R);
  		}
  		
  	  //MatrixUtils.println(R);
  	  MatrixUtility.println(r); 
  	  System.out.println("Theta: " + MatrixUtility.norm(r)*180.0/Math.PI);
  	}
  	
  	if(true){		//Misc. tests
  		//Camera.debugLM();
  		Camera.debugCalibration("C:/Kenton/Data/3D/Exports/axis10b.pc/");
  	}
  }
}