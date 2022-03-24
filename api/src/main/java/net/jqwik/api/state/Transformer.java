package net.jqwik.api.state;

import java.util.function.*;

import org.apiguardian.api.*;
import org.jetbrains.annotations.*;

import static org.apiguardian.api.API.Status.*;

/**
 * A transformer is used to transform a state of type {@code T} into another value of this type.
 * Transformation can create a new object, or change the inner state of an object and return it.
 *
 * @param <T> The type of state to be transformed in a chain
 * @see Chain
 * @see TransformerProvider
 */
@FunctionalInterface
@API(status = EXPERIMENTAL, since = "1.7.0")
public interface Transformer<T> extends Function<@NotNull T, @NotNull T> {

	Transformer<?> END_OF_CHAIN = new Transformer<Object>() {
		@Override
		public @NotNull Object apply(@NotNull Object t) {
			return t;
		}

		@Override
		public String transformation() {
			return "End of Chain";
		}
	};

	/**
	 * Use this transformer to stop further enhancement of a chain.
	 * @param <T> The transformer's state value type
	 * @return a transformer instance
	 */
	@SuppressWarnings("unchecked")
	static <T> Transformer<T> endOfChain() {
		return (Transformer<T>) END_OF_CHAIN;
	}

	/**
	 * Create a transformer with a description
	 *
	 * @param description The text to describe what the transformer is doing
	 * @param mutator The actual transforming function
	 * @param <S> The type of the state to transform
	 * @return a new instance of a transformer
	 */
	static <S> Transformer<S> transform(String description, Function<S, S> mutator) {
		return new Transformer<S>() {
			@Override
			public S apply(S s) {
				return mutator.apply(s);
			}

			@Override
			public String transformation() {
				return description;
			}
		};
	}

	/**
	 * Describe the transformation this {@linkplain Transformer} is doing in a human understandable way
	 *
	 * @return non-empty String
	 */
	default String transformation() {
		return toString();
	}
}