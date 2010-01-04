package edu.ncsa.model.loaders;
import edu.ncsa.model.Mesh;
import edu.ncsa.model.Utility;
import edu.ncsa.model.loaders.j3d.*;
import com.sun.j3d.loaders.*;
import org.web3d.j3d.loaders.*;

/**
 * A mesh file loader for *.wrl files.
 *  @author Kenton McHenry
 */
public class MeshLoaderJ3D_WRL extends MeshLoaderJ3D
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "wrl";
	}
	
  /**
   * Load a 3D model via an external loader.
   *  @param filename the file to load
   *  @return the loaded mesh
   */
  public Mesh load(String filename)
  {
  	String temp_path = "";
    String path = filename.substring(0, filename.lastIndexOf('/')+1);
    String file = filename.substring(filename.lastIndexOf('/')+1);
    
    try{
      if(!Utility.exists(temp_path + "." + file)) collapseWRL(path, file, temp_path);
      Loader loader = new VRML97Loader(VRML97Loader.LOAD_ALL & ~VRML97Loader.LOAD_BEHAVIOR_NODES);
      System.out.println();
      Scene scene = loader.load(temp_path + "." + file);

      Mesh mesh = loadScene(scene);
      mesh.addFileMetaData(filename);
      mesh.initialize();
    
      return mesh;
    }catch(Exception e){
      e.printStackTrace();
      return null;
    }
  }
	
	public boolean save(String filename, Mesh mesh) {return false;}
	
  /**
   * Remove VRML inlines by pasting their contents directly in the file.
   *  @param path the path to the file
   *  @param file the VRML file to collapse
   *  @param temp_path a folder to create temporary files in
   */
  public void collapseWRL(String path, String file, String temp_path)
  {
    String buffer = Utility.loadFile(path + file);
    buffer = collapseWRL_aux(buffer, path, temp_path);
    Utility.saveFile(temp_path + "." + file, buffer);
  }
  
  /**
   * Remove VRML inlines by pasting their contents directly in the file.
   *  @param input the contents of a VRML file
   *  @param path the path to the file
   *  @param temp_path a folder to create temporary files in
   *  @return the new collapsed contents of the given VRML file
   */
  public String collapseWRL_aux(String input, String path, String temp_path)
  {
    StringBuffer output = new StringBuffer();
    String url;
    String url_path;
    String tmp;
    int open_brackets;
    int at = 0;
    
    input = collapseWRL_directImages(input, path, temp_path);
    
    while(at < input.length()){
      if(input.charAt(at) == 'I' && (at+6)<=input.length() && input.substring(at,at+6).equals("Inline")){
        while(input.charAt(at) != '{') at++;        //Find first curly bracket
        at++;
        
        open_brackets = 1;
        
        while(open_brackets > 0){                   //Read off entire contents of inline block
          if(input.charAt(at) == '{'){
            open_brackets++;
          }else if(input.charAt(at) == '}'){
            open_brackets--;
          }else if(input.substring(at,at+3).equals("url")){
            while(input.charAt(at) != '\"') at++;   //Find first quote
            at++;
            
            url = "";
            while(input.charAt(at) != '\"'){        //Read in url
              url += input.charAt(at++);
            }
            
            url_path = url.substring(0, url.lastIndexOf('/')+1);
            tmp = Utility.loadFile(path + url);
            tmp = collapseWRL_aux(tmp, path+url_path, temp_path);
            output.append(tmp);                          //Append inline file
          }
          
          at++;
        }
      }else{
        output.append(input.charAt(at++));
      }
    }
    
    return output.toString();
  }
  
  /**
   * Re-direct images to the absolute path of an image with a VRML file.
   *  @param input the contents of a VRML file
   *  @param path the path to the file
   *  @param temp_path a folder to create temporary files in
   *  @return the new re-directed contents of the given VRML file
   */
  public String collapseWRL_directImages(String input, String path, String temp_path)
  {
    StringBuffer output = new StringBuffer();
    String url;
    int open_brackets;
    int at = 0;
    
    while(at < input.length()){
      if(input.charAt(at) == 'I' && (at+12)<=input.length() && input.substring(at,at+12).equals("ImageTexture")){
        while(input.charAt(at) != '{') output.append(input.charAt(at++));        //Find first curly bracket
        output.append(input.charAt(at++));
        
        open_brackets = 1;
        
        while(open_brackets > 0){                                                //Read off entire contents of inline block
          if(input.charAt(at) == '{'){
            open_brackets++;
          }else if(input.charAt(at) == '}'){
            open_brackets--;
          }else if(input.substring(at,at+3).equals("url")){
            while(input.charAt(at) != '\"') output.append(input.charAt(at++));   //Find first quote
            output.append(input.charAt(at++));
             
            url = "";
            while(input.charAt(at) != '\"'){                                     //Read in url
              url += input.charAt(at++);
            }
            
            output.append(Utility.relativePath(temp_path,path) + url);
          }
          
          output.append(input.charAt(at++));
        }
      }else{
        output.append(input.charAt(at++));
      }
    }
    
    return output.toString();
  }
}