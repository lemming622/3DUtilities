package edu.ncsa.model.loaders.teeve;

/**
 * A container class for gateway utility functions.
 * @author Kenton McHenry
 */
public class GatewayUtility
{
	/**
	 * Convert an integer into an array of 4 bytes.
	 * @param integer the integer to convert
	 * @param BIG_ENDIAN true if the bytes should be stored in big endian order
	 * @return the array of bytes representing the integer
	 */
	public static byte[] intToBytes(int integer, boolean BIG_ENDIAN)
	{
		byte[] bytes = new byte[4];
		
		if(BIG_ENDIAN){
			bytes[0] = (byte)((integer >> 24) & 0x000000ff);
			bytes[1] = (byte)((integer >> 16) & 0x000000ff);
			bytes[2] = (byte)((integer >> 8) & 0x000000ff);
			bytes[3] = (byte)(integer & 0x000000ff);
		}else{
			bytes[0] = (byte)(integer & 0x000000ff);
			bytes[1] = (byte)((integer >> 8) & 0x000000ff);
			bytes[2] = (byte)((integer >> 16) & 0x000000ff);
			bytes[3] = (byte)((integer >> 24)& 0x000000ff);
		}

		return bytes;
	}
	
	/**
	 * Convert an unsigned byte to an int.
	 * @param b the byte to convert
	 * @return the integer representation of the unsigned bytes value
	 */
  public static int byteToInt(byte b)
  {
    return (int)b & 0xff;
  }	
  
	/**
	 * Convert 4 bytes into an integer.
	 * @param b0 the first byte representing the the integer
	 * @param b1 the second byte representing the the integer
	 * @param b2 the third byte representing the the integer
	 * @param b3 the fourth byte representing the the integer
	 * @param BIG_ENDIAN true if the bytes should be stored in big endian order
	 * @return the integer represented by the given 4 bytes
	 */
	public static int bytesToInt(byte b0, byte b1, byte b2, byte b3, boolean BIG_ENDIAN)
	{
		int i0 = byteToInt(b0);
		int i1 = byteToInt(b1);
		int i2 = byteToInt(b2);
		int i3 = byteToInt(b3);
		
		if(BIG_ENDIAN){
			return (i0<<24) | i1<<16 | i2<<8 | i3;
		}else{
			return (i3<<24) | i2<<16 | i1<<8 | i0;
		}
	}
	
	/**
	 * Convert 2 bytes into a short.
	 * @param b0 the first byte representing the the integer
	 * @param b1 the second byte representing the the integer
	 * @param BIG_ENDIAN true if the bytes should be stored in big endian order
	 * @return the short represented by the given 2 bytes
	 */
	public static short bytesToShort(byte b0, byte b1, boolean BIG_ENDIAN)
	{
		int i0 = byteToInt(b0);
		int i1 = byteToInt(b1);
		
		if(BIG_ENDIAN){
			return (short)((i0<<8) | i1);
		}else{
			return (short)(i1<<8 | i0);
		}
	}
	
	/**
	 * Retrieve a sub-array from a larger array.
	 * @param array the larger array from which we want a sub-array
	 * @param i0 the starting index of the sub-array
	 * @param i1 the ending index of the sub-array
	 * @return the sub-array
	 */
	public static byte[] subArray(byte[] array, int i0, int i1)
	{
		int n = i1 - i0 + 1;
		byte[] sub_array = new byte[n];
		
		for(int i=0; i<n; i++){
			sub_array[i] = array[i+i0];
		}
		
		return sub_array;
	}
}