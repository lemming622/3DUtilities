package edu.ncsa.model.loaders.u3d;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*; 

public class BitStreamRead{
	public BitStreamRead()

	{
		this.contextManager = new ContextManager();
		this.high = 0x0000FFFF;
	}



	public int ReadU8()
	{
		int uValue = 0;
		uValue = ReadSymbol(Constants.Context8);
		uValue--;
		int value = SwapBits8(uValue);
		
		return  value;
	}
	public int ReadU16()
	{
		int low = 0;
	
		int high = 0;
		low = ReadU8();
		//System.out.println("low is " + low);
		high = ReadU8();
		//System.out.println("high is " + high);
		return ( low | ( high << 8));
	}
	public int ReadU32()
	{
		int low = 0;
		int high = 0;
		low = ReadU16();
		high = ReadU16();
		return ( low | ( high << 16));
	}
	public long ReadU64()
	{
		int low = 0;
		int high = 0;
		
		low = ReadU32();
		high = ReadU32();
		return ((long) low) | (((long) high) << 32);
	}
	public int ReadI32()
	{
		int uValue = 0;
		uValue = ReadU32();
		return uValue;
	}
	public float ReadF32()
	{
		int uValue = 0;
		uValue = ReadU32();
		return Float.intBitsToFloat(uValue);
	}
	public int ReadCompressedU32(int context)
	{
		
		int symbol = 0;
		int rValue = 0;
		if (context != Constants.Context8 && context < Constants.MaxRange)
		{ //the context is a compressed context
			symbol = ReadSymbol(context);
			System.out.println(symbol + ":" + context);
			if (symbol != 0)
			{ //the symbol is compressed
				rValue = symbol - 1;
			}
			else
			{ //escape character, the symbol was not compressed
				//return -1;
				rValue = ReadU32();
				//rValue = ReadSymbol(context)-1;
				this.contextManager.AddSymbol(context, rValue + 1);
			}
		}
		else
		{ //The context specified is uncompressed.
			rValue = ReadU32();
		}
		return rValue;
	}
	public int ReadCompressedU16(int context)
	{
		int symbol = 0;
		int rValue = 0;
		if (context != 0 && context < Constants.MaxRange)
		{ //the context is a compressed context
			symbol = ReadSymbol(context);
			if (symbol != 0)
			{ //the symbol is compressed
				rValue =(symbol - 1);
			}
			else
			{ //the symbol is uncompressed
				rValue = ReadU16();
				this.contextManager.AddSymbol(context, rValue + 1);
			}
		}
		else
		{ //the context specified is not compressed
			rValue = ReadU16();
		}
		return rValue;
	}
	public int ReadCompressedU8(int context )
	{
		int symbol = 0;
		int rValue=0;
		if (context != 0 && context < Constants.MaxRange)
		{ //the context is a compressed context
			symbol = ReadSymbol(context);
			if (symbol != 0)
			{ //the symbol is compressed
				rValue = (symbol - 1);
			}
			else
			{ //the symbol is not compressed
				rValue = ReadU8();
				this.contextManager.AddSymbol(context, rValue +
						1);
			}
		}
		else
		{ //the context specified is not compressed
			rValue = ReadU8();
		}
		return rValue;
	}
	
	/* internally the BitStreamRead object stores 64 bits from the
DataBlock's
	 * data section in dataLocal and dataLocalNext.
	 */
	/* SwapBits8
	 * reverses the order of the bits of an 8 bit value.
	 * E.g. abcdefgh -> hgfedcba
	 */
	private int SwapBits8(int rValue)
	{
		int result = 0;
		result = (Constants.Swap8[(rValue&0xf0) >> 4])| ((Constants.Swap8[rValue & 0xf])<<4);
		
		return result;
	}
	
	/* ReadSymbol
	 * Read a symbol from the datablock using the specified context.
	 * The symbol 0 represents the escape value and signifies that the
	 * next symbol read will be uncompressed.
	 */
	private int ReadSymbol(int context)
	{
		int uValue = 0;
		// Fill in the code word
		int position = 0;
		position = GetBitCount();
		this.code = ReadBit();
		this.dataBitOffset += this.underflow;
		while (this.dataBitOffset >= 32)
		{
			this.dataBitOffset -= 32;
			IncrementPosition();
		}
		int temp = 0;
		temp = Read15Bits();
		this.code <<= 15;
	
		this.code |= temp;
		SeekToBit(position);
		// Get total count to calculate probabilites
		int totalCumFreq =
			this.contextManager.GetTotalSymbolFrequency(context);
		// Get the cumulative frequency of the current symbol
		//if(this.code < this.low)
		//	this.low = this.code;
		//if(this.code >this.high)
		//	this.high = this.code;
		int range = this.high + 1 - this.low;
		// The relationship:
		// codeCumFreq <= (totalCumFreq * (this.code - this.low)) / range
		// is used to calculate the cumulative frequency of the current
		// symbol. The +1 and -1 in the line below are used to counteract
		// finite word length problems resulting from the division by
		//range.
		//System.out.println("total freq = " + totalCumFreq + " code = " + this.code + " low = " + this.low + " high = " + this.high );
		int codeCumFreq =
			((totalCumFreq) * (1 + this.code - this.low) - 1) / (range);
		// Get the current symbol
		uValue = this.contextManager.GetSymbolFromFrequency(context, codeCumFreq);
		// Update state and context
		int valueCumFreq =
			this.contextManager
			.GetCumulativeSymbolFrequency(context, uValue);
	
		int valueFreq =
			this.contextManager.GetSymbolFrequency(context, uValue);
		int low = this.low;
		int high = this.high;
		

		high = low + range * (valueCumFreq + valueFreq) /
		totalCumFreq -1;
	
		low = low + range * (valueCumFreq) / totalCumFreq;
		
	
		this.contextManager.AddSymbol(context, uValue);
		int bitCount;
		int maskedLow;
		int maskedHigh;
		// Count bits to read
		// Fast count the first 4 bits
		//compare most significant 4 bits of low and high
		bitCount =
			ReadCount[((low >> 12) ^ (high >> 12)) & 0x0000000F];
		low &= FastNotMask[bitCount];
		high &= FastNotMask[bitCount];
	
		high <<= bitCount;
		low <<= bitCount;
		high |=  ((1 << bitCount) -1);
		// Regular count the rest
		maskedLow = Constants.HalfMask & low;
		maskedHigh = Constants.HalfMask & high;
		while (((maskedLow | maskedHigh) == 0)
				|| ((maskedLow == Constants.HalfMask)
						&& maskedHigh == Constants.HalfMask))
		{
			low = (Constants.NotHalfMask & low) << 1;
			high = ((Constants.NotHalfMask & high) << 1) | 1;
			maskedLow = Constants.HalfMask & low;
			maskedHigh = Constants.HalfMask & high;
			bitCount++;
		}
		int savedBitsLow = maskedLow;
		int savedBitsHigh = maskedHigh;
		if(bitCount > 0)
		{
			
			bitCount += this.underflow;
			this.underflow = 0;
		}
		// Count underflow bits
		maskedLow = Constants.QuarterMask & low;
		maskedHigh = Constants.QuarterMask & high;
		int underflow = 0;
		while ((maskedLow == 0x4000) && (maskedHigh == 0))
		{
			low &= Constants.NotThreeQuarterMask;
			high &= Constants.NotThreeQuarterMask;
			low += low;
			high += high;
			high |= 1;
			maskedLow = Constants.QuarterMask & low;
			maskedHigh = Constants.QuarterMask & high;
			underflow++;
		}
	
		// Store the state
		this.underflow += underflow;
		low |= savedBitsLow;
		high |= savedBitsHigh;
		this.low = low;
		this.high = high;
		// Update bit read position
		this.dataBitOffset += bitCount;
		while(this.dataBitOffset >= 32)
		{
			this.dataBitOffset -= 32;
			IncrementPosition();
		}
		// Set return value
		return uValue ;
	}
	/*
	 * GetBitCount
	 * returns the number of bits read in rCount
	 */
	private int GetBitCount()

	{
		return ((this.dataPosition << 5) + this.dataBitOffset);
	}
	/* ReadBit
	 * Read the next bit in the datablock. The value is returned in
	 * rValue.
	 */
	private int ReadBit()
	{
		int uValue = 0;
		uValue = this.dataLocal >> this.dataBitOffset;
		uValue &= 1;
		this.dataBitOffset ++;
		if(this.dataBitOffset >= 32)
		{
			this.dataBitOffset -= 32;
			IncrementPosition();
		}
		return uValue;
	}
	
	/* Read15Bits
	 * Read the next 15 bits from the datablock. the value is returned
	 * in rValue.
	 */
	private int Read15Bits()
	{
		int uValue = this.dataLocal >> this.dataBitOffset;
			if(this.dataBitOffset > 17)
			{
				uValue |= (this.dataLocalNext << (32 - this.dataBitOffset));
			}
			uValue += uValue;
			uValue = (Constants.Swap8[(uValue >> 12) & 0xf])
			| ((Constants.Swap8[(uValue >> 8) & 0xf ]) << 4 )
			| ((Constants.Swap8[(uValue >> 4) & 0xf]) << 8 )
			| ((Constants.Swap8[uValue & 0xf]) << 12 );
			
			this.dataBitOffset += 15;
			if(this.dataBitOffset >= 32)
				
				{
				this.dataBitOffset -= 32;
				IncrementPosition();
				}
			return uValue;
	}
	/*
	 * IncrementPosition
	 * Updates the values of the datablock stored in dataLocal and
dataLocalNext
	 * to the next values in the datablock.
	 */
	private void IncrementPosition()
	{
		this.dataPosition++;
		this.dataLocal = this.data[dataPosition];
		if(this.data.length > this.dataPosition+1)
		{
			this.dataLocalNext = this.data[this.dataPosition+1];
		}
		else
		{
			this.dataLocalNext = 0;
		}
	}

	/* SeekToBit
	 * Sets the dataLocal, dataLocalNext and bitOffSet values so that
	 * the next read will occur at position in the datablock.
	 */
	private void SeekToBit(int position)
	{
		this.dataPosition = position >> 5;
			this.dataBitOffset = (position & 0x0000001F);
			GetLocal();
	}
	/*
	 * GetLocal
	 * store the initial 64 bits of the datablock in dataLocal and
	 * dataLocalNext
	 */
	public void GetLocal()
	{
		this.dataLocal = this.data[this.dataPosition];
		if(this.data.length > this.dataPosition + 1)
		{
			this.dataLocalNext = this.data[this.dataPosition+1];
		}
	}

	private ContextManager contextManager; //the context manager handles
	//the updates to the histograms
	//for the compression contexts.
	private int high; //high and low are the upper and
	private int low; //lower limits on the
	//probability
	private int underflow; //stores the number of bits of
	//underflow caused by the
	//limited range of high and low
	private int code; //the value as represented in
	//the datablock
	public int[] data; //the data section of the
	//datablock to read from.
	public int dataPosition; //the position currently read in
	//the datablock specified in 32
	//bit increments.
	private int dataLocal; //the local value of the data
	//corresponding to dataposition.
	private int dataLocalNext; //the 32 bits in data after
	
	//dataLocal
	public int dataBitOffset; //the offset into dataLocal that
	// the next read will occur
	private static  int[] FastNotMask
	= {0x0000FFFF, 0x00007FFF, 0x00003FFF, 0x00001FFF, 0x00000FFF};
	private static  int[] ReadCount
	= {4, 3, 2, 2, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0};
	
}


	