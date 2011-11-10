package edu.ncsa.model.loaders.vtk;
import javax.vecmath.Vector3f;
import ncsa.util.ReaderTokenizer;

/**
 * A class to read in and store normal vectors
 * @author Daniel Long
 */
public class NORMALS implements FileReader
{

	/**
	 * Constructor
	 */
    public NORMALS()
    {
        normals = new Vector3f[VTKLoader.getPointCount()];
    }
    
    /**
     * Prints an error message and the line in the file on which it occurred
     * @param readertokenizer The file being read
     * @param message The error message to be printed
     */
    void PrintError(ReaderTokenizer readertokenizer, String message)
    {
        System.out.println("File error, line #" + readertokenizer.lineno() + ": " + message);
    }

    /**
	 * Reads in a group of normals from the file
	 * @param readertokenizer The file being read
	 * @param format 0 if the file is ASCII, 1 if the file is binary
	 * @return True on success, false on failure
	 */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        readertokenizer.nextToken();
        if(readertokenizer.ttype != -101){
            PrintError(readertokenizer, "NORMALS: Expecting a data name.");
            return false;
        }
        
        readertokenizer.nextToken();
        if(readertokenizer.ttype != -101){
            PrintError(readertokenizer, "NORMALS: Expecting a data type.");
            return false;
        }
        
        String dataType = readertokenizer.sval;
        CharReader charreader = new CharReader(readertokenizer);
        if(format == 1){
            charreader.readBinary();
        }
        
        if(dataType.equalsIgnoreCase("bit") && (format == 1)){
            PrintError(readertokenizer, "VECTORS: Cannot read BIT datatype in binary files.");
            return false;
        }
        
        DataTypeReader datatypereader = new DataTypeReader(readertokenizer, dataType, format);
        for(int i = 0; i < VTKLoader.getPointCount(); i++){
            normals[i] = new Vector3f(datatypereader.read(), datatypereader.read(), datatypereader.read());
        }

        VTKLoader.setNormals(normals);
        return true;
    }

    private Vector3f normals[];
}
