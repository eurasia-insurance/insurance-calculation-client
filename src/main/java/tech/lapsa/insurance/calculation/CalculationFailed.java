package tech.lapsa.insurance.calculation;

public class CalculationFailed extends Exception {

    private static final long serialVersionUID = 1L;

    public CalculationFailed() {
	super();
    }

    public CalculationFailed(String message, Throwable cause) {
	super(message, cause);
    }

    public CalculationFailed(String message) {
	super(message);
    }

    public CalculationFailed(Throwable cause) {
	super(cause);
    }
}