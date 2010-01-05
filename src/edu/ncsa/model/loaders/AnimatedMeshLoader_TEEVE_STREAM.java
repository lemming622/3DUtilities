package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.model.loaders.teeve.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

/**
 * An animated mesh loader for *.teeve_stream connections.
 * @author Kenton McHenry
 */
public class AnimatedMeshLoader_TEEVE_STREAM extends AnimatedMeshLoader
{ 
	private String server = null;
	private int port;
	private GatewayConnection connection;
	private int initial_capacity = 100000;
	
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "teeve_stream";
	}
	
	 /**
   * Load a *.teeve_stream animated model.
   *  @param filename the file with the connection to load
   *  @return the loaded animated mesh
   */
	public AnimatedMesh load(String filename)
	{
		AnimatedMeshLoader_TEEVE_STREAM animation_loader = new AnimatedMeshLoader_TEEVE_STREAM();	//Create an instance of this loader to become apart of the returned animated mesh.
		Scanner ins;
		String token;
		
		try{	//Load connection information from file
			ins = new Scanner(new File(filename));
			
			while(ins.hasNext()){
				token = ins.next();
				
				if(token.equals("server")){
					animation_loader.server = ins.next();
				}else if(token.equals("port")){
					animation_loader.port = ins.nextInt();				
				}
			}
			
			ins.close();
		}catch(Exception e) {e.printStackTrace();}
				
		//Open connection
  	animation_loader.connection = new GatewayConnection(animation_loader.server, animation_loader.server, animation_loader.server, animation_loader.server, animation_loader.port);
		animation_loader.connection.readFrames();		
  	
		return new AnimatedMesh(animation_loader);
	}
	
	/**
	 * Get the length of the animation.
	 */
	public double length()
	{
		return -1;
	}
	
	/**
	 * Retrieve the next mesh.
	 * @return the next mesh
	 */
	public Mesh getMesh()
	{		
  	Mesh mesh = new Mesh();
  	Vector<Point> vertices = new Vector<Point>(initial_capacity);
  	Vector<Color> colors = new Vector<Color>(initial_capacity);
  	Vector<Face> faces = new Vector<Face>(initial_capacity);
  	byte[] buffer;
  	
  	buffer = connection.getBuffer();
  	
  	if(buffer != null){
  		//Packetizer.depacketize(vertices, colors, buffer, buffer.length);
  		Packetizer.depacketize(vertices, colors, faces, buffer, buffer.length);
  	}
  	
    //Transfer loaded data to a mesh and initialize auxiliary data structures
    mesh.setVertices(vertices);
    mesh.setVertexColors(colors);
    mesh.setFaces(faces);
    mesh.initialize();
  	
  	return mesh;
	}
	
	public Mesh getMesh(double t) {return null;}
	public boolean save(String filename, AnimatedMesh mesh) {return false;}
	
	/**
	 * A simple main for debug purposes.
	 * @param args command line arguments
	 */
	public static void main(String args[])
	{
		//Test gateway session
		GatewayConnection connection = new GatewayConnection("starbuck", "starbuck", "starbuck", "starbuck", 30);
		byte[] buffer;
    ModelViewer mv = new ModelViewer("ModelViewer.ini", 0, 0, false, false); mv.AUTO_REFRESH = true; mv.ADJUST = false;
		Vector<Point> vertices;
		Vector<Color> colors;
		Vector<Face> faces;
		Point center = null;
		double radius = 1;
		double scale = 200;
		long t0 = System.currentTimeMillis();
		long t1;
		int fps = 0;
		
    if(mv != null){
	    JFrame frame = new JFrame("Model Viewer");
	    frame.setSize(mv.width+9, mv.height+35);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.add(mv);
	    frame.setVisible(true);
    }
		
		connection.readFrames();
		
		while(true){
			buffer = connection.getBuffer();
			
			if(buffer != null){
				vertices = new Vector<Point>();
				colors = new Vector<Color>();
				faces = new Vector<Face>();
				
	  		//Packetizer.depacketize(vertices, colors, buffer, buffer.length);
	  		Packetizer.depacketize(vertices, colors, faces, buffer, buffer.length);

	  		if(true){		//Center and scale points
	  			if(center == null){
	  				center = Point.getCentroid(vertices);
	  				radius = Point.getRadius(vertices, center);
	  			}
	  			
	  			vertices = (Point.transform(vertices, center, radius/scale, null));
	  		}
	  		
				//System.out.println("Points: " + points.size() + ", Colors: " + colors.size());
				
	  		if(mv != null){
	  			Mesh mesh = new Mesh();
	  			mesh.setData(vertices, colors, faces, false);
					mv.setMesh(mesh);
				}
			}
		
			if(true){		//Display frames per second
				t1 = System.currentTimeMillis();
				fps++;
				
				if(t1-t0 > 1000){
					//System.out.println("FPS: " + fps);
					t0 = t1;
					fps = 0;
				}
			}
		}
	}
}