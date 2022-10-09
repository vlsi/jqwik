package net.jqwik.engine.facades;

import java.util.*;
import java.util.function.*;

import net.jqwik.api.*;
import net.jqwik.engine.properties.arbitraries.combinations.*;

/**
 * Is loaded through reflection in api module
 */
public class CombinatorsFacadeImpl extends Combinators.CombinatorsFacade {

	@Override
	public <T1, T2> Combinators.Combinator2<T1, T2> combine2(Arbitrary<T1> a1, Arbitrary<T2> a2) {
		return new DefaultCombinator2<>(a1, a2);
	}

	@Override
	public <R> Arbitrary<R> combine(Function<List<Object>, R> combinator, Arbitrary<?>... arbitraries) {
		return new CombineArbitrary<>(combinator, arbitraries);
	}

}
