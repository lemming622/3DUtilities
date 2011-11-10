package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;
import edu.ncsa.model.loaders.u3d.*;

/**
 * A mesh file loader for *.u3d files.
 * Can load the vertices for a file written by Adobe PDF/3D reviewer. 
 * does not load faces or vertices from other programs 
 * BitStreamRead and ContextManager classes are responsible for decompression 
 * and can be found in model.loaders.u3d
 * 
 * Specifications can be found in model.loaders.u3d
 * 
 *  @author Victoria Winner
 */
public class MeshLoader_U3D extends MeshLoader
{
	private DataInputStream reader;
	private BitStreamRead bitreader;
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "u3d";
	}
	
	/**
	 * Load a Universal-3D model.
	 *  @param filename the file to load
	 *  @return the loaded mesh
	 */
	public Mesh load(String filename)
	{
		Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
		try{
			reader = new DataInputStream(new FileInputStream(filename));
			readHeader();
			int next_block=0;
			int block_size=0;
			int meta_size=0;
			Vector<Point> vertices;
			while((next_block = readBytes(4)) != -1){
				
				System.out.println("next block is " + next_block);
				block_size = readBytes(4);
				meta_size = readBytes(4);
				System.out.println("data: " + block_size + "; metadata: " + meta_size);
				if(next_block == 0xFFFFFF3B){
					bitreader = new BitStreamRead();
					int data_length = block_size/4;
					if(block_size%4 !=0)data_length++;
					bitreader.data = new int [data_length + 2];
					for (int i=0; i<data_length; i++){
						bitreader.data[i] = readBytes(4);
					}
					System.out.println("array size: " + data_length + " or " + bitreader.data.length);
					bitreader.dataBitOffset = 0;
					bitreader.dataPosition = 0;
					bitreader.GetLocal();
					String name = "";
					int name_length = bitreader.ReadU16();
					System.out.println("name is " + name_length + "B");
					for(int i = 0; i<name_length; i++){
						name += (char)bitreader.ReadU8();
					}
					System.out.println("Mesh name is " + name);
					bitreader.ReadU32();
					int num_faces = bitreader.ReadU32();
					int num_points = bitreader.ReadU32();
					int num_normals = bitreader.ReadU32();
					int num_diff = bitreader.ReadU32();
					int num_spec = bitreader.ReadU32();
					int num_tex = bitreader.ReadU32();
					System.out.println("Faces: " + num_faces + "; Points: " + num_points);
					vertices = new Vector<Point>();
					for(int i = 0; i<num_points; i++){
						Point p = new Point(bitreader.ReadF32(), bitreader.ReadF32(), bitreader.ReadF32());
						vertices.add(p);
					}

					for(int i = 0; i<num_normals*3; i++){
						bitreader.ReadU32();
					}
					for(int i = 0; i<(num_spec+num_diff+num_tex)*4; i++){
						bitreader.ReadU32();
					}

					
					
					/**
					 * This section was meant to read the faces from the file, however the compression algorithm
					 * in the documentation is not clear enough that i was ever able to make it work successfully. 
					 */
					//i<4 correctly loads 4 faces(2 sides) of the cube
//					for(int i = 0; i<6; i++){
//						
//						int throwaway = bitreader.ReadCompressedU32(1);
//						System.out.println("compressed stuff was... " + throwaway);
//						Face f = new Face(3);
//					
//						for(int j = 0; j<3; j++){
//							System.out.println("on face #" + i + "; j=" + j);
//							
//							
//							f.v[j] = bitreader.ReadCompressedU32(0x400 + num_points);
//							if(num_normals != 0){
//								bitreader.ReadCompressedU32(0x400 + num_normals);
//							}
//							if(num_diff != 0){
//								bitreader.ReadCompressedU32(0x400 + num_diff);
//							}
//							if(num_spec != 0){
//								bitreader.ReadCompressedU32(0x400 + num_spec);
//							}
//							if(num_tex != 0){
//								bitreader.ReadCompressedU32(0x400 + num_tex);
//								
//
//							}
//							
//						}
//						
//						faces.add(f);
//					}
				
					mesh.addData(vertices, new Vector<Face>(), -1, name);
				}
				else{
					int x = block_size + (4-(block_size%4))%4 + meta_size + (4-(meta_size%4))%4;
					System.out.println("skipping " + x + "bytes");
					reader.skip(x);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		mesh.initialize();
		return mesh;
	}

	public boolean save(String filename, Mesh mesh) {return false;}
	private int readBytes(int byteCount)
    {
	    try{
		    int result = 0;
		    for(int i = 0; i < byteCount; i++){
		      	//3DS files are in little endian format
		        result += reader.readUnsignedByte() << (8 * i);
		    }
		    return result;
	    }catch(EOFException e){
	    	
	    	return -1;
	    }catch(IOException io){
	    	io.printStackTrace();
	    	return -1;
	    }
    
    }
	
	private void readHeader(){
	try{	
		int blocktype = readBytes(4);
		System.out.println("block type:" + blocktype);
		int blocksize = readBytes(4);
		int metasize = readBytes(4);
		System.out.println("data size: " + blocksize);
		System.out.println("meta-data size: " + metasize);
		int version_major = readBytes(2);
		int version_minor = readBytes(2);
		System.out.println("version number " + version_major + "." + version_minor);
		int pid = readBytes(4);
		if(pid == 0) System.out.println("Base profile; no optional features used");
		else{
			if((pid & 0x00000002) != 0) System.out.println("Extensible profile; uses extensibility features");
			if((pid & 0x00000004) != 0) System.out.println("No compression mode");
			if((pid & 0x00000008) != 0) System.out.println("Defined Units");
		}
		int dec_size = readBytes(4);
		System.out.println("Size of Header and Declaration blocks: " + dec_size + "B");
		long file_size = (readBytes(4)<<32) + readBytes(4); 
		System.out.println("Size of File: " + file_size + "B");
		int char_enc = readBytes(4);
		System.out.println("uses encoding: " + char_enc);
		if((pid & 0x00000008) != 0){
			double scaling = (readBytes(4)<<32) + readBytes(4);
			System.out.println("Has scaling factor of " + scaling);
		}
		int x = metasize + (4-(metasize%4))%4;
		System.out.println("skipping " + x + "bytes");
		reader.skip(x);
	}catch(IOException e){
    	
    	e.printStackTrace();
    	
    }
	}
}

