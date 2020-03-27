package net.jqwik.engine.properties;

import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;

import static org.assertj.core.api.Assertions.*;

@AddLifecycleHook(ResolveIntsTo41.class)
class ResolvingParametersInConstructorTests {

	private int shouldBe41;

	public ResolvingParametersInConstructorTests(int shouldBe41) {
		assertThat(ResolveIntsTo41.currentContext.optionalContainerClass().get())
			.isIn(
				ResolvingParametersInConstructorTests.class,
				ResolvingParametersInConstructorTests.Inner.class
			);
		this.shouldBe41 = shouldBe41;
	}

	@Example
	void example() {
		assertThat(shouldBe41).isEqualTo(41);
	}

	@Group
	@AddLifecycleHook(ResolveIntsTo41.class)
	class Inner {
		private int inner41;

		public Inner(int inner41) {
			assertThat(ResolveIntsTo41.currentContext.optionalContainerClass().get())
				.isEqualTo(ResolvingParametersInConstructorTests.Inner.class);
			this.inner41 = inner41;
		}

		@Example
		void innerExample() {
			assertThat(shouldBe41).isEqualTo(41);
			assertThat(inner41).isEqualTo(41);
		}
	}
}

class ResolveIntsTo41 implements ResolveParameterHook {

	static LifecycleContext currentContext;

	@Override
	public void prepareFor(LifecycleContext context) {
		currentContext = context;
	}

	@Override
	public Optional<ParameterSupplier> resolve(ParameterResolutionContext parameterContext) {
		if (parameterContext.typeUsage().isOfType(int.class)) {
			return Optional.of(lifecycleContext -> {
				assertThat(lifecycleContext).isInstanceOf(ContainerLifecycleContext.class);
				return 41;
			});
		}
		return Optional.empty();
	}
}
