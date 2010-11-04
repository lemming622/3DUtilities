package edu.ncsa.model.graphics.lwjgl.basic;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.model.MeshAuxiliary.Point;
import edu.ncsa.model.MeshLoader.ProgressEvent;
import edu.ncsa.model.graphics.*;
import edu.ncsa.utility.*;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import org.lwjgl.opengl.*;

/**
 * A panel that allows for the display and manipulation of 3D objects.
 * @author Kenton McHenry
 */
public class ModelViewer extends AbstractModelViewer implements ActionListener, MouseListener, MouseMotionListener, MouseWheelListener
{
  public Mesh mesh = new Mesh();
  private RigidTransformation transformation = new RigidTransformation();
  private int list_id = 0;

  private Timer timer = new Timer(10, this);
  private Pbuffer pbuffer;
  private BufferedImage buffered_image;
  private int[] image;
  private IntBuffer int_buffer;
  private boolean INITIALIZED = false;
    
  private String load_path = "./";
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
   * Class Constructor.
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
    
    setBackground(java.awt.Color.white);
    this.setBorder(null);
    setSize(width, height);
    setLayout(null);
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
  
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
	 * Set the mesh structure.
	 * @param mesh the mesh to display
	 */
	public void setMesh(Mesh mesh)
	{
		this.mesh = mesh;
	  mesh.center(0.8f*((width<height)?width:height)/2.0f);
	  refreshList();
	}

	/**
	 * Convert an array of floats to a FloatBuffer.
	 * @param array the array to convert
	 * @return the resulting FloatBuffer
	 */
	public static FloatBuffer toFloatBuffer(float[] array)
	{
		FloatBuffer fb = ByteBuffer.allocateDirect(array.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	 	fb.put(array).flip();
	 	
	 	return fb;
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
   */
  public void init()
  {
    FloatBuffer light0_position = toFloatBuffer(new float[]{0.0f, 0.0f, 1.0f, 0.0f});
    FloatBuffer light1_position = toFloatBuffer(new float[]{0.0f, 0.0f, -1.0f, 0.0f});
    FloatBuffer light_diff = toFloatBuffer(new float[]{0.7f, 0.7f, 0.7f, 1.0f});
    FloatBuffer model_ambient = toFloatBuffer(new float[]{0.5f, 0.5f, 0.5f, 1.0f});
    
    GL11.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
    GL11.glColor3f(0.5f, 0.5f, 0.5f);
    
    GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, light0_position);
    GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, light_diff);
    GL11.glEnable(GL11.GL_LIGHT0);
    
    GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, light1_position);
    GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, light_diff);
    GL11.glEnable(GL11.GL_LIGHT1);
    
    GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, model_ambient);
    GL11.glLightModeli(GL11.GL_LIGHT_MODEL_TWO_SIDE, GL11.GL_TRUE);
    
    GL11.glEnable(GL11.GL_NORMALIZE);    
    GL11.glEnable(GL11.GL_DEPTH_TEST);
    GL11.glEnable(GL11.GL_LIGHTING);
    
    GL11.glMatrixMode(GL11.GL_PROJECTION);
    GL11.glLoadIdentity();
    GL11.glOrtho(vleft, vright, vbottom, vtop, vnear, vfar);
    GL11.glScalef(1, -1, 1);
    GL11.glMatrixMode(GL11.GL_MODELVIEW);
    GL11.glLoadIdentity();
  }
  
	/**
	 * Render the scene.
	 */
	private void display()
	{
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
    
    GL11.glPushMatrix();
    GL11.glTranslated(transformation.tx, transformation.ty, transformation.tz);
    GL11.glRotatef((float)transformation.rx, 1, 0, 0);   
    GL11.glRotatef((float)transformation.ry, 0, 1, 0);
    GL11.glRotatef((float)transformation.rz, 0, 0, 1);
    GL11.glScaled(transformation.scl, transformation.scl, transformation.scl);

    if(list_id == 0){   //Rebuild the list
      list_id = GL11.glGenLists(1);
      
      GL11.glNewList(list_id, GL11.GL_COMPILE);
      draw(mesh);
      GL11.glEndList();
    }
    
    GL11.glCallList(list_id); 
		GL11.glPopMatrix();
		GL11.glFlush();
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
   * @param mesh the mesh to draw
   */
  private void draw(Mesh mesh)
  {
  	Vector<Point> vertices = mesh.getVertices();
  	Vector<Face> faces = mesh.getFaces();
  	
    Point norm;
    
    for(int i=0; i<faces.size(); i++){
      if(faces.get(i).VISIBLE && faces.get(i).v.length >= 3){																														//Not textured
    		GL11.glBegin(GL11.GL_POLYGON);
        norm = faces.get(i).normal;
        GL11.glNormal3f((float)norm.x, (float)norm.y, (float)norm.z);

        for(int j=0; j<faces.get(i).v.length; j++){
        	GL11.glVertex3f((float)vertices.get(faces.get(i).v[j]).x, (float)vertices.get(faces.get(i).v[j]).y, (float)vertices.get(faces.get(i).v[j]).z);
        }
        
        GL11.glEnd();
      }
    }
  }

	/**
	 * Paint this JPanel.
	 * @param g the graphics context
	 */
	public void paintComponent(Graphics g)
	{
		if(!INITIALIZED){
			try{
				pbuffer = new Pbuffer(width, height, new PixelFormat(32, 0, 0, 0, 0), null, null);
				pbuffer.makeCurrent();
			}catch(Exception e){
				e.printStackTrace();
			}
	
			buffered_image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			image = ((DataBufferInt)buffered_image.getRaster().getDataBuffer()).getData();
			int_buffer = ByteBuffer.allocateDirect(width*height*4).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
	
			init();
			
			INITIALIZED = true;
			start();
		}else{
			display();
	
			//Capture currently rendered frame
			GL11.glReadPixels(0, 0, width, height, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, int_buffer);
			int_buffer.clear();
	
			for(int x=height-1; x>=0; x--){
				int_buffer.get(image, x*width, width);
			}
	
			int_buffer.flip();
			g.drawImage(buffered_image, 0, 0, null);
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

	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	
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