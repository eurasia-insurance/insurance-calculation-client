package test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static test.TestObjectsCreatorHelper.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
import tech.lapsa.insurance.calculation.PolicyCalculation;

public class PolicyCalculationServiceTest {

    @Test
    public void testPolicyCalculationVariant1() throws CalculationFailed {
	Policy policy = generatePolicy();
	PolicyCalculation.calculatePolicyCost(policy);
	assertThat(policy.getCalculation().getPremiumCost(), equalTo(30804.07d));
    }

    @Test
    public void testPolicyCalculationPeriod1() throws CalculationFailed {
	Policy policy = generatePolicy();
	InsurancePeriodData period = new InsurancePeriodData();
	period.setFrom(LocalDate.of(2016, 10, 26));
	period.setTo(period.getFrom().plusDays(365));
	policy.setPeriod(period);
	PolicyCalculation.calculatePolicyCost(policy);
	assertThat(policy.getCalculation().getPremiumCost(), equalTo(30804.07d));
    }

    @Test
    public void testPolicyCalculationPeriod2() throws CalculationFailed {
	Policy policy = generatePolicy();
	InsurancePeriodData period = new InsurancePeriodData();
	period.setFrom(LocalDate.of(2016, 10, 26));
	period.setTo(period.getFrom().plusMonths(1));
	policy.setPeriod(period);
	PolicyCalculation.calculatePolicyCost(policy);
	assertThat(policy.getCalculation().getPremiumCost(), equalTo(2693.25d));
    }

    @Test
    public void testPolicyCalculationPeriod3() throws CalculationFailed {
	Policy policy = generatePolicy();
	InsurancePeriodData period = new InsurancePeriodData();
	period.setFrom(LocalDate.of(2016, 10, 26));
	period.setTo(period.getFrom().plus(1, ChronoUnit.YEARS));
	policy.setPeriod(period);
	PolicyCalculation.calculatePolicyCost(policy);
	assertThat(policy.getCalculation().getPremiumCost(), equalTo(30804.07d));
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

	PolicyCalculation.calculatePolicyCost(policy);
	assertThat(policy.getCalculation().getPremiumCost(), equalTo(43609.36d));
    }

}
