package net.jqwik.docs.state;

import net.jqwik.api.*;
import net.jqwik.api.state.*;

import static org.assertj.core.api.Assertions.*;

import static net.jqwik.api.Arbitraries.*;

class RegexChainExample {

	@Property(tries = 10)
	void generateABPlusC(@ForAll("abplusc") Chain<String> regex) {
		String string = null;
		for (String value : regex) {
			string = value;
		}
		System.out.println(string);
		assertThat(string).matches("ab+c");
	}

	@Provide
	Arbitrary<Chain<String>> abplusc() {
		return Chain.initializeWith(() -> "")
					.provideTransformer(TransformerProvider.when(String::isEmpty).provide(just(s -> s + "a")))
					.provideTransformer(TransformerProvider.<String>when(s -> s.endsWith("a")).provide(just(s -> s + "b")))
					.provideTransformer(TransformerProvider.<String>when(s -> s.endsWith("b")).provide(
						 frequency(
							 Tuple.of(5, s -> s + "b"),
							 Tuple.of(1, s -> s + "c")
						 )
					 ))
					.provideTransformer(TransformerProvider.<String>when(s -> s.endsWith("c")).provide(just(Transformer.endOfChain())))
					.infinite().dontShrink();
	}

}
