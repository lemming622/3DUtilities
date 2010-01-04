package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.model.loaders.stp.*;
import java.util.*;

/**
 * A mesh file loader for *.stp files.
 *  @author Kenton McHenry
 */
public class MeshLoader_STP extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "stp";
	}
	
  /**
   * Load a STEP file.
   *  @param filename the file to load
   *  @return the loaded mesh
   */
  public Mesh load(String filename)
  {
  	Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
  	Vector<Point> vertices = new Vector<Point>();
  	Vector<Face> faces = new Vector<Face>();
  	Vector<STEPAttribute> tmpv;
  	String tmps;
  	
    STEPFile sf = new STEPFile(filename);
    int n;
    
    //sf.print();
    
    if(sf.data != null){    	
      if(true){														//Load metadata
      	for(int e=0; e<sf.header.size(); e++){
      		if(sf.header.get(e) != null){
      			if(sf.header.get(e).name.equals("FILE_NAME")){
      				mesh.addMetaData("Name", (String)sf.header.get(e).attribute.get(0).value);
      				mesh.addMetaData("TimeStamp", (String)sf.header.get(e).attribute.get(1).value);
      				
      				//Get authors
      				tmpv = (Vector<STEPAttribute>)sf.header.get(e).attribute.get(2).value;
      				tmps = "";
      				
      				for(int i=0; i<tmpv.size(); i++){
      					tmps += tmpv.get(i).value;
      					if(i < tmpv.size()-1) tmps += ", ";
      				}
      				
      				mesh.addMetaData("Author", tmps);
      				
      				//Get organizations
      				tmpv = (Vector<STEPAttribute>)sf.header.get(e).attribute.get(3).value;
      				tmps = "";
      				
      				for(int i=0; i<tmpv.size(); i++){
      					tmps += tmpv.get(i).value;
      					if(i < tmpv.size()-1) tmps += ", ";
      				}
      				
      				mesh.addMetaData("Organization", tmps);
      				
      				//Get final attributes of FILE_NAME entity
      				mesh.addMetaData("PreProcessorVersion", (String)sf.header.get(e).attribute.get(4).value);
      				mesh.addMetaData("OriginatingSystem", (String)sf.header.get(e).attribute.get(5).value);
      				mesh.addMetaData("Authorization", (String)sf.header.get(e).attribute.get(6).value);
      			}
      		}
      	}
      }
          	
      if(true){    //Load faces
      	Utility.Pair<Vector<Point>,Vector<Face>> surface = null;
        
        for(int e=0; e<sf.data.size(); e++){
          if(sf.data.get(e) != null){
            if(sf.data.get(e).name.equals("ADVANCED_FACE") || sf.data.get(e).name.equals("POLY_LOOP")){
            	if(sf.data.get(e).name.equals("ADVANCED_FACE")){
            		surface = STEPUtility.getSurface(sf, e);
            	}else if(sf.data.get(e).name.equals("POLY_LOOP")){
            		surface = new Utility.Pair<Vector<Point>,Vector<Face>>();
            		surface.first = STEPUtility.getPolyLine(sf, e);
            		surface.second = new Polygon(surface.first).getConvexPolygonFaces();
            	}
            	
            	if(surface != null){
            		n = vertices.size();
            		vertices.addAll(surface.first);
            		faces.addAll(Face.plus(surface.second, n));
            	}
            }
          }
        }
      }
            
      if(faces.isEmpty()){    //Load edges
        Vector<Point> curve = null;
        
        for(int e=0; e<sf.data.size(); e++){
          if(sf.data.get(e) != null){
            if(sf.data.get(e).name.equals("EDGE_CURVE") || sf.data.get(e).name.equals("POLYLINE")){
            	if(sf.data.get(e).name.equals("EDGE_CURVE")){
            		curve = STEPUtility.getCurve(sf, e);
            	}else if(sf.data.get(e).name.equals("POLYLINE")){
            		curve = STEPUtility.getPolyLine(sf, e);
            	}
              
              if(curve != null){
                n = vertices.size();
                vertices.addAll(curve);
                
                for(int i=0; i<curve.size()-1; i++){
                  faces.add(new Face(n+i, n+i+1));
                }
              }
            }
          }
        }
      }

      if(vertices.isEmpty()){    //Load points
        for(int e=0; e<sf.data.size(); e++){        	
          if(sf.data.get(e) != null){
            if(sf.data.get(e).name.equals("CARTESIAN_POINT")){
              vertices.add(STEPUtility.getVertex(sf, e));
            }
          }
        }
      }
    }
    
    Point.zeroInvalidVertices(vertices);
    
    mesh.setVertices(vertices);
    mesh.setFaces(faces);
    mesh.initialize();
    
    return mesh;
  }
  
  public boolean save(String filename, Mesh mesh) {return false;}
}