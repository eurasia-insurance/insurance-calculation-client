package tech.lapsa.insurance.calculation;

import javax.ejb.Local;
import javax.ejb.Remote;

import com.lapsa.insurance.domain.casco.Casco;

public interface CascoCalculation extends EJBConstants {

    public static final String BEAN_NAME = "CascoCalculationBean";

    @Local
    public interface CascoCalculationLocal extends CascoCalculation {
    }

    @Remote
    public interface CascoCalculationRemote extends CascoCalculation {
    }

    void calculateCascoCost(final Casco casco) throws CalculationFailed;
}
