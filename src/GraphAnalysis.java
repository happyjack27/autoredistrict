
public class GraphAnalysis {
	/*
	double[][] agent_agents;
	int num_agents = 10;
	int num_factors = 10;

	double[][] agent_factors;
	double[][] factor_agents;
	
	double[][] agent_factors_swap;
	double[][] factor_agents_swap;
	*/

	
	public void iterate(
			double[][] agent_agents
			, double[][] agent_factors
			, double[][] factor_agents_to
			, double[][] factor_agents_from
			, int num_agents, int num_factors
			, double rate
			) {
		double[][] agent_factors_swap = new double[num_agents][num_factors];
		double[][] factor_agents_to_swap = new double[num_factors][num_agents];
		double[][] factor_agents_from_swap = new double[num_factors][num_agents];
		
		//learn what agents each factor represents
		//to
		for( int i = 0; i < num_agents; i++) {
			for( int j = 0; j < num_factors; j++) {
				for( int k = 0; k < num_agents; k++) {
					factor_agents_to_swap[j][k] += agent_factors[i][j]*agent_agents[i][k];
				}
			}
		}
		double fagents_norm = 1.0/(double)(num_agents);
		for( int j = 0; j < num_factors; j++) {
			for( int k = 0; k < num_agents; k++) {
				factor_agents_to[j][k] += (factor_agents_to_swap[j][k]*fagents_norm - factor_agents_to[j][k])*rate;
				factor_agents_to_swap[j][k] = 0;
			}
		}
		
		//from
		for( int i = 0; i < num_agents; i++) {
			for( int j = 0; j < num_factors; j++) {
				for( int k = 0; k < num_agents; k++) {
					factor_agents_from_swap[j][k] += agent_factors[i][j]*agent_agents[k][i];
				}
			}
		}
		for( int j = 0; j < num_factors; j++) {
			for( int k = 0; k < num_agents; k++) {
				factor_agents_from[j][k] += (factor_agents_from_swap[j][k]*fagents_norm - factor_agents_from[j][k])*rate;
				factor_agents_from_swap[j][k] = 0;
			}
		}
		
		//learn what factors each agent represents
		//to
		for( int i = 0; i < num_agents; i++) {
			for( int j = 0; j < num_factors; j++) {
				for( int k = 0; k < num_agents; k++) {
					agent_factors_swap[i][j] += factor_agents_to[j][k]*agent_agents[i][k];
				}
			}
		}
		double afactors_nom = 1.0/(double)(num_agents);
		for( int j = 0; j < num_agents; j++) {
			for( int k = 0; k < num_factors; k++) {
				agent_factors[j][k] += (agent_factors_swap[j][k]*afactors_nom - agent_factors[j][k])*rate;
				agent_factors_swap[j][k] = 0;
			}
		}

		//from
		for( int i = 0; i < num_agents; i++) {
			for( int j = 0; j < num_factors; j++) {
				for( int k = 0; k < num_agents; k++) {
					agent_factors_swap[i][j] += factor_agents_from[j][k]*agent_agents[k][i];
				}
			}
		}
		for( int j = 0; j < num_agents; j++) {
			for( int k = 0; k < num_factors; k++) {
				agent_factors[j][k] += (agent_factors_swap[j][k]*afactors_nom - agent_factors[j][k])*rate;
				agent_factors_swap[j][k] = 0;
			}
		}
	
	}
	
	public void iterate2(
			int[][] agent_agents
			, double[][] agent_factors
			, double[][] factor_agents
			, int num_agents, int num_factors
			, double rate
			) {
		double[][] agent_factors_swap = new double[num_agents][num_factors];
		double[][] factor_agents_swap = new double[num_factors][num_agents];
		
		//learn what agents each factor represents
		for( int i = 0; i < num_agents; i++) {
			for( int j = 0; j < num_factors; j++) {
				for( int m = 0; m < agent_agents[i].length; m++) {
					int k = agent_agents[i][m];
					factor_agents_swap[j][k] += agent_factors[i][j];
				}
			}
		}
		double fagents_norm = 1.0/(double)(num_agents);
		for( int j = 0; j < num_factors; j++) {
			for( int k = 0; k < num_agents; k++) {
				factor_agents[j][k] += (factor_agents_swap[j][k]*fagents_norm - factor_agents[j][k])*rate;
				factor_agents_swap[j][k] = 0;
			}
		}
		
		//learn what factors each agent represents
		for( int i = 0; i < num_agents; i++) {
			for( int j = 0; j < num_factors; j++) {
				for( int m = 0; m < agent_agents[i].length; m++) {
					int k = agent_agents[i][m];
					agent_factors_swap[i][j] += factor_agents[j][k];
				}
			}
		}
		double afactors_nom = 1.0/(double)(num_agents);
		for( int j = 0; j < num_agents; j++) {
			for( int k = 0; k < num_factors; k++) {
				agent_factors[j][k] += (agent_factors_swap[j][k]*afactors_nom - agent_factors[j][k])*rate;
				agent_factors_swap[j][k] = 0;
			}
		}
	}
	
	
}
