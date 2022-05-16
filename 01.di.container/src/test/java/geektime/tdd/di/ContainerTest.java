package geektime.tdd.di;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

public class ContainerTest {
    ContextConfig config;

    @BeforeEach
    public void setup() {
        config = new ContextConfig();
    }

    @Nested
    public class DependenciesSelection {
        @Nested
        public class ProviderType {
            // Context
            // TODO: could get Provider<T> from context

            // InjectionProvider
            // TODO: support inject constructor
            // TODO: support inject field
            // TODO: support inject method
        }

        @Nested
        public class Qualifier {

        }
    }

    @Nested
    public class LifecycleManagement {
    }
}