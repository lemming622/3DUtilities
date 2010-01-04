package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.utility.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.ply files.
 *  @author Kenton McHenry
 */
public class MeshLoader_PLY extends MeshLoader
{
  public static enum plyProperty {x, y, z, n, vertex1, vertex2, vertex3, unsupported}
  public static enum plyPropertyType {uint8, int32, float32}
  
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "ply";
	}
	
  /**
   * Load a Stanford polygon *.ply file.
   *  @param filename the file to load
   *  @return the loaded mesh
   */
  public Mesh load(String filename)
  {
    Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
  	Vector<Point> vertices = new Vector<Point>();
  	Vector<Face> faces = new Vector<Face>();
    
    try{
    	InputStream is1 = Utility.getInputStream(filename); 
    	InputStream is2 = Utility.getInputStream(filename);
      Scanner ins = new Scanner(is1);       
      DataInputStream dis = new DataInputStream(is2);
      String tmps;
    
      //Read in header
      String format = new String();
      Vector<plyProperty> vertex_properties = new Vector<plyProperty>();
      Vector<plyPropertyType> vertex_types = new Vector<plyPropertyType>();
      Vector<plyProperty> face_properties = new Vector<plyProperty>();
      Vector<plyPropertyType> face_types = new Vector<plyPropertyType>();
      int vertex_count = 0;
      int face_count = 0; 
      
      tmps = ins.next();
      
      while(true){
        if(tmps.equals("format")){
          format = ins.next();
          ins.next();   //Read in version
          tmps = ins.next();
        }else if(tmps.equals("element")){
          tmps = ins.next();
          
          if(tmps.equals("vertex")){
            vertex_count = ins.nextInt();
            tmps = ins.next();
            
            while(tmps.equals("property")){
              tmps = ins.next();  //Read in type
              
              if(tmps.equals("list")){  //Assume it's a triangle since that's all we support for now!
               System.out.println("Error: loadPly -> vertex lists are not supported!");
               System.exit(1);
              }else{
                vertex_types.add(plyPropertyType_valueOf(tmps));
                tmps = ins.next();
                vertex_properties.add(plyProperty_valueOf(tmps));
              }
              
              tmps = ins.next();
            }
          }else if(tmps.equals("face")){
            face_count = ins.nextInt();
            tmps = ins.next();
            
            while(tmps.equals("property")){
              tmps = ins.next();  //Read in type
              
              if(tmps.equals("list")){  //Assume it's a triangle since that's all we support for now!
                tmps = ins.next();      //Read in type for list length
                face_types.add(plyPropertyType_valueOf(tmps));
                face_properties.add(plyProperty.n);
                
                tmps = ins.next();      //Read in type for list
                face_types.add(plyPropertyType_valueOf(tmps));
                face_properties.add(plyProperty.vertex1);
                face_types.add(plyPropertyType_valueOf(tmps));
                face_properties.add(plyProperty.vertex2);
                face_types.add(plyPropertyType_valueOf(tmps));
                face_properties.add(plyProperty.vertex3);
                
                ins.next();             //Read in name of list                
              }else{
                face_types.add(plyPropertyType_valueOf(tmps));
                tmps = ins.next();
                face_properties.add(plyProperty_valueOf(tmps));
              }
              
              tmps = ins.next();
            }
          }
        }else if(tmps.equals("end_header")){
          break;
        }else{
          tmps = ins.next();
        }
      }
        
      //Read in data
      if(format.equals("ascii")){
        for(int i=0; i<vertex_count; i++){
          Point tmpv = new Point();
        
          for(int j=0; j<vertex_properties.size(); j++){
            if(vertex_properties.get(j) == plyProperty.x){
              tmpv.x = ins.nextFloat();
            }else if(vertex_properties.get(j) == plyProperty.y){
              tmpv.y = ins.nextFloat();
            }else if(vertex_properties.get(j) == plyProperty.z){
              tmpv.z = ins.nextFloat();
            }else{     //PLY_UNSUPORTED!
              ins.nextFloat();
            }
          }
        
          vertices.add(tmpv);
        }
      
        for(int i=0; i<face_count; i++){
          Face tmpf = new Face();
        
          for(int j=0; j<face_properties.size(); j++){
            if(face_properties.get(j) == plyProperty.n){
              if(ins.nextInt() != 3){
                System.out.println("Error: faces must contain 3 points!");
                System.exit(1);
              }
            }else if(face_properties.get(j) == plyProperty.vertex1){
              tmpf.v[0] = ins.nextInt();
            }else if(face_properties.get(j) == plyProperty.vertex2){
              tmpf.v[1] = ins.nextInt();
            }else if(face_properties.get(j) == plyProperty.vertex3){
              tmpf.v[2] = ins.nextInt();
            }else{     //PLY_UNSUPORTED!
              ins.nextFloat();
            }
          }
        
          faces.add(tmpf);
        }
      }else if(format.equals("binary_big_endian")){
        while(!dis.readLine().equals("end_header"));
        
        for(int i=0; i<vertex_count; i++){
          Point tmpv = new Point();
          
          for(int j=0; j<vertex_properties.size(); j++){
            if(vertex_properties.get(j) == plyProperty.x){
              if(vertex_types.get(j) == plyPropertyType.float32){
                tmpv.x = dis.readFloat();
              }
            }else if(vertex_properties.get(j) == plyProperty.y){
              if(vertex_types.get(j) == plyPropertyType.float32){
                tmpv.y = dis.readFloat();
              }
            }else if(vertex_properties.get(j) == plyProperty.z){
              if(vertex_types.get(j) == plyPropertyType.float32){
                tmpv.z = dis.readFloat();
              }
            }else{
              if(vertex_types.get(j) == plyPropertyType.uint8){
                dis.readByte();
              }else if(vertex_types.get(j) == plyPropertyType.int32){
                dis.readInt();
              }else if(vertex_types.get(j) == plyPropertyType.float32){
                dis.readFloat();
              }
            }
          }
          
          vertices.add(tmpv);
        }
        
        for(int i=0; i<face_count; i++){
          Face tmpf = new Face();
          
          for(int j=0; j<face_properties.size(); j++){
            if(face_properties.get(j) == plyProperty.vertex1){
              if(face_types.get(j) == plyPropertyType.int32){
                tmpf.v[0] = dis.readInt();
              }
            }else if(face_properties.get(j) == plyProperty.vertex2){
              if(face_types.get(j) == plyPropertyType.int32){
                tmpf.v[1] = dis.readInt();
              }
            }else if(face_properties.get(j) == plyProperty.vertex3){
              if(face_types.get(j) == plyPropertyType.int32){
                tmpf.v[2] = dis.readInt();
              }
            }else{
              if(face_types.get(j) == plyPropertyType.uint8){
                dis.readByte();
              }else if(face_types.get(j) == plyPropertyType.int32){
                dis.readInt();
              }else if(face_types.get(j) == plyPropertyType.float32){
                dis.readFloat();
              }
            }
          }
          
          faces.add(tmpf);
        }
        
        dis.close();
      }
      
      ins.close();
    }catch(Exception e){
    	return null;
    }
    
    mesh.setVertices(vertices);
    mesh.setFaces(faces);
    mesh.initialize();
    
    return mesh;
  }
  
  /**
   * Save a Stanford polygon *.ply file.
   *  @param filename the file to save to
   *  @param mesh the mesh to save
   *  @return true if successful
   */
  public boolean save(String filename, Mesh mesh)
  {
  	Vector<Point> vertices = mesh.getVertices();
    Vector<Face> faces = mesh.getFaces();
    
    try{
      BufferedWriter outs = new BufferedWriter(new FileWriter(filename));
      
      outs.write("ply"); outs.newLine();
      outs.write("format ascii 1.0"); outs.newLine();
      outs.write("element vertex " + vertices.size()); outs.newLine();
      outs.write("property float x"); outs.newLine();
      outs.write("property float y"); outs.newLine();
      outs.write("property float z"); outs.newLine();
      outs.write("element face " + faces.size()); outs.newLine();
      outs.write("property list uchar int vertex_index"); outs.newLine();
      outs.write("end_header"); outs.newLine();
      
      for(int i=0; i<vertices.size(); i++){
        outs.write(vertices.get(i).x + " " + vertices.get(i).y + " " + vertices.get(i).z);
        outs.newLine();
      }
      
      for(int i=0; i<faces.size(); i++){
        outs.write("3 " + faces.get(i).v[0] + " " + faces.get(i).v[1] + " " + faces.get(i).v[2]);
        outs.newLine();
      }

      outs.close();
    }catch(Exception e){
    	return false;
    }
    
    return true;
  }
    
  /**
   * Convert a string to a plyProperty
   *  @param s the string to convert
   *  @return the resulting plyProperty
   */
  public static plyProperty plyProperty_valueOf(String s)
  {
    if(s.equals("x")){
      return plyProperty.x;
    }else if(s.equals("y")){
      return plyProperty.y;
    }else if(s.equals("z")){
      return plyProperty.z;
    }else if(s.equals("vertex1")){
      return plyProperty.vertex1;
    }else if(s.equals("vertex2")){
      return plyProperty.vertex2;
    }else if(s.equals("vertex3")){
      return plyProperty.vertex3;
    }else{
      return plyProperty.unsupported;
    }
  }
  
  /**
   * Convert a string to a plyPropertyType
   *  @param s the string to convert
   *  @return the resulting plyPropertyType
   */
  public static plyPropertyType plyPropertyType_valueOf(String s)
  {
    if(s.equals("uint8")){
      return plyPropertyType.uint8;
    }else if(s.equals("int32")){
      return plyPropertyType.int32;
    }else if(s.equals("float32")){
      return plyPropertyType.float32;
    }else{
      return plyPropertyType.float32;
    }
  }
}