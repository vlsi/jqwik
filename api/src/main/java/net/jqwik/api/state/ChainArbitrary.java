package net.jqwik.api.state;

import net.jqwik.api.*;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL, since = "1.7.0")
public interface ChainArbitrary<T> extends Arbitrary<Chain<T>> {

	/**
	 * Set the intended number of steps of this chain.
	 */
	ChainArbitrary<T> ofMaxSize(int size);

}
