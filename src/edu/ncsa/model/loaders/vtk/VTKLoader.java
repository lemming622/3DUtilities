package edu.ncsa.model.loaders.vtk;
import java.io.*;
import java.util.ArrayList;
import java.util.Vector;
import javax.vecmath.*;
import edu.ncsa.model.Mesh;
import edu.ncsa.model.MeshAuxiliary.*;
import ncsa.util.ReaderTokenizer;

public class VTKLoader
{
	public VTKLoader()
	{
		filetype = 0;
	}

	void PrintError(String s)
	{
		System.out.println("File error, line #" + reader.lineno() + ": " + s);
	}

	public static Color4f[] getColors()
	{
		return colors;
	}

	public static Vector3f[] getNormals()
	{
		return normals;
	}

	public static String getPckg()
	{
		return pckg;
	}

	public static int getPointCount()
	{
		return pointCount;
	}

	public static Point[] getPoints()
	{
		return points.getPoints();
	}

	public Mesh load(Reader reader1, int endian_) throws IOException
	{
		endian = endian_;
		points = new POINTS();
		pointCount = 0;
		normals = null;
		colors = null;
		
		BufferedReader bufferedreader = new BufferedReader(reader1);
		String s = bufferedreader.readLine();
		
		if(s.substring(0, 14).equalsIgnoreCase("# vtk DataFile") == false){
			System.out.println("Incorrect VTK data file header.");
			return null;
		}
		
		String line = bufferedreader.readLine();
		
		reader = new ReaderTokenizer(bufferedreader);
		reader.nextToken();
		line = reader.sval;
		if(line.equalsIgnoreCase("ASCII")){
			filetype = 0;
		}else if(line.equalsIgnoreCase("BINARY")){
			filetype = 1;
		}else{
			PrintError("Expected keywords BINARY or ASCII not found.");
			return null;
		}
		
		Vector<FileReader> vector = new Vector<FileReader>();
		reader.nextToken();
		
		while(reader.ttype != -103){ 
			try{
				if(reader.ttype != -101){
					PrintError("Expecting a keyword string.");
					return null;
				}
				line = reader.sval;
				if(line.equalsIgnoreCase("DATASET") == false){
					if(debug){
						System.out.println("VTKLoader: reading " + line);
					}
					
					String packageName = new String(pckg + "." + line);
					vector.add((FileReader) Class.forName(packageName).newInstance());
					if(((edu.ncsa.model.loaders.vtk.FileReader)vector.lastElement()).read(reader, filetype) == false){
						return null;
					}
				}
				reader.nextToken();
			}catch(Exception exception){
				PrintError("Error in type: " + line);
				System.out.println("   (" + exception + ")");
				return null;
			}
		}
		
		Mesh mesh = new Mesh();
		
		Vector<Point> vertices = new Vector<Point>();
		for(int i = 0; i < points.getPoints().length; i++){
			vertices.add(new Point(points.getPoints()[i].x, points.getPoints()[i].y, points.getPoints()[i].z));
		}
		mesh.setVertices(vertices);
		
		Vector<Face> faces = new Vector<Face>();
		for(int i = 0; i < vector.size(); i++){
			String name = vector.elementAt(i).getClass().getName().substring(pckg.length() + 1);
			
			if(name.equals("POLYDATA")){
				for(int j = 0; j < ((POLYDATA) vector.elementAt(i)).readers.size(); j++){
					
					name = ((POLYDATA) vector.elementAt(i)).readers.elementAt(j).getClass().getName().substring(pckg.length() + 1);
					
					if(name.equals("LINES")){
						int[][] lines = ((LINES) ((POLYDATA) vector.elementAt(i)).readers.elementAt(j)).lines;
						for(int k = 0; k < lines.length; k++){
							for(int l = 0; l < lines[k].length - 1; l++){
								faces.add(new Face(lines[k][l], lines[k][l + 1]));
							}	
						}
					}else if(name.equals("POLYGONS")){
						int[][] polygons = ((POLYGONS) ((POLYDATA) vector.elementAt(i)).readers.elementAt(j)).getPolygons();
						for(int k = 0; k < polygons.length; k++){
							ArrayList<Integer> al = new ArrayList<Integer>();
							for(int l = 0; l < polygons[k].length; l++){
								al.add(polygons[k][l]);
							}
							faces.add(new Face(al));
						}
					}else{
						System.err.println("Unhandled subgroup of POLYDATA: " + name);
					}
				}
			}else if(name.equals("STRUCTURED_POINTS")){
				int x = ((STRUCTURED_POINTS) vector.elementAt(i)).dimX;
				int y = ((STRUCTURED_POINTS) vector.elementAt(i)).dimY;
				int z = ((STRUCTURED_POINTS) vector.elementAt(i)).dimZ;
				int[][] grid = ((STRUCTURED_POINTS) vector.elementAt(i)).connectPoints(x, y, z);
				for(int j = 0; j < grid.length; j++){
					faces.add(new Face(grid[j][0], grid[j][1]));
				}
			}else if(name.equals("STRUCTURED_GRID")){
				System.out.println("Unhandeled group: STRUCTURED_GRID");
			}else if(name.equals("UNSTRUCTURED_GRID")){
				CELLS cells = ((UNSTRUCTURED_GRID) vector.elementAt(i)).cells;
				CELL_TYPES cellTypes = ((UNSTRUCTURED_GRID) vector.elementAt(i)).cellTypes;
				ArrayList<Integer> face;
				
				for(int j = 0; j < cellTypes.cellTypes.length; j++){
					switch(cellTypes.cellTypes[j]){
					case 1: //VTK_VERTEX
						break;
						
					case 2: //VTK_POLYVERTEX
						System.out.println("Unhandeled cell type: VTK_POLYVERTEX");
						break;
						
					case 3: //VTK_LINE
						faces.add(new Face(cells.cells[j][0], cells.cells[j][1]));
						break;
						
					case 4: //VTK_POLY_LINE
						System.out.println("Unhandeled cell type: VTK_POLY_LINE");
						break;
						
					case 5: //VTK_TRIANGLE
						faces.add(new Face(cells.cells[j][0], cells.cells[j][1], cells.cells[j][2]));
						break;
						
					case 6: //VTK_TRIANGLE_STRIP
						for(int k = 2; k < cells.cells[j].length; k++){
							faces.add(new Face(cells.cells[j][k - 2], cells.cells[j][k - 1], cells.cells[j][k]));
						}
						break;
						
					case 7: //VTK_POLYGON
						face = new ArrayList<Integer>();
						for(int k = 0; k < cells.cells[j].length; k++){
							face.add(cells.cells[j][k]);
						}
						faces.add(new Face(face));
						break;
						
					case 8: //VTK_PIXEL
						System.out.println("Unhandeled cell type: VTK_PIXEL");
						break;
						
					case 9: //VTK_QUAD
						face = new ArrayList<Integer>();
						face.add(cells.cells[j][0]);
						face.add(cells.cells[j][1]);
						face.add(cells.cells[j][2]);
						face.add(cells.cells[j][3]);
						faces.add(new Face(face));
						break;
						
					case 10: //VTK_TETRA
						faces.add(new Face(cells.cells[j][0], cells.cells[j][1], cells.cells[j][2]));
						faces.add(new Face(cells.cells[j][0], cells.cells[j][1], cells.cells[j][3]));
						faces.add(new Face(cells.cells[j][0], cells.cells[j][2], cells.cells[j][3]));
						faces.add(new Face(cells.cells[j][1], cells.cells[j][2], cells.cells[j][3]));
						break;
						
					case 11: //VTK_VOXEL
						//This section is untested
						face = new ArrayList<Integer>();

						//First face consists of indexes 4, 5, 1, and 0
						face.add(cells.cells[j][4]);
						face.add(cells.cells[j][5]);
						face.add(cells.cells[j][1]);
						face.add(cells.cells[j][0]);
						faces.add(new Face(face));

						//Second face consists of indexes 5, 7, 3, and 1
						faces.clear();
						face.add(cells.cells[j][5]);
						face.add(cells.cells[j][7]);
						face.add(cells.cells[j][3]);
						face.add(cells.cells[j][1]);
						faces.add(new Face(face));

						//Third face consists of indexes 7, 6, 2, and 3
						faces.clear();
						face.add(cells.cells[j][7]);
						face.add(cells.cells[j][6]);
						face.add(cells.cells[j][2]);
						face.add(cells.cells[j][3]);
						faces.add(new Face(face));

						//Fourth face consists of indexes 6, 4, 0, and 2
						faces.clear();
						face.add(cells.cells[j][6]);
						face.add(cells.cells[j][4]);
						face.add(cells.cells[j][0]);
						face.add(cells.cells[j][2]);
						faces.add(new Face(face));

						//Fifth face consists of indexes 0, 1, 3, and 2
						faces.clear();
						face.add(cells.cells[j][0]);
						face.add(cells.cells[j][1]);
						face.add(cells.cells[j][3]);
						face.add(cells.cells[j][2]);
						faces.add(new Face(face));
						
						//Last face consists of indexes 6, 7, 5, and 4
						faces.clear();
						face.add(cells.cells[j][6]);
						face.add(cells.cells[j][7]);
						face.add(cells.cells[j][5]);
						face.add(cells.cells[j][4]);
						faces.add(new Face(face));
						
						System.out.println("Untested cell type: VTK_VOXEL");
						break;
						
					case 12: //VTK_HEXAHEDRON
						face = new ArrayList<Integer>();
						
						//First face consists of indexes 0, 1, 2, and 3
						face.add(cells.cells[j][0]);
						face.add(cells.cells[j][1]);
						face.add(cells.cells[j][2]);
						face.add(cells.cells[j][3]);
						faces.add(new Face(face));
						
						//Second face consists of indexes 0, 3, 7, and 4
						faces.clear();
						face.add(cells.cells[j][0]);
						face.add(cells.cells[j][3]);
						face.add(cells.cells[j][7]);
						face.add(cells.cells[j][4]);
						faces.add(new Face(face));
						
						//Third face consists of indexes 3, 7, 6, and 2
						faces.clear();
						face.add(cells.cells[j][3]);
						face.add(cells.cells[j][7]);
						face.add(cells.cells[j][6]);
						face.add(cells.cells[j][2]);
						faces.add(new Face(face));
						
						//Fourth face consists of indexes 1, 2, 6, and 5
						faces.clear();
						face.add(cells.cells[j][1]);
						face.add(cells.cells[j][2]);
						face.add(cells.cells[j][6]);
						face.add(cells.cells[j][5]);
						faces.add(new Face(face));
						
						//Fifth face consists of indexes 0, 1, 5, and 4
						faces.clear();
						face.add(cells.cells[j][0]);
						face.add(cells.cells[j][1]);
						face.add(cells.cells[j][5]);
						face.add(cells.cells[j][4]);
						faces.add(new Face(face));
						
						//Last face consists of indexes 4, 5, 6, and 7
						faces.clear();
						face.add(cells.cells[j][4]);
						face.add(cells.cells[j][5]);
						face.add(cells.cells[j][6]);
						face.add(cells.cells[j][7]);
						faces.add(new Face(face));
						
						break;
					default:
						System.out.println("None of the above");
						break;
					}
				}
				
			}else if(name.equals("POINT_DATA")){
				; //Do nothing; we already have the vertices
			}
			else{
				System.err.println("Invalid group: " + name);
			}
		}
		
		mesh.setFaces(faces);
		return mesh;
	}

	public static void setColors(Color4f acolor4f[])
	{
		colors = acolor4f;
	}

	public static void setNormals(Vector3f avector3f[])
	{
		normals = avector3f;
	}

	public static void setPointCount(int i)
	{
		pointCount = i;
	}

	public static void setPoints(Point[] points2)
	{
		points.setPoints(points2);
		pointCount = points2.length;
	}

	public static final int ASCII = 0;
	public static final int BINARY = 1;
	public static final int BIG = 0;
	public static final int LITTLE = 1;
	public static final int BIG_ENDIAN = 0;
	public static final int LITTLE_ENDIAN = 1;
	public static boolean debug = false;
	int filetype;
	public static int endian = 1;
	private ReaderTokenizer reader;
	static String pckg = "edu.ncsa.model.loaders.vtk";
	static POINTS points = new POINTS();
	static int pointCount = 0;
	static Vector3f normals[] = null;
	static Color4f colors[] = null;
}
