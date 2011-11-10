package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

/**
 * A class to read in and store a structured grid
 * @author Daniel Long
 */
public class STRUCTURED_GRID extends GRID implements FileReader
{
	/**
	 * Constructor
	 */
    public STRUCTURED_GRID()
    {
        dimX = 0;
        dimY = 0;
        dimZ = 0;
    }
    
	/**
	 * Reads in a structured grid from the file
	 * @param readertokenizer The file being read
	 * @param format 0 if the file is ASCII, 1 if the file is binary
	 * @return True on success, false on failure
	 */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        POINTS points = new POINTS();
        IntReader intreader = new IntReader(readertokenizer);
        
        readertokenizer.nextToken();
        if((readertokenizer.ttype == -101) && readertokenizer.sval.equalsIgnoreCase("DIMENSIONS")){
            dimX = intreader.read();
            dimY = intreader.read();
            dimZ = intreader.read();
        }else{
            System.out.println("STRUCTURED_GRID: expecting DIMENSIONS keyword.");
            return false;
        }
        
        readertokenizer.nextToken();
        if((readertokenizer.ttype == -101) && readertokenizer.sval.equalsIgnoreCase("POINTS")){
            points.read(readertokenizer, format);
        }else{
            System.out.println("STRUCTURED_GRID: expecting POINTS keyword.");
            return false;
        }
        
        return true;
    }

    private int dimX;
    private int dimY;
    private int dimZ;
}
