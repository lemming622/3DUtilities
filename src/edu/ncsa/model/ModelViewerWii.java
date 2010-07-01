package edu.ncsa.model;
import edu.ncsa.model.graphics.jogl.*;
import javax.swing.*;
import wiiusej.*;
import wiiusej.values.*;
import wiiusej.wiiusejevents.wiiuseapievents.*;
import wiiusej.wiiusejevents.physicalevents.*;
import wiiusej.wiiusejevents.utils.*;

public class ModelViewerWii extends ModelViewer implements WiimoteListener
{
	private Wiimote[] wiimotes = null;
	private Wiimote wiimote = null;
	private int pressed_button = -1;
	private boolean USE_IR = false;
	
	/**
	 * Class constructor.
	 */
	public ModelViewerWii() 
	{
		super();
		enableWiiMote();
	}
	
  /**
   * Class Constroctur specifying the INI file to load.
   *  @param filename INI file name containing initialization values
   *  @param DISABLE_HEAVYWEIGHT disable heavyweight GLCanvas (sacrificing performance for functionality)
   */
  public ModelViewerWii(String filename, boolean DISABLE_HEAVYWEIGHT)
  {
    super(filename, DISABLE_HEAVYWEIGHT);
    enableWiiMote();
  }
  
  /**
   * Class Constructor specifying INI file, initial dimensions and whether or not
   * to load the default model from the INI file or not.  The construct also builds the pop
   * up menu and starts a thread used to refresh the scene.
   *  @param filename INI file name containing initialization values
   *  @param w width of viewer
   *  @param h height of viewer
   *  @param DISABLE_HEAVYWEIGHT disable heavyweight GLCanvas (sacrificing performance for functionality)
   *  @param ld if false the viewer will not load the default model from the INI file
   */
  public ModelViewerWii(String filename, int w, int h, boolean DISABLE_HEAVYWEIGHT, boolean ld)
  {
  	super(filename, w, h, DISABLE_HEAVYWEIGHT, ld);
  	enableWiiMote();
  }
  
  /**
   * Enable nearby wiimotes.
   */
  public void enableWiiMote()
  {
  	if(wiimotes == null){
	  	System.loadLibrary("wiiuse");
	  	System.loadLibrary("WiiUseJ");
	    
	  	wiimotes = WiiUseApiManager.getWiimotes(1, true);
	    wiimote = wiimotes[0];
	    wiimote.activateIRTRacking();
	    wiimote.activateMotionSensing();
	    wiimote.addWiiMoteEventListeners(this);
  	}
  }
  
  /**
   * Listener for wiimote button events.
   *  @param e wiimote button event
   */
	public void onButtonsEvent(WiimoteButtonsEvent e)
	{
    if(e.isButtonAPressed()){
      pressed_button = 1;
    }else if(e.isButtonAJustReleased()){
    	pressed_button = -1;
    }else if(e.isButtonOnePressed()){
    	USE_IR = false;
    }else if(e.isButtonTwoPressed()){
    	USE_IR = true;
    }else if(e.isButtonPlusJustPressed() || e.isButtonPlusHeld()){
    	transformation.scl *= 1.04;
    }else if(e.isButtonMinusJustPressed() || e.isButtonMinusHeld()){
    	transformation.scl /= 1.04;
    }else if(e.isButtonLeftJustPressed() || e.isButtonLeftHeld()){
    	transformation.tx -= 5;
    }else if(e.isButtonRightJustPressed() || e.isButtonRightHeld()){
    	transformation.tx += 5;
    }else if(e.isButtonUpJustPressed() || e.isButtonUpHeld()){
    	transformation.ty += 5;
    }else if(e.isButtonDownJustPressed() || e.isButtonDownHeld()){
    	transformation.ty -= 5;
    }
    
    REFRESH = true;
	}
	
	/**
	 * Listener for wiimote motion events.
	 *  @param e wiimote motion event
	 */
	public void onMotionSensingEvent(MotionSensingEvent e)
	{
		if(pressed_button == 1){
			Orientation orientation = e.getOrientation();
			transformation.rx = (int)Math.round(-orientation.getPitch());
			transformation.rz = (int)Math.round(-orientation.getRoll());
	    REFRESH = true;
		}
	}
	
	/**
	 * Listener for wiimite IR events.
	 *  @param e wiimote IR event
	 */
	public void onIrEvent(IREvent e)
	{
		if(USE_IR && pressed_button == 1){
			transformation.tx = e.getX() - width/2;
			transformation.ty = -e.getY() + height/2;
			transformation.tz = e.getZ();
			REFRESH = true;
		}
	}	
	
	public void onExpansionEvent(ExpansionEvent e)
	{
		if(e instanceof NunchukEvent){
			NunchukEvent ne = (NunchukEvent) e;
			JoystickEvent nje = ne.getNunchukJoystickEvent();
			//NunchukButtonsEvent nbe = ne.getButtonsEvent();
			//MotionSensingEvent nme = ne.getNunchukMotionSensingEvent();
			
			if(pressed_button == 1){
				transformation.ry = (int)Math.round(-nje.getAngle());
				REFRESH = true;
			}
		}
	}
	
	public void onStatusEvent(StatusEvent e) {}
	public void onDisconnectionEvent(DisconnectionEvent e) {}
	public void onNunchukInsertedEvent(NunchukInsertedEvent e) {}
	public void onNunchukRemovedEvent(NunchukRemovedEvent e) {}
	public void onClassicControllerInsertedEvent(ClassicControllerInsertedEvent e) {}
	public void onClassicControllerRemovedEvent(ClassicControllerRemovedEvent e) {}
	public void onGuitarHeroInsertedEvent(GuitarHeroInsertedEvent e) {}
	public void onGuitarHeroRemovedEvent(GuitarHeroRemovedEvent e) {}
	
  /**
   * The main function used if this class is run by itself.
   *  @param args not used
   */
  public static void main(String args[])
  {  	
    ModelViewerWii mv = new ModelViewerWii("ModelViewer.ini", false);
    JFrame frame = new JFrame("Model Viewer");
    frame.setSize(mv.width+9, mv.height+35);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(mv);
    frame.setVisible(true);
  }
}