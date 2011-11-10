package edu.ncsa.model.loaders.vtk;

/**
 * A base grid class; RECTILINEAR_GRID and STRUCTURED_GRID inherit from here
 * @author Daniel Long
 */
public class GRID
{
	/**
	 * Connects all the points in the grids
	 * @param x ???
	 * @param y ???
	 * @param z ???
	 * @return An array of the line segments connecting each point
	 */
    public int[][] connectPoints(int x, int y, int z)
    {
        dimX = x;
        dimY = y;
        dimZ = z;
        int line = 0;
        
        int lines[][] = new int[dimX * dimZ + dimY * dimZ + dimX * dimY][2];
        for(int i = 0; i < dimZ; i++){
            for(int j = 0; j < dimX; j++){
                lines[line][0] = pt(j, 0, i);
                lines[line][1] = pt(j, dimY - 1, i);
                line++;
            }
        }

        for(int i = 0; i < dimZ; i++){
            for(int j = 0; j < dimY; j++){
                lines[line][0] = pt(0, j, i);
                lines[line][1] = pt(dimX - 1, j, i);
                line++;
            }
        }

        for(int i = 0; i < dimY; i++){
            for(int j = 0; j < dimX; j++){
                lines[line][0] = pt(j, i, 0);
                lines[line][1] = pt(j, i, dimZ - 1);
                line++;
            }
        }

        return lines;
    }

    /**
     * Generates a point from i, j, and k
     * @param i ???
     * @param j ???
     * @param k ???
     * @return The generated point
     */
    private int pt(int i, int j, int k)
    {
        return k * dimX * dimY + j * dimX + i;
    }

    private int dimX;
    private int dimY;
    private int dimZ;
}
