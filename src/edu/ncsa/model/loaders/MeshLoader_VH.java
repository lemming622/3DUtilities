package edu.ncsa.model.loaders;
import edu.ncsa.model.*;
import edu.ncsa.model.loaders.vh.*;
import edu.ncsa.model.ImageUtility.Pixel;
import edu.ncsa.model.MeshAuxiliary.*;
import edu.ncsa.model.matrix.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;
import java.util.*;

/**
 * A mesh file loader for *.vh files.
 *  @author Kenton McHenry
 */
public class MeshLoader_VH extends MeshLoader
{
	/**
	 * Get the type of file this loader loads.
	 *  @return the type loaded
	 */
	public String type()
	{
		return "vh";
	}
	
  /**
   * Load a model stored as a visual hull.
   *  @param filename the file to load
   *  @return the loaded mesh
   */
  public Mesh load(String filename)
  {
  	Mesh mesh = new Mesh(); mesh.addFileMetaData(filename);
  	
    Vector<Camera> cameras;
    Vector<Camera> cameras_gt = null;
    Vector<BufferedImage> images = new Vector<BufferedImage>();
    Vector<double[][]> masks = new Vector<double[][]>();
    Vector<Vector<Vector<Point>>> silhouettes = new Vector<Vector<Vector<Point>>>();
    String path = filename + "/";
    BufferedImage image = null;
    double x, y;
    
    //Load camera parameters
    if(!Utility.exists(path + "cameras.txt")) Camera.constructCameras(path);
    cameras = Camera.loadCameras(path + "cameras.txt");
    
    if(Utility.exists(path + "cameras.txt.gt")) cameras_gt = Camera.loadCameras(path + "cameras.txt.gt");
    
    //Load images
    System.out.println();
    
    for(int c=0; c<cameras.size(); c++){
      if(Utility.exists(path + cameras.get(c).name + ".jpg")){
        filename = path + cameras.get(c).name;
        System.out.println(" + Loading: " + filename + ".{jpg,pbm}");
        
        //Load color image
        try{
          image = ImageIO.read(new File(filename + ".jpg"));
          images.add(image);
        }catch(Exception e){
          e.printStackTrace();
        }
        
        //Load mask
        masks.add(ImageUtility.load_PBM(filename + ".pbm"));
        silhouettes.add(Point.getVertices(ImageUtility.getRegionBorders(MatrixUtility.to1D(masks.lastElement()), image.getWidth(), image.getHeight(), true)));
      }else{
        images.add(null);
        masks.add(null);
        silhouettes.add(null);
      }
    }

    //View loaded data
    if(true){
      mesh.addCameras(cameras, 1000, new Color(0.5f, 0.5f, 1), new Color(1, 1, 0));
      if(cameras_gt != null) mesh.addCameras(cameras_gt, 1000, new Color(0.5f, 0.5f, 1), new Color(0, 1, 0));
      
      //ImageViewer.show(images, "Texture");
      //ImageViewer.show(masks, "Masks");
      //ImageViewer.show(ImageUtils.drawMultiplePointGroups(borders, images.get(0).getWidth(), images.get(0).getHeight()), images.get(0).getWidth(), images.get(0).getHeight(), "Borders");
    }
    
    //Create cone faces for each camera
    Vector<Vector<Point[]>> cone_faces = new Vector<Vector<Point[]>>();
    Vector<Vector<Vector<Point>>> cone_rays = new Vector<Vector<Vector<Point>>>();
    Utility.Pair<Point,Point> r;
    Point v0, v1;
    double scl = 2500;    //This should be set automatically!
    int at;
    
    for(int c=0; c<silhouettes.size(); c++){
      cone_faces.add(new Vector<Point[]>());
      cone_rays.add(new Vector<Vector<Point>>());
      v0 = cameras.get(c).getPosition();
      
      for(int i=0; i<silhouettes.get(c).size(); i++){
        cone_rays.get(c).add(new Vector<Point>());
        at = cone_faces.get(c).size();
        
        for(int j=0; j<silhouettes.get(c).get(i).size(); j++){
          x = silhouettes.get(c).get(i).get(j).x;
          y = silhouettes.get(c).get(i).get(j).y;
          r = cameras.get(c).getRay(x, y);
          v1 = new Point((scl*r.second.x)+r.first.x, (scl*r.second.y)+r.first.y, (scl*r.second.z)+r.first.z);
          
          cone_faces.get(c).add(new Point[3]);
          cone_faces.get(c).get(at+j)[0] = v0;
          cone_faces.get(c).get(at+j)[1] = v1;
          cone_rays.get(c).get(i).add(v1);
        }
        
        for(int k=0; k<silhouettes.get(c).get(i).size()-1; k++){
          cone_faces.get(c).get(at+k)[2] = cone_faces.get(c).get(at+k+1)[1];
        }
        
        cone_faces.get(c).lastElement()[2] = cone_faces.get(c).get(0)[1];
      }
    }

    //Append cone faces to mesh
    if(false || cameras.size()==1){
    	Vector<Point> vertices = new Vector<Point>();
    	Vector<Face> faces = new Vector<Face>();
    	
      for(int c=0; c<cone_faces.size(); c++){
        for(int f=0; f<cone_faces.get(c).size(); f++){
          for(int i=0; i<3; i++){
            vertices.add(new Point(cone_faces.get(c).get(f)[i]));
          }
          
          if(false){
            Face tmpf = new Face(2);
            tmpf.v[0] = vertices.size()-3;
            tmpf.v[1] = vertices.size()-2;
            faces.add(tmpf);
          }else{
            Face tmpf = new Face(3);
            tmpf.v[0] = vertices.size()-3;
            tmpf.v[1] = vertices.size()-2;
            tmpf.v[2] = vertices.size()-1;
            faces.add(tmpf);
          }
        }
      }
      
      mesh.addData(vertices, faces, new Color(0.5f, 0.5f, 0.5f));
    }
    
    //Build edge bins for each camera pair
    Vector<Vector<EdgeBin>> edge_bins = new Vector<Vector<EdgeBin>>();
    Point r0, r1;
    
    for(int ca=0; ca<cameras.size(); ca++){
      edge_bins.add(new Vector<EdgeBin>());
      
      //Build all the edge bins that camera-A will need to use to clip it's visual cone
      for(int cb=0; cb<cameras.size(); cb++){
        if(ca != cb){
          edge_bins.get(ca).add(new EdgeBin(cameras.get(cb), cameras.get(ca)));
          
          //Set the bins
          for(int i=0; i<silhouettes.get(cb).size(); i++){
            for(int j=0; j<silhouettes.get(cb).get(i).size(); j++){
              edge_bins.get(ca).get(cb).addBin(silhouettes.get(cb).get(i).get(j));
            }
          }
          
          //Fill the bins
          for(int i=0; i<silhouettes.get(cb).size(); i++){
            for(int j=0; j<silhouettes.get(cb).get(i).size(); j++){
              edge_bins.get(ca).get(cb).addToBin(silhouettes.get(cb).get(i), j);
            }
          }
        }else{
          edge_bins.get(ca).add(null);
        }
      }
    }
    
    //View edge bins
    if(false){
      ImageViewer debug_viewer = new ImageViewer();
      int[] img;
      Vector<Double> bins; 
      Point epipole = null;
    
      for(int ca=0; ca<cameras.size(); ca++){
        for(int cb=0; cb<cameras.size(); cb++){
          if(ca != cb){
            //View edge bins
            img = ImageUtility.getNewARGBImage(images.get(cb).getWidth(), images.get(cb).getHeight(), 0xffffffff);
            bins = edge_bins.get(ca).get(cb).getBins();
            epipole = edge_bins.get(ca).get(cb).getEpipole();
            
            for(int i=0; i<bins.size(); i++){
              ImageUtility.drawLine(img, images.get(cb).getWidth(), images.get(cb).getHeight(), new Pixel(epipole.x, epipole.y), bins.get(i), 0xffff0000);
            }
            
            //View projected rays            
            for(int f=0; f<cone_faces.get(ca).size(); f++){
              r0 = cone_faces.get(ca).get(f)[1];
              r1 = cone_faces.get(ca).get(f)[2];
              r0 = Point.transform(cameras.get(cb).getM(), r0);              
              r1 = Point.transform(cameras.get(cb).getM(), r1);              
              ImageUtility.drawLine(img, images.get(cb).getWidth(), images.get(cb).getHeight(), new Pixel(epipole.x, epipole.y), edge_bins.get(ca).get(cb).getBin(r0), 0xff0000ff);
              ImageUtility.drawLine(img, images.get(cb).getWidth(), images.get(cb).getHeight(), new Pixel(epipole.x, epipole.y), edge_bins.get(ca).get(cb).getBin(r1), 0xff0000ff);
            }
            
            //Render edge bins and projected rays
            Point.drawPointGroups(img, silhouettes.get(cb), images.get(cb).getWidth(), images.get(cb).getHeight(), 0xff000000);
            debug_viewer.add(img, images.get(cb).getWidth(), images.get(cb).getHeight(), false);
          }
        }
      }
    }
    
    //Intersect cone faces with camera silohuetes and build mesh faces
    Vector<Vector<Vector<PolygonGroup>>> cone_face_polygons = new Vector<Vector<Vector<PolygonGroup>>>();

    if(false){
      PolygonGroup pg;
      
      for(int ca=0; ca<cameras.size(); ca++){
        cone_face_polygons.add(new Vector<Vector<PolygonGroup>>());
        
        for(int cb=0; cb<cameras.size(); cb++){
          if(ca != cb){
            for(int f=0; f<cone_faces.get(ca).size(); f++){
              cone_face_polygons.get(ca).add(new Vector<PolygonGroup>());
              r0 = cone_faces.get(ca).get(f)[1];
              r1 = cone_faces.get(ca).get(f)[2];
              pg = edge_bins.get(ca).get(cb).getPolygons(r0, r1);
              cone_face_polygons.get(ca).get(f).add(pg);
            }
          }
        }
      }
    }else{
      Vector<PolygonGroup> tmpv = null;
      
      //Add rays
      for(int ca=0; ca<cameras.size(); ca++){        
        for(int cb=0; cb<cameras.size(); cb++){
          if(ca != cb){
            for(int i=0; i<cone_rays.get(ca).size(); i++){
              for(int j=0; j<cone_rays.get(ca).get(i).size(); j++){
                r0 = cone_rays.get(ca).get(i).get(j);
                edge_bins.get(ca).get(cb).addRay(i, r0);
              }
            }
          }
        }
      }
      
      //Get polygons
      for(int ca=0; ca<cameras.size(); ca++){
        cone_face_polygons.add(new Vector<Vector<PolygonGroup>>());

        for(int cb=0; cb<cameras.size(); cb++){
          if(ca != cb){
            tmpv = edge_bins.get(ca).get(cb).getPolygons();
            
            if(cone_face_polygons.get(ca).isEmpty()){
              for(int f=0; f<tmpv.size(); f++){
                cone_face_polygons.get(ca).add(new Vector<PolygonGroup>());
                cone_face_polygons.get(ca).get(f).add(tmpv.get(f));
              }
            }else{
              for(int f=0; f<tmpv.size(); f++){
                cone_face_polygons.get(ca).get(f).add(tmpv.get(f));
              }
            }
          }
        }  
      }
    }
    
    //Add clipped cone face polygons to the mesh
    if(false){
      Vector<PolygonGroup> polygons;
      
      for(int ca=0; ca<cameras.size(); ca++){
        for(int f=0; f<cone_faces.get(ca).size(); f++){
          polygons = cone_face_polygons.get(ca).get(f);
          
          for(int i=0; i<polygons.size(); i++){
            for(int j=0; j<polygons.get(i).size(); j++){
              mesh.addData(polygons.get(i).get(j).getVertices(), polygons.get(i).get(j).getConvexPolygonFaces(), new Color(0.5f, 0.5f, 0.5f));
            }
          }
        }
      }
    }
    
    //Add intersection of a cone face's polygons to the mesh
    if(cameras.size() > 1){
	    if(false){								//Non-textured version
	      Vector<PolygonGroup> polygons;
	      PolygonGroup pg;
	      Point[] face;
	      
	      for(int ca=0; ca<cameras.size(); ca++){
	        for(int f=0; f<cone_faces.get(ca).size(); f++){
	          face = cone_faces.get(ca).get(f);
	          polygons = cone_face_polygons.get(ca).get(f);
	          pg = EdgeBin.getPlanarIntersection(polygons, face[0], face[1], face[2]);
	          
	          for(int i=0; i<pg.size(); i++){
	            mesh.addData(pg.get(i).getVertices(), pg.get(i).getConvexPolygonFaces(), new Color(0.5f, 0.5f, 0.5f));
	          }
	        }
	      }
	    }else if(false){					//Textured version
	      Vector<PolygonGroup> polygons;
	      PolygonGroup pg;
	      Point[] face;
	      Vector<Point> vertices;
	      Vector<Face> faces;
	      Material material;
	      Point tmpv;
	      int[] img;
	      int w, h, n, tid;
	      
	      //Get the first image to use as a texture map
		    w = images.get(0).getWidth(null);
		    h = images.get(0).getHeight(null);
		    img = new int[w*h];
		    images.get(0).getRGB(0, 0, w, h, img, 0, w);
		    
		    //Make sure image dimensions are a power of two
		  	n = (w > h) ? w : h;
		  	n = (int)Math.round(Math.pow(2, Math.ceil(Math.log(n)/Math.log(2))));
		    img = ImageUtility.resize(img, w, h, n, n);
		    
		    //Add the image as a texuture to the mesh
		    tid = mesh.addTexture("image0", img, n);      
	      material = new Material(tid);
	      
		    //Add cone faces
	      for(int ca=0; ca<cameras.size(); ca++){
	        for(int f=0; f<cone_faces.get(ca).size(); f++){
	          face = cone_faces.get(ca).get(f);
	          polygons = cone_face_polygons.get(ca).get(f);
	          pg = EdgeBin.getPlanarIntersection(polygons, face[0], face[1], face[2]);
	
	          for(int i=0; i<pg.size(); i++){
	          	vertices = pg.get(i).getVertices();
	          	faces = pg.get(i).getConvexPolygonFaces();
	          	
	          	//Set texture coordinates in faces
	          	for(int j=0; j<faces.size(); j++){
	          		faces.get(j).uv = new UV[faces.get(j).v.length];
	          		
	          		for(int k=0; k<faces.get(j).v.length; k++){
	          			tmpv = Point.transform(cameras.get(0).getM(), vertices.get(faces.get(j).v[k]));
	          			faces.get(j).uv[k] = new UV((float)tmpv.x/w, 1-(float)tmpv.y/h);
	          		}
	          	}
	          	
	            mesh.addData(vertices, faces, material, null);
	          }
	        }
	      }
	    }else if(true){						//View dependent texturing version
	      Vector<PolygonGroup> polygons;
	      PolygonGroup pg;
	      Point[] face;
	      Vector<Point> vertices;
	      Vector<Face> faces;
	      Vector<TreeSet<Integer>> visible_faces = new Vector<TreeSet<Integer>>();
	      Vector<Material> materials = new Vector<Material>();
	      Point center, normal, direction, point;
	      int[] img;
	      int w, h, n, tid, max_c;
	      double theta, max_theta;
	      boolean USE_VISIBILITY = false;
	      
	      //Add images as textures
	      for(int c=0; c<images.size(); c++){
			    w = images.get(c).getWidth(null);
			    h = images.get(c).getHeight(null);
			    img = new int[w*h];
			    images.get(c).getRGB(0, 0, w, h, img, 0, w);
			    
			    //Make sure image dimensions are a power of two
			  	n = (w > h) ? w : h;
			  	n = (int)Math.round(Math.pow(2, Math.ceil(Math.log(n)/Math.log(2))));
			    img = ImageUtility.resize(img, w, h, n, n);
			    
			    //Add to the mesh
			    tid = mesh.addTexture("image" + Utility.toString(c,2), img, n);      
		      materials.add(new Material(tid));
	      }
	      
		    //Add cone faces
	      for(int ca=0; ca<cameras.size(); ca++){
	        for(int f=0; f<cone_faces.get(ca).size(); f++){
	          face = cone_faces.get(ca).get(f);
	          polygons = cone_face_polygons.get(ca).get(f);
	          pg = EdgeBin.getPlanarIntersection(polygons, face[0], face[1], face[2]);
	          
	          for(int i=0; i<pg.size(); i++){
	            mesh.addData(pg.get(i).getVertices(), pg.get(i).getConvexPolygonFaces(), null);
	          }
	        }
	      }
	      
	      //Get complete mesh data
	      vertices = mesh.getVertices();
	      faces = mesh.getFaces();
	      
	      //Get face visiblity data
	      if(USE_VISIBILITY){
		      for(int c=0; c<cameras.size(); c++){
		      	visible_faces.add(mesh.getVisibleFaces(cameras.get(c).getRT(), images.get(c).getWidth(null), images.get(c).getHeight(null)));
		      }
	      }
	      
	      //Select a best camera/view for each face
	      for(int f=0; f<faces.size(); f++){
	      	if(faces.get(f).v.length >= 3){
		      	center = faces.get(f).center(vertices);
		      	normal = faces.get(f).normal(vertices);
		      	
		      	max_c = -1;
		      	max_theta = 0;
		      	
		      	for(int c=0; c<cameras.size(); c++){
		      		if(!USE_VISIBILITY || visible_faces.get(c).contains(f)){
			      		direction = cameras.get(c).getPosition().minus(center);
			      		direction.divideEquals(direction.magnitude());
			      		theta = Math.abs(direction.times(normal));
			      		if(Double.isNaN(theta)) theta = 0;
			      		
			      		if(theta > max_theta){
			      			max_c = c;
			      			max_theta = theta;
			      		}
		      		}
		      	}
		      	
		      	//Set the face texture coordiantes and material
		      	if(max_c >= 0){
	        		faces.get(f).uv = new UV[faces.get(f).v.length];
	        		
	        		for(int i=0; i<faces.get(f).v.length; i++){
	        			point = Point.transform(cameras.get(max_c).getM(), vertices.get(faces.get(f).v[i]));
	        			faces.get(f).uv[i] = new UV((float)point.x/images.get(max_c).getWidth(null), 1-(float)point.y/images.get(max_c).getHeight(null));
	        			faces.get(f).material = materials.get(max_c);
	        		}
		      	}
	      	}
	      }
	    }
    }
    
    //mesh.compressVertices();
    mesh.initialize();
    
    return mesh;
  }
  
  public boolean save(String filename, Mesh mesh) {return false;}
}