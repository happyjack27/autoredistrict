package tools.simpleImpute;

public class District {
	String name;
	boolean contested = true;
	double[] vote_totals;
	double[] impute_totals;
	public double vote_total = 0;
	
	District(String name, int num_parties) {
		this.name = name;
		vote_totals = new double[num_parties];
		impute_totals = new double[num_parties];
	}
}
