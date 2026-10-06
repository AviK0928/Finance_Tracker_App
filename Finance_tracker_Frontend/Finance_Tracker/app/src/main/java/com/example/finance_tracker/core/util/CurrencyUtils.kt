package com.example.finance_tracker.core.util

import java.math.BigDecimal

// Money is NUMERIC(19,2) on the backend. Amounts are BigDecimal end to end, never Double.

/** True when the value needs more than 2 decimal places: "10.50" and "10.500" do not, "10.555" does. */
fun BigDecimal.hasMoreThanTwoDecimals(): Boolean = stripTrailingZeros().scale() > 2

/** The value at scale 2, as the backend stores it. Only call it when [hasMoreThanTwoDecimals] is false. */
fun BigDecimal.toMoney(): BigDecimal = setScale(2)
