package edu.ncsa.model;
import edu.ncsa.model.Utility.ProgressEvent;

/**
 * An abstract class which provides an interface for file format loaders into the animated mesh class.
 *  @author Kenton McHenry
 */
public abstract class AnimatedMeshLoader
{
	protected ProgressEvent progressCallBack = null;
	
	/**
	 * Set a callback for progress updates.
	 * @param progressCallBack the callback for progress updates
	 */
	public void setProgressCallBack(ProgressEvent progressCallBack)
	{
		this.progressCallBack = progressCallBack;
	}
	
	abstract public String type();
	abstract public AnimatedMesh load(String filename);
	abstract public double length();
	abstract public Mesh getMesh();
	abstract public Mesh getMesh(double t);
	abstract public boolean save(String filename, AnimatedMesh mesh);
}