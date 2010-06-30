package edu.ncsa.model;
import edu.ncsa.model.Mesh.*;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.model.MeshAuxiliary.Color;
import edu.ncsa.model.MeshAuxiliary.RigidTransformation;
import edu.ncsa.model.MeshAuxiliary.Point;
import edu.ncsa.model.MeshAuxiliary.Face;
import edu.ncsa.model.MeshAuxiliary.Texture;
import edu.ncsa.model.MeshAuxiliary.Material;
import edu.ncsa.image.*;
import edu.ncsa.matrix.*;
import edu.ncsa.utility.*;
import edu.ncsa.model.MeshLoader.ProgressEvent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import javax.imageio.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;

/**
 * A panel that allows for the display and manipulation of 3D objects.
 *  @author Kenton McHenry
 */
public class ModelViewer extends JPanel implements Runnable, GLEventListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, ActionListener
{
  public Mesh mesh = new Mesh();
  public double[][] rotation_last = MatrixUtility.eye(4);
  public double[][] rotation_last_inv = null;
  public RigidTransformation transformation = new RigidTransformation();
  private Class Signature = null;
  private int list_id = 0;
  
	private Vector<Mesh> added_meshes = new Vector<Mesh>();
	private Vector<double[][]> added_rotation_last = new Vector<double[][]>();
	private Vector<RigidTransformation> added_transformations = new Vector<RigidTransformation>();
	private double[][] selected_rotation_last = rotation_last;	
	private RigidTransformation selected_transformation = transformation;

	private float cz = 2000;   	 					 //Camera position  
  private float adj_tx = 0;							 //Model adjustment in the x-direction
  private float adj_ty = 0;							 //Model adjustment in the y-direction
  private float adj_scl = 1;						 //Model scale adjustment
  
  private float[] light0_position = {0.0f, 0.0f, 1.0f, 0.0f};
  private float[] light1_position = {0.0f, 0.0f, -1.0f, 0.0f};
  private float[] light_diff = {0.7f, 0.7f, 0.7f, 1.0f};    
  private float[] model_ambient = {0.5f, 0.5f, 0.5f, 1.0f}; 
  
  //Declared here to prevent from being re-allocated in display()!
  private double[] projection_tmp = new double[16];
  private double[] modelview_tmp = new double[16];
  private double[][] projection = new double[4][4];
  private double[][] modelview = new double[4][4];
  private double[][] modelview_zeroT = new double[4][4];
  
	public String load_path = "./";
  private String export_path = "./";
  private String metadata_path = "./";
  
  public int width = 600;                //Window width
  public int height = 600;               //Window height
  private int halfwidth = width / 2;
  private int halfheight = height / 2;
  private double vleft = -width / 2.0;   //Left of viewport
  private double vright = width / 2.0;   //Right
  private double vbottom = height / 2.0; //Bottom
  private double vtop = -height / 2.0;   //Top
  private double vnear = -100000;         //Near clipping plane (default: 1000)
  private double vfar = 100000;           //Far clipping plane  (default: 1000)
  
  protected int clicked_button = 0;
  protected int last_x;
  protected int last_y;
  
  public Component canvas;
  private JPopupMenu popup_menu;
  private JMenuItem menuitem_OPEN;
  private JMenuItem menuitem_ADD;
  private JMenuItem menuitem_EXPORT_JPG;
  private JMenuItem menuitem_EXPORT_OBJ;
  private JMenuItem menuitem_EXPORT_OBJ_BIN1;
  private JMenuItem menuitem_EXPORT_OBJ_BIN2;
  private JMenuItem menuitem_EXPORT_PLY;
  private JMenuItem menuitem_EXPORT_VH;
  private JMenuItem menuitem_EXPORT_WRL;
  private JMenuItem menuitem_EXPORT_DEPTH;
  private JMenuItem menuitem_EXPORT_POINTS_CAMERAS;
  private JMenuItem menuitem_QUIT;
  private JCheckBoxMenuItem menuitem_ORTHO;
  private JCheckBoxMenuItem menuitem_AXIS;
  private JCheckBoxMenuItem menuitem_PC_AXIS;
  private JCheckBoxMenuItem menuitem_POINTS;
  private JCheckBoxMenuItem menuitem_WIRE;
  private JCheckBoxMenuItem menuitem_OUTLINE;
  private JRadioButtonMenuItem menuitem_TRANSPARENT;
  private JRadioButtonMenuItem menuitem_SOLID;
  private JRadioButtonMenuItem menuitem_SHADED;
  private JCheckBoxMenuItem menuitem_SHADED_SMOOTH;
  private JRadioButtonMenuItem menuitem_SHADED_LIGHTING_DISABLED;
  private JRadioButtonMenuItem menuitem_SHADED_LIGHTING_ENABLED;
  private JRadioButtonMenuItem menuitem_SHADED_LIGHTING_MATERIAL;
  private JRadioButtonMenuItem menuitem_SHADED_TEXTURE_DISABLED;
  private JRadioButtonMenuItem menuitem_SHADED_TEXTURE_DECAL;
  private JRadioButtonMenuItem menuitem_SHADED_TEXTURE_MODULATE;
  private JRadioButtonMenuItem menuitem_HIGHLIGHTS;
  private JRadioButtonMenuItem menuitem_ILLUSTRATION;
  private JRadioButtonMenuItem menuitem_METAL;
  private JMenuItem menuitem_RAYTRACE;
  private JMenuItem menuitem_VIEW_GROUPS_ALL;
  private Vector<JCheckBoxMenuItem> menuitem_VIEW_GROUPS = new Vector<JCheckBoxMenuItem>();
  private Vector<JRadioButtonMenuItem> menuitem_MESHES = new Vector<JRadioButtonMenuItem>();
  private JMenuItem menuitem_SELECT_BOUNDING_BOX;
  private JMenuItem menuitem_SELECT_HIGHLIGHT;
  private JMenuItem menuitem_SELECT_COMPONENT;
  private Vector<JMenuItem> menuitem_SELECT_GROUPS = new Vector<JMenuItem>();
  private JRadioButtonMenuItem menuitem_SELECT_FOR_NOTHING;
  private JRadioButtonMenuItem menuitem_SELECT_FOR_JOINT;
  private JRadioButtonMenuItem menuitem_SELECT_FOR_COLOR;
  private JRadioButtonMenuItem menuitem_SELECT_FOR_SMOOTH;
  private JCheckBoxMenuItem menuitem_SELECT_USING_DEPTH;
  private JMenuItem menuitem_SIMPLIFY;
  private JMenuItem menuitem_SUBDIVIDE;

  private boolean LOAD_DEFAULT = true;
  public boolean ADJUST = true; 
  private boolean ORTHO = true;
  private boolean AXIS = true;
  private boolean PC_AXIS = false;
  private boolean POINTS = false;
  private boolean WIRE = false;
  private boolean OUTLINE = false;
  private boolean TRANSPARENT = false;
  private boolean SOLID = false;
  private boolean SHADED = true;
  private boolean SHADED_SMOOTH = false;
  private boolean SHADED_LIGHTING_DISABLED = false;
  private boolean SHADED_LIGHTING_ENABLED = true;
  private boolean SHADED_LIGHTING_MATERIAL = false;
  private boolean SHADED_TEXTURE_DISABLED = false;
  private boolean SHADED_TEXTURE_DECAL = true;
  private boolean SHADED_TEXTURE_MODULATE = false;
  private boolean HIGHLIGHTS = false;
  private boolean ILLUSTRATION = false;
  private boolean METAL = false;
  private boolean REBUILD_SIGNATURES = false;
  private boolean RAYTRACE = false;  
  private boolean ANTI_ALIASING = false;
  public boolean AUTO_REFRESH = false;
  
  public boolean RUNNING = true;  
  private boolean UPDATE_CAMERA = false;
  protected boolean REFRESH = true;
  private Object refresh_lock = new Object();
  
  private boolean SAVE_IMAGE = false;
  private boolean SAVE_VISUAL_HULL_VIEW = false;
  private boolean SAVE_AXIS = false;
  private boolean SAVE_DEPTH = false;
  private boolean SAVE_POINTS_CAMERA = false;
  private String output_name = "";
  private boolean GRAB_IMAGE = false;
  private int[] grabbed_image = null;
  
  private boolean SELECT_USING_DEPTH = false;
  private boolean SELECT_FOR_NOTHING = true;
  private boolean SELECT_FOR_JOINT = false;
  private boolean SELECT_FOR_COLOR = false;
  private boolean SELECT_FOR_SMOOTH = false;
  private boolean SELECT_BOUNDING_BOX = false;
  private boolean SELECT_COMPONENT = false;
  private boolean SELECT_HIGHLIGHT = false;
  private boolean SELECTING = false;
  private Point selection_p0 = null;
  private Point selection_p1 = null;
	private double selection_minx;
	private double selection_maxx;
	private double selection_miny;
	private double selection_maxy;
	private TreeSet<Integer> visible_faces = null;
	private Vector<Integer> selected_vertices = null;
	private Vector<Integer> selected_vertex_faces = null;
  private boolean RIGHT_FUNCTION_PRIORITIZED = false;

  private int bend_smoothness = 0;
  private boolean BENDING_JOINT = false;
	private Point selected_joint = null;
	private Vector<Vector<Integer>> selected_components = null;
	private Vector<Vector<Integer>> selected_component_faces = null;
	private int selected_component = -1;
  
	private JColorChooser color_chooser = null;
	private JFrame color_chooser_frame = null;
	 
  private DrawOption lighting = DrawOption.ENABLED;
  private DrawOption texture = DrawOption.DECAL;
  
  public ModelViewer() {}
  
  /**
   * Class Constroctur specifying the INI file to load.
   *  @param filename INI file name containing initialization values
   *  @param DISABLE_HEAVYWEIGHT disable heavyweight GLCanvas (sacrificing performance for functionality)
   */
  public ModelViewer(String filename, boolean DISABLE_HEAVYWEIGHT)
  {
    this(filename, 0, 0, DISABLE_HEAVYWEIGHT, true);
  }
  
  /**
   * Class Constructor specifying INI file, initial dimensions and whether or not
   * to load the default model from the INI file or not.  The construct also builds the pop
   * up menu and starts a thread used to refresh the scene.
   *  @param filename INI file name containing initialization values
   *  @param w width of viewer
   *  @param h height of viewer
   *  @param DISABLE_HEAVYWEIGHT disable heavyweight GLCanvas (sacrificing performance for functionality)
   *  @param LOAD_DEFAULT if false the viewer will not load the default model from the INI file
   */
  public ModelViewer(String filename, int w, int h, boolean DISABLE_HEAVYWEIGHT, boolean LOAD_DEFAULT)
  {
    if(w > 0 && h > 0){
      width = w;
      height = h;
      halfwidth = width / 2;
      halfheight = height / 2;
      vleft = -width / 2.0;
      vright = width / 2.0;
      vbottom = height / 2.0;
      vtop = -height / 2.0;
    }
    
    this.LOAD_DEFAULT = LOAD_DEFAULT;
    
    if(filename != null){
      try{
        loadINI(new FileInputStream(filename));
      }catch(Exception e){}
    }
    
    //Setup panel and canvas
    setBackground(java.awt.Color.white);
    super.setSize(width, height);
    setLayout(null);
        	
    GLCapabilities capabilities = new GLCapabilities();
  	capabilities.setDoubleBuffered(true);
    capabilities.setStencilBits(1);
    capabilities.setSampleBuffers(true);
    capabilities.setNumSamples(4);
    
    if(!DISABLE_HEAVYWEIGHT){
    	canvas = new GLCanvas(capabilities);
    }else{
    	canvas = new GLJPanel(capabilities);
    }
    
    if(canvas instanceof GLCanvas){
    	((GLCanvas)canvas).addGLEventListener(this);
    }else if(canvas instanceof GLJPanel){
    	((GLJPanel)canvas).addGLEventListener(this);
    	((GLJPanel)canvas).setLayout(null);
    }
    
    canvas.setLocation(0, 0);
    canvas.setSize(width, height);
    canvas.setFocusable(true);
    super.add(canvas);
    
    setPopupMenu();
    
    Thread t1 = new Thread(this);
    t1.start();
  }
  
  /**
   * Load initialization file containing inital values for the viewer.
   *  @param fis the file input stream (note this is friendly to applets!)
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
            if(key.equals("Adjust")){
              ADJUST = Boolean.valueOf(value);
            }else if(key.equals("Signature")){
              Signature = Class.forName(value);
            }else if(key.equals("RebuildSignatures")){
              REBUILD_SIGNATURES = Boolean.valueOf(value);
            }else if(key.equals("DefaultModel")){
              if(LOAD_DEFAULT){
              	load(value);
              }
            }else if(key.equals("LoadPath")){
              load_path = value + "/";
            }else if(key.equals("ExportPath")){
              export_path = value + "/";
            }else if(key.equals("MetaDataPath")){
              metadata_path = value + "/";
            }else if(key.equals("Ortho")){
              ORTHO = Boolean.valueOf(value);
            }else if(key.equals("Axis")){
              AXIS = Boolean.valueOf(value);
            }else if(key.equals("Points")){
              POINTS = Boolean.valueOf(value);
            }else if(key.equals("Wire")){
              WIRE = Boolean.valueOf(value);
            }else if(key.equals("Outline")){
              OUTLINE = Boolean.valueOf(value);
            }else if(key.equals("Transparent")){
              TRANSPARENT = Boolean.valueOf(value);
            }else if(key.equals("Solid")){
              SOLID = Boolean.valueOf(value);
            }else if(key.equals("Shaded")){
              SHADED = Boolean.valueOf(value);
            }else if(key.equals("Smooth")){
              SHADED_SMOOTH = Boolean.valueOf(value);
            }else if(key.equals("Highlights")){
              HIGHLIGHTS = Boolean.valueOf(value);
            }else if(key.equals("Illustration")){
              ILLUSTRATION = Boolean.valueOf(value);
            }else if(key.equals("Metallic")){
              METAL = Boolean.valueOf(value);
            }else if(key.equals("AntiAliasing")){
            	ANTI_ALIASING = Boolean.valueOf(value);
            }else if(key.equals("AutoRefresh")){
              AUTO_REFRESH = Boolean.valueOf(value);
            }else if(key.equals("SaveAxis")){
              SAVE_AXIS = Boolean.valueOf(value);
            }
          }
        }
      }
      
      ins.close();
    }catch(Exception e) {e.printStackTrace();}
  }

	/**
   * Hack! Add this panel to a frame and make it temporarily visible for the sole purpose of getting
   * the display function to run.
   */
  public void activate()
  {
    JFrame frame = new JFrame();
    frame.add(this);
    frame.setVisible(true);
    frame.setVisible(false);
  }

	/**
   * Add components to the canvas component rather than this panel.
   *  @param c the component to add
   *  @return null
   */
  public Component add(Component c)
  {
  	if(canvas instanceof GLJPanel){
  		((GLJPanel)canvas).add(c);
  		//canvas_panel.add(c);
  	}
  	
  	return null;
  }

	/**
   * Remove components from the canvas component rather than this panel.
   *  @param c the component to remove
   */
  public void remove(Component c)
  {
  	if(canvas instanceof GLJPanel){
  		((GLJPanel)canvas).add(c);
  		//canvas_panel.remove(c);
  	}
  }

	/**
   * Sets the size of the viewer, the canvas and the mesh to best fit the new viewers size.
   *  @param w the new viewer width
   *  @param h the new viewer height
   */
  public void setSize(int w, int h)
  {
    if(width != w || height != h){
      width = w;
      height = h;
      halfwidth = width / 2;
      halfheight = height / 2;
      vleft = -width / 2.0;
      vright = width / 2.0;
      vbottom = height / 2.0;
      vtop = -height / 2.0;
      
      canvas.setSize(width, height);    
      super.setSize(width, height);
      
      if(ADJUST){
        mesh.center(0.8f*((width<height)?width:height)/2.0f);
        refreshList();
      }
      
      UPDATE_CAMERA = true;
      refresh(true);
    }
  }

	/**
   * Set the model adjustments.
   *  @param tx the x-offset
   *  @param ty the y-offset
   *  @param scl the scale change
   */
  public void setAdjustments(float tx, float ty, float scl)
  {
  	adj_tx = tx;
  	adj_ty = ty;
  	adj_scl = scl;
  }

	/**
   * Set the popup menu.
   */
  private void setPopupMenu()
  {
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    JMenu submenu1, submenu2, submenu3;
    ButtonGroup group1, group2;
    JLabel label;
    
    popup_menu = new JPopupMenu(); 
    menuitem_OPEN = new JMenuItem("Open"); menuitem_OPEN.addActionListener(this); popup_menu.add(menuitem_OPEN);
    menuitem_ADD = new JMenuItem("Add"); menuitem_ADD.addActionListener(this); popup_menu.add(menuitem_ADD);
  
    submenu1 = new JMenu("Export");
    menuitem_EXPORT_JPG = new JMenuItem("JPEG (*.jpg)"); menuitem_EXPORT_JPG.addActionListener(this); submenu1.add(menuitem_EXPORT_JPG);
    menuitem_EXPORT_OBJ = new JMenuItem("Wavefront (*.obj)"); menuitem_EXPORT_OBJ.addActionListener(this); submenu1.add(menuitem_EXPORT_OBJ);
    menuitem_EXPORT_OBJ_BIN1 = new JMenuItem("Wavefront (*.obj_bin1)"); menuitem_EXPORT_OBJ_BIN1.addActionListener(this); submenu1.add(menuitem_EXPORT_OBJ_BIN1);
    menuitem_EXPORT_OBJ_BIN2 = new JMenuItem("Wavefront (*.obj_bin2)"); menuitem_EXPORT_OBJ_BIN2.addActionListener(this); submenu1.add(menuitem_EXPORT_OBJ_BIN2);
    menuitem_EXPORT_PLY = new JMenuItem("Stanford (*.ply)"); menuitem_EXPORT_PLY.addActionListener(this); submenu1.add(menuitem_EXPORT_PLY);
    menuitem_EXPORT_VH = new JMenuItem("Visual Hull (*.vh)"); menuitem_EXPORT_VH.addActionListener(this); submenu1.add(menuitem_EXPORT_VH);
    menuitem_EXPORT_WRL = new JMenuItem("VRML 1.0 (*.wrl)"); menuitem_EXPORT_WRL.addActionListener(this); submenu1.add(menuitem_EXPORT_WRL);
    submenu1.addSeparator();
    menuitem_EXPORT_DEPTH = new JMenuItem("Depth Map"); menuitem_EXPORT_DEPTH.addActionListener(this); submenu1.add(menuitem_EXPORT_DEPTH);
    menuitem_EXPORT_POINTS_CAMERAS = new JMenuItem("Points/Cameras"); menuitem_EXPORT_POINTS_CAMERAS.addActionListener(this); submenu1.add(menuitem_EXPORT_POINTS_CAMERAS);
  
    popup_menu.add(submenu1);
    popup_menu.addSeparator();
    
    //View menu
    submenu1 = new JMenu("View");
    
    //View group menu
    if(!mesh.getGroups().isEmpty()){
    	Iterator<String> itr;
    	JCheckBoxMenuItem item;
    	int m = 0;
    	int n = 0;
    	int total_menuitems = mesh.getGroups().keySet().size();
    	int max_menuitems = 40;
    	int digits = (total_menuitems < 100) ? 2 : 3;
    	int tmpi;
    	
    	submenu2 = new JMenu("Groups");
  		menuitem_VIEW_GROUPS_ALL = new JMenuItem("All"); menuitem_VIEW_GROUPS_ALL.addActionListener(this); submenu2.add(menuitem_VIEW_GROUPS_ALL);
  		submenu2.addSeparator();
    	submenu3 = null;
    	
    	if(total_menuitems > max_menuitems){
    		submenu3 = new JMenu("Groups " + Utility.toString(1, digits) + "-" + Utility.toString(max_menuitems, digits));
    		m = 1;
    	}
    	
    	itr = mesh.getGroups().keySet().iterator();
    	menuitem_VIEW_GROUPS.clear();
    	
    	while(itr.hasNext()){
    		if(submenu3 != null && n >= max_menuitems){
    			tmpi = (m+1) * max_menuitems - 1;
    			if(tmpi > total_menuitems) tmpi = total_menuitems;
    			
    			submenu2.add(submenu3);
    			submenu3 = new JMenu("Groups " + Utility.toString(m*max_menuitems+1, digits) + "-" + Utility.toString(tmpi, digits));
    			m++;
    			n = 0;
    		}
    		
    		item = new JCheckBoxMenuItem(itr.next()); item.addActionListener(this);	item.setState(true); menuitem_VIEW_GROUPS.add(item);
    		
    		if(submenu3 == null){
    			submenu2.add(item);
    		}else{
    			submenu3.add(item);
    		}
    		
    		n++;
    	}
      
    	if(submenu3 != null && n > 0) submenu2.add(submenu3);
      submenu1.add(submenu2);
      submenu1.addSeparator();
    }
    
    menuitem_ORTHO = new JCheckBoxMenuItem("Ortho"); menuitem_ORTHO.addActionListener(this); submenu1.add(menuitem_ORTHO); menuitem_ORTHO.setState(ORTHO);
    menuitem_AXIS = new JCheckBoxMenuItem("Axis"); menuitem_AXIS.addActionListener(this); submenu1.add(menuitem_AXIS); menuitem_AXIS.setState(AXIS);
    menuitem_PC_AXIS = new JCheckBoxMenuItem("PC Axis"); menuitem_PC_AXIS.addActionListener(this); submenu1.add(menuitem_PC_AXIS); menuitem_PC_AXIS.setState(PC_AXIS);
    submenu1.addSeparator();
    menuitem_POINTS = new JCheckBoxMenuItem("Points"); menuitem_POINTS.addActionListener(this); submenu1.add(menuitem_POINTS); menuitem_POINTS.setState(POINTS);
    menuitem_WIRE = new JCheckBoxMenuItem("Wire Frame"); menuitem_WIRE.addActionListener(this); submenu1.add(menuitem_WIRE); menuitem_WIRE.setState(WIRE);
    menuitem_OUTLINE = new JCheckBoxMenuItem("Outline"); menuitem_OUTLINE.addActionListener(this); submenu1.add(menuitem_OUTLINE); menuitem_OUTLINE.setState(OUTLINE);
    submenu1.addSeparator();
    group1 = new ButtonGroup();
    menuitem_TRANSPARENT = new JRadioButtonMenuItem("Transparent"); menuitem_TRANSPARENT.addActionListener(this); submenu1.add(menuitem_TRANSPARENT); group1.add(menuitem_TRANSPARENT); menuitem_TRANSPARENT.setSelected(TRANSPARENT);
    menuitem_SOLID = new JRadioButtonMenuItem("Solid"); menuitem_SOLID.addActionListener(this); submenu1.add(menuitem_SOLID); group1.add(menuitem_SOLID); menuitem_SOLID.setSelected(SOLID);
    submenu2 = new JMenu("Shaded");
    menuitem_SHADED = new JRadioButtonMenuItem("Shaded"); menuitem_SHADED.addActionListener(this); submenu2.add(menuitem_SHADED); group1.add(menuitem_SHADED); menuitem_SHADED.setSelected(SHADED);
    submenu2.addSeparator();
    menuitem_SHADED_SMOOTH = new JCheckBoxMenuItem("Smooth"); menuitem_SHADED_SMOOTH.addActionListener(this); submenu2.add(menuitem_SHADED_SMOOTH); menuitem_SHADED_SMOOTH.setState(SHADED_SMOOTH);
    submenu3 = new JMenu("Lighting");
    group2 = new ButtonGroup();
    menuitem_SHADED_LIGHTING_DISABLED = new JRadioButtonMenuItem("Disabled"); menuitem_SHADED_LIGHTING_DISABLED.addActionListener(this); submenu3.add(menuitem_SHADED_LIGHTING_DISABLED); group2.add(menuitem_SHADED_LIGHTING_DISABLED); menuitem_SHADED_LIGHTING_DISABLED.setSelected(SHADED_LIGHTING_DISABLED);
    menuitem_SHADED_LIGHTING_ENABLED = new JRadioButtonMenuItem("Enabled"); menuitem_SHADED_LIGHTING_ENABLED.addActionListener(this); submenu3.add(menuitem_SHADED_LIGHTING_ENABLED); group2.add(menuitem_SHADED_LIGHTING_ENABLED); menuitem_SHADED_LIGHTING_ENABLED.setSelected(SHADED_LIGHTING_ENABLED);
    menuitem_SHADED_LIGHTING_MATERIAL = new JRadioButtonMenuItem("Material"); menuitem_SHADED_LIGHTING_MATERIAL.addActionListener(this); submenu3.add(menuitem_SHADED_LIGHTING_MATERIAL); group2.add(menuitem_SHADED_LIGHTING_MATERIAL); menuitem_SHADED_LIGHTING_MATERIAL.setSelected(SHADED_LIGHTING_MATERIAL);
    submenu2.add(submenu3);
    submenu3 = new JMenu("Texture");
    group2 = new ButtonGroup();
    menuitem_SHADED_TEXTURE_DISABLED = new JRadioButtonMenuItem("Disabled"); menuitem_SHADED_TEXTURE_DISABLED.addActionListener(this); submenu3.add(menuitem_SHADED_TEXTURE_DISABLED); group2.add(menuitem_SHADED_TEXTURE_DISABLED); menuitem_SHADED_TEXTURE_DISABLED.setSelected(SHADED_TEXTURE_DISABLED);
    menuitem_SHADED_TEXTURE_DECAL = new JRadioButtonMenuItem("Decal"); menuitem_SHADED_TEXTURE_DECAL.addActionListener(this); submenu3.add(menuitem_SHADED_TEXTURE_DECAL); group2.add(menuitem_SHADED_TEXTURE_DECAL); menuitem_SHADED_TEXTURE_DECAL.setSelected(SHADED_TEXTURE_DECAL);
    menuitem_SHADED_TEXTURE_MODULATE = new JRadioButtonMenuItem("Modulate"); menuitem_SHADED_TEXTURE_MODULATE.addActionListener(this); submenu3.add(menuitem_SHADED_TEXTURE_MODULATE); group2.add(menuitem_SHADED_TEXTURE_MODULATE); menuitem_SHADED_TEXTURE_MODULATE.setSelected(SHADED_TEXTURE_MODULATE);
    submenu2.add(submenu3);
    submenu1.add(submenu2);
    menuitem_HIGHLIGHTS = new JRadioButtonMenuItem("Highlights"); menuitem_HIGHLIGHTS.addActionListener(this); submenu1.add(menuitem_HIGHLIGHTS); group1.add(menuitem_HIGHLIGHTS); menuitem_HIGHLIGHTS.setSelected(HIGHLIGHTS);
    menuitem_ILLUSTRATION = new JRadioButtonMenuItem("Illustration"); menuitem_ILLUSTRATION.addActionListener(this); submenu1.add(menuitem_ILLUSTRATION); group1.add(menuitem_ILLUSTRATION); menuitem_ILLUSTRATION.setSelected(ILLUSTRATION);
    menuitem_METAL = new JRadioButtonMenuItem("Metallic"); menuitem_METAL.addActionListener(this); submenu1.add(menuitem_METAL); group1.add(menuitem_METAL); menuitem_METAL.setSelected(METAL);
    submenu1.addSeparator();
    menuitem_RAYTRACE = new JMenuItem("Ray Trace"); menuitem_RAYTRACE.addActionListener(this); submenu1.add(menuitem_RAYTRACE);
    popup_menu.add(submenu1);
    
    //Active mesh selection menu
    if(!added_meshes.isEmpty()){
    	JRadioButtonMenuItem item;
    	menuitem_MESHES.clear();
  
    	submenu1 = new JMenu("Meshes");
  		item = new JRadioButtonMenuItem("Mesh-0"); item.addActionListener(this);item.setSelected(true); menuitem_MESHES.add(item); submenu1.add(item);
  
    	for(int i=0; i<added_meshes.size(); i++){
    		item = new JRadioButtonMenuItem("Mesh-" + (i+1)); item.addActionListener(this);item.setSelected(false); menuitem_MESHES.add(item); submenu1.add(item);
    	}
    	
    	popup_menu.add(submenu1);
    }
    
    //Select menu
    submenu1 = new JMenu("Select");
    menuitem_SELECT_BOUNDING_BOX = new JMenuItem("Bounding Box"); menuitem_SELECT_BOUNDING_BOX.addActionListener(this); submenu1.add(menuitem_SELECT_BOUNDING_BOX);
    menuitem_SELECT_HIGHLIGHT = new JMenuItem("Highlight"); menuitem_SELECT_HIGHLIGHT.addActionListener(this); submenu1.add(menuitem_SELECT_HIGHLIGHT);
    menuitem_SELECT_COMPONENT = new JMenuItem("Component"); menuitem_SELECT_COMPONENT.addActionListener(this); submenu1.add(menuitem_SELECT_COMPONENT);

    //Select group menu
    if(!mesh.getGroups().isEmpty()){
    	Iterator<String> itr;
    	JMenuItem item;
    	int m = 0;
    	int n = 0;
    	int total_menuitems = mesh.getGroups().keySet().size();
    	int max_menuitems = 40;
    	int digits = (total_menuitems < 100) ? 2 : 3;
    	int tmpi;
    	
    	submenu2 = new JMenu("Group");
    	submenu3 = null;
    	
    	if(total_menuitems > max_menuitems){
    		submenu3 = new JMenu("Groups " + Utility.toString(1, digits) + "-" + Utility.toString(max_menuitems, digits));
    		m = 1;
    	}
    	
    	itr = mesh.getGroups().keySet().iterator();
    	menuitem_SELECT_GROUPS.clear();
    	
    	while(itr.hasNext()){
    		if(submenu3 != null && n >= max_menuitems){
    			tmpi = (m+1) * max_menuitems - 1;
    			if(tmpi > total_menuitems) tmpi = total_menuitems;
    			
    			submenu2.add(submenu3);
    			submenu3 = new JMenu("Groups " + Utility.toString(m*max_menuitems+1, digits) + "-" + Utility.toString(tmpi, digits));
    			m++;
    			n = 0;
    		}
    		
    		item = new JMenuItem(itr.next()); item.addActionListener(this); menuitem_SELECT_GROUPS.add(item);
    		
    		if(submenu3 == null){
    			submenu2.add(item);
    		}else{
    			submenu3.add(item);
    		}
    		
    		n++;
    	}
      
    	if(submenu3 != null && n > 0) submenu2.add(submenu3);
      submenu1.add(submenu2);
    }
    
    label = new JLabel(" For"); label.setFont(new Font("Default", Font.BOLD|Font.ITALIC, 8)); submenu1.add(label);
    submenu1.addSeparator();
    group1 = new ButtonGroup();
    menuitem_SELECT_FOR_NOTHING = new JRadioButtonMenuItem("Nothing"); menuitem_SELECT_FOR_NOTHING.addActionListener(this); submenu1.add(menuitem_SELECT_FOR_NOTHING); group1.add(menuitem_SELECT_FOR_NOTHING); menuitem_SELECT_FOR_NOTHING.setSelected(SELECT_FOR_NOTHING);
    menuitem_SELECT_FOR_JOINT = new JRadioButtonMenuItem("Joint"); menuitem_SELECT_FOR_JOINT.addActionListener(this); submenu1.add(menuitem_SELECT_FOR_JOINT); group1.add(menuitem_SELECT_FOR_JOINT); menuitem_SELECT_FOR_JOINT.setSelected(SELECT_FOR_JOINT);
    menuitem_SELECT_FOR_COLOR = new JRadioButtonMenuItem("Color"); menuitem_SELECT_FOR_COLOR.addActionListener(this); submenu1.add(menuitem_SELECT_FOR_COLOR); group1.add(menuitem_SELECT_FOR_COLOR); menuitem_SELECT_FOR_COLOR.setSelected(SELECT_FOR_COLOR);
    menuitem_SELECT_FOR_SMOOTH = new JRadioButtonMenuItem("Smooth"); menuitem_SELECT_FOR_SMOOTH.addActionListener(this); submenu1.add(menuitem_SELECT_FOR_SMOOTH); group1.add(menuitem_SELECT_FOR_SMOOTH); menuitem_SELECT_FOR_SMOOTH.setSelected(SELECT_FOR_SMOOTH);
    label = new JLabel(" Using"); label.setFont(new Font("Default", Font.BOLD|Font.ITALIC, 8)); submenu1.add(label);
    submenu1.addSeparator();
    menuitem_SELECT_USING_DEPTH = new JCheckBoxMenuItem("Depth"); menuitem_SELECT_USING_DEPTH.addActionListener(this); submenu1.add(menuitem_SELECT_USING_DEPTH); menuitem_SELECT_USING_DEPTH.setState(SELECT_USING_DEPTH);
    popup_menu.add(submenu1);

    submenu1 = new JMenu("Apply");
    menuitem_SIMPLIFY = new JMenuItem("Simplification"); menuitem_SIMPLIFY.addActionListener(this); submenu1.add(menuitem_SIMPLIFY); menuitem_SIMPLIFY.setEnabled(false);
    menuitem_SUBDIVIDE = new JMenuItem("Subdivision"); menuitem_SUBDIVIDE.addActionListener(this); submenu1.add(menuitem_SUBDIVIDE);
    popup_menu.add(submenu1);
    
    popup_menu.addSeparator();
    menuitem_QUIT = new JMenuItem("Quit"); menuitem_QUIT.addActionListener(this); popup_menu.add(menuitem_QUIT);
  }

	/**
   * Set the mesh structure.
   *  @param mesh the mesh to display
   */
  public synchronized void setMesh(Mesh mesh)
  {
    boolean FOUND_COLORS = false;
    
  	added_meshes.clear();
  	added_transformations.clear();
		selected_rotation_last = rotation_last;
  	selected_transformation = transformation;
  	selected_vertices = null;
  	selected_vertex_faces = null;
  	selected_components = null;
  	selected_component_faces = null;
  
  	this.mesh = mesh;
    
    if(Signature != null) mesh.setSignature(Signature, metadata_path + mesh.getMetaData("Name") + ".signature", REBUILD_SIGNATURES); 
    if(ADJUST) mesh.center(0.8f*((width<height)?width:height)/2.0f);
    
    //If no faces then display points
    POINTS = POINTS || mesh.faces.isEmpty();
    if(menuitem_POINTS != null) menuitem_POINTS.setSelected(POINTS); 
    
    //If colors then enable materials
    for(int i=0; i<mesh.getFaces().size(); i++){
    	if(mesh.getFaces().get(i).material != null && mesh.getFaces().get(i).material.diffuse != null){
    		FOUND_COLORS = true;
    		break;
    	}
    }
    
    if(mesh.getVertices().size() == mesh.getVertexColors().size()) FOUND_COLORS = true;
    
  	SHADED_LIGHTING_DISABLED = false;
  	SHADED_LIGHTING_ENABLED = !FOUND_COLORS;
  	SHADED_LIGHTING_MATERIAL = FOUND_COLORS;
  	if(menuitem_SHADED_LIGHTING_DISABLED != null) menuitem_SHADED_LIGHTING_DISABLED.setSelected(SHADED_LIGHTING_DISABLED);
  	if(menuitem_SHADED_LIGHTING_ENABLED != null) menuitem_SHADED_LIGHTING_ENABLED.setSelected(SHADED_LIGHTING_ENABLED);
  	if(menuitem_SHADED_LIGHTING_MATERIAL != null) menuitem_SHADED_LIGHTING_MATERIAL.setSelected(SHADED_LIGHTING_MATERIAL);
    	
  	if(!FOUND_COLORS){
  		lighting = DrawOption.ENABLED;
  	}else{
  		lighting = DrawOption.MATERIAL;
  	}
  	
  	if(SHADED_TEXTURE_DECAL){
  		texture = DrawOption.DECAL;
  	}else if(SHADED_TEXTURE_MODULATE){
  		texture = DrawOption.MODULATE;
  	}else{
  		texture = DrawOption.DISABLED;
  	}
    
    setPopupMenu();
    refreshList();
    UPDATE_CAMERA = true;		//Why do I need to update the camera (needed to display multiple modelviewers in modelbroswer)?
    REFRESH = true;
  }

	/**
   * Set points to visible/invisible
   * @param POINTS true if points are to be visible
   */
  public void enablePoints(boolean POINTS)
  {
  	this.POINTS = POINTS;
    if(menuitem_POINTS != null) menuitem_POINTS.setSelected(POINTS); 	
  }
  
  /**
   * Load model into our mesh structure.
   *  @param filename the absolute name of the file
   *  @param progressCallBack the callback handling progress updates
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
   *  @param filename the absolute name of the file
   */
  public void load(String filename)
  {
  	load(filename, null);
  }

	/**
   * Add a secondary model to the viewer.
   *  @param filename the absolute name of the file
   */
  public void add(String filename)
  {
    long t0 = 0, t1 = 0;
    double dt;
    
    if(filename.length() > 0){
    	added_meshes.add(new Mesh());
    	added_rotation_last.add(MatrixUtility.eye(4));
      added_transformations.add(new RigidTransformation());
      selected_rotation_last = rotation_last;
      selected_transformation = transformation;
      
      System.out.print("Adding: " + Utility.getFilename(filename) + "... ");
      t0 = System.currentTimeMillis();
      
      added_meshes.lastElement().load(filename);
      t1 = System.currentTimeMillis();
      dt = t1 - t0;	
      
      if(ADJUST) added_meshes.lastElement().center(0.8f*((width<height)?width:height)/2.0f);
      
      setPopupMenu();
      refresh(true);
      System.out.println("\t[Loaded in " + dt/1000.0 + "s]");
    }
  }

	/**
   * Save contents of our mesh structure.
   *  @param pathname path to the new model
   *  @param filename file name of the new model
   */
  public void save(String pathname, String filename)
  {
    long t0 = 0, t1 = 0;
    
    System.out.print("Saving: " + filename + "... ");
    
    if(filename.contains(".jpg")){			//Special case since we have to render stuff!
      t0 = System.currentTimeMillis();
      output_name = pathname + filename;
      SAVE_IMAGE = true;
      refresh(true);
      t1 = System.currentTimeMillis();
    }else if(filename.contains(".vh")){		
      t0 = System.currentTimeMillis();
      output_name = pathname + filename;
      AXIS = false; menuitem_AXIS.setState(AXIS);      
      ORTHO = false; menuitem_ORTHO.setState(ORTHO);
      UPDATE_CAMERA = true;
      SAVE_VISUAL_HULL_VIEW = true;
      refresh(true);
      t1 = System.currentTimeMillis();
    }else if(filename.contains(".pc")){
      t0 = System.currentTimeMillis();
      output_name = pathname + filename;
      ORTHO = false; menuitem_ORTHO.setState(ORTHO);
      UPDATE_CAMERA = true;
      SAVE_POINTS_CAMERA = true;
      refresh(true);
      t1 = System.currentTimeMillis();
    }else if(filename.contains(".pgm")){
      t0 = System.currentTimeMillis();
      output_name = pathname + filename;
      SAVE_DEPTH = true;
      refresh(true);
      t1 = System.currentTimeMillis();
    }else{
    	t0 = System.currentTimeMillis();
    	
    	//Merge meshes if others exists
    	for(int i=0; i<added_meshes.size(); i++){
    		mesh.addData(added_meshes.get(i), added_rotation_last.get(i), added_transformations.get(i).tx, added_transformations.get(i).ty, added_transformations.get(i).tz, added_transformations.get(i).scl);
    	}
    	
    	mesh.initialize();
    	
    	added_meshes.clear();
    	added_transformations.clear();
  		selected_rotation_last = rotation_last;
    	selected_transformation = transformation;
    	
    	//Save the mesh
    	mesh.save(pathname + filename);
    	
    	//Update the viewer
      setPopupMenu();
      refreshList();
      refresh(true);
      
      t1 = System.currentTimeMillis();
    }
  
    System.out.println("\t[Completed in " + (float)(t1-t0)/1000f + "s]");
  }

	/**
   * Save a screen shot to an image.
   * @param gl the OpenGL context
   * @param filename the name of the file to save to
   */
  public void saveImage(GL gl, String filename)
  {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    int[] rgb = grabImage(gl, GL.GL_BACK); 
    
    try{
    	if(filename.endsWith(".jpg")){
        image.setRGB(0, 0, width, height, rgb, 0, width);
        ImageIO.write(image, "jpeg", new File(filename));
    	}else{
    		System.out.println("Error: unsupported image type!");
    	}
    }catch(Exception e) {e.printStackTrace();}
  }

	/**
   * Save a view (image + camera paramters).
   *  @param gl the OpenGL context
   *  @param path the folder to save to
   */
  private void saveVisualHullView(GL gl, String path)
  {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    int[] pixels_rgb = null;
    int[] pixels_axis = null;
    double[][] pixels_mask = null;
    double[][] K = Camera.applyViewport(projection, halfwidth, halfheight);
    double[][] RT = modelview;
    String filename;
    int index = 1;
    
    //Create a directory for the visual hull data
    new File(path).mkdir();
    
    //Search the directory for the next availabe view index
    while(true){
      filename = path + "/" + Utility.toString(index, 2);
      
      if(!Utility.exists(filename + ".jpg")){
        break;
      }else{
        index++;
      }
    }
    
    pixels_rgb = grabImage(gl, GL.GL_BACK);    
    
    //Create mask
    pixels_mask = new double[height][width];
    
    for(int x=0; x<width; x++){
      for(int y=0; y<height; y++){
        if(pixels_rgb[y*width+x] != 0xffffffff) pixels_mask[y][x] = 1;
      }
    }
    
    if(SAVE_AXIS){   //Draw reference axis and capture image
    	//gl.glDrawBuffer(GL.GL_AUX0);
      gl.glClearColor(0, 0, 0, 0);
      gl.glClear(GL.GL_COLOR_BUFFER_BIT);
      gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
      gl.glClearColor(1, 1, 1, 0);  //Set back to default color!
      
      gl.glPushMatrix();
      if(!ORTHO) gl.glTranslated(0, 0, -cz);
      gl.glTranslated(transformation.tx, transformation.ty, transformation.tz);
      gl.glRotated(transformation.rx, 1, 0, 0);   
      gl.glRotated(transformation.ry, 0, 1, 0);
      gl.glRotated(transformation.rz, 0, 0, 1);
      gl.glMultMatrixd(MatrixUtility.to1D(MatrixUtility.transpose(rotation_last)), 0); 	//Must transpose since OpenGL uses column major order!
      gl.glScaled(transformation.scl, transformation.scl, transformation.scl);
      drawCalibrationAxis(gl, 150, 5);
      gl.glPopMatrix();
      gl.glFlush();
      
      pixels_axis = grabImage(gl, GL.GL_BACK);
      //gl.glDrawBuffer(GL.GL_BACK);
    }
  
    //View the captured images
    if(false){
      ImageViewer.show(pixels_rgb, width, height, "Texture");
      if(pixels_axis != null) ImageViewer.show(pixels_axis, width, height, "Axis");
      ImageViewer.show(MatrixUtility.to1D(pixels_mask), width, height, "Mask");
    }
    
    //Save view data
    //System.out.println(MatrixUtils.toString(K));
    //System.out.println(MatrixUtils.toString(RT)); 
    
    try{
      //Save the images
      image.setRGB(0, 0, width, height, pixels_rgb, 0, width);
      ImageIO.write(image, "jpeg", new File(filename + ".jpg"));
      
      if(pixels_axis != null){
        image.setRGB(0, 0, width, height, pixels_axis, 0, width);
        ImageIO.write(image, "jpeg", new File(filename + "_axis.jpg"));
      }
      
      ImageUtility.save_PBM(filename + ".pbm", pixels_mask);
      
      //Save the cameras
      BufferedWriter outs = new BufferedWriter(new FileWriter(path + "/cameras.txt", true));
      
      if(true){  //Save K as a matrix
        outs.write(Utility.toString(index, 2) + " i m ");
        
        for(int j=0; j<4; j++){
          for(int i=0; i<4; i++){
            outs.write(Double.toString(K[j][i]) + " ");
          }
        }
      }else{			//Save K as parameters
        outs.write(Utility.toString(index, 2) + " i p ");
        outs.write(Double.toString(K[0][0]*transformation.scl) + " ");
        outs.write(Double.toString(K[1][1]*transformation.scl) + " ");
        outs.write(Double.toString(K[0][2]) + " ");
        outs.write(Double.toString(K[1][2]) + " ");      	
      }
        
      outs.newLine();
      
      if(true){		//Save RT as a matrix
        outs.write(Utility.toString(index, 2) + " e m ");
        
        for(int j=0; j<4; j++){
          for(int i=0; i<4; i++){
            outs.write(Double.toString(RT[j][i]) + " ");
          }
        }
      }else{			//Save RT as parameters
        outs.write(Utility.toString(index, 2) + " e p ");
        outs.write(Double.toString(transformation.rx) + " ");
        outs.write(Double.toString(transformation.ry) + " ");
        outs.write(Double.toString(transformation.rz) + " ");
        outs.write(Double.toString(transformation.tx) + " ");
        outs.write(Double.toString(transformation.ty) + " ");
        outs.write(Double.toString(transformation.tz-cz) + " ");
      }
        
      outs.newLine();
      outs.close();
    }catch(Exception e) {}
  }

	/**
   * Save the mesh points and current camera location.
   */
  private void savePointsCamera(Mesh mesh, String path)
  {    
  	Vector<Point> vertices;
  	double[][] K = Camera.applyViewport(projection, halfwidth, halfheight);
    double[][] RT = modelview;
    String filename;
    int index = 1;
    
    //Create a directory for the data
    new File(path).mkdir();
    
    //Search the directory for the next available view index
    while(true){
      filename = path + "/" + Utility.toString(index, 2) + ".txt";
      
      if(!Utility.exists(filename)){
        break;
      }else{
        index++;
      }
    }
    
    //Save view data
    //System.out.println(MatrixUtility.toString(projection));
    System.out.println(MatrixUtility.toString(K));
    System.out.println(MatrixUtility.toString(RT));
    
    try{
    	BufferedWriter outs;
    	
    	//Save the 3D points 
    	vertices = mesh.getVertices();
    	
    	if(!Utility.exists(path + "/points.txt")){
    		outs = new BufferedWriter(new FileWriter(path + "/points.txt"));
    		
    		for(int i=0; i<vertices.size(); i++){
      		outs.write(vertices.get(i).x + " " + vertices.get(i).y + " " + vertices.get(i).z);
      		outs.newLine();
    		}
    		
    		outs.close();
    	}
    	
    	//Save image points
    	vertices = Point.transform(RT, vertices);
    	vertices = Point.transform(K, vertices);
    
    	outs = new BufferedWriter(new FileWriter(filename));
    	
    	for(int i=0; i<vertices.size(); i++){
    		outs.write(vertices.get(i).x + " " + vertices.get(i).y);
    		outs.newLine();
    	}
    	
    	outs.close();
    	
      //Save the camera
      outs = new BufferedWriter(new FileWriter(path + "/cameras.txt", true));
      
      if(true){  //Save K as a matrix
        outs.write(Utility.toString(index, 2) + " i m ");
        
        for(int j=0; j<4; j++){
          for(int i=0; i<4; i++){
            outs.write(Double.toString(K[j][i]) + " ");
          }
        }
      }else{			//Save K as parameters
        outs.write(Utility.toString(index, 2) + " i p ");
        outs.write(Double.toString(K[0][0]*transformation.scl) + " ");
        outs.write(Double.toString(K[1][1]*transformation.scl) + " ");
        outs.write(Double.toString(K[0][2]) + " ");
        outs.write(Double.toString(K[1][2]) + " ");      	
      }
        
      outs.newLine();
      
      if(true){		//Save RT as a matrix
        outs.write(Utility.toString(index, 2) + " e m ");
        
        for(int j=0; j<4; j++){
          for(int i=0; i<4; i++){
            outs.write(Double.toString(RT[j][i]) + " ");
          }
        }
      }else{			//Save RT as parameters
        outs.write(Utility.toString(index, 2) + " e p ");
        outs.write(Double.toString(transformation.rx) + " ");
        outs.write(Double.toString(transformation.ry) + " ");
        outs.write(Double.toString(transformation.rz) + " ");
        outs.write(Double.toString(transformation.tx) + " ");
        outs.write(Double.toString(transformation.ty) + " ");
        outs.write(Double.toString(transformation.tz-cz) + " ");
      }
        
      outs.newLine();
      outs.close();
    }catch(Exception e) {e.printStackTrace();}
  }

	/**
   * Initialize the OpenGL canvas.
   *  @param drawable the OpenGL context to render to
   */
  public void init(GLAutoDrawable drawable)
  {
    GL gl = drawable.getGL();
    gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
    
    gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, light0_position, 0);
    gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, light_diff, 0);
    gl.glEnable(GL.GL_LIGHT0);
    
    gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, light1_position, 0);
    gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, light_diff, 0);
    gl.glEnable(GL.GL_LIGHT1);
    
    gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, model_ambient, 0);
    gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);
    
    gl.glEnable(GL.GL_NORMALIZE);    
    gl.glEnable(GL.GL_DEPTH_TEST);
        	
    if(ANTI_ALIASING){
      gl.glHint(GL.GL_POINT_SMOOTH, GL.GL_NICEST);
      gl.glHint(GL.GL_LINE_SMOOTH, GL.GL_NICEST);
      gl.glHint(GL.GL_POLYGON_SMOOTH_HINT, GL.GL_NICEST);
      gl.glEnable(GL.GL_POINT_SMOOTH);
      gl.glEnable(GL.GL_LINE_SMOOTH);
      gl.glEnable(GL.GL_POLYGON_SMOOTH);
    }
    
    drawable.addKeyListener(this);
    drawable.addMouseListener(this);
    drawable.addMouseMotionListener(this);
    drawable.addMouseWheelListener(this);
    
    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();
    gl.glOrtho(vleft, vright, vbottom, vtop, vnear, vfar);
    gl.glScalef(1, -1, 1);
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glLoadIdentity();
  }

	/**
   * Render the scene.
   *  @param drawable the OpenGL context to render to
   */
  public synchronized void display(GLAutoDrawable drawable)
  {
    GL gl = drawable.getGL();
    gl.glClear(GL.GL_COLOR_BUFFER_BIT);
    gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
    
    if(UPDATE_CAMERA){
      gl.glMatrixMode(GL.GL_PROJECTION);
      gl.glLoadIdentity();
      
      if(ORTHO){
        gl.glOrtho(vleft, vright, vbottom, vtop, vnear, vfar);
        gl.glScalef(1, -1, 1);
      }else{
        GLU glu = new GLU();
        glu.gluPerspective(20.0, (float)width/(float)height, 1.0, 100000.0);  
      }
      
      gl.glMatrixMode(GL.GL_MODELVIEW);
      UPDATE_CAMERA = false;
    }
    
    gl.glPushMatrix();
    
    if(!ORTHO){ //Must place this in Modelview matrix so that Projection matrix is equivelent to K!
      gl.glTranslated(0, 0, -cz);
    }
    
    gl.glTranslatef(adj_tx, adj_ty, 0);
    gl.glTranslated(transformation.tx, transformation.ty, transformation.tz);
    gl.glRotated(transformation.rx, 1, 0, 0);   
    gl.glRotated(transformation.ry, 0, 1, 0);
    gl.glRotated(transformation.rz, 0, 0, 1);
    gl.glMultMatrixd(MatrixUtility.to1D(MatrixUtility.transpose(rotation_last)), 0); 	//Must transpose since OpenGL uses column major order!
    gl.glScaled(adj_scl, adj_scl, adj_scl);
    gl.glScaled(transformation.scl, transformation.scl, transformation.scl);
    
    //Extract camera and transformation if needed
    if(OUTLINE || HIGHLIGHTS || ILLUSTRATION || METAL || RAYTRACE || SELECTING || BENDING_JOINT || SAVE_VISUAL_HULL_VIEW || SAVE_DEPTH || SAVE_POINTS_CAMERA){
      gl.glGetDoublev(GL.GL_TRANSPOSE_PROJECTION_MATRIX, projection_tmp, 0);
      gl.glGetDoublev(GL.GL_TRANSPOSE_MODELVIEW_MATRIX, modelview_tmp, 0);
      MatrixUtility.to2D(projection, projection_tmp);
      MatrixUtility.to2D(modelview, modelview_tmp);
              
      modelview_zeroT = MatrixUtility.copy(modelview);
      modelview_zeroT[0][3] = 0;
      modelview_zeroT[1][3] = 0;
      modelview_zeroT[2][3] = 0;
    }
    
    if(PC_AXIS) drawPCs(gl, mesh, 0.1f*halfwidth);
    
    if(POINTS) drawPoints(gl, mesh);
    if(WIRE) drawEdges(gl, mesh);
    if(OUTLINE) drawOutline(gl, mesh, modelview);
    
    if(SOLID){
      drawSolid(gl, mesh);
    }else if(SHADED){
      if(list_id == 0){   //Rebuild the list
        list_id = gl.glGenLists(1);
        
        gl.glNewList(list_id, GL.GL_COMPILE);
        drawShaded(gl, mesh, SHADED_SMOOTH);
        gl.glEndList();
      }
      
      gl.glCallList(list_id);           
    }else if(HIGHLIGHTS){
      drawHighlights(gl, mesh, modelview_zeroT);
    }else if(ILLUSTRATION){
      drawIllustration(gl, mesh, modelview_zeroT);
    }else if(METAL){
      drawMetal(gl, mesh, modelview_zeroT);
    }
    
    if(RAYTRACE){
    	mesh.setMaterials();
      new RayTracer(mesh, projection, modelview, width/3, height/3, SHADED_SMOOTH);
      RAYTRACE = false;
    }
    
    //Draw added meshes
    for(int i=0; i<added_meshes.size(); i++){
    	gl.glPushMatrix();
    	
      gl.glTranslated(added_transformations.get(i).tx, added_transformations.get(i).ty, added_transformations.get(i).tz);
      gl.glRotated(added_transformations.get(i).rx, rotation_last_inv[0][0], rotation_last_inv[1][0], rotation_last_inv[2][0]);   
      gl.glRotated(added_transformations.get(i).ry, rotation_last_inv[0][1], rotation_last_inv[1][1], rotation_last_inv[2][1]);
      gl.glRotated(added_transformations.get(i).rz, rotation_last_inv[0][2], rotation_last_inv[1][2], rotation_last_inv[2][2]);
      gl.glMultMatrixd(MatrixUtility.to1D(MatrixUtility.transpose(added_rotation_last.get(i))), 0); 	//Must transpose since OpenGL uses column major order!
      gl.glScaled(added_transformations.get(i).scl, added_transformations.get(i).scl, added_transformations.get(i).scl);

      drawShaded(gl, added_meshes.get(i), SHADED_SMOOTH);

    	gl.glPopMatrix();
    }
    
    //Draw selection
    if(selected_vertices != null){
    	if(selected_vertex_faces != null){
      	gl.glColor3f(0, 0, 1);
      	drawShadedFlat(gl, mesh, selected_vertex_faces);
    	}
    	      	
    	if(selected_component_faces!=null){
    		Random random = new Random(100000);
    		double r, g, b;
    		
    		for(int i=0; i<selected_component_faces.size(); i++){
    			r = 0.5 + (random.nextFloat()-0.5)/4;
    			g = 0.5 + (random.nextFloat()-0.5)/4;
    			b = 0.5 + (random.nextFloat()-0.5)/4;

	      	gl.glColor3d(r, g, b);
	      	drawShadedFlat(gl, mesh, selected_component_faces.get(i));
    		}
    	}
    }
    
    gl.glPopMatrix();
    
    if(SELECTING){
    	if(SELECT_BOUNDING_BOX && selection_p0 != null && selection_p1 != null){
      	double z = 0.8 * vfar;

    		//Determine corners
    		selection_minx = (selection_p0.x < selection_p1.x) ? selection_p0.x : selection_p1.x;
    		selection_maxx = (selection_p0.x > selection_p1.x) ? selection_p0.x : selection_p1.x;
    		selection_miny = (selection_p0.y < selection_p1.y) ? selection_p0.y : selection_p1.y;
    		selection_maxy = (selection_p0.y > selection_p1.y) ? selection_p0.y : selection_p1.y;

    		//Correct corners for view
    		selection_minx -= halfwidth;
    		selection_maxx -= halfwidth;
    		selection_miny = halfheight - selection_miny;
    		selection_maxy = halfheight - selection_maxy;
    		
    		//Draw selection area
    		gl.glColor3f(0, 0, 1);
    		gl.glBegin(GL.GL_LINES);
    		
    		//Top line
    		gl.glVertex3d(selection_minx, selection_miny, z);
    		gl.glVertex3d(selection_maxx, selection_miny, z);
    		
    		//Bottom line
    		gl.glVertex3d(selection_minx, selection_maxy, z);
    		gl.glVertex3d(selection_maxx, selection_maxy, z);
    		
    		//Left line
    		gl.glVertex3d(selection_minx, selection_miny, z);
    		gl.glVertex3d(selection_minx, selection_maxy, z);
    		
    		//Top line
    		gl.glVertex3d(selection_maxx, selection_miny, z);
    		gl.glVertex3d(selection_maxx, selection_maxy, z);

    		gl.glEnd();
      }
    }
    
    if(AXIS){		//Only visible under orthographic projection!
      gl.glPushMatrix();
      gl.glTranslatef(0.8f*halfwidth, -0.8f*halfheight, 0.8f*(float)vfar);
      gl.glRotated(transformation.rx, 1, 0, 0);   
      gl.glRotated(transformation.ry, 0, 1, 0);
      gl.glRotated(transformation.rz, 0, 0, 1);
      gl.glMultMatrixd(MatrixUtility.to1D(MatrixUtility.transpose(rotation_last)), 0); 	//Must transpose since OpenGL uses column major order!
      drawAxis(gl, 0.1f, 3);
      gl.glPopMatrix();
    }
    
    gl.glFlush();
    
  	if(AUTO_REFRESH || (mesh instanceof AnimatedMesh && ((AnimatedMesh)mesh).getAnimationLoader()!=null)){
  		REFRESH = true;
  	}else{
  		REFRESH = false;
  	}
    
    if(GRAB_IMAGE){
    	grabbed_image = grabImage(gl, GL.GL_BACK);
      GRAB_IMAGE = false;	
    }
    
    if(SAVE_IMAGE){
    	saveImage(gl, output_name);
    	SAVE_IMAGE = false;
    }
    
    if(SAVE_VISUAL_HULL_VIEW){
      saveVisualHullView(gl, output_name);
      SAVE_VISUAL_HULL_VIEW = false;
      REFRESH = true;
    }
    
    if(SAVE_DEPTH){
    	Pair<int[],double[]> pair = mesh.getDepthMap(modelview, width, height);
    	double[][] img = MatrixUtility.to2D(height, width, pair.second);
    	
    	ImageUtility.save_PGM(output_name, img);
    	MatrixUtility.save(Utility.getFilenamePath(output_name) + Utility.getFilenameName(output_name) + ".txt", img);

    	SAVE_DEPTH = false;
    }
    
    if(SAVE_POINTS_CAMERA){
    	savePointsCamera(mesh, output_name);
    	SAVE_POINTS_CAMERA = false;
    }
  }

  /**
   * Set refresh value.
   * @param value true if the display should be refreshed
   */
  public synchronized void refresh(boolean value)
  {
  	REFRESH = value;
  }
  
	/**
   * Rebuild the display list.
   */
  public void refreshList()
  {
    list_id = 0;
  }

	/**
   * Draw the reference axis to the given OpenGL context.
   *  @param gl the OpenGL context
   *  @param scale the scale of the axis (relative to the window width/height)
   *  @param line_width the width of the lines when rendering the axis
   */
  private void drawAxis(GL gl, float scale, int line_width)
  {
  	float length = scale * 0.8f * ((width<height)?width:height)/2.0f;
  	
    gl.glPushMatrix();
    gl.glScalef(length, length, length);
    gl.glDisable(GL.GL_LIGHTING);
    gl.glLineWidth(line_width);
    gl.glBegin(GL.GL_LINES);
    
    gl.glColor3f(1, 0, 0);
    gl.glVertex3f(0, 0, 0);
    gl.glVertex3f(1, 0, 0);
    
    gl.glColor3f(0, 1, 0);
    gl.glVertex3f(0, 0, 0);
    gl.glVertex3f(0, 1, 0);
    
    gl.glColor3f(0, 0, 1);
    gl.glVertex3f(0, 0, 0);
    gl.glVertex3f(0, 0, 1);
    
    gl.glEnd();
    gl.glPopMatrix();
  }

	/**
   * Draw the reference axis to the given OpenGL context.
   *  @param gl the OpenGL context
   *  @param scale the scale of the axis
   *  @param line_width the width of the lines when rendering the axis
   */
  private void drawCalibrationAxis(GL gl, float scale, int line_width)
  {
    gl.glDisable(GL.GL_LIGHTING);
    gl.glPushMatrix();
    gl.glScalef(scale, scale, scale);
    
    //Axis    
    gl.glLineWidth(line_width);
    gl.glBegin(GL.GL_LINES);
    
    gl.glColor3f(1, 0, 0);
    gl.glVertex3f(0.05f, 0, 0);
    gl.glVertex3f(1, 0, 0);
    
    gl.glColor3f(0, 1, 0);
    gl.glVertex3f(0, 0.05f, 0);
    gl.glVertex3f(0, 1, 0);
    
    gl.glColor3f(0, 0, 1);
    gl.glVertex3f(0, 0, 0.05f);
    gl.glVertex3f(0, 0, 1);
    
    gl.glEnd();
    
     //LED's
    gl.glColor3f(1, 1, 0);
    drawSphere(gl, 0, 0, 0, 0.025f);
    drawSphere(gl, 1, 0, 0, 0.025f);
    drawSphere(gl, 0, 1, 0, 0.025f);
    drawSphere(gl, 0, 0, 1, 0.025f);
    
    //Mid-markers
    if(false){
	    gl.glColor3f(0, 0, 0);
	    drawSphere(gl, 0.5f, 0, 0, 2*0.025f, 0.025f, 0.025f);
	    drawSphere(gl, 0, 0.5f, 0, 0.025f, 2*0.025f, 0.025f);
	    drawSphere(gl, 0, 0, 0.5f, 0.025f, 0.025f, 2*0.025f);
    }
    
    //Third-markers
    if(true){
	    gl.glColor3f(0, 0, 0);
	    drawSphere(gl, 0.33f, 0, 0, 2*0.025f, 0.025f, 0.025f);
	    drawSphere(gl, 0.67f, 0, 0, 2*0.025f, 0.025f, 0.025f);
	    drawSphere(gl, 0, 0.33f, 0, 0.025f, 2*0.025f, 0.025f);
	    drawSphere(gl, 0, 0.67f, 0, 0.025f, 2*0.025f, 0.025f);
	    drawSphere(gl, 0, 0, 0.33f, 0.025f, 0.025f, 2*0.025f);
	    drawSphere(gl, 0, 0, 0.67f, 0.025f, 0.025f, 2*0.025f);
    }
    
    gl.glPopMatrix();
  }
  
  /**
   * Draw a sphere to the given OpenGL context.
   * @param gl the OpenGL context
   * @param x the x-coordinate of the sphere's center
   * @param y the y-coordinate of the sphere's center
   * @param z the z-coordinate of the sphere's center
   * @param radius the radius of the sphere
   */
  public void drawSphere(GL gl, float x, float y, float z, float radius)
  {
    drawSphere(gl, x, y, z, radius, radius, radius);
  }
  
  /**
   * Draw a sphere to the given OpenGL context.
   * @param gl the OpenGL context
   * @param x the x-coordinate of the sphere's center
   * @param y the y-coordinate of the sphere's center
   * @param z the z-coordinate of the sphere's center
   * @param sx the scale in the x-direction
   * @param sy the scale in the y-direction
   * @param sz the scale in the z-direction
   */
  public void drawSphere(GL gl, float x, float y, float z, float sx, float sy, float sz)
  {
    GLU glu = new GLU();
  	GLUquadric quadric = glu.gluNewQuadric();
  	
  	gl.glPushMatrix();
  	gl.glTranslatef(x, y, z);
    gl.glScalef(sx, sy, sz);
  	glu.gluSphere(quadric, 1, 10, 10);
  	gl.glPopMatrix();
  }
  
	/**
   * Grab the currently rendered image from the OpenGL context.
   *  @param gl the OpenGL context
   *  @param gl_color_buffer the OpenGL color buffer to read from
   *  @return the image data in ARGB row major order
   */
  public int[] grabImage(GL gl, int gl_color_buffer)
  {
    int[] pixels = new int[width*height];
    ByteBuffer buffer = ByteBuffer.allocateDirect(width*height*3);
    int r, g, b, at;
    
    gl.glReadBuffer(gl_color_buffer);
    gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
    gl.glReadPixels(0, 0, width, height, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, buffer);
    
    pixels = new int[width * height];
    at = 0;
    
    for(int y=height-1; y>=0; y--){
      for(int x=0; x<width; x++){
        r = buffer.get(at++);
        g = buffer.get(at++);
        b = buffer.get(at++);
        pixels[y*width+x] = 0xff000000 | (r & 0x000000ff) << 16 | (g & 0x000000ff) << 8 | (b & 0x000000ff);
      }
    }
    
    return pixels;
  }

	/**
   * Grab a screen shot of the mesh.
   *  @return the screen shot represented as an ARGB image
   */
  public int[] grabImage()
  {
  	GRAB_IMAGE = true;
  	refresh(true);
    
    while(GRAB_IMAGE){
    	Utility.pause(100);
    }
  	
  	return grabbed_image;
  }

	/**
   * Update the translation of a rigid transformation.  Does nothing if this is the master transformation.
   * However if this is an added mesh then it must first apply the inverse of the masters rotation to the
   * translation.
   *  @param rt the rigid transformation to modify
   *  @param x the translation along the x-axis
   *  @param y the translation along the y-axis
   *  @param z the translation along the z-axis
   */
  public void update_translation(RigidTransformation rt, double x, double y, double z)
  {
  	if(rt == transformation){
    	rt.tx += x;
    	rt.ty += y;
    	rt.tz += z;
  	}else{
  		Point t = Point.transform(rotation_last_inv, new Point(x, y, z));
  		
  		rt.tx += t.x;
  		rt.ty += t.y;
  		rt.tz += t.z;
  	}
  }
  
  /**
   * Bind any newly added textures.
   *  @param FORCE set to true to force rebinding of textures
   */
  public void bindTextures(GL gl, Mesh mesh, boolean FORCE)
  {
  	Vector<Texture> textures;
  	
  	if(mesh.UNBOUND_TEXTURES || FORCE){
  		textures = mesh.getTextures();
  		
  		for(int i=0; i<textures.size(); i++){
  			if(textures.get(i).tid == -1 || FORCE){
  				textures.get(i).tid = i;
		  	  gl.glBindTexture(gl.GL_TEXTURE_2D, textures.get(i).tid);
		  	  gl.glPixelStorei(gl.GL_UNPACK_ALIGNMENT, 1);
		  	  gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_REPEAT);
		  	  gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_REPEAT);
		  	  gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_LINEAR);
		  	  gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_LINEAR);
		  	  
		  	  if(texture == DrawOption.DECAL){
		  	  	gl.glTexEnvi(gl.GL_TEXTURE_ENV, gl.GL_TEXTURE_ENV_MODE, gl.GL_DECAL);
		  	  }else if(texture == DrawOption.MODULATE){
		  	  	gl.glTexEnvi(gl.GL_TEXTURE_ENV, gl.GL_TEXTURE_ENV_MODE, gl.GL_MODULATE);
		  	  }
		  	  
		  	  gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, gl.GL_RGB, textures.get(i).w, textures.get(i).h, 0, gl.GL_RGB, gl.GL_UNSIGNED_BYTE, textures.get(i).buffer);
  			}
  		}
  		
  		mesh.UNBOUND_TEXTURES = false;
  	}
  }
  
  /**
   * Draw the principal components of the currently loaded model.
   *  @param gl the OpenGL context to render to
   *  @param scl the scale of the axis representing the principle components
   */
  public void drawPCs(GL gl, Mesh mesh, float scl)
  {
  	Vector<Point> PC = mesh.getPC();
  	Point center = mesh.getCenter();
  	
  	if(PC != null){
	    gl.glDisable(GL.GL_LIGHTING);
	    gl.glLineWidth(3);
	    gl.glBegin(GL.GL_LINES);
	    
	    gl.glColor3f(1.0f, 0.0f, 0.0f);     
	    gl.glVertex3f((float)center.x, (float)center.y, (float)center.z);
	    gl.glVertex3f(scl*(float)PC.get(0).x, scl*(float)PC.get(0).y, scl*(float)PC.get(0).z);
	    
	    gl.glColor3f(0.0f, 1.0f, 0.0f);     
	    gl.glVertex3f((float)center.x, (float)center.y, (float)center.z);
	    gl.glVertex3f(scl*(float)PC.get(1).x, scl*(float)PC.get(1).y, scl*(float)PC.get(1).z);
	    
	    gl.glColor3f(0.0f, 0.0f, 1.0f);     
	    gl.glVertex3f((float)center.x, (float)center.y, (float)center.z);
	    gl.glVertex3f(scl*(float)PC.get(2).x, scl*(float)PC.get(2).y, scl*(float)PC.get(2).z);
	    
	    gl.glEnd();
  	}
  }
  
  /**
   * Draw the models points.
   *  @param gl the OpenGL context to render to
   */
  public void drawPoints(GL gl, Mesh mesh)
  {
  	Vector<Point> vertices = mesh.getVertices();
  	Vector<Color> vertex_colors = mesh.getVertexColors();
  	
    gl.glDisable(GL.GL_LIGHTING);
    gl.glPointSize(6);		//Default: 2
    gl.glBegin(GL.GL_POINTS);
    
    if(vertex_colors.size() != vertices.size()){
      gl.glColor3f(0.0f, 0.0f, 0.0f);
      
      for(int i=0; i<vertices.size(); i++){
        gl.glVertex3f((float)vertices.get(i).x, (float)vertices.get(i).y, (float)vertices.get(i).z);
      }
    }else{
      for(int i=0; i<vertices.size(); i++){
        gl.glColor3f(vertex_colors.get(i).r, vertex_colors.get(i).g, vertex_colors.get(i).b);     
        gl.glVertex3f((float)vertices.get(i).x, (float)vertices.get(i).y, (float)vertices.get(i).z);
      }
    }
    
    gl.glEnd();
  }
  
  /**
   * Draw the models edges.
   *  @param gl the OpenGL context to render to
   */
  public void drawEdges(GL gl, Mesh mesh)
  {
  	Vector<Edge> edges = mesh.getEdges();
  	Vector<Point> vertices = mesh.getVertices();
  	
    gl.glDisable(GL.GL_LIGHTING);
    gl.glColor3f(0.0f, 0.0f, 0.0f); 
    gl.glLineWidth(2);
    gl.glBegin(GL.GL_LINES);
    
    for(int i=0; i<edges.size(); i++){
      gl.glVertex3f((float)vertices.get(edges.get(i).v0).x, (float)vertices.get(edges.get(i).v0).y, (float)vertices.get(edges.get(i).v0).z);
      gl.glVertex3f((float)vertices.get(edges.get(i).v1).x, (float)vertices.get(edges.get(i).v1).y, (float)vertices.get(edges.get(i).v1).z);
    }
    
    gl.glEnd();
  }
  
  /**
   * Draw the occluding and critical edges of the model.
   *  @param gl the OpenGL context to render to
   *  @param M the current modelview matrix (stored elsewhere to prevent repeated extraction/conversion)
   */
  public void drawOutline(GL gl, Mesh mesh, double[][] M)
  {
  	Vector<Point> vertices = mesh.getVertices();
  	Vector<Face> faces = mesh.getFaces();
  	Vector<Boolean> faces_visible = mesh.getFacesVisible();
  	Vector<Edge> edges = mesh.getEdges();
  	Vector<Vector<Integer>> edge_incident_faces = mesh.getEdgeIncidentFaces();
  	Vector<Double> edge_dihedral_angles = mesh.getEdgeDihedralAngles();
  	
    Point p;
    Point cam = new Point(0, 0, -1000);   //Viewing direction
    Point view = new Point();
    Point norm;
    double max_angle = 60;
    double tmpd;
    
    //Label front facing faces
    for(int i=0; i<faces.size(); i++){
      p = Point.transform(M, faces.get(i).center);
      
      //Orthography
      cam.x = p.x;
      cam.y = p.y;

      view.assign(cam.minus(p));
      view.divideEquals(view.magnitude());
      
      norm = Point.transform(M, faces.get(i).normal);
      norm.divideEquals(norm.magnitude());
      
      tmpd = view.x * norm.x + view.y * norm.y + view.z * norm.z;
      
      if(tmpd > 0){ //Should be 0 but a little bit of tolerance looks better
        faces_visible.set(i, true);
      }else{
        faces_visible.set(i, false);
      }
    }

    //Find visible edges
    Vector<Integer> sharp_edges = new Vector<Integer>();
    Vector<Integer> outline_edges = new Vector<Integer>();
    
    for(int i=0; i<edge_incident_faces.size(); i++){
      if(edge_incident_faces.get(i).size() == 1){                      //Border edge
        sharp_edges.add(i);
      }else{                                  //Creases
        if(Math.abs(edge_dihedral_angles.get(i)) > max_angle){
          sharp_edges.add(i);
        }
      }

      //Silhouette
      if(edge_incident_faces.get(i).size() > 1 && faces_visible.get(edge_incident_faces.get(i).get(0)) ^ faces_visible.get(edge_incident_faces.get(i).get(1))){
        outline_edges.add(i);
      }
    }
    
    //Draw edges
    gl.glDisable(GL.GL_LIGHTING);
    gl.glColor3f(0.0f, 0.0f, 0.0f);    
    gl.glLineWidth(3);
    gl.glBegin(GL.GL_LINES);
    
    for(int i=0; i<outline_edges.size(); i++){
      gl.glVertex3f((float)vertices.get(edges.get(outline_edges.get(i)).v0).x, (float)vertices.get(edges.get(outline_edges.get(i)).v0).y, (float)vertices.get(edges.get(outline_edges.get(i)).v0).z);
      gl.glVertex3f((float)vertices.get(edges.get(outline_edges.get(i)).v1).x, (float)vertices.get(edges.get(outline_edges.get(i)).v1).y, (float)vertices.get(edges.get(outline_edges.get(i)).v1).z); 
    }
    
    for(int i=0; i<sharp_edges.size(); i++){
      gl.glVertex3f((float)vertices.get(edges.get(sharp_edges.get(i)).v0).x, (float)vertices.get(edges.get(sharp_edges.get(i)).v0).y, (float)vertices.get(edges.get(sharp_edges.get(i)).v0).z);
      gl.glVertex3f((float)vertices.get(edges.get(sharp_edges.get(i)).v1).x, (float)vertices.get(edges.get(sharp_edges.get(i)).v1).y, (float)vertices.get(edges.get(sharp_edges.get(i)).v1).z); 
    }
    
    gl.glEnd();  
  }
  
  /**
   * Draw the unshaded faces is a solid color.
   *  @param gl the OpenGL context to render to
   */
  public void drawSolid(GL gl, Mesh mesh)
  {
  	Vector<Face> faces = mesh.getFaces();
  	Vector<Point> vertices = mesh.getVertices();
  	
    gl.glDisable(GL.GL_LIGHTING);
    gl.glColor3f(1.0f, 1.0f, 1.0f);      
    gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
    gl.glPolygonOffset(1, 1);

    for(int i=0; i<faces.size(); i++){
      gl.glBegin(GL.GL_POLYGON);
      
      for(int j=0; j<faces.get(i).v.length; j++){
        gl.glVertex3f((float)vertices.get(faces.get(i).v[j]).x, (float)vertices.get(faces.get(i).v[j]).y, (float)vertices.get(faces.get(i).v[j]).z);
      }
      
      gl.glEnd();
    }
    
    gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
  }
  
  /**
   * Draw the shaded faces of the model.
   *  @param gl the OpenGL context to render to
   *  @param SMOOTH if true the vertex normals will be used instead of the face normals
   */
  public void drawShaded(GL gl, Mesh mesh, boolean SMOOTH)
  {
  	bindTextures(gl, mesh, true);	//Note: must force rebinding in case the canvas is resized!
  	
    if(!SMOOTH){
      drawShadedFlat(gl, mesh);
    }else{
      drawShadedSmooth(gl, mesh);
    }
    
    drawShadedDegenerate(gl, mesh);
  }
  
  /**
   * Draw the flat shaded faces of the model.
   *  @param gl the OpenGL context to render to
   */
  public void drawShadedFlat(GL gl, Mesh mesh)
  {
  	Vector<Point> vertices = mesh.getVertices();
  	Vector<Color> vertex_colors = mesh.getVertexColors();
  	Vector<Face> faces = mesh.getFaces();
  	
    Point norm;
    boolean TEXTURE = true;
    int tid = -1;
    
    //Draw faces
    gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
    gl.glPolygonOffset(1, 1);
    
    if(lighting == DrawOption.ENABLED){
    	//Restore default values in case materials were enabled and changed them!
    	gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, new float[]{0.2f, 0.2f, 0.2f, 1.0f}, 0);
    	gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, new float[]{0.8f, 0.8f, 0.8f, 1.0f}, 0);
    	
    	gl.glEnable(GL.GL_LIGHTING);
    }else if(lighting == DrawOption.MATERIAL){
	    gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE);
	    gl.glEnable(GL.GL_COLOR_MATERIAL);
	    gl.glEnable(GL.GL_LIGHTING);
    }
    
    if(texture == DrawOption.DISABLED){
    	TEXTURE = false;
    }else if(texture == DrawOption.DECAL){
	  	gl.glTexEnvi(gl.GL_TEXTURE_ENV, gl.GL_TEXTURE_ENV_MODE, gl.GL_DECAL);
	  }else if(texture == DrawOption.MODULATE){
	  	gl.glTexEnvi(gl.GL_TEXTURE_ENV, gl.GL_TEXTURE_ENV_MODE, gl.GL_MODULATE);
	  }

    if(vertex_colors.size() == vertices.size()){															//Use vertex colors
      for(int i=0; i<faces.size(); i++){
        if(faces.get(i).VISIBLE && faces.get(i).v.length >= 3){
          gl.glBegin(GL.GL_POLYGON);
          norm = faces.get(i).normal;
          gl.glNormal3f((float)norm.x, (float)norm.y, (float)norm.z);
        
          for(int j=0; j<faces.get(i).v.length; j++){
            gl.glColor3f(vertex_colors.get(faces.get(i).v[j]).r, vertex_colors.get(faces.get(i).v[j]).g, vertex_colors.get(faces.get(i).v[j]).b);
            gl.glVertex3f((float)vertices.get(faces.get(i).v[j]).x, (float)vertices.get(faces.get(i).v[j]).y, (float)vertices.get(faces.get(i).v[j]).z);
          }
          
          gl.glEnd();
        }
      }
    }else{																																		//Use face color
      for(int i=0; i<faces.size(); i++){
        if(faces.get(i).VISIBLE && faces.get(i).v.length >= 3){
        	if(TEXTURE && faces.get(i).uv != null && faces.get(i).material != null && faces.get(i).material.tid != -1){		//Textured 
        		gl.glEnable(gl.GL_TEXTURE_2D);
            
            if(tid != faces.get(i).material.tid){
            	tid = faces.get(i).material.tid;
            	gl.glBindTexture(gl.GL_TEXTURE_2D, tid);
            }
            
	          gl.glBegin(GL.GL_POLYGON);
	          norm = faces.get(i).normal;
	          gl.glNormal3f((float)norm.x, (float)norm.y, (float)norm.z);
	          
	          if(faces.get(i).material != null && faces.get(i).material.diffuse != null){	 
	          	gl.glColor3f(faces.get(i).material.diffuse.r, faces.get(i).material.diffuse.g, faces.get(i).material.diffuse.b);
	          }else{
	          	gl.glColor3f(1.0f, 1.0f, 1.0f);
	          }
	          
	          for(int j=0; j<faces.get(i).v.length; j++){
	            gl.glTexCoord2f(faces.get(i).uv[j].u, faces.get(i).uv[j].v);
	            gl.glVertex3f((float)vertices.get(faces.get(i).v[j]).x, (float)vertices.get(faces.get(i).v[j]).y, (float)vertices.get(faces.get(i).v[j]).z);
	          }
	          
	          gl.glEnd();
	          gl.glDisable(gl.GL_TEXTURE_2D);
        	}else{																															//Not textured
	          gl.glBegin(GL.GL_POLYGON);
	          norm = faces.get(i).normal;
	          gl.glNormal3f((float)norm.x, (float)norm.y, (float)norm.z);
	          
	          if(faces.get(i).material != null && faces.get(i).material.diffuse != null){
	          	gl.glColor3f(faces.get(i).material.diffuse.r, faces.get(i).material.diffuse.g, faces.get(i).material.diffuse.b);
	          }else{
	          	gl.glColor3f(0.5f, 0.5f, 0.5f);
	          }

	          for(int j=0; j<faces.get(i).v.length; j++){
	            gl.glVertex3f((float)vertices.get(faces.get(i).v[j]).x, (float)vertices.get(faces.get(i).v[j]).y, (float)vertices.get(faces.get(i).v[j]).z);
	          }
	          
	          gl.glEnd();
        	}
        }
      }
    }
    
    if(lighting == DrawOption.MATERIAL){
    	gl.glDisable(GL.GL_COLOR_MATERIAL);
    }
    
    gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
  }
  
  /**
   * Draw the flat shaded selected faces of the model.  Note, color can and should be set externally!
   *  @param gl the OpenGL context to render to
   *  @param selected_faces the faces to draw
   */
  public void drawShadedFlat(GL gl, Mesh mesh, Vector<Integer> selected_faces)
  {
  	Vector<Point> vertices = mesh.getVertices();
  	Vector<Face> faces = mesh.getFaces();
    Point norm;
    int index;
    
    //Draw faces
    gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
    gl.glPolygonOffset(-1, -1);
  	
    gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE);
    gl.glEnable(GL.GL_COLOR_MATERIAL);
    gl.glEnable(GL.GL_LIGHTING);
    
    for(int i=0; i<selected_faces.size(); i++){
    	index = selected_faces.get(i);
    	
      if(faces.get(index).VISIBLE && faces.get(index).v.length >= 3){
        gl.glBegin(GL.GL_POLYGON);
        norm = faces.get(index).normal;
        gl.glNormal3f((float)norm.x, (float)norm.y, (float)norm.z);

        for(int j=0; j<faces.get(index).v.length; j++){
          gl.glVertex3f((float)vertices.get(faces.get(index).v[j]).x, (float)vertices.get(faces.get(index).v[j]).y, (float)vertices.get(faces.get(index).v[j]).z);
        }
        
        gl.glEnd();
    	}
    }
    
  	gl.glDisable(GL.GL_COLOR_MATERIAL);
    gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
  }
  
  /**
   * Draw the smooth shaded faces of the model using vertex colors.
   *  @param gl the OpenGL context to render to
   */
  public void drawShadedSmooth(GL gl, Mesh mesh)
  {
  	Vector<Point> vertices = mesh.getVertices();
  	Vector<Point> vertex_normals = mesh.getVertexNormals();
  	Vector<Color> vertex_colors = mesh.getVertexColors();
  	Vector<Face> faces = mesh.getFaces();
    Point norm;
    boolean TEXTURE = true;
    int tid = -1;
    
    gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
    gl.glPolygonOffset(1, 1);
    
    if(lighting == DrawOption.ENABLED){
    	//Restore default values in case materials were enabled and changed them!
    	gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, new float[]{0.2f, 0.2f, 0.2f, 1.0f}, 0);
    	gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, new float[]{0.8f, 0.8f, 0.8f, 1.0f}, 0);
    	
    	gl.glEnable(GL.GL_LIGHTING);
    }else if(lighting == DrawOption.MATERIAL){
	    gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE);
	    gl.glEnable(GL.GL_COLOR_MATERIAL);
	    gl.glEnable(GL.GL_LIGHTING);
    }
    
    if(texture == DrawOption.DISABLED){
    	TEXTURE = false;
    }else if(texture == DrawOption.DECAL){
	  	gl.glTexEnvi(gl.GL_TEXTURE_ENV, gl.GL_TEXTURE_ENV_MODE, gl.GL_DECAL);
	  }else if(texture == DrawOption.MODULATE){
	  	gl.glTexEnvi(gl.GL_TEXTURE_ENV, gl.GL_TEXTURE_ENV_MODE, gl.GL_MODULATE);
	  }
    
    if(vertex_colors.size() == vertices.size()){															//Use vertex colors
      for(int i=0; i<faces.size(); i++){
        if(faces.get(i).VISIBLE && faces.get(i).v.length >= 3){
          gl.glBegin(GL.GL_POLYGON);
          
          for(int j=0; j<faces.get(i).v.length; j++){
          	if(faces.get(i).vn == null){
          		norm = vertex_normals.get(faces.get(i).v[j]);
          	}else{
          		norm = faces.get(i).vn[j];
          	}
          	
            gl.glNormal3f((float)norm.x, (float)norm.y, (float)norm.z);
            gl.glColor3f(vertex_colors.get(faces.get(i).v[j]).r, vertex_colors.get(faces.get(i).v[j]).g, vertex_colors.get(faces.get(i).v[j]).b);
            gl.glVertex3f((float)vertices.get(faces.get(i).v[j]).x, (float)vertices.get(faces.get(i).v[j]).y, (float)vertices.get(faces.get(i).v[j]).z);
          }
          
          gl.glEnd();
        }
      }
    }else{																																		//Use face color
      for(int i=0; i<faces.size(); i++){
        if(faces.get(i).VISIBLE && faces.get(i).v.length >= 3){
        	if(TEXTURE && faces.get(i).uv != null && faces.get(i).material != null && faces.get(i).material.tid != -1){		//Textured 
        		gl.glEnable(gl.GL_TEXTURE_2D);
            
            if(tid != faces.get(i).material.tid){
            	tid = faces.get(i).material.tid;
            	gl.glBindTexture(gl.GL_TEXTURE_2D, tid);
            }
            
	          gl.glBegin(GL.GL_POLYGON);
	          
	          if(faces.get(i).material != null && faces.get(i).material.diffuse != null){
	          	gl.glColor3f(faces.get(i).material.diffuse.r, faces.get(i).material.diffuse.g, faces.get(i).material.diffuse.b);
	          }else{
	          	gl.glColor3f(1.0f, 1.0f, 1.0f);
	          }
	        
	          for(int j=0; j<faces.get(i).v.length; j++){
	          	if(faces.get(i).vn == null){
	          		norm = vertex_normals.get(faces.get(i).v[j]);
	          	}else{
	          		norm = faces.get(i).vn[j];
	          	}
	          	
	            gl.glNormal3f((float)norm.x, (float)norm.y, (float)norm.z);
	            gl.glTexCoord2f(faces.get(i).uv[j].u, faces.get(i).uv[j].v);
	            gl.glVertex3f((float)vertices.get(faces.get(i).v[j]).x, (float)vertices.get(faces.get(i).v[j]).y, (float)vertices.get(faces.get(i).v[j]).z);
	          }
	          
	          gl.glEnd();
	          gl.glDisable(gl.GL_TEXTURE_2D);
        	}else{																															//Not textured
	          gl.glBegin(GL.GL_POLYGON);
	          
	          if(faces.get(i).material != null && faces.get(i).material.diffuse != null){
	          	gl.glColor3f(faces.get(i).material.diffuse.r, faces.get(i).material.diffuse.g, faces.get(i).material.diffuse.b);
	          }else{
	          	gl.glColor3f(0.5f, 0.5f, 0.5f);
	          }
	          
	          for(int j=0; j<faces.get(i).v.length; j++){
	          	if(faces.get(i).vn == null){
	          		norm = vertex_normals.get(faces.get(i).v[j]);
	          	}else{
	          		norm = faces.get(i).vn[j];
	          	}
	          	
	            gl.glNormal3f((float)norm.x, (float)norm.y, (float)norm.z);
	            gl.glVertex3f((float)vertices.get(faces.get(i).v[j]).x, (float)vertices.get(faces.get(i).v[j]).y, (float)vertices.get(faces.get(i).v[j]).z);
	          }
	          
	          gl.glEnd();
        	}
        }
      }
    }
      
    if(lighting == DrawOption.MATERIAL){
    	gl.glDisable(GL.GL_COLOR_MATERIAL);
    }
    
    gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
  }
  
  /**
   * Draw degenerate polygons (i.e. edges).
   *  @param gl the OpenGL context to render to
   */
  public void drawShadedDegenerate(GL gl, Mesh mesh)
  {
  	Vector<Point> vertices = mesh.getVertices();
  	Vector<Color> vertex_colors = mesh.getVertexColors();
  	Vector<Face> faces = mesh.getFaces();
  	
    gl.glDisable(GL.GL_LIGHTING);
    gl.glLineWidth(1.0f);
    gl.glBegin(GL.GL_LINES);

    for(int i=0; i<faces.size(); i++){
      if(faces.get(i).VISIBLE && faces.get(i).v.length == 2){
        for(int j=0; j<faces.get(i).v.length; j++){
        	if(faces.get(i).material != null && faces.get(i).material.diffuse != null){
	        	gl.glColor3f(faces.get(i).material.diffuse.r, faces.get(i).material.diffuse.g, faces.get(i).material.diffuse.b);
        	}else if(faces.get(i).v[j] < vertex_colors.size()){
            gl.glColor3f(vertex_colors.get(faces.get(i).v[j]).r, vertex_colors.get(faces.get(i).v[j]).g, vertex_colors.get(faces.get(i).v[j]).b);
	        }else{
	        	gl.glColor3f(0.5f, 0.5f, 0.5f);
	        }	
        	
          gl.glVertex3f((float)vertices.get(faces.get(i).v[j]).x, (float)vertices.get(faces.get(i).v[j]).y, (float)vertices.get(faces.get(i).v[j]).z);
        }
      }
    }
    
    gl.glEnd();
  }
  
  /**
   * Draw the faces shading them only with regards to a specular compent.  This is done in a manner
   * similar to that in drawIllustration.
   *  @param gl the OpenGL context to render to
   *  @param M the current modelview matrix
   */
  public void drawHighlights(GL gl, Mesh mesh, double[][] M)
  {
  	Vector<Point> vertices = mesh.getVertices();
  	Vector<Point> vertex_normals = mesh.getVertexNormals();
  	Vector<Face> faces = mesh.getFaces();
  	
    float ks = 1.0f;
    float ns = 2f; //16f;
    float tmpf;
      
    Point light = new Point(0.70711, -0.70711, 0.0);
    Point norm;
    Color ambient = new Color(0.7f, 0.7f, 0.7f);
    Color specular = new Color(1, 1, 1);
    Color color = new Color();
    
    gl.glDisable(GL.GL_LIGHTING);
    gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
    gl.glPolygonOffset(1, 1);

    for(int i=0; i<faces.size(); i++){
      gl.glBegin(GL.GL_POLYGON);
      
      for(int j=0; j<faces.get(i).v.length; j++){
        norm = Point.transform(M, vertex_normals.get(faces.get(i).v[j]));
        norm.divideEquals(norm.magnitude());
        tmpf = (float)light.times(norm);

        color.assign(ambient);

        //Specular
        if(tmpf < 0) tmpf = 0;
        color.r += ks*specular.r*Math.pow(tmpf,ns);
        color.g += ks*specular.g*Math.pow(tmpf,ns);
        color.b += ks*specular.b*Math.pow(tmpf,ns);
          
        gl.glColor3f(color.r, color.g, color.b);
        gl.glVertex3f((float)vertices.get(faces.get(i).v[j]).x, (float)vertices.get(faces.get(i).v[j]).y, (float)vertices.get(faces.get(i).v[j]).z);
      }
      
      gl.glEnd();
    }
    
    gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
  }
  
  /**
   * Draw the faces shaded in a non-photorealistic manner that is similar to book illustrations which
   * emphasize shape within the rendering [Gooch et al., SIGGRAPH 1998].
   *  @param gl the OpenGL context to render to
   *  @param M the current modelview matrix
   */
  public void drawIllustration(GL gl, Mesh mesh, double[][] M)
  {
  	Vector<Point> vertices = mesh.getVertices();
  	Vector<Point> vertex_normals = mesh.getVertexNormals();
  	Vector<Face> faces = mesh.getFaces();
  	
    float blue = 0.4f;
    float yellow = 0.4f;
    float alpha = 0.2f;
    float beta = 0.6f;
    float ks = 1.0f;
    float ns = 16f;
    float tmpf;
      
    Point light = new Point(0.70711, -0.70711, 0.0);
    Point norm;
    Color diffuse = new Color(1, 1, 1);
    Color specular = new Color(1, 1, 1);
    Color kblue = new Color(0, 0, blue);
    Color kyellow = new Color(yellow, yellow, 0);
    Color kcool = new Color();
    Color kwarm = new Color();
    Color color = new Color();
    
    gl.glDisable(GL.GL_LIGHTING);
    gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
    gl.glPolygonOffset(1, 1);

    for(int i=0; i<faces.size(); i++){
      gl.glBegin(GL.GL_POLYGON);
      
      for(int j=0; j<faces.get(i).v.length; j++){
        norm = Point.transform(M, vertex_normals.get(faces.get(i).v[j]));
        norm.divideEquals(norm.magnitude());
        tmpf = (float)light.times(norm);
          
        //Shading
        kcool.r = kblue.r + (alpha * diffuse.r);
        kcool.g = kblue.g + (alpha * diffuse.g);
        kcool.b = kblue.b + (alpha * diffuse.b);
        kwarm.r = kyellow.r + (beta * diffuse.r);
        kwarm.g = kyellow.g + (beta * diffuse.g);
        kwarm.b = kyellow.b + (beta * diffuse.b);

        color.r = (((1.0f+tmpf)/2.0f) * kwarm.r) + ((1.0f - ((1+tmpf)/2.0f)) * kcool.r);
        color.g = (((1.0f+tmpf)/2.0f) * kwarm.g) + ((1.0f - ((1+tmpf)/2.0f)) * kcool.g);
        color.b = (((1.0f+tmpf)/2.0f) * kwarm.b) + ((1.0f - ((1+tmpf)/2.0f)) * kcool.b);

        //Specular
        if(tmpf < 0) tmpf = 0;
        color.r += ks*specular.r*Math.pow(tmpf,ns);
        color.g += ks*specular.g*Math.pow(tmpf,ns);
        color.b += ks*specular.b*Math.pow(tmpf,ns);
          
        gl.glColor3f(color.r, color.g, color.b);
        gl.glVertex3f((float)vertices.get(faces.get(i).v[j]).x, (float)vertices.get(faces.get(i).v[j]).y, (float)vertices.get(faces.get(i).v[j]).z);
      }
      
      gl.glEnd();
    }
    
    gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
  }
  
  /**
   * Draw the faces shaded in a non-photorealistic manner that is similar to 
   * metallic illustrations [Gooch et al., SIGGRAPH 1998].
   *  @param gl the OpenGL context to render to
   *  @param M the current modelview matrix
   */
  public void drawMetal(GL gl, Mesh mesh, double[][] M)
  {
  	Vector<Point> vertices = mesh.getVertices();
  	Vector<Face> faces = mesh.getFaces();
  	Vector<Point> axis = mesh.getMetallicAxis();
  	Vector<Double> stripes = mesh.getMetallicStripes();
  	
    if(!mesh.INITIALIZED_METAL){
      mesh.initialize_metal();
      mesh.INITIALIZED_METAL = true;
    }
    
    //Vertex light = new Vertex(0.70711, -0.70711, 0.0);
    Point light = new Point(0.57735, 0.57735, 0.57735);
    Point radial = new Point();    
    Point tmpv = new Point();
    double tmpd;
    
    gl.glDisable(GL.GL_LIGHTING);
    gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
    gl.glPolygonOffset(1, 1);

    for(int i=0; i<faces.size(); i++){
      gl.glBegin(GL.GL_POLYGON);
      
      for(int j=0; j<faces.get(i).v.length; j++){
        int k = faces.get(i).v[j];
        tmpv.x = vertices.get(k).x - axis.get(k).x;
        tmpv.y = vertices.get(k).y - axis.get(k).y;
        tmpv.z = vertices.get(k).z - axis.get(k).z;
        
        tmpv.divideEquals(tmpv.magnitude());
        radial = Point.transform(M, tmpv);
        
        tmpd = light.x*radial.x +  light.y*radial.y + light.z*radial.z;
        tmpd = (tmpd + 1.0) / 2.0;        //Should be between 0 and 1
        if(tmpd < 0) tmpd = 0;
        if(tmpd > 1) tmpd = 1;
        tmpd = stripes.get((int)Math.round(tmpd * ((double)(stripes.size()-1))));
        
        gl.glColor3f((float)tmpd, (float)tmpd, (float)tmpd);
        gl.glVertex3f((float)vertices.get(faces.get(i).v[j]).x, (float)vertices.get(faces.get(i).v[j]).y, (float)vertices.get(faces.get(i).v[j]).z);
      }
      
      gl.glEnd();
    }
  }

	/**
   * Listener for mouse pressed events.  On left clicks the button pressed and the (x, y) coordinates
   * are stored.  In addition we enable refreshing of the display. On right clicks a menu is displayed.
   *  @param e the mouse event
   */
  public void mousePressed(MouseEvent e)
  {
  	if(e.getButton() == MouseEvent.BUTTON1){
  		clicked_button = 1;
  	}else if(e.getButton() == MouseEvent.BUTTON3){
  		clicked_button = 3;
  	}else{
  		clicked_button = -1;
  	}
  	
  	last_x = e.getX();
    last_y = e.getY();
    
    if(clicked_button == 1){
      RIGHT_FUNCTION_PRIORITIZED = false;
    }else if(clicked_button == 3){
    	if(SELECTING){
      	if(SELECT_BOUNDING_BOX){
	      	selection_p0 = new Point(e.getX(), e.getY(), 0);
	      	selection_p1 = new Point(e.getX(), e.getY(), 0);
      	}else if(SELECT_HIGHLIGHT || SELECT_COMPONENT){
      		if(SELECT_USING_DEPTH){
      			visible_faces = mesh.getVisibleFaces(modelview, width, height);
      		}else{
      			visible_faces = null;
      		}

      		if(SELECT_HIGHLIGHT){
      			selected_vertices = mesh.getSelectionVertices(modelview, e.getX()-halfwidth, halfheight-e.getY(), visible_faces);
      		}else if(SELECT_COMPONENT){
      			selected_vertices = mesh.getConnectedComponent(mesh.getSelectionVertices(modelview, e.getX()-halfwidth, halfheight-e.getY(), visible_faces));
      		}

  				selected_vertex_faces = mesh.getEnclosedFaces(selected_vertices);

  				if(SELECT_FOR_COLOR || SELECT_FOR_SMOOTH){
  					if(SELECT_FOR_COLOR){
	  					for(int i=0; i<selected_vertex_faces.size(); i++){
	  						mesh.getFace(selected_vertex_faces.get(i)).material = new Material(new MeshAuxiliary.Color(color_chooser.getColor().getRGB()));
	  					}
  					}else if(SELECT_FOR_SMOOTH){
  						mesh.smoothVertices(selected_vertices);
  					}
  					
  					//Don't need to save these and don't want to highlight them
  					selected_vertices = null;
  					selected_vertex_faces = null;
  					
  					refreshList();
  				}
      	}
      	
      	refresh(true);
      }else if(BENDING_JOINT){
        selected_component = mesh.getSelectionComponent(selected_component_faces, modelview, e.getX()-halfwidth, halfheight-e.getY());
        RIGHT_FUNCTION_PRIORITIZED = true;
      }else{
      	popup_menu.show(e.getComponent(), e.getX(), e.getY());
      }
    }
    
    //Give this panel focus to capture key events!
    canvas.requestFocus();
  }
  
  /**
   * Listener for mouse dragged events.  If the last pressed button was the left one then
   * adjust the translation of the scene if the shift button is also down or the rotation of the
   * scene otherwise.
   *  @param e the mouse event
   */
  public void mouseDragged(MouseEvent e)
  {
    if(clicked_button == 1){
      if(e.isShiftDown()){
      	update_translation(selected_transformation, e.getX()-last_x, last_y-e.getY(), 0);
      }else{
      	selected_transformation.rx = (selected_transformation.rx + (e.getY()-last_y)) % 360;
      	selected_transformation.ry = (selected_transformation.ry + (e.getX()-last_x)) % 360;
      }
    }else if(clicked_button == 3){
    	if(SELECTING){
    		if(SELECT_BOUNDING_BOX){
	    		selection_p1.x = e.getX();
	    		selection_p1.y = e.getY();
    		}else if(SELECT_HIGHLIGHT){
					selected_vertices = Utility.union(selected_vertices, mesh.getSelectionVertices(modelview, e.getX()-halfwidth, halfheight-e.getY(), visible_faces));
  				selected_vertex_faces = mesh.getEnclosedFaces(selected_vertices);

  				if(SELECT_FOR_COLOR || SELECT_FOR_SMOOTH){
  					if(SELECT_FOR_COLOR){
	  					for(int i=0; i<selected_vertex_faces.size(); i++){
	  						mesh.getFace(selected_vertex_faces.get(i)).material = new Material(new MeshAuxiliary.Color(color_chooser.getColor().getRGB()));
	  					}
  					}else if(SELECT_FOR_SMOOTH){
  						mesh.smoothVertices(selected_vertices);
  					}
  					
  					//Don't need to save these and don't want to highlight them
  					selected_vertices = null;
  					selected_vertex_faces = null;
  					
  					refreshList();
  				}
    		}
    	}else if(BENDING_JOINT){
    		mesh.transformVertices(selected_components.get(selected_component), modelview, selected_joint, (e.getY()-last_y)%360, (e.getX()-last_x)%360, 0, 1, 1, 1, bend_smoothness);
        refreshList();
    	}
    }
    
    last_x = e.getX();
    last_y = e.getY();
    
    refresh(true);
  }
  
  /**
   * Listener for mouse released events.
   *  @param e the mouse event
   */
  public void mouseReleased(MouseEvent e)
  {
  	if(clicked_button == 1){
	  	double[][] Rxyz = null;
	  	
	  	if(selected_transformation == transformation){
	  		Rxyz = MatrixUtility.rotateXYZ(selected_transformation.rx, selected_transformation.ry, selected_transformation.rz);
	  		
	  		MatrixUtility.set(selected_rotation_last, MatrixUtility.mtimes(Rxyz, selected_rotation_last));
	  		rotation_last_inv = MatrixUtility.transpose(rotation_last);
	  	}else{
	  		double[][] Rx = MatrixUtility.rotate(selected_transformation.rx, rotation_last_inv[0][0], rotation_last_inv[1][0], rotation_last_inv[2][0]);
	  		double[][] Ry = MatrixUtility.rotate(selected_transformation.ry, rotation_last_inv[0][1], rotation_last_inv[1][1], rotation_last_inv[2][1]);
	  		double[][] Rz = MatrixUtility.rotate(selected_transformation.rz, rotation_last_inv[0][2], rotation_last_inv[1][2], rotation_last_inv[2][2]);
	
	  		Rxyz = MatrixUtility.mtimes(MatrixUtility.mtimes(Rx, Ry), Rz);
	  		
	  		MatrixUtility.set(selected_rotation_last, MatrixUtility.mtimes(Rxyz, selected_rotation_last));
	  	}
	  	
	  	selected_transformation.rx = 0;
	  	selected_transformation.ry = 0;
	  	selected_transformation.rz = 0;
  	}
  	
  	if(clicked_button == 3){
	  	if(SELECTING){
	  		if(SELECT_BOUNDING_BOX){
	  			if(SELECT_USING_DEPTH){
	  				visible_faces = mesh.getVisibleFaces(modelview, width, height);
	  			}else{
	  				visible_faces = null;
	  			}
	  			
		  		selected_vertices = mesh.getSelectionVertices(modelview, selection_minx, selection_maxx, selection_miny, selection_maxy, visible_faces);
					selected_vertex_faces = mesh.getEnclosedFaces(selected_vertices);
		  		selection_p0 = null;
		  		selection_p1 = null;
		  		
					if(SELECT_FOR_COLOR || SELECT_FOR_SMOOTH){
						if(SELECT_FOR_COLOR){
							for(int i=0; i<selected_vertex_faces.size(); i++){
								mesh.getFace(selected_vertex_faces.get(i)).material = new Material(new MeshAuxiliary.Color(color_chooser.getColor().getRGB()));
							}
  					}else if(SELECT_FOR_SMOOTH){
  						mesh.smoothVertices(selected_vertices);
  					}
						
						//Don't need to save these and don't want to highlight them
						selected_vertices = null;
						selected_vertex_faces = null;
						
						refreshList();
					}
	  		}
	  		
	  		if(SELECT_FOR_JOINT){
	  			selected_joint = mesh.getCentroid(selected_vertices);
	  			selected_components = mesh.getCutComponents(selected_vertices);
	  			
	  			if(selected_components.size() > 1){
		  			selected_component_faces = new Vector<Vector<Integer>>();
		  			
		  			for(int i=0; i<selected_components.size(); i++){
		  				selected_component_faces.add(mesh.getEnclosedFaces(selected_components.get(i)));
		  			}
		  			
		  			BENDING_JOINT = true;
	  			}
	  		}
	  		
	  		if(!SELECT_FOR_COLOR && !SELECT_FOR_SMOOTH)	SELECTING = false;
	  	}
  	}
  	
  	refresh(true);
  }
  
  /** 
   * Listener for mouse wheel events.  Zoom in our out depending on wheel motion's direction.
   *  @param e mouse wheel event
   */
  public void mouseWheelMoved(MouseWheelEvent e)
  {
    if(e.getWheelRotation() < 0){
    	if(RIGHT_FUNCTION_PRIORITIZED){
    		mesh.transformVertices(selected_components.get(selected_component), modelview, selected_joint, 0, 0, 0, 1.04, 1.04, 1.04, bend_smoothness);
    		refreshList();
    	}else{
    		selected_transformation.scl *= 1.04;
    	}
    }else{
    	if(RIGHT_FUNCTION_PRIORITIZED){
    		mesh.transformVertices(selected_components.get(selected_component), modelview, selected_joint, 0, 0, 0, 0.96, 0.96, 0.96, bend_smoothness);
    		refreshList();
    	}else{
    		selected_transformation.scl /= 1.04;
    	}
    }
    
    refresh(true);
  }
  
  /**
   * Listener for keyboard events.
   *  @param e the keyboard event
   */
  public void keyPressed(KeyEvent e)
  {
  	if(e.getKeyChar() == 'r'){
  		if(RIGHT_FUNCTION_PRIORITIZED){
    		mesh.transformVertices(selected_components.get(selected_component), modelview, selected_joint, 0, 0, -2, 1, 1, 1, bend_smoothness);
    		refreshList();
  		}else{
  			selected_transformation.rz -= 2;
  		}
  		
  		refresh(true);
  	}else if(e.getKeyChar() == 'R'){
  		if(RIGHT_FUNCTION_PRIORITIZED){
    		mesh.transformVertices(selected_components.get(selected_component), modelview, selected_joint, 0, 0, 2, 1, 1, 1, bend_smoothness);
    		refreshList();
  		}else{
  			selected_transformation.rz += 2;
  		}
  		
  		refresh(true);
  	}else if(e.getKeyChar() == 'z'){
  		update_translation(selected_transformation, 0, 0, -10);
  		refresh(true);
  	}else if(e.getKeyChar() == 'Z'){
  		update_translation(selected_transformation, 0, 0, 10);
  		refresh(true);
  	}else if(e.getKeyChar() == '0'){
  		//Turn off all other radio buttons
			for(int i=0; i<menuitem_MESHES.size(); i++){
	  		menuitem_MESHES.get(i).setSelected(false);
	  	}
			
			//Turn on the selected radio button only
			menuitem_MESHES.get(0).setSelected(true);
			
			selected_rotation_last = rotation_last;
  		selected_transformation = transformation;
  	}else if(e.getKeyChar() >= '1' && e.getKeyChar() <='9'){
  		int index = Integer.valueOf(e.getKeyChar()+"") - 1;

  		if(index < added_transformations.size()){
  			//Turn off all other radio buttons
  			for(int i=0; i<menuitem_MESHES.size(); i++){
		  		menuitem_MESHES.get(i).setSelected(false);
		  	}
  			
  			//Turn on the selected radio button only
  			menuitem_MESHES.get(index+1).setSelected(true);
  			
  			//Set the appropriate transformation    			
  			selected_rotation_last = added_rotation_last.get(index);
  			selected_transformation = added_transformations.get(index);
  		}
  	}else if(e.getKeyCode() == KeyEvent.VK_SHIFT){
  		if(BENDING_JOINT){
  			bend_smoothness = (bend_smoothness+1) % 3;
  		}
  	}else if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
  		selected_vertices = null;
  		selected_vertex_faces = null;
  		
  		if(BENDING_JOINT){
  			mesh.initialize_positions();
  			
  			selected_components = null;
  			selected_component_faces = null;
  			RIGHT_FUNCTION_PRIORITIZED = false;
  			BENDING_JOINT = false;
  		}else if(SELECT_FOR_COLOR || SELECT_FOR_SMOOTH){
  			SELECTING = false;
  		}
  		
  		refresh(true);
  	}
  }
  
  /**
   * Listener for action events.  Deals with clicked buttons and selected menus.
   *  @param e the action event
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
    }else if(source == menuitem_ADD){
      JFileChooser fc = new JFileChooser(load_path);
      
      if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
        String pathname = Utility.unixPath(fc.getCurrentDirectory().getAbsolutePath()) + "/";
        String filename = fc.getSelectedFile().getName();
        add(pathname + filename);
      }
    }else if(source == menuitem_EXPORT_JPG){
      save(export_path, mesh.getMetaData("Name") + ".jpg");
    }else if(source == menuitem_EXPORT_OBJ){
      save(export_path, mesh.getMetaData("Name") + ".obj");
    }else if(source == menuitem_EXPORT_OBJ_BIN1){
      save(export_path, mesh.getMetaData("Name") + ".obj_bin1");
    }else if(source == menuitem_EXPORT_OBJ_BIN2){
      save(export_path, mesh.getMetaData("Name") + ".obj_bin2");
    }else if(source == menuitem_EXPORT_PLY){
      save(export_path, mesh.getMetaData("Name") + ".ply");
    }else if(source == menuitem_EXPORT_VH){
      save(export_path, mesh.getMetaData("Name") + ".vh"); 
    }else if(source == menuitem_EXPORT_WRL){
      save(export_path, mesh.getMetaData("Name") + ".wrl");
    }else if(source == menuitem_EXPORT_DEPTH){
      save(export_path, mesh.getMetaData("Name") + ".pgm");
    }else if(source == menuitem_EXPORT_POINTS_CAMERAS){
      save(export_path, mesh.getMetaData("Name") + ".pc");
    }else if(source == menuitem_VIEW_GROUPS_ALL){
    	for(int i=0; i<menuitem_VIEW_GROUPS.size(); i++){
    		menuitem_VIEW_GROUPS.get(i).setState(!menuitem_VIEW_GROUPS.get(i).getState());
  			mesh.toggleGroup(menuitem_VIEW_GROUPS.get(i).getText());
    	}
    	
    	refreshList();
    }else if(source == menuitem_ORTHO){
      ORTHO = !ORTHO;
      menuitem_ORTHO.setState(ORTHO);
      UPDATE_CAMERA = true;
    }else if(source == menuitem_SHADED_SMOOTH){
      SHADED_SMOOTH = !SHADED_SMOOTH;
      menuitem_SHADED_SMOOTH.setState(SHADED_SMOOTH);
      refreshList();
    }else if(source == menuitem_AXIS){
      AXIS = !AXIS;
      menuitem_AXIS.setState(AXIS);
    }else if(source == menuitem_PC_AXIS){
      PC_AXIS = !PC_AXIS;
      menuitem_PC_AXIS.setState(PC_AXIS);
    }else if(source == menuitem_POINTS){
      POINTS = !POINTS;
      menuitem_POINTS.setSelected(POINTS); 
    }else if(source == menuitem_WIRE){
      WIRE = !WIRE;
      menuitem_WIRE.setSelected(WIRE);
    }else if(source == menuitem_OUTLINE){
      OUTLINE = !OUTLINE;
      menuitem_OUTLINE.setSelected(OUTLINE);
      
      ORTHO = true;
      menuitem_ORTHO.setState(ORTHO);
      UPDATE_CAMERA = true;
    }else if(source == menuitem_TRANSPARENT || 
    				 source == menuitem_SOLID || 
    				 source == menuitem_SHADED || 
    				 source == menuitem_HIGHLIGHTS || 
    				 source == menuitem_ILLUSTRATION || 
    				 source == menuitem_METAL){
    	
      TRANSPARENT = menuitem_TRANSPARENT.isSelected();   	
      SOLID = menuitem_SOLID.isSelected();
      SHADED = menuitem_SHADED.isSelected();
      HIGHLIGHTS = menuitem_HIGHLIGHTS.isSelected();
      ILLUSTRATION = menuitem_ILLUSTRATION.isSelected();
      METAL = menuitem_METAL.isSelected();
    }else if(source == menuitem_SHADED_LIGHTING_DISABLED ||
    				 source == menuitem_SHADED_LIGHTING_ENABLED ||
    				 source == menuitem_SHADED_LIGHTING_MATERIAL){
    	
      SHADED = true; menuitem_SHADED.setSelected(SHADED);
      
      TRANSPARENT = menuitem_TRANSPARENT.isSelected();   	
      SOLID = menuitem_SOLID.isSelected();
      SHADED = menuitem_SHADED.isSelected();
      HIGHLIGHTS = menuitem_HIGHLIGHTS.isSelected();
      ILLUSTRATION = menuitem_ILLUSTRATION.isSelected();
      METAL = menuitem_METAL.isSelected();
      
    	SHADED_LIGHTING_DISABLED = menuitem_SHADED_LIGHTING_DISABLED.isSelected();
    	SHADED_LIGHTING_ENABLED = menuitem_SHADED_LIGHTING_ENABLED.isSelected();
    	SHADED_LIGHTING_MATERIAL = menuitem_SHADED_LIGHTING_MATERIAL.isSelected();
    	
    	if(source == menuitem_SHADED_LIGHTING_DISABLED){
    		lighting = DrawOption.DISABLED;
    	}else if(source == menuitem_SHADED_LIGHTING_ENABLED){
    		lighting = DrawOption.ENABLED;
    	}else if(source == menuitem_SHADED_LIGHTING_MATERIAL){
    		lighting = DrawOption.MATERIAL;
    	}
    	
    	refreshList();
    }else if(source == menuitem_SHADED_TEXTURE_DISABLED ||
    				 source == menuitem_SHADED_TEXTURE_DECAL ||
    				 source == menuitem_SHADED_TEXTURE_MODULATE){
    	
      SHADED = true; menuitem_SHADED.setSelected(SHADED);
      
      TRANSPARENT = menuitem_TRANSPARENT.isSelected();   	
      SOLID = menuitem_SOLID.isSelected();
      SHADED = menuitem_SHADED.isSelected();
      HIGHLIGHTS = menuitem_HIGHLIGHTS.isSelected();
      ILLUSTRATION = menuitem_ILLUSTRATION.isSelected();
      METAL = menuitem_METAL.isSelected();
      
    	SHADED_TEXTURE_DISABLED = menuitem_SHADED_TEXTURE_DISABLED.isSelected();
    	SHADED_TEXTURE_DECAL = menuitem_SHADED_TEXTURE_DECAL.isSelected();
    	SHADED_TEXTURE_MODULATE = menuitem_SHADED_TEXTURE_MODULATE.isSelected();

    	if(source == menuitem_SHADED_TEXTURE_DISABLED){
    		texture = DrawOption.DISABLED;
    	}else if(source == menuitem_SHADED_TEXTURE_DECAL){
    		texture = DrawOption.DECAL;
    	}else if(source == menuitem_SHADED_TEXTURE_MODULATE){
    		texture = DrawOption.MODULATE;
    	}
    	
    	refreshList();
    }else if(source == menuitem_RAYTRACE){
      ORTHO = false;
      menuitem_ORTHO.setState(ORTHO);
      UPDATE_CAMERA = true;
      RAYTRACE = true;
    }else if(source == menuitem_SELECT_BOUNDING_BOX ||
    				 source == menuitem_SELECT_HIGHLIGHT || 
    				 source == menuitem_SELECT_COMPONENT){
    	
    	SELECT_BOUNDING_BOX = false;
    	SELECT_HIGHLIGHT = false;
    	SELECT_COMPONENT = false;

    	if(source == menuitem_SELECT_BOUNDING_BOX){
    		SELECT_BOUNDING_BOX = true;
    	}else if(source == menuitem_SELECT_HIGHLIGHT){
    		SELECT_HIGHLIGHT = true;
    	}else if(source == menuitem_SELECT_COMPONENT){
      	SELECT_COMPONENT = true;
    	}
    	
    	SELECTING = true;
    	
    	if(SELECT_FOR_COLOR){		
    		if(!SHADED_LIGHTING_MATERIAL){	//In order to see colors enable materials during lighting
	    		SHADED_LIGHTING_MATERIAL = true; menuitem_SHADED_LIGHTING_MATERIAL.setSelected(SHADED_LIGHTING_MATERIAL);
	      	SHADED_LIGHTING_DISABLED = menuitem_SHADED_LIGHTING_DISABLED.isSelected();
	      	SHADED_LIGHTING_ENABLED = menuitem_SHADED_LIGHTING_ENABLED.isSelected();
	      	
	      	lighting = DrawOption.MATERIAL;
	      	
	      	refreshList();
    		}
    	}
    }else if(source == menuitem_SELECT_FOR_NOTHING ||
				 		 source == menuitem_SELECT_FOR_JOINT ||
				 		 source == menuitem_SELECT_FOR_COLOR ||
				 		 source == menuitem_SELECT_FOR_SMOOTH){
	
			SELECT_FOR_NOTHING = menuitem_SELECT_FOR_NOTHING.isSelected();
			SELECT_FOR_JOINT = menuitem_SELECT_FOR_JOINT.isSelected();
			SELECT_FOR_COLOR = menuitem_SELECT_FOR_COLOR.isSelected();
			SELECT_FOR_SMOOTH = menuitem_SELECT_FOR_SMOOTH.isSelected();
		
			if(source == menuitem_SELECT_FOR_COLOR){
				if(color_chooser == null){
					color_chooser = new JColorChooser();
				}
				
				if(color_chooser_frame == null){
					color_chooser_frame = new JFrame("Color");
					color_chooser_frame.setSize(430, 370);
					color_chooser_frame.add(color_chooser);
				}
				
				color_chooser_frame.setVisible(true);
			}
    }else if(source == menuitem_SELECT_USING_DEPTH){
    	SELECT_USING_DEPTH = menuitem_SELECT_USING_DEPTH.getState();
    }else if(source == menuitem_SIMPLIFY){
    	mesh.simplify();
    	refreshList();
    }else if(source == menuitem_SUBDIVIDE){
    	mesh.subdivideEdges();
    	refreshList();
    }else if(source == menuitem_QUIT){
      RUNNING = false;
    }else{
    	//Check for group toggling
    	for(int i=0; i<menuitem_VIEW_GROUPS.size(); i++){
    		if(source == menuitem_VIEW_GROUPS.get(i)){
    			mesh.toggleGroup(menuitem_VIEW_GROUPS.get(i).getText());
    			refreshList();
    			break;
    		}
    	}
    	
    	//Check for group selections
    	for(int i=0; i<menuitem_SELECT_GROUPS.size(); i++){
    		if(source == menuitem_SELECT_GROUPS.get(i)){
    			selected_vertices = mesh.getGroupVertices(menuitem_VIEW_GROUPS.get(i).getText());
    			selected_vertex_faces = mesh.getEnclosedFaces(selected_vertices);
    			
      		if(SELECT_FOR_JOINT){
      			selected_joint = mesh.getCentroid(selected_vertices);
      			selected_components = mesh.getCutComponents(selected_vertices);

      			if(selected_components.size() > 1){
	      			selected_component_faces = new Vector<Vector<Integer>>();
	      			
	      			for(int j=0; j<selected_components.size(); j++){
	      				selected_component_faces.add(mesh.getEnclosedFaces(selected_components.get(j)));
	      			}
	      			
	      			BENDING_JOINT = true;
      			}
      		}else if(SELECT_FOR_COLOR){
      			if(!SHADED_LIGHTING_MATERIAL){		//In order to see colors enable materials during lighting
	        		SHADED_LIGHTING_MATERIAL = true; menuitem_SHADED_LIGHTING_MATERIAL.setSelected(SHADED_LIGHTING_MATERIAL);
	          	SHADED_LIGHTING_DISABLED = menuitem_SHADED_LIGHTING_DISABLED.isSelected();
	          	SHADED_LIGHTING_ENABLED = menuitem_SHADED_LIGHTING_ENABLED.isSelected();
	          	
	          	lighting = DrawOption.MATERIAL;
      			}
          	
          	//Paint the groups faces
						for(int j=0; j<selected_vertex_faces.size(); j++){
							mesh.getFace(selected_vertex_faces.get(j)).material = new Material(new MeshAuxiliary.Color(color_chooser.getColor().getRGB()));
						}
						
						//Don't need to save these and don't want to highlight them
						selected_vertices = null;
						selected_vertex_faces = null;
						
						refreshList();
      		}
      		
    			break;
    		}
    	}
    	
    	//Check for mesh selections
    	for(int i=0; i<menuitem_MESHES.size(); i++){
    		if(source == menuitem_MESHES.get(i)){
    			//Turn off all other radio buttons
    			for(int j=0; j<menuitem_MESHES.size(); j++){
			  		menuitem_MESHES.get(j).setSelected(false);
			  	}
    			
    			//Turn on the selected radio button only
    			menuitem_MESHES.get(i).setSelected(true);
    			
    			//Set the appropriate transformation    			
    			if(i == 0){
    				selected_rotation_last = rotation_last;
    				selected_transformation = transformation;
    			}else{
    				selected_rotation_last = added_rotation_last.get(i-1);
    				selected_transformation = added_transformations.get(i-1);
    			}
    			
    			break;
    		}
    	}
    }
    
    refresh(true);
  }
  
  public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) {}
  public void displayChanged(GLAutoDrawable gLDrawable, boolean modeChanged, boolean deviceChanged) {}
  public void keyReleased(KeyEvent e) {}
  public void keyTyped(KeyEvent e) {}
  public void mouseExited(MouseEvent e) {}
  public void mouseMoved(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {}
  public void mouseClicked(MouseEvent e) {}
  
  /**
   * The paintComponent function for the panel.  In addition to calling the default JPanel paintComponent,
   * this function also resizes the viewer if it has changed.
   *  @param g the graphics context to paint to
   */
  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    setSize((int)getSize().getWidth(), (int)getSize().getHeight());
  }
  
	/**
   * Threaded method used to refresh the scene.
   */
  public void run()
  {
    canvas.requestFocus();
    
    while(RUNNING){
    	if(REFRESH){
    		if(mesh instanceof AnimatedMesh){
    			((AnimatedMesh)mesh).setMesh();
    			refreshList();
    		}
    		
	      if(canvas instanceof GLCanvas){
	      	((GLCanvas)canvas).display();
	      }else if(canvas instanceof GLJPanel){
	      	((GLJPanel)canvas).display();
	      }	      
    	}
    	
    	Thread.yield();
    }
    
    System.exit(0);
  }

	/**
   * The main function used if this class is run by itself.
   *  @param args not used
   */
  public static void main(String args[])
  {
    ModelViewer mv = new ModelViewer("ModelViewer.ini", false);
    mv.AUTO_REFRESH = true;
    
    JFrame frame = new JFrame("Model Viewer");
    frame.setSize(mv.width+9, mv.height+35);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(mv);
    frame.setVisible(true);
  }
}
