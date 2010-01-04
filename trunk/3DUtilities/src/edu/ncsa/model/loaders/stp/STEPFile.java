package edu.ncsa.model.loaders.stp;
import edu.ncsa.model.loaders.stp.STEPAttribute.Type;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * A structure representing the contents of STEP file.
 *  @author Kenton McHenry
 */
public class STEPFile
{
	public Vector<STEPEntity> header = new Vector<STEPEntity>();
  public Vector<STEPEntity> data = new Vector<STEPEntity>();
  
  /**
   * Class constructor.
   *  @param filename the file to load
   */
  public STEPFile(String filename)
  {
    Vector<String> data_lines = new Vector<String>();
    boolean READING_HEADER = false;
    boolean READING_DATA = false;
    String line;
    String tmp;
    int id;
    int max_id = 0;
    int tmpi;
    
    try{
      BufferedReader ins = new BufferedReader(new FileReader(filename));
      
      while((line=ins.readLine()) != null){
        line = line.trim();
        
        if(!line.isEmpty()){
          while(line.charAt(line.length()-1) != ';'){		//All lines must end with a ';'
            tmp = ins.readLine();
            
            if(tmp != null){
              line += tmp.trim();
            }else{
              throw new Exception("Incomplete data line");
            }
          }
            
          if(true){		//Remove comments
          	String line_tmp = "";
          	int at = 0;
          	
          	while(at < line.length()){
          		if(line.charAt(at) == '/' && at<line.length()-1 && line.charAt(at+1) == '*'){			//Entering a comment
          			at+=2;
          			
        				while(at < line.length()){
        					if(line.charAt(at) == '*' && at<line.length()-1 && line.charAt(at+1) == '/'){	//Exiting a comment
        						at+=2;
        						break;
        					}
        					
        					at++;
        				}
          		}else{
          			line_tmp += line.charAt(at);
          			at++;
          		}
          	}
          	
          	line = line_tmp;
          }
          
          if(READING_DATA && line.charAt(0) == '#'){		//All lines with data should start with '#' and end with ';'  
            data_lines.add(line);
          }else if(READING_HEADER){																				
        		if(line.contains("ENDSEC")){
        			READING_HEADER = false;
        			READING_DATA = true;
        		}else{
        			header.add(new STEPEntity(line));
        		}
          }else if(line.contains("HEADER")){
          	READING_HEADER = true;
          }
        }
      }
    }catch(Exception e) {}
    
    //Pass-1: Count number of data entities
    for(int i=0; i<data_lines.size(); i++){
      line = data_lines.get(i);
      tmpi = line.indexOf('=');
      
      if(tmpi >= 0){  //Get id
        tmp = line.substring(0, tmpi).trim();
        id = Integer.valueOf(tmp.substring(1));   //Remove '#'
        if(id > max_id) max_id = id;
      }
    }
    
    data.setSize(max_id+1);
    
    //Pass-2: Set the data entities
    for(int i=0; i<data_lines.size(); i++){
      line = data_lines.get(i);
      tmpi = line.indexOf('=');
      
      if(tmpi >= 0){  
        //Get id
        tmp = line.substring(0, tmpi).trim();
        id = Integer.valueOf(tmp.substring(1));   //Remove '#'
        
        //Get entity
        tmp = line.substring(tmpi+1).trim();
        data.set(id, new STEPEntity(tmp));
      }
    }
    
    expandInlineEntities();
  }
  
  /**
   * Expand all inline entities, moving them to their own lines and referencing them.
   */
  public void expandInlineEntities()
  {
    for(int i=0; i<data.size(); i++){
      if(data.get(i) != null){
        for(int j=0; j<data.get(i).attribute.size(); j++){
          expandInlineEntities(data.get(i).attribute.get(j));
        }
      }
    }
  }
  
  /**
   * Expand any inline entities within the given STEP attribute, moving them to their own
   * line and refering to them by their index.
   *  @param sa the STEP attribute to check
   */
  public void expandInlineEntities(STEPAttribute sa)
  {
    if(sa.type == Type.INLINE){
      data.add(new STEPEntity((String)sa.value + ";"));
      sa.type = STEPAttribute.Type.INDEX;    
      sa.value = data.size()-1;  
    }else if(sa.type == Type.VECTOR){
      Vector<STEPAttribute> tmpv = (Vector<STEPAttribute>)sa.value;
      
      for(int i=0; i<tmpv.size(); i++){
        expandInlineEntities(tmpv.get(i));
      }
    }
  }
  
  /**
   * Print the contents of the STEP file.
   */
  public void print()
  {
    if(data != null){
      System.out.println();
      
      for(int i=1; i<data.size(); i++){
        if(data.get(i) != null){
          System.out.print("#" + i + " = ");
          data.get(i).print();
        }
      }
    }
  }
  
  /**
   * Find an entity of the given name by using a depth first search on the tree 
   * rooted at the given entity.
   *  @param name the name of the desired entity
   *  @param e the index of the entity to root the search tree
   *  @return the index of the desired entity
   */
  public int findEntity(String name, int e)
  {
    STEPAttribute sa;
    int index, tmpi;
    
    if(e<data.size()){
      //Check this entity
      if(data.get(e).name.equals(name)){
        return e;
      }
      
      //Check this entities attributes
      for(int a=0; a<data.get(e).attribute.size(); a++){
        sa = data.get(e).attribute.get(a);
        
        if(sa.type == Type.INDEX){
          index = (Integer)sa.value;
          
          if(data.get(index).name.equals(name)){
            return index;
          }else{
            tmpi = findEntity(name, index);
            if(tmpi != -1) return tmpi;
          }
        }
      }
    }
    
    return -1;
  }
  
  /**
   * Ignore the tree structure of the file and just get the desired referenced entity.
   *  @param e the index of the entity which we want an attribute from
   *  @param a the index of the attribute within this entity
   *  @param i the index into the attibute if it is a vector
   *  @param name the name of the entity that this attribute references
   *  @return the index of the desired entity
   */
  public int getEntity(int e, int a, int i, String name)
  {
    STEPAttribute sa = null;
    
    if(e < data.size() && a < data.get(e).attribute.size()){
      sa = data.get(e).attribute.get(a);
      
      if(sa.type == Type.VECTOR){
        Vector<STEPAttribute> tmpv = (Vector<STEPAttribute>)sa.value;
        
        if(i < tmpv.size()){
          sa = tmpv.get(i);
        }
      }
      
      if(sa.type == Type.INDEX){
        return findEntity(name, (Integer)sa.value);
      }
    }
    
    return -1;
  }
  
  /**
   * Get the name of the referenced entity.
   *  @param e the index of the entity which we want an attribute from
   *  @param a the index of the attribute within this entity
   *  @param i the index into the attibute if it is a vector
   *  @return the name of the referenced entity
   */
  public String getEntityName(int e, int a, int i)
  {
    STEPAttribute sa = null;
    
    if(e < data.size() && a < data.get(e).attribute.size()){
      sa = data.get(e).attribute.get(a);
      
      if(sa.type == Type.VECTOR){
        Vector<STEPAttribute> tmpv = (Vector<STEPAttribute>)sa.value;
        
        if(i < tmpv.size()){
          sa = tmpv.get(i);
        }
      }
      
      if(sa.type == Type.INDEX){
        return data.get((Integer)sa.value).name;
      }
    }
    
    return null;
  }
}
