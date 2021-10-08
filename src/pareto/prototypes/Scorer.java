package pareto.prototypes;

public interface Scorer<T extends ParetoPoint<T>> {
	public double computeScore(ParetoPoint<T> paretoPoint);
}
