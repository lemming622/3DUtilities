package edu.ncsa.model.loaders.vtk;
import javax.vecmath.Color4f;
import ncsa.util.ReaderTokenizer;

/**
 * Reads in and stores a lookup table for colors
 * @author Daniel Long
 */
public class LOOKUP_TABLE implements FileReader
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
	 * Reads in a color lookup table from the file
	 * @param readertokenizer The file being read
	 * @param format 0 if the file is ASCII, 1 if the file is binary
	 * @return True on success, false on failure
	 */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        readertokenizer.nextToken();
        if(readertokenizer.ttype != -101){
            PrintError(readertokenizer, "LOOKUP_TABLE: Expecting a table name.");
            return false;
        }
        
        IntReader intreader = new IntReader(readertokenizer);
        int length = intreader.read();
        colors = new Color4f[length];
        
        CharReader charreader = new CharReader(readertokenizer);
        if(format == 1){
            charreader.readBinary();
        }
        
        FloatReader floatreader = new FloatReader(readertokenizer);
        for(int i = 0; i < length; i++){
            if(format == 0){
                colors[i] = new Color4f(floatreader.read(), floatreader.read(), floatreader.read(), floatreader.read());
            }else{
                colors[i] = new Color4f(charreader.readBinary(), charreader.readBinary(), charreader.readBinary(), charreader.readBinary());
            }
        }

        VTKLoader.setColors(colors);
        return true;
    }

    private Color4f colors[];
}
