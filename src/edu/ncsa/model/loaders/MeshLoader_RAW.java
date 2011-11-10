package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.raw files.
 *  @author Daniel Long
 */
public class MeshLoader_RAW extends MeshLoader
{
	public static enum rawType {color, noColor};
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "raw";
	}
	
	/**
	 * Load a POV-Ray RAW Triangle format model
	 *  @param filename the file to load
	 *  @return the loaded mesh
	 */
	public Mesh load(String filename)
	{
		Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
		Vector<Point> vertices = new Vector<Point>();
		Vector<Face> faces = new Vector<Face>();
   
		//Open file and read in vertices/faces
		Scanner sc;
		String line;
		rawType type = null;
		int lineCount = 0;
		
		try{
			BufferedReader ins = new BufferedReader(new FileReader(filename));
			line = ins.readLine();
			// RAW files either come with color or without color.
			// If they have color we have 12 floats per line.
			// If they don't, we have 9.
			// We first need to figure out which type this file is
			if(line != null){
				sc = new Scanner(line);
				int floatCount = 0;
				while(sc.hasNextFloat()){
					sc.nextFloat();
					floatCount++;
				}
				sc.close();
				if(floatCount == 9){
					type = rawType.noColor;
				}else if(floatCount == 12){
					type = rawType.color;
				}else{
					return null;
				}
			}
			while(line != null){
				sc = new Scanner(line);
				line = ins.readLine();
				if(sc.hasNextFloat() == false){
					continue;
				}
				if(type == rawType.color){
					// First three floats are colors (rgb)
					sc.nextFloat();
					sc.nextFloat();
					sc.nextFloat();
				}
				float x = sc.nextFloat();
				float y = sc.nextFloat();
				float z = sc.nextFloat();
				vertices.add(new Point(x, y, z));
				x = sc.nextFloat();
				y = sc.nextFloat();
				z = sc.nextFloat();
				vertices.add(new Point(x, y, z));
				x = sc.nextFloat();
				y = sc.nextFloat();
				z = sc.nextFloat();
				vertices.add(new Point(x, y, z));
				faces.add(new Face(3 * lineCount, 3 * lineCount + 1, 3 * lineCount + 2));
				lineCount++;
			}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}

		mesh.setVertices(vertices);
		mesh.setFaces(faces);
		mesh.initialize();
    
		return mesh;
	}
  
	public Mesh load(InputStream is) {return null;}
	public boolean save(String filename, Mesh mesh) {return false;}
}
