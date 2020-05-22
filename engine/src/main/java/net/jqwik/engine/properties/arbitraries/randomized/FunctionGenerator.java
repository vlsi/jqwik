package net.jqwik.engine.properties.arbitraries.randomized;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import net.jqwik.api.*;
import net.jqwik.api.Tuple.*;
import net.jqwik.engine.*;
import net.jqwik.engine.support.*;

public class FunctionGenerator<F, R> extends AbstractFunctionGenerator<F, R> {

	private final AtomicReference<Shrinkable<R>> lastResult = new AtomicReference<>();

	public FunctionGenerator(
		Class<F> functionalType,
		RandomGenerator<R> resultGenerator,
		List<Tuple2<Predicate<List<Object>>, Function<List<Object>, R>>> conditions
	) {
		super(functionalType, resultGenerator, conditions);
	}

	@Override
	public Shrinkable<F> next(Random random) {
		return new ShrinkableFunction(createFunction(random));
	}

	private F createFunction(Random random) {
		long baseSeed = random.nextLong();
		InvocationHandler handler = (proxy, method, args) -> {
			if (JqwikReflectionSupport.isToStringMethod(method)) {
				return handleToString(baseSeed);
			}
			if (JqwikReflectionSupport.isHashCodeMethod(method)) {
				return (int) baseSeed;
			}
			if (method.isDefault()) {
				return handleDefaultMethod(proxy, method, args);
			}
			return conditionalResult(args).orElseGet(() -> {
				Random randomForArgs = SourceOfRandomness.newRandom(seedForArgs(baseSeed, args));
				Shrinkable<R> shrinkableResult = resultGenerator.next(randomForArgs);
				storeLastResult(shrinkableResult);
				return new Object[]{shrinkableResult.value()};
			})[0];
		};
		return createFunctionProxy(handler);
	}

	private Object handleToString(final long baseSeed) {
		return String.format(
			"Function<%s>(baseSeed: %s)",
			functionalType.getSimpleName(),
			baseSeed
		);
	}

	private Object handleDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {

		throw new RuntimeException("Cannot (yet) handle default methods");

		// see: https://github.com/pholser/junit-quickcheck/pull/265/files
		// for potential solution

		// Constructor<MethodHandles.Lookup> METHOD_LOOKUP_CTOR =
		// 	JqwikReflectionSupport.findDeclaredConstructor(
		// 		MethodHandles.Lookup.class,
		// 		Class.class,
		// 		int.class);
		//
		// MethodHandles.Lookup lookup =
		// 	METHOD_LOOKUP_CTOR.newInstance(
		// 		method.getDeclaringClass(),
		// 		MethodHandles.Lookup.PRIVATE);
		// return lookup.unreflectSpecial(method, method.getDeclaringClass())
		// 			 .bindTo(proxy)
		// 			 .invokeWithArguments(args);
	}

	private void storeLastResult(Shrinkable<R> result) {
		lastResult.set(result);
	}

	private long seedForArgs(long baseSeed, Object[] args) {
		long seed = baseSeed;
		if (args != null) {
			for (Object arg : args) {
				seed = Long.rotateRight(seed, 16);
				if (arg != null) {
					seed ^= arg.hashCode();
				}
			}
		}
		return seed;
	}

	private class ShrinkableFunction implements Shrinkable<F> {

		private final F value;

		private ShrinkableFunction(F function) {
			value = function;
		}

		@Override
		public F value() {
			return value;
		}

		@Override
		public ShrinkingSequence<F> shrink(Falsifier<F> falsifier) {
			if (lastResult.get() == null) {
				return ShrinkingSequence.dontShrink(this);
			}
			Shrinkable<F> constantFunction = createConstantFunction(lastResult.get());
			return ShrinkingSequence.startWith(constantFunction, falsifier);
		}

		@Override
		public ShrinkingDistance distance() {
			return ShrinkingDistance.MAX;
		}
	}
}
