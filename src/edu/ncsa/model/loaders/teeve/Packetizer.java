package edu.ncsa.model.loaders.teeve;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

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
  	int n = 0;
  	
  	//System.out.println("Compressed: " + compressed);
  	
  	points.clear();
  	colors.clear();	
  	
  	if(!compressed){
  		n = buffer_length/(3 + 3*2);	//3 bytes for color, 2 bytes per coordinate
  		int c0 = 1 + n*3*2;
  					
  		point_buffer = GatewayUtility.subArray(buffer, 1, c0-1);
  		color_buffer = GatewayUtility.subArray(buffer, c0, buffer_length-1);
  	}else{
  		int point_buffer_length_c = GatewayUtility.bytesToInt(buffer[1], buffer[2], buffer[3], buffer[4], false);
  		int point_buffer_length = point_buffer_length_c*10;
  		int color_buffer_length_c = buffer_length - 1 - 4 - point_buffer_length_c;
  		int color_buffer_length = color_buffer_length_c*10;
  					  		
  		point_buffer = new byte[point_buffer_length];
  		byte[] point_buffer_c = GatewayUtility.subArray(buffer, 5, 5+point_buffer_length_c-1);
  		color_buffer = new byte[color_buffer_length];	
  		byte[] color_buffer_c = GatewayUtility.subArray(buffer, 5+point_buffer_length_c, 5+point_buffer_length_c+color_buffer_length_c-1);
  		
  		//Decompress
  		if(true){
	  		Deflater deflater = new Deflater();
	  		deflater.setInput(point_buffer_c);
	  		deflater.finish();
	  		deflater.deflate(point_buffer);
	  		
	  		deflater = new Deflater();
	  		deflater.setInput(color_buffer_c);
	  		deflater.finish();
	  		deflater.deflate(color_buffer);
  		}else{
  			try{
  				GZIPInputStream ins;
  				
  				ins = new GZIPInputStream(new ByteArrayInputStream(point_buffer_c));
  				ins.read(point_buffer, 0, point_buffer_length);
  				ins.close();
  				
  				ins = new GZIPInputStream(new ByteArrayInputStream(color_buffer_c));
  				ins.read(color_buffer, 0, color_buffer_length);
  				ins.close();
  			}catch(Exception e) {e.printStackTrace();}
  		}
  					
  		//Calculate number of points in message
  		n = point_buffer_length / (3*2);
  	}
  
  	Point point;
  	Color color;
  	int at = 0;
  	
  	for(int i=0; i<n; i++){
  		point = new Point();
  		point.x = GatewayUtility.bytesToShort(point_buffer[at], point_buffer[at+1], false);
  		point.y = GatewayUtility.bytesToShort(point_buffer[at+2], point_buffer[at+3], false);
  		point.z = GatewayUtility.bytesToShort(point_buffer[at+4], point_buffer[at+5], false);
  		points.add(point);
  		at += 6;
  	}
  
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
}