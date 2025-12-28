package me.bubner.zeusmonitor.util

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

/**
 * Math utilities.
 *
 * [Source](https://github.com/Murray-Bridge-Bunyips/BunyipsLib/blob/39f628e7d71113586cb98aaf0f106318c7b63a39/src/main/java/au/edu/sa/mbhs/studentrobotics/bunyipslib/external/Mathf.kt)
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

    /**
     * Returns value clamped between low and high boundaries.
     *
     * @param low   The lower boundary to which to clamp value.
     * @param high  The higher boundary to which to clamp value.
     * @return The clamped value.
     */
    @JvmStatic
    fun Number.clamp(low: Number, high: Number): Float {
        return max(low.toFloat(), min(this.toFloat(), high.toFloat()))
    }

    /**
     * Returns value clamped between low and high boundaries.
     *
     * @param range The range to which to clamp value.
     * @return The clamped value.
     */
    @JvmStatic
    infix fun Number.clamp(range: ClosedFloatingPointRange<Float>): Float {
        return clamp(range.start, range.endInclusive)
    }

    /**
     * Perform linear interpolation between two values.
     *
     * @param startValue The value to start at.
     * @param endValue   The value to end at.
     * @param t          How far between the two values to interpolate. This is clamped to [0, 1].
     * @return The interpolated value.
     */
    @JvmStatic
    fun lerp(startValue: Number, endValue: Number, t: Number): Float {
        return startValue.toFloat() + (endValue.toFloat() - startValue.toFloat()) * (t clamp (0f..1f))
    }

    /**
     * Perform linear interpolation between two values.
     *
     * @param t How far between the two values to interpolate. This is clamped to [0, 1].
     * @return The interpolated value.
     */
    @JvmStatic
    infix fun ClosedFloatingPointRange<Float>.lerp(t: Number): Float {
        return lerp(this.start, this.endInclusive, t)
    }
}