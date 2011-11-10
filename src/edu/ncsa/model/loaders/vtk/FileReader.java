package edu.ncsa.model.loaders.vtk;
import ncsa.util.ReaderTokenizer;

public interface FileReader
{
    public abstract boolean read(ReaderTokenizer readertokenizer, int i);
}
