package test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static test.TestObjectsCreatorHelper.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Currency;

import javax.inject.Inject;

import org.junit.Test;

import com.lapsa.insurance.domain.InsurancePeriodData;
import com.lapsa.insurance.domain.policy.Policy;
import com.lapsa.insurance.domain.policy.PolicyDriver;
import com.lapsa.insurance.domain.policy.PolicyVehicle;
import com.lapsa.insurance.elements.InsuranceClassType;
import com.lapsa.insurance.elements.InsuredAgeClass;
import com.lapsa.insurance.elements.InsuredExpirienceClass;
import com.lapsa.insurance.elements.VehicleAgeClass;
import com.lapsa.insurance.elements.VehicleClass;

import tech.lapsa.insurance.calculation.CalculationFailed;
import tech.lapsa.insurance.calculation.PolicyCalculation.PolicyCalculationLocal;

public class PolicyCalculationServiceTest extends ArquillianBaseTestCase {

    @Inject
    private PolicyCalculationLocal calc;

    @Test
    public void testPolicyCalculationVariant1() throws CalculationFailed {
	Policy policy = generatePolicy();
	calc.calculatePolicyCost(policy);
	assertThat(policy.getCalculation().getAmount(), equalTo(30804d));
    }

    @Test
    public void testPolicyCalculationPeriod1() throws CalculationFailed {
	Policy policy = generatePolicy();
	InsurancePeriodData period = new InsurancePeriodData();
	period.setFrom(LocalDate.of(2016, 10, 26));
	period.setTo(period.getFrom().plusDays(365));
	policy.setPeriod(period);
	calc.calculatePolicyCost(policy);
	assertThat(policy.getCalculation().getAmount(), equalTo(30804d));
	assertThat(policy.getCalculation().getCurrency(),
		allOf(not(nullValue()), equalTo(Currency.getInstance("KZT"))));
    }

    @Test
    public void testPolicyCalculationPeriod2() throws CalculationFailed {
	Policy policy = generatePolicy();
	InsurancePeriodData period = new InsurancePeriodData();
	period.setFrom(LocalDate.of(2016, 10, 26));
	period.setTo(period.getFrom().plusMonths(1));
	policy.setPeriod(period);
	calc.calculatePolicyCost(policy);
	assertThat(policy.getCalculation().getAmount(), equalTo(2693d));
	assertThat(policy.getCalculation().getCurrency(),
		allOf(not(nullValue()), equalTo(Currency.getInstance("KZT"))));
    }

    @Test
    public void testPolicyCalculationPeriod3() throws CalculationFailed {
	Policy policy = generatePolicy();
	InsurancePeriodData period = new InsurancePeriodData();
	period.setFrom(LocalDate.of(2016, 10, 26));
	period.setTo(period.getFrom().plus(1, ChronoUnit.YEARS));
	policy.setPeriod(period);
	calc.calculatePolicyCost(policy);
	assertThat(policy.getCalculation().getAmount(), equalTo(30804d));
	assertThat(policy.getCalculation().getCurrency(),
		allOf(not(nullValue()), equalTo(Currency.getInstance("KZT"))));
    }

    @Test
    public void testPolicyCalculationTemporaryEntry() throws CalculationFailed {
	Policy policy = generatePolicy();

	PolicyDriver drv = policy.getInsuredDrivers().iterator().next();
	drv.setAgeClass(InsuredAgeClass.OVER25);
	drv.setExpirienceClass(InsuredExpirienceClass.MORE2);
	drv.setInsuranceClassType(InsuranceClassType.CLASS_10);

	PolicyVehicle veh = policy.getInsuredVehicles().iterator().next();
	veh.setArea(null);
	veh.setCity(null);
	veh.setTemporaryEntry(true);
	veh.setVehicleAgeClass(VehicleAgeClass.OVER7);
	veh.setVehicleClass(VehicleClass.CAR);

	calc.calculatePolicyCost(policy);
	assertThat(policy.getCalculation().getAmount(), equalTo(43609d));
	assertThat(policy.getCalculation().getCurrency(),
		allOf(not(nullValue()), equalTo(Currency.getInstance("KZT"))));
    }

}
