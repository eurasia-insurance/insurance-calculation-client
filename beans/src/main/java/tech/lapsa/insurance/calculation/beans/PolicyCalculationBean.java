package tech.lapsa.insurance.calculation.beans;

import static tech.lapsa.insurance.calculation.beans.Calculations.*;
import static tech.lapsa.insurance.calculation.beans.PolicyRates.*;

import java.util.Currency;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.lapsa.insurance.domain.InsurancePeriodData;
import com.lapsa.insurance.domain.policy.Policy;
import com.lapsa.insurance.domain.policy.PolicyDriver;
import com.lapsa.insurance.domain.policy.PolicyVehicle;
import com.lapsa.insurance.elements.InsuranceClassType;
import com.lapsa.insurance.elements.InsuredAgeClass;
import com.lapsa.insurance.elements.InsuredExpirienceClass;
import com.lapsa.insurance.elements.PolicyTemporaryEntryTimeCategory;
import com.lapsa.insurance.elements.VehicleAgeClass;
import com.lapsa.insurance.elements.VehicleClass;
import com.lapsa.kz.country.KZArea;

import tech.lapsa.insurance.calculation.CalculationFailed;
import tech.lapsa.insurance.calculation.PolicyCalculation;
import tech.lapsa.insurance.calculation.PolicyCalculation.PolicyCalculationLocal;
import tech.lapsa.insurance.calculation.PolicyCalculation.PolicyCalculationRemote;

@Stateless(name = PolicyCalculation.BEAN_NAME)
public class PolicyCalculationBean implements PolicyCalculationLocal, PolicyCalculationRemote {

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public void calculatePolicyCost(final Policy policy) throws CalculationFailed {
	double cost = policyCost(policy.getInsuredDrivers(),
		policy.getInsuredVehicles(), policy.getPeriod());
	policy.getCalculation().setAmount(roundMoney(cost));
	policy.getCalculation().setCurrency(Currency.getInstance("KZT"));
    }

    // PRIVATE

    private static double policyCost(final List<PolicyDriver> drivers, final List<PolicyVehicle> vehicles,
	    final InsurancePeriodData period) throws CalculationFailed {
	double maximumCost = 0d;
	for (final PolicyDriver driver : drivers)
	    for (final PolicyVehicle vehicle : vehicles) {
		final double cost = policyCostVariant(driver, vehicle, period);
		if (maximumCost < cost)
		    maximumCost = cost;
	    }
	return maximumCost;
    }

    private static double policyCostVariant(final PolicyDriver insured, final PolicyVehicle vehicle,
	    final InsurancePeriodData period) throws CalculationFailed {

	double cost = 0d;

	{
	    // МРП
	    cost = getBase();
	}

	{
	    // базовая страховая премия
	    double rate = getBaseRate();
	    cost *= rate;
	}

	// Если временный въезд, то не учитывается:
	// - регион
	// - город (мажорность города)
	// - класс страхования (бонус-малус)
	// Gомимо этого, при временном въезде используется другая форула расчета
	// стоимости страхования на короткие сроки (до года)

	if (vehicle.isTemporaryEntry()) {

	    // для авто ВРЕМЕННО въезжающих на территорию РК (т.е. не состоящих
	    // на учете в РК)

	    {
		// коэффициент по временному въезду
		double rate = getTemporaryEntryRate();
		cost *= rate;
	    }

	    {
		// коэффициент по сроку страхования
		if (period != null) {
		    PolicyTemporaryEntryTimeCategory timeClass = detectTemporaryEntryTimeCategory(period);
		    double rate = getPolicyTemporaryEntryTimeCategoryRate(timeClass);
		    cost *= rate;
		}
	    }

	} else {

	    // для авто состоящих на учете РК

	    {
		// коэффициент по Региону
		double rate = getRegionRate(vehicle.getArea());
		cost *= rate;
	    }

	    {
		// коэффициент по городу регионального значения
		final Boolean isMajorCity;
		if (vehicle.getCity() != null) {
		    isMajorCity = isMajorCity(vehicle.getCity());
		} else {
		    if ((vehicle.getArea() == KZArea.GALM || vehicle.getArea() == KZArea.GAST)
			    && !vehicle.isForcedMajorCity())
			throw new CalculationFailed(String.format(
				"City for area %1$s must be major city or city must be set", vehicle.getArea().name()));
		    isMajorCity = vehicle.isForcedMajorCity();
		}

		if (!isMajorCity) {
		    double rate = getNonMajorCityCorrectionRate();
		    cost *= rate;
		}
	    }

	    {
		// коэффициент по классу страхователя (бонус-малус)
		double rate = getInsuranceClassTypeRate(insured.getInsuranceClassType());
		cost *= rate;
	    }
	}

	{
	    // коэффициент по типу ТС
	    double rate = getVehicleTypeRate(vehicle.getVehicleClass());
	    cost *= rate;
	}

	{
	    // коэффициент по "опыту" водителя (возраст/водительский стаж)
	    double rate = getDriverExpirienceTypeRate(insured.getAgeClass(), insured.getExpirienceClass());
	    cost *= rate;
	}

	{
	    // коэффициент по "возрасту" авто
	    double rate = getVehicleAgeTypeRate(vehicle.getVehicleAgeClass());
	    cost *= rate;
	}

	{
	    // скидка привелегированным (пенсионеры, участники, инвалиды)
	    if (insured.isHasAnyPrivilege()) {
		double rate = getPrivilegerRate();
		cost *= rate;
	    }
	}

	{
	    // неполный период страхования
	    if (period != null && !vehicle.isTemporaryEntry())
		cost = costAnnualToPeriod(cost, period);
	}

	// округляем до "копеек"
	cost = roundMoney(cost);

	return cost;
    }

    private static double getBase() {
	return MRP;
    }

    private static double getBaseRate() {
	return BASE_RATE;
    }

    private static double getPolicyTemporaryEntryTimeCategoryRate(
	    final PolicyTemporaryEntryTimeCategory contractTimeClass)
	    throws CalculationFailed {
	assertNotNull(contractTimeClass, PolicyTemporaryEntryTimeCategory.class);
	assertHasKey(contractTimeClass, POLICY_TEMPORARY_ENTRIY_TIME_CATEGORY_RATES,
		PolicyTemporaryEntryTimeCategory.class);
	return POLICY_TEMPORARY_ENTRIY_TIME_CATEGORY_RATES.get(contractTimeClass);
    }

    private static double getRegionRate(final KZArea region) throws CalculationFailed {
	assertNotNull(region, KZArea.class);
	assertHasKey(region, REGION_RATES, KZArea.class);
	return REGION_RATES.get(region);
    }

    private static double getVehicleTypeRate(final VehicleClass vehicleType) throws CalculationFailed {
	assertNotNull(vehicleType, VehicleClass.class);
	assertHasKey(vehicleType, VEHICLE_CLASS_RATES, VehicleClass.class);
	return VEHICLE_CLASS_RATES.get(vehicleType);
    }

    private static double getDriverExpirienceTypeRate(final InsuredAgeClass ageClass,
	    final InsuredExpirienceClass driverExpirienceClass) throws CalculationFailed {
	assertNotNull(ageClass, InsuredAgeClass.class);
	assertNotNull(driverExpirienceClass, InsuredExpirienceClass.class);
	assertHasKey(ageClass, INSURED_AGE_EXPIRIENCE_CLASS_RATES, InsuredAgeClass.class);
	assertHasKey(driverExpirienceClass, INSURED_AGE_EXPIRIENCE_CLASS_RATES.get(ageClass),
		InsuredExpirienceClass.class);
	return INSURED_AGE_EXPIRIENCE_CLASS_RATES.get(ageClass).get(driverExpirienceClass);
    }

    private static double getVehicleAgeTypeRate(final VehicleAgeClass vehicleAgeType) throws CalculationFailed {
	assertNotNull(vehicleAgeType, VehicleAgeClass.class);
	assertHasKey(vehicleAgeType, VEHICLE_AGE_CLASS_RATES, VehicleAgeClass.class);
	return VEHICLE_AGE_CLASS_RATES.get(vehicleAgeType);
    }

    private static double getInsuranceClassTypeRate(final InsuranceClassType insuranceClassType)
	    throws CalculationFailed {
	assertNotNull(insuranceClassType, InsuranceClassType.class);
	assertHasKey(insuranceClassType, INSURANCE_CLASS_TYPE_RATES, InsuranceClassType.class);
	return INSURANCE_CLASS_TYPE_RATES.get(insuranceClassType);
    }

    private static double getTemporaryEntryRate() {
	return TEMPORARY_ENTRY_RATE;
    }

    private static double getPrivilegerRate() throws CalculationFailed {
	return PRIVILEGER_RATE;
    }

    private static double getNonMajorCityCorrectionRate() throws CalculationFailed {
	return NON_MAJOR_CITY_CORRECTION_RATE;
    }
}
