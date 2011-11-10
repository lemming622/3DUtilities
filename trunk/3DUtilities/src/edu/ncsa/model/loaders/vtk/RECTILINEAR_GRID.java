package edu.ncsa.model.loaders.vtk;
import edu.ncsa.model.MeshAuxiliary.Point;
import ncsa.util.ReaderTokenizer;

/**
 * A class to read in and store a rectilinear grid
 * @author Daniel Long
 */
public class RECTILINEAR_GRID extends GRID implements FileReader
{
	/**
	 * Constructor
	 */
    public RECTILINEAR_GRID()
    {
        dimX = 0;
        dimY = 0;
        dimZ = 0;
    }

    /**
	 * Reads in a rectilinear grid from the file
	 * @param readertokenizer The file being read
	 * @param format 0 if the file is ASCII, 1 if the file is binary
	 * @return True on success, false on failure
	 */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        IntReader intreader = new IntReader(readertokenizer);
        
        readertokenizer.nextToken();
        if(readertokenizer.sval.equalsIgnoreCase("DIMENSIONS")){
            dimX = intreader.read();
            dimY = intreader.read();
            dimZ = intreader.read();
        }else{
            System.out.println("RECTILINEAR_GRID: expecting DIMENSIONS keyword.");
            return false;
        }
        
        float xCoords[] = new float[dimX];
        float yCoords[] = new float[dimY];
        float zCoords[] = new float[dimZ];
        String token;
        
        //Read in x coordinates
        readertokenizer.nextToken();
        if((readertokenizer.ttype == -101) && readertokenizer.sval.equalsIgnoreCase("X_COORDINATES"))
        {
            intreader.read();
            readertokenizer.nextToken();
            token = new String(readertokenizer.sval);
        }else{
            System.out.println("RECTILINEAR_GRID: expecting X_COORDINATES keyword.");
            return false;
        }
        
        CharReader charreader = new CharReader(readertokenizer);
        if(format == 1){
            charreader.readBinary();
        }
        
        if(token.equalsIgnoreCase("bit") && (format == 1)){
            System.out.println("RECTILINEAR_GRID: cannot read BIT datatype in binary files.");
            return false;
        }
        
        DataTypeReader datatypereader = new DataTypeReader(readertokenizer, token, format);
        for(int i = 0; i < dimX; i++){
            xCoords[i] = datatypereader.read();
        }

        //Read in y coordinates
        readertokenizer.nextToken();
        if((readertokenizer.ttype == -101) && readertokenizer.sval.equalsIgnoreCase("Y_COORDINATES")){
            intreader.read();
            readertokenizer.nextToken();
            token = new String(readertokenizer.sval);
        }else{
            System.out.println("RECTILINEAR_GRID; expecting Y_COORDINATES keyword.");
            return false;
        }
        
        if(format == 1){
            charreader.readBinary();
        }
        
        if(token.equalsIgnoreCase("bit") && (format == 1)){
            System.out.println("RECTILINEAR_GRID: cannot read BIT datatype in binary files.");
            return false;
        }
        
        datatypereader = new DataTypeReader(readertokenizer, token, format);
        for(int i = 0; i < dimY; i++){
            yCoords[i] = datatypereader.read();
        }

        //Read in z coordinates
        readertokenizer.nextToken();
        if((readertokenizer.ttype == -101) && readertokenizer.sval.equalsIgnoreCase("Z_COORDINATES")){
            intreader.read();
            readertokenizer.nextToken();
            token = new String(readertokenizer.sval);
        }else{
            System.out.println("RECTILINEAR_GRID; expecting Z_COORDINATES keyword.");
            return false;
        }
        
        if(format == 1){
            charreader.readBinary();
        }
        
        if(token.equalsIgnoreCase("bit") && (format == 1)){
            System.out.println("RECTILINEAR_GRID: cannot read BIT datatype in binary files.");
            return false;
        }
        
        datatypereader = new DataTypeReader(readertokenizer, token, format);
        for(int i = 0; i < dimZ; i++){
            zCoords[i] = datatypereader.read();
        }
        
        //The points are all possible combinations of the x, y, and z coordinates
        Point points[] = new Point[dimX * dimY * dimZ];
        for(int i = 0, l = 0; i < dimZ; i++){
            for(int j = 0; j < dimY; j++){
                for(int k = 0; k < dimX; k++, l++){
                    points[l] = new Point(xCoords[k], yCoords[j], zCoords[i]);
                }
            }
        }

        VTKLoader.setPoints(points);
        return true;
    }

    int dimX;
    int dimY;
    int dimZ;
}
