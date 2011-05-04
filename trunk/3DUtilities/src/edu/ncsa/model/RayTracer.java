package edu.ncsa.model;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.model.MeshAuxiliary.Point;
import edu.ncsa.model.MeshAuxiliary.Color;
import kgm.matrix.*;
import javax.swing.*;
import javax.vecmath.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

/**
 * A modified JFrame for display ray traced renderings of the data stored in
 * the Mesh class.
 *  @author Kenton Mchenry
 */
public class RayTracer extends JFrame
{
  /**
   * A structure for representing 3D rays.
   */
  public class Ray
  {
    public Point p = new Point();
    public Point d = new Point();
  }
  
  private BufferedImage image;
  private int[] pixels;
  private int width;
  private int height;
  private double half_width;
  private double half_height;
  
  Mesh mesh;
    
  private double[][] viewport = new double[4][4];
  private double[][] projection;
  private double[][] modelview;
  private double[][] modelview_i;
  private double[][] world2image;
  private double[][] image2world;
  
  private Point camera_position;
  private Vector<Point> light_positions = new Vector<Point>();
  private Vector<Color> light_colors = new Vector<Color>();
  private Color light_ambient;
  
  private double MIN_DISTANCE = 0.001;   //Threshold to avoid self collisions
  private int RAY_DEPTH_LIMIT = 3;
  private Color BACKGROUND_COLOR = new Color(1, 1, 1);
  private boolean SMOOTH;

  /**
   * Class constructor.
   *  @param m the mesh to render
   *  @param P the current projection matrix
   *  @param M the current modelview matrix
   *  @param w the desired width
   *  @param h the desired height
   *  @param S if true then vertex_normals will be used as opposed to face normals during the rendering
   */
  public RayTracer(Mesh m, double[][] P, double[][] M, int w, int h, boolean S)
  {
    mesh = m;
    SMOOTH = S;
    
    //Setup rendered image
    width = w;
    height = h;
    pixels = new int[width*height];
    image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
   
    //Construct viewport matrix
    half_width = ((double)width) / 2.0;
    half_height = ((double)height) / 2.0; 
    
    viewport[0][0] = half_width;
    viewport[0][3] = half_width;
    viewport[1][1] = -half_height;
    viewport[1][3] = half_height;
    viewport[2][2] = 1;
    viewport[3][3] = 1;
    
     //Initialize all other matrices 
    projection = P;
    modelview = M;
    
    GMatrix Mv = new GMatrix(4, 4, MatrixUtility.to1D(viewport));
    GMatrix Mp = new GMatrix(4, 4, MatrixUtility.to1D(projection));
    GMatrix Mm = new GMatrix(4, 4, MatrixUtility.to1D(modelview));
    GMatrix Mm_i = new GMatrix(Mm);
    Mm_i.invert();
    
    modelview_i = new double[4][4];
    
    for(int i=0; i<4; i++){
      for(int j=0; j<4; j++){
        modelview_i[j][i] = Mm_i.getElement(j, i);
      }
    }
    
    GMatrix Mw2i = new GMatrix(4, 4);
    Mw2i.mul(Mv, Mp);
    Mw2i.mul(Mm);
    GMatrix Mi2w = new GMatrix(Mw2i);
    Mi2w.invert();
    
    world2image = new double[4][4];
    image2world = new double[4][4];
    
    for(int i=0; i<4; i++){
      for(int j=0; j<4; j++){
        world2image[j][i] = Mw2i.getElement(j, i);
        image2world[j][i] = Mi2w.getElement(j, i);
      }
    }
    
    if(true){    //Debug: Print Matrices
      System.out.println();
      System.out.println("Viewport matrix: ");
      System.out.println(MatrixUtility.toString(viewport));
      System.out.println("Projection matrix: ");
      System.out.println(MatrixUtility.toString(projection));
      System.out.println("Modelview matrix: ");
      System.out.println(MatrixUtility.toString(modelview));
    }
    
    //Initialize Camera/Lights
    camera_position = new Point(0.0f, 0.0f, 0.0f);
    
    Point tmpv = new Point(500.0f, -500.0f, 0.0f);
    tmpv = Point.transform(modelview_i, tmpv);
    light_positions.add(tmpv);
    light_colors.add(new Color(0.7f, 0.7f, 0.7f));
    light_ambient = new Color(0.1f, 0.1f, 0.1f);
    
    //Setup Frame
    setTitle("Ray Trace");
    setSize(width, height+30);
    setVisible(true);
    
    clear(255, 255, 255);
    paint(this.getGraphics());
    
    //drawPoints();
    rayTrace();
  }
  
  /**
   * Clear the rendering with the specfied color.
   *  @param r the red component
   *  @param g the green component
   *  @param b the blue component
   */
  public void clear(int r, int g, int b)
  {
    for(int x=0; x<width; x++){
      for(int y=0; y<height; y++){
        pixels[y*width+x] = 0xff000000 | r<<16 | g<<8 | b;
      }
    }
  }
  
  /**
   * Draw the current rendering to the given graphics context.
   *  @param g the graphics context to draw to
   */
  public void paint(Graphics g)
  {
    //g.clearRect(0, 0, width, height);
    image.setRGB(0, 0, width, height, pixels, 0, width);
    g.drawImage(image, 0, 30, this);
  }
  
  /**
   * Draw the meshes transformed vertices.  Note no ray tracing is involved or needed for this process.
   */
  public void drawPoints()
  {
    Vector<Point> p = new Vector<Point>();
    int x, y;
    int minu, maxu, minv, maxv;
    int r = 1;
    
    for(int i=0; i<mesh.getVertices().size(); i++){
      p.add(new Point(mesh.getVertices().get(i)));
    }
    
    p = Point.transform(modelview, p);
    p = Point.transform(projection, p);
    p = Point.transform(viewport, p);
    
    for(int i=0; i<p.size(); i++){
      x = (int)Math.round(p.get(i).x);
      y = (int)Math.round(p.get(i).y);
      
      if(x>=0 && x<width && y>=0 && y<height){
        minu = x-r;
        maxu = x+r;
        minv = y-r;
        maxv = y+r;
        
        if(minu < 0) minu = 0;
        if(maxu >= width) maxu = width-1;
        if(minv < 0) minv = 0;
        if(maxv >= height) maxv = height-1;
       
        for(int u=minu; u<=maxu; u++){
          for(int v=minv; v<=maxv; v++){
            pixels[v*width+u] = 0xff000000;
          }
        }
      }
    }
  }
  
  /**
   * Reverse ray trace every pixel of the rendered image.
   */
  public void rayTrace()
  {
    for(int y=0; y<height; y++){   
      for(int x=0; x<width; x++){
        pixels[y*width+x] = trace(eyeRay(x,y), 0).getInt();
      }
      
      if(y%2 == 0) paint(this.getGraphics());
    }
    
    paint(this.getGraphics());
  }
  
  /**
   * Obtain the color received from the reverse ray specified.
   *  @param r the ray
   *  @param depth the number of reflections to consider
   *  @return the color seen from this ray
   */
  public Color trace(Ray r, int depth)
  {
    double d;
    int min_i = -1;
    double min_d = 1e100;

    for(int i=0; i<mesh.getFaces().size(); i++){
      d = computeIntersection_face(r, i);
      
      if(d>MIN_DISTANCE && d<min_d){
        min_i = i;
        min_d = d;
      }
    }

    if(min_i!=-1 && depth<=RAY_DEPTH_LIMIT){
      return shade(min_i, r, min_d, depth+1);
    }
    
    return BACKGROUND_COLOR;
  }
  
  /**
   * Get the color/shade of the specified face when viewed along the specified ray.
   *  @param face the face interesecting the ray
   *  @param r the ray
   *  @param distance the distance of the face from the camera along the ray
   *  @param depth the number of reflections so far
   *  @return the contributed color from this face
   */
  public Color shade(int face, Ray r, double distance, int depth)
  {
    Point P = r.p.plus(r.d.times(distance));
    Color color = light_ambient.plus(mesh.getFaceMaterial(face).emissive);
    Color tmpc;
    
    for(int i=0; i<light_positions.size(); i++){
      if(!obstructed(P, i)){
        color.plusEquals(shadePhong(face, P, i));
      }
    }
    
    if(false){
      tmpc = trace(reflectedRay(face, r, P), depth);
      color.r += mesh.getFaceMaterial(face).specular.r * tmpc.r;
      color.g += mesh.getFaceMaterial(face).specular.g * tmpc.g;
      color.b += mesh.getFaceMaterial(face).specular.b * tmpc.b;  
    }
    
    if(mesh.getFaceMaterial(face).transmissive != null){
      tmpc = trace(transmittedRay(face, r, P), depth);
      color.r += mesh.getFaceMaterial(face).transmissive.r * tmpc.r;
      color.g += mesh.getFaceMaterial(face).transmissive.g * tmpc.g;
      color.b += mesh.getFaceMaterial(face).transmissive.b * tmpc.b;
    }
    
    color.cap(1);

    return color;
  }
  
  /**
   * The diffuse phong shading of a specified point on a specified face.
   *  @param face the face
   *  @param P a point on this face
   *  @param light the index of the light source
   *  @return the phone shaded color of the point
   */
  public Color shadePhong(int face, Point P, int light)
  {
    Color color = new Color(0, 0, 0);
    double angle;

    Point N;
    
    if(!SMOOTH){
      N = mesh.getFaceNormal(face);
    }else{
      N = interpolatedNormal(face, P);
    }
    
    Point V = P.minus(camera_position);
    V.divideEquals(V.magnitude());

    Point L = light_positions.get(light).minus(P);
    L.divideEquals(L.magnitude());
    
    double NL = N.times(L);
    
    Point R = L.minus(N.times(2.0f*NL));
    R.divideEquals(R.magnitude());   
    
    double RV = R.times(V);
   
    //Diffuse
    angle = Math.acos(NL) * 180.0 / Math.PI;

    if(Math.abs(angle) < 90.0){ 
      color.r += light_colors.get(light).r * NL*mesh.getFaceMaterial(face).diffuse.r;
      color.g += light_colors.get(light).g * NL*mesh.getFaceMaterial(face).diffuse.g;
      color.b += light_colors.get(light).b * NL*mesh.getFaceMaterial(face).diffuse.b;
    }

    //Specular
    angle = Math.acos(RV) * 180.0 / Math.PI;
    
    if(Math.abs(angle) < 90.0){
      color.r += light_colors.get(light).r * mesh.getFaceMaterial(face).specular.r*Math.pow(RV, mesh.getFaceMaterial(face).shininess);
      color.g += light_colors.get(light).g * mesh.getFaceMaterial(face).specular.g*Math.pow(RV, mesh.getFaceMaterial(face).shininess);
      color.b += light_colors.get(light).b * mesh.getFaceMaterial(face).specular.b*Math.pow(RV, mesh.getFaceMaterial(face).shininess);
    }

    return color;
  }
  
  /**
   * Determine if the specified point is obstructed from the specified light source.  This is used
   * to determin if a point is in shadow.
   *  @param P a point
   *  @param light the index of the light to check
   *  @return true if the points is obstructed from the light
   */
  boolean obstructed(Point P, int light)
  {
    Ray r = shadowRay(P, light);
    Point PL = light_positions.get(light).minus(P);
    double min_d = 1e100;
    double tmpd;
    
    for(int i=0; i<mesh.getFaces().size(); i++){
      tmpd = computeIntersection_face(r, i);

      if(tmpd>MIN_DISTANCE && tmpd<min_d){
        min_d = tmpd;
      }
    }

    if((PL.magnitude()-min_d) > 0){
      return true;
    }else{
      return false;
    }
  }
  
  /**
   * Create a ray from the camera/eye through the pixel (x,y)
   *  @param x the x component of the pixel
   *  @param y the y component of the pixel
   *  @return the eye ray
   */
  public Ray eyeRay(int x, int y)
  {
    Ray r = new Ray();   
    
    Point pc = camera_position;    
    Point qs = new Point(((double)x)+0.5, ((double)y)+0.5, 0.0);
    Point pw = Point.transform(modelview_i, pc);
    Point qw = Point.transform(image2world, qs);

    r.p = pw;
    r.d = qw.minus(pw);
    r.d.divideEquals(r.d.magnitude());
    
    return r;
  }
  
  /**
   * Create a ray from a point to the light source so as to check if its occluded (thus in shadow).
   *  @param P the point
   *  @param light the index of the light source
   *  @return the ray from the point to the light source
   */
  public Ray shadowRay(Point P, int light)
  {
    Ray r = new Ray();
    r.p = new Point(P);
    r.d = light_positions.get(light).minus(P);
    r.d.divideEquals(r.d.magnitude());

    return r;
  }
  
  /**
   * Create a reflected ray based on the given ray and a faces normal.
   *  @param face the face intersecting the ray
   *  @param r0 the ray
   *  @param P the point of contact between the ray and the face
   *  @return the refleciton ray
   */
  public Ray reflectedRay(int face, Ray r0, Point P)
  {
    Ray r1 = new Ray();
    Point I = r0.d;
    Point N;
    
    if(!SMOOTH){
      N = mesh.getFaceNormal(face);
    }else{
      N = interpolatedNormal(face, P);
    }

    r1.p = new Point(P);
    r1.d = I.minus(N.times(2.0*N.times(I)));
    r1.d.divideEquals(r1.d.magnitude());

    return r1;
  }
  
  /**
   * Create a ray passing thourgh a face, distored by snell's law.
   *  @param face the face
   *  @param r0 the intersecting ray
   *  @param P the point where the ray hits the face
   *  @return the transmitted ray
   */
  public Ray transmittedRay(int face, Ray r0, Point P)
  {
    Ray r1 = new Ray();  
    Point I = r0.d;
    Point N;
    
    if(!SMOOTH){
      N = mesh.getFaceNormal(face);
    }else{
      N = interpolatedNormal(face, P);
    }
    
    double c = -N.times(I);
    double n = 1.0 / mesh.getFaceMaterial(face).index_of_refraction;
    
    r1.p = new Point(P);
    r1.d = (I.times(n)).plus(N.times(n*c - Math.sqrt(1.0+n*n*(c*c-1.0))));
    r1.d.divideEquals(r1.d.magnitude());

    return r1;
  }
  
  /**
   * Determine if the given ray intersects with the specfied face (assumed to be convex!).
   *  @param r the ray
   *  @param face the face index
   *  @return the distance to the point of collision (large if no intersection occurs)
   */
  double computeIntersection_face(Ray r, int face)
  {
    boolean HIT = true;
    double sign = 0; 
    double distance = 1e100;
    double tmpd;
    
    Face f = mesh.getFace(face);
    Point norm = mesh.getFaceNormal(face);

    //Check for intersection
    for(int i=0; i<f.v.length; i++){
      tmpd = Face.normal(r.p, mesh.getVertex(f.v[i]), mesh.getVertex(f.v[(i+1)%f.v.length])).times(r.d);

      if(i == 0){
        sign = tmpd;
      }else if((sign<0 && tmpd>0) || (sign>0 && tmpd<0)){
        HIT = false;
        break;
      }
    }

    if(HIT){
      tmpd = -norm.times(mesh.getVertex(mesh.getFace(face).v[0]));
      distance = -(norm.times(r.p)+tmpd)/norm.times(r.d);
    }

    return distance;
  }
  
  /**
   * Interpolate vertex normals to find the normal of a point within a face.
   *  @param face the face
   *  @param P a point on the face
   *  @return the points interpolated normal
   */
  public Point interpolatedNormal(int face, Point P)
  {
    Point v0 = mesh.getVertex(mesh.getFace(face).v[0]);
    Point v1 = mesh.getVertex(mesh.getFace(face).v[1]);
    Point v2 = mesh.getVertex(mesh.getFace(face).v[2]);
    Point n0 = mesh.getVertexNormal(mesh.getFace(face).v[0]);
    Point n1 = mesh.getVertexNormal(mesh.getFace(face).v[1]);
    Point n2 = mesh.getVertexNormal(mesh.getFace(face).v[2]);
    
    double a = Point.cross(v0, v1, v2).magnitude() / 2.0;
    double a0 = Point.cross(P, v1, v2).magnitude() / 2.0;
    double a1 = Point.cross(v0, P, v2).magnitude() / 2.0;
    //double a2 = Vertex.crossProduct(v0, v1, P).distance() / 2.0;
      
    double u = a0 / a;
    double v = a1 / a;
    //double w = a2 / a;
    double w = 1 - u - v;
    
    Point n = new Point();
    n.plusEquals(n0.times(u));
    n.plusEquals(n1.times(v));
    n.plusEquals(n2.times(w));
    n.divideEquals(n.magnitude());
    
    return n;
  }
}
