package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.Mesh;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Vector;
import ncsa.util.ReaderTokenizer;

/**
 * A mesh file loader for *.dxf files.
 *  @author Daniel Long
 */
public class MeshLoader_DXF extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "dxf";
	}
	
	/**
	 * Load an DXF model.
	 *  @param filename the file to load
	 *  @return the loaded mesh
	 */
	public Mesh load(String filename)
	{
		EOF = false;
		forceReadNextTokenAsInt = false;
		angdir = true; 
		dimaunit = 0;
		mesh = new Mesh();
		faces = new Vector<Face>();
		vertices = new Vector<Point>();
		
		try{
			FileReader reader = new FileReader(filename);
			fis = new ReaderTokenizer(reader);
			fis.ordinaryChar(35);
			fis.wordChars(34, 34);
				
			LoadDXFWaiting();
		}catch(FileNotFoundException e){
			e.printStackTrace();
			mesh = null;
		}		

		if(mesh != null){
			mesh.setVertices(vertices);
			mesh.setFaces(faces);
			mesh.addFileMetaData(filename);
			mesh.initialize();
		}

		return mesh;
	}
  
	public Mesh load(InputStream is) {return null;}
	public boolean save(String filename, Mesh mesh) {return false;}

	/**
	 * Advances through the file to the beginning of the next section (the
	 * first line after token "ENDSEC")
	 */
	private void GoToNextData()
	{
		int i = readInt(fis);
		for(String s = getLine(fis); (i != 0) || (s == null) || (s.compareTo("ENDSEC") != 0) && (EOF == false); s = getLine(fis)){
			i = readInt(fis);
		}
	}

	/** 
	 * Reads in a face from the DXF file
	 * @return The name of the next section to be processed
	 */
	private String LoadDXF3DFace()
	{
		float coords[] = new float[12];
		boolean flag = false; //Still don't know what this does
		String sectionName = null;
		
		for(int groupCode = readInt(fis); groupCode != 0; groupCode = readInt(fis)){
			switch(groupCode){	
				case 10:
				case 11:
				case 12:
				case 13:
				case 20:
				case 21:
				case 22:
				case 23:
				case 30:
				case 31:
				case 32:
				case 33:
					coords[(groupCode / 10 + (groupCode % 10) * 3) - 1] = readFloat(fis);
					if(groupCode % 10 == 3){
						flag = true;
					}
					break;
	
				default:
					getLine(fis);
					break;
			}
		}
		sectionName = getLine(fis);
		
		if(flag == false){
			coords[9] = coords[6];
			coords[10] = coords[7];
			coords[11] = coords[8];
		}
		
		//Every set of three vertices makes up a vertex
		Point v1 = new Point(coords[0], coords[1], coords[2]);
		Point v2 = new Point(coords[3], coords[4], coords[5]);
		Point v3 = new Point(coords[6], coords[7], coords[8]);
		Point v4 = new Point(coords[9], coords[10], coords[11]);
		ArrayList<Integer> face = new ArrayList<Integer>();
		int size = vertices.size();
		face.add(size);
		face.add(size + 1);
		face.add(size + 2);
		vertices.add(v1);
		vertices.add(v2);
		vertices.add(v3);
		
		//If v3 and v4 are identical, then the face is made up of
		//three vertices. Otherwise, it is made up of 4.
		if(v3 != v4){
			face.add(size + 3);
			vertices.add(v4);
		}
		faces.add(new Face(face));
		return sectionName;
	}

	/**
	 * Reads in an arc from the DXF file
	 * @return The name of the next section to be processed
	 */
	private String LoadDXFArc()
	{
		float center[] = new float[3]; //Center of the arc
		float extrusionDirection[] = new float[3];
		extrusionDirection[0] = 0;
		extrusionDirection[1] = 0;
		extrusionDirection[2] = 1;
		float radius = 1; //Radius of the arc
		float startAngle = 0; //Angles the arc makes at beginning and end
		float endAngle = 0;
		
		for(int groupCode = readInt(fis); groupCode != 0; groupCode = readInt(fis)){
			switch(groupCode){	
				case 10:
				case 20:
				case 30: //Center
					center[groupCode / 10 - 1] = readFloat(fis);
					break;
	
				case 40: //Radius
					radius = readFloat(fis);
					break;
	
				case 50: //Start angle
					startAngle = convertAngle(readFloat(fis));
					break;
	
				case 51: //End angle
					endAngle = convertAngle(readFloat(fis));
					break;
	
				case 62:
					readInt(fis); //Junk integer
					break;
	
				case 210: 
				case 220: 
				case 230: 
					extrusionDirection[(groupCode - 200) / 10 - 1] = readFloat(fis);
					break;
	
				default:
					getLine(fis);
					break;
			}
		}
		if(startAngle > endAngle){
			startAngle -= 2 * Math.PI;
		}

		if(angdir == false){
			float tmp = startAngle;
			startAngle = endAngle;
			endAngle = tmp;
		}

		//This section evaluates the arc
		//at 51 equally spaced points
		//These points are then added to the
		//mesh as a face
		ArrayList<Integer> face = new ArrayList<Integer>();
		float coords[] = new float[3];
		coords[2] = 0; //Always 0
		for(int i = 0; i <= 50; i++){
			float angle = startAngle + (i * (endAngle - startAngle)) / 50F;
			coords[0] = (float) (Math.cos(angle) * radius);
			coords[1] = (float) (Math.sin(angle) * radius);
			face.add(vertices.size());
			vertices.add(new Point(center[0] + extrusionDirection[0] * coords[2] + (extrusionDirection[1] + extrusionDirection[2]) * coords[0], center[1] + (extrusionDirection[0] + extrusionDirection[2]) * coords[1] + extrusionDirection[1] * coords[2], center[2] + extrusionDirection[0] * -coords[0] + extrusionDirection[1] * -coords[1] + extrusionDirection[2] * coords[2]));
		}
		faces.add(new Face(face));
		
		return getLine(fis);
	}

	/**
	 * Reads in a circle from the DXF file
	 * @return The name of the next section to be processed
	 */
	private String LoadDXFCircle()
	{
		float center[] = new float[3];
		float extrusionDirection[] = new float[3];
		extrusionDirection[0] = 0;
		extrusionDirection[1] = 0;
		extrusionDirection[2] = 1;
		float radius = 1;
		
		for(int groupCode = readInt(fis); groupCode != 0; groupCode = readInt(fis)){
			switch(groupCode){	
				case 10:
				case 20:
				case 30:
					center[groupCode / 10 - 1] = readFloat(fis);
					break;
	
				case 40:
					radius = readFloat(fis);
					break;
	
				case 62:
					readInt(fis);
					break;
	
				case 210: 
				case 220: 
				case 230: 
					extrusionDirection[(groupCode - 200) / 10 - 1] = readFloat(fis);
					break;
	
				default:
					getLine(fis);
					break;
			}
		}
		
		ArrayList<Integer> face = new ArrayList<Integer>();
		float coords[] = new float[3];
		coords[2] = 0;
		
		for(int i = 0; i <= 50; i++){
			float angle = (float) (i * Math.PI / 50);
			coords[0] = (float) (Math.cos(angle) * radius);
			coords[1] = (float) (Math.sin(angle) * radius);
			face.add(vertices.size());
			vertices.add(new Point(center[0] + extrusionDirection[0] * coords[2] + (extrusionDirection[1] + extrusionDirection[2]) * coords[0], center[1] + (extrusionDirection[0] + extrusionDirection[2]) * coords[1] + extrusionDirection[1] * coords[2], center[2] + extrusionDirection[0] * -coords[0] + extrusionDirection[1] * -coords[1] + extrusionDirection[2] * coords[2]));
		}
		faces.add(new Face(face));
		
		return getLine(fis);
	}

	/**
	 * Goes through the DXF file processing entities (ARC, CIRCLE, etc.) until the
	 * end of the section (token "ENDSEC") is reached
	 */
	private void LoadDXFData()
	{
		int i = readInt(fis);
		String sectionName = getLine(fis);
			
		if((i == 0) && (sectionName.equals("ENDSEC"))){
			return;
		}
			
		while(sectionName.equals("ENDSEC") == false){
			if(sectionName.equals("3DFACE")){
				sectionName = LoadDXF3DFace();
			}else if(sectionName.equals("ARC")){
				sectionName = LoadDXFArc();
			}else if(sectionName.equals("CIRCLE")){
				sectionName = LoadDXFCircle();
			}else if(sectionName.equals("ELLIPSE")){
				sectionName = LoadDXFEllipse();
			}else if(sectionName.equals("LINE")){
				sectionName = LoadDXFLine();
			}else if(sectionName.equals("POLYLINE")){
				sectionName = LoadDXFPolyline();
			}else if(sectionName.equals("SOLID")){
				sectionName = LoadDXFSolid();
			}else{
				sectionName = UseNextData();
			}
		}
	}

	/**
	 * Reads in an ellipse from the DXF file
	 * This section is untested. I have yet to find a
	 * DXF file which requires it.
	 * @return The name of the next section to be processed
	 */
	private String LoadDXFEllipse()
	{
		float center[] = new float[3];
		float majorAxis[] = new float[3]; //Endpoint of major axis, relative to center
		float ratio = 1; //Ratio of major axis to minor axis
		float extrusionDirection[] = new float[3];
		extrusionDirection[0] = 0;
		extrusionDirection[1] = 0;
		extrusionDirection[2] = 1;
		float startAngle = 0;
		float endAngle = (float) (2 * Math.PI);
		
		for(int groupCode = readInt(fis); groupCode != 0; groupCode = readInt(fis)){
			switch(groupCode){
				case 8:
				case 62:
					break;
		
				case 10:
				case 20:
				case 30:
					center[groupCode / 10 - 1] = readFloat(fis);
					break;
		
				case 11:
				case 21:
				case 31:
					majorAxis[groupCode / 10 - 1] = readFloat(fis);
					break;
		
				case 40:
					ratio = readFloat(fis);
					break;
		
				case 41:
					startAngle = convertAngle(readFloat(fis));
					break;
	
				case 42:
					endAngle = convertAngle(readFloat(fis));
					break;
	
				case 210: 
				case 220: 
				case 230: 
					extrusionDirection[(groupCode - 200) / 10 - 1] = readFloat(fis);
					break;
		
				default:
					getLine(fis);
					break;
			}
		}

		float distance = (float) Math.sqrt(majorAxis[0] * majorAxis[0] + majorAxis[1] * majorAxis[1] + majorAxis[2] * majorAxis[2]);
		//Euclidean distance between the center and the endpoint of the major axis
		float af4[] = new float[3];
		af4[2] = 0;
		float af5[] = new float[3];
		//Not quite sure exactly how af4 and af5 work
		//but they are used to generate the coordinates x, y, and z
			
		ArrayList<Integer> al = new ArrayList<Integer>();
		for(int i = 0; i <= 50; i++){
			float angle = (float) (startAngle + (i * (endAngle - startAngle)) / 50);
			af4[0] = (float) (Math.cos(angle) * distance);
			af4[1] = (float) (Math.sin(angle) * ratio * distance);
			af5[0] = (af4[0] * majorAxis[0] + af4[1] * -majorAxis[1] + af4[2] * -majorAxis[2]) / distance;
			af5[1] = ((af4[0] + af4[2]) * majorAxis[1] + af4[1] * majorAxis[0]) / distance;
			af5[2] = ((af4[0] + af4[1]) * majorAxis[2] + af4[2] * majorAxis[0]) / distance;
			al.add(vertices.size());
			vertices.add(new Point(center[0] + extrusionDirection[0] * af5[2] + (extrusionDirection[1] + extrusionDirection[2]) * af5[0], center[1] + (extrusionDirection[0] + extrusionDirection[2]) * af5[1] + extrusionDirection[1] * af5[2], center[2] + extrusionDirection[0] * -af5[0] + extrusionDirection[1] * -af5[1] + extrusionDirection[2] * af5[2]));
		}
		faces.add(new Face(al));

		return getLine(fis);
	}

	/**
	 * Reads in the header of the DXF File
	 */
	private void LoadDXFHeader()
	{
		int i = readInt(fis);
		for(String s = readString(fis); (i != 0) || (s.equals("ENDSEC") == false); s = readString(fis)){
			if(s.equals("$ANGDIR")){
				nextLine(fis);
				angdir = (readInt(fis) != 0);
			}else if(s.equals("$DIMAUNIT")){
				nextLine(fis);
				dimaunit = readInt(fis);
			}else if(s.equals("$SHADEDIF")){
				nextLine(fis);
				readFloat(fis);
			}
			i = readInt(fis);
		}
	}

	/**
	 * Reads in one line segment from the DXF file
	 * @return The name of the next section to be processed
	 */
	private String LoadDXFLine()
	{
		float coords[] = new float[6];
			
		for(int groupCode = readInt(fis); groupCode != 0; groupCode = readInt(fis)){
			switch(groupCode){	
				case 10:
				case 11:
				case 20:
				case 21:
				case 30:
				case 31:
					coords[(groupCode / 10 + 3 * (groupCode % 10)) - 1] = readFloat(fis);
					break;
		
				case 39:
					readFloat(fis);
					break;
						
				case 62:
					readInt(fis);
					break;
		
				default:
					getLine(fis);
					break;
			}
		}
		
		//Add a line from (af[0], af[1], af[2]) to (af[3], af[4], af[5])
		//This is implemented as a face with only two vertices
		vertices.add(new Point(coords[0], coords[1], coords[2]));
		vertices.add(new Point(coords[3], coords[4], coords[5]));
		ArrayList<Integer> face = new ArrayList<Integer>();
		face.add(vertices.size() - 2);
		face.add(vertices.size() - 1);
		faces.add(new Face(face));
		return getLine(fis);
	}

	/**
	 * Reads in a polyline from the DXF file
	 * @return The name of the next section to be processed
	 */
	private String LoadDXFPolyline()
	{		
		int flag1 = 0;
		ArrayList<Point> points = new ArrayList<Point>();
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		int groupCode = 1;
			
		for(groupCode = readInt(fis); groupCode != 0; groupCode = readInt(fis)){
			switch(groupCode){
				case 8:
					getLine(fis);
					break;
	
				case 10:
				case 20:
				case 30:
					readFloat(fis);
					break;
		
				case 62:
					readInt(fis);
					break;
		
				case 70:
					flag1 = readInt(fis);
					break;
		
				default:
					break;
			}
		}

		String sectionName = getLine(fis);
			
		while((groupCode != 0) || (sectionName == null) || (sectionName.equals("SEQEND") == false)){
			float coords[] = new float[3];
			int ai[] = new int[4];
			for(int i = 0; i < 4; i++){
				ai[i] = -1;
			}

			int flag2 = 0;
			for(groupCode = readInt(fis); groupCode != 0; groupCode = readInt(fis)){
				switch(groupCode){
					case 10:
					case 20:
					case 30:
						coords[groupCode / 10 - 1] = readFloat(fis);
						break;
	
					case 70:
						flag2 = readInt(fis);
						break;
	
					case 71:
					case 72:
					case 73:
					case 74:
						//Not sure what ai does
						//According to the DXF documentation
						//group codes 71 - 74 are used to define
						//the mesh vertex counts and surface densities
						//but that doesn't seem quite right
						ai[groupCode - 71] = readInt(fis);
						if(ai[groupCode - 71] < 0){
							ai[groupCode - 71] *= -1;
						}
						break;
		
					default:
						sectionName = getLine(fis);
						break;
				}
			}
			sectionName = getLine(fis);
				
			//Still not quite sure what the bits in each flag represent
			if(((flag1 & 0x40) > 0) && ((flag2 & 0xC0) == 128)){
				if(ai[3] <= 0){
					ai[3] = ai[2];
				}
				indexes.add(ai[0] - 1);
				indexes.add(ai[1] - 1);
				indexes.add(ai[2] - 1);
				indexes.add(ai[3] - 1);
			}else{
				points.add(new Point(coords[0], coords[1], coords[2]));
			}
		}

		if((flag1 & 0x40) == 0){
			ArrayList<Integer> face = new ArrayList<Integer>();
			for(int i = 0; i < points.size(); i++){
				face.add(vertices.size());
				vertices.add(new Point(points.get(i)));
			}
		}else{
			ArrayList<Integer> face = new ArrayList<Integer>();
			for(int i = 0; i < indexes.size(); i++){
				int index = indexes.get(i);
				if(index == -1){
					index = indexes.get(i - 1);
				}
				face.add(vertices.size());
				vertices.add(new Point(points.get(index)));
			}
			faces.add(new Face(face));
		}
		
		return UseNextData();
	}

	/**
	 * Determines the next DXF section to be processed and processes it
	 */
	private void LoadDXFSection()
	{
		int i = readInt(fis);
		String sectionName = readString(fis);
		if(i == 2){
			if(sectionName.equals("ENTITIES")){
				LoadDXFData();
			}else if(sectionName.equals("HEADER")){
				LoadDXFHeader();
			}else{
				GoToNextData();
			}
		}
	}

	/**
	 * Reads in a solid from the DXF file
	 * This section is untested. I have yet to find a
	 * DXF file which requires it.
	 * @return The name of the next section to be processed
	 */
	private String LoadDXFSolid()
	{
		float coords[] = new float[12];
		boolean flag = false;
		
		for(int groupCode = readInt(fis); groupCode != 0; groupCode = readInt(fis)){
			switch(groupCode){
				case 8:
					getLine(fis);
					break;
	
				case 10:
				case 11:
				case 12:
				case 13:
				case 20:
				case 21:
				case 22:
				case 23:
				case 30:
				case 31:
				case 32:
				case 33:
					coords[(groupCode / 10 + (groupCode % 10) * 3) - 1] = readFloat(fis);
					if(groupCode % 10 == 3){
						flag = true;
					}
					break;
	
				case 62:
					readInt(fis);
					break;
	
				default:
					getLine(fis);
					break;
			}
		}
		if(flag == false){
			coords[9] = coords[6];
			coords[10] = coords[7];
			coords[11] = coords[8];
		}
		
		ArrayList<Integer> face = new ArrayList<Integer>();
		for(int k = 0; k < 4; k++){
			face.add(vertices.size());
			vertices.add(new Point(coords[3 * k], coords[3 * k + 1], coords[3 * k + 2]));
		}
		faces.add(new Face(face));
		return getLine(fis);
	}

	/**
	 * Determines if another section exists in the DXF file (token "SECTION")
	 * or if we have reached the end of the file (token "EOF")
	 */
	private void LoadDXFWaiting()
	{
		while(true){
			int i = readInt(fis);
			String s = readString(fis);
			if(i == 0){
				if(s.equals("SECTION")){
					LoadDXFSection();
				}else if(s.equals("EOF")){
					break;
				}else{
					System.err.println("Error! File does not conform to DXF specs.");
					EOF = true;
				}
			}
			if(EOF == true){
				break;
			}
		}
	}

	/**
	 * Returns the next non-numeric token in the file
	 * @return The token
	 */
	private String UseNextData()
	{
		int i = readInt(fis);
		String s;
		for(s = getLine(fis); i != 0; s = getLine(fis)){
			i = readInt(fis);
		}
		return s;
	}

	/**
	 * Converts an angle to radians
	 * The units in which the original angle is in is determined
	 * by the global variable dimaunit
	 * @param angle The angle to be converted
	 * @return The converted angle
	 */
	private float convertAngle(float angle)
	{
		float result = 0;
		switch(dimaunit){
			case 0:
			case 1:
				//Convert from degrees to radians
				result = (float)(angle / 180 * Math.PI);
				break;
	
			case 2:
			case 4:
				//Convert from gradians to radians
				result = (float)(angle / 200 * Math.PI);
				break;
		
			default:
				//Angle is already in radians
				result = angle;
				break;
		}
		return result;
	}

	private String getLine(ReaderTokenizer readertokenizer)
	{
		readertokenizer.parseNumbersAsWords();
		readertokenizer.wordChars(32, 32);
		readertokenizer.wordChars(9, 9);
		int i = readertokenizer.lineno;
		nextLine(readertokenizer);
		String s = readertokenizer.sval;
		readertokenizer.whitespaceChar(9);
		readertokenizer.whitespaceChar(32);
		readertokenizer.parseNumbers();
		if(readertokenizer.lineno > i + 1)
		{
			readertokenizer.pushBack();
			s = "";
			forceReadNextTokenAsInt = true;
		}
		return s;
	}

	private void nextLine(ReaderTokenizer readertokenizer)
	{
		readertokenizer.nextToken();
		if(readertokenizer.ttype == -103)
			EOF = true;
	}

	private float readFloat(ReaderTokenizer readertokenizer)
	{
		int i = readertokenizer.lineno;
		float f = (float) readertokenizer.nval;
		nextLine(readertokenizer);
		float f1 = (float) readertokenizer.nval;
		if(readertokenizer.lineno > i + 1){
			readertokenizer.pushBack();
			f1 = f;
		}
		return f1;
	}

	private int readInt(ReaderTokenizer readertokenizer)
	{
		int i = readertokenizer.lineno;
		int j = (int) readertokenizer.nval;
		nextLine(readertokenizer);
		int k = (int) readertokenizer.nval;
		if(forceReadNextTokenAsInt){
			forceReadNextTokenAsInt = false;
			if(readertokenizer.ttype == -101){
				try{
					k = Integer.valueOf(readertokenizer.sval.trim()).intValue();
				}catch(NumberFormatException _ex){}
			}
		}
		if(readertokenizer.lineno > i + 1){
			readertokenizer.pushBack();
			k = j;
		}
		return k;
	}

	private String readString(ReaderTokenizer readertokenizer)
	{
		int i = readertokenizer.lineno;
		nextLine(readertokenizer);
		String s = readertokenizer.sval;
		if(readertokenizer.lineno > i + 1){
			readertokenizer.pushBack();
			s = "";
		}
		return s;
	}

	private ReaderTokenizer fis;
	private boolean EOF; //Indicates whether or not EOF has been reached
	private boolean forceReadNextTokenAsInt; //?
	private boolean angdir; //Determines whether angles are clockwise or counterclockwise
	private int dimaunit; //Determines what units angles are in (degrees, radians, or gradians)
	
	private Mesh mesh;
	private Vector<Face> faces;
	private Vector<Point> vertices;
}