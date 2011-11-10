package edu.ncsa.model.loaders._3ds;
import edu.ncsa.model.Mesh;
import edu.ncsa.model.MeshAuxiliary.*;
import kgm.image.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import javax.imageio.ImageIO;

/**
 * A mesh file loader for *.3ds files
 * @author Daniel Long
 */
public class Loader3ds
{
	/**
	 * Creates a new Loader3ds object
	 * @param filename The *.3ds file to be loaded
	 */
    public Loader3ds(String filename)
    {
    	try{
            mesh = new Mesh();
    		reader = new DataInputStream(new FileInputStream(filename));
    		tid = -1;
    	}catch(FileNotFoundException e){
    		e.printStackTrace();
    	}
    }
    
    /**
     * Creates a new Loader3ds object with a texture
     * @param filename The *.3ds file to be loaded
     * @param textureName_ The filename of the texture to use with the model
     */
    public Loader3ds(String filename, String textureName_)
    {
    	try{
            mesh = new Mesh();
    		reader = new DataInputStream(new FileInputStream(filename));
    		
    		//Load the texture
        	String textureName = textureName_;
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
    	}catch(IOException e){
			System.out.println("Unable to load texture file \"" + textureName_ + "\".");
		}catch(Exception e){
    		e.printStackTrace();
    	}
    }

	/**
	 * Loads a Mesh from a 3DS file
	 * @return A mesh containing the 3DS model
	 */
    public Mesh Load3ds()
    {
    	//Initialize variables
        materialNames = new ArrayList<String>();
        materials = new HashMap<String, Material>();
        mappingCoords = new Vector<UV>();
        vertices = new Vector<Point>();
        faces = new Vector<Face>();
        groupNumber = 1;
        mesh.clear();
        
        //Load in the first chunk,
        //make sure its the MAIN3DS
        //chunk and process it
        Chunk chunk = readChunk();
        if(chunk.id == 0x4D4D){ //MAIN3DS
            Load3dsMain(chunk.length - 6);
        }
        
        return mesh;
    }
    
    /**
     * Loads an array of faces from the 3ds file
     * @param sectionLength The length, in bytes, of this section
     */
    private void Load3dsFaceArray(int sectionLength)
    {
    	//Read in number of faces in the array
        int faceCount = readBytes(2);
        sectionLength -= 2;
        
        for(int i = 0; i < faceCount; i++){
        	//Every face consists of three vertices
        	//The vertices are given as 0-based indexes
        	//(referring to our Vector of vertices)
        	int v1 = readBytes(2);
        	int v2 = readBytes(2);
        	int v3 = readBytes(2);
        	readBytes(2); //Flags
        	faces.add(new Face(v1, v2, v3));
            sectionLength -= 8;
        }

        String materialName = null;
        //We still have more data to read in
        if(sectionLength > 0){
            Chunk chunk = readChunk();
            sectionLength -= 6;
            
            switch(chunk.id){
            	case 0x4130: //TRI_MATERIAL
            		materialName = readString();
            		skipBytes(sectionLength - materialName.length() - 1);
            		break;

            	default: //Unknown section; ignore it
            		skipBytes(sectionLength);
                	break;
            }
        }
        
        for(int i = 0; i < faceCount; i++){
        	materialNames.add(materialName);
        }
    }

    /**
     * Loads all the data in the EDIT3DS section (including Material and Objects)
     * @param sectionLength The length of this section in bytes
     */
    private void Load3dsMData(int sectionLength)
    {
        Chunk chunk;
        while(sectionLength > 0){
            chunk = readChunk();
            switch(chunk.id){
            	case 0x4000: //EDIT_OBJECT 
            		Load3dsObject(chunk.length - 6);
            		break;

            	case 0xAFFF: //EDIT_MATERIAL 
            		Load3dsMaterial(chunk.length - 6);
            		break;

            	default: //Unknown; skip it
            		skipBytes(chunk.length - 6);
            		break;
            }
            sectionLength -= chunk.length;
            
            //Set up the UV coordinates
            if(mappingCoords.size() != 0){
    	        for(int i = 0; i < faces.size(); i++)
    	        {
    	        	//Get the UV coordinates of those three vertices
    	        	UV[] uv = new UV[3];
    	        	uv[0] = mappingCoords.get(faces.get(i).v[0]);
    	        	uv[1] = mappingCoords.get(faces.get(i).v[1]);
    	        	uv[2] = mappingCoords.get(faces.get(i).v[2]);
    	        	faces.get(i).uv = uv;
    	        }
            }
            
            //Add the appropriate material to each face
            for(int i = 0; i < materialNames.size(); i++){
            	faces.get(i).material = materials.get(materialNames.get(i));
            	
            	/*
            	if(faces.get(i).material != null){
            		faces.get(i).color = faces.get(i).material.diffuse;
            	}
            	*/
            }
            
            mesh.addData(vertices, faces, tid, "Group #" + groupNumber);
            groupNumber++;
            vertices.clear();
            faces.clear();
            mappingCoords.clear();
            materialNames.clear();
        }
    }

    /**
     * The initial loader for the 3DS file. Searches for the EDIT3DS section.
     * @param sectionLength The length of the section in bytes
     */
    private void Load3dsMain(int sectionLength)
    {
        Chunk chunk;
        while(sectionLength > 0){
            chunk = readChunk();
            switch(chunk.id){
            	case 0x3D3D: //EDIT3DS 
            		Load3dsMData(chunk.length - 6);
            		break;

            	default: //Unknown; ignore it
            		skipBytes(chunk.length - 6);
                	break;
            }
            sectionLength -= chunk.length;
        }
    }
    
    /**
     * Loads the texture mapping coordinates from the 3DS file.
     * @param sectionLength Length of this section, in bytes
     */
    private void Load3dsMappingCoords(int sectionLength)
    {
    	int coordinateCount = readBytes(2);
    	while(coordinateCount > 0){
    		float u = readFloat();
    		float v = readFloat();
    		mappingCoords.add(new UV(u, v));
    		coordinateCount--;
    	}
    }

    /**
     * Loads in material data from the 3DS file.
     * @param sectionLength The length of this section, in bytes
     */
    private void Load3dsMaterial(int sectionLength)
    {
        String materialName = null;
        Chunk chunk;
        while(sectionLength > 0){
            chunk = readChunk();
            switch(chunk.id){
	            case 0xA000: //MAT_NAME01 (the name of the material layer)
	                materialName = readString(chunk.length - 6);
	                //Remove NULL terminator
	                materialName = materialName.substring(0, materialName.length() - 1);
	                //Map this material to the material itself
	                materials.put(materialName, new Material());
	                break;
	
	            case 0xA010: //Emissive light color
	            	materials.get(materialName).emissive = getColor(chunk.length - 6);
	                break;
	
	            case 0xA020: //Diffusive light color
	            	materials.get(materialName).diffuse = getColor(chunk.length - 6);
	                break;
	
	            case 0xA030: //Specular light color 
	            	materials.get(materialName).specular = getColor(chunk.length - 6);
	                break;
	
	            case 0xA040: //Shininess
	            	materials.get(materialName).shininess = getPercent(chunk.length - 6);
	                break;
	
	            default:
	                skipBytes(chunk.length - 6);
	                break;
            }
            sectionLength -= chunk.length;
        }
    }

    /**
     * Loads an object from a *.3ds file
     * @param sectionLength The length of this section, in bytes
     */
    private void Load3dsObject(int sectionLength)
    {
        String s = readString();
        Chunk chunk;
        for(sectionLength -= s.length() + 1; sectionLength > 0; sectionLength -= chunk.length)
        {
            chunk = readChunk();
            switch(chunk.id)
	        {
	            case 0x4100: //OBJ_TRIMESH 
	                Load3dsTriangleObject(chunk.length - 6);
	                break;
	
	            default:
	                skipBytes(chunk.length - 6);
                	break;
            }
        }
    }
    
    /**
     * Loads an array of points (vertices) from a *.3ds file
     */
    private void Load3dsPointArray()
    {
        int vertexCount = readBytes(2);
        while(vertexCount > 0){
        	float x = readFloat();
        	float y = readFloat();
        	float z = readFloat();
        	vertices.add(new Point(x, y, z));
        	vertexCount--;
        }
    }

    /**
     * Reads in a triangle object from a *.3ds file
     * @param sectionLength The length of this section in bytes
     */
    private void Load3dsTriangleObject(int sectionLength)
    {
        Chunk chunk;
        while(sectionLength > 0){
            chunk = readChunk();
            switch(chunk.id){
	            case 0x4110: //TRI_VERTEXL
	                Load3dsPointArray();
	                break;
	
	            case 0x4120: //TRI_FACEL1
	                Load3dsFaceArray(chunk.length - 6);
	                break;
	
	            case 0x4140: //Mapping coordinates
	            	Load3dsMappingCoords(chunk.length - 6);
	            	break;
	            	
	            default:
	                skipBytes(chunk.length - 6);
	                break;
            }
            sectionLength -= chunk.length;
        }
    }

    /**
     * Reads in a color from the file
     * @param sectionLength The length of this section in bytes
     * @return The color that was read in
     */
    private Color getColor(int sectionLength)
    {
        float af[] = new float[3];
        Chunk chunk = readChunk();
        switch(chunk.id)
        {
	        case 16:
	        case 18: //The color values are between 0 and 1
	            af[0] = readFloat();
	            af[1] = readFloat();
	            af[2] = readFloat();
	            readBytes(chunk.length - 6 - 12);
	            break;
	
	        case 17:
	        case 19: //The color values are between 0 and 255
	        	//Convert the values to be between 0 and 1
	            af[0] = (float) readBytes(1) / 255F;
	            af[1] = (float) readBytes(1) / 255F;
	            af[2] = (float) readBytes(1) / 255F;
	            readBytes(chunk.length - 6 - 3);
	            break;
	
	        default: //Default to black
	            af[0] = 0.0F;
	            af[1] = 0.0F;
	            af[2] = 0.0F;
	            readBytes(sectionLength - 6);
	            break;
        }
        return new Color(af[0], af[1], af[2]);
    }

    /**
     * Reads a percent (float between 0 and 100)
     * @param sectionLength The length of this section in bytes
     * @return The percent that was read in
     */
    private float getPercent(int sectionLength)
    {
        float f = 0; //Default to 0%
        Chunk chunk = readChunk();
        switch(chunk.id)
        {
	        case 48:
	            f = (float) readBytes(2) / 4095F;
	            break;
	
	        case 49:
	            f = readFloat();
	            break;
	
	        default:
	            readBytes(sectionLength - 6);
	            break;
        }
        return f;
    }

    /**
     * Reads a certain number of bytes from the file
     * @param byteCount The number of bytes to read in
     * @return The bytes read in
     */
    private int readBytes(int byteCount)
    {
	    try{
		    int result = 0;
		    for(int i = 0; i < byteCount; i++){
		      	//3DS files are in little endian format
		        result += reader.readUnsignedByte() << (8 * i);
		    }
		    return result;
	    }catch(IOException e){
	    	e.printStackTrace();
	    	return -1;
	    }
    }

    /**
     * Reads a char from the file
     * @return The char that was read
     */
    private char readChar()
    {
        return (char) readBytes(1);
    }

    /**
     * Reads a chunk from the file
     * @return The chunk that was read
     */
    private Chunk readChunk()
    {
        int i = readBytes(2);
        int j = readBytes(4);
        return new Chunk(i, j);
    }

    /**
     * Reads a single precision float from the file
     * @return The float that was read in
     */
    private float readFloat()
    {
        return Float.intBitsToFloat(readBytes(4));
    }

    /**
     * Reads a null-terminated string from the file
     * @return The string that was read in
     */
    private String readString()
    {
        String s = new String();
        for(int ch = readChar(); ch > 0; ch = readChar()){
            s += (char) ch;
        }
        return s;
    }

    /**
     * Reads in a string from the file 
     * @param length The length of the string to read in
     * @return The string that was read in
     */
    private String readString(int length)
    {
        String s = new String();
        for(int j = 0; j < length; j++){
            s += readChar();
        }
        return s;
    }

    /**
     * Skips bytes in the file
     * @param bytesToSkip The number of bytes to skip
     */
    private void skipBytes(int bytesToSkip)
    {
	    if(bytesToSkip > 0){
	    	try{
	    		reader.skip(bytesToSkip);
	        }catch(IOException e){
	            e.printStackTrace();
	        }
	    }
    }

    private Vector<Face> faces;
    private Vector<UV> mappingCoords;
    private ArrayList<String> materialNames;
    private HashMap<String, Material> materials;
    private DataInputStream reader;
    private Vector<Point> vertices;
    private Mesh mesh;
    private int tid;
    private int groupNumber;
}