package edu.ncsa.model.loaders.vtk;
import edu.ncsa.model.MeshAuxiliary.Point;
import ncsa.util.ReaderTokenizer;

/**
 * A class to read in and store points
 * @author Daniel Long
 */
public class POINTS
{   
	/**
     * Prints an error message and the line in the file on which it occurred
     * @param readertokenizer The file being read in
     * @param message The error message to be printed
     */
    void PrintError(ReaderTokenizer readertokenizer, String message)
    {
        System.out.println("File error, line #" + readertokenizer.lineno() + ": " + message);
    }

    /**
     * Returns the points stored in this class
     * @return The points stored in this class
     */
    public Point[] getPoints()
    {
        return points;
    }

    /**
     * Reads a group of points from the file
     * @param readertokenizer The file being read
     * @param format 0 if the file is ASCII, 1 if the file is binary
     * @return Returns true on success, false on failure
     */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        IntReader intreader = new IntReader(readertokenizer);
        
        int pointCount = intreader.read();
        VTKLoader.setPointCount(pointCount);
        points = new Point[pointCount];
        
        readertokenizer.nextToken();
        if(readertokenizer.ttype != -101){
            PrintError(readertokenizer, "POINTS: Expecting a data type.");
            return false;
        }
        
        String dataType = readertokenizer.sval;
        CharReader charreader = new CharReader(readertokenizer);
        if(format == 1){
            charreader.readBinary();
        }
        if(dataType.equalsIgnoreCase("bit") && (format == 1)){
            PrintError(readertokenizer, "POINTS: Cannot read BIT datatype in BINARY files.");
            return false;
        }
        
        DataTypeReader datatypereader = new DataTypeReader(readertokenizer, dataType, format);
        for(int i = 0; i < pointCount; i++){
        	points[i] = new Point(datatypereader.read(), datatypereader.read(), datatypereader.read());
        }
        VTKLoader.setPoints(points);
        
        return true;
    }

    /**
     * Sets the points
     * @param points The points to store
     */
    public void setPoints(Point points_[])
    {
        points = points_;
    }

    private Point points[];
}
