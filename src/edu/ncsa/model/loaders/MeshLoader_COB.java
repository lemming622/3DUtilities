package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.cob files.
 *  @author Daniel Long
 */
public class MeshLoader_COB extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "cob";
	}
	
	/**
	 * Load an cob model.
	 *  @param filename the file to load
	 *  @return the loaded mesh
	 */
	public Mesh load(String filename)
	{
		try{
    		reader = new DataInputStream(new FileInputStream(filename));
    	}catch(FileNotFoundException e){
    		System.err.println("Unable to open file \"" + filename + "\"");
    		return null;
    	}
    	mesh = new Mesh();
    	materialMap = new HashMap<Integer, Color>();
    	
    	//Read in the faces and material
        readHeader();
        for(Chunk chunk = readChunk(); (chunk.type.equals("END ") == false) && (chunk.id >= 0); chunk = readChunk()){
            if(chunk.type.equals("PolH")){
            	readPolH();
            }else if(chunk.type.equals("Mat1")){
                readMaterial(chunk.pid, chunk.length);
            }else if(binaryMode == true){
            	skipBytes(chunk.length);
            }else{
                skipBytes(chunk.length - 1);
            }
        }
        
        //Add material to faces
        for(int i = 0; i < mesh.getFaces().size(); i++){
        	if(mesh.getFace(i).material == null) mesh.getFace(i).material = new Material();
        	mesh.getFace(i).material.diffuse = materialMap.get(mesh.getFace(i).material.tid);
        }

				mesh.addFileMetaData(filename);
				mesh.initialize();
				
        return mesh;
	}
  
	public Mesh load(InputStream is) {return null;}
	public boolean save(String filename, Mesh mesh) {return false;}

    /**
     * Reads in the 32 byte header of the COB file to determine whether it
     * is in binary or ASCII mode
     */
    private void readHeader()
    {
        readBytes(15);
        if(readChar() == 'B'){
        	binaryMode = true;
        }else{
        	binaryMode = false;
        }
        readBytes(16);
    }
    
    /**
     * Reads in bytes from the COB file in little endian format
     * @param byteCount The number of bytes to read in
     * @return The bytes that were read in
     */
    public int readBytes(int byteCount)
    {
    	try{
    		int result = 0;
 		    for(int i = 0; i < byteCount; i++){
 		        result += reader.readUnsignedByte() << (8 * i);
 		    }
 		    return result;
 	    }catch(IOException e){
 	    	e.printStackTrace();
 	    	return -1;
 	    }
    }

     /**
      * Reads in a char from the COB file
      * @return The char
      */
     public char readChar()
     {
         return (char) readBytes(1);
     }

     /**
      * Reads in a "chunk" from the COB file
      * @return The chunk
      */
     public Chunk readChunk()
     {
         Chunk chunk = new Chunk();
         if(binaryMode == true){
             chunk.type = readString(4);
             skipBytes(4);
             chunk.id = readBytes(4);
             chunk.pid = readBytes(4);
             chunk.length = readBytes(4);
         }else{
             chunk.type = readString(4);
             readString();
             readString();
             chunk.id = Integer.parseInt(readString());
             readString();
             chunk.pid = Integer.parseInt(readString());
             readString();
             chunk.length = Integer.parseInt(readString());
         }
         return chunk;
     }
    
     /**
      * Reads in a float from the COB file
      * @return The float
      */
     public float readFloat()
     {
         return Float.intBitsToFloat(readBytes(4));
     }

     /**
      * Reads in material data from the COB file
      * @param i ???
      * @param sectionLength The length of this section, in bytes
      */
     public void readMaterial(int i, int sectionLength)
     {
         float r;
         float g;
         float b;
         int materialID;
         if(binaryMode == true){
             materialID = readBytes(2) + i;
             skipBytes(3);
             r = readFloat();
             g = readFloat();
             b = readFloat();
             skipBytes(sectionLength - 17); //Skip remaining bytes
         }else{
             int l = 61;
             readString();
             String s = readString();
             l += s.length();
             materialID = Integer.parseInt(s) + i;
             readString();
             l += readString().length();
             readString();
             l += readString().length();
             readString();
             s = readToChar(',', false);
             l += s.length();
             r = Float.parseFloat(s);
             s = readToChar(',', false);
             l += s.length();
             g = Float.parseFloat(s);
             s = readToChar(',', false);
             l += s.length();
             b = Float.parseFloat(s);
             skipBytes(sectionLength - l);
         }
         materialMap.put(materialID, new Color(r, g, b));
     }

     /**
      * Reads a polyhedron from the COB file
      */
     public void readPolH()
     {
         Vector<Point> vertices = new Vector<Point>();
         Vector<Face> faces = new Vector<Face>();
         String name = "";
         if(binaryMode == true){
        	 skipBytes(2); //dupCount
             name = readString(readBytes(2));
             //Skip next 24 floats
             //First 3 floats give the center
             //Next 9 floats give the axes
             //Remaining 12 floats give CurrentPos
             skipBytes(24 * 4);
             
             int vertexCount = readBytes(4);
             for(int i = 0; i < vertexCount; i++){
            	 float x = readFloat();
            	 float y = readFloat();
            	 float z = readFloat();
            	 vertices.add(new Point(x, y, z));
             }

             skipBytes(8 * readBytes(4));

             int faceCount = readBytes(4);
             for(int i = 0; i < faceCount; i++){
                 int l1 = readBytes(1);
                 if((l1 & 8) != 8){
                     int verticesInFace = readBytes(2);
                     int materialID = readBytes(2); //Material
                     ArrayList<Integer> al = new ArrayList<Integer>();
                     for(int j = 0; j < verticesInFace; j++){
                    	 int v = readBytes(4);
                    	 al.add(v);
                         skipBytes(4);
                     }
                     Face face = new Face(al);
                     face.material = new Material(materialID); //We don't have textures, so we'll store the material IDs here
                     faces.add(face);
                 }else{
                	 skipBytes(8 * readBytes(2));
                 }
             }
         }else{
             readString();
             String s;
             for(s = readToChar(',', true); s.charAt(s.length() - 1) == ' '; s = s + readToChar(',', true));
             if(s.charAt(s.length() - 1) == ','){
            	 readString(); //dupCount
             }
             name = s.substring(0, s.length() - 1);

             //Skip unneeded data, including center
             //and axes
             for(int i = 0; i < 38; i++){
            	 readString();
             }
             
             int vertexCount = Integer.parseInt(readString());
             for(int i = 0; i < vertexCount; i++){
            	 vertices.add(new Point(Float.parseFloat(readString()), Float.parseFloat(readString()), Float.parseFloat(readString())));
             }

             readString();
             readString();
             int linesToSkip = Integer.parseInt(readString());
             for(int i = 0; i < linesToSkip; i++)
             {
                 readString();
                 readString();
             }
             readString();
             
             int faceCount = Integer.parseInt(readString());
             for(int i = 0; i < faceCount; i++){
                 if(readString().equals("Face")){
                     readString();
                     int verticesInFace = Integer.parseInt(readString());
                     readString();
                     readString();
                     readString();
                     
                     int materialID = Integer.parseInt(readString());
                     ArrayList<Integer> al = new ArrayList<Integer>();
                     for(int j = 0; j < verticesInFace; j++){
                         readToChar('<', false);
                         al.add(Integer.parseInt(readToChar(',', false)));
                         readString();
                     }
                     Face face = new Face(al);
                     face.material = new Material(materialID);
                     faces.add(face);
                 }else{
                     readString();
                     int toSkip = Integer.parseInt(readString());
                     for(int j = 0; j < toSkip; j++){
                         readString();
                     }
                 }
             }
             skipBytes(1);
         }
         
         mesh.addData(vertices, faces, -1, name);
     }

     /**
      * Reads in a string from the file
      * @return The string that was read in
      */
     public String readString()
     {
         String result = "";
         int i;
         for(i = readChar(); i <= 32; i = readChar());
         for(; i > 32 && i < 256; i = readChar()){
             result += (char) i;
         }
         return result;
     }

     /**
      * Reads in a string from the file
      * @param length The length of the string to read in
      * @return The string that was read in
      */
     public String readString(int length)
     {
         String result = "";
         for(int i = 0; i < length; i++)
             result += readChar();
         return result;
     }

     /**
      * Reads in a string from the file and converts it to a float
      * @param delimiter The character indicating the end of the string
      * @return The converted string
      */
     public float readStringToFloat(char delimiter)
     {
    	 return Float.parseFloat(readToChar(delimiter, false));
     }

     /**
      * Reads in a string from the file until the delimiter is found.
      * @param delimiter The character indicating the end of the string
      * @param includeLastCharacter Whether or not the last character should
      * be included in the resulting string
      * @return The string that was read in
      */
     public String readToChar(char delimiter, boolean includeLastCharacter)
     {
         String result = "";
         int i;
         for(i = readChar(); i <= 32; i = readChar());
         for(; (i > 32) && (i < 256) && (i != delimiter); i = readChar()){
             result += (char) i;
         }
         if(includeLastCharacter == true){
             result += (char) i;
         }
         return result;
     }

     /**
      * If bytesToSkip is positive, skips that many bytes in reader. If 
      * bytesToSkip is not positive, this method has no effect.
      * @param bytesToSkip The number of bytes to skip in reader
      */
     public void skipBytes(int bytesToSkip)
     {
    	 if(bytesToSkip > 0){
    		 try{
    			 reader.skip(bytesToSkip);
    		 }catch(IOException e){
    			 e.printStackTrace();
    		 }
    	 }
     }
     
     private boolean binaryMode;
     private HashMap<Integer, Color> materialMap;
     private Mesh mesh;
     private DataInputStream reader;
}

class Chunk
{
    public String type;
    public int id;
    public int pid;
    public int length;
}