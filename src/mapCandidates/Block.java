package mapCandidates;
import java.util.*;

public class Block {
    public int index;
    double[] population;
    double[] prob_turnout;
    double[][] prob_vote = null;//new double[DistrictMap.candidates.size()];
    double[] vote_cache = null;
    double[][] vote_caches = null;
    static boolean use_vote_caches = true;
    static boolean use_vote_cache = true;
    static int cache_reuse_times = 16;
    static int vote_cache_size = 128;
    int cache_reused = 0;

    public Vector<Edge> edges = new Vector<Edge>();
    double[] getVotes() {
        if( use_vote_caches) {
            if( vote_caches == null) {
                vote_caches = new double[vote_cache_size][];
                for( int i = 0; i < vote_caches.length; i++) {
                    generateVotes();
                    vote_caches[i] = vote_cache;
                }
            }
            return vote_caches[(int)(Math.random()*(double)vote_caches.length)];
        } else if( vote_cache == null || cache_reused >= cache_reuse_times) {
            generateVotes();
            cache_reuse_times = 0;
        }
        cache_reuse_times++;
        return vote_cache;
    }

    void generateVotes() {
        double[] votes = new double[prob_vote.length];
        for(int i = 0; i < votes.length; i++) {
            votes[i] = 0;
        }
        for(int h = 0; h < population.length; h++) {
            for(int i = 0; i < population[h]; i++) {
                double p = Math.random();
                if( p > prob_turnout[h]) {
                    continue;
                }
                p = Math.random();
                for( int j = 0; j < prob_vote[h].length; j++) {
                    p -= prob_vote[h][j];
                    if( p <= 0) {
                        votes[j]++;
                        break;
                    }
                }
            }
        }
    vote_cache = votes;
    }
}