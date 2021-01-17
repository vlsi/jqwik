package net.jqwik.engine.properties.configurators;

import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.stream.*;

import net.jqwik.api.*;
import net.jqwik.api.arbitraries.*;
import net.jqwik.api.configurators.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.providers.*;
import net.jqwik.engine.support.*;

public class UniquenessConfigurator implements ArbitraryConfigurator {

	private static final Logger LOG = Logger.getLogger(UniquenessConfigurator.class.getName());

	@SuppressWarnings("unchecked")
	@Override
	public <T> Arbitrary<T> configure(Arbitrary<T> arbitrary, TypeUsage targetType) {
		return targetType.findAnnotation(Uniqueness.class).map(uniqueness -> {
			if (arbitrary instanceof ListArbitrary) {
				return (Arbitrary<T>) configureListArbitrary((ListArbitrary<?>) arbitrary, uniqueness);
			}
			if (arbitrary instanceof SetArbitrary) {
				return (Arbitrary<T>) configureSetArbitrary((SetArbitrary<?>) arbitrary, uniqueness);
			}
			if (arbitrary instanceof ArrayArbitrary) {
				return (Arbitrary<T>) configureArrayArbitrary((ArrayArbitrary<?, ?>) arbitrary, uniqueness);
			}
			if (targetType.isAssignableFrom(List.class)) {
				Arbitrary<List<?>> listArbitrary = (Arbitrary<List<?>>) arbitrary;
				return (Arbitrary<T>) listArbitrary.filter(l -> isUnique(l, extractor(uniqueness)));
			}
			if (targetType.isAssignableFrom(Set.class)) {
				Arbitrary<Set<?>> setArbitrary = (Arbitrary<Set<?>>) arbitrary;
				return (Arbitrary<T>) setArbitrary.filter(l -> isUnique(l, extractor(uniqueness)));
			}
			if (targetType.isArray()) {
				Arbitrary<Object[]> arrayArbitrary = (Arbitrary<Object[]>) arbitrary;
				return (Arbitrary<T>) arrayArbitrary.filter(array -> isUnique(Arrays.asList(array), extractor(uniqueness)));
			}
			return arbitrary;
		}).orElse(arbitrary);
	}

	private boolean isUnique(Collection<?> list, Function<Object, Object> extractor) {
		Set<Object> set = list.stream().map(extractor).collect(Collectors.toSet());
		return set.size() == list.size();
	}

	private Arbitrary<?> configureListArbitrary(ListArbitrary<?> arbitrary, Uniqueness uniqueness) {
		Function<Object, Object> extractor = extractor(uniqueness);
		return arbitrary.uniqueness(extractor::apply);
	}

	private Arbitrary<?> configureSetArbitrary(SetArbitrary<?> arbitrary, Uniqueness uniqueness) {
		Function<Object, Object> extractor = extractor(uniqueness);
		return arbitrary.uniqueness(extractor::apply);
	}

	private Arbitrary<?> configureArrayArbitrary(ArrayArbitrary<?, ?> arbitrary, Uniqueness uniqueness) {
		Function<Object, Object> extractor = extractor(uniqueness);
		return arbitrary.uniqueness(extractor::apply);
	}

	private Function<Object, Object> extractor(Uniqueness uniqueness) {
		Class<? extends Function<Object, Object>> extractorClass = uniqueness.by();
		return extractorClass.equals(Uniqueness.NOT_SET.class)
				? Function.identity()
				// TODO: Create instance in context of test instance.
				//       This requires an extension of ArbitraryConfiguration interface
				//       to provide access to PropertyLifecycleContext
				: JqwikReflectionSupport.newInstanceWithDefaultConstructor(extractorClass);
	}

}
