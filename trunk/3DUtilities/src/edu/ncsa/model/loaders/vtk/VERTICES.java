package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

/**
 * A class to read in and store vertex indexes
 * @author Daniel Long
 */
public class VERTICES implements FileReader
{
	/**
	 * Reads in a group of vertices from the file
	 * @param readertokenizer The file being read
	 * @param format 0 if the file is ASCII, 1 if the file is binary
	 * @return Always returns true
	 */
    public boolean read(ReaderTokenizer readertokenizer, int format)
    {
        IntReader intreader = new IntReader(readertokenizer);
        int length = intreader.read() - intreader.read();
        
        CharReader charreader = new CharReader(readertokenizer);
        if(format == 1){
            charreader.readBinary();
        }
        
        int i = 0;
        while(i < length){
            int vertexCount;
            if(format == 0){
                vertexCount = intreader.read();
            }else{
                vertexCount = intreader.readBinary();
            }
            
            for(int j = 0; j < vertexCount; i++, j++){
                if(format == 0){
                    vertices[i] = intreader.read();
                }else{
                    vertices[i] = intreader.readBinary();
                }
            }
        }

        return true;
    }

    int vertices[];
}
