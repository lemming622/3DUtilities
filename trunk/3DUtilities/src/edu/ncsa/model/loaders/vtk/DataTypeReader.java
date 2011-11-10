package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

/**
 * A generic data reading class; reads the appropriate data type from a
 * file, either ASCII or binary
 * @author Daniel Long
 */
public class DataTypeReader
{
	/**
	 * Constructor
	 * @param readertokenizer The file to be read
	 * @param dataType_ The appropriate data type to be read (for instance, "float")
	 * @param format 0 if the file is ASCII, 1 if the file is binary
	 */
    public DataTypeReader(ReaderTokenizer readertokenizer, String dataType_, int format)
    {
        reader = readertokenizer;
        dataType = dataType_;
        fileType = format;
    }

    /**
     * Reads the appropriate data type from the file and returns it, converted to a float 
     * @return The read data item
     */
    public float read()
    {
        if(dataType.equalsIgnoreCase("bit")){
            IntReader intreader = new IntReader(reader);
            if(fileType == 0)
            {
                return (float) intreader.read();
            }else{
                System.out.println("Cannot read BIT datatype in binary files.");
                return 0;
            }
        }else if(dataType.equalsIgnoreCase("short")){
            ShortReader shortreader = new ShortReader(reader);
            if(fileType == 0){
                return (float) shortreader.read();
            }else{
                return (float) shortreader.readBinary();
            }
        }else if(dataType.equalsIgnoreCase("int")){
            IntReader intreader1 = new IntReader(reader);
            if(fileType == 0){
                return (float) intreader1.read();
            }else{
                return (float) intreader1.readBinary();
            }
        }else if(dataType.equalsIgnoreCase("unsigned_char")){
            CharReader charreader = new CharReader(reader);
            if(fileType == 0){
                return (float) charreader.read();
            }else{
                return (float) charreader.readBinary();
            }
        }else if(dataType.equalsIgnoreCase("float")){
            FloatReader floatreader = new FloatReader(reader);
            if(fileType == 0){
                return floatreader.read();
            }else{
                return floatreader.readBinary();
            }
        }else if(dataType.equalsIgnoreCase("double")){
            DoubleReader doublereader = new DoubleReader(reader);
            if(fileType == 0){
                return (float) doublereader.read();
            }else{
                return (float) doublereader.readBinary();
            }
        }else{
            System.out.println("Unrecognized data type: " + dataType + ", line #" + reader.lineno());
            return 0;
        }
    }

    private ReaderTokenizer reader;
    private String dataType;
    private int fileType;
}
