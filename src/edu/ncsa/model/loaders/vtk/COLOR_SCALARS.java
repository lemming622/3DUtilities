package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

/**
 * A class to read in and store colors
 * @author Daniel Long
 */
public class COLOR_SCALARS implements FileReader
{
	/**
     * Prints an error message and the line in the file on which it occurred
     * @param readertokenizer The file being read
     * @param message The error message to be printed
     */
    void PrintError(ReaderTokenizer readertokenizer, String message)
    {
        System.out.println("File error, line #" + readertokenizer.lineno() + ": " + message);
    }

    /**
	 * Reads in a group of colors from the file
	 * @param readertokenizer The file being read
	 * @param format 0 if the file is ASCII, 1 if the file is binary
	 * @return True on success, false on failure
	 */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        readertokenizer.nextToken();
        if(readertokenizer.ttype != -101){
            PrintError(readertokenizer, "COLOR_SCALARS: Expecting a data name.");
            return false;
        }
        
        IntReader intreader = new IntReader(readertokenizer);
        int length = intreader.read();
        colorValues = new float[VTKLoader.getPointCount()][length];
        
        CharReader charreader = new CharReader(readertokenizer);
        if(format == 1){
            charreader.readBinary();
        }
        
        FloatReader floatreader = new FloatReader(readertokenizer);
        for(int i = 0; i < VTKLoader.getPointCount(); i++){
            for(int j = 0; j < length; j++){
                if(format == 0){
                    colorValues[i][j] = floatreader.read();
                }else{
                    colorValues[i][j] = charreader.readBinary();
                }
            }
        }

        return true;
    }

    public float colorValues[][];
}
