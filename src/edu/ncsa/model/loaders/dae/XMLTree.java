package edu.ncsa.model.loaders.dae;
import edu.ncsa.model.Utility.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import javax.swing.tree.*;
import org.jdom.*;
import org.jdom.input.*;

/**
 * A viewer that displays the contents of an XML file in a JTree.
 *  @author Kenton McHenry
 */
public class XMLTree
{
	/**
	 * Class construtor.
	 *  @param filename the XML file to display
	 */
	public XMLTree(String filename)
	{
		try{
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(new File(filename));
			Element root = document.getRootElement();
			DefaultMutableTreeNode jroot = new DefaultMutableTreeNode(root.getName());
			Element element, child;			
			DefaultMutableTreeNode node, jchild;
			Iterator<Element> itr1;
			Pair<Element,DefaultMutableTreeNode> pair;
			Stack<Pair<Element,DefaultMutableTreeNode>> stk = new Stack<Pair<Element,DefaultMutableTreeNode>>();
			Iterator<Attribute> itr2;
			Attribute attribute;
			String attributes;
			
			stk.push(new Pair<Element,DefaultMutableTreeNode>(root, jroot));
			
			while(!stk.isEmpty()){
			  pair = stk.pop();
			  element = pair.first;
			  node = pair.second;
			  
			  itr1 = element.getChildren().iterator();
			  
			  while(itr1.hasNext()){
			  	child = itr1.next();
			  	
			  	//Set attributes
			  	attributes = "";
			  	itr2 = child.getAttributes().iterator();
			  	
			  	while(itr2.hasNext()){
			  		attribute  = itr2.next();
			  		attributes += attribute.getName() + ":" + attribute.getValue() + ", ";
			  	}
			    
			  	if(!attributes.isEmpty()){
			  		attributes = " [" + attributes.substring(0, attributes.length()-2) + "]";
			  	}
			    
			  	//Set JTree node
			  	jchild = new DefaultMutableTreeNode(child.getName() + attributes);
			  	node.add(jchild);
			  	
			  	stk.push(new Pair<Element,DefaultMutableTreeNode>(child, jchild));
			  }
			}
			
			JFrame frame = new JFrame("XML Tree [" + filename + "]");
			JScrollPane sp = new JScrollPane(new JTree(jroot));
			frame.add(sp);
			frame.setSize(500, 600);
			frame.setVisible(true);
		}catch(Exception ex) {ex.printStackTrace();}
	}
}