package edu.ncsa.model;

/**
 * A program to convert between different 3D formats
 * @author Kenton McHenry
 */
public class ModelConverter
{
	/**
	 * Convert a 3D model from one format to another.
	 * @param args the input/output files
	 */
	public static void main(String args[])
	{
		String input, output;
		Mesh mesh;
		
		if(args.length != 2){
			System.out.println("Usage: ModelConverter input.abc output.xyz");
		}else{
			input = args[0];
			output = args[1];
			mesh = new Mesh();
			
			mesh.load(input);
			mesh.save(output);
		}
	}
}