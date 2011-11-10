package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * A mesh file loader for *.kmz files.
 *  @author Daniel Long
 */
public class MeshLoader_KMZ extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "kmz";
	}
	
	/**
	 * Load a Google Earth (KMZ) model.
	 *  @param filename the file to load
	 *  @return the loaded mesh
	 */
	public Mesh load(String filename)
	{
		Mesh mesh = new Mesh();
		mesh.addFileMetaData(filename);
		
		try{
			//A kmz file is a collection of files zipped up
			ZipFile zf = new ZipFile(filename);
			
			//Create a temporary folder to unzip the contents of the directory to
			File tmp = File.createTempFile("ModelViewer_KMZ", "");
			tmp.delete();
			tmp.mkdir();
			String path = tmp.getCanonicalPath() + "\\";
			String modelName = null;
			
			//Unzip the kmz file
			Enumeration entries = zf.entries();
			while(entries.hasMoreElements()){
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if(entry.isDirectory()) {
					(new File(path + entry.getName())).mkdir();
				}
				else{
					File file = new File(path + entry.getName());
					File dir = new File(file.getParent());
					
					//We are not always guaranteed that the current file we
					//are unzipping belongs in a directory that already
					//exists
					if(dir.exists() == false){
						dir.mkdirs();
					}
					
					//Check the extension to see if this is our model
					if(".dae".equalsIgnoreCase(getExtension(entry.getName()))){
						modelName = entry.getName();
					}
					createFile(zf.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(file)));
				}
			}
		
		    if(modelName == null){
		    	//There was no .dae file in the archive
		    	mesh = null;
		    }else{
				//Pass the file to the DAE loader
				MeshLoader_DAE loader = new MeshLoader_DAE();
				mesh = loader.load(path + modelName);
			}

			return mesh;
		}catch(Exception e){
			e.printStackTrace();
			return null;  
		}
	}
  
	public Mesh load(InputStream is) {return null;}
	public boolean save(String filename, Mesh mesh) {return false;}
	
	/**
	 * Copy the contents of is to os 
	 * @param is The input stream to copy
	 * @param os The output stream to write to
	 */
	private void createFile(InputStream is, OutputStream os) throws IOException
	{
		byte[] buffer = new byte[1024];
	    int length;
	    
	    while((length = is.read(buffer)) >= 0){
	    	os.write(buffer, 0, length);	
	    }
	    
	    is.close();
	    os.close();
	}
	
	/**
	 * Obtains file extension from a filename
	 * @param filename The filename whose extension is to be obtained
	 * @return The file extension of filename
	 */
	private String getExtension(String filename)
	{
		int index = filename.lastIndexOf('.');
		if(index == -1){
			//No file extension
			return "";
		}else{
			return filename.substring(index);
		}
	}
}