package me.bubner.zeusmonitor.util

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToLong

/**
 * Math utilities.
 *
 * @author Lucas Bubner, 2025
 */
object Math {
    /**
     * Round a number to a certain number of decimal points.
     *
     * @param thDigits The number of decimal places to use after the decimal point
     * @return The rounded number, or 0 if the number is null, or 0 if the number is null
     */
    infix fun Number?.round(thDigits: Int): Double {
        if (this == null) return 0.0
        val n = this.toDouble()
        if (n.isNaN()) return 0.0
        if (thDigits == 0)
            return n.roundToLong().toDouble()
        return BigDecimal(n.toString())
            .setScale(thDigits, RoundingMode.HALF_UP)
            .toDouble()
    }
}