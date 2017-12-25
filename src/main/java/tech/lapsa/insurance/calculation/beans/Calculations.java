package tech.lapsa.insurance.calculation.beans;

import java.time.LocalDate;
import java.time.Period;
import java.util.Map;

import com.lapsa.insurance.domain.InsurancePeriodData;
import com.lapsa.insurance.elements.PolicyTemporaryEntryTimeCategory;
import com.lapsa.kz.country.KZCity;

final class Calculations {

    private Calculations() {
    }

    static double roundMoney(final double input, final int digs) {
	double output = input;
	output *= Math.pow(10, digs);
	output = Math.round(output);
	output /= Math.pow(10, digs);
	return output;
    }

    static double roundMoney(final double input) {
	return roundMoney(input, 0);
    }

    static double costAnnualToPeriod(final double annual, InsurancePeriodData period) throws CalculationFailed {
	assertNotNull(period, InsurancePeriodData.class);
	double periodCost = costAnnualToPeriod(annual, period.getFrom(), period.getTo());
	periodCost = roundMoney(periodCost);
	return periodCost;
    }

    static boolean isMajorCity(final KZCity city) throws CalculationFailed {
	assertNotNull(city, KZCity.class);
	if (city.equals(KZCity.OTHER))
	    return false;
	switch (city.getType()) {
	case MAJOR:
	case REGIONAL_CENTER:
	case REGIONAL_SUBORDINATION:
	    return true;
	case DISTINCT_CENTER:
	case DISTINCT_SUBORDINATION:
	    return false;
	default:
	}
	throw new CalculationFailed("Invalid city type or null");
    }

    static PolicyTemporaryEntryTimeCategory detectTemporaryEntryTimeCategory(InsurancePeriodData period)
	    throws CalculationFailed {
	assertNotNull(period, InsurancePeriodData.class);
	assertNotNull(period.getFrom(), "period FROM must not be null");
	assertNotNull(period.getTo(), "period TO must not be null");

	Period per = period.getFrom().until(period.getTo());

	if (per.getYears() > 0)
	    return PolicyTemporaryEntryTimeCategory.FROM_9M;

	int days = per.getDays();
	int months = per.getMonths();

	if (months == 0 && days <= 15)
	    return PolicyTemporaryEntryTimeCategory.TO_15D_INCL;

	if (months == 0 || (months == 1 && days == 0))
	    return PolicyTemporaryEntryTimeCategory.FROM_16D_TO_1M_INCL;

	if (months == 1 || (months == 2 && days == 0))
	    return PolicyTemporaryEntryTimeCategory.FROM_1M_TO_2M_INCL;

	if (months == 2 || (months == 3 && days == 0))
	    return PolicyTemporaryEntryTimeCategory.FROM_2M_TO_3M_INCL;

	if (months == 3 || (months == 4 && days == 0))
	    return PolicyTemporaryEntryTimeCategory.FROM_3M_TO_4M_INCL;

	if (months == 4 || (months == 5 && days == 0))
	    return PolicyTemporaryEntryTimeCategory.FROM_4M_TO_5M_INCL;

	if (months == 5 || (months == 6 && days == 0))
	    return PolicyTemporaryEntryTimeCategory.FROM_5M_TO_6M_INCL;

	if (months == 6 || (months == 7 && days == 0))
	    return PolicyTemporaryEntryTimeCategory.FROM_6M_TO_7M_INCL;

	if (months == 7 || (months == 8 && days == 0))
	    return PolicyTemporaryEntryTimeCategory.FROM_7M_TO_8M_INCL;

	if (months == 8 || (months == 9 && days == 0))
	    return PolicyTemporaryEntryTimeCategory.FROM_8M_TO_9M_INCL;

	return PolicyTemporaryEntryTimeCategory.FROM_9M;
    }

    static double costAnnualToPeriod(final double annualCost, final LocalDate start, final LocalDate end)
	    throws CalculationFailed {
	assertNotNull(start, "Period FROM must not be null");
	assertNotNull(end, "Period TO must not be null");

	if (start.isAfter(end))
	    throw new CalculationFailed("Start date is after the end date");
	double cost = 0;
	LocalDate c = start;
	while (c.isBefore(end) || c.isEqual(end)) {
	    cost += annualCost / (start.getChronology().isLeapYear(start.getYear()) ? 366 : 365);
	    c = c.plusDays(1);
	}
	return cost;
    }

    static double costAnnualToPeriod(final double annualCost, final LocalDate start, final int days)
	    throws CalculationFailed {
	final LocalDate end = start.plusDays(days);
	return costAnnualToPeriod(annualCost, start, end);
    }

    static <T> void assertHasKey(final T value, final Map<T, ?> map, final Class<T> clazz)
	    throws CalculationFailed {
	if (!map.containsKey(value))
	    throw new CalculationFailed(String.format("%1$s has invalid value", clazz.getSimpleName()));
    }

    static <T> void assertNotNull(final T value, final Class<T> clazz) throws CalculationFailed {
	assertNotNull(value, String.format("%1$s must not be null", clazz.getSimpleName()));
    }

    static <T> void assertNotNull(final T value, String message) throws CalculationFailed {
	if (value == null)
	    throw new CalculationFailed(String.format(message));
    }
}
