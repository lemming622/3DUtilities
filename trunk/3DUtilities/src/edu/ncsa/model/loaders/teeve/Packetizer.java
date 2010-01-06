package edu.ncsa.model.loaders.teeve;
import edu.ncsa.model.MeshAuxiliary.*;
import java.util.*;
import com.jcraft.jzlib.*;

public class Packetizer
{
	/**
   * De-packetize the given buffer to extract point and color information.
   * @param points the list of points that this function will fill
   * @param colors the list of colors that this function will fill
   * @param buffer the buffer containing the data
   * @param buffer_length the used length of the buffer
   */
  public static void depacketize(Vector<Point> points, Vector<Color> colors, byte[] buffer, int buffer_length)
  {
  	boolean compressed = GatewayUtility.byteToInt(buffer[0]) != 0;
  	byte[] point_buffer = null;
  	byte[] color_buffer = null;
  	Point point;
  	Color color;
  	int n, at;
  	
  	points.clear();
  	colors.clear();
  	
  	//System.out.println("Compressed: " + compressed);

  	if(!compressed){
  		int c0;
  		
  		n = buffer_length / (3 + 3*2);	//Number of points based on 3 bytes for color and 2 bytes per coordinate
  		c0 = 1 + n*3*2;
  					
  		point_buffer = GatewayUtility.subArray(buffer, 1, c0-1);
  		color_buffer = GatewayUtility.subArray(buffer, c0, buffer_length-1);
  	}else{
  		byte[] point_buffer_c;
  		byte[] color_buffer_c;
  		int point_buffer_length, point_buffer_length_c;
  		int color_buffer_length, color_buffer_length_c;
  		
  		point_buffer_length_c = GatewayUtility.bytesToInt(buffer[1], buffer[2], buffer[3], buffer[4], false);
  		point_buffer_length = point_buffer_length_c*10;
  		color_buffer_length_c = buffer_length - 1 - 4 - point_buffer_length_c;
  		color_buffer_length = color_buffer_length_c*10;
  		n = point_buffer_length / (3*2);	//Number of points in message
  		
  		point_buffer = new byte[point_buffer_length];
  		point_buffer_c = GatewayUtility.subArray(buffer, 5, 5+point_buffer_length_c-1);
  		color_buffer = new byte[color_buffer_length];	
  		color_buffer_c = GatewayUtility.subArray(buffer, 5+point_buffer_length_c, 5+point_buffer_length_c+color_buffer_length_c-1);
  		
  		//Decompress
  		uncompress(point_buffer_c, point_buffer);
  		uncompress(color_buffer_c, color_buffer);
  	}
  	
  	//Retrieve points
  	at = 0;
  	
  	for(int i=0; i<n; i++){
  		point = new Point();
  		point.x = GatewayUtility.bytesToShort(point_buffer[at], point_buffer[at+1], false);
  		point.y = GatewayUtility.bytesToShort(point_buffer[at+2], point_buffer[at+3], false);
  		point.z = GatewayUtility.bytesToShort(point_buffer[at+4], point_buffer[at+5], false);
  		points.add(point);
  		at += 6;
  	}
  
  	//Retrieve colors
  	at = 0;
  
  	for(int i=0; i<n; i++){
  		color = new Color();
  		color.r = GatewayUtility.byteToInt(color_buffer[at]) / 255f;
  		color.g = GatewayUtility.byteToInt(color_buffer[at+1]) / 255f;
  		color.b = GatewayUtility.byteToInt(color_buffer[at+2]) / 255f;
  		colors.add(color);
  		at += 3;
  	}
  }
  
	/**
   * De-packetize the given buffer to extract vertex, color, and face information.
   * @param vertices the list of vertices that this function will fill
   * @param colors the list of colors that this function will fill
   * @param faces the list of faces that this function will fill
   * @param buffer the buffer containing the data
   * @param buffer_length the used length of the buffer
   */
  public static void depacketize(Vector<Point> vertices, Vector<Color> colors, Vector<Face> faces, byte[] buffer, int buffer_length)
  {
  	boolean compressed = GatewayUtility.byteToInt(buffer[0]) != 0;
  	byte[] vertex_buffer = null;
  	byte[] color_buffer = null;
  	byte[] face_buffer = null;
  	Point point;
  	Color color;
  	Face face;
  	int nv, nf, at;
  	
  	vertices.clear();
  	colors.clear();
  	faces.clear();
  	
  	//System.out.println("Compressed: " + compressed);
  	
  	//Retrieve number of vertices/faces
  	at = 1;
  	nv = GatewayUtility.bytesToInt(buffer[at+0], buffer[at+1], buffer[at+2], buffer[at+3], false);
  	//System.out.println("vertices: " + nv);
  	
  	at = 1 + 4;
  	nf = GatewayUtility.bytesToInt(buffer[at+0], buffer[at+1], buffer[at+2], buffer[at+3], false);
  	//System.out.println("faces: " + nf);
  	
  	//Retrieve buffers
  	if(!compressed){
  		at = 1 + 4 + 4;
  		vertex_buffer = GatewayUtility.subArray(buffer, at, at + nv*3*2-1);
  		
  		at += nv*3*2;
  		color_buffer = GatewayUtility.subArray(buffer, at, at + nv*3-1);
  		
  		at += nv*3;
  		face_buffer = GatewayUtility.subArray(buffer, at, buffer_length-1);
  	}else{
  		byte[] vertex_buffer_c;
  		byte[] color_buffer_c;
  		byte[] face_buffer_c;
  		int vertex_buffer_length, vertex_buffer_length_c;
  		int color_buffer_length, color_buffer_length_c;
  		int face_buffer_length, face_buffer_length_c;
  		
  		//Retrieve buffer sizes
  		at = 1 + 4 + 4;
  		vertex_buffer_length_c = GatewayUtility.bytesToInt(buffer[at+0], buffer[at+1], buffer[at+2], buffer[at+3], false);
  		vertex_buffer_length = vertex_buffer_length_c*10;
  		
  		at += 4;
  		color_buffer_length_c = GatewayUtility.bytesToInt(buffer[at+0], buffer[at+1], buffer[at+2], buffer[at+3], false);
  		color_buffer_length = color_buffer_length_c*10;
  		
  		at += 4;
  		face_buffer_length_c = GatewayUtility.bytesToInt(buffer[at+0], buffer[at+1], buffer[at+2], buffer[at+3], false);
  		face_buffer_length = face_buffer_length_c*10;
  		
  		//Retrieve compressed data buffers
  		at += 4;
  		vertex_buffer = new byte[vertex_buffer_length];
  		vertex_buffer_c = GatewayUtility.subArray(buffer, at, at+vertex_buffer_length_c-1);
  		
  		at += vertex_buffer_length_c;
  		color_buffer = new byte[color_buffer_length];	
  		color_buffer_c = GatewayUtility.subArray(buffer, at, at+color_buffer_length_c-1);
  		
  		at += color_buffer_length_c;
  		face_buffer = new byte[face_buffer_length];	
  		face_buffer_c = GatewayUtility.subArray(buffer, at, at+face_buffer_length_c-1);
  		
  		//Decompress
  		uncompress(vertex_buffer_c, vertex_buffer);
  		uncompress(color_buffer_c, color_buffer);
  		uncompress(face_buffer_c, face_buffer);
  	}
  	
  	//Retrieve vertices
  	at = 0;
  	
  	for(int i=0; i<nv; i++){
  		point = new Point();
  		point.x = GatewayUtility.bytesToShort(vertex_buffer[at], vertex_buffer[at+1], false);
  		point.y = GatewayUtility.bytesToShort(vertex_buffer[at+2], vertex_buffer[at+3], false);
  		point.z = GatewayUtility.bytesToShort(vertex_buffer[at+4], vertex_buffer[at+5], false);
  		vertices.add(point);
  		at += 6;
  	}
  
  	//Retrieve colors
  	at = 0;
  
  	for(int i=0; i<nv; i++){
  		color = new Color();
  		color.r = GatewayUtility.byteToInt(color_buffer[at]) / 255f;
  		color.g = GatewayUtility.byteToInt(color_buffer[at+1]) / 255f;
  		color.b = GatewayUtility.byteToInt(color_buffer[at+2]) / 255f;
  		colors.add(color);
  		at += 3;
  	}
  	
  	//Retrieve faces
  	at = 0;
  	
  	for(int i=0; i<nf; i++){
  		face = new Face(3);
  		face.v[0] = GatewayUtility.bytesToShort(face_buffer[at], face_buffer[at+1], false);
  		face.v[1] = GatewayUtility.bytesToShort(face_buffer[at+2], face_buffer[at+3], false);
  		face.v[2] = GatewayUtility.bytesToShort(face_buffer[at+4], face_buffer[at+5], false);
  		face.normal = new Point(0, 0, -1);
  		faces.add(face);
  		at += 6;
  	}
  }
  
  /**
   * A wrapper for jzlib's uncompression.
   * @param compressed_data the buffer containing the compressed data
   * @param uncompressed_data the buffer containing the uncompressed data
   */
  private static void uncompress(byte[] compressed_data, byte[] uncompressed_data)
  {
		ZStream zstream = new ZStream();
		zstream.next_in = compressed_data;
		zstream.next_in_index = 0;
		zstream.next_out = uncompressed_data;
		zstream.next_out_index = 0;
		zstream.inflateInit();
		
		while(zstream.total_out < uncompressed_data.length && zstream.total_in<compressed_data.length){
			zstream.avail_in = zstream.avail_out = 1;		//Force small buffers
			zstream.inflate(JZlib.Z_NO_FLUSH);
		}
		
		zstream.inflateEnd();
  }
}