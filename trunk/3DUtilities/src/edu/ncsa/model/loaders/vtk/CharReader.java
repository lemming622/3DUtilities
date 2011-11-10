package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

/**
 * A class to repeatedly read chars from a file
 * @author Daniel Long
 */
public class CharReader
{
	/**
	 * Constructor
	 * @param readertokenizer The file to read from
	 */
    public CharReader(ReaderTokenizer readertokenizer)
    {
        reader = readertokenizer;
    }

    /**
     * Reads a char from reader
     * @return The char that was read
     */
    public int read()
    {
        return reader.read();
    }

    /**
     * Reads a char from reader
     * @return The char that was read
     */
    public int readBinary()
    {
    	return reader.read();
    }

    ReaderTokenizer reader;
}
