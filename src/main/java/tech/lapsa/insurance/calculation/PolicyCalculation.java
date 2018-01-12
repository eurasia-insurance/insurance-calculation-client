package tech.lapsa.insurance.calculation;

import javax.ejb.Local;
import javax.ejb.Remote;

import com.lapsa.insurance.domain.CalculationData;
import com.lapsa.insurance.domain.policy.Policy;

public interface PolicyCalculation extends EJBConstants {

    public static final String BEAN_NAME = "PolicyCalculationBean";

    @Local
    public interface PolicyCalculationLocal extends PolicyCalculation {
	void calculatePolicyCost(final Policy policy) throws CalculationFailed;
    }

    @Remote
    public interface PolicyCalculationRemote extends PolicyCalculation {
    }

    CalculationData calculateAmount(final Policy policy) throws CalculationFailed;
}
