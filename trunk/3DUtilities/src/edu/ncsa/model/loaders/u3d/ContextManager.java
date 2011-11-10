package edu.ncsa.model.loaders.u3d;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;
public class ContextManager {
		public ContextManager()
		{
			this.symbolCount = new short[Constants.StaticFull][];
			this.cumulativeCount = new short[Constants.StaticFull][];
		}
	
		public void AddSymbol(int context, int symbol)
		{
			//System.out.println("Adding symbol " + context + ":" + symbol);
			if (context < Constants.StaticFull && context !=
				Constants.Context8 && symbol < MaximumSymbolInHistogram)
				
				{ //check if dynamic. nothing to do if static or if the
				//symbol is larger than the maximum symbol allowed in the
				//histogram
				short[] cumulativeCount = this.cumulativeCount[context];
				short[] symbolCount = this.symbolCount[context];
				if (cumulativeCount == null || cumulativeCount.length <=
					symbol)
				{ //allocate new arrays if they do not exist yet or if they
					//are too small.
					cumulativeCount = new short[symbol + ArraySizeIncr];
					symbolCount = new short[symbol + ArraySizeIncr];
					if(cumulativeCount != null && symbolCount != null)
					{//check that the arrays were allocated successfully
						if (this.cumulativeCount[context] == null)
						{//if this is a new context set up the histogram
							this.cumulativeCount[context] =
								cumulativeCount;
							this.cumulativeCount[context][0] = 1;
							this.symbolCount[context] = symbolCount;
							this.symbolCount[context][0] = 1;
						}
						else
						{//if this is an old context, copy over the values in
							//the histogram to the new arrays
							this.cumulativeCount[context] = cumulativeCount;
							this.symbolCount[context]= symbolCount;
						}
					}
					this.cumulativeCount[context] = cumulativeCount;
					this.symbolCount[context] = symbolCount;
				}
				if(cumulativeCount[0] >= Elephant)
				{//if total number of occurances is larger than Elephant,
					//scale down the values to avoid overflow
					
					int len = cumulativeCount.length;
					short tempAccum = 0;
					for(int i = len - 1; i >= 0; i--)
					{
						symbolCount[i] >>= 1;
						tempAccum += symbolCount[i];
						cumulativeCount[i] = tempAccum;
					}
					//preserve the initial escape value of 1 for the symbol
					//count and cumulative count
					symbolCount[0]++;
					cumulativeCount[0]++;
				}
				symbolCount[symbol]++;
				for(int i = 0; i <= symbol; i++)
				{
					cumulativeCount[i]++;
				}
				}
		}
		public int GetSymbolFrequency(int context, int symbol)
		{
			//the static case is 1.
			int rValue = 1;
			if (context < Constants.StaticFull && context !=
				Constants.Context8)
			{
				//the default for the dynamic case is 0
				rValue = 0;
				if ((this.symbolCount[context] != null)
						&& (symbol < this.symbolCount[context].length))
				{
					rValue = (int) this.symbolCount[context][symbol];
				}
				else if (symbol == 0)
				{ //if the histogram hasn't been created yet, the
					//symbol 0 is the escape value and should return 1
					rValue = 1;
					
				}
			}
			return rValue;
		}
		public int GetCumulativeSymbolFrequency(int context, int symbol)
		{
			//the static case is just the value of the symbol.
			int rValue = symbol - 1;
			if (context < Constants.StaticFull && context != Constants.Context8)
			{
				rValue = 0;
				if (this.cumulativeCount[context] != null)
				{
					if(symbol < this.cumulativeCount[context].length)
					{
						rValue = (int)(this.cumulativeCount[context][0]
						         - this.cumulativeCount[context][symbol]);
					}
					else
						rValue = (int)(this.cumulativeCount[context][0]);
				}
			}
			return rValue;
		}
		public int GetTotalSymbolFrequency(int context)
		{
			if (context < Constants.StaticFull && context != Constants.Context8)
			{
				int rValue = 1;
				if(this.cumulativeCount[context] != null)
					//System.out.println("cumulativecount[8] != null" + context);
					rValue = this.cumulativeCount[context][0];
				return rValue;
			}
			else
			{
				if (context == Constants.Context8)
					return 256;
				
				else
					return context - Constants.StaticFull;
			}
		}
		public int GetSymbolFromFrequency(int context, int
				symbolFrequency)
		{
			//System.out.println("context:" + context + " frequency:" + symbolFrequency);
			int rValue = 0;
			if (context < Constants.StaticFull && context !=
				Constants.Context8)
			{
				rValue = 0;
				if (this.cumulativeCount[context] != null
						&& symbolFrequency != 0
						&& this.cumulativeCount[context][0] >= symbolFrequency)
				{
					int i = 0;
					for(i = 0; i < this.cumulativeCount[context].length;
					i++)
					{
						if (this.GetCumulativeSymbolFrequency(context, i)
								<= symbolFrequency)
							rValue = i;
						else
							break;
					}
				}
			}
			else
			{
				rValue = symbolFrequency + 1;
			}
			return rValue;
		}

		private short[][] symbolCount; //an array of arrays that store the
		//number of occurrences of each
		// symbol for each dynamic context.
		private short[][] cumulativeCount; //an array of arrays that store the
		//cumulative frequency of each
		//symbol in each context. the value
		//is the number of occurences of a
		//symbol and every symbol with a
		//larger value.

		// The Elephant is a value that determines the number of
		// symbol occurences that are stored in each dynamic histogram.
		// Limiting the number of occurences avoids overflow of the U16 array
		// elements and allows the histogram to adapt to changing symbol
		// distributions in files.
		private  int Elephant = 0x00001fff;
		//the maximum value that is stored in a histogram
		private int MaximumSymbolInHistogram = 0x0000FFFF;
		//the ammount to increase the size of an array when reallocating
		//an array.
		private  int ArraySizeIncr = 32;

	}

