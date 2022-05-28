package net.jqwik.docs.contracts.eurocalc;

import java.util.*;

import org.assertj.core.api.*;
import org.assertj.core.data.*;

import net.jqwik.api.*;
import net.jqwik.api.arbitraries.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.domains.*;

@Group
@Label("Contract: RateProvider")
class RateProviderContractProperties {

	// This part is not used (yet) in jqwik.
	// Contracts should be automatically applied to all instances of fitting type that are generated by arbitraries:
	// - Preconditions, Postconditions, Invariants should be checked and throw a runtime error when violated
	@ContractFor(RateProvider.class)
	static class RateProviderContract {
		@ContractFor.Require
		boolean rate(
			@ConstrainedBy(CurrencyConstraint.class) String fromCurrency,
			@ConstrainedBy(CurrencyConstraint.class) String toCurrency
		) {
			return !fromCurrency.equals(toCurrency);
		}

		@ContractFor.Ensure
		boolean rate(String fromCurrency, String toCurrency, double result) {
			return result > 0.0;
		}

		@ContractFor.Invariant
		boolean anInvariant(RateProvider instance) {
			return true;
		}
	}

	// Constraints should be checked during precondition checking of a contract
	static class CurrencyConstraint implements Constraint<String> {

		private final Set<String> currencies = new LinkedHashSet<String>() {
			{
				add("EUR");
				add("USD");
				add("CHF");
			}
		};

		@Override
		public boolean isValid(String value) {
			return currencies.contains(value);
		}
	}

	interface RateProviderContractTests<E extends RateProvider> {

		@Property
		default boolean willReturnRateAboveZeroForValidCurrencies(
			@ForAll("currencies") String from,
			@ForAll("currencies") String to,
			@ForAll E provider) {
			return provider.rate(from, to) > 0.0;
		}

		@Property
		default void willThrowExceptionsForInvalidCurrencies(
			@ForAll("currencies") String from,
			@ForAll("invalid") String to,
			@ForAll E provider) {

			Assertions.assertThatThrownBy(() -> provider.rate(from, to)).isInstanceOf(IllegalArgumentException.class);
			Assertions.assertThatThrownBy(() -> provider.rate(to, from)).isInstanceOf(IllegalArgumentException.class);
		}

		@Provide
		default Arbitrary<String> currencies() {
			return Arbitraries.of("EUR", "USD", "CHF", "CAD");
		}

		@Provide
		default Arbitrary<String> invalid() {
			return Arbitraries.of("A", "", "XXX", "CADCAD");
		}

	}

	@Group
	@Label("SimpleRateProvider")
	@Domain(SimpleRateProviderTests.SimpleRateProviderDomain.class) //does not work here
	class SimpleRateProviderTests implements RateProviderContractTests<SimpleRateProvider> {
		@Provide
		Arbitrary<SimpleRateProvider> rateProvider() {
			return Arbitraries.just(new SimpleRateProvider());
		}

		class SimpleRateProviderDomain extends DomainContextBase {
			@Provide
			Arbitrary<SimpleRateProvider> simpleRateProviders() {
				return Arbitraries.just(new SimpleRateProvider());
			}
		}

	}

	@Group
	@Label("Collaborator: EuroConverter")
	class EuroConverterCollaborationTests {

		@Property
		boolean willAlwaysConvertToPositiveEuroAmount(
			@ForAll("nonEuroCurrencies") String from,
			@ForAll @DoubleRange(min = 0.01, max = 1000000.0) double amount,
			@ForAll("rateProvider") RateProvider provider
		) {

			double euroAmount = new EuroConverter(provider).convert(amount, from);
			return euroAmount > 0.0;
		}

		@Example
		void willCorrectlyUseExchangeRate() {
			RateProvider provider = (fromCurrency, toCurrency) -> 0.8;
			double euroAmount = new EuroConverter(provider).convert(8.0, "USD");
			Assertions.assertThat(euroAmount).isCloseTo(6.4, Offset.offset(0.01));
		}

		@Provide
		Arbitrary<String> nonEuroCurrencies() {
			return Arbitraries.of("USD", "CHF", "CAD");
		}

		@Provide
		Arbitrary<RateProvider> rateProvider() {
			DoubleArbitrary rate = Arbitraries.doubles().between(0.1, 10.0);
			return rate.map(exchangeRate -> (fromCurrency, toCurrency) -> {
				Assertions.assertThat(fromCurrency).isNotEqualTo(toCurrency);
				return exchangeRate;
			});
		}
	}

}
