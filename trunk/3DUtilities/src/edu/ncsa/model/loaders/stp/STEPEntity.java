package edu.ncsa.model.loaders.stp;
import java.util.*;

/**
 * A structure to represnet a STEP file entity containg a name and multiple attributes.
 *  @author Kenton McHenry
 */
public class STEPEntity
{
  public String name;
  public Vector<STEPAttribute> attribute = new Vector<STEPAttribute>();
  
  /**
   * Class constructor.
   *  @param line a line from a STEP file containing an entity that must be parsed
   */
  public STEPEntity(String line)
  {    
    String tmp;
    int tmpi;
    
    tmpi = line.indexOf('(');
    name = line.substring(0, tmpi).trim();
    line = line.substring(tmpi+1).trim();
    line = line.substring(0, line.length()-2).trim();  //Remove ");"
    
    if(!name.isEmpty()){
      while(!line.isEmpty()){
        tmpi = indexOfDelim(line);
        
        if(tmpi == -1){
          tmp = line;
          line = "";
        }else{
          tmp = line.substring(0, tmpi).trim();
          line = line.substring(tmpi+1).trim();
        }
        
        attribute.add(new STEPAttribute(tmp));
      }
    }
  }
  
  /**
   * Print contents to the standard output.
   */
  public void print()
  {
    System.out.print(name + "(");
    
    for(int i=0; i<attribute.size(); i++){
      if(i > 0) System.out.print(", ");
      attribute.get(i).print();
    }
    
    System.out.println(");");
  }
  
  /**
   * Find the index that delimits the next STEP attribute in the string.
   *  @param line the line to parse
   *  @return the index delimiting the end of the next STEP attribute
   */
  public static int indexOfDelim(String line)
  {
    int parens = 0;
      
    for(int i=0; i<line.length(); i++){
      if(line.charAt(i) == '('){
        parens++;
      }else if(line.charAt(i) == ')'){
        parens--;
      }else if(line.charAt(i) == ','){
        if(parens == 0){
          return i;
        }
      }
    }
    
    return -1;
  }
}
