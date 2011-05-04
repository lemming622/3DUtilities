package edu.ncsa.model.loaders.teeve;
import kgm.utility.*;
import java.io.*;

/**
 * A container class for gateway helper classes.
 * @author Kenton McHenry
 */
public class GatewayAuxiliary
{
	/**
	 * A structure for gateway messages.
	 */
	public static class Message
	{
		public int type;
		public char[] sender = new char[16];
		public char[] cluster = new char[16];
		public char[] stream = new char[16];
		public int size;
		public int data1;
		public int data2;
		
		public Message() {}
		
		/**
		 * Class constructor.
		 * @param sender the IP of the sender
		 * @param cluster the IP of the cluster
		 * @param stream the IP of the stream
		 */
		public Message(String sender, String cluster, String stream)
		{
			//Copy over sender IP
			for(int i=0; i<sender.length(); i++){
				this.sender[i] = sender.charAt(i);
			}
			
			this.sender[sender.length()] = '\0';
			this.sender[15] = '\0';
			
			//Copy over cluster IP
			for(int i=0; i<cluster.length(); i++){
				this.cluster[i] = cluster.charAt(i);
			}
			
			this.cluster[cluster.length()] = '\0';
			this.cluster[15] = '\0';
			
			//Copy over stream IP
			for(int i=0; i<stream.length(); i++){
				this.stream[i] = stream.charAt(i);
			}
			
			this.stream[stream.length()] = '\0';
			this.stream[15] = '\0';
		}
	
		/**
		 * Send a message over an output stream.
		 * @param outs the output stream to use
		 * @param message the message to send
		 */
		public static void send(OutputStream outs, Message message)
		{
			byte[] bytes;
			
			try{
				bytes = Utility.intToBytes(message.type, false);
				for(int i=0; i<4; i++) outs.write(bytes[i]);
	
				for(int i=0; i<16; i++) outs.write(message.sender[i]);
				for(int i=0; i<16; i++) outs.write(message.cluster[i]);
				for(int i=0; i<16; i++) outs.write(message.stream[i]);
				
				bytes = Utility.intToBytes(message.size, false);
				for(int i=0; i<4; i++) outs.write(bytes[i]);
				
				bytes = Utility.intToBytes(message.data1, false);
				for(int i=0; i<4; i++) outs.write(bytes[i]);
				
				bytes = Utility.intToBytes(message.data2, false);
				for(int i=0; i<4; i++) outs.write(bytes[i]);
			}catch(Exception e) {e.printStackTrace();}
		}
		
		/**
		 * Receive a message over an input stream.
		 * @param ins the input stream to use
		 * @return the message received
		 */
		public static Message receive(InputStream ins)
		{
			Message message = new Message();
			byte[] buffer = new byte[64];
			
			try{
				ins.read(buffer, 0, buffer.length);
				
				message.type = Utility.bytesToInt(buffer[0], buffer[1], buffer[2], buffer[3], false);
				
				for(int i=0; i<16; i++) message.sender[i] = (char)buffer[i+4];
				for(int i=0; i<16; i++) message.sender[i] = (char)buffer[i+20];
				for(int i=0; i<16; i++) message.sender[i] = (char)buffer[i+36];
				
				message.type = Utility.bytesToInt(buffer[52], buffer[53], buffer[54], buffer[55], false);
				message.type = Utility.bytesToInt(buffer[56], buffer[57], buffer[58], buffer[59], false);
				message.type = Utility.bytesToInt(buffer[60], buffer[61], buffer[62], buffer[63], false);
			}catch(Exception e) {e.printStackTrace();}
	
			return message;
		}
	}
}