package edu.ncsa.model.loaders;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import edu.ncsa.model.Mesh;
import edu.ncsa.model.MeshLoader;

public class MeshLoader_MP4 extends MeshLoader
{
	DataInputStream reader = null;
	
	public String type()
	{
		return "mp4";
	}
	
	public Mesh load(String filename)
	{
		Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
		try{
			reader = new DataInputStream(new FileInputStream(filename));
		}catch(FileNotFoundException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(readBytes(4));
		
		int workingBytes2; 
		

		int streamsize = readBytes(4);
		System.out.println("streamsize is " + streamsize);
		workingBytes = readBytes(1);
		int encodingmode = workingBytes >>5;
		System.out.println("encoding mode is " + encodingmode);
		workingBytes2 = readBytes(4);
		
		float creaseAngle = Float.intBitsToFloat((workingBytes & 0x1F)<<27 + workingBytes2>>5);
		System.out.println("Crease angle is " + creaseAngle);
		boolean ccw = (workingBytes2 & 0x10) != 0;
		System.out.println("ccw is " + ccw);
		boolean solid = (workingBytes2 & 0x08) != 0;
		System.out.println("solid is " + solid);
		boolean convex = (workingBytes2 & 0x04) != 0;
		System.out.println("convex is " + convex);
		boolean colorPerVertex = (workingBytes2 & 0x02) != 0;
		System.out.println("color per vertex is " + colorPerVertex);
		boolean normalPerVertex = (workingBytes2 & 0x01) != 0;
		System.out.println("normal per vertex is " + normalPerVertex);
		
		workingBytes = readBytes(1);
		boolean otherAttributesPerVertex = (workingBytes & 0x80) != 0;
		System.out.println("otherAttributesPerVertex is " + otherAttributesPerVertex);
		boolean isTriangularMesh = (workingBytes & 0x40) != 0;
		System.out.println("is Triangular Mesh is " + isTriangularMesh);
		boolean vertexOrderPres = (workingBytes & 0x20) != 0;
		System.out.println("vertex order preserved is " + vertexOrderPres);
		boolean triangleOrderPres = (workingBytes & 0x10) != 0;
		System.out.println("triangleOrderPres is " + triangleOrderPres);
		
		workingBytes2 = readBytes(4);
		int numberOfCoord = (workingBytes << 28) + workingBytes2 >>4;
		System.out.println("number of coordinates: " + numberOfCoord);
		
		workingBytes = readBytes(4);
		int numberOfNormal = (workingBytes2 << 28) + workingBytes >>4;
		System.out.println("number of normals: " + numberOfNormal);
		
		workingBytes2 = readBytes(4);
		int numberOfTexCoord = (workingBytes << 28) + workingBytes2 >>4;
		System.out.println("number of  texture coordinates: " + numberOfTexCoord);
		
		workingBytes = readBytes(4);
		int numberOfColor = (workingBytes2 << 28) + workingBytes >>4;
		System.out.println("number of colors: " + numberOfColor);
		workingBytes2 = readBytes(4);
		int numberOfOtherAttributes = (workingBytes << 28) + workingBytes2 >>4;
		System.out.println("number of other attributes: " + numberOfOtherAttributes);
		
		int placeinbyte = 28;
		workingBytes = workingBytes2;
		int dimensionOfOtherAttributes;
		if(numberOfOtherAttributes >0){
			dimensionOfOtherAttributes = readInt(placeinbyte);
		}
		return null;
	}
	
	public boolean save(String arg0, Mesh arg1)
	{
		// TODO Auto-generated method stub
		return false;
	}



	private int readBytes(int byteCount)
  {
    try{
	    int result = 0;
	    for(int i = 0; i < byteCount; i++){
	      	//3DS files are in little endian format
	        result += reader.readUnsignedByte() << (8 * (byteCount-i-1));
	    }
	    return result;
    }catch(EOFException e){
    	
    	return -1;
    }catch(IOException io){
    	io.printStackTrace();
    	return -1;
    }
  
  }
	private int workingBytes;
	private int readInt(int offset){
		int read = workingBytes<<offset;
		workingBytes = readBytes(4);
		read += workingBytes >>(32-offset);
		return read;
	}
	private int readBits(int numbits, int offset){
		int mask = 0;
		if(numbits + offset <32){
			for(int i = 0; i<numbits; i++){
				mask = (mask<<1) + 1;
			}
			return ((workingBytes >> (32-offset+numbits))&mask);
		}
		else{
			for(int i = 0; i<32-offset; i++){
				mask = (mask<<1) + 1;
			}
			int numleft = numbits-(32-offset);
			int part1 = (workingBytes>>(32-offset))& mask;
			part1 = part1<< numleft;
			
			mask = 0;
			for(int i = 0; i<numleft; i++){
				mask = (mask<<1) + 1;
			}
			workingBytes = readBytes(4);
			return part1 + ((workingBytes >> (32-numleft))&mask);
		}
	}
}
