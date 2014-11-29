package mapCandidates;

import java.util.*;

class DistrictMap implements iEvolvable, Comparable<DistrictMap> {
	
    public static int sorting_polarity = 1;

    public int num_districts = 0;
    public int[] block_districts = new int[]{};
    public double[] fairnessScores = new double[5];
    public double fitness_score = 0;
    
	public Vector<Block> blocks = new Vector<Block>();
	public Vector<District> districts = new Vector<District>();
	
	public static Vector<Candidate> candidates = new Vector<Candidate>();

    //makeLike
    //sfads

    //always find the most identical version before spawning new ones!
    //this dramatically reduces convergence time!
    public int[] getGenome(int[] baseline) {
         Vector<int[]> versions =  getIdenticalGenomes(getGenome());
         long closest = 9999999999999L;
         int[] closest_version = null;
         for( int[] version : versions) {
            int test = getGenomeHammingDistance(version,baseline);
            if( test < closest) {
                closest = test;
                closest_version = version;
            }
         }
         return closest_version;
    }

    public void mutate(double prob) {
        double max = candidates.size();
        for( int i = 0; i < block_districts.length; i++) {
            if( Math.random() < prob) {
                block_districts[i] = (int)(Math.floor(Math.random()*max)+1.0);
            }
        }
    }

    public void mutate_boundary(double prob) {
        boolean[] allow = new boolean[districts.size()+1];
        for( int i = 0; i < block_districts.length; i++) {
            if( Math.random() < prob) {
                for( int j = 0; j < allow.length; j++) {
                    allow[j] = false;
                }
                allow[block_districts[i]] = true;
                Block block = blocks.get(i);
                for( Edge edge : block.edges) {
                    Block other_block = edge.block1 == block ? edge.block2 : edge.block1;
                    allow[block_districts[other_block.index]] = true;
                }
                double count = 0;
                for( int j = 0; j < allow.length; j++) {
                    if( allow[j])
                        count++;
                }
                int d = (int)(Math.random()*count); 
                for( int j = 0; j < allow.length; j++) {
                    if( allow[j]) {
                        if( d == 0) {
                            block_districts[i] = j;
                            break;
                        }
                        d--;
                    }
                }
            }
        }
    }
    public void crossover(int[] genome1, int[] genome2) {
        for( int i = 0; i < block_districts.length; i++) {
            double r = Math.random();
            block_districts[i] = r < 0.5 ? genome1[i] : genome2[i];
        }
    }
    public void makeLike(int[] genome) {
        Vector<int[]> identicals = getIdenticalGenomes(getGenome());
        int lowest_score = 0;
        int best_match = -1;
        for( int i = 0; i < identicals.size(); i++) {
            int new_score = getGenomeHammingDistance(genome,identicals.get(i));
            if( best_match < 0 || new_score < lowest_score) {
                lowest_score = new_score;
                best_match = i;
            }
        }
        setGenome(identicals.get(best_match));
    }


    public static Vector<int[]> getIdenticalGenomes(int[] genome) {
        Vector<int[]> identicals = new Vector<int[]>();

        int max = 0;
        for( int i = 0; i < genome.length; i++) 
            if( genome[i] > max)
                max = genome[i];
        int[] rename = new int[max+1];
        for( int i = 0; i < rename.length; i++)
            rename[i] = i;

        //cycle through all permutations
        Vector<int[]> permutations = new Vector<int[]>();
        getPermutations(rename,0,permutations);
        for( int[] permutation : permutations) 
            identicals.add(getRenamed(genome,permutation));

        return identicals;

    }
    public static void getPermutations(int[] a, int k, Vector<int[]> results) {
        if(k==a.length) {
            int[] result = new int[a.length];
            for( int i = 0; i < a.length; i++)
                result[i] = a[i];
            results.add(result);
            return;
        }
        for (int i = k; i < a.length; i++) {
            int temp = a[k];
            a[k]=a[i];
            a[i]=temp;
            getPermutations(a,k+1,results);
            temp=a[k];
            a[k]=a[i];
            a[i]=temp;
        }
    }
    public static int[] getRenamed(int[] source, int[] rename) {
        int[] new_version = new int[source.length];
        for( int i = 0; i < source.length; i++)
            new_version[i] = rename[source[i]];
        return new_version;
    }
    public static int getGenomeHammingDistance(int[] genome1, int[] genome2) {
        int dist = 0;
        for( int i = 0; i < genome1.length; i++)
            dist += genome1[i] == genome2[i] ? 0 : 1;
        return dist;
    }

    //constructors
    public DistrictMap(Vector<Block> blocks, int num_districts, int[] genome) {
        this(blocks,num_districts);
        setGenome(genome);
    }
    public DistrictMap(Vector<Block> blocks, int num_districts) {
        this.num_districts = num_districts;
        this.blocks = blocks;
        districts = new Vector<District>();
        for( int i = 0; i < num_districts; i++)
            districts.add(new District());
        block_districts = new int[blocks.size()];
        mutate(1);
    }

    //genetic evolution primary functions
    public int[] getGenome() {
        return block_districts;
    }
    public void setGenome(int[] genome) {
        block_districts = genome;
        districts = new Vector<District>();
        for( int i = 0; i < num_districts; i++)
            districts.add(new District());
        for( int i = 0; i < genome.length; i++)
            districts.get(genome[i]).blocks.add(blocks.get(i));
    }

    //helper functions
    public double[][] getRandomResultSample() {
        double[] popular_vote = new double[candidates.size()]; //inited to 0
        double[] elected_vote = new double[candidates.size()]; //inited to 0
        for(District district : districts) {
            double[] district_vote = district.getVotes();
            for( int i = 0; i < district_vote.length; i++) {
                popular_vote[i] += district_vote[i];
            }
            elected_vote[district.last_winner]++;
        }
        return new double[][]{popular_vote,elected_vote};
    }

    //calculate kldiv as http://en.wikipedia.org/wiki/Kullback%E2%80%93Leibler_divergence [wikipedia.org] , where p=popular_results and q=election_results (q is used to approximate p)
    public double getKLDiv(double[] p, double[] q) {

        //get totals
        double totp = 0;
        for( int i = 0; i < p.length; i++)
            totp += p[i];  
        double totq = 0;
        for( int i = 0; i < q.length; i++)
            totq += q[i];  

        //make same ratio before regularizing.
        double ratio = totp/totq;
        for( int i = 0; i < q.length; i++)
            q[i] *= ratio;  

        //regularize (see "regularization" in statistics)
        for( int i = 0; i < p.length; i++)
            p[i]++;  
        for( int i = 0; i < q.length; i++)
            q[i]++;  

        //normalize
        totp = 0;
        for( int i = 0; i < p.length; i++)
            totp += p[i];  
        for( int i = 0; i < p.length; i++)
            p[i] /= totp;
        totq = 0;
        for( int i = 0; i < q.length; i++)
            totq += q[i];  
        for( int i = 0; i < q.length; i++)
            q[i] /= totq;

        //get kldiv
        double div = 0;
            for( int i = 0; i < q.length; i++)
                div += -p[i]*(Math.log(q[i]) - Math.log(p[i]));
        return div;
    }

    //returns total edge length, unfairness, population imbalance
    //a heuristic optimization algorithm would use a weighted combination of these 3 values as a cost function to minimize.
    public void calcFairnessScores(int trials) {
        double length = getEdgeLength();
        double total_population = 0;
        double[] dist_pops = new double[districts.size()];
        double[] dist_pop_frac = new double[districts.size()];

        for(int i = 0; i < districts.size(); i++) {
            District district = districts.get(i);
            district.resetWins();
            dist_pops[i] = district.getPopulation();
            total_population += dist_pops[i];
        }
        for(int i = 0; i < districts.size(); i++) {
            dist_pop_frac[i] =  dist_pops[i] / total_population;
        }

        double exp_population = total_population/districts.size();
        double[] perfect_dists = new double[districts.size()];
        for( int i = 0; i < perfect_dists.length; i++)
            perfect_dists[i] = exp_population;

        //simulate trials elections and accumulate the results
        double[] p = new double[candidates.size()];
        double[] q = new double[candidates.size()];
        for( int i = 0; i < trials; i++) {
            double[][] results = getRandomResultSample();
            for( int j = 0; j < candidates.size(); j++) {
                p[j] += results[0][j];
                q[j] += results[1][j];
            }
        }

        double[] voting_power = new double[districts.size()];
        double total_voting_power = 0;
        for(int i = 0; i < districts.size(); i++) {
            District district = districts.get(i);
            voting_power[i] = district.getSelfEntropy();
            total_voting_power += voting_power[i];
        }

        for(int i = 0; i < districts.size(); i++) {
            voting_power[i] /= total_voting_power;
        }

        double power_fairness = 0; //1 = perfect fairness
        for(int i = 0; i < districts.size(); i++) {
            power_fairness += dist_pop_frac[i]*voting_power[i];
        }

        double disconnected_pops = 0;
        if( Settings.disconnected_population_weight > 0) {
            for(District district : districts)
                disconnected_pops += district.getPopulation() - district.getRegionPopulation(district.getTopPopulationRegion(block_districts));
        }
        disconnected_pops /= total_population;
        fairnessScores = new double[]{length,Math.exp(getKLDiv(p,q)),Math.exp(getKLDiv(perfect_dists,dist_pops)),disconnected_pops,power_fairness}; //exponentiate because each bit represents twice as many people disenfranched
    }

    public int compareTo(DistrictMap o) {
        double d = (fitness_score-o.fitness_score)*sorting_polarity; 
        return  d > 0 ? 1 : d == 0 ? 0 : -1;
    }
    double getEdgeLength() {
        double length = 0;
        Vector<Edge> outerEdges = getOuterEdges(block_districts);
        for( Edge edge : outerEdges)
            length += edge.length;
        return length;
    }
    Vector<Edge> getOuterEdges(int[] block_districts) {
        Vector<Edge> outerEdges = new Vector<Edge>();
        for( Block block : blocks)
            for( Edge edge : block.edges)
                if( !edge.areBothSidesSameDistrict(block_districts))
                    outerEdges.add(edge);
        return outerEdges;
    }
}
