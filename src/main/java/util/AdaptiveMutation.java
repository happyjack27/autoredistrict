package util;
import new_metrics.CustomGammaDistribution;
import org.apache.commons.math3.distribution.*;

public class AdaptiveMutation {

	private final int max_sample_size = 250;
	private int num_samples = 0;
	
	public CustomGammaDistribution gamma = new CustomGammaDistribution(1,1);
	
	public AdaptiveMutation() {
		double[] starting_values = new double[] {
				//-Math.log(0.4)
				-Math.log(0.2)
				,-Math.log(0.1)
				,-Math.log(0.05)
				,-Math.log(0.025)
				,-Math.log(0.0125)
		};
		num_samples = 5;
		
		double[] ab = gamma.mom_estimate(starting_values);
		gamma.setShapeScale(ab[0], ab[1]);
	}

	public double getSample() {
		double d = gamma.inverseCumulativeProbability(Math.random());
		return Math.exp(-d);
	}
	public void addSamples(double[] d) {
		if( num_samples < max_sample_size) {
			for( int i = 0; i < d.length; i++) {
				gamma.update_mean_and_variance(-Math.log(d[i]), num_samples++);
			}
			if( num_samples > max_sample_size) {
				num_samples = max_sample_size;
			}
		} else {
			for( int i = 0; i < d.length; i++) {
				gamma.update_mean_and_variance(-Math.log(d[i]), num_samples);
			}
		}
		gamma.setShapeScaleFromMeanVariance();
		
	}
	public void addSample(double d) {
		if( d <= 0.000000001) {
			return;
		}
		gamma.update_mean_and_variance(-Math.log(d), num_samples);
		if( num_samples < max_sample_size) {
			num_samples++;
		}
	}
	public void recalculate() {
		gamma.setShapeScaleFromMeanVariance();
	}
}
