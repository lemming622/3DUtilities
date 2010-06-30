package edu.ncsa.model;
import edu.ncsa.utility.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

/**
 * Auxillary classes used by ModelBrowser.
 *  @author Kenton McHenry
 */
public class ModelBrowserAuxiliary
{
  /**
   * A structure to hold 3D model file information.
   */
  public static class FileInfo
  {
    public String absolutename;    //path + name + ext
    public String filename;        //name + ext
    public String path;
    public String name;
    public String extension;
    public int size;
        
    public JProgressBar progress_bar = null;
    public JButton convert_button = null;
    public Component components_parent = null;     //A reference to the component displaying the buttons (used to refresh during drawing!)
    public boolean CONVERTABLE = false;
    public boolean CONVERTING = false;
    public boolean CONVERTED = false;
    
    public String metadata_path = "./";
    public String converted_filename = "";
    public String converted_format = "obj";
    public String thumb_image = null;
    public ModelViewer_JOGL modelviewer = null;
    
    /**
     * Class constructor to initialize the file information.
     *  @param absolutename the absolute name of the file
     *  @param convertable_formats a list of the convertable file formats
     *  @param converted_format the target format to convert to
     *  @param metadata_path the directory to place the converted file
     */
    public FileInfo(String absolutename, Vector<String> convertable_formats, String converted_format, String metadata_path)
    {
      this.absolutename = absolutename;
      this.converted_format = converted_format;
      this.metadata_path = metadata_path;
      
      //Set filename
      int tmpi = absolutename.lastIndexOf('/');
      
      if(tmpi >= 0){
        filename = absolutename.substring(tmpi+1);
        path = absolutename.substring(0, tmpi+1);
      }else{
        filename = absolutename;
      }
      
      //Set name and extension
      tmpi = filename.lastIndexOf('.');
      
      if(tmpi >= 0){
      	name = filename.substring(0, tmpi);
      	extension = filename.substring(tmpi+1);
      	
      	if(extension.equals("gz") || extension.equals("zip")){
      		tmpi = name.lastIndexOf('.');
      		
      		if(tmpi >= 0){
      			extension = name.substring(tmpi+1);
      			name = name.substring(0, tmpi);
      		}
      	} 
      }
      
      progress_bar = new JProgressBar(0, 100);
      progress_bar.setValue(0);
      progress_bar.setStringPainted(true);
      progress_bar.setVisible(false);      
      
      //Determine if this is a convertable 3D model
      for(int i=0; i<convertable_formats.size(); i++){
        if(extension.equals(convertable_formats.get(i))){
          converted_filename = filename + "." + converted_format;          
          convert_button = new JButton("Convert");
          convert_button.setMargin(new Insets(0, 0, 0, 0));
          CONVERTABLE = true;
          
          if(Utility.exists(metadata_path + converted_filename)){
            CONVERTED = true;  
            convert_button.setEnabled(false);
            convert_button.setText("Converted");
          }
          
          break;
        }
      }
      
      //Obtain the file size
      File f = new File(absolutename);
      size = (int)f.length();
      
      //Check for thumbnails
      if(Utility.exists(metadata_path + "." + name + ".jpg")){
      	thumb_image = "." + name + ".jpg";
      }
    }
    
    /**
     * Convert this class to a string.
     *  @return the String version of this FileInfo structure
     */
    public String toString()
    {
      return filename;
    }
  }
  
  /**
   * A TreeCellRenderer designed for the FileInfo structure.
   */
  public static class FileInfoTreeCellRenderer implements TreeCellRenderer, TreeCellEditor
  {
    private DefaultTreeCellRenderer default_renderer = new DefaultTreeCellRenderer();
    private Icon leaf_icon = default_renderer.getLeafIcon();
    private Font font1 = UIManager.getFont("Tree.font");
    private Font font2 = font1;
    private Color selection_border_color, selection_foreground, selection_background, text_foreground, text_background;
    Border selection_border = BorderFactory.createLineBorder(new Color(0x006382bf));
    private int vertical_padding = 5;
    private int horizontal_padding = 5;
    private int left_margin = 40;
    protected LinkedList<CellEditorListener> listeners = new LinkedList<CellEditorListener>();

    private ActionListener action_listener = null;
    private int width = 0;
    private int filename_chars = 12;

    /**
     * Class constructor.
     *  @param width the width of the panel displaying the tree
     *  @param action_listener the listener for events emitted from within the FileInfo structure
     */
    public FileInfoTreeCellRenderer(int width, ActionListener action_listener)
    {
      this.width = width;
      this.action_listener = action_listener;      
      
      selection_border_color = UIManager.getColor("Tree.selectionBorderColor");
      selection_foreground = UIManager.getColor("Tree.selectionForeground");
      selection_background = UIManager.getColor("Tree.selectionBackground");
      selection_background = new Color(selection_background.getRed(), selection_background.getGreen(), selection_background.getBlue(), 200);
      text_foreground = UIManager.getColor("Tree.textForeground");
      text_background = UIManager.getColor("Tree.textBackground");
      
      font2 = font2.deriveFont(font2.getStyle() ^ Font.BOLD);
    }
    
    /**
     * Set the background color.
     *  @param c the color of the background
     */
    public void setBackground(Color c)
    {
      text_background = c;
    }
    
    /**
     * This is the function that is called by JTree when it needs to draw one of the nodes.
     *  @param tree the calling tree
     *  @param value
     *  @param selected indicates whether or not this node is currently selected
     *  @param expanded
     *  @param leaf
     *  @param row the row of this node in the tree
     *  @param hasFocus
     *  @return the drawn node
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
    {
      if(leaf && (value != null) && (value instanceof DefaultMutableTreeNode)){
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        Object userObject = node.getUserObject();      
        
        if(userObject instanceof FileInfo){
          FileInfo fileinfo = (FileInfo)userObject;
          FontMetrics fm;          
          JPanel leaf_renderer = new JPanel();
          String label_text = fileinfo.name;
          String label_size = "  (" + Utility.getBytes(fileinfo.size) + ")";          
          int string_width, string_height;
          int x = 0;
          
          if(label_text.length() > filename_chars) label_text = label_text.substring(0, filename_chars-1) + "*";
          label_text += "." + fileinfo.extension;
          
          JLabel icon = new JLabel(leaf_icon);
          icon.setLocation(x, 0);
          icon.setSize(leaf_icon.getIconWidth(), leaf_icon.getIconHeight());
          x += leaf_icon.getIconWidth() + horizontal_padding;
          
          JLabel label1 = new JLabel(label_text);
          label1.setLocation(x, 0);
          fm = leaf_renderer.getFontMetrics(font1);
          string_width = fm.stringWidth(label_text);
          string_height = fm.getHeight();
          label1.setSize(string_width, string_height);          
          label1.setFont(font1);
          x += string_width + horizontal_padding;
          
          JLabel label2 = new JLabel(label_size);
          label2.setLocation(x, 0);
          fm = leaf_renderer.getFontMetrics(font2);
          string_width = fm.stringWidth(label_size);
          string_height = fm.getHeight();
          label2.setSize(string_width, string_height);
          label2.setFont(font2);
          x += string_width + horizontal_padding;
          
          if(fileinfo.thumb_image == null){
          	leaf_renderer.setToolTipText(fileinfo.filename);
          }else{
          	leaf_renderer.setToolTipText("<html><img src=file:"+ fileinfo.metadata_path + fileinfo.thumb_image + "><br>" + fileinfo.filename + "</html>");
          }
          
          leaf_renderer.setLayout(null);
          leaf_renderer.add(icon);
          leaf_renderer.add(label1);
          leaf_renderer.add(label2);

          if(fileinfo.CONVERTABLE){
            fileinfo.convert_button.setSize(70, string_height);
            fileinfo.convert_button.setLocation(width-fileinfo.convert_button.getWidth()-horizontal_padding-left_margin, vertical_padding / 2);
            fileinfo.convert_button.addActionListener(action_listener);
            leaf_renderer.add(fileinfo.convert_button);
          }
          
          fileinfo.progress_bar.setSize(70, string_height);            
          fileinfo.progress_bar.setLocation(width-fileinfo.progress_bar.getWidth()-horizontal_padding-left_margin, vertical_padding / 2);
          leaf_renderer.add(fileinfo.progress_bar);
  
          if(selected){
            label2.setForeground(new Color(0x0070a27c));
            leaf_renderer.setForeground(selection_foreground);
            leaf_renderer.setBackground(selection_background);
            //leaf_renderer.setBorder(selection_border);
          }else{
            label2.setForeground(selection_background);
            leaf_renderer.setForeground(text_foreground);
            leaf_renderer.setBackground(text_background);
            //leaf_renderer.setBorder(null);
          }
          
          leaf_renderer.setSize(width-left_margin, string_height+vertical_padding);            
          leaf_renderer.setEnabled(tree.isEnabled());
          
          return leaf_renderer;
        }
      }
      
      return default_renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }
   
    // -------------- TreeCellEditor Methods ----------------
    /**
     * This is the function that is called by JTree when a node is modified.
     *  @param tree the calling tree
     *  @param value
     *  @param selected indicates whether or not this node is currently selected
     *  @param expanded
     *  @param leaf
     *  @param row the row of this node in the tree
     *  @return the re-drawn node
     */
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row)
    {
      return getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
    }
 
    /**
     * This method is called when editing is completed.
     *  @return the new value to be stored in the cell
     */
    public Object getCellEditorValue()
    {
      //return new FileInfo("", new String[0]);
      return null;
    }

    /**
     * This method is called when editing has stopped.  It is responisble for calling the 
     * associated listeners.
     */
    protected void fireEditingStopped()
    {
      if(listeners.size() > 0){
        ChangeEvent ce = new ChangeEvent(this);
        for(CellEditorListener l:listeners){
          l.editingStopped(ce);
        }
      }
    }
    
    public void cancelCellEditing() {}
    public boolean stopCellEditing() {return true;}
    public boolean isCellEditable(EventObject eo) {return true;}
    public boolean shouldSelectCell(EventObject eo){return true;}
    public void addCellEditorListener(CellEditorListener cel) {listeners.add(cel);}
    public void removeCellEditorListener(CellEditorListener cel) {listeners.remove(cel);}
  }
  
  /**
   * A replacement for BasicTreeUI.  The BasicTreeUI does not support multiple-selection 
   * when a custom editor is used.  This UI allows for multiple selections!
   */
  public static class MyTreeUI extends BasicTreeUI
  {
    public TreeSelectionListener[] listeners = null;
    
    /**
     * Disable the tree's listeners by removing them.
     */
    public void disableListeners()
    {
      for(int i=0; i<listeners.length; i++){
        this.tree.removeTreeSelectionListener(listeners[i]);
      }
    }
    
    /**
     * Enable the tree's listeners by adding them back to it.
     */
    public void enableListeners()
    {
      for(int i=0; i<listeners.length; i++){
        this.tree.addTreeSelectionListener(listeners[i]);
      }
    }
    
    /**
     * Call the tree's listeners.
     *  @param path the tree path
     */
    public void callListeners(TreePath path)
    {
      TreeSelectionEvent tse = new TreeSelectionEvent(this, path, true, null, path);

      for(int i=0; i<listeners.length; i++){
        listeners[i].valueChanged(tse);
      }
    }
    
    /**
     * This method is called when a node is edited/selected.
     * The BasicTreeUI startEditing(..) method doesn't handle multiple selections well. 
     * This circumvents that when Ctrl or Shift is held down by first saving the current selection, 
     * and then restoring it after calling super.startEditing(..).
     *  @param path the path within the tree to this node
     *  @param event the mouse event responsible
     *  @return true if ...
     */
    protected boolean startEditing(TreePath path, MouseEvent event)
    {
      Vector<TreePath> selected_paths = null;

      if(listeners == null){
        listeners = this.tree.getTreeSelectionListeners();
      }
      
      if(tree.getSelectionCount()>0 && event.isControlDown()){
        selected_paths = new Vector<TreePath>(tree.getSelectionCount() + 1);
        
        for(int i=0; i<tree.getSelectionPaths().length; i++){
          selected_paths.add(tree.getSelectionPaths()[i]);
        }
 
        if(tree.isPathSelected(path)){
          selected_paths.remove(path);
        }else{
          selected_paths.add(path);
        }
        
        callListeners(path);
      }else if(tree.getSelectionCount()>0 && event.isShiftDown()){
        int anchor = tree.getAnchorSelectionPath() == null ? -1 : tree.getRowForPath(tree.getAnchorSelectionPath());
        int row1 = tree.getRowForPath(path);
        int row0 = anchor < 0 ? row1 : anchor;

        if(row0 > row1){
          int temp = row1;
          row1 = row0;
          row0 = temp;
        }
 
        selected_paths = new Vector<TreePath>(row1 - row0 + 1);
 
        for(int row=row0; row<=row1; row++){
          selected_paths.add(tree.getPathForRow(row));
          if(row != anchor) callListeners(tree.getPathForRow(row));
        }
      }else{
        //Remove old paths
        for(int i=0; i<tree.getSelectionCount(); i++){
          callListeners(tree.getSelectionPaths()[i]);
        }

        selected_paths = new Vector<TreePath>(1);
        selected_paths.add(path);
        callListeners(path);
      }
      
      disableListeners();
      
      boolean val = super.startEditing(path, event);
 
      if(selected_paths != null){
        tree.setSelectionPaths(selected_paths.toArray(new TreePath[0]));
      }
      
      enableListeners();
 
      return val;
    }
  }
  
  /**
   * A replacement for BasicScrollBarUI.
   */
  public static class MyScrollBarUI extends BasicScrollBarUI
  {
    public static int size = 15;    //The preferred the width and height of the arrowbuttons
    public static int alpha = 180;
    
    /**
     * A replacement for BasicArrowButton that returns the set size.
     */
    private class MyArrowButton extends BasicArrowButton
    {
      public MyArrowButton(int orientation) {super(orientation);}
      public Dimension getPreferredSize() {return new Dimension(size, size);}
    }
      
    /**
     * Get the preferred size of this component.
     *  @param c
     *  @return the size
     */
    public Dimension getPreferredSize(JComponent c)
    {
      return (scrollbar.getOrientation()==JScrollBar.VERTICAL) ?  new Dimension(size, 48) : new Dimension(48, size);
    }
    
    /**
     * Draw the slider.
     *  @param g the graphics context to draw to
     *  @param c
     *  @param bounds the bounds of this component
     */
    public void paintThumb(Graphics g, JComponent c, Rectangle bounds)
    {
      Graphics2D g2 = (Graphics2D)g;
      GradientPaint gradientColour = new GradientPaint(0, 1, new Color(255, 255, 255, alpha), 15, 1, new Color(0xdd, 0xdd, 0xdd, alpha));

      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(gradientColour);
      g2.fill(new RoundRectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 5, 5));

      //Draw border
      if(true){
        g2.setPaint(new Color(0x00dddddd));
        g2.draw(new RoundRectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth()-2, bounds.getHeight()-1, 5, 5));
      }
      
      //Draw bars
      if(true){
        //Black bar one, two, and three    
        g2.setPaint(Color.black);
        g2.draw(new Line2D.Double(bounds.getX()+5, bounds.getCenterY()-2, bounds.getWidth()-5, bounds.getCenterY()-2));
        g2.draw(new Line2D.Double(bounds.getX()+5, bounds.getCenterY(), bounds.getWidth()-5, bounds.getCenterY()));
        g2.draw(new Line2D.Double(bounds.getX()+5, bounds.getCenterY()+2, bounds.getWidth()-5, bounds.getCenterY()+2));
        
        //White bar one, two, and three
        g2.setPaint(Color.white);      
        g2.draw(new Line2D.Double(bounds.getX()+4, bounds.getCenterY()-1, bounds.getWidth()-6, bounds.getCenterY()-1));
        g2.draw(new Line2D.Double(bounds.getX()+4, bounds.getCenterY()+1, bounds.getWidth()-6, bounds.getCenterY()+1));
        g2.draw(new Line2D.Double(bounds.getX()+4, bounds.getCenterY()+3, bounds.getWidth()-6, bounds.getCenterY()+3));
      }
    }

    /**
     * Draw the track.
     *  @param g the graphics context to draw to
     *  @param c
     *  @param bounds the bounds of this component
     */
    protected void paintTrack(Graphics g, JComponent c, Rectangle bounds)
    {
      Graphics2D g2 = (Graphics2D)g;
      GradientPaint gradientColour = new GradientPaint(0, 1, Color.lightGray, 15, 1, Color.white);

      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(new Color(0xfa, 0xfa, 0xfa, alpha));
      g2.fill(new RoundRectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0, 0));
  
      //Draw border
      if(false){
        g2.setPaint(Color.black);
        g2.draw(new RoundRectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth()-2, bounds.getHeight()-1, 0, 0));
      }
    }
    
    public static ComponentUI createUI(JComponent c) {return new MyScrollBarUI();}  
    public JButton createDecreaseButton(int orientation) {return new MyArrowButton(orientation);}
    public JButton createIncreaseButton(int orientation) {return new MyArrowButton(orientation);}
  }
  
  /**
   * An interface by which to issue batch conversion requests to a Polyglot server.
   * Note: this version is specialized to update the status bars in modelbrowser's fileinfo structures.
   */
  public static class PolyglotRequests implements Runnable
  {
    private String url = "http://localhost/";
    private String format = "obj";
    
    private String username = "username";               //User name for protected upload targets
    private String password = "password";               //Password for protected upload targets
    private String metadata = null;                     //Metadata on the documents such as keywords etc.
    
    private JProgressBar progress_bar = null;           //The current progress bar
    private Component progress_bar_parent = null;       //A reference to the parent of the progress bar
    private Color progress_default_foreground = (new JProgressBar()).getForeground();
    private Color button_default_background = (new JButton()).getBackground();
    
    private Queue<Pair<FileInfo,String>> requests = new LinkedList<Pair<FileInfo,String>>();
    private Object request_lock = new Object();
    private boolean RUNNING = true;
    
    /**
     * Class constructor.
     *  @param url the URL of the Polyglot server
     *  @param format the target format for the requested conversions
     */
    public PolyglotRequests(String url, String format)
    {
      this.url = url;
      this.format = format;
      
      Thread t = new Thread(this);
      t.start();
    }
    
    /**
     * A helper function to set the progress bars within the FileInfo structures.
     *  @param value the current progress value to display
     *  @param color the color of the bar
     */
    private void setProgress(int value, Color color)
    {
      if(progress_bar != null){
        if(value < 0){
          progress_bar.setValue(100);
          //progress_bar.setStringPainted(false);
        }else{
          //progress_bar.setStringPainted(true);        
          progress_bar.setValue(value);
        }
        
        if(color != null){
          progress_bar.setForeground(color);
        }else{
          progress_bar.setForeground(progress_default_foreground);
        }
        
        if(progress_bar_parent != null) progress_bar_parent.repaint();
      }
    }
    
    /**
     * Upload a file to the Polyglot server for conversion.
     *  @param f the file to upload
     */
    private void uploadFile(File f)
    {
      Vector<File> tmpv = new Vector<File>();
      tmpv.add(f);
      uploadFiles(tmpv);
    }
    
    /**
     * Upload multiple files to the Polyglot server for conversion.
     *  @param files the vector of files to upload
     */
    private void uploadFiles(Vector<File> files)
    {
      String upload_url = url + "polyglot/bin/upload.php";    
      HttpURLConnection.setFollowRedirects(false);
      HttpURLConnection conn = null;
      String authHash = null; //Authentication hash
      
      if(username != null && password != null){
        String s = username + ":" + password;                
        authHash = new sun.misc.BASE64Encoder().encode(s.getBytes());   
      }
          
      //For each file, make an upload request
      for(int i=0; i<files.size(); i++){
        if(Thread.currentThread().isInterrupted()) continue;    //Check for interrupt
        File f = files.get(i);                
        String fileName = f.getName();
        int fileSize = (int) Math.floor((double)f.length()/1024);
        
        System.out.print("  Uploading: " + f.getName() + " (" + fileSize + " KB) ...");
        
        try{
          conn = (HttpURLConnection)new URL(upload_url).openConnection();  //Create reusable connection
          
          if(authHash != null){                                     //Authorize connection if necessary
            conn.setDoInput(true);
            conn.setRequestProperty("Authorization", "Basic " + authHash);
            conn.connect();
            conn.disconnect();
          }
          
          String boundary = "boundary220394209402349823";           //Boundary string
          String tail = "\r\n--" + boundary + "--\r\n";             //Tail string            
          String metadataPart = "--" + boundary + "\r\nContent-Disposition: form-data; name=\"metadata\"\r\n\r\n" + metadata + "\r\n";          

          conn.setRequestMethod("POST");
          conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
          conn.setDoOutput(true);
         
          //Create file data header
          String fileHeader1 = "--" + boundary + "\r\n"
            + "Content-Disposition: form-data; name=\"uploadfile\"; filename=\"" + fileName + "\"\r\n"
            + "Content-Type: application/octet-stream\r\n"
            + "Content-Transfer-Encoding: binary\r\n";              
          
          long fileLength  = f.length() + tail.length();
          String fileHeader2 = "Content-length: " + fileLength + "\r\n";
          String fileHeader  = fileHeader1 + fileHeader2 + "\r\n";
          String stringData = metadataPart + fileHeader;          //Non-binary part of message
          
          long requestLength = stringData.length() + fileLength ;
          conn.setRequestProperty("Content-length", "" + requestLength);
          conn.setFixedLengthStreamingMode((int)requestLength);   //Prevent buffering so that the progress bar actually displays the upload progress
          conn.connect();             
          
          DataOutputStream outs = new DataOutputStream(conn.getOutputStream());
          outs.writeBytes(stringData);                             //Write header                      
          outs.flush();
          
          //Write data
          BufferedInputStream ins1 = new BufferedInputStream(new FileInputStream(f));
          byte b[] = new byte[1024];
          int bytes_read = 0;
          int tmpi = 0;
          
          while((tmpi=ins1.read(b)) != -1){
            outs.write(b, 0, tmpi); 
            outs.flush();
            
            bytes_read += tmpi;
            setProgress((int)Math.round(100.0*((double)bytes_read/(double)f.length())), null);
          }
          
          outs.writeBytes(tail);
          outs.flush();
          outs.close();
          
          //Get server response
          if(false){  
            BufferedReader ins2 = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            
            while((line = ins2.readLine()) != null){
              System.out.println(line);
            }
          }
          
          //If we got a 401 (unauthorized), we can't get that data. We will get an IOException. 
          // This makes no sense since a 401 does not consitute an IOException, 
          // it just says we need to provide the username again.
          try{
            if(false){
              System.out.println("conn.getResponseCode(): " + conn.getResponseCode());
              System.out.println("conn.getResponseMessage(): " + conn.getResponseMessage());
            }else{
              System.out.println("  [" + conn.getResponseMessage() + "]");
            }
          }catch(IOException ioe){
            System.out.println(ioe.getMessage());
          }
        }catch(Exception e){
          e.printStackTrace();
        }finally{       //Once we are done we want to make sure to disconnect from the server.
          if(conn != null) conn.disconnect();
        }
      }
    }
    
    /**
     * Commit the files uploaded thus far (i.e. notify the server that it may begin converting).
     *  @return the ID assigned to the folder of uploaded files in the servers downloads directory
     */
    private String commitJob()
    { 
      HttpURLConnection conn = null;
      String commit_url = url + "polyglot/bin/commit_job.php?format=" + format;
      String folder = "";
      
      try{
        conn = (HttpURLConnection)new URL(commit_url).openConnection();
        conn.connect();
        conn.getResponseCode();  //Do something that blocks until response is ready

        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        folder = rd.readLine();
        
        conn.disconnect();
      }catch(Exception e){
        e.printStackTrace();
      }finally{
        if(conn != null) conn.disconnect();
      }
      
      return folder;
    }
    
    /**
     * Attempt to download a file from the web.
     *  @param folder_url the URL of the folder where the file resides
     *  @param source_filename the file to download
     *  @param target_filename the absolute name of the location to store the downloaded file
     *  @return true if the file existed
     */
    private boolean downloadFileAux(String folder_url, String source_filename, String target_filename)
    {
      HttpURLConnection.setFollowRedirects(false);
      HttpURLConnection conn = null;
      Vector<Integer> data = new Vector<Integer>();
      boolean SUCCESS = false;
      int tmpi;
      
      try{
        //Read data from url
        conn = (HttpURLConnection)new URL(folder_url + Utility.urlEncode(source_filename)).openConnection();
        conn.connect();
        
        DataInputStream ins = new DataInputStream(new BufferedInputStream(conn.getInputStream()));
        
        while((tmpi=ins.read()) != -1){
          data.add(tmpi);
        }
        
        conn.disconnect();
        
        //Write data to file
        DataOutputStream outs = new DataOutputStream(new FileOutputStream(target_filename));
        
        for(int i=0; i<data.size(); i++){
          outs.write(data.get(i));
          setProgress((int)Math.round(100.0*((double)i/(double)data.size())), null);
        }
        
        outs.close();
        SUCCESS = true;
      }catch(FileNotFoundException e){
      }catch(Exception e){
        e.printStackTrace();
      }finally{
        if(conn != null) conn.disconnect();
      }
      
      return SUCCESS;
    }
    
    /**
     * Poll for the existence of a file for specified amount of time.  If it is found download it.
     *  @param folder_url the URL of the folder where the file is located
     *  @param source_filename the name of the file to download
     *  @param target_filename the absolute name of the location to store the downloaded file
     *  @return true if the file was succesfully downloaded
     */
    private boolean downloadFile(String folder_url, String source_filename, String target_filename)
    {
      int max_time = 60;  //In seconds
      int max_attempts = 2 * max_time;
      int attempts = 0;
      
      System.out.print("  Downloading: " + source_filename + " ...");

      while(!downloadFileAux(folder_url, source_filename, target_filename)){
        attempts++;
        
        if(attempts >= max_attempts){
          break;
        }else{
          //System.out.print(".");        
          Utility.pause(500);
        }
      }
          
      if(attempts < max_attempts){
        System.out.println("  [OK]");
        return true;
      }else{
        return false;
      }
    }
    
    /**
     * Issue a request to convert a file.  If a job is currently being performed on the Polyglot server
     * then the request will be cached until the Polyglot completes the previous job.  In the meantime however
     * control is returned to the calling function.  
     *  @param fi the file to convert
     *  @param target_path the path to store the converted files
     */
    public void convertFile(FileInfo fi, String target_path)
    {
      fi.CONVERTING = true;
      fi.convert_button.setVisible(false);
      fi.progress_bar.setVisible(true);
          
      //Cache request until polyglot is ready
      Pair<FileInfo,String> p = new Pair<FileInfo,String>();
      p.first = fi;
      p.second = target_path;
      
      synchronized(request_lock){
        requests.add(p);
      }
    }
    
    /**
     * Submit a job to the Polyglot server and download the results.
     *  @param job_info a vector of conversion jobs
     */
    private void submitJob(Vector<Pair<FileInfo,String>> job_info)
    {
      FileInfo fi;
      String target_path;
      String filename;
      String folder;
      
      //Upload files
      for(int i=0; i<job_info.size(); i++){
        fi = job_info.get(i).first;
        target_path = job_info.get(i).second;
        filename = fi.absolutename;
        this.progress_bar = fi.progress_bar;
        this.progress_bar_parent = fi.components_parent;
      
        System.out.println("\n[Polyglot]");
        uploadFile(new File(filename));
        setProgress(-1, new Color(0x00ff9595));
      }
      
      //Commit job
      folder = commitJob();
      
      for(int i=0; i<job_info.size(); i++){
        fi = job_info.get(i).first;
        this.progress_bar = fi.progress_bar;
        this.progress_bar_parent = fi.components_parent;
        setProgress(-1, new Color(0x0096b768));
      }
      
      //Download files (TODO: download in order completed!)
      for(int i=0; i<job_info.size(); i++){
        fi = job_info.get(i).first;
        target_path = job_info.get(i).second;
        this.progress_bar = fi.progress_bar;
        this.progress_bar_parent = fi.components_parent;

        if(downloadFile(url + "polyglot/downloads/" + folder + "/", fi.name + "." + format, target_path + fi.converted_filename)){
          fi.CONVERTED = true;
          fi.progress_bar.setVisible(false);
          fi.convert_button.setEnabled(false);
          fi.convert_button.setBackground(button_default_background);
          fi.convert_button.setText("Converted");
          fi.convert_button.setVisible(true);
    
          if(fi.modelviewer != null){
            fi.modelviewer.load(fi.metadata_path + fi.converted_filename);
            fi.modelviewer.getRootPane().repaint();
          }
        }else{
          fi.progress_bar.setVisible(false);
          fi.convert_button.setBackground(new Color(0x00fffeb7));
          fi.convert_button.setText("Retry");
          fi.convert_button.setVisible(true);
        }
      
        System.out.println();
        if(progress_bar_parent != null) progress_bar_parent.repaint();
        fi.CONVERTING = false;
      }
    }
    
    /**
     * The main thread that is started when this class is instantiated.  If requests are pending
     * it will issue the request to the Polyglot server.  If not will wait for a request.
     */
    public void run()
    {
      Vector<Pair<FileInfo,String>> job_info = new Vector<Pair<FileInfo,String>>();
      
      while(RUNNING){
        synchronized(request_lock){
          while(!requests.isEmpty()){
            job_info.add(requests.remove());
          }
        }
          
        if(!job_info.isEmpty()){
          submitJob(job_info);
          job_info.clear();
        }else{      
          Utility.pause(500);
        }
      }
    }
  }
}