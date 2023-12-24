package solutions;

public interface iEvolvable {
	
    int[] getGenome();
    int[] getGenome(int[] baseline);
    void setGenome(int[] genome);
    void calcFairnessScores();
    void crossover(int[] genome1, int[] genome2);
    void mutate(double prob);
}
