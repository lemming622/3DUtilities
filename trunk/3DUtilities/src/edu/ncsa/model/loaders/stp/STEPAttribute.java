package edu.ncsa.model.loaders.stp;
import java.util.*;

/**
 * A structure to represent a STEP file attribute 
 *  @author Kenton McHenry
 */
public class STEPAttribute
{
  enum Type {BOOLEAN, FLOAT, INDEX, INLINE, INTEGER, STRING, VECTOR, WILD, UNKNOWN};
    
  public Type type = null;
  public Object value = null;
  
  /**
   * Class constructor.
   *  @param token a token from a STEP file containing an attribute that must be parsed
   */
  public STEPAttribute(String token)
  {
    String tmp;
    int tmpi;
    
    if(token.charAt(0) == '('){                                                   //N-ary attribute
      token = token.substring(1, token.length()-1).trim();                        //Remove outside parenthises
      Vector<STEPAttribute> tmpv = new Vector<STEPAttribute>();
      
      while(!token.isEmpty()){
        tmpi = STEPEntity.indexOfDelim(token);
        
        if(tmpi == -1){
          tmp = token;
          token = "";
        }else{
          tmp = token.substring(0, tmpi).trim();
          token = token.substring(tmpi+1).trim();
        }
        
        tmpv.add(new STEPAttribute(tmp));
      }
      
      type = STEPAttribute.Type.VECTOR;
      value = tmpv;
    }else if(token.contains("(")){                                                //Inline entity (TODO: literal strings can have parenthisis!)
      type = STEPAttribute.Type.INLINE;
      value = token;
    }else if(token.equals("*")){                                                  //Wild card attribute?
      type = STEPAttribute.Type.WILD;
      value = '*'; 
    }else if(token.charAt(0) == '\'' && token.charAt(token.length()-1) == '\''){  //String attribute
      token = token.substring(1, token.length()-1).trim();                        //Remove quotes
      type = STEPAttribute.Type.STRING;
      value = token;
    }else if(token.contains(".")){
      if(token.length()>1 && token.charAt(0)=='.' && token.charAt(token.length()-1)=='.'){
        if(token.charAt(1) == 'T'){                                               //Boolean attribute
          type = STEPAttribute.Type.BOOLEAN;
          value = true;
        }else{
          type = STEPAttribute.Type.BOOLEAN;
          value = false;
        }
      }else{                                                                      //Float attribute
        type = STEPAttribute.Type.FLOAT;
        value = Double.valueOf(token);
      }
    }else if(token.charAt(0) == '#'){                                             //Index attribute
      type = STEPAttribute.Type.INDEX;
      value = Integer.valueOf(token.substring(1));
    }else{
      try{
        type = STEPAttribute.Type.INTEGER;
        value = Integer.valueOf(token);
      }catch(NumberFormatException e){
        type = STEPAttribute.Type.UNKNOWN;
        value = token;
      }
    }
  }
  
  /**
   * Print contents to the standard output.
   */
  public void print()
  {
    if(type == Type.INDEX){
      System.out.print("#" + value);
    }else if(type == Type.STRING){
      System.out.print("\'" + value + "\'");
    }else if(type == Type.VECTOR){
      Vector<STEPAttribute> tmpv = (Vector<STEPAttribute>)value;
      
      System.out.print("(");
      
      for(int i=0; i<tmpv.size(); i++){
        if(i > 0) System.out.print(", ");
        tmpv.get(i).print();
      }
      
      System.out.print(")");        
    }else{
      System.out.print(value);
    }
  }
}
