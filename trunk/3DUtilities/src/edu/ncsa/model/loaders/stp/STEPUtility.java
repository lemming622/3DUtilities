package edu.ncsa.model.loaders.stp;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.utility.*;
import edu.ncsa.matrix.*;
import java.util.*;

public class STEPUtility
{
	private static boolean VERBOSE = true;
	
	/**
   * Extract a Vertex from a CARTESIAN_POINT entity.
   *  @param sf the STEP file
   *  @param e the index of the entity to attempt to extract the information from
   *  @return the Vertex
   */
  public static Point getVertex(STEPFile sf, int e)
  {
    STEPAttribute sa = sf.data.get(e).attribute.get(1);
    Vector<STEPAttribute> va = (Vector<STEPAttribute>)sa.value;
    double x, y, z;
    
    x = (Double)(va.get(0)).value;
    y = (Double)(va.get(1)).value;
    
    if(va.size() < 3){	//Watch for 2D points
    	z = 0;
    }else{
    	z = (Double)(va.get(2)).value;
    }
    
    return new Point(x, y, z);
  }
  
  /**
   * Extract an array containing a direction from a DIRECTION entity.
   *  @param sf the STEP file
   *  @param e the index of the entity to attempt to extract the information from
   *  @return the array
   */
  public static double[] getDirection(STEPFile sf, int e)
  {
    STEPAttribute sa = sf.data.get(e).attribute.get(1);
    Vector<STEPAttribute> va = (Vector<STEPAttribute>)sa.value;
    double[] arr = new double[3];
    
    arr[0] = (Double)(va.get(0)).value;
    arr[1] = (Double)(va.get(1)).value;
    arr[2] = (Double)(va.get(2)).value;
    
    return arr;
  }
  
  /**
   * Extract a coordinate frame from an AXIS2_PLACEMENT_3D entity.
   *  @param sf the STEP file
   *  @param e the index of the entity to attempt to extract the information from
   *  @return the transformation to the described coordinate frame
   */
  public static double[][] getAxis(STEPFile sf, int e)
  {
  	double[][] RT;
    double[] rx = {1, 0, 0};
    double[] ry = {0, 1, 0};
    double[] rz = {0, 0, 1};
  	double[] ra, rb, rc;
  	Point t;
  	int tmpi;
  	
    //Get the origin
    tmpi = sf.getEntity(e, 1, 0, "CARTESIAN_POINT");
    if(tmpi < 0) return null;
    t = getVertex(sf, tmpi);
    
    //Get the first direction
    tmpi = sf.getEntity(e, 2, 0, "DIRECTION");
    if(tmpi < 0) return null;
    rc = getDirection(sf, tmpi);
    
    //Get the second direction
    tmpi = sf.getEntity(e, 3, 0, "DIRECTION");
    if(tmpi < 0) return null;
    ra = getDirection(sf, tmpi);
    
    //Construct the rotation matrix
    rb = MatrixUtility.cross(rc, ra);
    RT = MatrixUtility.rotate(ra, rb, rc, rx, ry, rz);
    
    //Add the translation
    RT[0][3] = t.x;
    RT[1][3] = t.y;
    RT[2][3] = t.z;
    
  	return RT;
  }
  
  /**
   * Extract a POLYLINE entity.
   *  @param sf the STEP file
   *  @param e the index of the entity to attempt to extract the information from
   *  @return the curve
   */
  public static Vector<Point> getPolyLine(STEPFile sf, int e)
  {
    STEPAttribute sa = sf.data.get(e).attribute.get(1);
    Vector<STEPAttribute> va = (Vector<STEPAttribute>)sa.value;
    Vector<Point> pl = new Vector<Point>();
    
    for(int i=0; i<va.size(); i++){
    	pl.add(getVertex(sf, (Integer)va.get(i).value));
    }
    
    return pl;
  }
  
  /**
   * Extract a POLY_LOOP entity.
   *  @param sf the STEP file
   *  @param e the index of the entity to attempt to extract the information from
   *  @return the polygon
   */
  public static Vector<Point> getPolyLoop(STEPFile sf, int e)
  {
    STEPAttribute sa = sf.data.get(e).attribute.get(1);
    Vector<STEPAttribute> va = (Vector<STEPAttribute>)sa.value;
    Vector<Point> pl = new Vector<Point>();
    
    for(int i=0; i<va.size(); i++){
    	pl.add(getVertex(sf, (Integer)va.get(i).value));
    }
    
    return pl;
  }
  
  /**
   * Extract a curve from an EDGE_CURVE entity.
   *  @param sf the STEP file
   *  @param e the index of the entity to attempt to extract the information from
   *  @return the curve
   */
  public static Vector<Point> getCurve(STEPFile sf, int e)
  {
    Vector<Point> curve = new Vector<Point>();
    STEPAttribute sa;
    Vector<STEPAttribute> va = new Vector<STEPAttribute>();
    int i_cp0, i_cp1, i_gmt, i_axs;
    double r1, r2;
    String edge_geometry;
    boolean same_sense;
    Point edge_start, edge_end, v0, v1;
    int degree;
    Vector<Point> control_points = new Vector<Point>();
    Vector<Integer> multiplicity = new Vector<Integer>();
    Vector<Double> knots_unique = new Vector<Double>();
    Vector<Double> knots = new Vector<Double>();
    
    double[][] RT, RTi;
    double theta0, theta1, theta, tmpd;
    double x, y, z;
    
    //Get EDGE_CURVE attributes
    i_cp0 = sf.getEntity(e, 1, 0, "CARTESIAN_POINT");
    if(i_cp0 < 0) return null;
    edge_start = getVertex(sf, i_cp0);

    i_cp1 = sf.getEntity(e, 2, 0, "CARTESIAN_POINT");
    if(i_cp1 < 0) return null;
    edge_end = getVertex(sf, i_cp1);
    
    edge_geometry = sf.getEntityName(e, 3, 0);
    same_sense = (Boolean)sf.data.get(e).attribute.get(4).value;
                  
    //Process information based on geometry type
    if(edge_geometry.equals("LINE")){
    	if(false){
	      curve.add(edge_start);
	      curve.add(edge_end);
    	}else{		//Sample the line
    		v0 = edge_start;
    		v1 = edge_end.minus(edge_start);
    		
    		for(double i=0.0; i<=1.0; i+=0.1){
    			curve.add(v0.plus(v1.times(i)));
    		}
    	}
    }else if(edge_geometry.equals("CIRCLE") || edge_geometry.equals("ELLIPSE")){
      i_gmt = sf.getEntity(e, 3, 0, edge_geometry);
      if(i_gmt < 0) return null;
      
      //Get the circle attributes
      i_axs = sf.getEntity(i_gmt, 1, 0, "AXIS2_PLACEMENT_3D");
      if(i_axs < 0) return null;
      RT = getAxis(sf, i_axs);
      RTi = GMatrixUtility.inverse(RT);
      r1 = (Double)sf.data.get(i_gmt).attribute.get(2).value;
      r2 = edge_geometry.equals("ELLIPSE") ? (Double)sf.data.get(i_gmt).attribute.get(3).value : r1;

      //Calculate starting angle
      edge_start = Point.transform(RTi, edge_start);
      edge_start.divideEquals(edge_start.magnitude());
      theta0 = Math.acos(edge_start.x) * 180.0 / Math.PI;
      if(Math.asin(edge_start.y) <= 0) theta0 = 360.0 - theta0;
      
      //Calculate ending angle
      edge_end = Point.transform(RTi, edge_end);
      edge_end.divideEquals(edge_end.magnitude());
      theta1 = Math.acos(edge_end.x) * 180.0 / Math.PI;
      if(Math.asin(edge_end.y) <= 0) theta1 = 360.0 - theta1;

      //Fix angles
      if(!same_sense){
        tmpd = theta0;
        theta0 = theta1;
        theta1 = tmpd;
      }
      
      if(theta1 <= theta0){
        theta1 += 360.0;
      }
      
      //Tesselate the circe/ellipse
      for(double i=theta0; i<=theta1-1; i+=5){	//Note: we don't want to duplicate the endpoint (thus theta1-1 is our limit).
        theta = (Math.PI * (i % 360.0)) / 180.0;
        x = r1 * Math.cos(theta);
        y = r2 * Math.sin(theta);
        z = 0;
        
        curve.add(Point.transform(RT, new Point(x, y, z)));
      }
      
      //Make sure end point was reached in tesselation
      theta = (Math.PI * (theta1 % 360.0)) / 180.0;
      x = r1 * Math.cos(theta);
      y = r2 * Math.sin(theta);
      z = 0;
      
      curve.add(Point.transform(RT, new Point(x, y, z)));
    }else if(edge_geometry.equals("B_SPLINE_CURVE_WITH_KNOTS")){
      i_gmt = sf.getEntity(e, 3, 0, edge_geometry);
      if(i_gmt < 0) return null;
      
      //Get degree
      degree = (Integer)sf.data.get(i_gmt).attribute.get(1).value;
      
      //Get control points
      sa = sf.data.get(i_gmt).attribute.get(2);
      va = (Vector<STEPAttribute>)sa.value;
      
      control_points.clear();
      
      for(int i=0; i<va.size(); i++){
        control_points.add(getVertex(sf, (Integer)va.get(i).value));
      }
      
      //Get multiplicity
      sa = sf.data.get(i_gmt).attribute.get(6);
      va = (Vector<STEPAttribute>)sa.value;
      
      multiplicity.clear();
      
      for(int i=0; i<va.size(); i++){
        multiplicity.add((Integer)va.get(i).value);
      }
      
      //Get knots
      sa = sf.data.get(i_gmt).attribute.get(7);
      va = (Vector<STEPAttribute>)sa.value;
      
      knots_unique.clear();
      
      for(int i=0; i<va.size(); i++){
        knots_unique.add((Double)va.get(i).value);
      }
      
      //Expand knots
      knots.clear();
      
      for(int i=0; i<multiplicity.size(); i++){
        for(int j=0; j<multiplicity.get(i); j++){
          knots.add(knots_unique.get(i));
        }
      }
      
      if(false){   //Debug
      	System.out.println("\nDegree: " + degree);
        System.out.println("\nControl points (n=" + control_points.size() + "): ");
        
        for(int i=0; i<control_points.size(); i++){
          System.out.println(control_points.get(i).toString());
        }
        
        System.out.println("\nKnots (n=" + knots.size() + "): ");
        
        for(int i=0; i<knots.size(); i++){
          System.out.println(knots.get(i));
        }
      }
      
      //Tesselate the spline
      curve = control_points;
    }else{
    	if(VERBOSE){
	      System.out.println("Unsupported curve: " + edge_geometry);
    	}
    }
    
    return curve;
  }
  
  /**
   * Extract a surface from an ADVACED_FACE entity.
   *  @param sf the STEP file
   *  @param e the index of the entity to attempt to extract the information from
   *  @return the surface
   */  
  public static Pair<Vector<Point>,Vector<Face>> getSurface(STEPFile sf, int e)
  {
    Vector<Point> surface_vertices = new Vector<Point>();
    Vector<Face> surface_faces = new Vector<Face>();
    LinkedList<Integer> loop_indices = new LinkedList<Integer>();
    LinkedList<Point> loop_vertices = new LinkedList<Point>();
    STEPAttribute sa;
    Vector<STEPAttribute> va, va1, va2;
    Polygon tmpp;
    String face_geometry;
    Point edge_start, edge_end, tmpv;
    Vector<Point> curve;
    Pair<Vector<Point>,Vector<Face>> vf;
    Vector<Point> vertices = new Vector<Point>();
    Vector<Face> faces = new Vector<Face>();
    Vector<Vector<Point>> control_points = new Vector<Vector<Point>>();
    Vector<Integer> u_multiplicities = new Vector<Integer>();
    Vector<Integer> v_multiplicities = new Vector<Integer>();
    Vector<Double> u_knots_unique = new Vector<Double>();
    Vector<Double> v_knots_unique = new Vector<Double>();
    Vector<Double> u_knots = new Vector<Double>();
    Vector<Double> v_knots = new Vector<Double>();
    double[][] RT;
    double radius;
    boolean same_sense, u_closed, v_closed, self_intersect;
    boolean b_sns;
    int u_degree, v_degree;
    int i_edl, i_cp0, i_cp1, i_gmt, i_axs;
    int va_i, va_j, m, n, tmpi;
    boolean USE_CURVE = true;

    //Get advanced_face attributes
    face_geometry = sf.getEntityName(e, 2, 0);
    same_sense = (Boolean)sf.data.get(e).attribute.get(3).value;
    va_i = 0;
    
    while(true){
    	i_edl = sf.getEntity(e, 1, va_i, "EDGE_LOOP");
    	if(i_edl == -1) break;

    	//Extract the edge loop
      loop_indices.clear();
      loop_vertices.clear();
      va_j = 0;
      
      while(true){
        tmpi = sf.getEntity(i_edl, 1, va_j, "EDGE_CURVE");
        if(tmpi == -1) break;
        curve = getCurve(sf, tmpi);
        
        i_cp0 = sf.getEntity(tmpi, 1, 0, "CARTESIAN_POINT");
        i_cp1 = sf.getEntity(tmpi, 2, 0, "CARTESIAN_POINT");
        b_sns = (Boolean)sf.data.get(tmpi).attribute.get(4).value;
        if(i_cp0 < 0 || i_cp1 < 0) continue;
        
        edge_start = getVertex(sf, i_cp0);
        edge_end = getVertex(sf, i_cp1);
        
        if(!b_sns){
        	tmpv = edge_start;
        	edge_start = edge_end;
        	edge_end = tmpv;
        }
        
        if(loop_indices.isEmpty()){
          loop_indices.add(i_cp0);
          loop_indices.add(i_cp1);
          
          if(!USE_CURVE){
        	  loop_vertices.add(edge_start);
        	  loop_vertices.add(edge_end);
          }else{
          	loop_vertices.addAll(curve);
          }
        }else if(loop_indices.getLast() == i_cp0){
          loop_indices.addLast(i_cp1);
          
          if(!USE_CURVE){
          	loop_vertices.addLast(edge_end);
          }else{
          	for(int i=0; i<curve.size(); i++){
          		loop_vertices.addLast(curve.get(i));
          	}
          }
        }else if(loop_indices.getLast() == i_cp1){
          loop_indices.addLast(i_cp0);
          
          if(!USE_CURVE){
          	loop_vertices.addLast(edge_start);
          }else{
          	for(int i=curve.size()-1; i>=0; i--){
          		loop_vertices.addLast(curve.get(i));
          	}
          }
        }else if(loop_indices.getFirst() == i_cp0){
          loop_indices.addFirst(i_cp1);
        	
          if(!USE_CURVE){
          	loop_vertices.addFirst(edge_end);
          }else{
          	for(int i=0; i<curve.size(); i++){
          		loop_vertices.addFirst(curve.get(i));
          	}
          }
        }else if(loop_indices.getFirst() == i_cp1){
          loop_indices.addFirst(i_cp0);

          if(!USE_CURVE){
          	loop_vertices.addFirst(edge_start);
          }else{
          	for(int i=curve.size()-1; i>=0; i--){
          		loop_vertices.addFirst(curve.get(i));
          	}
          }
        }else{
          System.out.println("Warning: couldn't add point to polygon.");
        }
        
        va_j++;
      }
      
      if(!loop_indices.getFirst().equals(loop_indices.getLast())){
        System.out.print("Warning: disconnected loop, " + loop_indices.getFirst().toString() + " -> " + loop_indices.getLast().toString());
        System.out.println(" (" +  loop_vertices.getFirst().toString() + " -> " + loop_vertices.getLast().toString() + ")");
      }
      
      if(face_geometry.equals("PLANE")){
      	tmpp = new Polygon(loop_vertices);
      	
      	n = surface_vertices.size();
        surface_vertices.addAll(loop_vertices);
	      surface_faces.addAll(Face.plus(tmpp.getSimplePolygonFaces(), n));
	      //surface_faces.addAll(Face.plus(tmpp.getEdgeFaces(), n));			//View loops
      }else if(face_geometry.equals("CYLINDRICAL_SURFACE")){
        i_gmt = sf.getEntity(e, 2, 0, face_geometry);
        if(i_gmt < 0) return null;
        
        i_axs = sf.getEntity(i_gmt, 1, 0, "AXIS2_PLACEMENT_3D");
        if(i_axs < 0) return null;
        RT = getAxis(sf, i_axs);
        radius = (Double)sf.data.get(i_gmt).attribute.get(2).value;
        
        vf = MeshAuxiliary.Primitive.getCylinder(radius, radius, 10, RT);       
        vf = MeshAuxiliary.Primitive.getBoundedSurface(vf.first, vf.second, new Polygon(loop_vertices), 1e-2 * radius);
        
        n = surface_vertices.size();
        surface_vertices.addAll(vf.first);
        surface_faces.addAll(Face.plus(vf.second, n));			
        
        if(false){		//View loops
	        n = surface_vertices.size();
	        surface_vertices.addAll(loop_vertices);
		      surface_faces.addAll(Face.plus((new Polygon(loop_vertices)).getEdgeFaces(), n));
        }
      }else if(face_geometry.equals("B_SPLINE_SURFACE_WITH_KNOTS")){
        i_gmt = sf.getEntity(e, 2, 0, face_geometry);
        if(i_gmt < 0) return null;
        
        //System.out.println("Geometry: " + i_gmt);
        
        //Get degrees
        u_degree = (Integer)sf.data.get(i_gmt).attribute.get(1).value;
        v_degree = (Integer)sf.data.get(i_gmt).attribute.get(2).value;
        
        //Get control points
        sa = sf.data.get(i_gmt).attribute.get(3);
        va1 = (Vector<STEPAttribute>)sa.value;
        
        control_points.clear();
        
        for(int i=0; i<va1.size(); i++){
        	va2 = (Vector<STEPAttribute>)va1.get(i).value;
        	control_points.add(new Vector<Point>());
        	
          for(int j=0; j<va2.size(); j++){
            control_points.get(i).add(getVertex(sf, (Integer)va2.get(j).value));
          }
        }
        
        //Get curve info
        u_closed = (Boolean)sf.data.get(i_gmt).attribute.get(5).value;
        v_closed = (Boolean)sf.data.get(i_gmt).attribute.get(6).value;
        self_intersect = (Boolean)sf.data.get(i_gmt).attribute.get(7).value;

        //Get u-multiplicities
        sa = sf.data.get(i_gmt).attribute.get(8);
        va = (Vector<STEPAttribute>)sa.value;
        
        u_multiplicities.clear();
        
        for(int i=0; i<va.size(); i++){
          u_multiplicities.add((Integer)va.get(i).value);
        }
        
        //Get v-mulitiplicities
        sa = sf.data.get(i_gmt).attribute.get(9);
        va = (Vector<STEPAttribute>)sa.value;
        
        v_multiplicities.clear();
        
        for(int i=0; i<va.size(); i++){
          v_multiplicities.add((Integer)va.get(i).value);
        }

        //Get u-knots
        sa = sf.data.get(i_gmt).attribute.get(10);
        va = (Vector<STEPAttribute>)sa.value;
        
        u_knots_unique.clear();
        
        for(int i=0; i<va.size(); i++){
          u_knots_unique.add((Double)va.get(i).value);
        }
        
        //Get v-knots
        sa = sf.data.get(i_gmt).attribute.get(11);
        va = (Vector<STEPAttribute>)sa.value;
        
        v_knots_unique.clear();
        
        for(int i=0; i<va.size(); i++){
          v_knots_unique.add((Double)va.get(i).value);
        }
        
        //Expand u_knots
        u_knots.clear();
        
        for(int i=0; i<u_multiplicities.size(); i++){
          for(int j=0; j<u_multiplicities.get(i); j++){
            u_knots.add(u_knots_unique.get(i));
          }
        }
        
        //Expand v_knots
        v_knots.clear();
        
        for(int i=0; i<v_multiplicities.size(); i++){
          for(int j=0; j<v_multiplicities.get(i); j++){
            v_knots.add(v_knots_unique.get(i));
          }
        }
        
        if(false){   //Debug
        	System.out.println("\nu-Degree: " + u_degree);
        	System.out.println("v-Degree: " + u_degree);

          System.out.println("\nu-Control points (n=" + control_points.size()*control_points.get(0).size() + "): ");
          
          for(int i=0; i<control_points.size(); i++){
          	for(int j=0; j<control_points.get(i).size(); j++){
          		System.out.print("(" + control_points.get(i).get(j).toString() + ") ");
          	}
          	
          	System.out.println();
          }
          
          System.out.println("\nu-Knots (n=" + u_knots.size() + "): ");
          
          for(int i=0; i<u_knots.size(); i++){
            System.out.println(u_knots.get(i));
          }
          
          System.out.println("\nv-Knots (n=" + v_knots.size() + "): ");
          
          for(int i=0; i<v_knots.size(); i++){
            System.out.println(v_knots.get(i));
          }
        }
        
        //Tesselate the surface
        vertices.clear();
        faces.clear();
        
        m = control_points.size();
        n = control_points.get(0).size();
        
        for(int j=0; j<m; j++){
        	for(int i=0; i<n; i++){
        		vertices.add(control_points.get(j).get(i));
        	}
        }
        
        for(int j=0; j<m-1; j++){
        	for(int i=0; i<n-1; i++){
        		faces.add(new Face(j*n+i, j*n+(i+1), (j+1)*n+(i+1)));
        		faces.add(new Face(j*n+i, (j+1)*n+(i+1), (j+1)*n+i));
        	}
        }
       
        //vf = new Pair<Vector<Vertex>,Vector<Face>>(vertices,faces);
        vf = MeshAuxiliary.Primitive.getBoundedSurface(vertices, faces, new Polygon(loop_vertices), 1e-1);
        
        n = surface_vertices.size();
        surface_vertices.addAll(vf.first);
        surface_faces.addAll(Face.plus(vf.second, n));	
      }else{
      	if(VERBOSE){	
	        System.out.println("Unsupported surface: " + face_geometry);
      	}
      }
      
      va_i++;
    }
    
    return new Pair<Vector<Point>,Vector<Face>>(surface_vertices, surface_faces);
  }
}