package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

/**
 * A class to read in and store texture coordinates
 * @author Daniel Long
 */
public class TEXTURE_COORDINATES implements FileReader
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
	 * Reads in texture coordinates from the file
	 * @param readertokenizer The file being read
	 * @param format 0 if the file is ASCII, 1 if the file is binary
	 * @return True on success, false on failure
	 */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        readertokenizer.nextToken();
        if(readertokenizer.ttype != -101){
            PrintError(readertokenizer, "TEXTURE_COORDS.: Expecting a data name.");
            return false;
        }
        
        IntReader intreader = new IntReader(readertokenizer);
        int length = intreader.read();
        textCoords = new float[VTKLoader.getPointCount()][length];
        
        readertokenizer.nextToken();
        if(readertokenizer.ttype != -101){
            PrintError(readertokenizer, "TEXTURE_COORDS.: Expecting a data type.");
            return false;
        }
        
        String token = readertokenizer.sval;
        CharReader charreader = new CharReader(readertokenizer);
        if(format == 1){
            charreader.readBinary();
        }
        if(token.equalsIgnoreCase("bit") && (format == 1)){
            PrintError(readertokenizer, "VECTORS: Cannot read BIT datatype in binary files.");
            return false;
        }
        
        DataTypeReader datatypereader = new DataTypeReader(readertokenizer, token, format);
        for(int i = 0; i < VTKLoader.getPointCount(); i++)
        {
            for(int j = 0; j < length; j++){
                textCoords[i][j] = datatypereader.read();
            }
        }

        return true;
    }

    float textCoords[][];
}
