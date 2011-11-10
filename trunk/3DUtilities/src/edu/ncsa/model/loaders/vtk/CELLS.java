package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

/**
 * A class to read in and store cells
 * @author Daniel Long
 */
public class CELLS
{
	/**
	 * Reads in a group of cells from the file
	 * @param readertokenizer The file being read
	 * @param format 0 if the file is ASCII, 1 if the file is binary
	 * @return Always returns true
	 */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        IntReader intreader = new IntReader(readertokenizer);
        int cellCount = intreader.read();
        cells = new int[cellCount][];
        intreader.read();
        CharReader charreader = new CharReader(readertokenizer);
        
        if(format == 1){
            charreader.readBinary();
        }
        
        for(int i = 0; i < cellCount; i++){
            int cellLength;
            if(format == 0){
                cellLength = intreader.read();
            }else{
                cellLength = intreader.readBinary();
            }
            
            cells[i] = new int[cellLength];
            for(int j = 0; j < cellLength; j++){
                if(format == 0){
                    cells[i][j] = intreader.read();
                }else{
                    cells[i][j] = intreader.readBinary();
                }
            }
        }

        return true;
    }

    int cells[][];
}
