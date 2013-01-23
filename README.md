autoredistrict
==============

Programmatically makes a fair congressional district map (prevents gerrymandering)


/*
block and length data can be taken from a standard dime file, which i presume any municiplality has. the census data, well i'm sure they have it electronically, cause hey, they draw districts. so getting the geolocation dataset is not a problem. you still have to add in code to make sure the districts are contiguous, though.
*/

//QUANTITIATIVE GERRYMANDER CALCULATOR 
//for use in heuristic optimization (e.g. genetic algoritm / swarm ) for computer-automated redistricting.
class District {
    Vector blocks;
    double getEdgeLength() {
        double length = 0;
        for( Block block : blocks)
            for( Edge edge : block.edges)
                if( edge.block1.district != edge.block2.district)
                    length += edge.length;
        return length;
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
public double[][] getRandomResultSample(Vector districts) {
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
public double[] getGerryManderScores(Vector districts) {
    double length = 0;
    for(District district : districts) {
        length += district.getEdgeLength();
    } //TODO: calculate kldiv as http://en.wikipedia.org/wiki/Kullback%E2%80%93Leibler_divergence [wikipedia.org] , where p=popular_results and q=election_results (q is used to approximate p)
    double kldiv = 0;
    for( int i = 0; i < 100; i++) {
        double[][] results = getRandomResultSample(districts);
        double[] popular_results = results[0];
        double[] election_results = results[1];
    }
    return new double[]{length,Math.exp(kldiv)}; //exponentiate because each bit represents twice as many people disenfranched
}
