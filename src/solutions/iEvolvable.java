package solutions;
import java.util.*;

public interface iEvolvable {
	
    public int[] getGenome();
    public int[] getGenome(int[] baseline);
    public void setGenome(int[] genome);
    public void calcFairnessScores();
    public void crossover(int[] genome1,int[] genome2);
    public void mutate( double prob);
}
