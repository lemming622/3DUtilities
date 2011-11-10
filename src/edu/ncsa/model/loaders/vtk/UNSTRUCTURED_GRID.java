package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

/**
 * A class to read in and store an unstructured grid
 * @author Daniel Long
 */
public class UNSTRUCTURED_GRID implements FileReader
{

	/**
	 * Constructor
	 */
    public UNSTRUCTURED_GRID()
    {
        cells = null;
        cellTypes = null;
    }
    
	/**
	 * Reads in an unstructured grid from the file
	 * @param readertokenizer The file being read
	 * @param format 0 if the file is ASCII, 1 if the file is binary
	 * @return True on success, false on failure
	 */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        String token = null;
        readertokenizer.nextToken();
        for(token = readertokenizer.sval; (readertokenizer.ttype == -101) && (readertokenizer.sval.equalsIgnoreCase("POINTS") || readertokenizer.sval.equalsIgnoreCase("CELLS") || readertokenizer.sval.equalsIgnoreCase("CELL_TYPES")); token = readertokenizer.sval){
            if(token.equalsIgnoreCase("POINTS")){
                POINTS points = new POINTS();
                points.read(readertokenizer, format);
            }else if(token.equalsIgnoreCase("CELLS"))
            {
                cells = new CELLS();
                cells.read(readertokenizer, format);
            }else if(token.equalsIgnoreCase("CELL_TYPES")){
                cellTypes = new CELL_TYPES();
                cellTypes.read(readertokenizer, format);
            }else{
                System.out.println("Unsupported unstructured grid type: " + token);
                System.out.println("Break at line #" + readertokenizer.lineno());
                return false;
            }
            readertokenizer.nextToken();
        }

        if(token.equalsIgnoreCase("POINT_DATA")){
            readertokenizer.pushBack();
        }
        return true;
    }

    CELLS cells;
    CELL_TYPES cellTypes;
}
