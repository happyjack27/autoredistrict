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
    
    import java.util.*;

    interface iEvolvable {
        public int[] getGenome();
        public void setGenome(int[] genome);
        public double getFitnessScore();
    }

    class DistrictMap implements iEvolvable {
        public static double geometry_weight = 1;
        public static double disenfranchise_weight = 1;
        public static double population_balance_weight = 1;
        
        Vector<Block> blocks;
        Vector<District> districts;
        int num_districts = 0;
        int[] block_districts;
        
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
                districts.get(genome[i]).add(blocks.get(i));
        }
        public double getFitnessScore() {
            double[] scores_to_minimize = getGerryManderScores(1000);
            return -(scores_to_minimize[0]*geometry_weight + scores_to_minimize[1]*disenfranchise_weight + scores_to_minimize[2]*population_balance_weight);
        }
        
        //helper functions
        public double[][] getRandomResultSample() {
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
    
            //get totals
            double totp = 0;
            for( int i = 0; i < p.length; i++)
                totp += p[i];  
            double totq = 0;
            for( int i = 0; i < q.length; i++)
                totq += q[i];  
    
            //make same ratio before regularizing.
            double ratio = p[i] / q[i];
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
        public double[] getGerryManderScores(int trials) {
            double length = 0;
            double total_population = 0;
            double[] dist_pops = new double[districts.size()];
            int h = 0;
            for(District district : districts) {
                length += district.getEdgeLength(block_districts);
                dist_pops[h] = district.getPopulation();
                total_population += dist_pops[h];
                h++;
            }
            double exp_population = total_population/districts.size();
            double[] perfect_dists = new double[districts.size()];
            for( int i = 0; i < perfect_dists.length; i++)
                perfect_dists[i] = exp_population;
        
            //simulate trials elections and accumulate the results
            double[] p = new double[num_parties];
            double[] q = new double[num_parties];
            for( int i = 0; i < trials; i++) {
                double[][] results = getRandomResultSample();
                for( int j = 0; j < num_parties; j++) {
                    p[j] += results[0][j];
                    q[j] += results[1][j];
                }
            }
            return new double[]{length,Math.exp(getKLDiv(p,q)),Math.exp(getKLDiv(perfect_dists,dist_pops))}; //exponentiate because each bit represents twice as many people disenfranched
        }
    }
    
    //buisness objects
    class District {
        Vector<Block> blocks = new Vector<Block>();
        double getEdgeLength(int[] block_districts) {
            double length = 0;
            Vector<Edge> outerEdges = getOuterEdges();
            for( Edge edge : outerEdges)
                length += edge.length;
            return length;
        }
        double getPopulation() {
            double pop = 0;
            for( Block block : blocks)
                  pop += block.population;
            return pop;
        }
        
        //getRegionCount() counts the number of contiguous regions by counting the number of vertex cycles.  a proper map will have exactly 1 contiguous region per district.
        //this is a constraint to apply _AFTER_ a long initial optimization.  as a final tuning step.
        int getRegionCount() {
            return getRegions().size();
        }
        Vector<Vector<Block>> getRegions() {
            Hashtable<Block,Vector<Block>> region_hash = new Hashtable<Block,Vector<Block>>();
            Vector<Vector<Block>> regions = new Vector<Vector<Block>>();
            for( Block block : blocks) {
                if( region_hash.get(block) != null)
                    continue;
                Vector<Block> region = new Vector<Block>();
                regions.add(region);
                addAllConnected(block,region,region_hash);
            }
            return regions;
        }
        //recursively insert connected blocks.
        void addAllConnected( Block block, Vector<Block> region,  Hashtable<Block,Vector<Block>> region_hash) {
            if( region_hash.get(block) != null)
                return;
            region.add(block);
            region_hash.put(block,region);
            for( Edge edge : block.edges)
                if( edge.areBothSidesSameDistrict(block_districts))
                    addAllConnected( edge.block1 == block ? edge.block2 : edge.block1, region, region_hash);
        }
        void getRegionPopulation(Vector<Block> region) {
            double population = 0;
            for( Block block : region)
                population += region.population;
            return population;
        }

        
        Vector<Edge> getOuterEdges() {
            Vector<Edge> outerEdges = new Vector<Edge>();
            for( Block block : blocks)
                for( Edge edge : block.edges)
                    if( !edge.areBothSidesSameDistrict(block_districts))
                        outerEdges.add(edge);
            return outerEdges;
        }
    }
    class Block {
        int index;
        double population;
        double prob_turnout;
        double[] prob_vote = new double[num_parties];
        Vector<Edge> edges = new Vector<Edge>();
        double[] getVotes() {
            double[] votes = new double[num_parties];
            //TODO: get random variables of the vote distribution given the statistics for the block.
            
            return votes;
        }
    }
    class Edge {
        Block block1;
        Block block2;
        Vertex vertex1;
        Vertex vertex2;
        double length;
        boolean areBothSidesSameDistrict(int[] block_districts) {
            return block_districts[block1.index] == block_districts[block2.index];
        }
    }
    class Vertex {
    }
