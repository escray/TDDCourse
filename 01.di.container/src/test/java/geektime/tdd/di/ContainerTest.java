package geektime.tdd.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ContainerTest {
    Context context;

    @BeforeEach
    public void setup() {
        context = new Context();
    }

    @Nested
    public class ComponentConstruction {
        // TODO: instance
        @Test
        public void should_bind_type_to_a_specific_instance() {
            Component instance = new Component() {};
            context.bind(Component.class, instance);
            assertSame(instance, context.get(Component.class).get());
        }

        // TODO: abstract class
        // TODO: interface


        @Test
        public void should_return_empty_if_component_not_defined() {
            assertThrows(DependencyNotFoundException.class, () -> context.get(Component.class).orElseThrow(DependencyNotFoundException::new));
            Optional<Component> component = context.get(Component.class);
            assertTrue(component.isEmpty());
        }

        @Nested
        public class ConstructorInjection {
            // DONE: No args constructor
            @Test
            public void should_bind_type_to_a_class_with_default_constructor() {
                context.bind(Component.class, ComponentWithDefaultConstructor.class);
                Component instance = context.get(Component.class).get();
                assertNotNull(instance);
                assertTrue(instance instanceof ComponentWithDefaultConstructor);
            }

            // DONE: with dependencies
            @Test
            public void should_bind_type_to_a_class_with_inject_constructor() {
                context.bind(Component.class, ComponentWithInjectConstructor.class);
                Dependency dependency = new Dependency() {

                };
                context.bind(Dependency.class, dependency);

                Component instance = context.get(Component.class).get();
                assertNotNull(instance);
                assertSame(dependency, ((ComponentWithInjectConstructor) instance).getDependency());
            }

            // DONE: A -> B -> C
            @Test
            public void should_bind_type_to_a_class_with_transitive_dependencies() {
                context.bind(Component.class, ComponentWithInjectConstructor.class);
                context.bind(Dependency.class, DependencyWithInjectConstructor.class);
                context.bind(String.class, "indirect dependency");

                Component instance = context.get(Component.class).get();
                assertNotNull(instance);

                Dependency dependency = ((ComponentWithInjectConstructor)instance).getDependency();
                assertNotNull(dependency);

                assertEquals("indirect dependency", ((DependencyWithInjectConstructor)dependency).getDependency());
            }

            // sad path, error condition
            // DONE: multi inject constructors
            @Test
            public void should_throw_exception_if_multi_inject_constructor_provided() {
                assertThrows(IllegalComponentException.class, () -> {
                    context.bind(Component.class, ComponentWithMultiInjectConstructors.class);
                });
            }

            // DONE: no default constructor and inject constructor
            @Test
            public void should_throw_exception_if_no_inject_nor_default_constructor_provider() {
                assertThrows(IllegalComponentException.class, () -> {
                    context.bind(Component.class, ComponentWithNoInjectConstructorNorDefaultConstructor.class);
                });
            }

            // TODO: dependencies not exist
            @Test
            public void should_throw_exception_if_dependency_not_found() {
                context.bind(Component.class, ComponentWithInjectConstructor.class);

                assertThrows(DependencyNotFoundException.class, () -> context.get(Component.class).get());
            }

            @Test
            public void should_throw_exception_if_cyclic_dependencies_found() {
                context.bind(Component.class, ComponentWithInjectConstructor.class);
                context.bind(Dependency.class, DependencyDependedOnComponent.class);

                assertThrows(CyclicDependenciesFound.class, () -> context.get(Component.class));

            }

            // A -> B -> C -> A
            @Test
            public void should_throw_exception_if_transitive_cyclic_dependencies() {
                context.bind(Component.class, ComponentWithInjectConstructor.class);
                context.bind(Dependency.class, DependencyDependedOnAnotherDependency.class);
                context.bind(AnotherDependency.class, AnotherDependencyDependedOnComponent.class);

                assertThrows(CyclicDependenciesFound.class, () -> context.get(Component.class));
            }
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