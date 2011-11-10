package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;

import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.poly files.
 * Specifications can be found at http://local.wasp.uwa.edu.au/~pbourke/dataformats/poly/
 * Light and material properties are not supported
 *  @author Victoria Winner
 */
public class MeshLoader_POLY extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "poly";
	}

	public Mesh load(String filename) 
	{
		Mesh mesh = new Mesh(); 
		mesh.addFileMetaData(filename);
		Vector<Point> vertices = new Vector<Point>();
		Vector<Point> vertexNormals = new Vector<Point>();
		Vector<Face> faces = new Vector<Face>();
		
		Scanner sc;
		String line = "";
		int numVertices = 0;
		int numFaces = 0;
		int numMaterials = 0;
		int numLights = 0;
		try{
		BufferedReader ins = new BufferedReader(new FileReader(filename));
		
		while((line = ins.readLine()) != null){
			//We are looking for lines starting with "Object"
			sc = new Scanner(line);
			if(sc.hasNext() == false){
				sc.close();
				continue;
			}
		
			line = sc.next();
			
			if(line.contains("OBJECT")){
				
				
				if(numFaces !=0){
					System.out.println("file format failure");
					return null;
				}
				numFaces = sc.nextInt();
				sc.close();
				
				for(int i = 0; i<numFaces; i++){
					sc = new Scanner(ins.readLine());
					line = sc.next();
					if(line.contains("EN.")){
						numFaces = i;
						break;
					}
					if(line.contains("PO.")){
						Face f = new Face();
						sc.nextInt();
						sc.nextInt();
						f.v[0]= sc.nextInt();
						f.v[1]= sc.nextInt();
						f.v[2]= sc.nextInt();
						faces.add(f);
					}
					sc.close();
				}
			//	if(line.contains("EN.")== false){
			//		System.out.print("file format error");
			//		return null;
			//	}
			}
			if(line.equalsIgnoreCase("VERTEX")){
				if(numVertices != 0){
					System.out.println("file format failure");
				}
				numVertices = sc.nextInt();
				
				
				for(int i = 0; i<numVertices; i++){
					sc.close();
					sc = new Scanner(ins.readLine());
					Point vertex = new Point();
					Point normal = new Point();
					vertex.x = sc.nextDouble();
					vertex.y = sc.nextDouble();
					vertex.z = sc.nextDouble();
					normal.x = sc.nextDouble();
					normal.y = sc.nextDouble();
					normal.z = sc.nextDouble();
					vertices.add(vertex);
					vertexNormals.add(normal);
				}
			}
		}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
		mesh.addData(vertices, faces, null);
		mesh.initialize();
		return mesh;
	}
	public Mesh load(InputStream is) {return null;}
	public boolean save(String filename, Mesh mesh) {return false;}
}