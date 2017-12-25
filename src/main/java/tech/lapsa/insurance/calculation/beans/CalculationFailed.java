package tech.lapsa.insurance.calculation.beans;

public class CalculationFailed extends Exception {
    private static final long serialVersionUID = 1L;

    CalculationFailed() {
    }

    CalculationFailed(String message) {
	super(message);
    }
}