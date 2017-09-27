package com.lapsa.insurance.calculation;

import static com.lapsa.commons.function.MyMaps.*;
import static com.lapsa.insurance.elements.CascoDeductiblePartialRate.*;

import java.util.Map;

import com.lapsa.insurance.elements.CascoDeductiblePartialRate;

final class CascoRates {

    private CascoRates() {
    }

    public static final double DISCOUNT_CONTRACT_ENDS_AFTER_FIRST_CASE = 0.25d; // 25%
    public static final double DISCOUNT_COVER_ROAD_ACCIDENTS_ONLY = 0.25d; // 25%
    public static final double DISCOUNT_COVER_NON_ROAD_ACCIDENTS_ONLY = 0.50d; // 50%
    public static final double DICOUNT_COVER_ALL_ACCIDENTS = 0.00d; // no
								    // discount

    public static final double AMOUNT_THIRD_PARTY_LIABILITY_COVER = 8000d;
    public static final double AMOUNT_DRIVER_COVERATE = 2000d;

    public static final double NO_DEDUCTIBLE_BASE_RATE = 0.0351d; // 3.51%

    public static final Map<CascoDeductiblePartialRate, Double> BASE_RATES = ofEntries(
	    entry(PERCENT0_5, 0.0260d), // 2.60%
	    entry(PERCENT1, 0.0240d), // 2,40%
	    entry(PERCENT2, 0.0220d), // 2,20%
	    entry(PERCENT3, 0.0200d), // 2,00%
	    entry(PERCENT5, 0.0180d), // 1,80%
	    entry(PERCENT10, 0.0160d) // 1,60%
    );

    public static final double RATES_DEDUCTIBLE_FULL_TO_5_PERCENT = 0.10d; // 10%
    public static final double RATES_SPECIAL_STATINONS_UPTO3 = 0.10d;// 10%
    public static final double RATES_SPECIAL_STATINONS_3TO7 = 0.20d; // 20%

    public static final double RATES_NO_POLICE_CALL_REQUIRED = 0.15d;// 15%

    public static final double RATES_NO_GUILT_NO_DEDUCTIBLE_REQUIRED = 0.20d;// 20%
    public static final double RATES_HELP_WITH_POLICY_REQUIRED = 0.10d;// 10%
    public static final double RATES_EVACUATOR_REQUIRED = 0.05d; // 5%
    public static final double RATES_REPLACEMENT_CAR_REQUIRED = 0.35d; // 35%
}