package net.jqwik.kotlin.api

import net.jqwik.api.Arbitrary
import org.apiguardian.api.API

/**
 * Create a new arbitrary of the same type but inject null values with a probability of `nullProbability`.
 * <p>
 *     This is a type-safe version of {@linkplain Arbitrary.injectNull()}
 * </p>
 *
 * @param nullProbability the probability. &ge; 0 and  &le; 1.
 * @return a new arbitrary instance
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.6.0")
fun <T> Arbitrary<T>.orNull(nullProbability: Double): Arbitrary<T?> {
    return this.injectNull(nullProbability)
}
