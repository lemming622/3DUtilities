package edu.ncsa.model.loaders.vh;
import edu.ncsa.model.*;
import edu.ncsa.model.ImageUtility.*;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.model.Utility.*;
import edu.ncsa.model.matrix.*;
import java.util.*;

/**
 * An implementation of the edge bins described in [Matusik, 2001].
 * The edge bin provides structured storage of edges within an image based
 * on the epipole of another camera.  This is very useful for minimizing the
 * cost of ray/silouhette searches during construct of a visual hull.
 * 
 * Caution:
 *   This structure delibratley references vertices from the passed in polygons.
 *   One must not modify the polygons or their vertices during the lifespan of this 
 *   structure.
 *   
 *  @author Kenton McHenry
 */
public class EdgeBin
{
  public Camera camera;
  private Point epipole;    //The reference point (i.e. the epiple of the other camera)
  private Camera epipole_camera;
  private Point epipole_camera_position;
  private boolean INVALID_EPIPOLE = false;
  private Point dx;         //The reference x-direction
  private Point dy;         //The reference y-direction
  
  private TraversableTreeMap<Double,Vector<EdgeInfo>> edge_bins = new TraversableTreeMap<Double,Vector<EdgeInfo>>();
  private Vector<Vector<Vector<IntersectionInfo>>> silhouette_intersections = new Vector<Vector<Vector<IntersectionInfo>>>();
  private boolean BINS_FINALIZED = false;
  
  private static ImageViewer debug_viewer1 = null; //new ImageViewer("Debug-1");
  private static ImageViewer debug_viewer2 = null; //new ImageViewer("Debug-2");
  private static ImageViewer debug_viewer3 = null; //new ImageViewer("Debug-3");

  /**
   * A convenient structure for accessing edges within the EdgeBin class.
   *  @author Kenton McHenry
   */
  private class EdgeInfo implements Comparable
  {
    Vector<Point> polygon = null;
    public int index;
    public double distance;
    public Point p0;  //This is here just to speed up 2D intersection tests
    public Point p1;  //...
    
    /**
     * A comparator method for the EdgeInfo structure.
     *  @param obj the object to compare to
     *  @return the result (-1=less, 0=equal, 1=greater)
     */
    public int compareTo(Object obj)
    {
      EdgeInfo ei = (EdgeInfo)obj;
      Double d = distance;
      
      return d.compareTo(ei.distance);
    }
  }
  
  /**
   * A convenient structure for storing information about intersections between a ray and an edge.
   *  @author Kenotn McHenry
   */
  private class IntersectionInfo
  {
    public Point ray_world;
    public Point ray;
    public double a;
    public EdgeInfo ei;
    public Point p;             //The intersection point
    public Point p_aux = null;  //If the edge in ei is hit by the other ray also store that intersection here!
    
    /**
     * Class constructor.
     */
    public IntersectionInfo(Point ray_world, Point ray, double a, EdgeInfo ei, Point p)
    {
      this.ray_world = ray_world;
      this.ray = ray;
      this.a = a;
      this.ei = ei;
      this.p = p;
    }
  }
  
  /**
   * Class constructor.
   *  @param camera the camera in which we are dividing edges
   *  @param epipole_camera the camera projecting the rays into this camera
   */
  public EdgeBin(Camera camera, Camera epipole_camera)
  {
    this.camera = camera;
    this.epipole_camera = epipole_camera;
    this.epipole_camera_position = epipole_camera.getPosition();
    
    epipole = Point.transform(camera.getM(), epipole_camera.getPosition());

    if(epipole.isValid()){    //Set the reference direction as the vector from the epipole to the image origin (0,0)
      this.epipole = new Point(epipole);
      dx = new Point(1, 0, 0);
      dy = new Point(0, 1, 0);
    }else{
      System.out.println(" - Warning: epipole at (" + epipole.x + ", " + epipole.y + ", " + epipole.z + ")");
      System.exit(1);
    }
  }
  
  /**
   * Get the reference point.
   *  @return the reference point
   */
  public Point getEpipole()
  {
    return epipole;  
  }
  
  /**
   * Get the bin associated with the given vertex.
   *  @param v the vertex
   *  @return the bin value
   */
  public double getBin(Point v)
  { 
    double bin = -1;
    double tmpd;
    
    if(!INVALID_EPIPOLE){   //Bin is equal to angle from refrence vector out of epipole
      Point tmpv = new Point(v.x-epipole.x, v.y-epipole.y, 0);
      tmpv.divideEquals(tmpv.magnitude());
      
      bin = 180.0 * Math.acos(dx.times(tmpv)) / Math.PI;
      tmpd = 180.0 * Math.asin(dy.times(tmpv)) / Math.PI;
      if(tmpd < 0) bin = 360.0 - bin;
    }
    
    return bin;
  }
  
  /**
   * Get the angles used in the edge bin.
   *  @return a vector of the angles used
   */
  public Vector<Double> getBins()
  {
    return edge_bins.getKeys();
  }
  
  /**
   * Add a bin for the given value.
   *  @param bin the bin value
   */
  public void addBin(double bin)
  {
    if(bin >= 0){
      if(edge_bins.get(bin) == null){
        edge_bins.put(bin, new Vector<EdgeInfo>());
      }
    }
  }
  
  /**
   * Add a bin for the given vertex.
   *  @param v the vertex
   */
  public void addBin(Point v)
  {
    addBin(getBin(v));
  }
  
  /**
   * Add an edge represented by two vertices to all crossed bins.
   * Note: you should not any new bins after calling this!
   *  @param polygon the polygon containing the edge
   *  @param index the index of the starting vertex of the edge within the polygon
   */
  public void addToBin(Vector<Point> polygon, int index)
  {
    int p0 = index;
    int p1 = (p0+1) % polygon.size();
    double b0 = getBin(polygon.get(p0));
    double b1 = getBin(polygon.get(p1));
    double bi;
    double d0 = epipole.distance(polygon.get(p0));
    double d1 = epipole.distance(polygon.get(p1));
    
    //Setup edge information
    EdgeInfo ei = new EdgeInfo();
    ei.polygon = polygon;
    ei.index = p0;
    ei.distance = d0 > d1 ? d0 : d1;
    ei.p0 = polygon.get(p0);
    ei.p1 = polygon.get(p1);
    
    //Add edge information to all relevent bins
    if(Math.abs(b1-b0) < 360.0-Math.abs(b1-b0)){    //Assume the smaller arc is the correct arc
      bi = (b0 < b1) ? b0 : b1;
    }else{
      bi = (b0 > b1) ? b0 : b1;
    }
    
    edge_bins.get(bi).add(ei);

    while(true){
      bi = edge_bins.nextKey(bi);
      edge_bins.get(bi).add(ei);
      if(bi == b1 || bi == b0) break;
    }
  }
  
  /**
   * Add a ray and all its intersections to this structure.
   *  @param index the polygon index to which this ray belongs
   *  @param r the end point of the ray in the world coordinate system
   */
  public void addRay(int index, Point r)
  {
    if(!BINS_FINALIZED){
      Vector<Double> bins = edge_bins.getKeys();
      
      for(int i=0; i<bins.size(); i++){
        Collections.sort(edge_bins.get(bins.get(i)));
      }
      
      BINS_FINALIZED = true;
    }

    Point rt = Point.transform(camera.getM(), r);              
    double a = getBin(rt);
    Vector<EdgeInfo> ei = edge_bins.getValue(a);
    Point vi;
    
    while(index >= silhouette_intersections.size()) silhouette_intersections.add(new Vector<Vector<IntersectionInfo>>());
    silhouette_intersections.get(index).add(new Vector<IntersectionInfo>());
    
    for(int i=0; i<ei.size(); i++){
      vi = Point.getRayIntersection(epipole, rt, ei.get(i).p0, ei.get(i).p1);
      if(vi != null) silhouette_intersections.get(index).lastElement().add(new IntersectionInfo(r, rt, a, ei.get(i), vi));
    }
  }
  
  /**
   * Using the stored polygon rays return all the clipped polygons created.
   *  @return a vector of clipped cone face polygon groups
   */
  public Vector<PolygonGroup> getPolygons()
  {    
    Vector<PolygonGroup> polygons = new Vector<PolygonGroup>();
    PolygonGroup face_polygons;
    Point r0, r1;
    Point r0t, r1t;
    int r0i, r1i;
    double a0, a1;
    
    for(int s=0; s<silhouette_intersections.size(); s++){
      for(int r=0; r<silhouette_intersections.get(s).size(); r++){
        r0i = r;
        r1i = (r + 1) % silhouette_intersections.get(s).size();
        
        if(!silhouette_intersections.get(s).get(r0i).isEmpty() && !silhouette_intersections.get(s).get(r1i).isEmpty()){
          r0 = silhouette_intersections.get(s).get(r0i).get(0).ray_world;
          r0t = silhouette_intersections.get(s).get(r0i).get(0).ray;
          a0 = silhouette_intersections.get(s).get(r0i).get(0).a;
          
          r1 = silhouette_intersections.get(s).get(r1i).get(0).ray_world;
          r1t = silhouette_intersections.get(s).get(r1i).get(0).ray;
          a1 = silhouette_intersections.get(s).get(r1i).get(0).a;
          
          //Initialize across intersections
          for(int i=0; i<silhouette_intersections.get(s).get(r0i).size(); i++){
            silhouette_intersections.get(s).get(r0i).get(i).p_aux = null;
          }
          
          for(int i=0; i<silhouette_intersections.get(s).get(r1i).size(); i++){
            silhouette_intersections.get(s).get(r1i).get(i).p_aux = null;
          }
          
          for(int i=0; i<silhouette_intersections.get(s).get(r0i).size(); i++){
            for(int j=0; j<silhouette_intersections.get(s).get(r1i).size(); j++){
              if(silhouette_intersections.get(s).get(r0i).get(i).ei == silhouette_intersections.get(s).get(r1i).get(j).ei){
                silhouette_intersections.get(s).get(r0i).get(i).p_aux = silhouette_intersections.get(s).get(r1i).get(j).p;
                silhouette_intersections.get(s).get(r1i).get(j).p_aux = silhouette_intersections.get(s).get(r0i).get(i).p;
              }
            }
          }
          
          if(debug_viewer1 != null || debug_viewer2 != null){
            Vector<Vector<IntersectionInfo>> ii_tmp = new Vector<Vector<IntersectionInfo>>();
            ii_tmp.add(silhouette_intersections.get(s).get(r0i));
            ii_tmp.add(silhouette_intersections.get(s).get(r1i));
  
            if(debug_viewer1 != null) debugRayIntersections(debug_viewer1, r0t, r1t, ii_tmp);
            if(debug_viewer2 != null) debugRayIntersectionsZoomed(debug_viewer2, r0t, r1t, ii_tmp);
          }
          
          //Grab all the edges that make up this polygon
          LinkedList<LineSegment> edges = new LinkedList<LineSegment>();
          Vector<EdgeInfo> tmpv;
          double b0, b1, bi;
          
          //Ray edges
          for(int i=0; i<silhouette_intersections.get(s).get(r0i).size(); i+=2){
            edges.add(new LineSegment(silhouette_intersections.get(s).get(r0i).get(i).p, silhouette_intersections.get(s).get(r0i).get(i+1).p));  
          }
          
          for(int i=0; i<silhouette_intersections.get(s).get(r1i).size(); i+=2){
            edges.add(new LineSegment(silhouette_intersections.get(s).get(r1i).get(i).p, silhouette_intersections.get(s).get(r1i).get(i+1).p));  
          }
          
          //System.out.println("Debug (ray edges): " + edges.size());
          
          //Ray intersected edges
          for(int i=0; i<silhouette_intersections.get(s).get(r0i).size(); i++){
            if(silhouette_intersections.get(s).get(r0i).get(i).p_aux != null){          //Double intersected edges (don't duplicate!)
              edges.add(new LineSegment(silhouette_intersections.get(s).get(r0i).get(i).p, silhouette_intersections.get(s).get(r0i).get(i).p_aux));
            }else if(silhouette_intersections.get(s).get(r0i).get(i).p_aux == null){    //Single intersected edges
              if(isBetween(silhouette_intersections.get(s).get(r0i).get(i).ei.p0, a0, a1)){
                edges.add(new LineSegment(silhouette_intersections.get(s).get(r0i).get(i).p, silhouette_intersections.get(s).get(r0i).get(i).ei.p0));
              }else{
                edges.add(new LineSegment(silhouette_intersections.get(s).get(r0i).get(i).p, silhouette_intersections.get(s).get(r0i).get(i).ei.p1));
              }
            }
          }
                  
          for(int i=0; i<silhouette_intersections.get(s).get(r1i).size(); i++){
            if(silhouette_intersections.get(s).get(r1i).get(i).p_aux == null){          //Single intersected edges
              if(isBetween(silhouette_intersections.get(s).get(r1i).get(i).ei.p0, a0, a1)){
                edges.add(new LineSegment(silhouette_intersections.get(s).get(r1i).get(i).p, silhouette_intersections.get(s).get(r1i).get(i).ei.p0));
              }else{
                edges.add(new LineSegment(silhouette_intersections.get(s).get(r1i).get(i).p, silhouette_intersections.get(s).get(r1i).get(i).ei.p1));
              }
            }
          }
          
          //System.out.println("Debug (+interseted edges): " + edges.size());
          
          //Unique internal edges that don't intersect rays
          TreeSet<EdgeInfo> internal_edges = new TreeSet<EdgeInfo>();
          EdgeInfo ei;
          
          b0 = edge_bins.getKey(a0);
          b1 = edge_bins.getKey(a1);
          
          if(Math.abs(b1-b0) < 360.0-Math.abs(b1-b0)){    //Assume the smaller arc is the correct arc
            bi = (b0 < b1) ? b0 : b1;
          }else{
            bi = (b0 > b1) ? b0 : b1;
          }
          
          tmpv = edge_bins.get(bi);
          
          for(int i=0; i<tmpv.size(); i++){
            if(isBetween(tmpv.get(i), a0, a1)){
              internal_edges.add(tmpv.get(i));
            }
          }
  
          if(b0 != b1){
            while(true){
              bi = edge_bins.nextKey(bi);
              tmpv = edge_bins.get(bi);
              
              for(int i=0; i<tmpv.size(); i++){
                if(isBetween(tmpv.get(i), a0, a1)){
                  internal_edges.add(tmpv.get(i));
                }
              }
              
              if(bi == b1 || bi == b0) break;
            }
          }
          
          // => Copy over and store unique edges
          Iterator<EdgeInfo> itr = internal_edges.iterator();
          
          while(itr.hasNext()){
            ei = itr.next();
            edges.add(new LineSegment(ei.p0, ei.p1));
          }
          
          //System.out.println("Debug (+internal edges): " + edges.size());
          
          face_polygons = PolygonGroup.edgesToPolygons(edges);
          face_polygons = projectToPlane(face_polygons, camera, epipole_camera_position, r0, r1);
          polygons.add(face_polygons);
        }else{
          polygons.add(new PolygonGroup());   //Just a place holder to maintain number of faces!
        }
      }
    }
    
    return polygons;
  }
  
  /**
   * Get the polygons created by intersecting the given two rays with the polygon stored in the edge bins.
   *  @param r0 an endpoint representing a ray with the stored epipole
   *  @param r1 a second endpoint representing a ray with the stored epipole
   *  @return the resulting polygon
   */
  public PolygonGroup getPolygons(Point r0, Point r1)
  {
    if(!BINS_FINALIZED){
      Vector<Double> bins = edge_bins.getKeys();
      
      for(int i=0; i<bins.size(); i++){
        Collections.sort(edge_bins.get(bins.get(i)));
      }
      
      BINS_FINALIZED = true;
    }

    PolygonGroup polygons = null;
    Point r0t = Point.transform(camera.getM(), r0);              
    Point r1t = Point.transform(camera.getM(), r1); 
    double a0 = getBin(r0t);
    double a1 = getBin(r1t);
    Vector<EdgeInfo> ei0 = edge_bins.getValue(a0);
    Vector<EdgeInfo> ei1 = edge_bins.getValue(a1);
    Point vi;
    
    //Determine which edges this ray intersects with
    Vector<Vector<IntersectionInfo>> intersections = new Vector<Vector<IntersectionInfo>>();
    
    //Ray-0 intersections
    intersections.add(new Vector<IntersectionInfo>());
    
    for(int i=0; i<ei0.size(); i++){
      vi = Point.getRayIntersection(epipole, r0t, ei0.get(i).p0, ei0.get(i).p1);
      if(vi != null) intersections.get(0).add(new IntersectionInfo(r0, r0t, a0, ei0.get(i), vi));
    }
    
    //Ray-1 intersections
    intersections.add(new Vector<IntersectionInfo>());
    
    for(int i=0; i<ei1.size(); i++){
      vi = Point.getRayIntersection(epipole, r1t, ei1.get(i).p0, ei1.get(i).p1);
      if(vi != null) intersections.get(1).add(new IntersectionInfo(r1, r1t, a1, ei1.get(i), vi));
    }
    
    //Ray-0 and Ray-1 intersections
    for(int i=0; i<intersections.get(0).size(); i++){
      for(int j=0; j<intersections.get(1).size(); j++){
        if(intersections.get(0).get(i).ei == intersections.get(1).get(j).ei){
          intersections.get(0).get(i).p_aux = intersections.get(1).get(j).p;
          intersections.get(1).get(j).p_aux = intersections.get(0).get(i).p;
        }
      }
    }
    
    if(debug_viewer1 != null) debugRayIntersections(debug_viewer1, r0t, r1t, intersections);
    if(debug_viewer2 != null) debugRayIntersectionsZoomed(debug_viewer2, r0t, r1t, intersections);
    
    //Grab all the edges that make up this polygon
    if(!intersections.get(0).isEmpty() && !intersections.get(1).isEmpty()){
      LinkedList<LineSegment> edges = new LinkedList<LineSegment>();
      Vector<EdgeInfo> tmpv;
      double b0, b1, bi;
      double dist0, dist1;
      
      //Ray edges
      for(int i=0; i<intersections.size(); i++){
        for(int j=0; j<intersections.get(i).size(); j+=2){
          edges.add(new LineSegment(intersections.get(i).get(j).p, intersections.get(i).get(j+1).p));  
        }
      }
      
      //System.out.println("Debug (ray edges): " + edges.size());
      
      //Ray intersected edges
      for(int i=0; i<intersections.size(); i++){
        for(int j=0; j<intersections.get(i).size(); j++){          
          if(i==0 && intersections.get(i).get(j).p_aux != null){    //Double intersected edges (don't duplicate!)
            edges.add(new LineSegment(intersections.get(i).get(j).p, intersections.get(i).get(j).p_aux));
          }else if(intersections.get(i).get(j).p_aux == null){      //Single intersected edges
            //Check if both end points are in between (if so go to furthers one!)
            if(isBetween(intersections.get(i).get(j).ei, a0, a1)){  //TODO: Add this check to other version of getPolygons()?
              dist0 = intersections.get(i).get(j).p.distance(intersections.get(i).get(j).ei.p0);
              dist1 = intersections.get(i).get(j).p.distance(intersections.get(i).get(j).ei.p1);

              if(dist0 < dist1){
                edges.add(new LineSegment(intersections.get(i).get(j).p, intersections.get(i).get(j).ei.p1));
              }else{
                edges.add(new LineSegment(intersections.get(i).get(j).p, intersections.get(i).get(j).ei.p0));
              }   
            }else if(isBetween(intersections.get(i).get(j).ei.p0, a0, a1)){
              edges.add(new LineSegment(intersections.get(i).get(j).p, intersections.get(i).get(j).ei.p0));
            }else if(isBetween(intersections.get(i).get(j).ei.p1, a0, a1)){
              edges.add(new LineSegment(intersections.get(i).get(j).p, intersections.get(i).get(j).ei.p1));
            }else{
              System.out.println("Error: getPolygons -> intersected an edge that isn't within bounds!");
              System.exit(1);
            }
          }
        }
      }
      
      //System.out.println("Debug (+interseted edges): " + edges.size());
      
      //Unique internal edges that don't intersect rays
      TreeSet<EdgeInfo> intersected_edges = new TreeSet<EdgeInfo>();
      TreeSet<EdgeInfo> internal_edges = new TreeSet<EdgeInfo>();
      EdgeInfo ei;
      
      // => Keep track of intersected edges
      for(int i=0; i<intersections.size(); i++){
        for(int j=0; j<intersections.get(i).size(); j++){
          intersected_edges.add(intersections.get(i).get(j).ei);
        }
      }
            
      // => Traverse all spanned edges and keep only those that are unique and not intersected
      b0 = edge_bins.getKey(a0);
      b1 = edge_bins.getKey(a1);
      
      if(Math.abs(b1-b0) < 360.0-Math.abs(b1-b0)){    //Assume the smaller arc is the correct arc
        bi = (b0 < b1) ? b0 : b1;
      }else{
        bi = (b0 > b1) ? b0 : b1;
      }
      
      tmpv = edge_bins.get(bi);
      
      for(int i=0; i<tmpv.size(); i++){
        //if(!intersected_edges.contains(tmpv.get(i))){
          if(isBetween(tmpv.get(i), a0, a1)){
            internal_edges.add(tmpv.get(i));
          }
        //}
      }
  
      if(b0 != b1){
        while(true){
          bi = edge_bins.nextKey(bi);
          tmpv = edge_bins.get(bi);
          
          for(int i=0; i<tmpv.size(); i++){
            //if(!intersected_edges.contains(tmpv.get(i))){
              if(isBetween(tmpv.get(i), a0, a1)){
                internal_edges.add(tmpv.get(i));
              }
            //}
          }
          
          if(bi == b1 || bi == b0) break;
        }
      }
      
      // => Copy over and store unique edges
      Iterator<EdgeInfo> itr = internal_edges.iterator();
      
      while(itr.hasNext()){
        ei = itr.next();
        edges.add(new LineSegment(ei.p0, ei.p1));
      }
      
      //System.out.println("Debug (+internal edges): " + edges.size());
      
      polygons = PolygonGroup.edgesToPolygons(edges);
      polygons = projectToPlane(polygons, camera, epipole_camera_position, r0, r1);
    }else{
      polygons = new PolygonGroup();
    }
    
    return polygons;
  }
  
  /**
   * Determine if the given object is between the two angles with regards to the epipole.
   *  @param obj the object to be checked
   *  @param a0 the first angle
   *  @param a1 the second angle
   *  @return true if the object is within these angles (note: assumes the smaller angle is the correct angle!)
   */
  private boolean isBetween(Object obj, double a0, double a1)
  {
    if(a0 == a1) return false;
    
    double amin = (a0 < a1) ? a0 : a1;
    double amax = (a0 > a1) ? a0 : a1;
    boolean BETWEEN_ISVALID = true;
    boolean BETWEEN;
    double tmpd;
    
    if(Math.abs(amax-amin) > 360.0-Math.abs(amax-amin)){    //Assume the smaller arc is the correct arc
      BETWEEN_ISVALID = false;
    }
    
    if(obj instanceof Point){
      tmpd = getBin((Point)obj);
    }else if(obj instanceof EdgeInfo){
      EdgeInfo ei = (EdgeInfo)obj;
      return isBetween(ei.p0, a0, a1) && isBetween(ei.p1, a0, a1);
    }else{
      return false;
    }
    
    BETWEEN = (tmpd >= amin) && (tmpd <= amax);
    
    if(BETWEEN && BETWEEN_ISVALID){
      return true;
    }else if(!BETWEEN && !BETWEEN_ISVALID){
      return true;
    }else{
      return false;
    }
  }
  
  /**
   * Project the points of the given 2D image polygon onto the world space plane specified by the 3 points.
   *  @param polygons the vector of polygons represented as orderered sequences of vertices
   *  @param camera the camera in which this polygon currently exists
   *  @param p0 a vertex on the plane we are projecting too
   *  @param p1 a vertex on the plane we are projecting too
   *  @param p2 a vertex on the plane we are projecting too
   */
  private static PolygonGroup projectToPlane(PolygonGroup polygons, Camera camera, Point p0, Point p1, Point p2)
  {
    PolygonGroup polygons3D = new PolygonGroup();
    Polygon polygon;
    Point norm = Face.normal(p0, p1, p2);
    Point vtmp;    
    Pair<Point,Point> r;
    double distance;
    
    for(int i=0; i<polygons.size(); i++){
      polygon = new Polygon();
      
      for(int j=0; j<polygons.get(i).size(); j++){
        vtmp = polygons.get(i).get(j);
        r = camera.getRay(vtmp.x, vtmp.y);
        distance = -(norm.times(r.first)-norm.times(p0))/norm.times(r.second);
                
        vtmp = new Point();
        vtmp.x = distance*r.second.x + r.first.x;
        vtmp.y = distance*r.second.y + r.first.y;
        vtmp.z = distance*r.second.z + r.first.z;
        polygon.add(vtmp);        
      }
      
      if(!polygon.isEmpty()) polygons3D.add(polygon);
    }
    
    return polygons3D;
  }
  
  /**
   * Return the intersection of the given polygons which lie on the given plane.  
   * Note: this a version specialiaed to the EdgeBin class for debugging purposes!
   *  @param polygons the polygons
   *  @param p0 a vertex on the plane we are projecting too
   *  @param p1 a vertex on the plane we are projecting too
   *  @param p2 a vertex on the plane we are projecting too
   *  @return the intersection of the polygons
   */
  public static PolygonGroup getPlanarIntersection(Vector<PolygonGroup> polygons, Point p0, Point p1, Point p2)
  {
    PolygonGroup intersection = new PolygonGroup();
    Vector<PolygonGroup> polygonsT = new Vector<PolygonGroup>();
    Point norm = Face.normal(p0, p1, p2);
    double[][] M = GMatrixUtility.translate(Point.rotateToZ(norm), -p0.x, -p0.y, -p0.z);   
    double[][] Mi = GMatrixUtility.inverse(M);

    //if(debug_viewer3 != null) debugPolygonIntersections(debug_viewer3, polygons);
    
    //Transform the polygons so that the face normal is aligned with the z axis
    for(int i=0; i<polygons.size(); i++){
      polygonsT.add(PolygonGroup.transform(M, polygons.get(i)));
    }
    
    //View z-coordinates (should all be the same)
    if(false){
      for(int i=0; i<polygonsT.size(); i++){
        for(int j=0; j<polygonsT.get(i).size(); j++){
          for(int k=0; k<polygonsT.get(i).get(j).size(); k++){
            System.out.println("Z (" + i + " of " + polygonsT.size() + "): " + polygonsT.get(i).get(j).get(k).z);
          }
        }
      }
    }
    
    if(debug_viewer3 != null) debugPolygonIntersections(debug_viewer3, polygonsT);

    intersection = polygonsT.get(0);
    
    //Calculate intersection
    if(true){      
      for(int i=1; i<polygonsT.size(); i++){
        intersection = PolygonGroup.getPlanarIntersection(intersection, polygonsT.get(i));
      }
    }
    
    //View intersection
    if(true){
      polygonsT.clear();
      polygonsT.add(intersection);
      
      if(debug_viewer3 != null) debugPolygonIntersections(debug_viewer3, polygonsT);
    }
    
    intersection = PolygonGroup.transform(Mi, intersection);
    
    return intersection;
  }
  
  /**
   * A method to display intersections.
   *  @param r0 a point on a ray from the stored epipole
   *  @param r1 a point on another ray from the stored epipole
   *  @param intersection the intersection information between these rays and another polygon
   */
  private void debugRayIntersections(ImageViewer iv, Point r0, Point r1, Vector<Vector<IntersectionInfo>> intersections)
  {
    int pad = 5;
    int[] img;
    int w, h;
    double offx, offy;
    double minx = Double.MAX_VALUE;
    double maxx = -Double.MAX_VALUE;
    double miny = Double.MAX_VALUE;
    double maxy = -Double.MAX_VALUE;
    Vector<Point> polygon;
    boolean EMPTY = true;
    IntersectionInfo ii;
    Point p;
    
    if(false){
      System.out.println("(Debug) r0: " + r0.toString() + " -> " + getBin(r0));
      System.out.println("(Debug) r1: " + r1.toString() + " -> " + getBin(r1));
    }
    
    //Determine the images width/height
    for(int i=0; i<intersections.size(); i++){
      Iterator<IntersectionInfo> itr = intersections.get(i).iterator();
      
      while(itr.hasNext()){
        ii = itr.next();
        EMPTY = false;
        
        for(int j=0; j<ii.ei.polygon.size(); j++){
          p = ii.ei.polygon.get(j);
          if(p.x < minx) minx = p.x;
          if(p.x > maxx) maxx = p.x;
          if(p.y < miny) miny = p.y;
          if(p.y > maxy) maxy = p.y;
        }
      }
    }
    
    if(EMPTY){
      minx = 0;
      maxx = 100;
      miny = 0;
      maxy = 100;
    }
    
    minx = Math.floor(minx);
    maxx = Math.ceil(maxx);
    miny = Math.floor(miny);
    maxy = Math.ceil(maxy);
    
    minx -= pad;
    maxx += pad;
    miny -= pad;
    maxy += pad;
    
    w = (int)(maxx - minx);
    h = (int)(maxy - miny);
    img = ImageUtility.getNewARGBImage(w, h, 0xffffffff); 
    
    offx = -minx;
    offy = -miny;
    
    //Determine extremes of intersections
    minx = Double.MAX_VALUE;
    maxx = -Double.MAX_VALUE;
    miny = Double.MAX_VALUE;
    maxy = -Double.MAX_VALUE;
    
    for(int i=0; i<intersections.size(); i++){
      Iterator<IntersectionInfo> itr = intersections.get(i).iterator();
      
      while(itr.hasNext()){
        ii = itr.next();
        if(ii.p.x < minx) minx = ii.p.x;
        if(ii.p.x > maxx) maxx = ii.p.x;
        if(ii.p.y < miny) miny = ii.p.y;
        if(ii.p.y > maxy) maxy = ii.p.y;
      }
    }
    
    minx += offx;
    maxx += offx;
    miny += offy;
    maxy += offy;
    
    if(false){
      System.out.println("Width: " + w + ", " + "Height: " + h);
      System.out.println("Intersection Exteremes: " + minx + ", " + miny + ", " + maxx + ", " + maxy);
    }
    
    //Draw the rays
    if(false){
      ImageUtility.drawLine(img, w, h, new Pixel(epipole.x+offx, epipole.y+offy), getBin(r0), 0xff0000ff);
      ImageUtility.drawLine(img, w, h, new Pixel(epipole.x+offx, epipole.y+offy), getBin(r1), 0xff0000ff);
    }else{
      ImageUtility.drawLine(img, w, h, epipole.x+offx, epipole.y+offy, r0.x+offx, r0.y+offy, 0xff0000ff);
      ImageUtility.drawLine(img, w, h, epipole.x+offx, epipole.y+offy, r1.x+offx, r1.y+offy, 0xff0000ff);
    }
    
    //Draw the polygon
    for(int i=0; i<intersections.size(); i++){
      Iterator<IntersectionInfo> itr = intersections.get(i).iterator();

      while(itr.hasNext()){
        ii = itr.next();
        polygon = Point.transform(ii.ei.polygon, 1, 1, offx, offy);
        Point.drawPolygon(img, w, h, polygon, 0xff000000);
      }
    }
    
    ImageUtility.drawBox(img, w, h, minx, miny, maxx, maxy, 0xffff0000);
    iv.add(img, w, h, false);
  }
  
  /**
   * A method to display intersections.
   *  @param r0 a point on a ray from the stored epipole
   *  @param r1 a point on another ray from the stored epipole
   *  @param intersection the intersection information between these rays and another polygon
   */
  private void debugRayIntersectionsZoomed(ImageViewer iv, Point r0, Point r1, Vector<Vector<IntersectionInfo>> intersections)
  {
    int pad = 5;
    int[] img;
    int w, h;
    double offx, offy;
    double minx = Double.MAX_VALUE;
    double maxx = -Double.MAX_VALUE;
    double miny = Double.MAX_VALUE;
    double maxy = -Double.MAX_VALUE;
    double sx, sy, tx, ty;
    boolean EMPTY = true;
    Vector<Point> polygon;
    IntersectionInfo ii;
    Point p;
    Point v0, v1;
    
    //Determine the images width/height
    for(int i=0; i<intersections.size(); i++){
      Iterator<IntersectionInfo> itr = intersections.get(i).iterator();
      
      while(itr.hasNext()){
        ii = itr.next();
        EMPTY = false;
        
        for(int j=0; j<ii.ei.polygon.size(); j++){
          p = ii.ei.polygon.get(j);
          if(p.x < minx) minx = p.x;
          if(p.x > maxx) maxx = p.x;
          if(p.y < miny) miny = p.y;
          if(p.y > maxy) maxy = p.y;
        }
      }
    }
    
    if(EMPTY){
      minx = 0;
      maxx = 100;
      miny = 0;
      maxy = 100;
    }
    
    minx = Math.floor(minx);
    maxx = Math.ceil(maxx);
    miny = Math.floor(miny);
    maxy = Math.ceil(maxy);
    
    minx -= pad;
    maxx += pad;
    miny -= pad;
    maxy += pad;
    
    w = (int)(maxx - minx);
    h = (int)(maxy - miny);
    img = ImageUtility.getNewARGBImage(w, h, 0xffffffff);
    
    offx = -minx;
    offy = -miny;
    
    //Determine extremes of intersections
    minx = Double.MAX_VALUE;
    maxx = -Double.MAX_VALUE;
    miny = Double.MAX_VALUE;
    maxy = -Double.MAX_VALUE;
    
    for(int i=0; i<intersections.size(); i++){
      Iterator<IntersectionInfo> itr = intersections.get(i).iterator();
      
      while(itr.hasNext()){
        ii = itr.next();
        if(ii.p.x < minx) minx = ii.p.x;
        if(ii.p.x > maxx) maxx = ii.p.x;
        if(ii.p.y < miny) miny = ii.p.y;
        if(ii.p.y > maxy) maxy = ii.p.y;
      }
    }
    
    minx += offx;
    maxx += offx;
    miny += offy;
    maxy += offy;
    
    //Adjust transformation to zoom into intersection
    sx = 0.8 * ((double)w) / (maxx-minx);
    sy = 0.8 * ((double)h) / (maxy-miny);
    tx = sx * (offx - minx - (maxx-minx)/2.0) + w/2;
    ty = sy * (offy - miny - (maxy-miny)/2.0) + h/2;
    
    //Draw the rays
    Vector<Point> rays = new Vector<Point>();
    rays.add(new Point(epipole.x, epipole.y, 0));
    rays.add(new Point(r0.x, r0.y, 0));
    rays.add(new Point(r1.x, r1.y, 0));
    
    rays = Point.transform(rays, sx, sy, tx, ty);
    Point.drawPolygon(img, w, h, rays, 0xff0000ff);

    //Draw the polygon
    for(int i=0; i<intersections.size(); i++){
      Iterator<IntersectionInfo> itr = intersections.get(i).iterator();

      while(itr.hasNext()){
        ii = itr.next();
        polygon = Point.transform(ii.ei.polygon, sx, sy, tx, ty);
        Point.drawPolygon(img, w, h, polygon, 0xff000000);
      }
    }
    
    //Draw intersected edges
    //int[] color = {0xff00ff00};
    int[] color = {0xffaa0000, 0xffffff00, 0xff00ff00, 0xff00aa00};
    int c;
    
    for(int i=0; i<intersections.size(); i++){
      Iterator<IntersectionInfo> itr = intersections.get(i).iterator();
      c = 0;
      
      while(itr.hasNext()){
        ii = itr.next();
        
        v0 = new Point(ii.ei.polygon.get(ii.ei.index));
        v1 = new Point(ii.ei.polygon.get((ii.ei.index+1)%ii.ei.polygon.size()));
        
        v0 = v0.transform(sx, sy, tx, ty);
        v1 = v1.transform(sx, sy, tx, ty);
        
        ImageUtility.drawLine(img, w, h, v0.x, v0.y, v1.x, v1.y, color[c]);
        c = (c+1) % color.length;
      }
    }
    
    //Draw the intersections
    for(int i=0; i<intersections.size(); i++){
      Iterator<IntersectionInfo> itr = intersections.get(i).iterator();
      
      while(itr.hasNext()){
        ii = itr.next();
        Point.drawPoint(img, w, h, ii.p.transform(sx, sy, tx, ty), 0xffff0000);
      }
    }
    
    iv.add(img, w, h, false);
  }
  
  /**
   * A method to display 2D polygons and their intersections (ignores z-values).
   *  @param iv the image viewer to display to
   *  @param polygons the polygons
   */
  private static void debugPolygonIntersections(ImageViewer iv, Vector<PolygonGroup> polygons)
  {
    if(iv.getDataSize() < 500){
      int pad = 10;
      int w = 500;
      int h = 300;
      int wp = w + 2*pad;
      int hp = h + 2*pad;
      int[] img = ImageUtility.getNewARGBImage(wp, hp, 0xffffffff);
      int color;

      double minx = Double.MAX_VALUE;
      double maxx = -Double.MAX_VALUE;
      double miny = Double.MAX_VALUE;
      double maxy = -Double.MAX_VALUE;
      boolean EMPTY = true;
      double sclx, scly;
      Vector<Point> polygon;
      Point v, vt;
            
      for(int i=0; i<polygons.size(); i++){
        for(int j=0; j<polygons.get(i).size(); j++){
          for(int k=0; k<polygons.get(i).get(j).size(); k++){
            v = polygons.get(i).get(j).get(k);
            EMPTY = false;
            
            if(v.x < minx) minx = v.x;
            if(v.x > maxx) maxx = v.x;
            if(v.y < miny) miny = v.y;
            if(v.y > maxy) maxy = v.y;
          }
        }
      }
      
      if(!EMPTY){
        sclx = w / (maxx-minx);
        scly = h / (maxy-miny);
        
        for(int i=0; i<polygons.size(); i++){
          color = ImageUtility.getRandomBrightColor();
          
          for(int j=0; j<polygons.get(i).size(); j++){
            polygon = new Vector<Point>();
            
            for(int k=0; k<polygons.get(i).get(j).size(); k++){
              v = polygons.get(i).get(j).get(k);
              vt = new Point((v.x-minx)*sclx+pad, (v.y-miny)*scly+pad, 0);
              polygon.add(vt);
            }
            
            Point.drawPolygon(img, wp, hp, polygon, color);
            Point.drawThickPoints(img, wp, hp, polygon, 1, 0xffff0000);
          }
        }
      }
      
      iv.add(img, wp, hp, false);
      
      if(true){
        System.out.print(iv.getDataSize() + ": " + polygons.size() + " -> ");
        
        for(int i=0; i<polygons.size(); i++){
          if(i != 0) System.out.print(", ");
          System.out.print(polygons.get(i).size());
        }
        
        System.out.println();
      }
    }
  }
}