package edu.ncsa.model;
import javax.swing.*;

/**
 * An applet that allows for the display and manipulation of 3D objects.
 *  @author Kenton McHenry
 */
public class ModelViewerApplet extends JApplet
{
  private ModelViewer modelviewer;
  int width = 0;
  int height = 0;
  
  /**
   * Creates a ModelViewer panel and loads a model.
   */
  public void init()
  {
    modelviewer = new ModelViewer(null, false);
    String filename = getParameter("filename");
    String path = "";
    String w = getParameter("width");
    String h = getParameter("height");
    
    if(w != null && h != null){
      width = Integer.valueOf(w);
      height = Integer.valueOf(h);
    }
    
    if(width > 0 && height > 0) modelviewer.setSize(width, height);
    
    //Load model
    try{  
    	path = getCodeBase().toString();
      
      if(filename == null){
        if(path.contains("file:")){  //Local test (eclipse executes from bin folder)
          path += "../../../Data/3D/Models/Misc/";
          filename = "dc10.obj";
        }else{                       //Internet test
          path += "misc/samples/";
          filename = "dc10.obj";
        }
      }
      
      System.out.println("URL: " + path + filename);
      
      modelviewer.load(path + filename);
    }catch(Exception e){}
    
    setSize(modelviewer.width, modelviewer.height);
    add(modelviewer);
  }
}