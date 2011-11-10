package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

/**
 * A class to repeatedly read floats from a file
 * @author Daniel Long
 */
public class FloatReader
{
	/**
	 * Constructor
	 * @param readertokenizer The file to read from
	 */
    public FloatReader(ReaderTokenizer readertokenizer)
    {
        reader = readertokenizer;
    }
    
    /**
     * Prints an error message and the line in the file on which it occurred
     * @param message The error message to be printed
     */
    void PrintError(String message)
    {
        System.out.println("File error, line #" + reader.lineno() + ": " + message);
    }
    
    /**
     * Reads a float from reader in ASCII format 
     * @return The float that was read
     */
    public float read()
    {
        int token = reader.nextToken();
        if(token != -102){
            PrintError("Expecting float value.");
            return 0;
        }else{
            return (float) reader.nval;
        }
    }
    
    /**
     * Reads a float from reader in binary format
     * @return The float that was read
     */
    public float readBinary()
    {
        int result = 0;
        for(int i = 0; i < 4; i++){
            if(VTKLoader.endian == 1){
                result += reader.read() << 8 * i;
            }else{
                result += reader.read() << 8 * (3 - i);
            }
        }
        return Float.intBitsToFloat(result);
    }

    ReaderTokenizer reader;
}
