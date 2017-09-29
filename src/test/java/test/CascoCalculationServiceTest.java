package test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static test.TestObjectsCreatorHelper.*;

import org.junit.Test;

import com.lapsa.insurance.calculation.CalculationFailed;
import com.lapsa.insurance.calculation.CascoCalculation;
import com.lapsa.insurance.domain.casco.Casco;
import com.lapsa.insurance.elements.CascoCarAgeClass;
import com.lapsa.insurance.elements.CascoDeductibleFullRate;
import com.lapsa.insurance.elements.CascoDeductiblePartialRate;

public class CascoCalculationServiceTest {

    @Test
    public void testCascoCalculationVariant1() throws CalculationFailed {
	Casco casco = generateCasco();
	CascoCalculation.calculateCascoCost(casco);
	assertThat(casco.getCalculation().getPremiumCost(), equalTo(282993.75d));
    }

    @Test
    public void testCascoCalculationVariant2() throws CalculationFailed {
	Casco casco = generateCasco();
	casco.setDeductiblePartialRequired(true);
	casco.setDeductiblePartialRate(CascoDeductiblePartialRate.PERCENT2);
	CascoCalculation.calculateCascoCost(casco);
	assertThat(casco.getCalculation().getPremiumCost(), equalTo(177375.00d));
    }

    @Test
    public void testCascoCalculationVariant3() throws CalculationFailed {
	Casco casco = generateCasco();
	casco.setDeductiblePartialRequired(true);
	casco.setDeductiblePartialRate(CascoDeductiblePartialRate.PERCENT10);
	casco.getInsuredVehicle().setCarAgeClass(CascoCarAgeClass.UNDER3);
	casco.setNoPoliceCallRequired(false);
	casco.setNoGuiltNoDeductibleRequired(false);
	casco.setDeductibleFullRate(CascoDeductibleFullRate.PERCENT10);
	casco.setReplacementCarRequired(false);

	casco.setCoverRoadAccidents(false);
	casco.setCoverNonRoadAccidents(true);

	CascoCalculation.calculateCascoCost(casco);
	assertThat(casco.getCalculation().getPremiumCost(), equalTo(50000.00d));
    }

}