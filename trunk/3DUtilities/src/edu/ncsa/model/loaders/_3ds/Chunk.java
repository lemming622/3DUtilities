package edu.ncsa.model.loaders._3ds;

/**
 * A class representing the header of a 3DS section
 * A chunk consists of two bytes identifying the type
 * of section and four bytes identifying the length of
 * the section (including these six header bytes)
 * @author Daniel Long
 */
class Chunk
{
	/**
	 * Initializes the chunk
	 * @param id_ The two byte id
	 * @param length_ The length of the 3DS section
	 */
    public Chunk(int id_, int length_)
    {
        id = id_;
        length = length_;
    }

    public int id;
    public int length;
}
