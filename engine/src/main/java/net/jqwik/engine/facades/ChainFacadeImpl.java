package net.jqwik.engine.facades;

import java.util.*;
import java.util.function.*;

import net.jqwik.api.Tuple.*;
import net.jqwik.api.state.*;
import net.jqwik.engine.properties.state.*;

/**
 * Is loaded through reflection in api module
 */
public class ChainFacadeImpl extends Chain.ChainFacade {

	@Override
	public <T> ChainArbitrary<T> initializeChainWith(Supplier<? extends T> initialSupplier) {
		return new DefaultChainArbitrary<>(initialSupplier);
	}

}
