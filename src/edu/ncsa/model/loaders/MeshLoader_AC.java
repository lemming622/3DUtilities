package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import kgm.image.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;

/**
 * A mesh file loader for *.ac files.
 *  @author Daniel Long
 */
public class MeshLoader_AC extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "ac";
	}
	
	/**
	 * Load an AC3D model.
	 *  @param filename the file to load
	 *  @return the loaded mesh
	 */
	public Mesh load(String filename)
	{
		Mesh mesh = new Mesh();
		mesh.addFileMetaData(filename);
   
		//Open file and read in vertices/faces
		Scanner sc;
		String line;
		HashMap<String, Integer> textureMap = new HashMap<String, Integer>();
		int kids = 0;
		Stack<Integer> kidsStack = new Stack<Integer>();
		float totalTranslation[] = new float[3];
		float translation[];
		Stack<float[]> translationStack = new Stack<float[]>();
		
		try{
			BufferedReader ins = new BufferedReader(new FileReader(filename));
			ins.readLine(); //The first line should be AC3Dx where x is some hexadecimal digit
			
			while((line = ins.readLine()) != null){
				sc = new Scanner(line);
				if(sc.hasNext() == false){
					sc.close();
					continue;
				}
				
				if("OBJECT".equalsIgnoreCase(sc.next())){ //This is the only token we care about
					//Define a few variables we'll need
					Vector<Point> vertices = new Vector<Point>();
					Vector<Face> faces = new Vector<Face>();
					translation = new float[3];
					String name = "";
					String textureName = "";
					
					if(kids != 0){
						kids--;
					}else{
						while((kids == 0) && (kidsStack.size() != 0)){
							kids = kidsStack.pop();
							translation = translationStack.pop();
							totalTranslation[0] -= translation[0];
							totalTranslation[1] -= translation[1];
							totalTranslation[2] -= translation[2];
						}
						if(kids != 0){
							kids--;
						}
					}
					
					while(true){
						sc.close();
						sc = new Scanner(ins.readLine());
						if(sc.hasNext() == false){
							continue;
						}
						String token = sc.next();
						if("kids".equalsIgnoreCase(token)){
							//this is the last part of the object structure
							int newKids = sc.nextInt();
							if(newKids != 0){
								kidsStack.push(kids);
								float tmp[] = new float[3];
								tmp[0] = translation[0];
								tmp[1] = translation[1];
								tmp[2] = translation[2];
								translationStack.push(tmp);
								kids = newKids;
							}else{
								//Undo the translation
								totalTranslation[0] -= translation[0];
								totalTranslation[1] -= translation[1];
								totalTranslation[2] -= translation[2];
							}
							
							//Add vertices, faces, and texture
							int tid = -1;
							
							if(textureName.equals("") == false){
								//Remove quotes from beginning and end of filename
								textureName = textureName.substring(1, textureName.length() - 1);
								
								//Add proper path before the texture names
								textureName = filename.substring(0, filename.lastIndexOf('/') + 1) + textureName;
								
								if(textureMap.containsKey(textureName) == true){
									tid = textureMap.get(textureName);
								}else{
						    		//Load the texture
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
								        tid = mesh.addTexture(textureName, img, w);
									}catch(Exception e){
										System.err.println("Unable to open texture file \"" + textureName + "\".");
									}
									
									//Add this texture to the map so we don't have
									//to repeatedly read the same image
									textureMap.put(textureName, tid);
								}
							}
							
							mesh.addData(vertices, faces, tid, name);
							break;
						}else if("name".equalsIgnoreCase(token)){
							name = sc.next();
						}else if("texture".equalsIgnoreCase(token)){
							textureName = sc.next();
						}else if("loc".equalsIgnoreCase(token)){
							//Next come 3 floats giving a translation for
							//the object
							translation[0] = sc.nextFloat();
							translation[1] = sc.nextFloat();
							translation[2] = sc.nextFloat();
							totalTranslation[0] += translation[0];
							totalTranslation[1] += translation[1];
							totalTranslation[2] += translation[2];
						}else if("numvert".equalsIgnoreCase(token)){
							int vertexCount = sc.nextInt();
							
							//Next we have vertexCount lines with
							//three floats on each line giving the
							//coordinates of the vertices
							while(vertexCount > 0){
								sc.close();
								sc = new Scanner(ins.readLine());
								float x = sc.nextFloat();
								float y = sc.nextFloat();
								float z = sc.nextFloat();

								//Translate the coordinates
								vertices.add(new Point(x + totalTranslation[0], y + totalTranslation[1], z + totalTranslation[2]));
								vertexCount--;
							}
						}else if("numsurf".equalsIgnoreCase(token)){
							int surfaceCount = sc.nextInt();
							
							while(surfaceCount > 0){
								sc.close();
								ins.readLine(); //This line specifies the type of surface
								ins.readLine(); //This line specifies the material this surface has
								sc = new Scanner(ins.readLine());
								sc.next(); //This token should be "refs"
								int vertexCount = sc.nextInt();
								UV uv[] = new UV[vertexCount];
								ArrayList<Integer> al = new ArrayList<Integer>();
								//vertexCount lines giving the vertex indexes making
								//up this surface (face)
								
								for(int i = 0; i < vertexCount; i++){
									sc.close();
									sc = new Scanner(ins.readLine());
									al.add(sc.nextInt());
									uv[i] = new UV(sc.nextFloat(), sc.nextFloat());
								}
								
								Face face = new Face(al);
								face.uv = uv;
								faces.add(face);
								surfaceCount--;
							}
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
