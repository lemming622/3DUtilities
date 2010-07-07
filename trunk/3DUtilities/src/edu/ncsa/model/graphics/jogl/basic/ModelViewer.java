package edu.ncsa.model.graphics.jogl.basic;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.model.MeshAuxiliary.Point;
import edu.ncsa.utility.*;
import edu.ncsa.model.MeshLoader.ProgressEvent;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.media.opengl.*;

/**
 * A panel that allows for the display and manipulation of 3D objects.
 * @author Kenton McHenry
 */
public class ModelViewer extends JPanel implements GLEventListener, MouseListener, MouseMotionListener, MouseWheelListener, ActionListener
{
  public Mesh mesh = new Mesh();
  public RigidTransformation transformation = new RigidTransformation();
  private int list_id = 0;
  private Timer timer = new Timer(10, this);
  
  private float[] light0_position = {0.0f, 0.0f, 1.0f, 0.0f};
  private float[] light1_position = {0.0f, 0.0f, -1.0f, 0.0f};
  private float[] light_diff = {0.7f, 0.7f, 0.7f, 1.0f};    
  private float[] model_ambient = {0.5f, 0.5f, 0.5f, 1.0f}; 
  
	public String load_path = "./";
  private boolean LOAD_DEFAULT = true;

  public int width = 600;                //Window width
  public int height = 600;               //Window height
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
  private JMenuItem menuitem_QUIT;
  
  public ModelViewer() {}
  
  /**
   * Class constructor specifying the INI file to load.
   * @param filename INI file name containing initialization values
   * @param DISABLE_HEAVYWEIGHT disable heavy-weight GLCanvas (sacrificing performance for functionality)
   */
  public ModelViewer(String filename, boolean DISABLE_HEAVYWEIGHT)
  {
    this(filename, 0, 0, DISABLE_HEAVYWEIGHT, true);
  }
  
  /**
   * Class constructor specifying INI file, initial dimensions and whether or not
   * to load the default model from the INI file or not.  The construct also builds the pop
   * up menu and starts a thread used to refresh the scene.
   * @param filename INI file name containing initialization values
   * @param w width of viewer
   * @param h height of viewer
   * @param DISABLE_HEAVYWEIGHT disable heavy-weight GLCanvas (sacrificing performance for functionality)
   * @param LOAD_DEFAULT if false the viewer will not load the default model from the INI file
   */
  public ModelViewer(String filename, int w, int h, boolean DISABLE_HEAVYWEIGHT, boolean LOAD_DEFAULT)
  {
    if(w > 0 && h > 0){
      width = w;
      height = h;
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
    setSize(width, height);
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
    add(canvas);
    
    setPopupMenu();
    start();
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
	 * Set the mesh structure.
	 * @param mesh the mesh to display
	 */
	public synchronized void setMesh(Mesh mesh)
	{
		this.mesh = mesh;
	  mesh.center(0.8f*((width<height)?width:height)/2.0f);
	  refreshList();
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
	 * Overridden update to avoid clearing.
	 * @param g the graphics context
	 */
	public void update(Graphics g)
	{
		paint(g);
	}

	/**
	 * Start rendering.
	 */
	public void start()
	{
		timer.start();
	}

	/**
	 * Stop rendering.
	 */
	public void stop()
	{
		timer.stop();
	}
	
	/**
   * Initialize the OpenGL canvas.
   * @param drawable the OpenGL context to render to
   */
  public void init(GLAutoDrawable drawable)
  {
    GL gl = drawable.getGL();
    gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
  	gl.glColor3f(0.5f, 0.5f, 0.5f);

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
  	gl.glEnable(GL.GL_LIGHTING);

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
   * @param drawable the OpenGL context to render to
   */
  public synchronized void display(GLAutoDrawable drawable)
  {
    GL gl = drawable.getGL();
    gl.glClear(GL.GL_COLOR_BUFFER_BIT);
    gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
    
    gl.glPushMatrix();
    gl.glTranslated(transformation.tx, transformation.ty, transformation.tz);
    gl.glRotated(transformation.rx, 1, 0, 0);   
    gl.glRotated(transformation.ry, 0, 1, 0);
    gl.glRotated(transformation.rz, 0, 0, 1);
    gl.glScaled(transformation.scl, transformation.scl, transformation.scl);
    
    if(list_id == 0){   //Rebuild the list
      list_id = gl.glGenLists(1);
      
      gl.glNewList(list_id, GL.GL_COMPILE);
      draw(gl, mesh);
      gl.glEndList();
    }
    
    gl.glCallList(list_id);           
    gl.glPopMatrix();
    gl.glFlush();
  }
  
	/**
   * Rebuild the display list.
   */
  private void refreshList()
  {
    list_id = 0;
  }

	/**
   * Draw the given model.
   * @param gl the OpenGL context to render to
   * @param mesh the mesh to draw
   */
  private void draw(GL gl, Mesh mesh)
  {
  	Vector<Point> vertices = mesh.getVertices();
  	Vector<Face> faces = mesh.getFaces();
  	
    Point norm;
    
    for(int i=0; i<faces.size(); i++){
      if(faces.get(i).VISIBLE && faces.get(i).v.length >= 3){
        gl.glBegin(GL.GL_POLYGON);
        norm = faces.get(i).normal;
        gl.glNormal3f((float)norm.x, (float)norm.y, (float)norm.z);

        for(int j=0; j<faces.get(i).v.length; j++){
          gl.glVertex3f((float)vertices.get(faces.get(i).v[j]).x, (float)vertices.get(faces.get(i).v[j]).y, (float)vertices.get(faces.get(i).v[j]).z);
        }
        
        gl.glEnd();
      }
    }
  }

	/**
   * Listener for mouse pressed events.
   * @param e the mouse event
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
    
    if(clicked_button == 3){
      popup_menu.show(e.getComponent(), e.getX(), e.getY());
    }
  }
  
  /**
   * Listener for mouse dragged events.
   * @param e the mouse event
   */
  public void mouseDragged(MouseEvent e)
  {
    if(clicked_button == 1){
      if(e.isShiftDown()){
      	transformation.tx += e.getX()-last_x;
      	transformation.ty += last_y-e.getY();
      }else{
      	transformation.rx = (transformation.rx + (e.getY()-last_y)) % 360;
      	transformation.ry = (transformation.ry + (e.getX()-last_x)) % 360;
      }
    }
    
    last_x = e.getX();
    last_y = e.getY();
  }
  
  /** 
   * Listener for mouse wheel events.
   * @param e mouse wheel event
   */
  public void mouseWheelMoved(MouseWheelEvent e)
  {
    if(e.getWheelRotation() < 0){
    	transformation.scl *= 1.04;
    }else{
    	transformation.scl /= 1.04;
    }
  }
  
  /**
   * Listener for action events.
   * @param e the action event
   */
  public void actionPerformed(ActionEvent e)
  {
  	if(e.getSource() instanceof JMenuItem){
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
  	}else{
  		paintImmediately(0, 0, getWidth(), getHeight());
  	}
  }
  
  public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) {}
  public void displayChanged(GLAutoDrawable gLDrawable, boolean modeChanged, boolean deviceChanged) {}
  public void mouseReleased(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
  public void mouseMoved(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {}
  public void mouseClicked(MouseEvent e) {}
  
  /**
   * The paintComponent function for the panel.
   * @param g the graphics context to paint to
   */
  public void paintComponent(Graphics g)
  {
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

	/**
   * The main function used if this class is run by itself.
   * @param args not used
   */
  public static void main(String args[])
  {
    ModelViewer mv = new ModelViewer("ModelViewer.ini", false);
    
    JFrame frame = new JFrame("Model Viewer");
    frame.setSize(mv.width+9, mv.height+35);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(mv);
    frame.setVisible(true);
  }
}
