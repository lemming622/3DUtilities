package edu.ncsa.model;
import edu.ncsa.model.ModelBrowserAuxiliary.*;
import edu.ncsa.model.graphics.jogl.*;
import edu.ncsa.image.*;
import edu.ncsa.utility.*;
import edu.ncsa.model.MeshLoader.ProgressEvent;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/**
 * A program to conveniently view and manipulate 3D files within a directory.
 *  @author Kenton McHenry
 */
public class ModelBrowser extends JPanel implements ActionListener, TreeSelectionListener, ProgressEvent
{
  private int default_viewer_width = 750;
  private int default_viewer_height = 700;
  
  private int left_panel_width = 340;
  private int bottom_panel_height = 125;
  private int window_width;
  private int window_height;
  private int viewer_width;
  private int viewer_height;
  private int viewer_halfwidth;
  private int viewer_halfheight;
  private int viewer_smallwidth = 150;   //100
  private int viewer_smallheight = 150;  //100
  private int viewer_halfsmallwidth = viewer_smallwidth / 2;
  private int viewer_halfsmallheight = viewer_smallheight / 2;
  private double radius = 0.75;          //For displaying multiple viewers

  private Class Viewer;
  private Vector<ModelViewer> modelviewer = new Vector<ModelViewer>();
  private HTMLPanel outp = new HTMLPanel();
  private JFrame frame;
  private JSplitPane splitpane_lr;
  private JSplitPane splitpane_tb;
  private JPanel viewer_panel;
  private JPanel folder_panel;
  private JScrollPane folder_panel_sp;
  private JMenuBar menubar;
  private JMenuItem menuitem_OPEN;
  private JMenuItem menuitem_REFRESH;
  private JMenuItem menuitem_THUMB;
  private JMenuItem menuitem_QUIT;
  private JMenuItem menuitem_FILTER_ALL;
  private Vector<JCheckBoxMenuItem> menuitem_FILTER = new Vector<JCheckBoxMenuItem>();
  private JMenuItem menuitem_ABOUT;
  private JTree tree1;
    
  private String load_path = ".";     //This is the target so no "/" as last character!
  private Vector<String> files = new Vector<String>();
  private Vector<String> extensions = new Vector<String>();
  private TreeMap<String,Boolean> filtered_extensions = new TreeMap<String,Boolean>();
  private Vector<FileInfo> fileinfo = new Vector<FileInfo>();
  private LinkedList<FileInfo> selected_models = new LinkedList<FileInfo>();
  private Vector<Pair<Integer,Integer>> viewer_locations = new Vector<Pair<Integer,Integer>>();
  private FileInfo loading_file = null;
  private boolean SPLITPANE_ENABLED = false;
  
  private String metadata_path = "./";
  private String polyglot_url = "http://localhost/";
  private String convertable_list = "";
  private String converted_format = "obj";
  
  private PolyglotRequests pgr = null;
  private Vector<String> loadable_formats;  
  private Vector<String> convertable_formats = new Vector<String>();
  private Vector<String> converted_formats = new Vector<String>();
  private int folder_panel_sp_gap = 4;
  
  private boolean VIEWER_MOVED = false;
  private boolean TRANSPARENT_PANELS = false;
  private boolean WIIMOTE = false;
  private boolean REBUILD_THUMBS = false;
  
  /**
   * Class constructor.
   */
  public ModelBrowser()
  {
    super(null);
  }
  
  /**
   * Class constructor responsible for loading the INI file, loading the initial folder, and building the GUI.
   *  @param frame the frame in which this panel will be displayed in
   */
  public ModelBrowser(JFrame frame)
  {
    this.frame = frame;
    
    //Load initialization file
    try{
      loadINI(new FileInputStream("ModelBrowser.ini"));
    }catch(Exception e) {}
    
    setSize(default_viewer_width + left_panel_width, default_viewer_height + bottom_panel_height);
    setLayout(null);
        
    //Initialize polyglot connection
    pgr = new PolyglotRequests(polyglot_url, converted_format);
    
    //Setup initial viewer
    if(WIIMOTE){
	    try{
	    	new ModelViewerWii();
	    	Viewer = ModelViewerWii.class;
	    	System.out.println("WiiMote: enabled\n");
	    }catch(Throwable t){
	    	WIIMOTE = false;
	    	Viewer = ModelViewer.class;
	    	System.out.println("WiiMote: disabled\n");
	    }
    }else{
    	Viewer = ModelViewer.class;
    }
    
    try{
    	modelviewer.add((ModelViewer)Viewer.getDeclaredConstructor(String.class, int.class, int.class, boolean.class, boolean.class).newInstance("ModelViewer.ini", viewer_width, viewer_height, true, true));
    }catch(Exception e){
    	e.printStackTrace();
    }
    
    modelviewer.get(0).setSize(viewer_width, viewer_height);
    modelviewer.get(0).setLocation(0, 0);

    //Set loadable formats
    loadable_formats = modelviewer.get(0).mesh.formats;
    
    //Set convertable formats
    convertable_formats.clear();
    
    try{
	    Scanner sc = new Scanner(new File(convertable_list));
	    
	    while(sc.hasNextLine()){
	    	convertable_formats.add(sc.nextLine());
	    }
    }catch(Exception e) {e.printStackTrace();}
    
    //Set formats that must be converted first
    converted_formats.clear();
    
    for(int i=0; i<convertable_formats.size(); i++){
      if(!isLoadable(convertable_formats.get(i))){
      	converted_formats.add(convertable_formats.get(i));
      }
    }
    
    //Sort the format lists
    Collections.sort(loadable_formats);
    Collections.sort(convertable_formats);
    Collections.sort(converted_formats);
    
    //Setup canvas panel
    viewer_panel = new JPanel();
    viewer_panel.setBackground(Color.white);
    viewer_panel.setLayout(null);
    viewer_panel.setLocation(0, 0);    
    viewer_panel.setSize(viewer_width, viewer_height);
    viewer_panel.add(modelviewer.get(0));
    
    //Setup folders panel
    folder_panel = new JPanel();
    
    if(TRANSPARENT_PANELS){
    	folder_panel.setBackground(new Color(0, 0, 0, 0));
    }else{
    	folder_panel.setBackground(Color.white);
    }
    
    folder_panel.setLayout(null);
    folder_panel.setLocation(0, 0);
    
    if(TRANSPARENT_PANELS){
	    outp.setBackground(new Color(0, 0, 0, 0));
	    modelviewer.get(0).add(outp);
	    modelviewer.get(0).add(folder_panel);
	    modelviewer.get(0).setAdjustments(0.5f*left_panel_width, 0.5f*bottom_panel_height, 0.8f);
	    
	    splitpane_lr = new JSplitPane();
	    splitpane_lr.setLocation(0, 0);
	    splitpane_lr.setSize(window_width, window_height);
	    splitpane_lr.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
	    splitpane_lr.setDividerSize(0);
	    
	    add(viewer_panel);
    }else{
      splitpane_tb = new JSplitPane();  
      splitpane_tb.setBorder(new EmptyBorder(0, 0, 0, 0));
      splitpane_tb.setOrientation(JSplitPane.VERTICAL_SPLIT);
      splitpane_tb.setDividerSize(0);   //4
      splitpane_tb.setDividerLocation(viewer_height);    
      splitpane_tb.setTopComponent(viewer_panel);
      splitpane_tb.setBottomComponent(outp);
   
      splitpane_lr = new JSplitPane();
      splitpane_lr.setLocation(0, 0);
      splitpane_lr.setSize(window_width, window_height);
      splitpane_lr.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
      splitpane_lr.setDividerSize(0);   //6
      splitpane_lr.setDividerLocation(left_panel_width - 13);
      splitpane_lr.setLeftComponent(folder_panel);
      splitpane_lr.setRightComponent(splitpane_tb);
      
      add(splitpane_lr);
    }
    
    //Load folder contents
    loadFolder(load_path);
    drawFolderTree();
  }
  
  /**
   * Load an INI file and initialize this class.
   *  @param fis the file stream to load from
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
            if(key.equals("LoadPath")){
              load_path = value + "/";
            }else if(key.equals("MetaDataPath")){
              metadata_path = value + "/";
            }else if(key.equals("Polyglot")){
              polyglot_url = value + "/";
            }else if(key.equals("ConvertableList")){
              convertable_list = value;
            }else if(key.equals("TransparentPanels")){
            	TRANSPARENT_PANELS = Boolean.valueOf(value);
            }else if(key.equals("WiiMote")){
            	WIIMOTE = Boolean.valueOf(value);
            }else if(key.equals("RebuildThumbs")){
            	REBUILD_THUMBS = Boolean.valueOf(value);
            }
          }
        }
      }
      
      ins.close();
    }catch(Exception e) {}
  }
  
  /**
   * Determine if the given extension is loadable or not.
   *  @param ext the extension in question
   *  @return true if the extension is loadable
   */
  public boolean isLoadable(String ext)
  {
    for(int i=0; i<loadable_formats.size(); i++){
      if(ext.equals(loadable_formats.get(i))) return true;
    }
    
    return false;
  }
  
  /**
   * Determine if the given extension is convertable or not.
   *  @param ext the extension in question
   *  @return true if the extension is convertable
   */
  public boolean isConvertable(String ext)
  {
    for(int i=0; i<convertable_formats.size(); i++){
      if(ext.equals(convertable_formats.get(i))) return true;
    }
    
    return false;
  }
  
  /**
   * Load all loadable/convertable 3D files beneath the given folder.
   *  @param path the folder to search
   */
  public void loadFolder(String path)
  {
    String filename;
    String ext;
    int tmpi;
    
    //Load model files in this folder
    files.clear();
    
    FilenameFilter filter = new FilenameFilter(){
      public boolean accept(File dir, String name){
        return !name.startsWith(".");
      }
    };
    
    Stack<File> stk = new Stack<File>();
    stk.push(new File(path));
    
    while(!stk.empty()){
      File folder = stk.pop();
      File[] folder_files = folder.listFiles(filter);
      
      if(folder_files != null) {
        for(int i=0; i<folder_files.length; i++){
          if(folder_files[i].isDirectory()) stk.push(folder_files[i]);  //Check this directory aswell!
          
          //Check file extension
          filename = folder_files[i].getName();
          tmpi = filename.lastIndexOf('.');
          
          if(tmpi >= 0){
            ext = filename.substring(tmpi + 1);
            
            if(ext.equals("gz") || ext.equals("zip")){
              String tmp = filename.substring(0, tmpi);
              tmpi = tmp.lastIndexOf('.');              
              if(tmpi >= 0) ext = tmp.substring(tmpi + 1);
            }
          
            if(isLoadable(ext) || isConvertable(ext)){
              files.add(Utility.unixPath(folder_files[i].getAbsolutePath()));
            }
          } 
        }
      }
    }
    
    Collections.sort(files, new FileNameComparer());
    
    //Create file information list
    fileinfo.clear();
    
    for(int i=0; i<files.size(); i++){
      fileinfo.add(new FileInfo(files.get(i), converted_formats, converted_format, metadata_path));
    }
    
    //Collect all observed file extensions
    TreeSet<String> set = new TreeSet<String>();
    Iterator<String> itr;
    
    extensions.clear(); 
       
    for(int i=0; i<fileinfo.size(); i++){
    	set.add(fileinfo.get(i).extension);
    }
        
    itr = set.iterator();
    
    while(itr.hasNext()){
    	extensions.add(itr.next());
    }

    if(frame != null){
    	JMenu menu;
    	JMenu filter_menu;
    	JCheckBoxMenuItem menuitem;
  	
    	if(menubar != null){
    		frame.remove(menubar);
    	}
    	
	    menubar = new JMenuBar();
	    menu = new JMenu("Browse");
	    menuitem_OPEN = new JMenuItem("Folder"); menuitem_OPEN.addActionListener(this); menu.add(menuitem_OPEN);
	    menuitem_REFRESH = new JMenuItem("Refresh"); menuitem_REFRESH.addActionListener(this); menu.add(menuitem_REFRESH);
	    menu.addSeparator();
	    menuitem_QUIT = new JMenuItem("Quit"); menuitem_QUIT.addActionListener(this); menu.add(menuitem_QUIT);
	    menubar.add(menu);
	    
	    menu = new JMenu("Options");
	    
	    //Build filters
	    menuitem_FILTER.clear();
	    filter_menu = new JMenu("Filter");
  		menuitem_FILTER_ALL = new JMenuItem("All"); menuitem_FILTER_ALL.addActionListener(this); filter_menu.add(menuitem_FILTER_ALL);
  		filter_menu.addSeparator();
  		
	    for(int i=0; i<extensions.size(); i++){
		    menuitem = new JCheckBoxMenuItem(extensions.get(i)); menuitem.addActionListener(this); menuitem.setState(true); filter_menu.add(menuitem);
		    menuitem_FILTER.add(menuitem);
	    }
	    
	    menu.add(filter_menu);
	   
	    menuitem_THUMB = new JMenuItem("Generate Thumbs"); menuitem_THUMB.addActionListener(this); menu.add(menuitem_THUMB);
	    menubar.add(menu);
	    
	    menu = new JMenu("Help");
	    menuitem_ABOUT = new JMenuItem("About"); menuitem_ABOUT.addActionListener(this); menu.add(menuitem_ABOUT);
	    menubar.add(menu);
    
    	frame.setJMenuBar(menubar);
	    menubar.revalidate();
    }
  }
  
  /**
   * Draw the folder tree.
   */
  public void drawFolderTree()
  {
    DefaultMutableTreeNode root;
    DefaultMutableTreeNode child;
    int count = 0;
    
    if(TRANSPARENT_PANELS){
    	root = new DefaultMutableTreeNode();
    }else{
    	root = new DefaultMutableTreeNode("Models");
    }
    
    //Clear selected models
    for(int j=modelviewer.size()-1; j>0; j--){
      viewer_panel.remove(modelviewer.get(j));
      modelviewer.remove(j);
    }
    
    selected_models.clear();
    repaint();
    
    //Update the tree
    for(int i=0; i<files.size(); i++){
    	if(filtered_extensions.get(fileinfo.get(i).extension) == null){
	      child = new DefaultMutableTreeNode(fileinfo.get(i));                
	      root.add(child);
	      count++;
    	}
    }
    
    if(folder_panel_sp != null) folder_panel.remove(folder_panel_sp);
    FileInfoTreeCellRenderer fileinfo_renderer = new FileInfoTreeCellRenderer(left_panel_width-25, this);
    fileinfo_renderer.setBackground(folder_panel.getBackground());
    
    tree1 = new JTree(root);
    tree1.setBackground(folder_panel.getBackground());
    tree1.addTreeSelectionListener(this);
    tree1.setCellRenderer(fileinfo_renderer);
    tree1.setUI(new MyTreeUI()); tree1.setCellEditor(fileinfo_renderer); tree1.setEditable(true);
    ToolTipManager.sharedInstance().registerComponent(tree1);

    //Set parent of components in FileInfo
    for(int i=0; i<fileinfo.size(); i++){
      fileinfo.get(i).components_parent = tree1;
    }
    
    //if(folder_panel_sp != null) modelviewer.get(0).remove(folder_panel_sp);
    folder_panel_sp = new JScrollPane(tree1);
    folder_panel_sp.setBackground(folder_panel.getBackground());
    folder_panel_sp.setBorder(new EmptyBorder(0, 0, 0, 0));
    folder_panel_sp.setLocation(12, folder_panel_sp_gap);    
    folder_panel_sp.setSize(left_panel_width-25, window_height-2*folder_panel_sp_gap);
    folder_panel_sp.getVerticalScrollBar().setOpaque(false);
    folder_panel_sp.getVerticalScrollBar().setUI(new MyScrollBarUI());
    folder_panel.add(folder_panel_sp);
    
    tree1.revalidate();
    
  	if(frame != null) frame.setTitle("Model Browser [" + load_path + "] - " + count + " models");
  }
  
  /**
   * Handle action events from menus and buttons.
   *  @param e the action event
   */
  public void actionPerformed(ActionEvent e)
  {
    if(e.getSource() == menuitem_OPEN){
      JFileChooser fc = new JFileChooser(load_path + "..");
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

      if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
        load_path = Utility.unixPath(fc.getCurrentDirectory().getAbsolutePath()) + "/" + fc.getSelectedFile().getName() + "/";
        loadFolder(load_path);
        drawFolderTree();
      }
    }else if(e.getSource() == menuitem_REFRESH){
      loadFolder(load_path);
      drawFolderTree();
    }else if(e.getSource() == menuitem_THUMB){
    	new Thread(new Runnable(){
    		public void run()
        {
    			ImageViewer viewer = null; //new ImageViewer();
    			String thumb_image;
    			
    			System.out.println("\nCreating thumbs...");
    	
		    	for(int i=0; i<files.size(); i++){
		    		if(!fileinfo.get(i).CONVERTABLE){
		    			thumb_image = "." + fileinfo.get(i).name + ".jpg";
		    			
		    			if(!Utility.exists(metadata_path + thumb_image) || REBUILD_THUMBS){
			    			ModelViewer mv = new ModelViewer("ModelViewer.ini", 100, 100, false, false);
			    			mv.load(fileinfo.get(i).absolutename);
			    			mv.activate();
			    			mv.save(metadata_path + thumb_image);
			    			fileinfo.get(i).thumb_image = thumb_image;
			    			
			    			//Debug: view the thumbnails
			    			if(viewer != null) viewer.add(mv.grabImage(), mv.width, mv.height, true);
		    			}
            }
		    	}
		    	
		     	System.out.println("Thumbs completed!\n");
        }
      }).start();
    }else if(e.getSource() == menuitem_QUIT){
      System.exit(0);
    }else if(e.getSource() == menuitem_ABOUT){
      JFrame about = new JFrame("About");
      about.setSize(500, 360);
      about.setLayout(null);
      HTMLPanel hp = new HTMLPanel();
      hp.setLocation(0, 0);
      hp.setSize((int)about.getSize().getWidth()-7, (int)about.getSize().getHeight()-33);
      hp.setBackground(Color.white);
      hp.setHorizontalOffset(20);
      
      String tmp = "";
      tmp += "<center><h1><i>NCSA 3D Model Browser</i></h1></center><br>";
      tmp += "<u>Loadable formats</u>: <i>";
      
      for(int i=0; i<modelviewer.get(0).mesh.formats.size(); i++){
        if(i > 0) tmp += ", ";
        tmp += modelviewer.get(0).mesh.formats.get(i);
      }
      
      tmp += "</i><br><br><u>Convertable formats</u>: <i>";
      
      for(int i=0; i<converted_formats.size(); i++){
        if(i > 0) tmp += ", ";
        tmp += converted_formats.get(i);
      }
      
      tmp += "</i><br><br><br>";
      tmp += "<center><img src=file:logos/ncsa_horizontal_small.gif width=107 height=23></img></center><br>";	//143x31
      hp.setText(tmp);
      about.add(hp);
      about.setLocation(100, 100);
      about.setVisible(true);
    }else if(e.getSource() == menuitem_FILTER_ALL){
    	for(int i=0; i<menuitem_FILTER.size(); i++){
    		menuitem_FILTER.get(i).setState(!menuitem_FILTER.get(i).getState());
    		
  			if(filtered_extensions.get(extensions.get(i)) == null){
  				filtered_extensions.put(extensions.get(i), true);
  			}else{
  				filtered_extensions.remove(extensions.get(i));
  			}
    	}
    	
    	drawFolderTree();
    }else{
    	//Check for filter changes
    	for(int i=0; i<menuitem_FILTER.size(); i++){
    		if(e.getSource() == menuitem_FILTER.get(i)){
    			if(filtered_extensions.get(extensions.get(i)) == null){
    				filtered_extensions.put(extensions.get(i), true);
    			}else{
    				filtered_extensions.remove(extensions.get(i));
    			}
    			
    			drawFolderTree();
    		}
    	}
    	
    	//Check for conversion requests
      for(int i=0; i<fileinfo.size(); i++){
        if(fileinfo.get(i).CONVERTABLE){
          if(e.getSource() == fileinfo.get(i).convert_button){
            if(!fileinfo.get(i).CONVERTING && !fileinfo.get(i).CONVERTED){
              pgr.convertFile(fileinfo.get(i), metadata_path);
            }
          }
        }
      }
    }
  }

  /**
   * Handle selection events from the side JTree.
   *  @param e the tree selection event
   */
  public void valueChanged(TreeSelectionEvent e)
  {
    LinkedList<FileInfo> new_models = new LinkedList<FileInfo>();
    LinkedList<FileInfo> old_models = new LinkedList<FileInfo>();
    TreePath[] treepaths = e.getPaths();  //Paths that have been added or removed from the selection
    DefaultMutableTreeNode node;
    FileInfo fi;
    
    //Extract changes from JTree
    for(int i=0; i<treepaths.length; i++){
      if(treepaths[i].getPathCount() > 1){
        node = (DefaultMutableTreeNode)treepaths[i].getPathComponent(1);
        fi = (FileInfo)node.getUserObject();
        
        if(selected_models.contains(fi)){
          selected_models.remove(fi);
          old_models.add(fi);
        }else{
          selected_models.add(fi);
          new_models.add(fi);
        }
      }
    }
    
    //Remove old models
    for(int i=0; i<old_models.size(); i++){
      fi = old_models.get(i);      
        
      for(int j=modelviewer.size()-1; j>=0; j--){
        if(modelviewer.get(j).mesh.getMetaData("Name").equals(fi.name)){
          if(modelviewer.size() > 1){ //Don't delete last modelviewer
            viewer_panel.remove(modelviewer.get(j));
            modelviewer.remove(j);
            VIEWER_MOVED = true;
          }
          
          break;
        }
      }
    }
    
    //Load new models
    for(int i=0; i<new_models.size(); i++){  
      if(selected_models.size() > 1){
        try{
        	modelviewer.add((ModelViewer)Viewer.getDeclaredConstructor(String.class, int.class, int.class, boolean.class, boolean.class).newInstance("ModelViewer.ini", viewer_smallwidth, viewer_smallheight, true, false));
        }catch(Exception ex) {}

        moveModel(modelviewer.size()-1, viewer_smallwidth, viewer_smallheight, viewer_halfwidth-viewer_halfsmallwidth, viewer_halfheight-viewer_halfsmallheight);

        viewer_panel.add(modelviewer.get(modelviewer.size()-1));
        VIEWER_MOVED = true;
      }
      
      //Perform the actual load outside of the Swing GUI thread.
      final ProgressEvent progressCallBack = this;
      loading_file = new_models.get(i);
      loading_file.modelviewer = modelviewer.get(modelviewer.size()-1);
      
      //Prevent others from clicking until this load completes
      folder_panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      tree1.setEnabled(false);	
      
      new Thread(new Runnable(){
      	public void run()
      	{
		      if(loading_file.CONVERTABLE){
		      	if(loading_file.CONVERTED){
		      		modelviewer.get(modelviewer.size()-1).load(loading_file.metadata_path + loading_file.converted_filename);
		      	}else{
		      		//Wait for the file to be converted and loaded (performed elsewhere!)
		      		while(!loading_file.CONVERTED){
		      			Utility.pause(500);
		      		}
		      	}
		      }else{
		        modelviewer.get(modelviewer.size()-1).load(loading_file.path + loading_file.filename, progressCallBack);
		      }
		      
		      //Re-enable tree and refresh screen
		      loading_file = null;
		      tree1.setEnabled(true);
		      folder_panel.setCursor(Cursor.getDefaultCursor());
		      frame.repaint();
        }
      }).start();
    }
    
    if(TRANSPARENT_PANELS){		//Use splitpane only if mutlipe models selected
	    if(selected_models.size() > 1 && !SPLITPANE_ENABLED){
	    	remove(viewer_panel);
	    	modelviewer.get(0).remove(outp);
	    	modelviewer.get(0).remove(folder_panel);
		    modelviewer.get(0).setAdjustments(0, 0, 1);
	    	
	      tree1.setBackground(Color.white);
	    	splitpane_lr.setLeftComponent(folder_panel);
	    	splitpane_lr.setRightComponent(viewer_panel);
	      splitpane_lr.setDividerLocation(left_panel_width - 13);
	    	add(splitpane_lr);
	    	SPLITPANE_ENABLED = true;
	    	
	    	revalidate();
	    }else if(selected_models.size() == 1 && SPLITPANE_ENABLED){
	    	remove(splitpane_lr);
	    	splitpane_lr.setLeftComponent(null);
	    	splitpane_lr.setRightComponent(null);
	    	
	    	tree1.setBackground(new Color(0, 0, 0, 0));
	    	modelviewer.get(0).add(outp);
	    	modelviewer.get(0).add(folder_panel);
		    modelviewer.get(0).setAdjustments(0.5f*left_panel_width, 0.5f*bottom_panel_height, 0.8f);
	    	viewer_panel.setLocation(0, 0);
	    	viewer_panel.setSize(viewer_width, viewer_height);
	    	add(viewer_panel);
	    	SPLITPANE_ENABLED = false;
	    	
	    	revalidate();
	    }
    }
    
    repaint();
  }
  
  /**
   * Handle progress updates from loaded models.
   * @param value the current progress
   * @param total the maximum total progress that can be made
   */
  public void progressEvent(double value, double total)
  {
  	if(loading_file != null){
  		loading_file.progress_bar.setValue((int)Math.round(100*value/total));

  		if(value < total){
  			loading_file.progress_bar.setVisible(true);
  		}else{
      	loading_file.progress_bar.setVisible(false);
      }
  		
      if(loading_file.components_parent != null){
      	loading_file.components_parent.repaint();
      }
  	}
  	
  	//System.out.println("Loading: " + value + " of " + total);
  }
  
  /**
   * Move a model viewer to a new location within the panel.
   *  @param i the index of the viewer
   *  @param w the desired width
   *  @param h the desired height
   *  @param x the desired x location
   *  @param y the desired y location
   */
  public void moveModel(int i, int w, int h, int x, int y)
  {
    modelviewer.get(i).setSize(w, h);
    modelviewer.get(i).setLocation(x, y); 
  }
    
  /**
   * Adjust the size of this panel and all its sub-panels.
   *  @param w the new width
   *  @param h the new height
   */
  public void setSize(int w, int h)
  {
    if(window_width != w || window_height != h){
      super.setSize(w, h);    //Just in case the user didn't resize the window!
      window_width = w;
      window_height = h;
      
      if(TRANSPARENT_PANELS){
      	viewer_width = window_width;
      	viewer_height = window_height;
      }else{
      	viewer_width = window_width - left_panel_width;
      	viewer_height = window_height - bottom_panel_height;
      }
      
      viewer_halfwidth = viewer_width / 2;
      viewer_halfheight = viewer_height / 2;
       
      if(splitpane_lr != null) splitpane_lr.setSize(window_width, window_height);
      
      if(splitpane_tb != null){
      	splitpane_tb.setDividerLocation(viewer_height);
      	splitpane_tb.updateUI();
      }
      
      if(folder_panel != null) folder_panel.setSize(left_panel_width, window_height);
      if(folder_panel_sp != null) folder_panel_sp.setSize(left_panel_width-25, window_height-2*folder_panel_sp_gap);
      
      if(modelviewer.size() == 1){
      	modelviewer.get(0).setSize(viewer_width, viewer_height);
      	
      	if(TRANSPARENT_PANELS){
	      	if(viewer_panel != null) viewer_panel.setSize(viewer_width, viewer_height);
	
	      	if(outp != null){
		      	outp.setLocation(left_panel_width, viewer_height-bottom_panel_height);
		        outp.setSize(viewer_width, bottom_panel_height);
	      	}
      	}
      }
      
      //Reset the viewer positions for the new window size
      viewer_locations.clear();
      
      revalidate();
    }
  }
  
  /**
   * Draw this panel.
   *  @param g the graphics context to draw to.
   */
  public void paint(Graphics g)
  {
    double x, y, tmpd;
    int viewer_pseudo_halfwidth;
    
    super.paint(g);    
    setSize((int)getSize().getWidth(), (int)getSize().getHeight());
    
    //Refresh mesh information panel
    outp.setLeftOffset(40);
    outp.setText(modelviewer.get(0).mesh.getMetaDataHTML(new String[]{"Name", "File", "Type", "Vertices", "Faces", "Radius"}, true));
    
    //Set viewer locations
    if(modelviewer.size() > 1){
      if(modelviewer.size() != viewer_locations.size()){   
        viewer_locations.clear();
        viewer_pseudo_halfwidth = (window_width - left_panel_width) / 2;
        
        for(int i=0; i<modelviewer.size(); i++){
          x = Math.cos(2.0*Math.PI*(double)i/(double)modelviewer.size());
          y = Math.sin(2.0*Math.PI*(double)i/(double)modelviewer.size());
          
          //Scale to fit in panel
          x = radius*viewer_pseudo_halfwidth*x + viewer_pseudo_halfwidth;
          y = radius*viewer_halfheight*y + viewer_halfheight;
          
          viewer_locations.add(new Pair<Integer,Integer>());        
          viewer_locations.get(i).first = (int)Math.round(x);
          viewer_locations.get(i).second = (int)Math.round(y);
        }
      }
      
      for(int i=0; i<viewer_locations.size(); i++){
        moveModel(i, viewer_smallwidth, viewer_smallheight, viewer_locations.get(i).first-viewer_halfsmallwidth, viewer_locations.get(i).second-viewer_halfsmallheight);
      }
      
      //Draw connecting edges
      g.setColor(new Color(0x00dddddd));
      
      for(int i=0; i<viewer_locations.size(); i++){
        for(int j=i+1; j<viewer_locations.size(); j++){
          g.drawLine(viewer_locations.get(i).first+left_panel_width, viewer_locations.get(i).second, 
                     viewer_locations.get(j).first+left_panel_width, viewer_locations.get(j).second);
        }
      }
      
      //Draw in edge values
      if(true){
        FontMetrics fm = g.getFontMetrics();
        int ascent = fm.getMaxAscent ();
        int descent= fm.getMaxDescent ();
        int msg_width;
        String msg;
        
        g.setColor(Color.black);
        
        for(int i=0; i<viewer_locations.size(); i++){
          for(int j=0; j<viewer_locations.size(); j++){
          	if(tree1.isEnabled()){		//Synchronize with load by checking if the model selection tree is enabled yet.
          		tmpd = modelviewer.get(i).mesh.compareTo(modelviewer.get(j).mesh);
          		tmpd = ((double)Math.round(1000.0*tmpd))/1000.0;    //Set precision          		
          	}else{
          		tmpd = 0;
          	}

            msg = Double.toString(tmpd);
            msg_width = fm.stringWidth(msg);   
            
            x = (viewer_locations.get(i).first + viewer_locations.get(j).first) / 2 + left_panel_width;
            y = (viewer_locations.get(i).second + viewer_locations.get(j).second) / 2;
            g.drawString(msg, (int)Math.round(x)-msg_width/2, (int)Math.round(y)-descent/2+ascent/2);
          }
        }
      }
      
      //Need to redraw after first positioning since it will erase the drawn lines!
      if(VIEWER_MOVED){
        VIEWER_MOVED = false;
        repaint();
      }
    }else{
      moveModel(0, viewer_width, viewer_height, 0, 0);
    }
  }
  
  /**
   * The main for this program.
   *  @param args
   */
  public static void main(String args[])
  {
    JFrame frame = new JFrame();
    ModelBrowser mb = new ModelBrowser(frame);
    frame.setSize(mb.window_width, mb.window_height);
    frame.add(mb);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }
}