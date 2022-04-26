package geektime.tdd.di;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ContainerTest {
    interface Component {};

    static class ComponentWithDefaultConstructor implements Component {
        public ComponentWithDefaultConstructor() {
        }
    }

    Context context;

    @BeforeEach
    public void setup() {
        context = new Context();
    }

    @Nested
    public class ComponentConstruction {
        // TODO: instance
        @Test
        public void should_bin_type_to_a_specific_instance() {
            Component instance = new Component() {};
            context.bind(Component.class, instance);
            assertSame(instance, context.get(Component.class));
        }

        // TODO: actract class
        // TODO: interface
        @Nested
        public class ConstructorInjection {
            // TODO: No args constructor
            @Test
            public void should_bind_type_to_a_class_with_default_constructor() {
                context.bind(Component.class, ComponentWithDefaultConstructor.class);
                Component instance = context.get(Component.class);
                assertNotNull(instance);
                assertTrue(instance instanceof ComponentWithDefaultConstructor);
            }

            // TODO: with dependencies

            // TODO: A -> B -> C

            // TODO: error
        }

        @Nested
        public class FieldInjection {

        }

        @Nested
        public class MethodInjection {

        }
    }

    @Nested
    public class DependenciesSelection {

    }

    @Nested
    public class LifecycleManagement {

    }
}
