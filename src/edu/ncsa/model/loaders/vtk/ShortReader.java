package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

/**
 * A class to repeatedly read shorts from a file
 * @author Daniel Long
 */
public class ShortReader
{	
	/**
	 * Constructor
	 * @param readertokenizer The file to read from
	 */
    public ShortReader(ReaderTokenizer readertokenizer)
    {
        reader = readertokenizer;
    }

    /**
     * Prints an error message and the line in the file on which it occurred
     * @param message The error message to be printed
     */
    void PrintError(String s)
    {
        System.out.println("File error, line #" + reader.lineno() + ": " + s);
    }

    /**
     * Reads a short from reader in ASCII format 
     * @return The short that was read
     */
    public short read()
    {
        int token = reader.nextToken();
        if(token != -102){
            PrintError("Expecting an int value.");
            return 0;
        }else{
        	return (short) reader.nval;
        }
    }
    
    /**
     * Reads a short from reader in binary format
     * @return The short that was read
     */
    public short readBinary()
    {
        short result = 0;
        for(int i = 0; i < 2; i++){
            if(VTKLoader.endian == 1){
                result += reader.read() << 8 * i;
            }else{
                result += reader.read() << 8 * (1 - i);
            }
        }
        return result;
    }

    ReaderTokenizer reader;
}
