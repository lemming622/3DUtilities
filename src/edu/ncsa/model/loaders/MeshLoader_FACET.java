package edu.ncsa.model.loaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Scanner;
import java.util.Vector;

import edu.ncsa.model.Mesh;
import edu.ncsa.model.MeshAuxiliary.Face;
import edu.ncsa.model.MeshAuxiliary.Point;
import edu.ncsa.model.MeshLoader;

/**
 * A mesh file loader for *.facet files. Facet File Format 
 * the following web page has a description of the FACET file format
 * http://cubit.sandia.gov/help-version12.0/geometry/import/importing_facet.htm
 * 
 * However, the format description on the web page does not match the Matlab implementation
 * that was available with the sample FACET file. THus, the java implementation follows 
 * the Matlab implementation and deviates from the description below:
 *  *******************************
 * The format for the ASCII facet file is as follows
 * 
 * n m 
 * id1 x1 y1 z1 
 * id2 x2 y2 z2 
 * id3 x3 y3 z3 . . . 
 * idn xn yn zn 
 * fid1 id<1> id<2> id<3> [id<4>] 
 * fid2 id<1> id<2> id<3> [id<4>] 
 * fid3 id<1> id<2> id<3> [id<4>] . . . 
 * fidm id<1> id<2> id<3> [id<4>]
 * 
 * Where:
 * 
 * n = number of vertices
 * m = number of facet
 * id<i> = vertex ID if vertex i 
 * x<i> y<i> z<i> = location of vertex i 
 * fid<j> = facet ID if facet j 
 * id<1> id<2> id<3> = IDs of facet vertices 
 * [id<4>] = optional fourth vertex for quads
 * 
 * As noted above, the facets can be either quadrilaterals or triangles. Upon
 * import, the facets serve as the underlying representation for the geometry.
 * By default, the facets are not visible once the geometry has been imported.
 * To view the facets, use the following command:
 * *******************************
 * 
 * 
 * 
 * @author Peter Bajcsy
 * 
 */
public class MeshLoader_FACET extends MeshLoader {
	/**
	 * Get the type of file this loader loads.
	 * 
	 * @return the type loaded
	 */
	@Override
	public String type() {
		return "facet";
	}

	/**
	 * Load a stereolithograph model.
	 * 
	 * @param filename
	 *            the file to load
	 * @return the loaded mesh
	 */
	@Override
	public Mesh load(String filename) {
		Mesh mesh = new Mesh();
		mesh.addFileMetaData(filename);
		Vector<Point> vertices = new Vector<Point>();
		Vector<Face> faces = new Vector<Face>();

		//Open file and read in vertices/faces
		Scanner sc;
		String token;
		int[] n_faces = new int[2];
		int n_facet = 0;
		int n_vertex = 0;
		int n_mobject = 0;
		int n_current_vertex = 0;
		int n_object = 0;
		int i, j, k, m;
		int offset = 0;
		boolean debug = false;

		try {
			BufferedReader ins = new BufferedReader(new FileReader(filename));
			sc = new Scanner(ins.readLine());
			token = sc.next();
			if (!"FACET".equalsIgnoreCase(token)) {
				System.err.println("The file does not start with the FACET label");
				return null;
			}
			sc.close();

			sc = new Scanner(ins.readLine());
			n_mobject = sc.nextInt();
			System.out.println("n_mobject =" + n_mobject);
			sc.close();

			for (k = 0; k < n_mobject; k++) {
				// should be just ins.readLine() after debugging
				ins.readLine();
				ins.readLine();

				// Get the number of vertices
				sc = new Scanner(ins.readLine());
				n_current_vertex = sc.nextInt();
				if (debug) {
					System.out.println("n_current_vertex =" + n_current_vertex);
				}
				sc.close();
				n_vertex = n_vertex + n_current_vertex;

				// Load the vertex data
				for (i = 0; i < n_current_vertex; i++) {
					sc = new Scanner(ins.readLine());
					// The three remaining tokens are the vertex coordinates
					float vx = sc.nextFloat();
					float vy = sc.nextFloat();
					float vz = sc.nextFloat();

					//test
					if (debug && (i == 0)) {
						System.out.println("vx =" + vx);
						System.out.println("vy =" + vy);
						System.out.println("vz =" + vz);
					}
					vertices.add(new Point(vx, vy, vz));
					sc.close();
				}

				// Get the number of objects built from the vertices
				sc = new Scanner(ins.readLine());
				n_object = sc.nextInt();
				sc.close();
				if (debug) {
					System.out.println(" number of objects =" + n_object);
				}

				for (i = 0; i < n_object; i++) {
					//Skip one line 
					ins.readLine();

					sc = new Scanner(ins.readLine());
					n_faces[0] = sc.nextInt();
					n_faces[1] = sc.nextInt();
					n_facet = n_facet + n_faces[0];
					if (debug) {
						System.out.println("n_faces[0] =" + n_faces[0]);
						System.out.println("n_faces[1] =" + n_faces[1]);
					}
					sc.close();

					// the faces are not initialize to zero but it should be fine
					for (j = 0; j < n_faces[0]; j++) {
						sc = new Scanner(ins.readLine());
						Face myFace = new Face(n_faces[1]);
						for (m = 0; m < n_faces[1]; m++) {
							int idx = sc.nextInt();
							myFace.v[m] = idx + offset - 1;
						}
						faces.add(myFace);
						sc.close();
					}
				}
				offset = vertices.size();
			}
			ins.close();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		mesh.setVertices(vertices);
		mesh.setFaces(faces);
		mesh.initialize();

		return mesh;
	}

	public Mesh load(InputStream is) {
		return null;
	}

	@Override
	public boolean save(String filename, Mesh mesh) {
		return false;
	}

	/**
	 * 
	 * @param Scanner
	 *            class sc after reading a line This method is for debugging
	 *            purposes However, the reset function does not work as needing
	 *            and therefore the pointer is not reset to the beginning of the
	 *            stream
	 */
	private static void printLine(Scanner sc) {
		String token = null;
		while (sc.hasNext()) {
			token = sc.next();
			System.out.print(token + " ");
		}
		System.out.println();
		sc.reset();
	}
}
