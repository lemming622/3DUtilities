package edu.ncsa.model.graphics.j3d.basic;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.model.MeshAuxiliary.Point;
import edu.ncsa.model.graphics.*;
import kgm.utility.*;
import edu.ncsa.model.MeshLoader.ProgressEvent;
import javax.swing.*;
import javax.vecmath.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.media.j3d.*;
import javax.media.j3d.Material;
import com.sun.j3d.utils.behaviors.vp.*;
import com.sun.j3d.utils.behaviors.mouse.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.*;

/**
 * A panel that allows for the display and manipulation of 3D objects.
 * @author Kenton McHenry
 */
public class ModelViewer extends AbstractModelViewer implements ActionListener, MouseListener
{
  public Mesh mesh = new Mesh();

	public String load_path = "./";
  public int width = 600;                //Window width
  public int height = 600;               //Window height
  private boolean LOAD_DEFAULT = true;

  private Canvas3D canvas3D = null;
  private JPopupMenu popup_menu;
  private JMenuItem menuitem_OPEN;
  private JMenuItem menuitem_QUIT;

  /**
   * Class constructor specifying the INI file to load.
   * @param filename INI file name containing initialization values
   */
  public ModelViewer(String filename)
  {
  	this(filename, -1, -1, true);
  }
  
  /**
   * Class constructor.
   * @param filename INI file name containing initialization values
   * @param w width of viewer
   * @param h height of viewer
   * @param LOAD_DEFAULT if false the viewer will not load the default model from the INI file
   */
  public ModelViewer(String filename, int w, int h, boolean LOAD_DEFAULT)
  {
    if(w >= 0 && h >= 0){
      width = w;
      height = h;
    }
    
    this.LOAD_DEFAULT = LOAD_DEFAULT;
    	 
    if(filename != null){
      try{
        loadINI(new FileInputStream(filename));
      }catch(Exception e){}
    }
    
    setBackground(java.awt.Color.white);
    this.setBorder(null);
    setSize(width, height);
    setLayout(null);
  
    setPopupMenu();
  }
  
  /**
   * Class constructor specifying INI file, initial dimensions and whether or not
   * to load the default model from the INI file or not.  The construct also builds the pop
   * up menu and starts a thread used to refresh the scene.
   * @param filename INI file name containing initialization values
   * @param w width of viewer
   * @param h height of viewer
   * @param DISABLE_HEAVYWEIGHT disable heavy-weight canvas (not used!)
   * @param LOAD_DEFAULT if false the viewer will not load the default model from the INI file
   */
  public ModelViewer(String filename, int w, int h, boolean DISABLE_HEAVYWEIGHT, boolean LOAD_DEFAULT)
  {
  	this(filename, w, h, LOAD_DEFAULT);
  }

	/**
	 * Load initialization file containing initial values for the viewer.
	 * @param fis the file input stream (note this is friendly to applets!)
	 */
	public void loadINI(FileInputStream fis)
	{
	  try{
	    BufferedReader ins = new BufferedReader(new InputStreamReader(fis));
	    String line;
	    String key;
	    String value;
	    
	    while((line=ins.readLine()) != null){
	      if(line.contains("=")){
	        key = line.substring(0, line.indexOf('='));
	        value = line.substring(line.indexOf('=')+1);
	        
	        if(key.charAt(0) != '#'){
	          if(key.equals("DefaultModel")){
	            if(LOAD_DEFAULT){
	            	load(value);
	            }
	          }else if(key.equals("LoadPath")){
	            load_path = value + "/";
	          }
	        }
	      }
	    }
	    
	    ins.close();
	  }catch(Exception e) {e.printStackTrace();}
	}

	/**
   * Set the popup menu.
   */
  private void setPopupMenu()
  {
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    
    popup_menu = new JPopupMenu(); 
    menuitem_OPEN = new JMenuItem("Open"); menuitem_OPEN.addActionListener(this); popup_menu.add(menuitem_OPEN);
    popup_menu.addSeparator();
    menuitem_QUIT = new JMenuItem("Quit"); menuitem_QUIT.addActionListener(this); popup_menu.add(menuitem_QUIT);
  }
  
	/**
	 * Convert a mesh into a Java3D branch group.
	 * @param mesh the mesh to convert
	 * @return the resulting branch group
	 */
	private BranchGroup meshToBranchGroup(Mesh mesh)
	{		
		BranchGroup bg = new BranchGroup();
		Vector<Point> vertices = mesh.getVertices();
		Vector<Face> faces = mesh.getFaces();
		Point3f[] nodes = new Point3f[vertices.size()];
		int[] indices = new int[faces.size() * 3];
		
		//Transfer vertex and face data
		for(int i=0; i<vertices.size(); i++){
			nodes[i] = new Point3f((float)vertices.get(i).x, (float)vertices.get(i).y, (float)vertices.get(i).z);
		}
		
		for(int i=0; i<faces.size(); i++){
			indices[i*3 + 0] = faces.get(i).v[0];
			indices[i*3 + 1] = faces.get(i).v[1];
			indices[i*3 + 2] = faces.get(i).v[2];
		}
		
		//Create node
		GeometryInfo gi = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
		gi.setCoordinates(nodes);
		gi.setCoordinateIndices(indices);
		NormalGenerator ng = new NormalGenerator();
		ng.generateNormals(gi);
		
		Shape3D shape = new Shape3D((IndexedTriangleArray)gi.getIndexedGeometryArray());
		Appearance appearance = new Appearance();
		PolygonAttributes attributes = new PolygonAttributes();
		attributes.setCullFace(PolygonAttributes.CULL_NONE);
		attributes.setBackFaceNormalFlip(true);
		appearance.setPolygonAttributes(attributes);
		Material material = new Material();
		material.setDiffuseColor(new Color3f(0.5f, 0.5f, 0.5f));
		appearance.setMaterial(material);
		shape.setAppearance(appearance);
		bg.addChild(shape);
		
		return bg;
	}

	/**
	 * Set the mesh structure.
	 * @param mesh the mesh to display
	 */
	public void setMesh(Mesh mesh)
	{
		this.mesh = mesh;
		
	  //Set up Java 3D canvas		
	  GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
	  if(canvas3D != null) remove(canvas3D);
	  canvas3D = new Canvas3D(config);
	  canvas3D.setSize(width, height);	  
	  canvas3D.addMouseListener(this);
	  canvas3D.setFocusable(true);
	  canvas3D.requestFocus();       
	  add("Center", canvas3D);

	  BranchGroup bg = new BranchGroup();
	  Bounds bounds = new BoundingSphere(new Point3d(0, 0, 0), mesh.getRadius());
	  
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
	
	  DirectionalLight light1 = new DirectionalLight(white, new Vector3f(-1.0f, -1.0f, -1.0f));
	  light1.setInfluencingBounds(bounds);
	  bg.addChild(light1);	  
	  
	  DirectionalLight light2 = new DirectionalLight(white, new Vector3f(1.0f, -1.0f, 1.0f));
	  light2.setInfluencingBounds(bounds);
	  bg.addChild(light2);
	      
	  //Set mouse rotation behavior (to move object directly)
	  TransformGroup tg_m = new TransformGroup();
	  tg_m.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
	  tg_m.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	  
	  MouseRotate mouse_rotate = new MouseRotate();
	  mouse_rotate.setSchedulingBounds(bounds);	  
	  mouse_rotate.setTransformGroup(tg_m);
	  tg_m.addChild(mouse_rotate);
	  
	  //Add the model
	  Transform3D t3d_t = new Transform3D();
	  t3d_t.setTranslation(new Vector3d(-mesh.getCenter().x, -mesh.getCenter().y, -mesh.getCenter().z));
	  TransformGroup tg_t = new TransformGroup(t3d_t);
	  Transform3D t3d_s = new Transform3D(); 
	  t3d_s.setScale(0.8*1.0/mesh.getRadius());	
	  TransformGroup tg_s = new TransformGroup(t3d_s);
	  tg_t.addChild(meshToBranchGroup(mesh));
	  tg_s.addChild(tg_t);
	  tg_m.addChild(tg_s);
	  bg.addChild(tg_m);
	  
	  //Setup the universe	  
	  SimpleUniverse universe = new SimpleUniverse(canvas3D);	
	  ViewingPlatform vp = universe.getViewingPlatform();
	  universe.addBranchGraph(bg);

	  //Set the users position
	  //canvas3D.getView().setProjectionPolicy(View.PARALLEL_PROJECTION);
	  canvas3D.getView().setProjectionPolicy(View.PERSPECTIVE_PROJECTION);
	  TransformGroup tg_view = vp.getViewPlatformTransform();
	  Transform3D t3d = new Transform3D();
	  tg_view.getTransform(t3d);
	  //t3d.lookAt(new Point3d(0, 0, 10), new Point3d(0,0,0), new Vector3d(0,1,0));
	  t3d.lookAt(new Point3d(0, 0, 3), new Point3d(0,0,0), new Vector3d(0,1,0));
	  t3d.invert();
	  tg_view.setTransform(t3d);
	
	  //Set up the users controls for translation and zooming (ok to move camera for these)
	  OrbitBehavior orbit = new OrbitBehavior(canvas3D, OrbitBehavior.DISABLE_ROTATE | OrbitBehavior.REVERSE_TRANSLATE);
	  orbit.setSchedulingBounds(bounds);
	  vp.setViewPlatformBehavior(orbit);
	}

	/**
   * Load model into our mesh structure.
   * @param filename the absolute name of the file
   * @param progressCallBack the callback handling progress updates
   */
  public void load(String filename, ProgressEvent progressCallBack)
  {
  	Mesh mesh = new AnimatedMesh();		//Must be an AnimatedMesh to allow them to load!
    long t0 = 0, t1 = 0;
    double dt;
    
    if(filename.length() > 0){
      System.out.print("Loading: " + Utility.getFilename(filename) + "... ");
      
      t0 = System.currentTimeMillis();
    	mesh.load(filename, progressCallBack);
    	setMesh(mesh);
      t1 = System.currentTimeMillis();
      dt = t1 - t0;	
      
      System.out.println("\t[Loaded in " + dt/1000.0 + "s]");
    }
  }
  
	/**
   * Load model into our mesh structure.
   * @param filename the absolute name of the file
   */
  public void load(String filename)
  {
  	load(filename, null);
  }
  	
  /**
   * Listener for action events.
   * @param e the action event
   */
  public void actionPerformed(ActionEvent e)
	{
    JMenuItem source = (JMenuItem)e.getSource();
    
    if(source == menuitem_OPEN){
      JFileChooser fc = new JFileChooser(load_path);
      
      if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
        String pathname = Utility.unixPath(fc.getCurrentDirectory().getAbsolutePath()) + "/";
        String filename = fc.getSelectedFile().getName();
        load(pathname + filename);
      }
    }else if(source == menuitem_QUIT){
    	System.exit(0);
    }
	}

	/**
   * Listener for mouse pressed events.
   * @param e the mouse event
   */
	public void mousePressed(MouseEvent e) 
  {
  	if(e.getButton() == MouseEvent.BUTTON3){
    	popup_menu.show(e.getComponent(), e.getX(), e.getY());
  	}
  }

	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	
	/**
   * The main function used if this class is run by itself.
   * @param args not used
   */
  public static void main(String args[])
  {
    ModelViewer viewer = new ModelViewer("ModelViewer.ini");
    
    JFrame frame = new JFrame("Model Viewer");
    frame.setSize(viewer.width+9, viewer.height+35);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(viewer);
    frame.setVisible(true);
  }
}
