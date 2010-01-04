package edu.ncsa.model.loaders.dwg;
import java.io.*;
import java.util.*;

/**
 * A class to conventiently access data within binary files.
 *  @author Kenton McHenry
 */
public class BinaryFile
{
	private byte[] bytes;
	
	/**
	 * Class constructor.
	 *  @param filename the file to load
	 */
	public BinaryFile(String filename)
	{
		DataInputStream dis = null;
		Vector<Byte> tmpv = new Vector<Byte>();
		
		try{
			dis = new DataInputStream(new FileInputStream(filename));
			
			while(true){
				tmpv.add(dis.readByte());
			}
		}catch(EOFException eof){
		}catch(Exception e) {e.printStackTrace();}
		
		try{
			dis.close();
		}catch(Exception e) {e.printStackTrace();}
		
		//Store data as an array of bytes
		bytes = new byte[tmpv.size()];
		
		for(int i=0; i<tmpv.size(); i++){
			bytes[i] = tmpv.get(i);
		}
	}
	
	/**
	 * Treat the bytes as characters and return the string between the given indices.
	 *  @param i0 the starting index (inclusive)
	 *  @param i1 the ending index (inclusive)
	 *  @return the string within the given indices
	 */
	public String getString(int i0, int i1)
	{
		String str = "";
		
		for(int i=i0; i<=i1; i++){
		  str += (char)bytes[i];	
		}
		
		return str;
	}
	
	/**
	 * Get the block of data between the given indices.
	 *  @param i0 the starting index (inclusive)
	 *  @param i1 the ending index (inclusive)
	 *  @return the block of data within the given indices
	 */
	public byte[] getBlock(int i0, int i1)
	{
		int n = i1 - i0 + 1;
		byte[] data = new byte[n];
		
		for(int i=0; i<n; i++){
		  data[i] = bytes[i0+i];	
		}
		
		return data;
	}
}