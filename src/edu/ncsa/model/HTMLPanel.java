package edu.ncsa.model;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * A panel built for easily displaying html.
 *  @author Kenton McHenry
 */
public class HTMLPanel extends JPanel
{
  private JEditorPane ep;
  private JScrollPane sp;
  private String text = "";
  private int horizontal_offset = 8;
  private int vertical_offset = 2;
  
  /**
   * Class constructor.  Sets margins, editor and scroll pane.
   */
  public HTMLPanel()
  {
    super();
    
    ep = new JEditorPane("text/html", text);
    ep.setEditable(false);
    ep.setBorder(new EmptyBorder(vertical_offset, horizontal_offset, vertical_offset, horizontal_offset));
    
    sp = new JScrollPane(ep);
    sp.setBackground(Color.white);    
    sp.setBorder(new EmptyBorder(0, 0, 0, 0));
    this.setLayout(new BorderLayout());
    this.add(sp, BorderLayout.CENTER);
  }
  
  /**
   * Sets left margin.
   *  @param x the offset for the left margin
   */
  public void setLeftOffset(int x)
  {
    ep.setBorder(new EmptyBorder(vertical_offset, x, vertical_offset, horizontal_offset));
  }
  
  /**
   * Sets horizontal margins.
   *  @param x the new value for the horizontal margin (in pixels)
   */
  public void setHorizontalOffset(int x)
  {
    horizontal_offset = x;
    ep.setBorder(new EmptyBorder(vertical_offset, horizontal_offset, vertical_offset, horizontal_offset));
  }
  
  /**
   * Set the background color of this panel.
   *  @param c the desired background color
   */
  public void setBackground(Color c)
  {
    super.setBackground(c);
    if(ep != null) ep.setBackground(c);
    if(sp != null) sp.setBackground(c);
  }
  
  /**
   * Set the text to display in the panel.
   *  @param s the desired text
   */
  public void setText(String s)
  {
    if(!s.equals(text)){
      text = s;
      ep.setText(text);
      ep.setCaretPosition(0);
    }
  }
}