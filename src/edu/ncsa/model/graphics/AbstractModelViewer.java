package edu.ncsa.model.graphics;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshLoader.ProgressEvent;
import javax.swing.*;

public abstract class AbstractModelViewer extends JPanel
{
	public Mesh mesh;
	
  public void activate() {}
  public void setAdjustments(float tx, float ty, float scl) {}
  public void load(String filename, ProgressEvent progressCallBack) {}
  public void load(String filename) {}
  public void save(String filename) {}  
	public int[] grabImage() {return null;}
}