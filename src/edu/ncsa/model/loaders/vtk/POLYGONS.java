package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

/**
 * A class to read in and store polygons
 * @author Daniel Long
 */
public class POLYGONS implements FileReader
{	
	/**
	 * Returns the polygons
	 * @return The polygons
	 */
	int[][] getPolygons()
	{
		return polygons;
	}
	
	/**
	 * Reads in polydata from the file
	 * @param readertokenizer The file being read
	 * @param format 0 if the file is ASCII, 1 if the file is binary
	 * @return Always returns true
	 */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        IntReader intreader = new IntReader(readertokenizer);
        int polygonCount = intreader.read();
        polygons = new int[polygonCount][];
        intreader.read();
        CharReader charreader = new CharReader(readertokenizer);
        if(format == 1){
            charreader.readBinary();
        }
        
        for(int i = 0; i < polygonCount; i++){
            int vertices;
            if(format == 0){
                vertices = intreader.read();
            }else{
                vertices = intreader.readBinary();
            }
            
            polygons[i] = new int[vertices];
            for(int j = 0; j < vertices; j++){
                if(format == 0){
                    polygons[i][j] = intreader.read();
                }else
                    polygons[i][j] = intreader.readBinary();
            }
        }

        return true;
    }

    /**
     * Sets the polygons
     * @param polygons_ The polygons to store
     */
    void setPolygons(int ai[][])
    {
        polygons = ai;
    }

    private int polygons[][];
}
