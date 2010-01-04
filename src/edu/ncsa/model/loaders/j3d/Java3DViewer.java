package edu.ncsa.model.loaders.j3d;
import javax.swing.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.behaviors.vp.*;

/**
 * A modified JFrame for displaying Java3D scenes.
 *  @author Kenton McHenry
 */
public class Java3DViewer extends JFrame
{
  private static final int width = 512;
  private static final int height = 512; 
  private static final Point3d user_position = new Point3d(0, 0, 10);
  private double radius = 0;
  private Point3d center = new Point3d();

  /**
   * Class constructor.
   *  @param model a Java3D scene to display
   */
  public Java3DViewer(BranchGroup model)
  {
    //Set up Java 3D canvas
    GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
    Canvas3D canvas3D = new Canvas3D(config);
    add("Center", canvas3D);
    canvas3D.setFocusable(true);
    canvas3D.requestFocus();
    
    //Get bounding sphere and scale
    radius = Math.abs(((BoundingSphere)model.getBounds()).getRadius());
    ((BoundingSphere)model.getBounds()).getCenter(center);
    Bounds bounds = new BoundingSphere(new Point3d(0, 0, 0), radius);
    Transform3D t3d_t = new Transform3D();
    t3d_t.setTranslation(new Vector3d(-center.x, -center.y, -center.z));
    TransformGroup tg_t = new TransformGroup(t3d_t);
    Transform3D t3d_s = new Transform3D(); 
    t3d_s.setScale(1.0/radius);
    TransformGroup tg_s = new TransformGroup(t3d_s);
    System.out.println("\nRadius: " + radius);
    System.out.println("Center: " + center.toString());
       
    //Prepare scene
    SimpleUniverse su = new SimpleUniverse(canvas3D);
    BranchGroup bg = new BranchGroup();
    
    //Set up background
    Background back = new Background();
    back.setApplicationBounds(bounds);
    back.setColor(1.0f, 1.0f, 1.0f);
    bg.addChild(back);
    
    //Set up lighting
    Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
    AmbientLight ambientLightNode = new AmbientLight(white);
    ambientLightNode.setInfluencingBounds(bounds);
    bg.addChild(ambientLightNode);

    Vector3f light1Direction = new Vector3f(-1.0f, -1.0f, -1.0f);
    Vector3f light2Direction = new Vector3f(1.0f, -1.0f, 1.0f);
    DirectionalLight light1 = new DirectionalLight(white, light1Direction);
    light1.setInfluencingBounds(bounds);
    bg.addChild(light1);

    DirectionalLight light2 = new DirectionalLight(white, light2Direction);
    light2.setInfluencingBounds(bounds);
    bg.addChild(light2);
    
    //Add the model
    tg_t.addChild(model);
    tg_s.addChild(tg_t);
    bg.addChild(tg_s);
    
    //Set the users position
    canvas3D.getView().setProjectionPolicy(View.PARALLEL_PROJECTION);
    ViewingPlatform vp = su.getViewingPlatform();
    TransformGroup steerTG = vp.getViewPlatformTransform();
    Transform3D t3d = new Transform3D();
    steerTG.getTransform(t3d);
    t3d.lookAt(user_position, new Point3d(0,0,0), new Vector3d(0,1,0));
    t3d.invert();
    steerTG.setTransform(t3d);

    //Set up the users controls
    OrbitBehavior orbit = new OrbitBehavior(canvas3D, OrbitBehavior.REVERSE_ALL);
    orbit.setSchedulingBounds(bounds);
    vp = su.getViewingPlatform();
    vp.setViewPlatformBehavior(orbit);
    
    //Add scene to universe
    su.addBranchGraph(bg);
    
    //Set up frame
    setTitle("Java 3D Viewer");
    setSize(width, height+30);
    setVisible(true);
  }
}