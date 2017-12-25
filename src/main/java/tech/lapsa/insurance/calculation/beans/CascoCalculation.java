package tech.lapsa.insurance.calculation;

import static tech.lapsa.insurance.calculation.CascoRates.*;

import java.util.Currency;

import com.lapsa.insurance.domain.casco.Casco;
import com.lapsa.insurance.elements.CascoCarAgeClass;
import com.lapsa.insurance.elements.CascoDeductibleFullRate;
import com.lapsa.insurance.elements.CascoDeductiblePartialRate;

public final class CascoCalculation {

    public static void calculateCascoCost(final Casco casco) throws CalculationFailed {
	double cost = cascoCostAnnual(casco);

	casco.getCalculation().setAmount(cost);
	casco.getCalculation().setCurrency(Currency.getInstance("KZT"));
    }

    // PRIVATE

    private CascoCalculation() {
    }

    private static double cascoCostAnnual(final Casco casco) throws CalculationFailed {

	// insurance rate
	final double base = getBaseRate(casco.isDeductiblePartialRequired(),
		casco.getDeductiblePartialRate());
	double rate = base;
	rate += base * getIncrRateSpecialServiceStation(casco.isSpecialServiceStationRequired(),
		casco.getInsuredVehicle().getCarAgeClass());

	rate += base * getIncrRateNoPoliceCallRequired(casco.isNoPoliceCallRequired());
	rate += base * getIncrRateNoGuiltNoDeductibleRequired(casco.isNoGuiltNoDeductibleRequired());
	rate += base * getIncrRateDeductibleFull(casco.getDeductibleFullRate());
	rate += base * getIncrRateHelpWithPoliceRequired(casco.isHelpWithPoliceRequired());
	rate += base * getIncrRateEvacuatorRequired(casco.isEvacuatorRequired());
	rate += base * getIncrRateReplacementCarRequired(casco.isReplacementCarRequired());

	double cost = casco.getInsuredVehicle().getCost() * rate;

	// discount
	double discount = 0;
	discount += getDiscountCoverage(casco.isCoverRoadAccidents(),
		casco.isCoverNonRoadAccidents());
	discount += getDiscountContractEndsAfterFirstCase(casco.isContractEndsAfterFirstCase());

	cost = cost * (1 - discount);

	// complex insurance
	cost += getAmountThirdPartyLiabilityCoverage(casco.isThirdPartyLiabilityCoverage());
	cost += getAmountDriverAndPassengerCoverage(casco.isDriverAndPassengerCoverage(),
		casco.getDriverAndPassengerCount() == null ? 0 : casco.getDriverAndPassengerCount());

	if (casco.getPeriod() != null)
	    cost = Calculations.costAnnualToPeriod(cost, casco.getPeriod());

	cost = Calculations.roundMoney(cost);

	return cost;
    }

    private static double getBaseRate(final boolean deductible, final CascoDeductiblePartialRate deductibleValue)
	    throws CalculationFailed {
	if (!deductible)
	    return NO_DEDUCTIBLE_BASE_RATE;
	if (!BASE_RATES.containsKey(deductibleValue))
	    throw new CalculationFailed(String.format("No rate for '%1$s'", deductibleValue));
	return BASE_RATES.get(deductibleValue);
    }

    private static double getIncrRateDeductibleFull(final CascoDeductibleFullRate deductibleFullDeathRate)
	    throws CalculationFailed {
	switch (deductibleFullDeathRate) {
	case PERCENT5:
	    return RATES_DEDUCTIBLE_FULL_TO_5_PERCENT;
	case PERCENT10:
	    return 0;
	default:
	    throw new CalculationFailed(String.format("No rate for '%1$s'", deductibleFullDeathRate));
	}
    }

    private static double getIncrRateSpecialServiceStation(final boolean specialServiceStationRequired,
	    final CascoCarAgeClass carAgeClass) throws CalculationFailed {
	if (!specialServiceStationRequired)
	    return 0;
	switch (carAgeClass) {
	case UNDER3:
	    return RATES_SPECIAL_STATINONS_UPTO3;
	case FROM3TO7:
	    return RATES_SPECIAL_STATINONS_3TO7;
	default:
	    throw new CalculationFailed(String.format("No rate for '%1$s'", carAgeClass));
	}
    }

    private static double getIncrRateNoPoliceCallRequired(final boolean noPoliceRequired) throws CalculationFailed {
	if (!noPoliceRequired)
	    return 0;
	return RATES_NO_POLICE_CALL_REQUIRED;
    }

    private static double getIncrRateNoGuiltNoDeductibleRequired(
	    final boolean noGuiltNoDeductibleRequired) throws CalculationFailed {
	if (!noGuiltNoDeductibleRequired)
	    return 0;
	return RATES_NO_GUILT_NO_DEDUCTIBLE_REQUIRED;
    }

    private static double getIncrRateHelpWithPoliceRequired(final boolean helpWithPoliceRequired)
	    throws CalculationFailed {
	if (!helpWithPoliceRequired)
	    return 0;
	return RATES_HELP_WITH_POLICY_REQUIRED;
    }

    private static double getIncrRateEvacuatorRequired(final boolean evacuatorRequired) throws CalculationFailed {
	if (!evacuatorRequired)
	    return 0;
	return RATES_EVACUATOR_REQUIRED;
    }

    private static double getIncrRateReplacementCarRequired(final boolean replacementCarRequired)
	    throws CalculationFailed {
	if (!replacementCarRequired)
	    return 0;
	return RATES_REPLACEMENT_CAR_REQUIRED;
    }

    private static double getDiscountCoverage(final boolean coverRoadAccidents,
	    final boolean coverNonRoadAccidents) throws CalculationFailed {
	if (coverRoadAccidents && !coverNonRoadAccidents)
	    return DISCOUNT_COVER_ROAD_ACCIDENTS_ONLY;
	if (!coverRoadAccidents && coverNonRoadAccidents)
	    return DISCOUNT_COVER_NON_ROAD_ACCIDENTS_ONLY;
	if (coverRoadAccidents && coverNonRoadAccidents)
	    return DICOUNT_COVER_ALL_ACCIDENTS;
	throw new CalculationFailed(
		"Casco cost calculation failed. At least one of the coverage types required: Road Accidents non-Road Accidents");
    }

    private static double getDiscountContractEndsAfterFirstCase(
	    final boolean contractEndsAfterFirstCase) throws CalculationFailed {
	if (!contractEndsAfterFirstCase)
	    return 0;
	return DISCOUNT_CONTRACT_ENDS_AFTER_FIRST_CASE;
    }

    private static double getAmountThirdPartyLiabilityCoverage(final boolean thirdPartyLiabilityCoverage)
	    throws CalculationFailed {
	if (!thirdPartyLiabilityCoverage)
	    return 0;
	return AMOUNT_THIRD_PARTY_LIABILITY_COVER;
    }

    private static double getAmountDriverAndPassengerCoverage(final boolean driverAndPassengerCoverage,
	    final int driverAndPassengerCount)
	    throws CalculationFailed {
	if (!driverAndPassengerCoverage)
	    return 0;
	if (driverAndPassengerCount < 1)
	    throw new CalculationFailed("Invalid count of insured drivers/passengers");
	return AMOUNT_DRIVER_COVERATE * driverAndPassengerCount;
    }
}
