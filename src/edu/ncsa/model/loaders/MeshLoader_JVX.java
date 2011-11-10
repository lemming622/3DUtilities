package edu.ncsa.model.loaders;
import edu.ncsa.model.loaders.dae.*;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import kgm.image.*;
import kgm.utility.*;
import org.jdom.*;
import org.jdom.input.*;

import java.io.*;
import java.util.*;
import java.awt.image.*;
import javax.imageio.*;

/**
 * A mesh loader for *.jvx files.
 * Specifications and sample models can be found at http://www.eg-models.de/formats/Format_Jvx.html
 *  @author Victoria Winner
 */
public class MeshLoader_JVX extends MeshLoader
{	
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "jvx";
	}

	/**
	 * Load a *.jvx model.
	 *  @param filename the file to load
	 *  @return the loaded mesh
	 */
	public Mesh load(String filename)
	{
		Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
		try{

			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(new File(filename));
			Element root = document.getRootElement();
			Element geo = root.getChild("geometries");
			List<Element> geometries= geo.getChildren("geometry");
			Iterator<Element> itr = geometries.iterator();
			Element pointset;
			Element point;
			Element geometry;
			Element faceset;
			Element lineset;
			Scanner sc;
			Vector<Point> vertices;
			Vector<Face> faces;
			while(itr.hasNext()){
				geometry = itr.next();
				pointset = geometry.getChild("pointSet");
				Element points = pointset.getChild("points");
				List<Element> p = points.getChildren("p");
				Iterator<Element> itr2 = p.iterator();
				vertices = new Vector<Point>();
				faces = new Vector<Face>();
				while(itr2.hasNext()){
					point = itr2.next();
					sc = new Scanner(point.getText());
					Point vertex = new Point(sc.nextDouble(), sc.nextDouble(), sc.nextDouble());
					sc.close();
					vertices.add(vertex);
				}
				if((faceset = geometry.getChild("faceSet")) != null){
					Element face = faceset.getChild("faces");
					List<Element> f = face.getChildren("f");
					itr2= f.iterator();
					while(itr2.hasNext()){
						Element current = itr2.next();
						Vector<Integer> corners = new Vector<Integer>();
						sc = new Scanner(current.getText());
						while(sc.hasNextInt()){
							corners.add(sc.nextInt());
						}
						sc.close();
						Face fa = new Face(corners.size());
						for(int i = 0; i<corners.size(); i++){
							fa.v[i] = corners.get(i);
						}
						faces.add(fa);
					}
				}
				if((lineset = geometry.getChild("lineSet")) != null){
					Element line = lineset.getChild("lines");
					List<Element> l = line.getChildren("l");
					itr2= l.iterator();
					while(itr2.hasNext()){
						Element li = itr2.next();
						sc = new Scanner(li.getText());
						Face fa = new Face(sc.nextInt(), sc.nextInt());
						sc.close();
						
						faces.add(fa);
					}
				}
				String name = geometry.getAttributeValue("name");
				mesh.addData(vertices, faces, null, name);	
			} 
		}catch(Exception e){
			e.printStackTrace();
		}
		mesh.initialize();
		return mesh;
	}
	public boolean save(String filename, Mesh mesh) {return false;}
}
