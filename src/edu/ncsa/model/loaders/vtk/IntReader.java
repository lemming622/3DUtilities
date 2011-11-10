package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

/**
 * A class to repeatedly read ints from a file
 * @author Daniel Long
 */
public class IntReader
{	
	/**
	 * Constructor
	 * @param readertokenizer The file to read from
	 */
    public IntReader(ReaderTokenizer readertokenizer)
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
     * Reads an int from reader in ASCII format 
     * @return The int that was read
     */
    public int read()
    {
        int token = reader.nextToken();
        if(token != -102){
            PrintError("Expecting an int value.");
            return 0;
        }else{
        	return (int) reader.nval;
        }
    }
    
    /**
     * Reads an int from reader in binary format
     * @return The int that was read
     */
    public int readBinary()
    {
        int result = 0;
        for(int i = 0; i < 4; i++){
            if(VTKLoader.endian == 1){
                result += reader.read() << 8 * i;
            }else{
                result += reader.read() << 8 * (3 - i);
            }
        }
        return result;
    }

    ReaderTokenizer reader;
}
