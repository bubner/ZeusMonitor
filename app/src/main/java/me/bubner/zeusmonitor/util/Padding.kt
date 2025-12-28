package me.bubner.zeusmonitor.util

import kotlin.math.max

/**
 * Pad a number to at least [n] decimal places in length (e.g. 0.5 with n=2 will return the string 0.50).
 *
 * Can be unstable with non-positive, erroneously large, or erroneously small numbers.
 */
infix fun Number.pad(n: Int) =
    toString().let { it + "0".repeat(max(0, n - it.substringAfter(".", "").length)) }