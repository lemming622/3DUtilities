package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

/**
 * A class to read in and store the types of Cells
 * @author Daniel Long
 */
public class CELL_TYPES
{
    /**
     * Reads in the cell types from the file
     * @param readertokenizer The file being read
     * @param format 0 if the file is ASCII, 1 if it is binary
     * @return Always returns true
     */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        IntReader intreader = new IntReader(readertokenizer);
        int cells = intreader.read();
        cellTypes = new int[cells];
        CharReader charreader = new CharReader(readertokenizer);
        
        if(format == 1){
            charreader.readBinary();
        }
        
        for(int i = 0; i < cells; i++){
            if(format == 0){
                cellTypes[i] = intreader.read();
            }else{
                cellTypes[i] = intreader.readBinary();
            }
        }
        return true;
    }

    int cellTypes[];
}
