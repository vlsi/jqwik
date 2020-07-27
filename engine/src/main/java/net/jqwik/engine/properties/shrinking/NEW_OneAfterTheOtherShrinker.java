package net.jqwik.engine.properties.shrinking;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import net.jqwik.api.*;
import net.jqwik.api.Tuple.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.engine.properties.*;

class NEW_OneAfterTheOtherShrinker extends NEW_AbstractShrinker {

	@Override
	public FalsifiedSample shrink(
		Falsifier<List<Object>> falsifier,
		FalsifiedSample sample,
		Consumer<FalsifiedSample> shrinkSampleConsumer,
		Consumer<FalsifiedSample> shrinkAttemptConsumer
	) {
		FalsifiedSample current = sample;
		for (int i = 0; i < sample.size(); i++) {
			current = shrinkSingleParameter(falsifier, current, shrinkSampleConsumer, shrinkAttemptConsumer, i);
		}
		return current;
	}

	private FalsifiedSample shrinkSingleParameter(
		Falsifier<List<Object>> falsifier,
		FalsifiedSample sample,
		Consumer<FalsifiedSample> shrinkSampleConsumer,
		Consumer<FalsifiedSample> shrinkAttemptConsumer,
		int parameterIndex
	) {
		Function<List<Shrinkable<Object>>, Stream<List<Shrinkable<Object>>>> shrinker =
			shrinkables -> {
				Shrinkable<Object> shrinkable = shrinkables.get(parameterIndex);
				return shrinkable.shrink().map(s -> replaceIn(s, parameterIndex, sample.shrinkables()));
			};

		return shrink(
			falsifier,
			sample,
			shrinkSampleConsumer,
			shrinkAttemptConsumer,
			shrinker
		);
	}

	private <T> List<T> replaceIn(T object, int index, List<T> old) {
		List<T> newList = new ArrayList<>(old);
		newList.set(index, object);
		return newList;
	}

}