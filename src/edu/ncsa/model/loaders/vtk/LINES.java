package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

/**
 * A class to read in and store lines
 * @author Daniel Long
 */
public class LINES implements FileReader
{
	/**
	 * Reads in a group of lines from the file
	 * @param readertokenizer The file being read
	 * @param format 0 if the file is ASCII, 1 if the file is binary
	 * @return Always returns true
	 */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        IntReader intreader = new IntReader(readertokenizer);
        int lineCount = intreader.read();
        lines = new int[lineCount][];
        intreader.read();
        CharReader charreader = new CharReader(readertokenizer);
        if(format == 1){
            charreader.readBinary();
        }
        
        for(int i = 0; i < lineCount; i++){
            int lineSize;
            if(format == 0){
                lineSize = intreader.read();
            }else{
                lineSize = intreader.readBinary();
            }
            
            lines[i] = new int[lineSize];
            for(int j = 0; j < lineSize; j++){
                if(format == 0){
                    lines[i][j] = intreader.read();
                }else{
                    lines[i][j] = intreader.readBinary();
                }
            }
        }

        return true;
    }

    /**
     * Sets the lines
     * @param lines_ The lines to store
     */
    public void setLines(int lines_[][])
    {
        lines = lines_;
    }

    public int lines[][];
}
