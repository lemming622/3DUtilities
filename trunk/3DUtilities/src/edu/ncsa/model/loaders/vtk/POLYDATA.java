package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;
import java.util.Vector;

/**
 * A class to read in and store polydata
 * @author Daniel Long
 */
public class POLYDATA implements FileReader
{

	/**
	 * Constructor; initializes objs
	 */
    public POLYDATA()
    {
        readers = new Vector<FileReader>();
    }
    
	/**
     * Prints an error message and the line in the file on which it occurred
     * @param readertokenizer The file being read in
     * @param message The error message to be printed
     */
    void PrintError(ReaderTokenizer readertokenizer, String s)
    {
        System.out.println("File error, line #" + readertokenizer.lineno() + ": " + s);
    }
    
    /**
     * Reads a polydata group from the file
     * @param readertokenizer The file being read
     * @param format 0 if the file is ASCII, 1 if the file is binary
     * @return Returns true on success, false on failure
     */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        readertokenizer.nextToken();
        if((readertokenizer.ttype == -101) && readertokenizer.sval.equalsIgnoreCase("POINTS")){
            POINTS points = new POINTS();
            points.read(readertokenizer, format);
        }else{
            System.out.println("POLYDATA: POINTS keyword expected, line #" + readertokenizer.lineno());
            return false;
        }
        
        String packageName = null;
        readertokenizer.nextToken();
        while(readertokenizer.ttype != -103){ 
            try{
                packageName = readertokenizer.sval;
                
                if(readertokenizer.ttype != -101){
                    PrintError(readertokenizer, "POLYDATA: Expecting a keyword string.");
                    return false;
                }
                
                if(packageName.equalsIgnoreCase("POINT_DATA")){
                    readertokenizer.pushBack();
                    return true;
                }
                
                packageName = new String(VTKLoader.getPckg() + "." + packageName);
                readers.add((FileReader) Class.forName(packageName).newInstance());
                ((FileReader) readers.lastElement()).read(readertokenizer, format);
                readertokenizer.nextToken();
            }catch(Exception exception){
                System.out.println("Error in type: " + packageName + " at line #" + readertokenizer.lineno());
                System.out.println("   (" + exception + ")");
                return false;
            }
        }
        
        return true;
    }

    Vector<FileReader> readers;
}
