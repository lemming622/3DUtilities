package edu.ncsa.model.loaders.vtk;
import edu.ncsa.model.MeshAuxiliary.Point;
import ncsa.util.ReaderTokenizer;

/**
 * A class to read in and store a set of structured points
 * @author Daniel Long
 */
public class STRUCTURED_POINTS extends GRID implements FileReader
{
	/**
	 * Constructor
	 */
    public STRUCTURED_POINTS()
    {
        dimX = 0;
        dimY = 0;
        dimZ = 0;
    }
    
    /**
	 * Reads in a set of structured points from the file
	 * @param readertokenizer The file being read
	 * @param format 0 if the file is ASCII, 1 if the file is binary
	 * @return Always returns true
	 */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        float originX = 0;
        float originY = 0;
        float originZ = 0;
        float spacingX = 0;
        float spacingY = 0;
        float spacingZ = 0;
        IntReader intreader = new IntReader(readertokenizer);
        FloatReader floatreader = new FloatReader(readertokenizer);
        
        for(int i = 3; i > 0; ){
            boolean foundToken = false;
            readertokenizer.nextToken();
            if((readertokenizer.ttype == -101) && readertokenizer.sval.equalsIgnoreCase("DIMENSIONS")){
                dimX = intreader.read();
                dimY = intreader.read();
                dimZ = intreader.read();
                foundToken = true;
                i--;
            }
            if((readertokenizer.ttype == -101) && readertokenizer.sval.equalsIgnoreCase("ORIGIN")){
                originX = floatreader.read();
                originY = floatreader.read();
                originZ = floatreader.read();
                foundToken = true;
                i--;
            }
            if((readertokenizer.ttype == -101) && (readertokenizer.sval.equalsIgnoreCase("SPACING") || readertokenizer.sval.equalsIgnoreCase("ASPECT_RATIO"))){
                spacingX = floatreader.read();
                spacingY = floatreader.read();
                spacingZ = floatreader.read();
                foundToken = true;
                i--;
            }
            
            if(foundToken == false){
                System.out.println("STRUCTURED_POINTS: expecting DIMENSIONS, ORIGIN, SPACING, or ASPECT_RATIO.");
                return false;
            }
        }
        
        Point points[] = new Point[dimX * dimY * dimZ];
        for(int i = 0, j = 0; j < dimZ; j++){
            for(int k = 0; k < dimY; k++){
                for(int l = 0; l < dimX; l++, i++){
                    points[i] = new Point(l * spacingX + originX, k * spacingY + originY, j * spacingZ + originZ);
                }
            }
        }

        VTKLoader.setPoints(points);
        return true;
    }

    public int dimX;
    public int dimY;
    public int dimZ;
}
