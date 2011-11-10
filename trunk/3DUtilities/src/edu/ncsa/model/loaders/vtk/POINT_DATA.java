package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

/**
 * A class to read in a group of points
 * @author Daniel Long
 */
public class POINT_DATA implements FileReader
{

	/**
	 * Constructor
	 */
    public POINT_DATA()
    {
        reader = null;
    }
    
    /**
     * Prints an error message and the line in the file on which it occurred
     * @param message The error message to be printed
     */
    void PrintError(ReaderTokenizer readertokenizer, String message)
    {
        System.out.println("File error, line #" + readertokenizer.lineno() + ": " + message);
    }

    /**
	 * Reads in a group of points from the file
	 * @param readertokenizer The file being read
	 * @param format 0 if the file is ASCII, 1 if the file is binary
	 * @return True on success, false on failure
	 */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        IntReader intreader = new IntReader(readertokenizer);
        int pointCount = intreader.read();
        int oldPointCount = VTKLoader.getPointCount();
        if(oldPointCount == 0){
            VTKLoader.setPointCount(pointCount);
        }
        else if(oldPointCount != pointCount){
            System.out.println("The number of POINT_DATA items (" + pointCount + ")");
            System.out.println("does not match the number of points in the dataset (" + oldPointCount + ")");
            return false;
        }
        
        String packageName = null;
        readertokenizer.nextToken();
        while(readertokenizer.ttype != -103){ 
            try{
                if(readertokenizer.ttype != -101){
                    PrintError(readertokenizer, "POINT_DATA: Expecting a keyword string.");
                    return false;
                }
                
                packageName = VTKLoader.getPckg() + "." + readertokenizer.sval;
                FileReader filereader = (FileReader)Class.forName(packageName).newInstance();
                filereader.read(readertokenizer, format);
                readertokenizer.nextToken();
            }catch(Exception exception){
                System.out.println("Error in type: " + packageName + " at line #" + readertokenizer.lineno());
                System.out.println("   (" + exception + ")");
                return false;
            }
        }
        
        return true;
    }

    ReaderTokenizer reader;
}
