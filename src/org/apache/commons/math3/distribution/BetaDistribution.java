/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.math3.distribution;

import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.special.Beta;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;

import new_metrics.*;

/**
 * Implements the Beta distribution.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Beta_distribution">Beta distribution</a>
 * @since 2.0 (changed to concrete class in 3.0)
 */
public class BetaDistribution extends AbstractRealDistribution {
	
	public BetaDistribution(double[] xs) {
		super(new Well19937c());
		double[] ps = MLE(xs);
		alpha = ps[0];
		beta = ps[1];
		z = Double.NaN;
	}
	
    public double sample_mean(double[] xs) {
        // compute mean
        double mean = 0;
        for (double x : xs) {
            mean += x;
        }
        mean /= (double)xs.length;
        return mean;
    }
    
    public double sample_variance(double[] xs) {
        double mean = sample_mean(xs);
        double var = 0;
        for (double x : xs) {
            double s = x - mean;
            var += s * s;
        }
        var /= (double)(xs.length - 1);
        return var;
    }
	
	
    public double[] mom_estimate(double[] xs) {
        double mean = sample_mean(xs);
        double var = sample_variance(xs);
        
        // compute a and B
        return new double[]{
        		(mean) * (mean * (1 - mean) / var - 1)
        		,(1 - mean) * (mean * (1 - mean) / var - 1)
        };
    }

	    public double[] MLE(double[] xs)
	    {
	        return MLE_Newton(xs);
	    }

	    public double[] MLE_Newton(double[] xs)
	    {
	        double[] params = mom_estimate(xs);
	        //System.out.println("initial: "+params[0]+","+params[1]);
	        if(true) {
	        	return params;
	        }
	        
	        double delta = 1;
	        double delta_mult = 0.66; // slight overlap
	        double done = 0.0001;
	        
	        double best = avg_log_likelihood(xs, params[0], params[1]);
	        while (delta > done) {
	            // correct a
	        	double a_up = params[0] * Math.exp(delta);
	        	double a_dn = params[0] * Math.exp(-delta);
	        	double up = avg_log_likelihood(xs, a_up, params[1]);
	        	double dn = avg_log_likelihood(xs, a_dn, params[1]);
	            if (up > best) {
	                params[0] = a_up;
	                best = up;
	    	        //System.out.println("improved: "+params[0]+","+params[1]);
	            }
	            if (dn > best) {
	                params[0] = a_dn;
	                best = dn;
	    	        //System.out.println("improved: "+params[0]+","+params[1]);
	            }
	            // correct b
	            a_up = params[1] * Math.exp(delta);
	            a_dn = params[1] * Math.exp(- delta);
	            up = avg_log_likelihood(xs, params[0], a_up);
	            dn = avg_log_likelihood(xs, params[0], a_dn);
	            if (up > best) {
	                params[1] = a_up;
	                best = up;
	    	        //System.out.println("improved: "+params[0]+","+params[1]);
	            }
	            if (dn > best) {
	                params[1] = a_dn;
	                best = dn;
	    	        //System.out.println("improved: "+params[0]+","+params[1]);
	            }
	            // zoom in
	            delta *= delta_mult;
	        }
	        //System.out.println("final: "+params[0]+","+params[1]);

	        return params;
	    }

	    public double avg_log_likelihood(double[] xs, double a, double b) {

	    	this.alpha = a;
	    	this.beta = b;
	    	
	        double sum = 0;
	        for( double x : xs) {
	        	try {
	        		sum += logDensity(x);
	        	} catch (Exception ex) {
	        		
	        	}
	        }
	        System.out.println("ll: "+(sum / xs.length));
	        return -sum / xs.length;
	    }
	
	
    /**
     * Default inverse cumulative probability accuracy.
     * @since 2.1
     */
    public static final double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1e-9;
    /** Serializable version identifier. */
    private static final long serialVersionUID = -1221965979403477668L;
    /** First shape parameter. */
    private double alpha;
    /** Second shape parameter. */
    private double beta;
    /** Normalizing factor used in density computations.
     * updated whenever alpha or beta are changed.
     */
    private double z;
    /** Inverse cumulative probability accuracy. */
    private double solverAbsoluteAccuracy = DEFAULT_INVERSE_ABSOLUTE_ACCURACY;

    /**
     * Build a new instance.
     * <p>
     * <b>Note:</b> this constructor will implicitly create an instance of
     * {@link Well19937c} as random generator to be used for sampling only (see
     * {@link #sample()} and {@link #sample(int)}). In case no sampling is
     * needed for the created distribution, it is advised to pass {@code null}
     * as random generator via the appropriate constructors to avoid the
     * additional initialisation overhead.
     *
     * @param alpha First shape parameter (must be positive).
     * @param beta Second shape parameter (must be positive).
     */
    public BetaDistribution(double alpha, double beta) {
        this(alpha, beta, DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    /**
     * Build a new instance.
     * <p>
     * <b>Note:</b> this constructor will implicitly create an instance of
     * {@link Well19937c} as random generator to be used for sampling only (see
     * {@link #sample()} and {@link #sample(int)}). In case no sampling is
     * needed for the created distribution, it is advised to pass {@code null}
     * as random generator via the appropriate constructors to avoid the
     * additional initialisation overhead.
     *
     * @param alpha First shape parameter (must be positive).
     * @param beta Second shape parameter (must be positive).
     * @param inverseCumAccuracy Maximum absolute error in inverse
     * cumulative probability estimates (defaults to
     * {@link #DEFAULT_INVERSE_ABSOLUTE_ACCURACY}).
     * @since 2.1
     */
    public BetaDistribution(double alpha, double beta, double inverseCumAccuracy) {
        this(new Well19937c(), alpha, beta, inverseCumAccuracy);
    }

    /**
     * Creates a &beta; distribution.
     *
     * @param rng Random number generator.
     * @param alpha First shape parameter (must be positive).
     * @param beta Second shape parameter (must be positive).
     * @since 3.3
     */
    public BetaDistribution(RandomGenerator rng, double alpha, double beta) {
        this(rng, alpha, beta, DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    /**
     * Creates a &beta; distribution.
     *
     * @param rng Random number generator.
     * @param alpha First shape parameter (must be positive).
     * @param beta Second shape parameter (must be positive).
     * @param inverseCumAccuracy Maximum absolute error in inverse
     * cumulative probability estimates (defaults to
     * {@link #DEFAULT_INVERSE_ABSOLUTE_ACCURACY}).
     * @since 3.1
     */
    public BetaDistribution(RandomGenerator rng,
                            double alpha,
                            double beta,
                            double inverseCumAccuracy) {
        super(rng);

        this.alpha = alpha;
        this.beta = beta;
        z = Double.NaN;
        solverAbsoluteAccuracy = inverseCumAccuracy;
    }

    /**
     * Access the first shape parameter, {@code alpha}.
     *
     * @return the first shape parameter.
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * Access the second shape parameter, {@code beta}.
     *
     * @return the second shape parameter.
     */
    public double getBeta() {
        return beta;
    }

    /** Recompute the normalization factor. */
    private void recomputeZ() {
        if (Double.isNaN(z)) {
            z = Gamma.logGamma(alpha) + Gamma.logGamma(beta) - Gamma.logGamma(alpha + beta);
        }
    }

    /** {@inheritDoc} */
    public double density(double x) {
    	try {
    		final double logDensity = logDensity(x);
    		return logDensity == Double.NEGATIVE_INFINITY ? 0 : FastMath.exp(logDensity);
    	} catch (Exception ex) {
    		return 0;
    	}
    }

    /** {@inheritDoc} **/
    @Override
    public double logDensity(double x) {
        recomputeZ();
        if (x < 0 || x > 1) {
            return Double.NEGATIVE_INFINITY;
        } else if (x == 0) {
            if (alpha < 1) {
                throw new NumberIsTooSmallException(LocalizedFormats.CANNOT_COMPUTE_BETA_DENSITY_AT_0_FOR_SOME_ALPHA, alpha, 1, false);
            }
            return Double.NEGATIVE_INFINITY;
        } else if (x == 1) {
            if (beta < 1) {
                throw new NumberIsTooSmallException(LocalizedFormats.CANNOT_COMPUTE_BETA_DENSITY_AT_1_FOR_SOME_BETA, beta, 1, false);
            }
            return Double.NEGATIVE_INFINITY;
        } else {
            double logX = FastMath.log(x);
            double log1mX = FastMath.log1p(-x);
            return (alpha - 1) * logX + (beta - 1) * log1mX - z;
        }
    }

    /** {@inheritDoc} */
    public double cumulativeProbability(double x)  {
        if (x <= 0) {
            return 0;
        } else if (x >= 1) {
            return 1;
        } else {
            return Beta.regularizedBeta(x, alpha, beta);
        }
    }

    /**
     * Return the absolute accuracy setting of the solver used to estimate
     * inverse cumulative probabilities.
     *
     * @return the solver absolute accuracy.
     * @since 2.1
     */
    @Override
    protected double getSolverAbsoluteAccuracy() {
        return solverAbsoluteAccuracy;
    }

    /**
     * {@inheritDoc}
     *
     * For first shape parameter {@code alpha} and second shape parameter
     * {@code beta}, the mean is {@code alpha / (alpha + beta)}.
     */
    public double getNumericalMean() {
        final double a = getAlpha();
        return a / (a + getBeta());
    }

    /**
     * {@inheritDoc}
     *
     * For first shape parameter {@code alpha} and second shape parameter
     * {@code beta}, the variance is
     * {@code (alpha * beta) / [(alpha + beta)^2 * (alpha + beta + 1)]}.
     */
    public double getNumericalVariance() {
        final double a = getAlpha();
        final double b = getBeta();
        final double alphabetasum = a + b;
        return (a * b) / ((alphabetasum * alphabetasum) * (alphabetasum + 1));
    }

    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is always 0 no matter the parameters.
     *
     * @return lower bound of the support (always 0)
     */
    public double getSupportLowerBound() {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * The upper bound of the support is always 1 no matter the parameters.
     *
     * @return upper bound of the support (always 1)
     */
    public double getSupportUpperBound() {
        return 1;
    }

    /** {@inheritDoc} */
    public boolean isSupportLowerBoundInclusive() {
        return false;
    }

    /** {@inheritDoc} */
    public boolean isSupportUpperBoundInclusive() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * The support of this distribution is connected.
     *
     * @return {@code true}
     */
    public boolean isSupportConnected() {
        return true;
    }


    /** {@inheritDoc}
    * <p>
    * Sampling is performed using Cheng algorithms:
    * </p>
    * <p>
    * R. C. H. Cheng, "Generating beta variates with nonintegral shape parameters.".
    *                 Communications of the ACM, 21, 317â€“322, 1978.
    * </p>
    */
    @Override
    public double sample() {
        return ChengBetaSampler.sample(random, alpha, beta);
    }

    /** Utility class implementing Cheng's algorithms for beta distribution sampling.
     * <p>
     * R. C. H. Cheng, "Generating beta variates with nonintegral shape parameters.".
     *                 Communications of the ACM, 21, 317â€“322, 1978.
     * </p>
     * @since 3.6
     */
    private static final class ChengBetaSampler {

        /**
         * Returns one sample using Cheng's sampling algorithm.
         * @param random random generator to use
         * @param alpha distribution first shape parameter
         * @param beta distribution second shape parameter
         * @return sampled value
         */
        static double sample(RandomGenerator random, final double alpha, final double beta) {
            final double a = FastMath.min(alpha, beta);
            final double b = FastMath.max(alpha, beta);

            if (a > 1) {
                return algorithmBB(random, alpha, a, b);
            } else {
                return algorithmBC(random, alpha, b, a);
            }
        }

        /**
         * Returns one sample using Cheng's BB algorithm, when both &alpha; and &beta; are greater than 1.
         * @param random random generator to use
         * @param a0 distribution first shape parameter (&alpha;)
         * @param a min(&alpha;, &beta;) where &alpha;, &beta; are the two distribution shape parameters
         * @param b max(&alpha;, &beta;) where &alpha;, &beta; are the two distribution shape parameters
         * @return sampled value
         */
        private static double algorithmBB(RandomGenerator random,
                                          final double a0,
                                          final double a,
                                          final double b) {
            final double alpha = a + b;
            final double beta = FastMath.sqrt((alpha - 2.) / (2. * a * b - alpha));
            final double gamma = a + 1. / beta;

            double r;
            double w;
            double t;
            do {
                final double u1 = random.nextDouble();
                final double u2 = random.nextDouble();
                final double v = beta * (FastMath.log(u1) - FastMath.log1p(-u1));
                w = a * FastMath.exp(v);
                final double z = u1 * u1 * u2;
                r = gamma * v - 1.3862944;
                final double s = a + r - w;
                if (s + 2.609438 >= 5 * z) {
                    break;
                }

                t = FastMath.log(z);
                if (s >= t) {
                    break;
                }
            } while (r + alpha * (FastMath.log(alpha) - FastMath.log(b + w)) < t);

            w = FastMath.min(w, Double.MAX_VALUE);
            return Precision.equals(a, a0) ? w / (b + w) : b / (b + w);
        }

        /**
         * Returns one sample using Cheng's BC algorithm, when at least one of &alpha; and &beta; is smaller than 1.
         * @param random random generator to use
         * @param a0 distribution first shape parameter (&alpha;)
         * @param a max(&alpha;, &beta;) where &alpha;, &beta; are the two distribution shape parameters
         * @param b min(&alpha;, &beta;) where &alpha;, &beta; are the two distribution shape parameters
         * @return sampled value
         */
        private static double algorithmBC(RandomGenerator random,
                                          final double a0,
                                          final double a,
                                          final double b) {
            final double alpha = a + b;
            final double beta = 1. / b;
            final double delta = 1. + a - b;
            final double k1 = delta * (0.0138889 + 0.0416667 * b) / (a * beta - 0.777778);
            final double k2 = 0.25 + (0.5 + 0.25 / delta) * b;

            double w;
            for (;;) {
                final double u1 = random.nextDouble();
                final double u2 = random.nextDouble();
                final double y = u1 * u2;
                final double z = u1 * y;
                if (u1 < 0.5) {
                    if (0.25 * u2 + z - y >= k1) {
                        continue;
                    }
                } else {
                    if (z <= 0.25) {
                        final double v = beta * (FastMath.log(u1) - FastMath.log1p(-u1));
                        w = a * FastMath.exp(v);
                        break;
                    }

                    if (z >= k2) {
                        continue;
                    }
                }

                final double v = beta * (FastMath.log(u1) - FastMath.log1p(-u1));
                w = a * FastMath.exp(v);
                if (alpha * (FastMath.log(alpha) - FastMath.log(b + w) + v) - 1.3862944 >= FastMath.log(z)) {
                    break;
                }
            }

            w = FastMath.min(w, Double.MAX_VALUE);
            return Precision.equals(a, a0) ? w / (b + w) : b / (b + w);
        }

    }
}
