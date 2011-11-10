package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

/**
 * A class to repeatedly read doubles from a file
 * @author Daniel Long
 */
public class DoubleReader
{
	/**
	 * Constructor
	 * @param readertokenizer The file to read from
	 */
    public DoubleReader(ReaderTokenizer readertokenizer)
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
     * Reads a double from reader in ASCII format 
     * @return The double that was read
     */
    public double read()
    {
        int token = reader.nextToken();
        if(token != -102){
            PrintError("Expecting a double floating point value.");
            return 0;
        }else{
            return reader.nval;
        }
    }

    /**
     * Reads a double from reader in binary format
     * @return The double that was read
     */
    public double readBinary()
    {
        long result = 0;
        for(int i = 0; i < 8; i++){
            if(VTKLoader.endian == 1){
                result += reader.read() << 8 * i;
            }else{
                result += reader.read() << 8 * (7 - i);
            }
        }
        return Double.longBitsToDouble(result);
    }

    ReaderTokenizer reader;
}
