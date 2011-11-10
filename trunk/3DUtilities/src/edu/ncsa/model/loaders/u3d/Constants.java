package edu.ncsa.model.loaders.u3d;
import edu.ncsa.model.*;
import edu.ncsa.model.MeshAuxiliary.*;
import java.io.*;
import java.util.*;
public class Constants
{

//context ranges
/// <summary>

/// the context for uncompressed U8
/// </summary>
public static int Context8 = 0;
/// <summary>
/// contexts >= StaticFull are static contexts.
/// </summary>
public static int StaticFull = 0x00000400;
///<summary>
///The largest allowable static context. values written to contexts
//> MaxRange are
///written as uncompressed.
///</summary>
public static int MaxRange = StaticFull + 0x00003FFF;
/// <summary>
/// a defualt buffer size for U3D
/// </summary>
public static int SizeBuff = 1024;
/// <summary>
/// the initial size allocated for buffers
/// </summary>
public static int DataSizeInitial = 0x00000010;
//Bit masks for reading and writing symbols.
/// <summary>
/// masks all but the most significan bit
/// </summary>
public static int HalfMask = 0x00008000;
/// <summary>
/// masks the most significant bit
/// </summary>
public static int NotHalfMask = 0x00007FFF;
/// <summary>
/// masks all but the 2nd most significan bit
/// </summary>
public static int QuarterMask = 0x00004000;

/// <summary>
/// masks the 2 most significant bits
/// </summary>
public static int NotThreeQuarterMask = 0x00003FFF;
/// <summary>
/// used to swap 8 bits in place
/// </summary>
public static int[] Swap8
= {0, 8, 4, 12, 2, 10, 6, 14, 1, 9, 5, 13, 3,
	11, 7, 15};

}
