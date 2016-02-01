package solutions;

public class StaticFunctions {
    static int[] factorial_cache = null;
    static int[] factorial_precache = null;
    static long[][] binomial_cache = null;
    static long[][] binomial_precache = null;
    
    public static double[] convertProbsToResults(double[] probs, int voters) {
    	double[] results = new double[probs.length];
    	int target = (int)Math.ceil((double)voters/(double)results.length);
    	double total = 0;
    	for( int i = 0; i < results.length; i++) {
    		total += probs[i];
    	}
    	total = 1.0/total;
    	for( int i = 0; i < results.length; i++) {
    		results[i] = probs[i]*total;
    	}
    	for( int i = 0; i < results.length; i++) {
    		results[i] = binomial_cdf(voters,target,results[i]);
    		
    	}
    	return results;
    }
    
    public static double binomial_cdf(int n, int k, double p) {
    	if( n >= Settings.approximate_binomial_as_normal_at_n_equals) {
    		return Gaussian.binomial_as_normal(n, k, p);
    	}
    	double d = 0;
    	double tp = 1;
    	double tpm = p;
    	double np = Math.pow(1-p,n);
    	double npm = 1.0/(1-p);
    	
    	for( int i = 0; i < k; i++) {
    		d += binomial(n,i)*tp*np;
    		tp *= tpm;
    		np *= npm;
    	}
    	/*
    	for( int i = 0; i < k; i++) {
    		d += binomial_pdf(n,i,p);
    	}*/
    	return d;
    }
    public static double binomial_pdf(int n, int k, double p) {
    	return ((double)binomial(n,k))*Math.pow(p,k)*Math.pow(1-p,n-k);
    }
    
    public static int factorial(int n) {
    	if( factorial_precache == null) {
    		System.out.println("creating factorial cache...");

    		factorial_precache = new int[64];
    		for( int i = 0; i < factorial_precache.length; i++) {
    			factorial_precache[i] = factorial(i);
    		}
    		factorial_cache = factorial_precache;
    		System.out.println("factorial cache created.");
    	}
    	if( factorial_cache != null && factorial_cache.length > n) {
    		return factorial_cache[n];
    	}
    	int start = 1;
    	int fact = 1;
    	if( factorial_cache != null) {
    		start = factorial_cache.length-1;
    		fact = factorial_cache[start];
    	}
        for (int i = start; i <= n; i++) {
            fact *= i;
        }
        return fact;
    }

    public static long binomial(int n, int k) {
    	if( binomial_precache == null) {
    		System.out.println("creating binomial cache...");
    		binomial_precache = new long[Settings.binomial_cache_size][];
    		for( int i = 0; i < binomial_precache.length; i++) {
        		binomial_precache[i] = new long[Settings.binomial_cache_size];
        		for( int j = 0; j < binomial_precache[i].length; j++) {
        			binomial_precache[i][j] = binomial(i,j);
        		}
    		}
    		binomial_cache = binomial_precache;
    		System.out.println("binomial cache created.");
    	}
    	if( binomial_cache != null && binomial_cache.length > n && binomial_cache.length > k) {
    		return binomial_cache[n][k];
    	}

    	
        if (k>n-k)
            k=n-k;
 
        long b=1;
        for (int i=1, m=n; i<=k; i++, m--)
            b=b*m/i;
        return b;
    }


}
