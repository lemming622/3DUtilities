package edu.ncsa.model;

/**
 * An abstract class which provides an interface for file format loaders into the mesh class.
 *  @author Kenton McHenry
 */
public abstract class MeshLoader
{
  /**
   * An interface to allow for progress update callbacks.
   */
  public interface ProgressEvent
  {
  	public void progressEvent(double value, double total);
  }
  
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
	abstract public Mesh load(String filename);
	abstract public boolean save(String filename, Mesh mesh);
}