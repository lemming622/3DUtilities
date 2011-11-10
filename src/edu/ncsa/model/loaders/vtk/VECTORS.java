package edu.ncsa.model.loaders.vtk;
import javax.vecmath.Vector3f;
import ncsa.util.ReaderTokenizer;

/**
 * A class to read in and store a group of vectors
 * @author Daniel Long
 */
public class VECTORS implements FileReader
{
	/**
	 * Constructor
	 */
    public VECTORS()
    {
        vectors = new Vector3f[VTKLoader.getPointCount()];
    }

    /**
     * Prints an error message and the line in the file on which it occurred
     * @param readertokenizer The file being read in
     * @param message The error message to be printed
     */
    void PrintError(ReaderTokenizer readertokenizer, String format)
    {
        System.out.println("File error, line #" + readertokenizer.lineno() + ": " + format);
    }

    /**
	 * Reads in a group of vectors from the file
	 * @param readertokenizer The file being read
	 * @param format 0 if the file is ASCII, 1 if the file is binary
	 * @return True on success, false on failure
	 */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        readertokenizer.nextToken();
        if(readertokenizer.ttype != -101){
            PrintError(readertokenizer, "VECTORS: Expecting a data name.");
            return false;
        }
        
        readertokenizer.nextToken();
        if(readertokenizer.ttype != -101){
            PrintError(readertokenizer, "VECTORS: Expecting a data type.");
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
            vectors[i] = new Vector3f(datatypereader.read(), datatypereader.read(), datatypereader.read());
        }

        return true;
    }

    private Vector3f vectors[];
}
