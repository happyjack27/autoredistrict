package pareto.prototypes;

public interface Scorer<T extends ParetoPoint<T>> {
	double computeScore(ParetoPoint<T> paretoPoint);
}
