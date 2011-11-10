package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

/**
 * A class to read in and store scalars from the file
 * @author Daniel Long
 */
public class SCALARS implements FileReader
{
	/**
	 * Constructor
	 */
    public SCALARS()
    {
        scalars = null;
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
	 * Reads in a group of scalars from the file
	 * @param readertokenizer The file being read
	 * @param format 0 if the file is ASCII, 1 if the file is binary
	 * @return True on success, false on failure
	 */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        scalars = new float[VTKLoader.getPointCount()];
        readertokenizer.nextToken();
        if(readertokenizer.ttype != -101){
            PrintError(readertokenizer, "SCALARS: Expecting a data name.");
            return false;
        }
        
        readertokenizer.nextToken();
        if(readertokenizer.ttype != -101){
            PrintError(readertokenizer, "SCALARS: Expecting a data type.");
            return false;
        }
        
        String token = readertokenizer.sval;
        readertokenizer.nextToken();
        if(readertokenizer.ttype != -101){
            readertokenizer.nextToken();
        }
        
        if(readertokenizer.sval.equalsIgnoreCase("LOOKUP_TABLE")){
            readertokenizer.nextToken();
        }
        
        CharReader charreader = new CharReader(readertokenizer);
        if(format == 1){
            charreader.readBinary();
        }
        
        if(token.equalsIgnoreCase("bit") && (format == 1)){
            PrintError(readertokenizer, "SCALARS: Cannot read BIT datatype in binary files.");
            return false;
        }
        
        DataTypeReader datatypereader = new DataTypeReader(readertokenizer, token, format);
        for(int i = 0; i < VTKLoader.getPointCount(); i++){
            scalars[i] = datatypereader.read();
        }

        return true;
    }

    float scalars[];
}
