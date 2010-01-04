package edu.ncsa.model.loaders.teeve;
import edu.ncsa.model.loaders.teeve.GatewayAuxiliary.*;
import edu.ncsa.model.loaders.teeve.GatewayUtility;
import java.io.*;
import java.net.*;

/**
 * An interface to a UIUC/CS virtual router/gateway.
 * @author Kenton McHenry
 */
public class GatewayConnection implements Runnable
{
	protected String gateway;
	protected String sender;
	protected String cluster;
	protected String stream;
	protected Socket socket;
	protected OutputStream outs;
	protected InputStream ins;
	protected byte[] buffer = null;
	protected int buffer_length = 0;
	protected boolean CONNECTED;
	
	public GatewayConnection() {}
	
	/**
	 * Class constructor.
	 * @param gateway the gateway
	 * @param sender the sender
	 * @param cluster the cluster
	 * @param stream the stream
	 * @param port the port to use
	 */
	public GatewayConnection(String gateway, String sender, String cluster, String stream, int port)
	{
		try{
			this.gateway = InetAddress.getByName(gateway).getHostAddress();
			this.sender = InetAddress.getByName(sender).getHostAddress();
			this.cluster = InetAddress.getByName(cluster).getHostAddress();
			this.stream = InetAddress.getByName(stream).getHostAddress();
			
			socket = new Socket(this.gateway, port);
			outs = socket.getOutputStream();
			ins = socket.getInputStream();
		}catch(Exception e) {e.printStackTrace();}
		
		if(socket != null && outs != null && ins != null){
			System.out.println("Connected to gateway: " + this.gateway);
			CONNECTED = true;
		}
	}
	
	/**
	 * Join a session.
	 */
	protected void join()
	{
		Message message = new Message(sender, cluster, stream);
		byte[] parameters = new byte[140];	//34*4+4
		
		try{
			//Send "Camera_Stream" message
			message.type = 10;
			Message.send(outs, message);
			
			//Receive "Camera_Join" message
			message = Message.receive(ins);
			
			//Receive parameter message
			ins.read(parameters, 0, parameters.length);
		}catch(Exception e) {e.printStackTrace();}
	}
	
	/**
	 * Read current frame data from the gateway.
	 */
	protected synchronized void readFrame()
	{
		byte[] header = new byte[18];		//10 or 18
		int total, tmpi;
		
		try{
			//Get header
			ins.read(header, 0, header.length);
			buffer_length = GatewayUtility.bytesToInt(header[6], header[7], header[8], header[9], false);
			
			//Get data
			if(buffer == null || buffer_length > buffer.length){
				//System.out.println("Resizing buffer: " + buffer_length);
				buffer = new byte[buffer_length];
			}
			
			total = 0;
			
			while(total < buffer_length){
				tmpi = ins.read(buffer, total, buffer_length-total);
				total += tmpi;
			}
			
			//Debug received data
			if(false){	
			  System.out.print("\nHeader (length=" + header.length + "): ");
			  
			  for(int i=0; i<header.length; i++){
			  	System.out.print(GatewayUtility.byteToInt(header[i]) + " ");
			  }
			 
			  printBuffer();
			}
		}catch(Exception e) {
			e.printStackTrace();
			CONNECTED = false;
		}
	}
	
	/**
   * Run the runnable portion of this instance in its own thread.
   */
  public void readFrames()
  {
  	Thread thread = new Thread(this);
  	thread.start();
  }
  
  /**
   * Get the buffer containing the data from the last frame downloaded.
   * @return a copy of the buffer
   */
  public synchronized byte[] getBuffer()
  {
  	byte[] tmp = null;
  	
  	if(buffer != null){
  		tmp = new byte[buffer_length];
	  	System.arraycopy(buffer, 0, tmp, 0, buffer_length);
  	}
  	
  	return tmp;
  }

	/**
   * Print the buffers current contents.
   */
  protected void printBuffer()
  {
  	System.out.print("\nBuffer (length=" + buffer_length + "): ");
    
    for(int i=0; i<15; i++){
    	//System.out.print(GatewayUtility.byteToInt(buffer[i]) + " ");
    	System.out.print(buffer[i] + " ");
    }
    
    System.out.print("... ");
    
    for(int i=15; i>1; i--){
    	//System.out.print(GatewayUtility.byteToInt(buffer[buffer_length-i]) + " ");
    	System.out.print(buffer[buffer_length-i] + " ");
    }
    
    System.out.println("\n");
  }

	/**
   * Close gateway connection.
   */
  protected void close()
  {
  	if(outs != null) try{outs.close();}catch(Exception e){e.printStackTrace();}
  	if(ins != null) try{ins.close();}catch(Exception e){e.printStackTrace();}
  	if(socket != null) try{socket.close();}catch(Exception e){e.printStackTrace();}
  }

	/**
   * Download points as quickly as possible in a separate thread.
   */
  public void run()
  {
  	join();
  	
  	while(CONNECTED){
  		readFrame(); 
  		Thread.yield();
  	}
  	
  	close();
  }
}