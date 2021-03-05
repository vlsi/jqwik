package net.jqwik.time.internal.properties.configurators;

import java.time.*;

import net.jqwik.api.*;
import net.jqwik.api.configurators.*;
import net.jqwik.api.providers.*;
import net.jqwik.time.api.arbitraries.*;
import net.jqwik.time.api.constraints.*;

public class DateRangeForLocalDateConfigurator extends ArbitraryConfiguratorBase {

	@Override
	protected boolean acceptTargetType(TypeUsage targetType) {
		return targetType.isAssignableFrom(LocalDate.class);
	}

	public Arbitrary<?> configure(Arbitrary<?> arbitrary, DateRange range) {
		LocalDate min = isoDateToLocalDate(range.min());
		LocalDate max = isoDateToLocalDate(range.max());
		if (arbitrary instanceof LocalDateArbitrary) {
			LocalDateArbitrary localDateArbitrary = (LocalDateArbitrary) arbitrary;
			return localDateArbitrary.between(min, max);
		} else {
			return arbitrary.filter(v -> filter((LocalDate) v, min, max));
		}
	}

	private boolean filter(LocalDate date, LocalDate min, LocalDate max) {
		return !date.isBefore(min) && !date.isAfter(max);
	}

	public static LocalDate isoDateToLocalDate(String iso) {
		return LocalDate.parse(iso);
	}

}