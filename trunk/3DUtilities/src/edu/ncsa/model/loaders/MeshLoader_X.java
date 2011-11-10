package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import kgm.image.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

/**
 * A mesh file loader for *.x files.
 *  @author Daniel Long
 */
public class MeshLoader_X extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "x";
	}
	
	/**
	 * Load a Direct X model.
	 *  @param filename the file to load
	 *  @return the loaded mesh
	 */
	public Mesh load(String filename)
	{
		Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
   
		//Open file and read in vertices/faces
		Scanner sc;
		String line;
		
		HashMap<String, Integer> materialMap = new HashMap<String, Integer>();
		
		//Note regarding the sc.useDelimiter() commands:
		//This format allows (but does not require) the numbers we are looking for
		//(vertexCount, faceCount, vertex indexes, and vertex
		//coordinates) to be immediately followed by the appropriate
		//delimiter (a semicolon or comma) without any whitespace.
		//The scanner class will throw an exception if you request
		//nextInt() when the next token is "541;", so the delimiters
		//have to be redefined.
		
		try{
			BufferedReader ins = new BufferedReader(new FileReader(filename));
			
			while((line = ins.readLine()) != null){
				//We are looking for lines starting with "Mesh"
				sc = new Scanner(line);
				if(sc.hasNext() == false){
					sc.close();
					continue;
				}
				
				String next = sc.next();
				if("Mesh".equalsIgnoreCase(next)){
					sc.next();
					Vector<Point> vertices = new Vector<Point>();
					Vector<Face> faces = new Vector<Face>();
					
					//Next line is number of vertices
					sc.close();
					sc = new Scanner(ins.readLine());
					
					//The delimiter is required whitespace followed
					//by an optional semicolon or options whitespace
					//followed by a required semicolon.
					sc.useDelimiter("(\\s+;?)|(\\s*;)");
					
					int vertexCount = sc.nextInt();
					sc.close();
					
					//Next vertexCount lines define the 
					//vertices. x, y, and z are separated
					//by semicolons
					while(vertexCount > 0){
						sc = new Scanner(ins.readLine());
						sc.useDelimiter("(\\s+;?)|(\\s*;)"); //Same delimiter as before
						if(sc.hasNextFloat() == true){
							float x = sc.nextFloat();
							float y = sc.nextFloat();
							float z = sc.nextFloat();
							vertices.add(new Point(x, y, z));
							vertexCount--;
						}
						sc.close();
					}
					
					//Next token defines the number of faces
					int faceCount = -1;
					while(faceCount == -1){
						sc = new Scanner(ins.readLine());
						sc.useDelimiter("(\\s+;?)|(\\s*;)"); //Same delimiter as before
						if(sc.hasNextInt()){
							faceCount = sc.nextInt();
						}
						sc.close();
					}
					
					//Finally we have faceCount lines defining the faces
					//The first token is the number of vertices in the
					//face. The remaining tokens are the indexes of those
					//vertices. The first token is separated from the second
					//by a semicolon. The other tokens are comma-separated.
					while(faceCount > 0){
						sc = new Scanner(ins.readLine());
						
						//The delimiter is required whitespace followed by an
						//optional semicolon or comma OR optional whitespace followed
						//by a required semicolon or comma
						sc.useDelimiter("(\\s+[;,]?)|(\\s*[;,])");
						
						if(sc.hasNextInt() == true){
							int verticesInFace = sc.nextInt();
							ArrayList<Integer> al = new ArrayList<Integer>();
							while(verticesInFace > 0){
								al.add(sc.nextInt());
								verticesInFace--;
							}
							
							faces.add(new Face(al));
							faceCount--;	
						}
						sc.close();
					}
					
					//Search for MeshMaterialList
					String token = "";
					while("MeshMaterialList".equals(token) == false){
						sc = new Scanner(ins.readLine());
						if(sc.hasNext()){
							token = sc.next();
						}
						sc.close();
					}
					
					sc = new Scanner(ins.readLine());
					sc.useDelimiter("(\\s+[;,]?)|(\\s*[;,])");
					int materialCount = sc.nextInt();
					sc.close();
					
					sc = new Scanner(ins.readLine());
					sc.useDelimiter("(\\s+[;,]?)|(\\s*[;,])");
					int indexes = sc.nextInt();
					sc.close();
					
					for(int i = 0; i < indexes; i++){
						sc = new Scanner(ins.readLine());
						sc.useDelimiter("(\\s+[;,]?)|(\\s*[;,])");
						faces.get(i).material = new Material(sc.nextInt());
					}
					
					//The tid's just set aren't the proper tids we use, but
					//refer instead to the materials listed on the next
					//materialCount lines
					ArrayList<String> materialNames = new ArrayList<String>();
					for(int i = 0; i < materialCount; i++){
						line = ins.readLine();
						if(line.contains("{ ") && line.contains(" }")){
							line = line.substring(line.indexOf("{ ") + 2, line.lastIndexOf(" }"));
						}
						materialNames.add(line);
					}
					
					//Convert over the tids
					for(int i = 0; i < indexes; i++){
						String materialName = materialNames.get(faces.get(i).material.tid);
						
						if(materialMap.containsKey(materialName)){
							faces.get(i).material.tid = materialMap.get(materialName);
						}else{
							faces.get(i).material.tid = -1;
						}
					}
					
					//Search for MeshTextureCoords
					while(("MeshTextureCoords".equals(token) == false) && ((line = ins.readLine()) != null)){
						sc = new Scanner(line);
						if(sc.hasNext()){
							token = sc.next();
						}
						sc.close();
					}
					
					//Load texture coords
					if(line != null){
						sc = new Scanner(ins.readLine());
						sc.useDelimiter("(\\s+[;,]?)|(\\s*[;,])");
						int coordCount = sc.nextInt();
						sc.close();
						UV coords[] = new UV[coordCount];
						
						//coords[i] gives the coords for the ith vertex
						for(int i = 0; i < coordCount; i++){
							sc = new Scanner(ins.readLine());
							sc.useDelimiter("(\\s+[;,]?)|(\\s*[;,])");
							coords[i] = new UV(sc.nextFloat(), 1 - sc.nextFloat());
						}
						
						//Add these coords to the faces
						for(int i = 0; i < faces.size(); i++){
							UV uv[] = new UV[faces.get(i).v.length];
							for(int j = 0; j < faces.get(i).v.length; j++){
								uv[j] = coords[faces.get(i).v[j]];
							}
							faces.get(i).uv = uv;
						}
					}
					
					mesh.addData(vertices, faces, null);
				}else if("Material".equals(next)){
					//Look for the texture filename
					String materialName = sc.next();
					String textureName = "";
					while(true){
						sc.close();
						sc = new Scanner(ins.readLine());
						if(sc.hasNext() == false){
							continue;
						}
						
						next = sc.next();
						if("}".equals(next)){
							break;
						}else if("TextureFilename".equals(next)){
							sc.close();
							textureName = ins.readLine();
						}
					}
					
					//Load the texture
					if("".equals(textureName)){
						//No texture name found
						materialMap.put(materialName, -1);
					}else{
						if(textureName.contains("\\\\") == true){
							textureName = textureName.substring(textureName.lastIndexOf("\\\\") + 2, textureName.lastIndexOf('\"'));
						}else{
							textureName = textureName.substring(textureName.indexOf('\"') + 1, textureName.lastIndexOf('\"'));
						}
						textureName = filename.substring(0, filename.lastIndexOf('/')) + '/' + textureName;
						
						try{
				        	File f = new File(textureName);
				        	BufferedImage bufferedimage = ImageIO.read(f);
					        int w = bufferedimage.getWidth(null);
					        int h = bufferedimage.getHeight(null);
					        int[] img = new int[w * h];
					        bufferedimage.getRGB(0, 0, w, h, img, 0, w);
					    
					        //Make sure image dimensions are a power of two
					      	int n = (w > h) ? w : h;
					      	n = (int)Math.round(Math.pow(2, Math.ceil(Math.log(n)/Math.log(2))));
					        img = ImageUtility.resize(img, w, h, n, n);
					        w = n;
					        h = n;
					        
					        //Add the texture
					        materialMap.put(materialName, mesh.addTexture(textureName, img, w));
						}catch(Exception e){
							System.err.println("Unable to open texture file \"" + textureName + "\"");
							materialMap.put(materialName, -1);
						}
					}
				}
				sc.close();
			}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}

		mesh.initialize();    
		return mesh;
	}
  
	public Mesh load(InputStream is) {return null;}
	public boolean save(String filename, Mesh mesh) {return false;}
}