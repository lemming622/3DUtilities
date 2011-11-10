package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

/**
 * A class to read in and store triangle strips
 * @author Daniel Long
 */
public class TRIANGLE_STRIPS implements FileReader
{
	/**
	 * Reads in triangle strips from the file
	 * @param readertokenizer The file being read
	 * @param format 0 if the file is ASCII, 1 if the file is binary
	 * @return Always returns true
	 */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        IntReader intreader = new IntReader(readertokenizer);
        int triangleCount = intreader.read();
        triangles = new int[triangleCount][];
        intreader.read();
        CharReader charreader = new CharReader(readertokenizer);
        if(format == 1){
            charreader.readBinary();
        }
        
        for(int i = 0; i < triangleCount; i++){
            int length;
            if(format == 0){
                length = intreader.read();
            }else{
                length = intreader.readBinary();
            }
            
            triangles[i] = new int[length];
            for(int j = 0; j < length; j++){
                if(format == 0){
                    triangles[i][j] = intreader.read();
                }else{
                    triangles[i][j] = intreader.readBinary();
                }
            }
        }

        return true;
    }
    
    private int triangles[][];
}
