autoredistrict
==============

Programmatically makes a fair congressional district map (prevents gerrymandering)

/*
block and length data can be taken from a standard dime file, which i presume any municiplality has. the census data, well i'm sure they have it electronically, cause hey, they draw districts. so getting the geolocation dataset is not a problem. you still have to add in code to make sure the districts are contiguous, though.

basic idea is you do it gestalt, without worrying about contiguity a prioiri.

any standard heuristic optimization algorithm will do fine; generic algorithm and swarm intelligence are the two that come most readily to mind.

the gene of a genetic algorithm would just be a vector of which district each block belongs to, in order.  so one would have to add an ordering to the blocks.  (i.e. number them 1 through whatever).  

the two non trivial things: calculating geometric complexity and calculating democratic representativeness are what are shown.  the first, by minimizing border length, the second, by minimizing kullback-leibler divergence of the election results from the popular vote. 
*/

//QUANTITIATIVE GERRYMANDER CALCULATOR 
//for use in heuristic optimization (e.g. genetic algoritm / swarm ) for computer-automated redistricting.

//language: Java

class District {
    Vector<Block> blocks;
    double getEdgeLength() {
        double length = 0;
        for( Block block : blocks)
            for( Edge edge : block.edges)
                if( edge.block1.district != edge.block2.district)
                    length += edge.length;
        return length;
    }
    double getPopulation() {
        double pop = 0;
        for( Block block : blocks)
              pop += block.population;
        return pop;
    }
}
class Block {
    int district;
    double population;
    double prob_turnout;
    double[] prob_vote = new double[num_parties];
    Vector edges;
    double[] getVotes() { //TODO: get random variables of the vote distribution given the statistics for the block.
    }
}
class Edge {
    Block block1;
    Block block2;
    double length;
}
public double[][] getRandomResultSample(Vector<District> districts) {
    double[] popular_vote = new double[num_parties]; //inited to 0
    double[] elected_vote = new double[num_parties]; //inited to 0
    for(District district : districts) {
        double district_vote = new double[num_parties]; //inited to 0
        for( Block block : district.blocks) {
            double[] block_vote = block.getVotes();
            for( int i = 0; i most_value) {
                most_index = i;
                most_value = district_vote[i];
            }
        }
        elected_vote[most_index]++;
    }
    return new double[][]{popular_vote,elected_vote};
}

//calculate kldiv as http://en.wikipedia.org/wiki/Kullback%E2%80%93Leibler_divergence [wikipedia.org] , where p=popular_results and q=election_results (q is used to approximate p)
public double getKLDiv(double[] p, double[] q) {
    //normalize
    double tot = 0;
    for( int i = 0; i < p.length; i++)
        tot += p[i];  
    for( int i = 0; i < p.length; i++)
        p[i] /= tot;
    tot = 0;
    for( int i = 0; i < q.length; i++)
        tot += q[i];  
    for( int i = 0; i < q.length; i++)
        q[i] /= tot;

    //get kldiv
    double div = 0;
    for( int i = 0; i < q.length; i++)
        div += -p[i]*Math.log(q[i]) + p[i]*Math.log(p[i]);
    return div;
}

//returns total edge length, unfairness, population imbalance
//a heuristic optimization algorithm would use a weighted combination of these 3 values as a cost function to minimize.
public double[] getGerryManderScores(Vector<District> districts) {
    double length = 0;
    double total_population = 0;
    double[] dist_pops = new double[districts.size()];
    int h = 0;
    for(District district : districts) {
        length += district.getEdgeLength();
        dist_pops[h] = district.getPopulation();
        total_population += dist_pops[h];
        h++;
    }
    double exp_population = total_population/districts.size();
    double[] perfect_dists = new double[districts.size()];
    for( int i = 0; i < perfect_dists.length; i++)
        perfect_dists[i] = exp_population;

    double[] p = new double[num_parties];
    double[] q = new double[num_parties];
    for( int i = 0; i < 1000; i++) {
        double[][] results = getRandomResultSample(districts);
        for( int j = 0; j < num_parties; j++) {
             p[j] += results[0][j];
             q[j] += results[1][j];
        }
    }
    return new double[]{length,Math.exp(getKLDiv(p,q)),Math.exp(getKLDiv(perfect_dists,dist_pops))}; //exponentiate because each bit represents twice as many people disenfranched
}
