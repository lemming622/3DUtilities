package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.iob files.
 *  @author Daniel Long
 */
public class MeshLoader_IOB extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "iob";
	}
	
	/**
	 * Load an IOB model.
	 *  @param filename the file to load
	 *  @return the loaded mesh
	 */
	public Mesh load(String filename)
	{
		try{
			reader = new DataInputStream(new FileInputStream(filename));
			vertices = new Vector<Point>();
			faces = new Vector<Face>();
			edges = new Vector<Edge>();
			readBytes(12);
			mainCall(1);
			
			//The items in faces refer to edges which in turn refer to
			//vertices. We now replace these edge references with the 
			//vertex indices.
			for(int i = 0; i < faces.size(); i++){
				//Each vertex index will be repeated, so to
				//get three unique indexes we put them in an array,
				//sort the array, and grab indexes 0, 2, and 4
				int points[] = new int[6];
				points[0] = edges.get(faces.get(i).v[0]).v0;
				points[1] = edges.get(faces.get(i).v[0]).v1;
				points[2] = edges.get(faces.get(i).v[1]).v0;
				points[3] = edges.get(faces.get(i).v[1]).v1;
				points[4] = edges.get(faces.get(i).v[2]).v0;
				points[5] = edges.get(faces.get(i).v[2]).v1;
				Arrays.sort(points);
				faces.set(i, new Face(points[0], points[2], points[4]));
			}
		   
			Mesh mesh = new Mesh();
			mesh.setVertices(vertices);
			mesh.setFaces(faces);
			mesh.addFileMetaData(filename);
			mesh.initialize();
			return mesh;
		}catch(FileNotFoundException e){
			e.printStackTrace();
			return null;
		}
	}
  
	public Mesh load(InputStream is) {return null;}
	public boolean save(String filename, Mesh mesh) {return false;}

	private int descCall(int a, int b)
	{
		while(a > 0){
			IOBChunk chunk = readChunk();
			int length = 0;
			if(chunk.name.equals("COLR") || chunk.name.equals("TRAN")){
					readColor();
					length = chunk.length;
			}else if(chunk.name.equals("EDGE")){
					int edgeCount = readBytes(2);
					for(int i = 0; i < edgeCount; i++){
						edges.add(new Edge(readBytes(2), readBytes(2)));
					}
					length = chunk.length;
			}else if(chunk.name.equals("FACE")){
					int faceCount = readBytes(2);
					for(int i = 0; i < faceCount; i++){
						faces.add(new Face(readBytes(2), readBytes(2), readBytes(2)));
					}
					length = chunk.length;
			}else if(chunk.name.equals("PNTS")){
					int pointCount = readBytes(2);
					for(int i = 0; i < pointCount; i++){
						vertices.add(new Point(readVertex()));
					}
					length = chunk.length;
			}
			if(chunk.length - length > 0){
				readBytes(chunk.length - length);
			}
			a -= chunk.length + 8;
			if(chunk.length % 2 == 1){
				readBytes(1);
				a--;
			}
		}
		return a;
	}

	private int mainCall(int length)
	{
		while(length > 0){
			IOBChunk chunk = readChunk();
			if(chunk.name.equals("OBJ ")){
				objCall(chunk.length);
			}else{
				readBytes(chunk.length);
			}
			length -= chunk.length + 8;
			if(chunk.length % 2 == 1){
				readBytes(1);
				length--;
			}
		}
		return length;
	}

	private int objCall(int length)
	{
		while(length > 0){
			IOBChunk chunk = readChunk();
			if(chunk.name.equals("DESC")){
				descCall(chunk.length, objID++);
			}else{
				readBytes(chunk.length);
			}
			length -= chunk.length + 8;
			if(chunk.length % 2 == 1){
				readBytes(1);
				length--;
			}
		}
		return length;
	}

	/**
	 * Reads bytes from the file
	 * @param bytesToRead The number of bytes to read in
	 * @return The bytes that were read in
	 */
	private int readBytes(int bytesToRead)
	{
		int result = 0;
		for(int i = 0; i < bytesToRead; i++){
			try{
				result = reader.readUnsignedByte() + (result << 8);
			}catch(IOException ioexception){
				System.err.println("Error with file.\n" + ioexception.toString());
				System.exit(1);
			}
		}
		return result;
	}

	/**
	 * Reads a char from the file
	 * @return The char that was read in
	 */
	private char readChar()
	{
		return (char) readBytes(1);
	}

	/**
	 * Reads a chunk from the file
	 * @return The chunk that was read in
	 */
	private IOBChunk readChunk()
	{
		return new IOBChunk(readString(4), readBytes(4));
	}

	/**
	 * Reads a color from the file
	 * @return The color that was read in
	 */
	private Color readColor()
	{
		readBytes(1);
		return new Color(readBytes(1), readBytes(1), readBytes(1));
	}

	/**
	 * Reads a vertex from the file
	 * @return The vertex that was read in
	 */
	private Point readVertex()
	{
		return new Point(readBytes(4) / 65536.0, readBytes(4) / 65536.0, readBytes(4) / 65536.0);
	}

	/**
	 * Reads a string from the file
	 * @param length The length of the string to be read in
	 * @return The string that was read in
	 */
	private String readString(int length)
	{
		String s = "";
		for(int i = 0; i < length; i++){
			s += readChar();
		}
		return s;
	}

	private static int objID = 0;
	
	private DataInputStream reader;
	private Vector<Point> vertices;
	private Vector<Face> faces;
	private Vector<Edge> edges;
}

class IOBChunk
{
	public IOBChunk(String name_, int length_)
	{
		name = name_;
	    length = length_;
	}

	public String name;
	public int length;
}